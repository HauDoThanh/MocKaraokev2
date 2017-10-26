package home.mockaraokev2.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
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
 Created by admin on 6/8/2017.
 */

public class Fragment_New extends Fragment {
    private static ClickListenerNew clickListener;

    private Adap_ItemVideo adap_itemVideo;
    private List<VideoObject> listNew;

    private VideoResult result;
    private Command command;

    private View item;
    private ProgressBar progressBar;
    private TextView txtProgress;

    public static Fragment_New newInstance() {
        Bundle args = new Bundle();

        Fragment_New fragment = new Fragment_New();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (item == null) {
            item = inflater.inflate(R.layout.fragment_new, container, false);

            progressBar = item.findViewById(R.id.progress);
            txtProgress = item.findViewById(R.id.txtProgress);

            RecyclerView recyclerView = item.findViewById(R.id.hatnhieunhat);
            LinearLayoutManager manager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(manager);
            listNew = new ArrayList<>();
            command = Command.getInstance();
            adap_itemVideo = new Adap_ItemVideo(listNew, getContext(), false, 2);
            recyclerView.setAdapter(adap_itemVideo);

            event();
            requestYoutubeItem();
        }
        return item;
    }

    private void event() {
        adap_itemVideo.setOnItemClickListenerNew(new Adap_ItemVideo.ClickListenerNew() {
            @Override
            public void onItemClick(View v, int position) {
                clickListener.onItemClick(listNew.get(position));
            }
        });
    }

    private void requestYoutubeItem() {
        command.execute("date", "", "video", "", "30", new Callback<VideoResult>() {
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
            listNew.add(new VideoObject(id, spi.getTitle(),
                    spi.getThumbnails().getMedium().getUrl()));
            adap_itemVideo.notifyDataSetChanged();

        }
        progressBar.setVisibility(View.GONE);
        txtProgress.setVisibility(View.GONE);
    }

    public void setOnItemClickListener(ClickListenerNew clickListener) {
        Fragment_New.clickListener = clickListener;
    }

    public interface ClickListenerNew {
        void onItemClick(VideoObject videoObject);
    }

}
