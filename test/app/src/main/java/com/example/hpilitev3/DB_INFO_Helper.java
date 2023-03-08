package com.example.hpilitev3;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * INFOTABLE : 사용자 기기 정보 저장하는 테이블 Helper
 */

public class DB_INFO_Helper extends SQLiteOpenHelper {

    String TableName = "INFOTABLE";
    static String Last_WearDeviceName;
    static String Last_WearDeviceID;
    private static final int DB_VERSION = 3;

    public DB_INFO_Helper(Context context) {
        super(context, "INFOTABLE", null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // DB 생성
       String sql = "CREATE TABLE if not exists " + TableName + "("
                + "_id integer primary key autoincrement,"
                + "UserName varchar(100) not null,"
                + "UserID varchar(100) not null,"
                + "WearDeviceName varchar(100) not null,"
                + "WearDeviceID varchar(100) not null,"
                + "ODroidID varchar(100) not null,"
                + "MAC varchar(100) not null,"
                + "Date integer not null"
                + ");";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        String sql = "DROP TABLE IF EXISTS " + TableName;
        db.execSQL(sql);
        onCreate(db);
    }
    // 강아지 이름 INSERT
    public void UserName_Insert(String UserName){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO " + TableName + " VALUES(NULL, '"
                + UserName + "')");
        db.close();
    }
    // 데이터 INSERT
    public void Insert(String UserName, String UserID, String WearDeviceName, String WearDeviceID, String OdroidID, String Mac, long Date) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO " + TableName + " VALUES(NULL, '"
                + UserName + "', '"
                + UserID + "', '"
                + WearDeviceName + "', '"
                + WearDeviceID + "', '"
                + OdroidID + "', '"
                + Mac + "', "
                + Date + ")");
        db.close();
    }

    // 데이터 조회
    public String GetResult() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM " + TableName, null);
        while (cursor.moveToNext()) {
            result += cursor.getString(1)
                    + cursor.getString(2)
                    + cursor.getString(3)
                    + cursor.getString(4)
                    + cursor.getString(5)
                    + cursor.getString(6)
                    + cursor.getInt(7)
                    + "\n";
        }

        return result;
    }

    public String getPetName() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TableName, null);
        int count = cursor.getCount();
        if (count > 0) {
            cursor = db.rawQuery("SELECT UserName FROM " + TableName
                    + " ORDER BY _id DESC LIMIT 1", null);
            cursor.moveToFirst();
            Log.e("CEX", "getPetName: " + cursor.getString(0));
            return cursor.getString(0);
        }
        else
            return "";
    }

    public String GetLastWearDeviceName() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TableName, null);
        int count = cursor.getCount();
        if (count > 0) {
            cursor = db.rawQuery("SELECT WearDeviceName FROM " + TableName
                    + " ORDER BY _id DESC LIMIT 1", null);
            cursor.moveToFirst();
            return cursor.getString(0);
        }
        else
            return "";
    }

    public String GetLastWearDeviceID() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TableName, null);
        int count = cursor.getCount();

        if (count > 0) {
            cursor = db.rawQuery("SELECT WearDeviceID FROM " + TableName
                    + " ORDER BY _id DESC LIMIT 1", null);
            cursor.moveToFirst();
            return cursor.getString(0);
        }
        else
            return "";
    }

    public String GetRecentdata(int row, int index) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TableName, null);
        int count = cursor.getCount();

        if (count > 0) {
            cursor = db.rawQuery("SELECT _id, UserID, WearDeviceID, Date FROM " + TableName + " ORDER BY _id DESC LIMIT 5",
                    null);
            cursor.moveToPosition(row);

            return cursor.getString(index);
        }
        else
            return "";
    }
}
