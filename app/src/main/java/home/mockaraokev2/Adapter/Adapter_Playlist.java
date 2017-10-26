package home.mockaraokev2.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import home.mockaraokev2.Object.PlayListObject;
import home.mockaraokev2.R;

/**
 Created by admin on 6/4/2017.
 */

public class Adapter_Playlist extends RecyclerView.Adapter<Adapter_Playlist.DataViewHolder> {

    private final Context context;
    private final List<PlayListObject> listPlaylist;
    private  ClickListener listener;

    public Adapter_Playlist(Context context, List<PlayListObject> listPlaylist) {
        this.context = context;
        this.listPlaylist = listPlaylist;
    }

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tuyentap, parent, false);

        return new DataViewHolder(item);
    }

    @Override
    public void onBindViewHolder(DataViewHolder holder, int position) {
        PlayListObject object = listPlaylist.get(position);

        holder.txtName.setText(object.getName());
        Glide.with(context).load(object.getImgage()).into(holder.imgAlbum);
    }

    @Override
    public int getItemCount() {
        return listPlaylist.size();
    }

    class DataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView imgAlbum;
        final TextView txtName;

        DataViewHolder(View itemView) {
            super(itemView);

            imgAlbum = itemView.findViewById(R.id.imgAlbum);
            txtName = itemView.findViewById(R.id.txtNameAlbum);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClick(v, getAdapterPosition());
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        listener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(View v, int position);
    }
}
