package home.mockaraokev2.Actitivy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import home.mockaraokev2.Adapter.Adap_MyRecord;
import home.mockaraokev2.R;

import static home.mockaraokev2.Class.Constant.RECORD_PATH;

public class Act_MyRecord extends AppCompatActivity {

    private ArrayList<String> listNameRecord;
    private Toolbar toolbarMyRecord;
    private RecyclerView recyclerMyRecord;

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_my_record);

        initWidget();
        setupToolbar();
        setUpRecyclerView();
    }

    private void initWidget() {
        recyclerMyRecord = (RecyclerView) findViewById(R.id.recyclerMyRecord);
        toolbarMyRecord = (Toolbar) findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbarMyRecord);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbarMyRecord.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle("Danh sách thu âm");

        //setup button home
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
    }

    private void setUpRecyclerView() {
        recyclerMyRecord.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        recyclerMyRecord.addItemDecoration(dividerItemDecoration);
        recyclerMyRecord.setItemAnimator(new DefaultItemAnimator());
        recyclerMyRecord.setLayoutManager(layoutManager);

        listNameRecord = new ArrayList<>();

        getFileMP3();
        Adap_MyRecord adapMyRecord = new Adap_MyRecord(listNameRecord, getApplicationContext());
        recyclerMyRecord.setAdapter(adapMyRecord);
        adapMyRecord.setOnItemClick(new Adap_MyRecord.ItemClick() {
            @Override
            public void listener(View view, int position) {
                Intent intent = new Intent(Act_MyRecord.this, Act_PlayRecord.class);
                intent.putExtra("pathRecord", RECORD_PATH + "/" + listNameRecord.get(position).replace(" ", "_") + ".mp3");

                startActivity(intent);
            }
        });
    }

    private void getFileMP3() {
        if (isExternalStorageAvailable()) {
                File home = new File(RECORD_PATH);
                if (home.listFiles(new FileExtensionFilter()) != null &&
                        home.listFiles(new FileExtensionFilter()).length > 0) {
                    listNameRecord = new ArrayList<>();
                    for (File file : home.listFiles(new FileExtensionFilter())) {
                        listNameRecord.add(file.getName().replace("_", " ").replace(".mp3", ""));
                    }
                } else {
                    Toast.makeText(this, "Chưa có file ghi âm nào", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private class FileExtensionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".MP3") || name.endsWith(".mp3"));
        }
    }
}