package home.mockaraokev2.Actitivy;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import home.mockaraokev2.Adapter.Adap_ItemVideo;
import home.mockaraokev2.Object.VideoObject;
import home.mockaraokev2.R;
import home.mockaraokev2.network.models.PlaylistItem;
import home.mockaraokev2.network.models.PlaylistResult;
import home.mockaraokev2.network.models.VideoItem;
import home.mockaraokev2.network.models.VideoResult;
import home.mockaraokev2.network.retrofit.Command;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 Created by buimi on 7/4/2017.
 */

public class Act_Playlist_Show extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    private final List<VideoObject> videoObjectList = new ArrayList<>();
    private Adap_ItemVideo adap_itemVideo;

    private String id;

    private boolean isLoading = false;
    private String nextPage;

    private Command command;

    private PlaylistResult result;
    private ProgressBar progressBar;

    private VideoResult resultSinger;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_playlist_show);

        init();
        initRecyclerView();

        event();
    }

    private void init() {
        command = Command.getInstance();

        progressBar = (ProgressBar) findViewById(R.id.progress);

        //init toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;

        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        //Lấy ID từ VideoItem
        Bundle extras = this.getIntent().getExtras();
        id = extras.getString("playlistId");
        String title = extras.getString("title");
        String singer = getIntent().getStringExtra("name");

        if (singer == null) {
            requestYoutubeItem(id, "");
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(title);
            Log.e("hhh", "not singer");
        } else {
            requestYoutubeItemSinger(singer);
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle("Ca sĩ " + singer);
            Log.e("hhh", "singer");
        }
    }

    private void initRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerPlaylist);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        adap_itemVideo = new Adap_ItemVideo(videoObjectList, this, false, 4);
        recyclerView.setAdapter(adap_itemVideo);
    }

    private void event() {
        adap_itemVideo.setOnItemClickListenerShowPlaylist(new Adap_ItemVideo.ClickListenerShowPlaylist() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent(Act_Playlist_Show.this, Act_PlayVideo.class);
                intent.putExtra("videoAddFa", videoObjectList.get(position));
                startActivity(intent);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int totalItemCount = linearLayoutManager.getItemCount();
                int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                totalItemCount = totalItemCount - 1;
                if (totalItemCount == (lastVisibleItem)) {
                    if (isLoading && nextPage.length() > 0) {
                        requestYoutubeItem(id, nextPage);
                        isLoading = false;
                    }
                }
            }
        });

    }

    private void requestYoutubeItem(final String playlistId, final String pageToken) {
        command.execute(playlistId, pageToken, "50", new Callback<PlaylistResult>() {
            @Override
            public void onResponse(Call<PlaylistResult> call, Response<PlaylistResult> response) {
                if (response.isSuccessful()) {
                    result = response.body();
                    getData();
                }
            }

            @Override
            public void onFailure(Call<PlaylistResult> call, Throwable t) {

            }
        });
    }

    private void getData() {
        for (int i = 0; i < result.getItems().size(); i++) {
            if (result.getItems().get(i).getSnippet().getThumbnails() != null) {
                PlaylistItem.Snippet spi = result.getItems().get(i).getSnippet();

                String id = spi.getResourceId().getVideoId();

                if (result.getNextPageToken() != null) {
                    nextPage = result.getNextPageToken();
                    isLoading = true;
                } else {
                    nextPage = "";
                    isLoading = false;
                }
                videoObjectList.add(new VideoObject(id, spi.getTitle(),
                        spi.getThumbnails().getMedium().getUrl()));
                adap_itemVideo.notifyDataSetChanged();
            }
        }
        progressBar.setVisibility(View.GONE);
    }

    private void requestYoutubeItemSinger(String q) {
        command.execute("relevance", "", "video", q, "50", new Callback<VideoResult>() {
            @Override
            public void onResponse(Call<VideoResult> call, Response<VideoResult> response) {
                if (response.isSuccessful()) {
                    resultSinger = response.body();
                    getDataSinger();
                }
            }

            @Override
            public void onFailure(Call<VideoResult> call, Throwable t) {

            }
        });

    }

    ///getData from retrofit:
    private void getDataSinger() {
        for (int i = 0; i < resultSinger.getItems().size(); i++) {
            VideoItem.Snippet spi = resultSinger.getItems().get(i).getSnippet();
            String id = resultSinger.getItems().get(i).getId().getVideoId();
            videoObjectList.add(new VideoObject(id, spi.getTitle(),
                    spi.getThumbnails().getMedium().getUrl()));
            adap_itemVideo.notifyDataSetChanged();
        }
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}