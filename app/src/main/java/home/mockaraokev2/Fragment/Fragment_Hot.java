package home.mockaraokev2.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import home.mockaraokev2.Adapter.Adap_ItemVideo;
import home.mockaraokev2.Object.VideoObject;
import home.mockaraokev2.R;
import home.mockaraokev2.network.models.VideoItem;
import home.mockaraokev2.network.models.VideoResult;
import home.mockaraokev2.network.retrofit.Command;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
  Created by admin on 6/6/2017.
 */

public class Fragment_Hot extends Fragment {
    private static ClickListenerHot clickListener;

    private List<VideoObject> listHot;
    private Adap_ItemVideo adapter;

    private Command command;
    private VideoResult result;

    private View view;
    private ProgressBar progressBar;
    private TextView txtProgress;

    public static Fragment_Hot newInstance() {
        return new Fragment_Hot();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            command = Command.getInstance();
            view = inflater.inflate(R.layout.fragment_hot, container, false);

            progressBar = view.findViewById(R.id.progress);
            txtProgress = view.findViewById(R.id.txtProgress);
            RecyclerView listVideo = view.findViewById(R.id.hatnhieunhat);
            LinearLayoutManager manager = new LinearLayoutManager(getContext());
            listVideo.setLayoutManager(manager);
            listHot = new ArrayList<>();
            adapter = new Adap_ItemVideo(listHot, getContext(), false, 1);
            listVideo.setAdapter(adapter);

            event();
            requestYoutubeItem();
        }
        return view;
    }

    private void event() {
        adapter.setOnItemClickListenerHot(new Adap_ItemVideo.ClickListenerHot() {
            @Override
            public void onItemClick(View v, int position) {
                clickListener.onItemClick(listHot.get(position));
            }
        });
    }

    private void requestYoutubeItem() {
        command.execute("viewCount", "", "video", "", "30", new Callback<VideoResult>() {
            @Override
            public void onResponse(Call<VideoResult> call, Response<VideoResult> response) {
                if (response.isSuccessful()) {
                    result = response.body();
                    getData();
                }
            }

            @Override
            public void onFailure(Call<VideoResult> call, Throwable t) {

            }
        });

    }

    ///getData from retrofit:
    private void getData() {
        for (int i = 0; i < result.getItems().size(); i++) {
            VideoItem.Snippet spi = result.getItems().get(i).getSnippet();
            String id = result.getItems().get(i).getId().getVideoId();
            listHot.add(new VideoObject(id, spi.getTitle(),
                    spi.getThumbnails().getMedium().getUrl()));
            adapter.notifyDataSetChanged();
        }
        progressBar.setVisibility(View.GONE);
        txtProgress.setVisibility(View.GONE);
    }

    public void setOnItemClickListener(ClickListenerHot clickListener) {
        Fragment_Hot.clickListener = clickListener;
    }

    public interface ClickListenerHot {
        void onItemClick(VideoObject videoObject);
    }
}
