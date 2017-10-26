package home.mockaraokev2.Class;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import home.mockaraokev2.R;

/**
 Created by buimi on 7/11/2017.
 */

public class ModelHistory {


    private final SQLiteDatabase database;

    //fix 23-07
    private final Button[] btn = new Button[10];
    private SearchView searchView;
    private LinearLayout lineHistory;
    private Context context;
    private int countButton;
    //End fix

    private static ModelHistory instance = null;

    private ModelHistory(Context context) {
        CreateDatabase create = new CreateDatabase(context);
        database = create.getWritableDatabase();
    }

    //fix 23-07
    public void setSearchView(SearchView searchView) {
        this.searchView = searchView;
    }

    public void setLineHistory(LinearLayout lineHistory) {
        this.lineHistory = lineHistory;
    }

    public void setContext(Context context) {
        this.context = context;
    }
    //End fix


    public static ModelHistory Instances(Context context) {
        if (instance == null) {
            instance = new ModelHistory(context);
        }
        return instance;
    }

    public boolean addHistoryVideo(String s) {
        //Thêm dữ liệu
        ContentValues values = new ContentValues();
        values.put(CreateDatabase.NAME, s);

        database.insert(CreateDatabase.TABLE_HISTORY, null, values);

        Log.i("HD", "Da thêm dữ liệu thành công: " + s);

        //Kiểm tra vượt 7 ki tự
        Cursor c = database.query(CreateDatabase.TABLE_HISTORY, null, null, null, null, null, null);
        c.moveToFirst();
        String sFirst = c.getString(c.getColumnIndex(CreateDatabase.NAME));

        if (countRecordData() > 7) {
            database.delete(CreateDatabase.TABLE_HISTORY, CreateDatabase.NAME + "=?", new String[]{sFirst});
        }
        //fix 23-07
        //Cập nhật lại các button
        c.moveToLast();
        for (int i = 0; i < countButton; i++) {
            btn[i].setText(c.getString(0));
            c.moveToPrevious();
        }

        for (int i = countButton; i < countRecordData(); i++) {
            countButton++;
            setNewButton(i, c);
            c.moveToPrevious();
        }
        //End fix

        Log.i("HD", "Đã cập nhật các nút xong");
        c.close();
        return true;
    }

    //Đếm số đòng lịch sử
    private int countRecordData() {
        String sql = "SELECT * FROM " + CreateDatabase.TABLE_HISTORY;

        Cursor cursor = database.rawQuery(sql, null, null);
        int x = cursor.getCount();
        cursor.close();
        return x;
    }

    public void loadDataIntoButton() {
        Cursor c = database.query(CreateDatabase.TABLE_HISTORY, null, null, null, null, null, null);
        c.moveToLast();
        Log.i("HD", "......................");

        for (int i = 0; i < countRecordData(); i++) {

            //Khởi tạo đếm số nút
            //fix 23-07
            countButton = countRecordData();
            setNewButton(i, c);
            //End fix

            c.moveToPrevious();
        }
        c.close();

    }

    //fix 23-07
    private void setNewButton(int i, Cursor c) {
        btn[i] = new Button(context);
        btn[i].setText(c.getString(0));
        btn[i].setBackgroundResource(R.drawable.duongvien);
        btn[i].setAllCaps(false);
        btn[i].setGravity(Gravity.CENTER);
        btn[i].setPadding(5, 0, 5, 0);
        btn[i].setTextColor(Color.parseColor("#5c1a0f"));
        final int finalI = i;

        Log.i("HD", i + "_" + btn[i].getText().toString());

        btn[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                searchView.setQuery(btn[finalI].getText().toString(), true);
            }
        });

        int highText = 20 + ((int) btn[i].getTextSize());
        LinearLayoutCompat.LayoutParams lp = new LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT, highText);
        lp.setMargins(0, 5, 0, 5);
        lineHistory.addView(btn[i], lp);

        Log.i("HD", "dã tạo nút " + (i + 1) + ": " + c.getString(0));
    }
    //End fix
}
