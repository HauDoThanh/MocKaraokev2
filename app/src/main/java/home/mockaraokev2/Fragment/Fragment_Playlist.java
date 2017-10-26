package home.mockaraokev2.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import home.mockaraokev2.Actitivy.Act_Playlist_Show;
import home.mockaraokev2.Adapter.Adapter_Playlist;
import home.mockaraokev2.Object.PlayListObject;
import home.mockaraokev2.R;
import home.mockaraokev2.network.models.VideoResult;
import home.mockaraokev2.network.retrofit.Command;
import retrofit2.Call;
import retrofit2.Callback;

public class Fragment_Playlist extends Fragment {

    private String nextPage = "";
    private boolean isLoading = false;
    private RecyclerView listVideo;
    private List<PlayListObject> listData;
    private Adapter_Playlist adapterVideo;
    private View item;
    private LinearLayoutManager manager;
    private Command command;
    private ProgressBar progressBar;
    private TextView txtProgress;

    public static Fragment_Playlist newInstance() {
        Bundle args = new Bundle();

        Fragment_Playlist fragment = new Fragment_Playlist();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (item == null) {
            command = Command.getInstance();
            item = inflater.inflate(R.layout.fragment_tuyentap, container, false);

            progressBar = item.findViewById(R.id.progress);
            txtProgress = item.findViewById(R.id.txtProgress);
            listVideo = item.findViewById(R.id.tuyentap);
            manager = new LinearLayoutManager(getContext());
            listVideo.setLayoutManager(manager);

            listData = new ArrayList<>();

            requestYoutubeItem("date", "");
            adapterVideo = new Adapter_Playlist(getContext(), listData);
            listVideo.setAdapter(adapterVideo);
            event();
        }
        return item;
    }

    private void requestYoutubeItem(String order, String pageToken) {
        command.execute(order, pageToken, "playlist", "", "50", new Callback<VideoResult>() {
            @Override
            public void onResponse(Call<VideoResult> call, retrofit2.Response<VideoResult> response) {
                if (response.isSuccessful()) {
                    VideoResult result = response.body();
                    for (int i = 0; i < result.getItems().size(); i++) {
                        if (result.getNextPageToken() != null) {
                            nextPage = result.getNextPageToken();
                            isLoading = true;
                        } else {
                            nextPage = "";
                            isLoading = false;
                        }
                        listData.add(new PlayListObject(result.getItems().get(i).getSnippet().getTitle(),
                                result.getItems().get(i).getId().getPlaylistId(),
                                result.getItems().get(i).getSnippet().getThumbnails().getMedium().getUrl()));
                        adapterVideo.notifyDataSetChanged();
                    }
                    progressBar.setVisibility(View.GONE);
                    txtProgress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<VideoResult> call, Throwable t) {

            }
        });

    }

    private void event() {
        listVideo.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int totalItemCount = manager.getItemCount();
                int lastVisibleItem = manager.findLastVisibleItemPosition();
                totalItemCount = totalItemCount - 1;

                if (totalItemCount == (lastVisibleItem)) {
                    if (isLoading && nextPage.length() > 0) {
                        requestYoutubeItem("", nextPage);
                        isLoading = false;
                    }
                }
            }
        });

        adapterVideo.setOnItemClickListener(new Adapter_Playlist.ClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intentViewDetail = new Intent(getContext(), Act_Playlist_Show.class);

                Log.e("loyyy", "idd: " + listData.get(position).getId());

                intentViewDetail.putExtra("playlistId", listData.get(position).getId());
                intentViewDetail.putExtra("title", listData.get(position).getName());
                startActivity(intentViewDetail);
            }
        });
    }

}

