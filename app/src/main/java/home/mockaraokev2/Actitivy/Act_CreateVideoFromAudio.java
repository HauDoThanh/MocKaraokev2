package home.mockaraokev2.Actitivy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import home.mockaraokev2.MainActivity;
import home.mockaraokev2.R;
import home.mockaraokev2.Service.ServiceCreateVideoFromImage;

import static home.mockaraokev2.Class.Constant.RECORD_PATH;
import static home.mockaraokev2.Class.Constant.VIDEO_PATH;

public class Act_CreateVideoFromAudio extends AppCompatActivity {

    private CallbackManager callbackManager;
    private ProgressDialog progressDialog;

    private TextView txtSongName, txtSingerName;
    private EditText edtSongName, edtSingerName;

    private Typeface typeSong, typeSinger;
    private String fileInput;

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_create_video_from_audio);

        initWidget();
        initData();
        createVideoFacebook();
        setUpToolbar();
        event();
    }

    private void initWidget() {
        txtSingerName = (TextView) findViewById(R.id.txtSinger);
        txtSongName = (TextView) findViewById(R.id.txtSongName);

        edtSingerName = (EditText) findViewById(R.id.edtSingerName);
        edtSongName = (EditText) findViewById(R.id.edtSongName);

        ImageView imgBanner = (ImageView) findViewById(R.id.imgBanner);

        typeSinger = Typeface.createFromAsset(getAssets(), "UTM_LinotypeZapfino_KT.ttf");
        typeSong = Typeface.createFromAsset(getAssets(), "UTM_Caviar.ttf");

        txtSongName.setTypeface(typeSong);
        txtSingerName.setTypeface(typeSinger);
    }

    private void initData() {
        fileInput = getIntent().getStringExtra("fileEffect");

        if (fileInput != null) {
            //file input: a_b_c
            edtSongName.setText(fileInput.replace("_", " ").replace(".mp3", ""));
            txtSongName.setText(fileInput.replace("_", " ").replace(".mp3", ""));
        }
    }

    private void setUpToolbar() {
        Toolbar toolbarFavorite = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbarFavorite);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;

        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
    }

    //thêm sự kiện
    private void event() {
        onTextChangeListener(edtSongName, txtSongName, true);
        onTextChangeListener(edtSingerName, txtSingerName, false);

        boolean checkShare = getIntent().getBooleanExtra("ShareFacebook", false);
        String fileShare = getIntent().getStringExtra("FileOutput");
        if (checkShare) {
            shareVideoFacebook(VIDEO_PATH + "/" + fileShare + ".mp4");
        }
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

    //sự kiện thay đổi edittext thì text view thay đổi theo
    private void onTextChangeListener(final EditText edt, final TextView txtShow, final boolean check) {
        edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (edt.getText().toString().length() >= 0 && !check) {
                    txtShow.setText(String.format("Trình bày: %s", edt.getText().toString()));
                } else {
                    txtShow.setText(edt.getText().toString());
                }
            }
        });
    }

    //click button lưu
    public void SaveAndCreateVideo(View view) {
        if (!isVideoExist()) {
            //lấy file vừa tạo hiệu ứng để ghép thành video
            String songName = txtSongName.getText().toString();
            String singerName = txtSingerName.getText().toString();
            //tạo ảnh
            Bitmap bitmap = createBanner(songName, singerName);
            String nameOfBanner = saveImageFile(bitmap, songName);

            Log.d("CREATE_VIDEO", "Act_CreateVideo: " + fileInput);

            Intent intent = new Intent(Act_CreateVideoFromAudio.this, ServiceCreateVideoFromImage.class);
            Bundle bundle = new Bundle();
            bundle.putString("nameOfBanner", nameOfBanner);
            bundle.putString("fileInput", fileInput.replace(" ", "_"));
            Log.d("CREATE_VIDEO", "Name sent from Act Create Videp: " + fileInput.replace(" ", "_"));

            intent.putExtra("valueCreateVideo", bundle);
            startService(intent);
        } else Toast.makeText(this, "Tên video bị trùng", Toast.LENGTH_SHORT).show();
    }

    /*+++++++++++++++++++++++++++ Tạo hình ảnh có tên người hát, tên ca sĩ++++++++++++++++++++*/
    private Bitmap createBanner(String songName, String singerName) {
        Resources resources = this.getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.banner);

        Bitmap xx = drawTextOnBitmap(bitmap, songName, typeSong, 20, scale, 10);

        return drawTextOnBitmap(xx, singerName, typeSinger, 16, scale, 3);
    }

    private Bitmap drawTextOnBitmap(Bitmap bitmap, String text, Typeface type,
                                    int textSize, float scale, float vty) {

        //đặt định dạng cho ảnh
        android.graphics.Bitmap.Config bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;

        // resource bitmaps are immutable, so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(bitmap);

        // new antialiased Paint
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        // text color - #3D3D3D
        paint.setColor(Color.rgb(255, 254, 253));

        // text size in pixels
        paint.setTextSize((int) (bitmap.getHeight() / textSize * scale));

        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        //font chữ
        paint.setTypeface(type);

        // set text width to canvas width minus 16dp padding
        int textWidth = canvas.getWidth() - (int) (16 * scale);

        // init StaticLayout for text
        //Layout.Alignment.ALIGN_OPPOSITE: canh bên phải
        StaticLayout textLayout = new StaticLayout(text, paint, textWidth,
                Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

        // get height of multiline text
        int textHeight = textLayout.getHeight();

        // get position of text's top left corner
        float x = (bitmap.getWidth() - textWidth) / 2;
        float y = (bitmap.getHeight() - textHeight) / vty;

        // draw text to the Canvas center
        canvas.save();
        canvas.translate(x, y);
        textLayout.draw(canvas);
        canvas.restore();

        return bitmap;
    }

    /*+++++++++++++++++++++++++++ Lưu hình ảnh vừa tạo xong +++++++++++++++++++++++++++++++++*/
    private String saveImageFile(Bitmap bitmap, String songName) {
        File file = new File(RECORD_PATH);

        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.mkdirs();
        }

        FileOutputStream out;
        songName = songName.replace(" ", "_");
        String filename = file.getAbsolutePath() + "/" + songName + ".png";
        try {
            out = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return songName;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        progressDialog.hide();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
    }

    private boolean isVideoExist() {
        if (isExternalStorageAvailable()) {
            File home = new File(VIDEO_PATH);
            if (home.listFiles(new FileExtensionFilter()) != null &&
                    home.listFiles(new FileExtensionFilter()).length > 0) {
                for (File file : home.listFiles(new FileExtensionFilter())) {
                    String name = file.getName();
                    Log.e("createVideo", name + "  " + txtSongName.getText().toString().replace(" ", "_"));
                    if (name.equalsIgnoreCase(txtSongName.getText().toString().replace(" ", "_") + ".mp4")) {
                        Log.e("createVideo", "return true");
                        return true;
                    }
                }
            } else {
                return false;
            }
        }
        return false;
    }

    private class FileExtensionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp4") || name.endsWith(".MP4"));
        }
    }
}
