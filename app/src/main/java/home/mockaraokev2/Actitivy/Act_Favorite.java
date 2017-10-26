package home.mockaraokev2.Actitivy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import home.mockaraokev2.Adapter.Adap_Favorite;
import home.mockaraokev2.Class.ModelFavorite;
import home.mockaraokev2.Class.SimpleItemTouchHelperCallback;
import home.mockaraokev2.Interface.OnStartDragListener;
import home.mockaraokev2.Object.VideoObject;
import home.mockaraokev2.R;

public class Act_Favorite extends AppCompatActivity implements OnStartDragListener {

    private ItemTouchHelper itemTouchHelper;
    private Adap_Favorite adapterFavorite;
    private ArrayList<VideoObject> listFavorite;

    private ModelFavorite modelFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_favorite_song);

        setUpToolbar();
        setUpRecyclerView();
        setClickItemVideo();
    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onBackPressed() {
        modelFavorite.dropTable();
        for (VideoObject object : listFavorite) {
            modelFavorite.addFavoriteVideo(object);
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        modelFavorite.dropTable();
        for (VideoObject object : listFavorite) {
            modelFavorite.addFavoriteVideo(object);
        }
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
        Runtime.getRuntime().gc();
    }

    private void setClickItemVideo() {
        adapterFavorite.setOnItemClickListenerAct(new Adap_Favorite.OnItemClickListenerAct() {
            @Override
            public void onItemClick(View v, int position) {
                String idd = listFavorite.get(position).getId();

                Intent intent = new Intent(Act_Favorite.this, Act_PlayVideo.class);
                intent.putParcelableArrayListExtra("listFavorite", listFavorite);
                intent.putExtra("idVideo", idd);
                intent.putExtra("stopProgress", "xx");
                intent.putExtra("videoAddFa", listFavorite.get(position));
                startActivity(intent);
            }
        });
    }

    private void setUpToolbar() {
        Toolbar toolbarFavorite = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbarFavorite);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;

        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
    }

    private void setUpRecyclerView() {
        RecyclerView recyclerviewFavorite = (RecyclerView) findViewById(R.id.recyclerviewFavorite);
        listFavorite = new ArrayList<>();

        modelFavorite = ModelFavorite.Instances(this);
        listFavorite = modelFavorite.getFavoriteVideo();
        adapterFavorite = new Adap_Favorite(Act_Favorite.this, listFavorite, this);

        recyclerviewFavorite.setHasFixedSize(true);
        recyclerviewFavorite.setAdapter(adapterFavorite);
        recyclerviewFavorite.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapterFavorite);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerviewFavorite);
    }

    public void PlayListVideo(View view) {
        final ArrayList<String> listId = new ArrayList<>();

        for (int i = 0; i < listFavorite.size(); i++) {
            listId.add(listFavorite.get(i).getId());
        }

        Intent intent = new Intent(this, Act_PlayVideo.class);
        intent.putStringArrayListExtra("listIDVideo", listId);
        intent.putParcelableArrayListExtra("listFavorite", listFavorite);
        intent.putExtra("stopProgress", "xx");

        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}