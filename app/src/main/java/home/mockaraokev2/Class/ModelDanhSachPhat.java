package home.mockaraokev2.Class;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import home.mockaraokev2.Object.VideoObject;

/**
 Created by admin on 6/19/2017.
 */

public class ModelDanhSachPhat {
    private final SQLiteDatabase database;

    private static ModelDanhSachPhat instance = null;

    private ModelDanhSachPhat(Context context) {
        CreateDatabase create = new CreateDatabase(context);
        database = create.getWritableDatabase();
    }

    public static ModelDanhSachPhat Instances(Context context) {
        if (instance == null) {
            instance = new ModelDanhSachPhat(context);
        }
        return instance;
    }

    public boolean addVideoNow(VideoObject video) {
        ContentValues values = new ContentValues();

        values.put(CreateDatabase.ID_VIDEO, video.getId());
        values.put(CreateDatabase.NAME, video.getName());
        values.put(CreateDatabase.IMAGE, video.getImg());

        return database.insert(CreateDatabase.TABLE_NOW, null, values) > 0;
    }

    public ArrayList<VideoObject> getAllVideo() {
        String sql = "SELECT * FROM " + CreateDatabase.TABLE_NOW;
        ArrayList<VideoObject> list = new ArrayList<>();

        Cursor cursor = database.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String id = cursor.getString(cursor.getColumnIndex(CreateDatabase.ID_VIDEO));
            String name = cursor.getString(cursor.getColumnIndex(CreateDatabase.NAME));
            String image = cursor.getString(cursor.getColumnIndex(CreateDatabase.IMAGE));

            list.add(new VideoObject(id, name, image));

            cursor.moveToNext();
        }
        cursor.close();

        return list;
    }

    public boolean deleteVideo(String id) {
        return database.delete(CreateDatabase.TABLE_NOW, CreateDatabase.ID_VIDEO +
                " = " + "'" + id + "'", null) > 0;
    }

    public boolean deleteAll(){
        return database.delete(CreateDatabase.TABLE_NOW, null, null) > 0;
    }
}
