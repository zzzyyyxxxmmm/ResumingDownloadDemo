package com.example.jikangwang.resumingdownload;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.jikangwang.resumingdownload.bean.FileInfo;
import com.example.jikangwang.resumingdownload.bean.RequestInfo;
import com.example.jikangwang.resumingdownload.config.Constants;
import com.example.jikangwang.resumingdownload.config.DownloadStatus;
import com.example.jikangwang.resumingdownload.db.DBimpl;

import java.io.File;
import java.util.ArrayList;

public class DownloadHelper {
    private static volatile DownloadHelper instance;

    private DownloadHelper(){}

    public static DownloadHelper getInstance() {
        if(instance==null){
            synchronized (DownloadHelper.class){
                if(instance==null){
                    instance=new DownloadHelper();
                }
            }
        }
        return instance;
    }

    ArrayList<RequestInfo> requestInfos=new ArrayList<>();

    public void submit(Context context){
        if(requestInfos.size()==0){
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.SERVICE_INTENT_EXTRA,requestInfos);
        context.startService(intent);
        requestInfos.clear();
    }
    public DownloadHelper addtask(String url,File file,String action){
        RequestInfo requestInfo=new RequestInfo(url,file,action, DownloadStatus.WAIT);
        requestInfos.add(requestInfo);
        Log.i("resuminga","addtask successfully");
        return this;
    }

    public DownloadHelper pausetask(String url,File file,String action){
        RequestInfo requestInfo=new RequestInfo(url,file,action,DownloadStatus.PAUSE);
        requestInfos.add(requestInfo);
        return this;
    }
    public DownloadHelper deletetask(String url,File file,String action){
        RequestInfo requestInfo=new RequestInfo(url,file,action,DownloadStatus.DELETE);
        requestInfos.add(requestInfo);
        return this;
    }
    public DownloadHelper gettask(String url,File file,String action){
        RequestInfo requestInfo=new RequestInfo(url,file,action,DownloadStatus.GET);
        requestInfos.add(requestInfo);
        return this;
    }
}
