package home.mockaraokev2.Fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import home.mockaraokev2.Actitivy.Act_PlayVideo;
import home.mockaraokev2.Class.Constant;
import home.mockaraokev2.R;
import home.mockaraokev2.Service.ServiceGhepAnh;

import static home.mockaraokev2.Class.Constant.VIDEO_PATH;

/**
 Created by quocb14005xx on 5/23/2017.
 */

public class Fragment_Camera extends Fragment implements SurfaceHolder.Callback {

    public static boolean mIsRecording = false;
    public static int mPreviewHeight;
    private static Button btnQuay;
    private static MediaRecorder mRecorder;
    private static SurfaceHolder mSurfaceHolder = null;
    private static Camera mCamera = null;
    private static int mPreviewWidth;

    private View item;
    private int huongCamera;
    private int camId;
    private String path, beatFileName;
    private int startTime, resultTime;
    private TextView txtQuay;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        item = inflater.inflate(R.layout.fragment_camera, container, false);
        setUpCamera();
        init();

        event();

        return item;
    }

    private void event() {
        btnQuay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    btnQuay.setEnabled(false);
                    Timer buttonTimer = new Timer();
                    buttonTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            getActivity().runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    btnQuay.setEnabled(true);
                                }
                            });
                        }
                    }, 4000);
                    if (mIsRecording) {
                        txtQuay.setText(" <= Click để quay");
                        stopRecording();
                        resultTime = ((int) System.currentTimeMillis() / 1000) - startTime;
                        Act_PlayVideo.btnCamera.setEnabled(true);

                        btnQuay.setBackgroundColor(Color.parseColor("#cacaca"));
                        btnQuay.setText("QUAY");
                        Act_PlayVideo.youtubePlayer.setShowFullscreenButton(true);

                    } else {
                        if (prepareVideoRecorder()) {
                            Act_PlayVideo.youtubePlayer.setShowFullscreenButton(false);
                            startTime = (int) System.currentTimeMillis() / 1000;
                            btnQuay.setText("XONG");
                            txtQuay.setText("Đang quay ........");
                            btnQuay.setBackgroundColor(Color.RED);
                            Act_PlayVideo.btnCamera.setEnabled(false);
                            mRecorder.start();
                            Toast.makeText(getContext(), "Bắt đầu đang quay!", Toast.LENGTH_SHORT).show();
                            mIsRecording = true;
                        } else {
                            releaseMediaRecorder();

                        }
                    }
                }
            }
        });
    }


    private void init() {
        File abc = new File(Environment.getExternalStorageDirectory(), Constant.FOLDER + "/Video");
        if (!abc.exists()) {
            abc.mkdirs();
            Log.e("log", "nkdir");
        }
        getAllVideo();
        btnQuay = item.findViewById(R.id.btnInsideQuay);
        txtQuay = item.findViewById(R.id.txtInsideTrangThai);
    }

    private void setUpCamera() {

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                releaseMediaRecorder();
                releaseCamera();
            }
        });
        Bundle bd = getArguments();
        camId = bd.getInt("CAMID", -1);
        beatFileName = bd.getString("beatFileName");
        if (camId == 1) {
            huongCamera = 270;
        } else {
            huongCamera = 90;
        }
        mCamera = getCamera();
        SurfaceView mSurfaceView = item.findViewById(R.id.surfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        int mCameraContainerWidth = mSurfaceView.getLayoutParams().width;
    }

    private void stopRecording() {
        mRecorder.stop();
        releaseMediaRecorder();
        mCamera.lock();
        Toast.makeText(getContext(), "Quay hoàn tất", Toast.LENGTH_SHORT).show();
        mIsRecording = false;

        Act_PlayVideo.youtubePlayer.pause();
        setNameForVideo();

    }

    private void setNameForVideo() {
        final Dialog dialog = new Dialog(getContext());
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

                if (!newName.contains(".mp4")) {
                    newName += ".mp4";
                }
                Log.d("TAO", "Ten sau khi kiem tra duoi .mp4: " + newName);


                //kiểm tra trùng với tên cũ
                boolean nameIsExists = false;
                List<String> listVideo = getAllVideo();
                for (String name : listVideo) {
                    Log.d("TAO", "newName: " + newName + "   " + "oldName: " + name);
                    if (name.equals(newName)) {
                        nameIsExists = true;
                        break;
                    }
                }

                if (!nameIsExists) {
                    dialog.dismiss();
                    File oldFile = new File(VIDEO_PATH + "/" + "videoTemp.mp4");
                    File newFile = new File(VIDEO_PATH + "/" + newName);

                    Log.d("TAO", "old: " + oldFile.getPath());
                    Log.d("TAO", "new: " + newFile.getPath());

                    boolean check = oldFile.renameTo(newFile);

                    Log.d("TAO", "old after: " + oldFile.getPath());
                    Log.d("TAO", "check: " + check);

                    path = VIDEO_PATH + "/" + newName;
                    beatFileName = newName;
                    Log.e("service", "beatFileName: " + beatFileName);
                    Log.e("service", "path: " + beatFileName);

                    Intent itservice = new Intent(getContext(), ServiceGhepAnh.class);
                    itservice.putExtra("pathpath", path);
                    itservice.putExtra("tongTg", resultTime);
                    itservice.putExtra("beatFileName", beatFileName);

                    getContext().startService(itservice);
                    FragmentManager fm = getFragmentManager();
                    fm.popBackStack();
                } else {
                    Log.d("TAO", "Ten bi trung");
                    Toast.makeText(view.getContext(), "Tên bị trùng, vui lòng đổi tên khác!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseMediaRecorder();
        releaseCamera();
    }

    private Camera getCamera() {

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        if (camId == 1) {
            for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    try {
                        return mCamera = Camera.open(camIdx);
                    } catch (RuntimeException e) {
                        Log.e("cameras", "Camera failed to open: " + e.getLocalizedMessage());
                    }
                }
            }
        } else {
            for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    try {
                        return mCamera = Camera.open(camIdx);
                    } catch (RuntimeException e) {
                        Log.e("cameras", "Camera failed to open: " + e.getLocalizedMessage());
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseMediaRecorder();
        releaseCamera();
    }


    private boolean prepareVideoRecorder() {
        mRecorder = new MediaRecorder();
        mRecorder.setOrientationHint(huongCamera);//luu video voi landscape
        mCamera.unlock();
        mRecorder.setCamera(mCamera);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        //CamcorderProfile profile = CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_FRONT, CamcorderProfile.QUALITY_LOW);
        mRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        //path = Environment.getExternalStorageDirectory() + "/MOCKaraoke/Video/" + getFileName_CustomFormat() + ".mp4";
        path = VIDEO_PATH + "/videoTemp.mp4";

        mRecorder.setOutputFile(path);

        mRecorder.setMaxDuration(500000);
        mRecorder.setMaxFileSize(500000000);
        mRecorder.setVideoSize(mPreviewWidth, mPreviewHeight);
        mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

        try {
            mRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("Logg", "exception: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("Logg", "exception: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        if (mRecorder != null) {
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
            mCamera.lock();
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRecordingHint(true);
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);//hien thi camera truoc voi landscape
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mPreviewHeight = mCamera.getParameters().getPreviewSize().height;
        mPreviewWidth = mCamera.getParameters().getPreviewSize().width;
        mCamera.stopPreview();
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mIsRecording) {
            stopRecording();
        }

        releaseMediaRecorder();
        releaseCamera();
    }

    private List<String> getAllVideo() {
        List<String> listVideo = new ArrayList<>();

        File home = new File(Environment.getExternalStorageDirectory() + "/MOCKaraoke/Video");
        if (home.listFiles(new FileExtensionFilter()) != null && home.listFiles(new FileExtensionFilter()).length > 0) {
            for (File file : home.listFiles(new FileExtensionFilter())) {
                listVideo.add(file.getName());
            }
        } else {
            Toast.makeText(getContext(), "Bạn chưa quay bài hát nào", Toast.LENGTH_SHORT).show();
        }
        return listVideo;
    }

    private class FileExtensionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp4") || name.endsWith(".MP4"));
        }
    }
}
