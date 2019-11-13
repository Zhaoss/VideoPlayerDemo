package com.zhaoss.videoplayerdemo.bean;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.enums.AssignType;

/**
 * Created by zhaoshuang on 2018/10/25.
 */

public class VideoCacheBean {

    public static final String PLAY_TIME = "playTime";
    public static final String KEY = "key";

    @PrimaryKey(AssignType.BY_MYSELF)
    @Column(KEY)
    private String key;
    @Column(PLAY_TIME)
    private long playTime;
    @Column("playCount")
    private int playCount;
    @Column("videoPath")
    private String videoPath;
    @Column("indexPath")
    private String indexPath;
    @Column("fileSize")
    private long fileSize;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getPlayTime() {
        return playTime;
    }

    public void setPlayTime(long playTime) {
        this.playTime = playTime;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getIndexPath() {
        return indexPath;
    }

    public void setIndexPath(String indexPath) {
        this.indexPath = indexPath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
