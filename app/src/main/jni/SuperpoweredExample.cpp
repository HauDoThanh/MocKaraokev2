#include "SuperpoweredExample.h"

#include <SuperpoweredCPU.h>
#include <android/log.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_AndroidConfiguration.h>
#include <malloc.h>
#include <SuperpoweredDecoder.h>

static void playerEventCallbackA(void *clientData, SuperpoweredAdvancedAudioPlayerEvent event,
                                 void *__unused value) {
    if (event == SuperpoweredAdvancedAudioPlayerEvent_LoadSuccess) {
        SuperpoweredAdvancedAudioPlayer *playerA = *((SuperpoweredAdvancedAudioPlayer **) clientData);
        playerA->setBpm(126.0f);
        playerA->setFirstBeatMs(353);
        playerA->setPosition(playerA->firstBeatMs, false, false);
    };
}

static bool audioProcessing(void *clientdata, short int *audioIO, int numberOfSamples,
                            int __unused samplerate) {
    return ((SuperpoweredExample *) clientdata)->processA(audioIO, (unsigned int) numberOfSamples);
}

SuperpoweredExample::SuperpoweredExample(unsigned int samplerate, unsigned int buffersize,
                                         const char *path)
        : activeFx(0),
          crossValue(0.0f),
          volA(1.0f * headroom) {
    stereoBuffer = (float *) memalign(16, (buffersize + 16) * sizeof(float) * 2);

    playerA = new SuperpoweredAdvancedAudioPlayer(&playerA, playerEventCallbackA, samplerate, 0);
    playerA->open(path);

    playerA->syncMode = SuperpoweredAdvancedAudioPlayerSyncMode_TempoAndBeat;
    echo = new SuperpoweredEcho(samplerate);
    reverb = new SuperpoweredReverb(samplerate);

    audioSystem = new SuperpoweredAndroidAudioIO(samplerate, buffersize, false, true,
                                                 audioProcessing, this, -1, SL_ANDROID_STREAM_MEDIA,
                                                 buffersize * 2);
}

SuperpoweredExample::~SuperpoweredExample() {
    delete audioSystem;
    delete playerA;
    free(stereoBuffer);
}

void SuperpoweredExample::onPlayPause(bool play) {
    if (!play) {
        playerA->pause();
    } else {
        playerA->play(false);
    };
    SuperpoweredCPU::setSustainedPerformanceMode(play); // <-- Important to prevent audio dropouts.
}

bool SuperpoweredExample::processA(short int *output, unsigned int numberOfSamples) {

    double masterBpm = playerA->currentBpm;

    bool silence = !playerA->process(stereoBuffer, false, numberOfSamples, volA, masterBpm,
                                     playerA->msElapsedSinceLastBeat);

    reverb->process(stereoBuffer, stereoBuffer, numberOfSamples);
    echo->process(stereoBuffer, stereoBuffer, numberOfSamples);
    SuperpoweredVolume(stereoBuffer, stereoBuffer, volumnValue, volumnValue, numberOfSamples);
    SuperpoweredFloatToShortInt(stereoBuffer, output, numberOfSamples);
    return !silence;
}

void SuperpoweredExample::onEchoValue(int value) {
    float scaleValue = (float) (0.01 * value);
    echoMix = scaleValue;

    __android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredExample", "echo: %0.2f",
                        scaleValue);
    echo->enable(true);
    echo->setMix(scaleValue);
}

void SuperpoweredExample::onFxReverbValue(int value) {
    float scaleValue = 0.01f * value;
    reverdMix = scaleValue;
    __android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredExample", "reverb: %0.2f",
                        scaleValue);
    reverb->enable(true);
    reverb->setWidth(scaleValue);
    reverb->setRoomSize(scaleValue);
}

void SuperpoweredExample::onVolume(int value, unsigned int numberOfSamples) {
    volumnValue = value;
    //SuperpoweredVolume(stereoBuffer, stereoBuffer, volumnValue, volumnValue, numberOfSamples);
    __android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredExample", "Volume: %d",
                        value);
}

static SuperpoweredExample *example = NULL;

extern "C" JNIEXPORT void
Java_home_mockaraokev2_Actitivy_Act_1MainAudio_SuperpoweredExample(JNIEnv *javaEnvironment,
                                                                 jobject __unused obj,
                                                                 jint samplerate, jint buffersize,
                                                                 jstring apkPath) {
    const char *path = javaEnvironment->GetStringUTFChars(apkPath, JNI_FALSE);
    example = new SuperpoweredExample((unsigned int) samplerate, (unsigned int) buffersize, path);
    javaEnvironment->ReleaseStringUTFChars(apkPath, path);
}

extern "C" JNIEXPORT void
Java_home_mockaraokev2_Actitivy_Act_1MainAudio_onPlayPause(JNIEnv *__unused javaEnvironment,
                                                         jobject __unused obj, jboolean play) {
    example->onPlayPause(play);
}

extern "C" JNIEXPORT void
Java_home_mockaraokev2_Actitivy_Act_1MainAudio_onFxReverbValue(JNIEnv *__unused javaEnvironment,
                                                             jobject __unused obj,
                                                             jint value) {
    example->onFxReverbValue(value);
}

extern "C" JNIEXPORT void
Java_home_mockaraokev2_Actitivy_Act_1MainAudio_onEcho(JNIEnv *__unused javaEnvironment,
                                                    jobject __unused obj,
                                                    jint mix) {
    example->onEchoValue(mix);
}
extern "C" JNIEXPORT void
Java_home_mockaraokev2_Actitivy_Act_1MainAudio_onVolume(JNIEnv *__unused javaEnvironment,
                                                      jobject __unused obj,
                                                      jint value, unsigned int numberOfSamples) {
    example->onVolume(value, numberOfSamples);
}

extern "C" JNIEXPORT void
Java_home_mockaraokev2_Actitivy_Act_1MainAudio_onWrite(JNIEnv *__unused javaEnvironment,
                                                     jobject __unused obj,
                                                     jstring input, jstring inputEffect,
                                                     jstring beat, jstring beatEffect,
                                                     jstring output) {
    const char *in = javaEnvironment->GetStringUTFChars(input, JNI_FALSE);
    const char *b = javaEnvironment->GetStringUTFChars(beat, JNI_FALSE);
    const char *inEffect = javaEnvironment->GetStringUTFChars(inputEffect, JNI_FALSE);
    const char *beEffect = javaEnvironment->GetStringUTFChars(beatEffect, JNI_FALSE);
    const char *out = javaEnvironment->GetStringUTFChars(output, JNI_FALSE);
    example->applyEffect(in, inEffect, true);

    example->applyEffect(b, beEffect, false);

}

bool SuperpoweredExample::applyEffect(const char *input, const char *output, const bool enableEffect) {

    SuperpoweredDecoder *decoder = new SuperpoweredDecoder();

    const char *openError = decoder->open(input, false);
    if (openError) {
        delete decoder;
        return false;
    };

    FILE *fd = createWAV(output, decoder->samplerate, 2);
    if (!fd) {
        delete decoder;
        return false;
    };

    SuperpoweredEcho *echoEffect = NULL;
    SuperpoweredReverb *reverbEffect = NULL;
    echoEffect = new SuperpoweredEcho(decoder->samplerate);
    reverbEffect = new SuperpoweredReverb(decoder->samplerate);

    if(enableEffect){
        echoEffect->setMix(echoMix);
        reverbEffect->setMix(reverdMix);
        echoEffect->enable(true);
        reverbEffect->enable(true);
    }

// Create a buffer for the 16-bit integer samples coming from the decoder.
    short int *intBuffer = (short int *) malloc(
            decoder->samplesPerFrame * 2 * sizeof(short int) + 16384);
// Create a buffer for the 32-bit floating point samples required by the effect.
    float *floatBuffer = (float *) malloc(decoder->samplesPerFrame * 2 * sizeof(float) + 1024);

    unsigned int samplesDecoded;
// Processing.
    while (true) {
        // Decode one frame. samplesDecoded will be overwritten with the actual decoded
        // number of samples.
        samplesDecoded = decoder->samplesPerFrame;
        if (decoder->decode(intBuffer, &samplesDecoded) == SUPERPOWEREDDECODER_ERROR) {
            break;
        }
        if (samplesDecoded < 1) {
            break;
        }

        // Apply the effect.

        // Convert the decoded PCM samples from 16-bit integer to 32-bit floating point.
        SuperpoweredShortIntToFloat(intBuffer, floatBuffer, samplesDecoded);
        if(enableEffect){
            echoEffect->process(floatBuffer, floatBuffer, samplesDecoded);
            reverbEffect->process(floatBuffer, floatBuffer, samplesDecoded);
            SuperpoweredVolume(floatBuffer, floatBuffer, volumnValue, volumnValue, samplesDecoded);
        }

        // Convert the PCM samples from 32-bit floating point to 16-bit integer.
        SuperpoweredFloatToShortInt(floatBuffer, intBuffer, samplesDecoded);
        fwrite(intBuffer, 1, samplesDecoded * 4, fd);
    }

    // Write the audio to disk.
    fflush(fd);
    fclose(fd);

    return true;
};