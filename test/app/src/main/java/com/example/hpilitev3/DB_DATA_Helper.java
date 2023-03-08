package com.example.hpilitev3;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DATATABLE : 사용자 데이터 정보 저장하는 테이블 Helper
 */

public class DB_DATA_Helper extends SQLiteOpenHelper {


    public DB_DATA_Helper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE if not exists mytable ("
                + "_id integer primary key autoincrement,"
                + "txt text);";

        db.execSQL(sql);

        //db.execSQL(DBCreate.INFOTABLE.);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //db.execSQL("DROP TABLE IF EXISTS "+ Database_Create.CreateDB._TABLENAME0);
        //onCreate(db);
    }
}
