package com.example.jikangwang.resumingdownload.net;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.IntRange;
import android.util.Log;

import com.example.jikangwang.resumingdownload.bean.FileInfo;
import com.example.jikangwang.resumingdownload.bean.RequestInfo;
import com.example.jikangwang.resumingdownload.config.Constants;
import com.example.jikangwang.resumingdownload.config.DownloadStatus;
import com.example.jikangwang.resumingdownload.db.DBimpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask implements Runnable {


    private Context context;
    private DBimpl dBimpl;
    private RequestInfo requestInfo;

    private FileInfo fileInfo;
    private boolean isPause;

    public DownloadTask(Context context, DBimpl dBimpl, RequestInfo requestInfo) {
        this.context = context;
        this.dBimpl = dBimpl;
        this.requestInfo = requestInfo;

        fileInfo=new FileInfo();
        fileInfo.setId(requestInfo.getId());
        fileInfo.setDownloadurl(requestInfo.getUrl());
        fileInfo.setPathurl(requestInfo.getFile().getAbsolutePath());

        FileInfo dbfileinfo=dBimpl.getfile(fileInfo.getId());
        long offset=0;
        long filesize=0;
        if(dbfileinfo!=null){
            offset=dbfileinfo.getDownloadoffset();
            filesize=dbfileinfo.getSize();
            if(offset==0){
                if(requestInfo.getFile().exists()){
                    requestInfo.getFile().delete();
                }
            }else{
                if(!requestInfo.getFile().exists()){
                    dBimpl.deletefile(requestInfo.getId());
                    offset=0;
                    filesize=0;
                }
            }
        }else{
            if(requestInfo.getFile().exists()){
                requestInfo.getFile().delete();
            }
        }

        fileInfo.setDownloadoffset(offset);
        fileInfo.setSize(filesize);
    }

    public int getStatus(){
        if (null != fileInfo){
            return fileInfo.getDownloadstatus();
        }
        return DownloadStatus.FAIL;
    }

    public void setFileStatus( @IntRange(from = DownloadStatus.WAIT, to = DownloadStatus.FAIL)
                                       int status){
        fileInfo.setDownloadstatus(status);
    }

    public void pause(){
        isPause=true;
    }

    @Override
    public void run() {

        Log.i("rusuminga","downloading");
        fileInfo.setDownloadstatus(DownloadStatus.PREPARE);

        Intent intent = new Intent();
        intent.setAction(requestInfo.getAction());
        intent.putExtra(Constants.SERVICE_INTENT_EXTRA,fileInfo);
        context.sendBroadcast(intent);

        RandomAccessFile accessFile = null;
        HttpURLConnection http = null;
        InputStream inStream = null;

        try {
            URL sizeUrl = new URL(requestInfo.getUrl());
            HttpURLConnection sizeHttp = (HttpURLConnection)sizeUrl.openConnection();
            sizeHttp.setRequestMethod("GET");
            sizeHttp.connect();
            long totalSize = sizeHttp.getContentLength();
            sizeHttp.disconnect();

            if (totalSize <= 0){
                if (requestInfo.getFile().exists()){
                    requestInfo.getFile().delete();
                }
                dBimpl.deletefile(requestInfo.getId());
                return;
            }

            fileInfo.setSize(totalSize);
            accessFile = new RandomAccessFile(requestInfo.getFile(), "rwd");

            URL url = new URL(requestInfo.getUrl());
            http = (HttpURLConnection)url.openConnection();
            http.setConnectTimeout(10000);
            http.setRequestProperty("Connection", "Keep-Alive");
            http.setReadTimeout(10000);
            http.setRequestProperty("Range", "bytes=" + fileInfo.getDownloadoffset() + "-");
            http.connect();

            inStream = http.getInputStream();
            byte[] buffer = new byte[1024*8];
            int offset;

            accessFile.seek(fileInfo.getDownloadoffset());
            long  millis = SystemClock.uptimeMillis();
            while ((offset = inStream.read(buffer)) != -1){
                if (isPause){
                    fileInfo.setDownloadstatus(DownloadStatus.PAUSE);
                    isPause = false;
                    dBimpl.savefile(fileInfo);
                    context.sendBroadcast(intent);

                    http.disconnect();
                    accessFile.close();
                    inStream.close();
                    return;
                }
                accessFile.write(buffer,0, offset);
                fileInfo.setDownloadoffset( fileInfo.getDownloadoffset()+offset );
                fileInfo.setDownloadstatus(DownloadStatus.LOADING);

                if (SystemClock.uptimeMillis()-millis >= 1000){
                    millis = SystemClock.uptimeMillis();
                    dBimpl.savefile(fileInfo);
                    context.sendBroadcast(intent);
                }
            }

            fileInfo.setDownloadstatus(DownloadStatus.COMPLETE);
            dBimpl.savefile(fileInfo);
            context.sendBroadcast(intent);
        } catch (Exception e){
            dBimpl.savefile(fileInfo);
            context.sendBroadcast(intent);
            e.printStackTrace();
        } finally {
            try {
                if (accessFile != null){
                    accessFile.close();
                }
                if (inStream != null){
                    inStream.close();
                }
                if (http != null){
                    http.disconnect();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }
}
