package com.zhaoss.videoplayerdemo;

import android.app.Application;

import com.zhaoss.videoplayerdemo.util.VideoLRUCacheUtil;

/**
 * Created by zhaoshuang on 2018/11/1.
 */

public class MyApplication extends Application {

    public static MyApplication mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        this.mContext = this;

        //清理超过大小和存储时间的视频缓存文件
        VideoLRUCacheUtil.checkCacheSize();
    }
}
