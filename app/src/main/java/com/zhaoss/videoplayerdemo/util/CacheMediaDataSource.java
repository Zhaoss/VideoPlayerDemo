package com.zhaoss.videoplayerdemo.util;

import com.zhaoss.videoplayerdemo.bean.VideoCacheBean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

/**
 * 自己实现播放器的网络下载
 * Created by zhaoshuang on 2018/11/9.
 */

public class CacheMediaDataSource implements IMediaDataSource {

    private String mVideoData;
    private String mMd5;

    //视频长度
    private long contentLength;
    //url对应的本地视频文件
    private File localVideoFile;
    //是否读取的缓存视频
    private boolean isCacheVideo;
    //网络流
    private InputStream networkInPutStream;
    //本地文件流
    private RandomAccessFile localStream;

    public CacheMediaDataSource(String videoData) {
        this.mVideoData = videoData;
        mMd5 = MD5(videoData);
    }

    public String MD5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(s.getBytes(StandardCharsets.UTF_8));

            final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
            StringBuilder ret = new StringBuilder(bytes.length * 2);
            for (int i=0; i<bytes.length; i++) {
                ret.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
                ret.append(HEX_DIGITS[bytes[i] & 0x0f]);
            }
            return ret.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     *
     * @param position 视频流读取进度
     * @param buffer 要把读取到的数据存到这个数组
     * @param offset 数据开始写入的坐标
     * @param size 本次一共读取数据的大小
     * @throws IOException
     */
    //记录当前读取流的索引
    long mPosition = 0;
    @Override
    public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {

        if(position>=contentLength || localStream==null){
            return -1;
        }

        //是否将此字节缓存到本地
        boolean isWriteVideo = syncInputStream(position);

        //读取的流的长度不能大于contentLength
        if (position+size > contentLength) {
            size -= position+size-contentLength;
        }

        //读取指定大小的视频数据
        byte[] bytes;
        if(isCacheVideo){
            //从本地读取
            bytes = readByteBySize(localStream, size);
        }else{
            //从网络读取
            bytes = readByteBySize(networkInPutStream, size);
        }
        if(bytes != null) {
            //写入到播放器的数组中
            System.arraycopy(bytes, 0, buffer, offset, size);
            if (isWriteVideo && !isCacheVideo) {
                //缓存到本地
                localStream.write(bytes);
            }
            //记录数据流读取到哪步了
            mPosition += size;
        }

        return size;
    }

    //同步数据流
    private boolean syncInputStream(long position) throws IOException {

        boolean isWriteVideo = true;
        //判断两次读取数据是否连续
        if(mPosition != position){
            if(isCacheVideo){
                //如果是本地缓存, 直接跳转到该索引
                localStream.seek(position);
            }else{
                if(mPosition > position){
                    //同步本地缓存流
                    localStream.close();
                    deleteFileByPosition(position);
                    localStream.seek(position);
                }else{
                    isWriteVideo = false;
                }
                networkInPutStream.close();
                //重新开启一个网络流
                networkInPutStream = openHttpClient((int) position);
            }
            mPosition = position;
        }
        return isWriteVideo;
    }

    /**
     * 从inputStream里读取size大小的数据
     */
    private byte[] readByteBySize(InputStream inputStream, int size) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] buf = new byte[size];
        int len;
        while ((len = inputStream.read(buf)) != -1) {
            out.write(buf, 0, len);
            if (out.size() == size) {
                return out.toByteArray();
            } else {
                buf = new byte[size - out.size()];
            }
        }
        return null;
    }

    /**
     * 从inputStream里读取size大小的数据
     */
    private byte[] readByteBySize(RandomAccessFile inputStream, int size) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] buf = new byte[size];
        int len;
        while ((len = inputStream.read(buf)) != -1) {
            out.write(buf, 0, len);
            if (out.size() == size) {
                return out.toByteArray();
            } else {
                buf = new byte[size - out.size()];
            }
        }
        return null;
    }

    /**
     * 删除file一部分字节, 从position到file.size
     */
    private void deleteFileByPosition(long position) throws IOException {

        FileInputStream in = new FileInputStream(localVideoFile);

        File tempFile = VideoLRUCacheUtil.createTempFile();
        FileOutputStream out = new FileOutputStream(tempFile);

        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) != -1) {
            if(position <= len){
                out.write(buf, 0, (int) position);
                out.close();

                in.close();
                localVideoFile.delete();
                tempFile.renameTo(localVideoFile);
                localStream = new RandomAccessFile(localVideoFile, "rw");
                return ;
            }else{
                position -= len;
                out.write(buf, 0, len);
            }
        }
        tempFile.delete();
    }

    @Override
    public long getSize() throws IOException {

        if(networkInPutStream == null) {
            initInputStream();
        }
        return contentLength;
    }

    //初始化一个视频流出来, 可能是本地或网络
    private void initInputStream() throws IOException {

        File file;
        if(!mVideoData.startsWith("http")){
            file = new File(mVideoData);
        }else {
            file = checkCache(mMd5);
        }

        if(file!=null){
            if(file.exists()) {
                //更新一下缓存文件
                VideoLRUCacheUtil.updateVideoCacheBean(mMd5, file.getAbsolutePath(), "");
                //读取的本地缓存文件
                isCacheVideo = true;
                localVideoFile = file;
                //开启一个本地视频流
                localStream = new RandomAccessFile(localVideoFile, "rw");
                contentLength = file.length();
            }else{
                throw new IOException("文件不存在");
            }
        }else {
            //没有缓存 开启一个网络流, 并且开启一个缓存流, 实现视频缓存
            isCacheVideo = false;
            //开启一个网络视频流
            networkInPutStream = openHttpClient(0);
            //要写入的本地缓存文件
            localVideoFile = VideoLRUCacheUtil.createCacheFile(mMd5);
            //要写入的本地缓存视频流
            localStream = new RandomAccessFile(localVideoFile, "rw");
        }
    }

    //检查本地是否有缓存, 2步确认, 数据库中是否存在, 本地文件是否存在
    private File checkCache(String md5){

        //查询数据库
        VideoCacheBean bean = VideoCacheDBUtil.query(md5);
        if(bean != null){
            File file = new File(bean.getVideoPath());
            if(file.exists()){
                return file;
            }
        }
        return null;
    }

    //打开一个网络视频流, 从startIndex开始下载
    private InputStream openHttpClient(int startIndex) throws IOException {

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .header("RANGE", "bytes=" + startIndex + "-")
                .url(mVideoData)
                .get()
                .build();
        Call call = okHttpClient.newCall(request);
        ResponseBody responseBody = call.execute().body();
        if(responseBody != null){
            contentLength = responseBody.contentLength()+startIndex;
            return responseBody.byteStream();
        }else{
            return null;
        }
    }

    public void onError(){
        VideoCacheBean bean = VideoCacheDBUtil.query(mMd5);
        if(bean != null){
            VideoCacheDBUtil.delete(bean);
        }
    }

    @Override
    public void close() throws IOException {
        if(networkInPutStream != null){
            networkInPutStream.close();
            networkInPutStream = null;
        }
        if(localStream != null){
            localStream.close();
            localStream = null;
        }

        if(localVideoFile!=null && localVideoFile.length()!=contentLength){
            localVideoFile.delete();
        }
    }
}
