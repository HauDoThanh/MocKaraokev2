package com.github.hiteshsondhi88.sampleffmpeg;

import android.content.Context;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import home.mockaraokev2.Service.ServiceCreateVideoFromImage;

@Module(
        injects = ServiceCreateVideoFromImage.class
)
@SuppressWarnings("unused")
public class ModuleCreateVideo {
    private final Context context;

    public ModuleCreateVideo(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    FFmpeg provideFFmpeg() {
        return FFmpeg.getInstance(context.getApplicationContext());
    }
}
