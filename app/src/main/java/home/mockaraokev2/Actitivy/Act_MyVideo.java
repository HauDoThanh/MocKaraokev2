package home.mockaraokev2.Actitivy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import home.mockaraokev2.Adapter.Adap_MyVideo;
import home.mockaraokev2.Class.Constant;
import home.mockaraokev2.R;

import static home.mockaraokev2.Class.Constant.VIDEO_PATH;

public class Act_MyVideo extends AppCompatActivity {

    private Toolbar toolbar;
    private List<String> listMyVideo;
    private RecyclerView recyclerViewVideo;
    private Adap_MyVideo adapterMyVideo;

    private CallbackManager callbackManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_my_video);

        initWidget();
        setupToolbar();
        initValue();
        createVideoFacebook();
        event();
    }

    private void initWidget() {
        recyclerViewVideo = (RecyclerView) findViewById(R.id.recyclerMyVideo);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
    }

    private void initValue() {
        Bitmap logoChannel = BitmapFactory.decodeResource(getResources(), R.drawable.newlogo);
        OutputStream outStream = null;
        File file = new File(Environment.getExternalStorageDirectory(), Constant.FOLDER + "/logo_channel.png");
        try {
            outStream = new FileOutputStream(file);
            logoChannel.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception ignored) {

        }

        listMyVideo = getAllFileMp4FromSDCard();
        adapterMyVideo = new Adap_MyVideo(listMyVideo, this);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerViewVideo.setLayoutManager(manager);

        recyclerViewVideo.setAdapter(adapterMyVideo);
    }

    private void event() {
        adapterMyVideo.setOnItemClickListener(new Adap_MyVideo.ClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent it = new Intent(Act_MyVideo.this, Act_XemVideoRecord.class);
                it.putExtra("PATHVIDEO", VIDEO_PATH + "/" + listMyVideo.get(position).replace(" ", "_") + ".mp4");
                startActivity(it);
            }
        });

        adapterMyVideo.setOnItemClickListenerShareFacebook(new Adap_MyVideo.ClickShareFacebookListener() {
            @Override
            public void onItemClick(View v, int position) {
                shareVideoFacebook(VIDEO_PATH + "/" + listMyVideo.get(position).replace(" ", "_") + ".mp4");
            }
        });
    }

    private List<String> getAllFileMp4FromSDCard() {
        List<String> myVideo = new ArrayList<>();
        File home = new File(VIDEO_PATH);
        if (home.listFiles(new FileExtensionFilter()) != null &&
                home.listFiles(new FileExtensionFilter()).length > 0) {
            for (final File file : home.listFiles(new FileExtensionFilter())) {
                myVideo.add(file.getName().replace(".mp4", "").replace("_", " "));
            }
        } else {
            Toast.makeText(this, "Bạn chưa quay bài hát nào", Toast.LENGTH_SHORT).show();
        }
        return myVideo;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void shareVideoFacebook(String pathFileVideo) {
        Uri pathVideo = Uri.fromFile(new File(pathFileVideo));

        ShareVideo video = new ShareVideo.Builder()
                .setLocalUrl(pathVideo)
                .build();
        ShareVideoContent clip = new ShareVideoContent.Builder()
                .setVideo(video)
                .build();

        ShareDialog dialog = new ShareDialog(this);
        if (ShareDialog.canShow(ShareVideoContent.class)) {
            dialog.show(clip);
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Share video");
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        dialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                progressDialog.hide();
                //Toast.makeText(Act_CreateVideoFromAudio.this, "Đăng bài thành công", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                progressDialog.hide();
                //Toast.makeText(Act_CreateVideoFromAudio.this, "Hủy bài đăng", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                progressDialog.hide();
                //Toast.makeText(Act_CreateVideoFromAudio.this, "Lỗi khi đăng bài", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createVideoFacebook() {
        callbackManager = CallbackManager.Factory.create();
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "home.mockaraokev2",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            Log.e("logg", e.getMessage());
        }
    }

    private class FileExtensionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp4") || name.endsWith(".MP4"));
        }
    }
}
