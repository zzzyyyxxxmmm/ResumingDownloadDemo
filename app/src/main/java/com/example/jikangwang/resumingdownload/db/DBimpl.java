package com.example.jikangwang.resumingdownload.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.example.jikangwang.resumingdownload.bean.FileInfo;
import com.example.jikangwang.resumingdownload.config.DBConfig;

import java.io.File;

public class DBimpl {
    private SQLiteDatabase mDb;
    private Context context;

    public DBimpl(Context context) {
        this.context = context;
        mDb=new DbHelper(context).getWritableDatabase();
    }

    public void updateStatus(String id,int state){
        if (TextUtils.isEmpty(id)){
            return;
        }

        ContentValues values = new ContentValues();
        values.put(DBConfig.downloadstatus, state);
        mDb.update(DBConfig.tablename, values, DBConfig.id + " = ?", new String[]{id});
    }
    public FileInfo getfile(String id){
        Cursor cursor = mDb.query(DBConfig.tablename, null, " " + DBConfig.id+ " = ? ", new String[]{id}, null, null, null);
        FileInfo downloadFile = null;
        while (cursor.moveToNext()){
            downloadFile = new FileInfo();
            downloadFile.setId( cursor.getString(cursor.getColumnIndex(DBConfig.id)) );
            downloadFile.setDownloadurl( cursor.getString(cursor.getColumnIndex(DBConfig.downloadurl)) );
            downloadFile.setPathurl( cursor.getString(cursor.getColumnIndex(DBConfig.pathurl)) );
            downloadFile.setSize( cursor.getLong( cursor.getColumnIndex(DBConfig.filesize)) );
            downloadFile.setDownloadoffset( cursor.getLong( cursor.getColumnIndex(DBConfig.downloadoffset)));
            downloadFile.setDownloadstatus( cursor.getInt(cursor.getColumnIndex(DBConfig.downloadstatus)) );

            File file = new File(downloadFile.getPathurl());
            if (!file.exists()){
                deletefile(id);
                return null;
            }
        }
        cursor.close();
        return downloadFile;
    }

    public void savefile(FileInfo fileinfo){
        ContentValues contentValues=new ContentValues();
        contentValues.put(DBConfig.id,fileinfo.getId());
        contentValues.put(DBConfig.downloadurl,fileinfo.getDownloadurl());
        contentValues.put(DBConfig.pathurl,fileinfo.getPathurl());
        contentValues.put(DBConfig.filesize,fileinfo.getSize());
        contentValues.put(DBConfig.downloadoffset,fileinfo.getDownloadoffset());
        contentValues.put(DBConfig.downloadstatus,fileinfo.getDownloadstatus());

        if(fileexist(fileinfo.getId())){
            mDb.update(DBConfig.tablename,contentValues,DBConfig.id+"=?",new String[]{fileinfo.getId()});
        }
        else{
            mDb.insert(DBConfig.tablename,null,contentValues);
        }
    }

    public void deletefile(String id){
        if(fileexist(id)){
            mDb.delete(DBConfig.tablename," "+DBConfig.id+" = ? ",new String[]{id});
        }
    }

    private boolean fileexist(String id){
        Cursor cursor = mDb.query(DBConfig.tablename, null,  " " + DBConfig.id + " = ? ", new String[]{id}, null, null, null);
        boolean has = cursor.moveToNext();
        cursor.close();
        return has;
    }
}
