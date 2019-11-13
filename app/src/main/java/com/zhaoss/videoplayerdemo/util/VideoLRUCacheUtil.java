package com.zhaoss.videoplayerdemo.util;

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

    public static final String CACHE_DIR_PATH = Environment.getExternalStorageDirectory() + "/VideoPlayerDemo/";
    //缓存最大空间 200M 单位是字节
    public static final long maxDirSize = 1024*1024*200;
    //缓存最大时间 7天
    public static final long maxCacheTime = 1000*60*60*24*7;

    public static void updateVideoCacheBean(String md5, String videoPath, String indexPath){

        VideoCacheBean videoCacheBean = new VideoCacheBean();
        videoCacheBean.setKey(md5);
        videoCacheBean.setPlayTime(System.currentTimeMillis());
        videoCacheBean.setVideoPath(videoPath);
        videoCacheBean.setIndexPath(indexPath);
        videoCacheBean.setPlayCount(videoCacheBean.getPlayCount()+1);
        VideoCacheDBUtil.save(videoCacheBean);
    }

    public static File createTempFile() throws IOException {
        File tempFile = new File(CACHE_DIR_PATH, System.currentTimeMillis()+"");
        if(!tempFile.exists()){
            tempFile.createNewFile();
        }
        return tempFile;
    }

    public static File createCacheFile(String md5) throws IOException{

        //创建一个视频缓存文件, 在data/data目录下
        File filesDir = new File(CACHE_DIR_PATH);

        File cacheFile = new File(filesDir, md5);
        if(!cacheFile.exists()) {
            cacheFile.createNewFile();
        }
        //将缓存信息存到数据库
        VideoLRUCacheUtil.updateVideoCacheBean(md5, cacheFile.getAbsolutePath(), "");
        return cacheFile;
    }

    public static void checkCacheSize(){

        ArrayList<VideoCacheBean> videoCacheList = VideoCacheDBUtil.query();

        for (VideoCacheBean bean : videoCacheList){
            if(bean.getFileSize() == 0){
                File videoFile = new File(bean.getVideoPath());
                if(videoFile.exists()){
                    bean.setFileSize(videoFile.length());
                    VideoCacheDBUtil.save(bean);
                }else{
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

        deleteDirRoom(new File(CACHE_DIR_PATH), VideoCacheDBUtil.query());
    }

    public static void deleteVideoBean(String url){
        VideoCacheBean bean = VideoCacheDBUtil.query(Util.MD5(url));
        if(bean != null){
            VideoCacheDBUtil.delete(bean);
            new File(bean.getVideoPath()).delete();
        }
    }

    private static void deleteDirRoom(File dir, ArrayList<VideoCacheBean> videoCacheList){
        if(dir.exists()) {
            if(dir.isDirectory()) {
                File[] files = dir.listFiles();
                if(files != null) {
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
            if(file.getAbsolutePath().equals(bean.getVideoPath()) || file.getAbsolutePath().equals(bean.getIndexPath())){
                return true;
            }
        }
        return false;
    }
}