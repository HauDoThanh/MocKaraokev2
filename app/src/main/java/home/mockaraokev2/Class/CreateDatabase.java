package home.mockaraokev2.Class;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 Created by admin on 6/19/2017.
 */

public class CreateDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MOCKaraoke";

    static final String TABLE_NOW = "ListNow";
    static final String TABLE_FAVORITE = "ListFavorite";
    static final String TABLE_HISTORY = "ListHistory";

    static final String ID_VIDEO = "idVideo";
    static final String IMAGE = "Image";
    static final String NAME = "Name";


    public CreateDatabase(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "CREATE TABLE " + TABLE_NOW + " ( " +
                ID_VIDEO + " TEXT PRIMARY KEY, " + NAME + " TEXT, " +
                IMAGE + " TEXT );";

        String sql2 = "CREATE TABLE " + TABLE_FAVORITE + " ( " +
                ID_VIDEO + " TEXT PRIMARY KEY, " + NAME + " TEXT, " +
                IMAGE + " TEXT);";

        String sql3 = "CREATE TABLE " + TABLE_HISTORY + " ( " + NAME + " TEXT PRIMARY KEY);";

        sqLiteDatabase.execSQL(sql);
        sqLiteDatabase.execSQL(sql2);
        sqLiteDatabase.execSQL(sql3);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
