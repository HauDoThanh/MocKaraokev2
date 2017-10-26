package home.mockaraokev2.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
 Created by admin on 6/16/2017.
 */


public class Fragment_Now extends Fragment {

    private static ClickListenerHot clickListener;

    private Adap_ItemVideo adap_itemVideo;
    private List<VideoObject> listPhat = new ArrayList<>();

    private Command command;
    private VideoResult result;

    private View item;
    private ProgressBar progressBar;
    private TextView txtProgess;

    //collections, thread, generic, StringBuilder, StringBuffer

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (item == null) {
            item = inflater.inflate(R.layout.fragment_dangphat, container, false);

            progressBar = item.findViewById(R.id.progress);
            txtProgess = item.findViewById(R.id.txtProgress);

            RecyclerView recyclerView = item.findViewById(R.id.hatnhieunhat);
            LinearLayoutManager manager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(manager);

            listPhat = new ArrayList<>();
            command = Command.getInstance();

            listPhat.clear();

            String casi = getArguments().getString("casi");
            ArrayList<VideoObject> arrDangPhat = getActivity().getIntent().getParcelableArrayListExtra("listPhat");
            ArrayList<VideoObject> arrFavorite = getActivity().getIntent().getParcelableArrayListExtra("listFavorite");

            if (arrDangPhat != null) {
                listPhat = arrDangPhat;
            } else if (arrFavorite != null) {
                listPhat = arrFavorite;
            } else {
                requestYoutubeItem(casi);
            }
            adap_itemVideo = new Adap_ItemVideo(listPhat, getContext(), true, 3);
            recyclerView.setAdapter(adap_itemVideo);

            String xx = getActivity().getIntent().getStringExtra("stopProgress");
            if (xx != null){
                progressBar.setVisibility(View.GONE);
            }
            event();
        }

        return item;
    }

    private void event() {
        adap_itemVideo.setOnItemClickListenerDangPhat(new Adap_ItemVideo.ClickListenerDangPhat() {
            @Override
            public void onItemClick(View v, int position) {
                clickListener.onItemClick(listPhat.get(position).getId(),
                        listPhat.get(position).getName(),
                        listPhat.get(position).getImg());
            }
        });
    }

    private void requestYoutubeItem(String q) {
        command.execute("relevance", "", "video", q, "10", new Callback<VideoResult>() {
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

    private void getData() {
        for (int i = 0; i < result.getItems().size(); i++) {
            VideoItem.Snippet spi = result.getItems().get(i).getSnippet();
            String id = result.getItems().get(i).getId().getVideoId();
            listPhat.add(new VideoObject(id, spi.getTitle(),
                    spi.getThumbnails().getMedium().getUrl()));
            adap_itemVideo.notifyDataSetChanged();
        }
        progressBar.setVisibility(View.GONE);
        txtProgess.setVisibility(View.GONE);
    }

    public void setOnItemClickListener(ClickListenerHot clickListener) {
        Fragment_Now.clickListener = clickListener;
    }

    public interface ClickListenerHot {
        void onItemClick(String id, String name, String img);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.e("rrr", "Dang ky event bus");
    }

}
