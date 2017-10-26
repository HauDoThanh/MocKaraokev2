package home.mockaraokev2.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import home.mockaraokev2.Actitivy.Act_CreateVideoFromAudio;
import home.mockaraokev2.R;

import static home.mockaraokev2.Class.Constant.RECORD_PATH;

/**
 Created by Au Nguyen on 7/6/2017.
 */
public class Adap_MyRecord extends RecyclerView.Adapter<Adap_MyRecord.ViewHolder> {

    private static ItemClick itemClick;
    private final ArrayList<String> listMyRecord;
    private final Context context;

    public Adap_MyRecord(ArrayList<String> listMyRecord, Context context) {
        this.listMyRecord = listMyRecord;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_my_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.tvNameMyRecord.setText(listMyRecord.get(position));
        holder.spinner.setSelection(3);
        holder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        createVideo(adapterView, position);
                        holder.spinner.setSelection(3);

                        break;
                    case 1:
                        renameAudio(adapterView, position);
                        holder.spinner.setSelection(3);

                        break;
                    case 2:
                        deleteAudio(adapterView, position);
                        holder.spinner.setSelection(3);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void createVideo(View view, final int position) {
        Intent intent = new Intent(view.getContext(), Act_CreateVideoFromAudio.class);
        String fileNameRecord = listMyRecord.get(position).replace(" ", "_");
        Log.e("logg", fileNameRecord);
        intent.putExtra("fileEffect", fileNameRecord + ".mp3");
        view.getContext().startActivity(intent);
    }

    private void renameAudio(View view, final int position) {
        final Dialog dialog = new Dialog(view.getContext());
        dialog.setContentView(R.layout.dialog_setnameaudio);
        dialog.setTitle("Đổi tên");

        final EditText edtName = dialog.findViewById(R.id.edtNameAudio);
        Button btnSave = dialog.findViewById(R.id.btnSaveName);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        edtName.setText(listMyRecord.get(position));

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = edtName.getText().toString().replace(" ", "_");

                if (!newName.contains(".mp3")) {
                    newName += ".mp3";
                }
                //kiểm tra trùng với tên cũ
                boolean nameIsExists = false;
                for (String name : listMyRecord) {
                    if (name.equals(newName)) {
                        nameIsExists = true;
                        break;
                    }
                }

                if (!nameIsExists) {
                    File oldFile = new File(RECORD_PATH, listMyRecord.get(position).replace(" ", "_") + ".mp3");
                    File newFile = new File(RECORD_PATH, newName);

                    boolean check = oldFile.renameTo(newFile);
                    listMyRecord.set(position, newName.replace("_", " ").replace(".mp3", ""));
                    notifyDataSetChanged();
                    dialog.dismiss();
                    Toast.makeText(view.getContext(), "Đổi tên thành công!", Toast.LENGTH_SHORT).show();
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

    @SuppressWarnings("ConstantConditions")
    private void deleteAudio(View view, final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc chắn muốn xóa?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String path = RECORD_PATH + "/" + listMyRecord.get(position).replace(" ", "_");
                        new File(path).delete();

                        listMyRecord.remove(position);
                        notifyDataSetChanged();
                        Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Huỷ", null);

        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.color.background_xam);
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return listMyRecord.size();
    }

    public void setOnItemClick(ItemClick click) {
        itemClick = click;
    }


    public interface ItemClick {
        void listener(View view, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView tvNameMyRecord;
        private final Spinner spinner;

        ViewHolder(View itemView) {
            super(itemView);
            tvNameMyRecord = itemView.findViewById(R.id.tvNameMyRecord);
            spinner = itemView.findViewById(R.id.spinner);
            itemView.setOnClickListener(this);

            final String[] arrSpinner;
            ArrayAdapter<String> adapterFunction;
            arrSpinner = context.getResources().getStringArray(R.array.itemMyRecord);

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
            itemClick.listener(view, getAdapterPosition());
        }
    }

    private class FileExtensionFilterMP3 implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp3") || name.endsWith(".MP3"));
        }
    }
}