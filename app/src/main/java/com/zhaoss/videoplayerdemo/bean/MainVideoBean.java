package com.zhaoss.videoplayerdemo.bean;

import java.io.Serializable;

/**
 * Created by zhaoshuang on 2018/11/1.
 */

public class MainVideoBean implements Serializable{

    private int avatarRes;
    private String videoUrl;
    private String userName;
    private String content;
    private String coverUrl;

    public MainVideoBean(int avatarRes, String videoUrl, String userName, String content, String coverUrl) {
        this.avatarRes = avatarRes;
        this.videoUrl = videoUrl;
        this.userName = userName;
        this.content = content;
        this.coverUrl = coverUrl;
    }

    public int getAvatarRes() {
        return avatarRes;
    }

    public void setAvatarRes(int avatarRes) {
        this.avatarRes = avatarRes;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
}
