package com.example.jikangwang.resumingdownload;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.example.jikangwang.resumingdownload.bean.FileInfo;
import com.example.jikangwang.resumingdownload.bean.RequestInfo;
import com.example.jikangwang.resumingdownload.config.Constants;
import com.example.jikangwang.resumingdownload.config.DownloadStatus;
import com.example.jikangwang.resumingdownload.db.DBimpl;
import com.example.jikangwang.resumingdownload.net.DownloadExecutor;
import com.example.jikangwang.resumingdownload.net.DownloadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class DownloadService extends Service {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(3, CPU_COUNT/2);
    private static final int MAX_POOL_SIZE =  CORE_POOL_SIZE * 2;
    private static final long KEEP_ALIVE_TIME  = 0L;

    private DownloadExecutor mExecutor = new DownloadExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
            KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>());


    HashMap<String,DownloadTask> mTasks=new HashMap<>();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ArrayList<RequestInfo> requestInfos=(ArrayList<RequestInfo>)intent.getSerializableExtra(Constants.SERVICE_INTENT_EXTRA);

        for(RequestInfo r:requestInfos){
            Log.i("resuminga","browsing requestinfo");
            executedownload(r);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void executedownload(RequestInfo requestInfo) {
        DownloadTask task=mTasks.get(requestInfo.getId());
        DBimpl dBimpl=new DBimpl(getBaseContext());
        FileInfo fileInfo=dBimpl.getfile(requestInfo.getId());
        Log.i("rusuminga","executedownload");


        if(requestInfo.getStatus()==DownloadStatus.DELETE){

            if(task!=null){
                task.pause();
                mTasks.remove(task);
            }
            if(fileInfo!=null){
                dBimpl.deletefile(requestInfo.getId());
            }
            if(requestInfo.getFile().exists()){
                requestInfo.getFile().delete();
            }
            return;
        }
        else if(requestInfo.getStatus()==DownloadStatus.GET){
            if(fileInfo!=null){
                Intent intent = new Intent();
                intent.setAction(requestInfo.getAction());
                intent.putExtra(Constants.SERVICE_INTENT_EXTRA, fileInfo);
                sendBroadcast(intent);
            }
            return;
        }


        if(task==null){
            if(fileInfo==null){ //Task不存在，fileInfo也不存在，那说明是一个完全崭新的任务，直接创建就行
                Log.i("rusuminga","taks==null&&fileinfo==null");
                task=new DownloadTask(this,dBimpl,requestInfo);
            }
            else{   //Task不存在，fileinfo存在，那说明之前的下载被中断了，或者已经下载完成了，检查fileinfo的状态，这种状态下不可能提交暂停请求，因为UI界面已经更新了
                //如果正在下载，或者即将开始下载，那么我们继续从之前的地方下载,这一步的逻辑已经在downloadTask里实现了，所以最后提交一下就可以了
                Log.i("rusuminga","taks==null&&fileinfo!=null");
                if(fileInfo.getDownloadstatus()== DownloadStatus.LOADING||fileInfo.getDownloadstatus()==DownloadStatus.PREPARE){

                }
                else if(fileInfo.getDownloadstatus()==DownloadStatus.COMPLETE){ //下载完成就直接通知就行了
                    if(requestInfo.getFile().exists()){ //如果文件存在，则不需要下载了，直接广播发送下载完成
                        if (!TextUtils.isEmpty(requestInfo.getAction())){
                            Intent intent = new Intent();
                            intent.setAction(requestInfo.getAction());
                            intent.putExtra(Constants.SERVICE_INTENT_EXTRA, fileInfo);
                            sendBroadcast(intent);
                        }
                        return;
                    }else{  //如果文件不存在，则说明之前的白下载了，需要重新下载
                        Log.i("rusuminga","redownload");
                        dBimpl.deletefile(requestInfo.getId());
                    }
                }
            }
            if(requestInfo.getStatus()==DownloadStatus.WAIT){   //这里不用判断暂停请求，因为根本没task，也暂停不了
                task = new DownloadTask(this, dBimpl,requestInfo);
                mTasks.put(requestInfo.getId(),task);
                Log.i("rusuminga","mTasks.put(requestInfo.getId(),task);");
            }

        }else{  //Task存在则fileInfo一定存在

        }

        if(task!=null){
            if(requestInfo.getStatus()==DownloadStatus.WAIT){
                mExecutor.execute(task);
            }
            else if(requestInfo.getStatus()==DownloadStatus.PAUSE){
                task.pause();
            }
        }




    }


}
