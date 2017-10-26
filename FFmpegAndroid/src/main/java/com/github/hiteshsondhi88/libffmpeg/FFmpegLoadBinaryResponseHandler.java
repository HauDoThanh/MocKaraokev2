package com.github.hiteshsondhi88.libffmpeg;

public interface FFmpegLoadBinaryResponseHandler extends ResponseHandler {

    /**
     * on Fail
     */
    void onFailure();

    /**
     * on Success
     */
    void onSuccess();

}
