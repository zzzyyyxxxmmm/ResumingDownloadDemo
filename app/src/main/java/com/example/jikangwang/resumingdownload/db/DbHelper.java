package com.example.jikangwang.resumingdownload.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.jikangwang.resumingdownload.config.DBConfig;

public class DbHelper extends SQLiteOpenHelper {

    public DbHelper(Context context) {
        super(context, DBConfig.dbname, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql="create table if not exists "+DBConfig.tablename+"("+
                DBConfig.id+ " varchar(500),"+
                DBConfig.downloadurl+" varchar(100),"+
                DBConfig.pathurl+" varchar(100),"+
                DBConfig.filesize+" integer,"+
                DBConfig.downloadoffset+" integer,"+
                DBConfig.downloadstatus+" integer)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
