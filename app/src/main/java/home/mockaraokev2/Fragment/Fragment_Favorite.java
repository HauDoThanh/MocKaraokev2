package home.mockaraokev2.Fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import home.mockaraokev2.Actitivy.Act_PlayVideo;
import home.mockaraokev2.Adapter.Adap_Favorite;
import home.mockaraokev2.Class.ModelFavorite;
import home.mockaraokev2.Object.VideoObject;
import home.mockaraokev2.R;

/**
 Created by admin on 6/16/2017.
 */

public class Fragment_Favorite extends Fragment {

    private static ClickListenerHot clickListener;
    private LinearLayoutManager manager;
    private TextView txtReport;
    private String signal;
    private Adap_Favorite adap_itemVideo;
    private List<VideoObject> listPhat = new ArrayList<>();

    public static Fragment_Favorite newInstance() {

        Bundle args = new Bundle();

        Fragment_Favorite fragment = new Fragment_Favorite();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View item = inflater.inflate(R.layout.fragment_yeu_thich, container, false);

        RecyclerView recyclerView = item.findViewById(R.id.listFavorite);
        txtReport = item.findViewById(R.id.txtReport);
        manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);

        ModelFavorite modelFavorite = ModelFavorite.Instances(getContext());
        listPhat = new ArrayList<>();

        listPhat = modelFavorite.getFavoriteVideo();
        if (listPhat.size() == 0)
            txtReport.setVisibility(View.VISIBLE);
        adap_itemVideo = new Adap_Favorite(getContext(), listPhat);

        recyclerView.setAdapter(adap_itemVideo);
        updateListFavorite();

        event();
        return item;
    }

    private void event() {
        adap_itemVideo.setOnItemClickListener(new Adap_Favorite.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                clickListener.onItemClick(listPhat.get(position).getId(),
                        listPhat.get(position).getName(),
                        listPhat.get(position).getImg());
            }
        });
    }

    public void setOnItemClickListener(ClickListenerHot clickListener) {
        Fragment_Favorite.clickListener = clickListener;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void getEvent(VideoObject object) {
        boolean test = false;
        Log.e("logxx", "tín hiệu: " + signal);

        for (int i = 0; i < listPhat.size(); i++) {
            if (listPhat.get(i).getId().equals(object.getId()) && signal.equals("Remove")) {
                listPhat.remove(i);
                adap_itemVideo.notifyDataSetChanged();
                test = true;
                Log.e("logxx", "Hanh dong: xoa");
                break;
            }
        }

        if (!test) {

            Log.e("logxx", "Hanh dong: them");
        }

        adap_itemVideo.notifyDataSetChanged();
    }

    @Subscribe
    public void getEvent2(String s) {
        if (s.equals("add")) {
            signal = "Add";
        } else signal = "Remove";
    }

    private void updateListFavorite() {

        Act_PlayVideo act_playVideo = new Act_PlayVideo();
        act_playVideo.setOnFavoriteButtonClickListener(new Act_PlayVideo.SentDataListsner() {
            @Override
            public void sentVideoFavorite(VideoObject videoAddFavo, boolean signal) {
                Log.e("logxx", "tín hiệu: " + signal);

                for (int i = 0; i < listPhat.size(); i++) {
                    if (listPhat.get(i).getId().equals(videoAddFavo.getId()) &&
                            signal) {
                        listPhat.remove(i);
                        adap_itemVideo.notifyDataSetChanged();
                        Log.e("logxx", "Hanh dong: xoa");
                        break;
                    }
                }

                if (!signal) {
                    listPhat.add(videoAddFavo);
                    adap_itemVideo.notifyDataSetChanged();
                    txtReport.setVisibility(View.INVISIBLE);
                    Log.e("logxx", "Hanh dong: them");
                }
            }
        });
    }

    public interface ClickListenerHot {
        void onItemClick(String id, String name, String img);
    }
}
