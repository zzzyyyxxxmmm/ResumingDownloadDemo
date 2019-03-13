package com.example.jikangwang.resumingdownload.bean;

import java.io.Serializable;

public class FileInfo implements Serializable{
    private String id;
    private String downloadurl;
    private String pathurl;
    private long downloadoffset;
    private long size;

    private int downloadstatus;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDownloadurl() {
        return downloadurl;
    }

    public void setDownloadurl(String downloadurl) {
        this.downloadurl = downloadurl;
    }

    public String getPathurl() {
        return pathurl;
    }

    public void setPathurl(String pathurl) {
        this.pathurl = pathurl;
    }

    public long getDownloadoffset() {
        return downloadoffset;
    }

    public void setDownloadoffset(long downloadoffset) {
        this.downloadoffset = downloadoffset;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getDownloadstatus() {
        return downloadstatus;
    }

    public void setDownloadstatus(int downloadstatus) {
        this.downloadstatus = downloadstatus;
    }
}
