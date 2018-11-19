package com.zhaoss.videoplayerdemo.util;

import android.content.Context;
import android.os.Environment;

import com.zhaoss.videoplayerdemo.bean.VideoCacheBean;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by zhaoshuang on 2018/10/25.
 * 视频缓存的LRUCache算法
 */

public class VideoLRUCacheUtil {

    //缓存最大空间 200M
    public static final long maxDirSize = 1024*1024*200;
    //缓存最大时间 7天
    public static final long maxCacheTime = 1000*60*60*24*7;

    //更新缓存文件的播放次数和最后播放时间
    public static void updateVideoCacheBean(String md5, String videoPath, long fileSize){

        VideoCacheBean videoCacheBean = VideoCacheDBUtil.query(md5);
        if(videoCacheBean == null){
            videoCacheBean = new VideoCacheBean();
            videoCacheBean.setKey(md5);
            videoCacheBean.setVideoPath(videoPath);
            videoCacheBean.setFileSize(fileSize);
        }
        videoCacheBean.setPlayCount(videoCacheBean.getPlayCount()+1);
        videoCacheBean.setPlayTime(System.currentTimeMillis());

        VideoCacheDBUtil.save(videoCacheBean);
    }

    public static File createTempFile(Context context) throws IOException{
        File tempFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), System.currentTimeMillis()+"");
        if(!tempFile.exists()){
            tempFile.createNewFile();
        }
        return tempFile;
    }

    public static File createCacheFile(Context context, String md5, long fileSize) throws IOException{

        //创建一个视频缓存文件, 在data/data目录下
        File filesDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        File cacheFile = new File(filesDir, md5);
        if(!cacheFile.exists()) {
            cacheFile.createNewFile();
        }
        //将缓存信息存到数据库
        VideoLRUCacheUtil.updateVideoCacheBean(md5, cacheFile.getAbsolutePath(), fileSize);
        return cacheFile;
    }

    public static void checkCacheSize(Context context){

        ArrayList<VideoCacheBean> videoCacheList = VideoCacheDBUtil.query();

        //检查一下数据库里面的缓存文件是否存在
        for (VideoCacheBean bean : videoCacheList){
            if(bean.getFileSize() == 0){
                File videoFile = new File(bean.getVideoPath());
                //如果文件不存在或者文件大小不匹配, 那么删除
                if(!videoFile.exists() && videoFile.length()!=bean.getFileSize()){
                    VideoCacheDBUtil.delete(bean);
                }
            }
        }

        long currentSize = 0;
        long currentTime = System.currentTimeMillis();
        for (VideoCacheBean bean : videoCacheList){
            //太久远的文件删除
            if(currentTime-bean.getPlayTime() > maxCacheTime){
                VideoCacheDBUtil.delete(bean);
            }else {
                //大于存储空间的删除
                if (currentSize + bean.getFileSize() > maxDirSize) {
                    VideoCacheDBUtil.delete(bean);
                } else {
                    currentSize += bean.getFileSize();
                }
            }
        }

        //删除不符合规则的缓存
        deleteDirRoom(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), VideoCacheDBUtil.query());
    }

    private static void deleteDirRoom(File dir, ArrayList<VideoCacheBean> videoCacheList){
        if(dir.exists()) {
            if(dir.isDirectory()) {
                File[] files = dir.listFiles();
                if(files != null){
                    for (File f : files) {
                        deleteDirRoom(f, videoCacheList);
                    }
                }
            }else{
                if(!isVideoExists(dir, videoCacheList)) {
                    dir.delete();
                }
            }
        }
    }

    private static boolean isVideoExists(File file, ArrayList<VideoCacheBean> videoCacheList){
        for (VideoCacheBean bean : videoCacheList) {
            if(file.getAbsolutePath().equals(bean.getVideoPath())){
                return true;
            }
        }
        return false;
    }
}
