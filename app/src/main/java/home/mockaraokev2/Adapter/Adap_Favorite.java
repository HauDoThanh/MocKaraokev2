package home.mockaraokev2.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.Collections;
import java.util.List;

import home.mockaraokev2.Class.ModelFavorite;
import home.mockaraokev2.Interface.ItemTouchHelperAdapter;
import home.mockaraokev2.Interface.ItemTouchHelperViewHolder;
import home.mockaraokev2.Interface.OnStartDragListener;
import home.mockaraokev2.Object.VideoObject;
import home.mockaraokev2.R;

/**
 Created by Au Nguyen on 6/1/2017.
 */

public class Adap_Favorite extends RecyclerView.Adapter<Adap_Favorite.ViewHolder>
        implements ItemTouchHelperAdapter {

    private final List<VideoObject> list;
    private OnStartDragListener mDragStartListener;
    private final Context context;
    private final int codeListener;

    private static OnItemClickListener clickListener;
    private static OnItemClickListenerAct clickListenerAct;

    public Adap_Favorite(Context context, List<VideoObject> list,
                         OnStartDragListener dragStartListener) {
        this.mDragStartListener = dragStartListener;
        this.list = list;
        this.context = context;
        this.codeListener = 2;
    }

    public Adap_Favorite(Context context, List<VideoObject> list) {
        this.list = list;
        this.context = context;
        this.codeListener = 1;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        VideoObject favoriteObject = list.get(position);
        holder.tvName.setText(favoriteObject.getName());

        Glide.with(context).load(favoriteObject.getImg()).into(holder.imagePicture);

        holder.tvName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                holder.linearLayout.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {

                        if (MotionEventCompat.getActionIndex(motionEvent) == MotionEvent.ACTION_DOWN
                                && codeListener != 1) {
                            mDragStartListener.onStartDrag(holder);
                        }
                        return false;
                    }
                });
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(list, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        ModelFavorite modelFavorite = ModelFavorite.Instances(context);
        boolean x = modelFavorite.delete(list.get(position).getId());

        if (x) {
            Toast.makeText(context, "Đã xoá!", Toast.LENGTH_SHORT).show();
        } else Toast.makeText(context, "Lỗi!", Toast.LENGTH_SHORT).show();

        list.remove(position);
        notifyItemRemoved(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder, View.OnClickListener {

        final TextView tvName;
        final ImageView imagePicture;
        final LinearLayout linearLayout;

        ViewHolder(final View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvNameLoveSong);
            imagePicture = itemView.findViewById(R.id.imgPictureLoveSong);
            linearLayout = itemView.findViewById(R.id.linear);

            itemView.setOnClickListener(this);
            tvName.setOnClickListener(this);
        }

        @Override
        public void onItemSelected() {
            linearLayout.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            linearLayout.setBackgroundColor(0);
        }

        @Override
        public void onClick(View view) {
            if (codeListener == 1)
                clickListener.onItemClick(view, getAdapterPosition());
            if (codeListener == 2)
                clickListenerAct.onItemClick(view, getAdapterPosition());
        }
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        Adap_Favorite.clickListener = clickListener;
    }

    public void setOnItemClickListenerAct(OnItemClickListenerAct clickListener) {
        Adap_Favorite.clickListenerAct = clickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    public interface OnItemClickListenerAct {
        void onItemClick(View v, int position);
    }
}
