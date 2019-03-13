package com.example.jikangwang.resumingdownload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jikangwang.resumingdownload.bean.FileInfo;
import com.example.jikangwang.resumingdownload.bean.RequestInfo;
import com.example.jikangwang.resumingdownload.config.Constants;
import com.example.jikangwang.resumingdownload.config.DownloadStatus;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.button_start_pause) Button startorpause;
    @BindView(R.id.button_cancel) Button cancel;
    @BindView(R.id.textview_status) TextView textView;

    //先获得这个单例对象
    final DownloadHelper mDownloadHelper = DownloadHelper.getInstance();
    public boolean pause;


    File file;

    private static final String FIRST_ACTION = "download_helper_first_action";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        pause=false;
        file=new File(this.getFilesDir().getPath().toString()+"/d.apk");
        IntentFilter filter = new IntentFilter();
        filter.addAction(FIRST_ACTION);

        registerReceiver(receiver, filter);
        mDownloadHelper.gettask("https://dl.google.com/drive/InstallBackupAndSync.dmg", file, FIRST_ACTION).submit(this);
        startorpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download(pause);
                pause=!pause;
                if(pause){
                    startorpause.setText("pause");
                }
                else{
                    startorpause.setText("start");
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
            }
        });




    }

    void delete() {
            mDownloadHelper.deletetask("https://dl.google.com/drive/InstallBackupAndSync.dmg", file, FIRST_ACTION).submit(this);

            Toast.makeText(this,"deleted",Toast.LENGTH_LONG).show();
            progressBar.setProgress(0);
            textView.setText("ready");
    }

    void download(boolean pause){
        if(!pause){

            mDownloadHelper.addtask("https://dl.google.com/drive/InstallBackupAndSync.dmg", file, FIRST_ACTION).submit(this);
        }
        else{
            mDownloadHelper.pausetask("https://dl.google.com/drive/InstallBackupAndSync.dmg", file, FIRST_ACTION).submit(this);
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != intent){
                switch (intent.getAction()){
                    case FIRST_ACTION: {
                        FileInfo firstFileInfo = (FileInfo) intent.getSerializableExtra(Constants.SERVICE_INTENT_EXTRA);
                        textView.setText(String.valueOf(firstFileInfo.getDownloadstatus()));
                        int status=firstFileInfo.getDownloadstatus();
                        if(status==DownloadStatus.LOADING||status==DownloadStatus.PAUSE){
                            float pro = (float) (firstFileInfo.getDownloadoffset()*1.0/ firstFileInfo.getSize());
                            int progress = (int)(pro*100);
                            progressBar.setProgress(progress);
                        }
                        else if(status==DownloadStatus.COMPLETE){

                        }
                        else if(status==DownloadStatus.GET){
                            if(firstFileInfo==null){
                                progressBar.setProgress(0);
                                textView.setText("fail");
                            }
                            else{
                                float pro = (float) (firstFileInfo.getDownloadoffset()*1.0/ firstFileInfo.getSize());
                                int progress = (int)(pro*100);
                                progressBar.setProgress(progress);
                            }
                        }
                        else{
                            progressBar.setProgress(0);
                        }
                    }
                    default:

                }
            }
        }
    };


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
