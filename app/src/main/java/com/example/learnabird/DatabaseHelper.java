package com.example.learnabird;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String COLUMN_NAME="name";
    public static final String COLUMN_INFO="info";
    public static final String COLUMN_IMAGE="image";
    public static final String COLUMN_SOUND="sound";

    private DatabaseHelper db;

    private static final String DATABASE_NAME = "learnabird";
    private static final String TABLE_NAME = "birdinfo";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT,"+
            COLUMN_NAME + " VARCHAR,"+
            COLUMN_INFO + " TEXT,"+
            COLUMN_IMAGE + " VARCHAR,"+
            COLUMN_SOUND+ " VARCHAR)";
    private static final String DROP_TABLE_SQL = "DROP TABLE "+TABLE_NAME;

    private Context context;

    private SQLiteDatabase sqLiteDatabase;

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME,null,DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //sqLiteDatabase.execSQL(DROP_TABLE_SQL);
        sqLiteDatabase = db;
        sqLiteDatabase.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_SQL);
    }


    public boolean addBird(String name, String info, String photo, String sound){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME,name);
        values.put(COLUMN_INFO,info);
        values.put(COLUMN_IMAGE,info);
        values.put(COLUMN_SOUND,info);

        long result;
        try {
            db.execSQL(CREATE_TABLE_SQL);
            result = db.insert(TABLE_NAME,null, values);

        }catch (SQLException sqle){
            Log.e("SQLException in insert","Data error");
            result = -1;

        }

        if(result == -1){
            return false;

        }else{
            return true;
        }
    }

    public Cursor getBirdList(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(CREATE_TABLE_SQL);
        Cursor list = db.rawQuery("SELECT * FROM "+ TABLE_NAME,null);
        return list;
    }

}
