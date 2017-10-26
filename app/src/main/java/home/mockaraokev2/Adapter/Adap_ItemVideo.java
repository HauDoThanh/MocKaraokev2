package home.mockaraokev2.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import home.mockaraokev2.Class.ModelDanhSachPhat;
import home.mockaraokev2.Class.ModelFavorite;
import home.mockaraokev2.Object.VideoObject;
import home.mockaraokev2.R;

/**
 Created by admin on 5/24/2017.
 */

public class Adap_ItemVideo extends RecyclerView.Adapter<Adap_ItemVideo.DataHolder> {

    private final List<VideoObject> videoList;
    private final Context context;
    private final boolean check;
    private final int codeListen;

    private static ClickListenerNew listenerNew;
    private static ClickListenerHot listenerHot;
    private static ClickListenerDangPhat listenerDangPhat;
    private static ClickListenerShowPlaylist listenerShowPlaylist;
    private static ClickListenerSearch listenerSearch;

    public Adap_ItemVideo(List<VideoObject> videoList, Context context, boolean check,
                          int codeListen) {
        this.videoList = videoList;
        this.context = context;
        this.check = check;
        this.codeListen = codeListen;
    }

    @Override
    public DataHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new DataHolder(item);
    }

    @Override
    public void onBindViewHolder(final DataHolder holder, final int position) {
        final VideoObject video = videoList.get(position);

        holder.txtName.setText(video.getName());

        Glide.with(context).load(video.getImg()).into(holder.img);
    }

    @Override
    public int getItemCount() {
        return videoList == null ? 0 : videoList.size();
    }

    class DataHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            AdapterView.OnItemSelectedListener {
        private final ImageView img;
        private final TextView txtName;
        private final Spinner spTinhNang;

        DataHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgVideo);
            txtName = itemView.findViewById(R.id.txtVideoName);

            spTinhNang = itemView.findViewById(R.id.spinner);

            itemView.setOnClickListener(this);

            final String[] arrSpinner;
            ArrayAdapter<String> adapterFunction;
            arrSpinner = context.getResources().getStringArray(R.array.Function);

            if (check) {
                adapterFunction = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item,
                        arrSpinner) {
                    @Override
                    public int getCount() {
                        return arrSpinner.length - 1;
                    }
                };
                spTinhNang.setAdapter(adapterFunction);
            } else {
                adapterFunction = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item,
                        arrSpinner) {
                    @Override
                    public int getCount() {
                        return arrSpinner.length - 2;
                    }
                };
                spTinhNang.setAdapter(adapterFunction);
            }
            spTinhNang.setSelection(3);
            spTinhNang.setOnItemSelectedListener(this);
        }

        @Override
        public void onClick(View view) {
            if (codeListen == 1) {
                listenerHot.onItemClick(view, getAdapterPosition());
            }
            if (codeListen == 2) {
                listenerNew.onItemClick(view, getAdapterPosition());
            }
            if (codeListen == 3) {
                listenerDangPhat.onItemClick(view, getAdapterPosition());
            }
            if (codeListen == 4) {
                listenerShowPlaylist.onItemClick(view, getAdapterPosition());
            }
            if (codeListen == 5) {
                listenerSearch.onItemClick(view, getAdapterPosition());
            }
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            int position = getAdapterPosition();
            spTinhNang.setSelection(3);

            ModelDanhSachPhat modelDanhSachPhat = ModelDanhSachPhat.Instances(context);
            ModelFavorite modelFavorite = ModelFavorite.Instances(context);

            Log.e("lokk","item video " + i);
            switch (i) {
                case 0:
                    VideoObject video = videoList.get(position);
                    boolean x = modelFavorite.addFavoriteVideo(new VideoObject(video.getId(),
                            video.getName(), video.getImg()));
                    if (x) {
                        Toast.makeText(context, "Đã thêm vào danh sách yêu thích!", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(context, "Đã có trong danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    break;

                case 1:
                    VideoObject video2 = videoList.get(position);
                    boolean y = modelDanhSachPhat.addVideoNow(new VideoObject(video2.getId(),
                            video2.getName(), video2.getImg()));
                    if (y) {
                        Toast.makeText(context, "Đã thêm vào danh sách phát!", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(context, "Đã có trong danh sách phát", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    modelDanhSachPhat.deleteVideo(videoList.get(position).getId());

                    Toast.makeText(context, "Đã xoá!", Toast.LENGTH_SHORT).show();
                    videoList.remove(position);
                    notifyDataSetChanged();

                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    public void setOnItemClickListenerNew(ClickListenerNew clickListener) {
        Adap_ItemVideo.listenerNew = clickListener;
    }

    public void setOnItemClickListenerHot(ClickListenerHot clickListener) {
        Adap_ItemVideo.listenerHot = clickListener;
    }

    public void setOnItemClickListenerDangPhat(ClickListenerDangPhat clickListener) {
        Adap_ItemVideo.listenerDangPhat = clickListener;
    }

    public void setOnItemClickListenerShowPlaylist(ClickListenerShowPlaylist clickListener) {
        Adap_ItemVideo.listenerShowPlaylist = clickListener;
    }

    public void setOnItemClickListenerSearch(ClickListenerSearch clickListener) {
        Adap_ItemVideo.listenerSearch = clickListener;
    }

    public interface ClickListenerNew {
        void onItemClick(View v, int position);
    }

    public interface ClickListenerHot {
        void onItemClick(View v, int position);
    }

    public interface ClickListenerDangPhat {
        void onItemClick(View v, int position);
    }

    public interface ClickListenerShowPlaylist {
        void onItemClick(View v, int position);
    }

    public interface ClickListenerSearch {
        void onItemClick(View v, int position);
    }
}