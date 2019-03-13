package com.example.jikangwang.resumingdownload.bean;

import java.io.File;
import java.io.Serializable;

public class RequestInfo implements Serializable{
    private String url;
    private File file;
    private String action;
    private int status;

    public RequestInfo(String url, File file, String action, int status) {
        this.url = url;
        this.file = file;
        this.action = action;
        this.status = status;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
    public String getId(){
        return url + file.getAbsolutePath();
    }
}
