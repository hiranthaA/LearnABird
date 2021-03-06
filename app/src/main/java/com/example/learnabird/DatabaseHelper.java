package com.example.learnabird;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*
* DatabaseHelper
* Handle all the database related activities
*/
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String COLUMN_NAME="name";
    public static final String COLUMN_INFO="info";
    public static final String COLUMN_IMAGE="image";
    public static final String COLUMN_SOUND="sound";

    private Context context;
    private SQLiteDatabase sqLiteDatabase;
    private static final String DATABASE_NAME = "learnabirddb";
    private static final String TABLE_NAME = "birdinfo";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT,"+
            COLUMN_NAME + " VARCHAR,"+
            COLUMN_INFO + " TEXT,"+
            COLUMN_IMAGE + " VARCHAR,"+
            COLUMN_SOUND+ " VARCHAR)";

    private static final String DROP_TABLE_SQL = "DROP TABLE "+TABLE_NAME;


    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME,null,DATABASE_VERSION);
        this.context = context;
    }

    /*
    Initialize database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        sqLiteDatabase = db;
        sqLiteDatabase.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_SQL);
    }

    /*
    Add new raw to the database
    return true if success
     */
    public boolean addBird(String name, String info, String photo, String sound){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME,name);
        values.put(COLUMN_INFO,info);
        values.put(COLUMN_IMAGE,photo);
        values.put(COLUMN_SOUND,sound);

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

    /*
    get all stored data from the database
     */
    public Cursor getBirdList(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(CREATE_TABLE_SQL);
        Cursor list = db.rawQuery("SELECT ID,name,info,image,sound FROM "+ TABLE_NAME,null);
        return list;
    }

    /*
    delete a given entry using the ID of the record
     */
    public boolean deleteBird(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "ID=?",new String[]{Integer.toString(id)}) > 0;
    }

    /*
    update a given row with given details by using ID field
     */
    public boolean updateBird(int id, String name, String info, String photo, String sound){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME,name);
        contentValues.put(COLUMN_INFO,info);
        contentValues.put(COLUMN_IMAGE,photo);
        contentValues.put(COLUMN_SOUND,sound);
        return db.update(TABLE_NAME,contentValues,"ID=?",new String[]{Integer.toString(id)}) > 0;
    }
}
