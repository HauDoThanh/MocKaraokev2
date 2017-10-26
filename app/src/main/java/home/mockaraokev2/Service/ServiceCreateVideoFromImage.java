package home.mockaraokev2.Service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.github.hiteshsondhi88.sampleffmpeg.ModuleCreateVideo;

import java.io.File;

import javax.inject.Inject;

import dagger.ObjectGraph;
import home.mockaraokev2.Actitivy.Act_CreateVideoFromAudio;
import home.mockaraokev2.R;

import static home.mockaraokev2.Class.Constant.RECORD_PATH;
import static home.mockaraokev2.Class.Constant.VIDEO_PATH;

/**
 Created by admin on 10/2/2017.
 */

public class ServiceCreateVideoFromImage extends Service {
    @Inject
    public FFmpeg ffmpeg;

    private String fileOutput;
    private String nameOfBanner, input;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private int totalFrame, currentFrame;

    public ServiceCreateVideoFromImage() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ObjectGraph.create(new ModuleCreateVideo(this)).inject(this);
        loadLibrary();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getBundleExtra("valueCreateVideo");
        nameOfBanner = bundle.getString("nameOfBanner");
        input = bundle.getString("fileInput");
        fileOutput = nameOfBanner;
        Log.d("CREATE_VIDEO", "Name Receive From Acr_create: " + input);

        showNotification();
        return START_STICKY;
    }

    private void showNotification() {
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Tạo video")
                .setContentText("Đang xử lý video, vui lòng chờ!")
                .setSmallIcon(R.drawable.ic_tick)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        createVideo(nameOfBanner, input);
        //new Downloader().execute();
    }

    private void createVideo(final String nameOfBanner, final String input) {
        ffmpeg = FFmpeg.getInstance(this);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler());

            String[] cmd = new String[]{
                    "-loop",
                    "1",
                    "-i",
                    RECORD_PATH + "/" + nameOfBanner + ".png",
                    "-i",
                    RECORD_PATH + "/" + input,
                    "-r",
                    "2",
                    "-c:v",
                    "libx264",
                    "-profile:v",
                    "baseline",
                    "-level",
                    "3.0",
                    "-pix_fmt",
                    "yuv420p",
                    "-c:a",
                    "copy",
                    "-shortest",
                    VIDEO_PATH + "/" + nameOfBanner + ".mp4"
            };

            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                @Override
                public void onStart() {
                    super.onStart();
                    String[] cmd = new String[]{
                            "-i",
                            RECORD_PATH + "/" + input,
                            "-hide_banner"
                    };

                    try {
                        ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                            @Override
                            public void onFailure(String message) {
                                super.onFailure(message);
                                int indexDuration = message.indexOf("Duration: ") + 13;
                                String start = message.substring(indexDuration, indexDuration + 5);

                                String[] units = start.split(":");
                                int minutes = Integer.parseInt(units[0]); //second element
                                int seconds = Integer.parseInt(units[1]); //second element
                                totalFrame = (60 * minutes + seconds) * 2 + 49;

                                mBuilder.setProgress(totalFrame, currentFrame, false);
                                mNotifyManager.notify(1, mBuilder.build());
                            }
                        });

                    } catch (FFmpegCommandAlreadyRunningException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onSuccess(String message) {
                    Log.e("createVideo", "Success with output : " + message);
                    new File(RECORD_PATH + "/" + nameOfBanner + ".png").delete();
                }

                @Override
                public void onProgress(String message) {
                    int indexOfFrame = message.indexOf("frame=");
                    String valueProgress = "";
                    int value;
                    if (indexOfFrame > -1) {
                        valueProgress = message.substring(indexOfFrame + 6, indexOfFrame + 11).trim();
                        value = Integer.parseInt(valueProgress);
                        mBuilder.setProgress(totalFrame, value, false);
                        mNotifyManager.notify(1, mBuilder.build());
                    }

                    Log.e("createVideo", currentFrame++ + " progress " + valueProgress +
                            " " + message);
                }

                @Override
                public void onFailure(String message) {
                    Log.e("createVideo", "FAILED with output : " + message);
                }

                @Override
                public void onFinish() {
                    Intent intent = new Intent(ServiceCreateVideoFromImage.this,
                            Act_CreateVideoFromAudio.class);
                    intent.putExtra("ShareFacebook", true);
                    intent.putExtra("FileOutput", fileOutput);
                    PendingIntent pendingIntent = PendingIntent.getActivities(ServiceCreateVideoFromImage.this,
                            113, new Intent[]{intent},
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    mBuilder.setProgress(0, 0, false)
                            .setContentText("Đã tạo xong video, nhấn vào đây để chia sẻ!")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true);

                    mNotifyManager.notify(1, mBuilder.build());

                    Log.e("createVideo progress", currentFrame + "");
                    stopSelf();
                }
            });

        } catch (FFmpegNotSupportedException | FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    private void loadLibrary() {
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                }
            });
        } catch (FFmpegNotSupportedException ignored) {
        }
    }

}
