package home.mockaraokev2.Actitivy;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

import home.mockaraokev2.MainActivity;
import home.mockaraokev2.R;
import home.mockaraokev2.network.models.Mp3Object;
import home.mockaraokev2.network.models.PostObject;
import home.mockaraokev2.network.retrofit.Command;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static home.mockaraokev2.Class.Constant.RECORD_PATH;

/**
 * Created by admin on 9/25/2017.
 */

public class Act_MainAudio extends AppCompatActivity {
    static {
        System.loadLibrary("SuperpoweredExample");
    }

    private Button btnPlay, btnSave, btnCreateVideo;

    private int timeEndRecord, timePlayRecord;
    private String recordedFileName, beatFileName;
    private String beatFile = "";

    private String outputFile;
    private String convertFile;

    private String tmpBeatFile;
    private String tmpRecordFile;
    private String timeEnd;
    private String samplerateString = null, buffersizeString = null;
    private FFmpeg ffmpeg;

    private Context mContext;
    private TextView txtMix, txtEchoValue, txtVolume;
    private final OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int id = seekBar.getId();
            String value = String.valueOf(Math.round(progress) / 100.00f);
            String volume = String.valueOf(Math.round(progress) / 10.00f);

            switch (id) {
                case R.id.echo_mix:
                    onEcho(progress);
                    txtEchoValue.setText(value);
                    break;

                case R.id.reverb_mix:
                    onFxReverbValue(progress);
                    txtMix.setText(value);
                    break;
                case R.id.volume:
                    onVolume(progress, Integer.parseInt(samplerateString));
                    txtVolume.setText(volume);
                    Log.d("SuperpoweredExample", "value: " + volume);

                    break;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    private MediaPlayer mPlayer;
    private ProgressDialog progressDialog;
    private ProgressBar progressDownload;
    private TextView txtProgress;
    private LinearLayout layoutMainAudio;
    private boolean playing = false;
    private SeekBar fxMixEcho, fxMix, fxVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main_audio);

        mContext = this;

        initWidget();
        initFFmpeg();
        initToolbar();
        event();
        callMethod();
    }

    private void initWidget() {
        progressDownload = (ProgressBar) findViewById(R.id.progress);
        layoutMainAudio = (LinearLayout) findViewById(R.id.layoutMainAudio);

        btnPlay = (Button) findViewById(R.id.playPause);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnCreateVideo = (Button) findViewById(R.id.btnCreateVideo);

        txtMix = (TextView) findViewById(R.id.reverb_value);
        txtEchoValue = (TextView) findViewById(R.id.echo_value);
        txtVolume = (TextView) findViewById(R.id.tvVolumeValue);
        txtProgress = (TextView) findViewById(R.id.txtProgress);

        fxMixEcho = (SeekBar) findViewById(R.id.echo_mix);
        fxMix = (SeekBar) findViewById(R.id.reverb_mix);
        fxVolume = (SeekBar) findViewById(R.id.volume);

        fxMix.incrementProgressBy(1);
        fxMixEcho.incrementProgressBy(1);
        fxVolume.incrementProgressBy(1);

        fxMix.setEnabled(false);
        fxMixEcho.setEnabled(false);
        fxVolume.setEnabled(false);
    }

    private void initFFmpeg() {
        ffmpeg = FFmpeg.getInstance(this);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler());
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
        }
    }

    @SuppressLint("RestrictedApi")
    private void initToolbar() {
        Toolbar toolbarFavorite = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbarFavorite);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;

        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
    }

    private void event() {
        fxMixEcho.setOnSeekBarChangeListener(seekBarChangeListener);
        fxMix.setOnSeekBarChangeListener(seekBarChangeListener);
        fxVolume.setOnSeekBarChangeListener(seekBarChangeListener);

        progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle("Waiting");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void callMethod() {
        getInfoToAddEffect();
        /*progressDownload.setVisibility(View.GONE);
        txtProgress.setVisibility(View.GONE);
        layoutMainAudio.setVisibility(View.VISIBLE);

        splitFileBeat();*/
        getTokenAndDownloadBeat(beatFileName.replace("_", " "));
    }

    /*+++++++++++ Lấy thông tin từ Act_PlayVideo để tải beat về & cắt beat ++++++++++++++ */
    private void getInfoToAddEffect() {
        Bundle bundle = getIntent().getBundleExtra("infoProcess");
        beatFileName = bundle.getString("beatNameFile");
        recordedFileName = bundle.getString("recordFileName");
        timeEndRecord = bundle.getInt("timeEnd");

        //recordedFileName = recordedFileName.replace(".wav", "");
        recordedFileName = recordedFileName.replace(".mp3", "");

    }

    /*++++++++++ Xác thực người dùng để download file beat về +++++++++++++++++++++++++++ */
    private void getTokenAndDownloadBeat(final String fileName) {
        final Command command = Command.getInstance();
        Log.e("vvv", "Main Audio " + "abcd" + "    " + "1234");

        command.execute("abcd", "1234", new Callback<PostObject>() {
            @Override
            public void onResponse(Call<PostObject> call, Response<PostObject> response) {
                if (response.isSuccessful()) {
                    String token = response.body().getToken();
                    if (token != null)
                        getMp3(token, fileName);
                }
            }

            @Override
            public void onFailure(Call<PostObject> call, Throwable t) {

            }
        });
    }

    private void getMp3(String token, final String fileName) {
        Command command = Command.getInstance();
        Log.e("logg", token + " \n " + fileName);
        command.executeMp3(token, fileName, new Callback<Mp3Object>() {
            @Override
            public void onResponse(Call<Mp3Object> call, Response<Mp3Object> response) {
                if (response.isSuccessful()) {
                    Log.e("logg", response.body().getLink());
                    DownloadFile downloadFile = new DownloadFile();
                    downloadFile.execute(response.body().getLink(), fileName);
                } else Log.e("logg", "get mp3 that bai" + response.message());
            }

            @Override
            public void onFailure(Call<Mp3Object> call, Throwable t) {
                Log.e("logg", "onFail get mp3 that bai" + t.getMessage());
            }
        });
    }

    /*+++++++++ Cắt file beat dựa theo thông tin đã có ++++++++++++++++++++++++++++++++ */

    private void splitFileBeat() {
        String input = RECORD_PATH + "/" + recordedFileName + ".mp3";
        String[] cmdinfo = new String[]{
                "-i",
                input,
                "-hide_banner"
        };

        try {
            ffmpeg.execute(cmdinfo, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String message) {
                    super.onFailure(message);
                    int indexDuration = message.indexOf("Duration: ") + 13;
                    timeEnd = "00:" + message.substring(indexDuration, indexDuration + 5) + ".000";

                    Log.d("TIME_TO_XXX", message);
                    Log.d("TIME_TO_XXX", "Start: " + timeEnd);

                    String[] cmd = new String[]{
                            "-i",
                            RECORD_PATH + "/" + "beatDownload.mp3",
                            "-ss",
                            "00:00:00.023",
                            "-to",
                            timeEnd,
                            "-c",
                            "copy",
                            "-y",
                            RECORD_PATH + "/beatCut.mp3"
                    };
                    Log.d("TIME_TO_XXX", "cmd split beat " + Arrays.toString(cmd));

                    try {
                        ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                            @Override
                            public void onFailure(String s) {
                                Log.d("TIME_TO_XXX", "Split FAILED  : " + s);
                            }

                            @Override
                            public void onSuccess(String s) {
                                Log.d("TIME_TO_XXX", "Split Success : " + s);
                            }

                            @Override
                            public void onProgress(String s) {
                                Log.d("TIME_TO_XXX", "Split Processing : " + s);
                                progressDialog.setMessage("Split Processing\n" + s);
                            }

                            @Override
                            public void onStart() {
                                Log.d("TIME_TO_XXX", "Split start");
                            }

                            @Override
                            public void onFinish() {
                                Log.d("TIME_TO_XXX", "Split onFinish");
                            }
                        });

                    } catch (FFmpegCommandAlreadyRunningException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    /*++++++++++ Phát file beat & file record để chỉnh hiệu ứng và lưu lại ++++++++++++++++*/
    public void SuperpoweredExample_PlayPause(View button) {  // Play/pause
        btnSave.setEnabled(true);
        fxMix.setEnabled(true);
        fxMixEcho.setEnabled(true);
        fxVolume.setEnabled(true);

        timePlayRecord = timeEndRecord / 1000 * 1000;
        playing = !playing;

        if (playing) {
            btnPlay.setText(String.format("%s", "Stop"));

            mPlayer = MediaPlayer.create(this, Uri.parse(RECORD_PATH + "/beatCut.mp3"));
            Log.e("record", "file beat: " + RECORD_PATH + "/beatCut.mp3");

            new CountDownTimer(timePlayRecord, 1000) {

                @Override
                public void onTick(long l) {
                    timePlayRecord -= 1000;
                    Log.d("logg", timePlayRecord + "");
                }

                @Override
                public void onFinish() {
                    onPlayPause(false);
                    Log.d("logg", "Xong");
                    btnPlay.setText(String.format("%s", "Play"));
                }
            }.start();

            try {
                mPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (Build.VERSION.SDK_INT >= 17) {
                AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                samplerateString = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
                buffersizeString = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
            }
            if (samplerateString == null) samplerateString = "41100";
            if (buffersizeString == null) buffersizeString = "512";

            // SuperpoweredExample(Integer.parseInt(samplerateString), Integer.parseInt(buffersizeString), RECORD_PATH + "/" + recordedFileName + ".wav");
            SuperpoweredExample(Integer.parseInt(samplerateString), Integer.parseInt(buffersizeString), RECORD_PATH + "/" + recordedFileName + ".mp3");
            Log.e("record", "file record: " + RECORD_PATH + "/" + recordedFileName + ".mp3");

        } else {
            mPlayer.pause();
        }
        onPlayPause(playing);

    }

    public void btnSaveClick(View view) {
        btnCreateVideo.setEnabled(true);
        onPlayPause(false);
        if (mPlayer.isPlaying())
            mPlayer.stop();

        tmpBeatFile = RECORD_PATH + "/beatEffect.wav";
        tmpRecordFile = RECORD_PATH + "/inputEffect.wav";

        outputFile = RECORD_PATH + "/" + recordedFileName + "_out.mp3";
        convertFile = RECORD_PATH + "/" + recordedFileName + "_mixed.mp3";

        beatFile = RECORD_PATH + "/beatCut.mp3";

        onWrite(RECORD_PATH + "/" + recordedFileName + ".mp3", tmpRecordFile, beatFile, tmpBeatFile, outputFile);

        mergeRecordAndAudio(RECORD_PATH + "/" + recordedFileName + ".mp3", beatFile, outputFile);

    }

    private void mergeRecordAndAudio(String record, String beat, String out) {
        Log.e("logg", "ghep file");
        String[] cmd = new String[]{
                "-i",
                record,
                "-i",
                beat,
                "-filter_complex",
                "amerge",
                "-ac",
                "1",
                out
        };
        try {
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                @Override
                public void onStart() {
                    super.onStart();
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Processing...");
                    progressDialog.show();
                    Log.e("logg", "start");
                }

                @Override
                public void onProgress(String message) {
                    super.onProgress(message);
                    Log.e("record: ", message);
                    progressDialog.setMessage("Processing\n" + message);
                }

                @Override
                public void onSuccess(String message) {
                    super.onSuccess(message);
                    Log.e("record: ", "SUCCESS with output : " + message);


                    new File(tmpBeatFile).delete();
                    new File(tmpRecordFile).delete();
                    new File(beatFile).delete();
                    new File(RECORD_PATH + "/" + recordedFileName + ".mp3").delete();
                    new File(RECORD_PATH + "/beatDownload.mp3").delete();
                    //new File(RECORD_PATH + "/" + recordedFileName + ".wav").delete();

                    //convertFileToMp3();
                }

                @Override
                public void onFailure(String message) {
                    super.onFailure(message);
                    Log.e("record: ", "FAILED with output : " + message);
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    showToast();
                    progressDialog.dismiss();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
            Log.e("logg", e.getMessage());
        }
    }


    /*private void convertFileToMp3() {
        String[] command = new String[]{
                "-i",
                outputFile,
                "-acodec",
                "libmp3lame",
                convertFile
        };
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    super.onSuccess(message);
                    Log.e("logg", "convert thành công " + message);
                    new File(RECORD_PATH + "/" + recordedFileName + "_out.wav").delete();
                    progressDialog.dismiss();
                    btnCreateVideo.setEnabled(true);
                }

                @Override
                public void onFailure(String message) {
                    super.onFailure(message);
                    Log.e("logg", "Failure: " + message);
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }*/

    /*+++++++++++ Qua giao diện tạo video từ audio +++++++++++++++++++++++++++++*/
    public void CreateVideo(View view) {
        Intent intent = new Intent(this, Act_CreateVideoFromAudio.class);
        intent.putExtra("fileEffect", recordedFileName + "_mixed.mp3");
        startActivity(intent);
    }

    private void showToast() {
        Toast.makeText(mContext, "Đã xử lý!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
    }

    /*+++++++++++ Code C++ xử lý âm thanh +++++++++++++++++++++++++++++++*/
    private native void SuperpoweredExample(int samplerate, int buffersize, String apkPath);

    private native void onPlayPause(boolean play);

    private native void onFxReverbValue(int value);

    private native void onEcho(int mix);

    private native void onVolume(int value, int sampleRate);

    private native void onWrite(String input, String inputEffect, String beat, String beatEffect, String output);

    private class DownloadFile extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... urlParams) {
            int count;
            try {
                URL url = new URL(urlParams[0]);
                URLConnection conexion = url.openConnection();

                conexion.connect();
                // this will be useful so that you can show a tipical 0-100% progress bar
                int lenghtOfFile = conexion.getContentLength();

                // downlod the file
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(RECORD_PATH + "/" +
                        "beatDownload.mp3");

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    publishProgress((int) (total * 100 / lenghtOfFile));
                    output.write(data, 0, count);
                    Log.e("lokkk", lenghtOfFile + "");
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception ignored) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            splitFileBeat();
            progressDownload.setVisibility(View.GONE);
            txtProgress.setVisibility(View.GONE);
            layoutMainAudio.setVisibility(View.VISIBLE);
        }
    }

}