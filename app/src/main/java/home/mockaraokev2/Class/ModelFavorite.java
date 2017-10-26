package home.mockaraokev2.Class;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import home.mockaraokev2.Object.VideoObject;

/**
 Created by admin on 6/19/2017.
 */

public class ModelFavorite {
    private final SQLiteDatabase database;

    private static ModelFavorite instance = null;

    private ModelFavorite(Context context) {
        CreateDatabase create = new CreateDatabase(context);
        database = create.getWritableDatabase();
    }

    public static ModelFavorite Instances(Context context) {
        if (instance == null) {
            instance = new ModelFavorite(context);
        }
        return instance;
    }

    public boolean  delete(String id) {
        return database.delete(CreateDatabase.TABLE_FAVORITE,
                CreateDatabase.ID_VIDEO + " = " + "'" + id + "'", null) > 0;
    }

    public void dropTable() {
        database.delete(CreateDatabase.TABLE_FAVORITE, null, null);
    }

    public boolean addFavoriteVideo(VideoObject videoObject) {
        ContentValues values = new ContentValues();
        values.put(CreateDatabase.ID_VIDEO, videoObject.getId());
        values.put(CreateDatabase.NAME, videoObject.getName());
        values.put(CreateDatabase.IMAGE, videoObject.getImg());

        return database.insert(CreateDatabase.TABLE_FAVORITE, null, values) > 0;
    }

    public ArrayList<VideoObject> getFavoriteVideo() {
        String sql = "SELECT * FROM " + CreateDatabase.TABLE_FAVORITE;
        ArrayList<VideoObject> list = new ArrayList<>();

        Cursor cursor = database.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String id = cursor.getString(cursor.getColumnIndex(CreateDatabase.ID_VIDEO));
            String name = cursor.getString(cursor.getColumnIndex(CreateDatabase.NAME));
            String image = cursor.getString(cursor.getColumnIndex(CreateDatabase.IMAGE));

            list.add(new VideoObject(id, name, image));
            Log.e("ooo", new VideoObject(id, name, image).toString());
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    public boolean checkVideo(String id){
        String sql = "SELECT idVideo FROM " + CreateDatabase.TABLE_FAVORITE + " WHERE " +
                CreateDatabase.ID_VIDEO + " = " + "'" + id + "'";
        Cursor cursor = database.rawQuery(sql, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }
}
