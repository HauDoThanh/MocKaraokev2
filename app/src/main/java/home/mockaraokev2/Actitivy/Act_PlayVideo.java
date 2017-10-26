package home.mockaraokev2.Actitivy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import home.mockaraokev2.Adapter.Adap_ViewPager;
import home.mockaraokev2.Class.Constant;
import home.mockaraokev2.Class.ModelFavorite;
import home.mockaraokev2.Fragment.Fragment_Camera;
import home.mockaraokev2.Fragment.Fragment_Favorite;
import home.mockaraokev2.Fragment.Fragment_Now;
import home.mockaraokev2.MainActivity;
import home.mockaraokev2.Object.VideoObject;
import home.mockaraokev2.R;

import static home.mockaraokev2.Class.Constant.RECORD_PATH;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Act_PlayVideo extends AppCompatActivity implements YouTubePlayer.PlayerStateChangeListener {
    private static final String AUDIO_RECORDER_TEMP_FILE = "/record_temp.mp3";
    private static final String TAG = "uuuuuu";
    @SuppressLint("StaticFieldLeak")
    public static Button btnCamera;
    public static YouTubePlayer youtubePlayer;
    private static SentDataListsner dataListsner;

    static {
        System.loadLibrary("SuperpoweredExample");
    }

    private final int PERMISSION_ALL = 1;
    private final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    boolean checkClickRecord = false;
    private int timer = 0;
    private Timer t = new Timer();
    private Dialog dialog;
    private FragmentTransaction transaction;
    private YouTubePlayerSupportFragment youtubeFragment;
    private Button btnRecord, btnFavorite;
    private MediaRecorder mediaRecorder;
    private ArrayList<String> arrId;
    private ArrayList<VideoObject> arrDangPhat, arrFavorite;
    private VideoObject videoAddFavo;
    private ModelFavorite modelFavorite;
    private MusicIntentReceiver myReceiver;
    private String beatFileName, idVideo, casi;
    private boolean isRecoder, checkFullScreen, checkClick, isHeadPhonePlugin = false;
    private int seek, videoCurrent, timeEndRecord;
    private int clickCamera = 1;
    private TextView txtShow;

    //Hàm xin quyền truy cập
    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_play_video);
        myReceiver = new MusicIntentReceiver();
        registerReceiver(myReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));

        init();
        if (savedInstanceState != null) {
            seek = savedInstanceState.getInt("seek");
            checkFullScreen = savedInstanceState.getBoolean("isFullScreen");
            idVideo = savedInstanceState.getString("idVideo");
            videoCurrent = savedInstanceState.getInt("currentVideo");
        }

        setupTabLayout();
        setupYoutubePlayerView();
        onItemFragmentClick();
    }

    /**************************** START khởi tạo & setup****************************/
    private void init() {
        txtShow = (TextView) findViewById(R.id.txtShow);
        btnCamera = (Button) findViewById(R.id.btnOpenCamera);
        btnRecord = (Button) findViewById(R.id.btnRecord);
        btnFavorite = (Button) findViewById(R.id.btnFavorite);
        youtubeFragment = (YouTubePlayerSupportFragment) getSupportFragmentManager()
                .findFragmentById(R.id.youtubeFragment);

        //getData from playlist Fragment
        videoAddFavo = getIntent().getParcelableExtra("videoAddFa");

        modelFavorite = ModelFavorite.Instances(this);

        if (videoAddFavo != null) {
            idVideo = videoAddFavo.getId();

            String[] key = videoAddFavo.getName().split("\\|");
            if (key.length == 3) {
                casi = key[2];
                beatFileName = key[1] + "-" + key[2];
                beatFileName = beatFileName.trim();
            } else {
                casi = videoAddFavo.getName();
            }

            if (modelFavorite.checkVideo(idVideo)) {
                btnFavorite.setBackgroundResource(R.drawable.ic_heart_full);
            }
        }

        //get list id from Act_Favorite
        arrId = new ArrayList<>();
        arrId = getIntent().getStringArrayListExtra("listIDVideo");

        //getData from Act_Favorite
        arrFavorite = new ArrayList<>();
        arrFavorite = getIntent().getParcelableArrayListExtra("listFavorite");

        //getData from Navi DangPhat
        arrDangPhat = new ArrayList<>();
        arrDangPhat = getIntent().getParcelableArrayListExtra("listPhat");

    }

    private void setupTabLayout() {
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        Adap_ViewPager adapViewPager = new Adap_ViewPager(getSupportFragmentManager());

        Fragment_Now fragment_now = new Fragment_Now();
        Bundle bundle = new Bundle();

        if (casi != null) {
            bundle.putString("casi", casi);
        }
        if (arrDangPhat != null) {
            bundle.putParcelableArrayList("listPhat", arrDangPhat);
        }
        if (arrFavorite != null) {
            bundle.putParcelableArrayList("listFavorite", arrFavorite);
        }
        fragment_now.setArguments(bundle);

        adapViewPager.addFragment(fragment_now, "Đang phát");
        adapViewPager.addFragment(Fragment_Favorite.newInstance(), "Yêu thích");
        viewPager.setAdapter(adapViewPager);

        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                viewPager.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**************************** END khởi tạo & setup****************************/

    //cài đặt youtube player
    private void setupYoutubePlayerView() {
        youtubeFragment.initialize(Constant.YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean b) {
                youtubePlayer = player;
                youtubePlayer.setPlayerStateChangeListener(Act_PlayVideo.this);
                stateYoutube();
                if (checkFullScreen) {
                    youtubePlayer.setFullscreen(true);//nút bấm vào sẽ full
                    playModeVideo();
                    Log.e("ppp", "check = true");
                } else {
                    youtubePlayer.setFullscreen(false);//bấm vào sẽ trở về màn hình ban đầu
                    playModeVideo();
                    Log.e("ppp", "check = false");
                }

                youtubePlayer.setShowFullscreenButton(true);
                youtubePlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
                    @Override
                    public void onFullscreen(boolean b) {
                        if (!b) {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            checkFullScreen = false;
                        } else {
                            checkFullScreen = true;
                        }
                    }
                });
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        });


    }

    /**************************** START Phát video ****************************/
    //phát video từ trên fragment đang phát và fragment yêu thích
    private void onItemFragmentClick() {
        Fragment_Now fraNow = new Fragment_Now();
        fraNow.setOnItemClickListener(new Fragment_Now.ClickListenerHot() {
            @Override
            public void onItemClick(String id, String name, String img) {
                idVideo = id;
                if (youtubePlayer != null)
                    youtubePlayer.loadVideo(id);
                else setupYoutubePlayerView();
                videoAddFavo = new VideoObject(id, name, img);
                if (modelFavorite.checkVideo(idVideo)) {
                    btnFavorite.setBackgroundResource(R.drawable.ic_heart_full);
                } else btnFavorite.setBackgroundResource(R.drawable.ic_heart);
            }
        });

        Fragment_Favorite fraFa = new Fragment_Favorite();
        fraFa.setOnItemClickListener(new Fragment_Favorite.ClickListenerHot() {
            @Override
            public void onItemClick(String id, String name, String img) {
                idVideo = id;
                if (youtubePlayer != null)
                    youtubePlayer.loadVideo(id);
                else setupYoutubePlayerView();
                videoAddFavo = new VideoObject(id, name, img);
                if (modelFavorite.checkVideo(idVideo)) {
                    btnFavorite.setBackgroundResource(R.drawable.ic_heart_full);
                } else btnFavorite.setBackgroundResource(R.drawable.ic_heart);
            }
        });
    }

    /**************************** END Phát video ****************************/

    //phát danh sách video & phát video lẻ
    private void playModeVideo() {
        if (arrId != null) {
            youtubePlayer.loadVideos(arrId, arrId.indexOf(idVideo), seek);//load danh sách video
            youtubePlayer.setPlaylistEventListener(new YouTubePlayer.PlaylistEventListener() {
                @Override
                public void onPrevious() {
                    idVideo = arrId.get(--videoCurrent);
                }

                @Override
                public void onNext() {
                    idVideo = arrId.get(++videoCurrent);
                }

                @Override
                public void onPlaylistEnded() {

                }
            });
        } else {
            youtubePlayer.loadVideo(idVideo, seek);//load video lẻ
        }
    }

    /**************************** START Ghi âm ****************************/

    public void prepareAudioRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(RECORD_PATH + AUDIO_RECORDER_TEMP_FILE);
    }

    //button ghi âm
    public void RecordAudio(View view) {

        if (!isRecoder) {
            if (!youtubePlayer.isPlaying()) {
                Toast.makeText(this, "Xin vui lòng chờ cho đến khi video được load!", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Nhan bat dau ghi am");
                youtubePlayer.seekToMillis(11);

                checkClickRecord = true;
                isRecoder = true;
            }

        } else {
            Log.d(TAG, "Dung ghi am");
            mediaRecorder.stop();
            t.cancel();
            timeEndRecord = youtubePlayer.getCurrentTimeMillis();
            if (youtubePlayer.isPlaying())
                youtubePlayer.pause();

            btnCamera.setEnabled(true);
            btnRecord.setBackgroundResource(R.drawable.ic_record);
            isRecoder = false;
            checkClickRecord = false;
            setNameForAudio();
        }
    }

    private void stateYoutube() {
        youtubePlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
            @Override
            public void onPlaying() {
                Log.d(TAG, "playback event: on Playing");
            }

            @Override
            public void onPaused() {
                Log.d(TAG, "playback event: on Pause");
            }

            @Override
            public void onStopped() {

            }

            @Override
            public void onBuffering(boolean b) {
                if (!b && checkClickRecord) {
                    prepareAudioRecorder();
                    Log.d(TAG, "playback event:  onRecord");

                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                        btnCamera.setEnabled(false);

                        timeEndRecord = youtubePlayer.getDurationMillis();
                        Toast.makeText(Act_PlayVideo.this, "Bắt đầu", Toast.LENGTH_SHORT).show();
                        btnRecord.setBackgroundResource(R.drawable.stop);
                        isRecoder = true;
                        t.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        timer++;
                                       // Log.d(TAG, timer + "");
                                        //  if (timer % 1000 == 0)
                                        //txtShow.setText(String.valueOf(timer));
                                    }
                                });
                            }
                        }, 0, 1);
                    } catch (IllegalStateException | IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "playback event: on not record");
                }
            }

            @Override
            public void onSeekTo(int i) {
                Log.d(TAG, "playback event: on Seek to: " + i);
            }
        });
    }

    private void setNameForAudio() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_setnameaudio);
        dialog.setTitle("Lưu ý");

        Button btnSave = dialog.findViewById(R.id.btnSaveName);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        final EditText edtName = dialog.findViewById(R.id.edtNameAudio);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = edtName.getText().toString().replace(" ", "_");
                Log.d("TAO", "Ten lay tren edt: " + newName);

                if (!newName.contains(".mp3")) {
                    newName += ".mp3";
                }
                Log.d("TAO", "Ten sau khi kiem tra duoi .mp3: " + newName);

                //kiểm tra trùng với tên cũ
                boolean nameIsExists = false;
                List<String> listVideo = getAllAudio();
                for (String name : listVideo) {
                    Log.d("TAO", "newName: " + newName + "   " + "oldName: " + name);
                    if (name.equals(newName)) {
                        nameIsExists = true;
                        break;
                    }
                }

                if (!nameIsExists) {
                    File oldFile = new File(RECORD_PATH + "/" + AUDIO_RECORDER_TEMP_FILE);
                    File newFile = new File(RECORD_PATH + "/" + newName);
                    oldFile.renameTo(newFile);

                    dialog.dismiss();
                    goToMainAudio(newName);
                } else {
                    Log.d("TAO", "Ten bi trung");
                    Toast.makeText(view.getContext(), "Tên bị trùng, vui lòng đổi tên khác!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTempFile();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void goToMainAudio(final String newName) {
        if (isHeadPhonePlugin) {
            Intent intent = new Intent(Act_PlayVideo.this, Act_MainAudio.class);
            Bundle bundle = new Bundle();
            bundle.putString("recordFileName", newName);
            bundle.putString("beatNameFile", beatFileName.replace(" ", "_"));
            bundle.putInt("timeEnd", timeEndRecord);
            intent.putExtra("infoProcess", bundle);
            startActivity(intent);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Thông báo")
                    .setMessage("Bạn có muốn tạo video từ bài hát vừa thu âm không?")
                    .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Act_PlayVideo.this, Act_CreateVideoFromAudio.class);
                            intent.putExtra("fileEffect", newName);
                            Log.d("CREATE_VIDEO", "Name sent from Act play video: " + newName);

                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(Act_PlayVideo.this, MainActivity.class));
                        }
                    });
            Dialog dialog = builder.create();
            dialog.show();
        }
    }

    private List<String> getAllAudio() {
        List<String> listVideo = new ArrayList<>();

        File home = new File(RECORD_PATH);
        if (home.listFiles(new FileExtensionFilter()) != null && home.listFiles(new FileExtensionFilter()).length > 0) {
            for (File file : home.listFiles(new FileExtensionFilter())) {
                listVideo.add(file.getName());
            }
        }
        return listVideo;
    }

    //xoá file tạm đi
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteTempFile() {
        File file = new File(RECORD_PATH + AUDIO_RECORDER_TEMP_FILE);
        file.delete();
    }

    /**************************** END Ghi âm ****************************/

    //xem lại file ghi âm
    public void ReviewRecord(View view) {
        if (checkClick) {
            Toast.makeText(this, "Xin vui lòng tắt camera", Toast.LENGTH_SHORT).show();
        } else
            startActivity(new Intent(Act_PlayVideo.this, Act_MyRecord.class));
    }

    /**************************** START quay video ****************************/
    //button quay video
    public void RecordVideo(View view) {
        if (hasPermissions(this, PERMISSIONS)) {
            final FragmentManager manager = getSupportFragmentManager();
            transaction = manager.beginTransaction();

            if (clickCamera == 1) {
                clickCamera = 0;
                btnRecord.setEnabled(false);
                final Dialog dialog = new Dialog(this);
                dialog.setTitle("Chọn camera quay bạn hát!");
                dialog.setContentView(R.layout.dialog_choose_cam);
                dialog.setCanceledOnTouchOutside(false);
                Button btnFront, btnRear;
                btnFront = dialog.findViewById(R.id.btnFrontCam);
                btnRear = dialog.findViewById(R.id.btnRearCam);
                dialog.show();
                final Bundle bd = new Bundle();
                btnFront.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        checkClick = true;
                        bd.putInt("CAMID", 1);
                        bd.putString("beatFileName", beatFileName);
                        Fragment_Camera fragment_camera = new Fragment_Camera();
                        fragment_camera.setArguments(bd);
                        transaction.addToBackStack("camera");
                        transaction.replace(R.id.frame, fragment_camera);
                        transaction.commit();
                        dialog.dismiss();
                    }
                });
                btnRear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        checkClick = true;
                        bd.putInt("CAMID", 0);
                        bd.putString("beatFileName", beatFileName);
                        Fragment_Camera fragment_camera = new Fragment_Camera();
                        transaction.addToBackStack("camera");
                        fragment_camera.setArguments(bd);
                        transaction.replace(R.id.frame, fragment_camera);
                        transaction.commit();
                        dialog.dismiss();
                    }
                });
            } else {
                btnRecord.setEnabled(true);
                clickCamera = 1;
                checkClick = false;
                btnCamera.setBackgroundResource(R.drawable.record_video);
                manager.popBackStack();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Bạn chưa cấp quyền Camera, Micro và ghi bộ nhớ", Toast.LENGTH_SHORT).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ActivityCompat.requestPermissions(Act_PlayVideo.this, PERMISSIONS, PERMISSION_ALL);
                }
            }, 2000);
        }
    }

    /**************************** END quay video ****************************/

    //xem lại video đã quay
    public void ReviewVideo(View view) {
        if (checkClick) {
            Toast.makeText(this, "Xin vui lòng tắt camera", Toast.LENGTH_SHORT).show();
        } else
            startActivity(new Intent(Act_PlayVideo.this, Act_MyVideo.class));
    }

    /**************************** Start thêm vào ds yêu thích ***************************/

    //bắt sự kiện click trên nút yêu thích
    public void setOnFavoriteButtonClickListener(SentDataListsner dataListsner) {
        Act_PlayVideo.dataListsner = dataListsner;
    }

    //thêm vào ds yêu thích
    public void AddToFavorite(View view) {
        boolean check = modelFavorite.checkVideo(videoAddFavo.getId());
        if (check) {
            modelFavorite.delete(videoAddFavo.getId());
            btnFavorite.setBackgroundResource(R.drawable.ic_heart);
            dataListsner.sentVideoFavorite(videoAddFavo, true);

            Toast.makeText(this, "Đã xoá khỏi danh sách yêu thích!",
                    Toast.LENGTH_SHORT).show();
        } else {
            modelFavorite.addFavoriteVideo(videoAddFavo);
            btnFavorite.setBackgroundResource(R.drawable.ic_heart_full);
            dataListsner.sentVideoFavorite(videoAddFavo, false);

            Toast.makeText(this, "Đã thêm vào danh sách yêu thích!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void messageDialog(String message, DialogInterface.OnClickListener clickNegative) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Thông báo");
        builder.setMessage(message);
        builder.setNegativeButton("Ok", clickNegative);
        builder.setPositiveButton(null, null);

        dialog = builder.create();
        //noinspection ConstantConditions
        dialog.getWindow().setBackgroundDrawableResource(R.color.background_xam);
        dialog.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (youtubePlayer != null) {
            youtubePlayer.release();
        }
        youtubePlayer = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (youtubePlayer != null) {
            seek = youtubePlayer.getCurrentTimeMillis();
            youtubePlayer.release();
        }
        youtubePlayer = null;

        outState.putString("idVideo", idVideo);

        outState.putInt("seek", seek);
        outState.putBoolean("isFullScreen", checkFullScreen);
        outState.putInt("currentVideo", videoCurrent);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        File temp = new File(RECORD_PATH + AUDIO_RECORDER_TEMP_FILE);
        if (temp.exists())
            deleteTempFile();

        if (Fragment_Camera.mIsRecording) {
            Toast.makeText(this, "Bạn đang quay video dừng hẳn rồi thoát!", Toast.LENGTH_SHORT).show();
        }

        if (checkFullScreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            checkFullScreen = false;
            youtubePlayer.setFullscreen(false);
        } else
            super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 2: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("logg", "Fds");
                } else {
                    Toast.makeText(this, "Yêu cầu quyền camera!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && Fragment_Camera.mIsRecording) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onPause() {
        unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupYoutubePlayerView();
        //đăng ký sự kiện gắn headphone
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(myReceiver, filter);

        super.onResume();
    }


    //Youtube State
    @Override
    public void onLoading() {
        Log.d(TAG, "onLoading");
    }

    @Override
    public void onLoaded(String s) {
        Log.d(TAG, "state video: onLoaded");

    }

    @Override
    public void onAdStarted() {
        Log.d(TAG, "state video: onStarted");

    }

    @Override
    public void onVideoStarted() {
        Log.d(TAG, "state video: VideoStarted");
    }

    @Override
    public void onVideoEnded() {

    }

    @Override
    public void onError(YouTubePlayer.ErrorReason errorReason) {

    }

    private void xuLyHeadPhone() {
        if (youtubePlayer.isPlaying()) {
            youtubePlayer.seekToMillis(0);
        }
        dialog.dismiss();
        isRecoder = false;
        deleteTempFile();
        btnRecord.setBackgroundResource(R.drawable.ic_record);
    }

    public interface SentDataListsner {
        void sentVideoFavorite(VideoObject videoObject, boolean signal);
    }

    //bắt sự kiện gắn headphone
    private class MusicIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        isHeadPhonePlugin = false;
                        Log.e("headphone", "Headset is unplugged");
                        if (isRecoder) {
                            messageDialog("Bạn vừa tháo headphone, xin vui lòng ghi âm lại từ đầu!",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            xuLyHeadPhone();
                                        }
                                    });
                        }
                        break;
                    case 1:
                        Log.e("headphone", "Headset is plugged");
                        isHeadPhonePlugin = true;
                        if (isRecoder) {
                            messageDialog("Bạn vừa gắn headphone, xin vui lòng ghi âm lại từ đầu!",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            xuLyHeadPhone();
                                        }
                                    });
                        }
                        break;
                }
            }
        }
    }

    private class FileExtensionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".WAV") || name.endsWith(".wav") ||
                    name.endsWith(".mp3") || name.endsWith(".MP3"));
        }
    }
}