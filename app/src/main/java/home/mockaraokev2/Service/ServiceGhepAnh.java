package home.mockaraokev2.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.github.hiteshsondhi88.sampleffmpeg.DaggerDependencyModuleGhepVideo;

import javax.inject.Inject;

import dagger.ObjectGraph;
import home.mockaraokev2.Actitivy.Act_MyVideo;
import home.mockaraokev2.Class.Constant;
import home.mockaraokev2.Fragment.Fragment_Camera;
import home.mockaraokev2.R;

import static home.mockaraokev2.Class.Constant.VIDEO_PATH;

public class ServiceGhepAnh extends Service {

    @Inject
    public FFmpeg ffmpeg;
    private int yy;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    public ServiceGhepAnh() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ObjectGraph.create(new DaggerDependencyModuleGhepVideo(this)).inject(this);
        loadFFMpegBinary();
        yy = Fragment_Camera.mPreviewHeight + 75;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String pathInput = intent.getStringExtra("pathpath");
        String outPut = intent.getStringExtra("beatFileName");
        int totalTime = intent.getIntExtra("tongTg", -1);
        Log.e("service", outPut);
        GhepAnhVaoVideo(pathInput, totalTime, outPut);
        return START_STICKY;
    }


    private void GhepAnhVaoVideo(final String path, final int tongTg, final String outputName) {
        try {
            String[] cmd = new String[]
                    {
                            "-y",
                            "-i",
                            path,
                            "-strict",
                            "experimental",
                            "-vf",
                            "movie=" + Environment.getExternalStorageDirectory() + "/" + Constant.FOLDER + "/logo_channel.png" + " [watermark]; " +
                                    "[in][watermark] overlay=10:main_h-overlay_h [out]",

                            "-s",
                            "720x1280",
                            "-r",
                            "20",
                            "-b",
                            "2000k",
                            "-vcodec",
                            "mpeg4",
                            "-ab",
                            "48000",
                            "-ac",
                            "2",
                            "-ar",
                            "22050",
                            VIDEO_PATH + "/" + outputName
                    };

            ffmpeg.execute(cmd, new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    Log.d("TAO TAO", "onSuccess");
                }

                @Override
                public void onProgress(String message) {

                    int start = message.indexOf("time=");
                    int end = message.indexOf(" bitrate");
                    if (start != -1 && end != -1) {
                        String duration = message.substring(start + 5, end);
                        if (!duration.equals("")) {
                            String[] s = duration.split(":");
                            String ss = s[2].substring(0, 2);
                            int seconds = Integer.parseInt(ss);
                            int phantam = (seconds * 100) / tongTg;
                            mBuilder.setProgress(100, phantam, false);
                            mNotifyManager.notify(1, mBuilder.build());
                            Log.d("TAO TAO", "onProgress: " + phantam + " %" + "duration " + duration);
                        }
                    }
                }

                @Override
                public void onFailure(String message) {
                    Log.d("TAO TAO", "onFailure: " + message);
                }

                @Override
                public void onStart() {
                    Log.d("TAO", "onStart: ");
                    mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mBuilder = new NotificationCompat.Builder(ServiceGhepAnh.this);
                    mBuilder.setContentTitle("Moctv Karaoke")
                            .setContentText("Đang xử lý bản quyền video")
                            .setSmallIcon(R.drawable.logoapp2)
                            .setPriority(Notification.PRIORITY_HIGH);
                    mBuilder.setProgress(100, 0, false);
                    mBuilder.setAutoCancel(true);
                    mNotifyManager.notify(1, mBuilder.build());
                }

                @Override
                public void onFinish() {
                    mBuilder.setContentText("Đã xử lý xong bài " + outputName.substring(0, outputName.length() - 4))
                            .setProgress(0, 0, false)
                            .setSmallIcon(R.drawable.ic_tick)
                    ;

                    PendingIntent pending = PendingIntent.getActivities(ServiceGhepAnh.this, 111, new Intent[]{new Intent(ServiceGhepAnh.this, Act_MyVideo.class)}, PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(pending);
                    mNotifyManager.notify(1, mBuilder.build());
                    Log.d("TAO TAO", ": Finished ");
                    stopSelf();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    private void loadFFMpegBinary() {
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
