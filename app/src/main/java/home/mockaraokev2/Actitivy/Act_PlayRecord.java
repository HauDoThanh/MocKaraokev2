package home.mockaraokev2.Actitivy;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

import home.mockaraokev2.R;


public class Act_PlayRecord extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_play_record);

        MediaController mediaController1 = new MediaController(Act_PlayRecord.this);
        videoView = (VideoView) findViewById(R.id.videoView);

        Toolbar toolbarFavorite = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbarFavorite);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;

        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        String fileRecord = getIntent().getStringExtra("pathRecord");

        if (fileRecord != null){
            File file = new File(fileRecord);
            actionBar.setTitle(file.getName());
            videoView.setVideoPath(fileRecord);
        }

        final MediaController mediaController = new MediaController(videoView.getContext());
        mediaController.setMediaPlayer(videoView);
        videoView.setMediaController(mediaController);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                videoView.start();
                mediaPlayer.start();
                mediaController.show(0);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}