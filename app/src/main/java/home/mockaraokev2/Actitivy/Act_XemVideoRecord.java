package home.mockaraokev2.Actitivy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.VideoView;

import home.mockaraokev2.R;

public class Act_XemVideoRecord extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_xem_video_record);

        VideoView vd = (VideoView) findViewById(R.id.videoView);
        MediaController mc = new MediaController(Act_XemVideoRecord.this);
        mc.setAnchorView(vd);
        vd.setMediaController(mc);
        vd.setFitsSystemWindows(true);
        vd.setVideoPath(getIntent().getStringExtra("PATHVIDEO"));
        Log.e("logg", "path: " + getIntent().getStringExtra("PATHVIDEO") );
        vd.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }

}
