#ifndef Header_SuperpoweredExample
#define Header_SuperpoweredExample

#include <math.h>
#include <pthread.h>
#include <stdio.h>

#include "SuperpoweredExample.h"
#include <SuperpoweredAdvancedAudioPlayer.h>
#include <SuperpoweredFilter.h>
#include <SuperpoweredRoll.h>
#include <SuperpoweredFlanger.h>
#include <AndroidIO/SuperpoweredAndroidAudioIO.h>
#include <SuperpoweredRecorder.h>
#include <SuperpoweredReverb.h>
#include <SuperpoweredLimiter.h>
#include <jni.h>
#include <SuperpoweredEcho.h>
#include "SuperpoweredMixer.h"
#include <SuperpoweredSimple.h>

#define HEADROOM_DECIBEL 3.0f
static const float headroom = powf(10.0f, -HEADROOM_DECIBEL * 0.025f);

class SuperpoweredExample {
public:

    SuperpoweredExample(unsigned int samplerate, unsigned int buffersize, const char *path);
    ~SuperpoweredExample();

    bool processA(short int *output, unsigned int numberOfSamples);
    void onPlayPause(bool play);
    void onFxReverbValue(int value);
    void onEchoValue(int value);
    void onVolume(int value, unsigned int numberOfSamples);
    bool applyEffect(const char *input, const char *output, const bool enableEffect);

private:
    SuperpoweredAndroidAudioIO *audioSystem;
    SuperpoweredAdvancedAudioPlayer *playerA;
    SuperpoweredReverb *reverb;
    SuperpoweredEcho *echo;
    float echoMix=0.5f, reverdMix=0.5f, volumnValue=1;
    float *stereoBuffer;
    unsigned char activeFx;
    float crossValue, volA;
};

#endif