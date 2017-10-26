package home.mockaraokev2.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import home.mockaraokev2.R;

import static home.mockaraokev2.Class.Constant.VIDEO_PATH;

/**
 Created by Au Nguyen on 7/6/2017.
 */
public class Adap_MyVideo extends RecyclerView.Adapter<Adap_MyVideo.ViewHolder> {

    private static ClickShareFacebookListener listenerShareFacebook;
    private static ClickListener clickListener;
    private final List<String> listMyVideo;
    private final Context context;

    public Adap_MyVideo(List<String> listMyVideo, Context context) {
        this.listMyVideo = listMyVideo;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return listMyVideo.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_my_video, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.tvNameVideo.setText(listMyVideo.get(position));
        holder.spinner.setSelection(3);

        holder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        shareFacebook(adapterView, position);
                        holder.spinner.setSelection(3);

                        break;
                    case 1:
                        renameVideo(adapterView, position);
                        holder.spinner.setSelection(3);

                        break;
                    case 2:
                        deleteVideo(adapterView, position);
                        holder.spinner.setSelection(3);

                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void renameVideo(View view, final int position) {
        final Dialog dialog = new Dialog(view.getContext());
        dialog.setContentView(R.layout.dialog_setnameaudio);
        dialog.setTitle("Đổi tên");
        Button btnSave = dialog.findViewById(R.id.btnSaveName);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        final EditText edtName = dialog.findViewById(R.id.edtNameAudio);
        edtName.setText(listMyVideo.get(position));

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = edtName.getText().toString().replace(" ", "_");

                if (!newName.contains(".mp4")) {
                    newName += ".mp4";
                }

                //kiểm tra trùng với tên cũ
                boolean nameIsExists = false;
                for (String name : listMyVideo) {
                    if (name.equals(newName)) {
                        nameIsExists = true;
                        break;
                    }
                }

                if (!nameIsExists) {
                    File oldFile = new File(VIDEO_PATH, listMyVideo.get(position).replace(" ", "_") + ".mp4");
                    File newFile = new File(VIDEO_PATH, newName);

                    oldFile.renameTo(newFile);
                    listMyVideo.set(position, newName.replace("_", " ").replace(".mp4", ""));
                    notifyDataSetChanged();
                    dialog.dismiss();
                } else {
                    Toast.makeText(view.getContext(), "Tên bị trùng, vui lòng đổi tên khác!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void deleteVideo(View view, final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc chắn muốn xóa?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        File file = new File(VIDEO_PATH + "/" + listMyVideo.get(position));
                        boolean deleted = file.delete();
                        listMyVideo.remove(position);

                        Toast.makeText(context, "Đã xoá", Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Huỷ", null);

        Dialog dialog = builder.create();
        //noinspection ConstantConditions
        dialog.getWindow().setBackgroundDrawableResource(R.color.background_xam);
        dialog.show();

    }

    private void shareFacebook(View view, int position) {
        listenerShareFacebook.onItemClick(view, position);
    }

    public void setOnItemClickListenerShareFacebook(ClickShareFacebookListener clickListener) {
        Adap_MyVideo.listenerShareFacebook = clickListener;
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        Adap_MyVideo.clickListener = clickListener;
    }

    public interface ClickShareFacebookListener {
        void onItemClick(View v, int position);
    }

    public interface ClickListener {
        void onItemClick(View v, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView tvNameVideo;
        private final ImageView imgIcon;
        private final Spinner spinner;

        ViewHolder(View itemView) {
            super(itemView);
            tvNameVideo = itemView.findViewById(R.id.tvNameMyRecord);
            imgIcon = itemView.findViewById(R.id.iconVideoRecord);
            spinner = itemView.findViewById(R.id.spinner);

            imgIcon.setImageResource(R.mipmap.ic_movie_48px_vectorized);
            itemView.setOnClickListener(this);

            final String[] arrSpinner;
            ArrayAdapter<String> adapterFunction;
            arrSpinner = context.getResources().getStringArray(R.array.itemMyVideo);

            adapterFunction = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item,
                    arrSpinner) {
                @Override
                public int getCount() {
                    return arrSpinner.length - 1;
                }
            };
            spinner.setAdapter(adapterFunction);
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(view, getAdapterPosition());
        }

    }

}