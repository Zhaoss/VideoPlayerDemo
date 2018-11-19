package com.zhaoss.videoplayerdemo.util;

import android.graphics.SurfaceTexture;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.view.Surface;

import com.zhaoss.videoplayerdemo.view.PlayTextureView;

import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicInteger;

import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by zhaoshuang on 2018/9/13.
 */

public class MediaPlayerTool implements IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnInfoListener {

    public final static int PLAY_PROGRESS = 1;

    private IMediaPlayer mMediaPlayer;
    private VideoListener mVideoListener;
    private SurfaceTexture playSurfaceTexture;
    //记录上次播放器的hasCode
    private AtomicInteger playHasCode = new AtomicInteger(0);
    //视频旋转播放角度
    private int mRotation;
    //视频时长
    private long mDuration;
    //视频音量
    private float mVolume;

    //加载bilibili库成功
    private boolean loadIjkSucc = false;

    private SoftReference<PlayTextureView> srPlayTextureView;
    private CacheMediaDataSource mMediaDataSource;

    //这里会自动初始化so库 有些手机会找不到so, 会自动使用系统的播放器
    private MediaPlayerTool(){
        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
            loadIjkSucc = true;
        }catch (UnsatisfiedLinkError e){
            e.printStackTrace();
            loadIjkSucc = false;
        }
    }

    public static MediaPlayerTool mMediaPlayerTool;
    public synchronized static MediaPlayerTool getInstance(){

        if(mMediaPlayerTool == null){
            mMediaPlayerTool = new MediaPlayerTool();
        }
        return mMediaPlayerTool;
    }

    public void start(){
        if(mMediaPlayer != null){
            mMediaPlayer.start();
        }
    }

    public void pause() {
        if(mMediaPlayer != null){
            mMediaPlayer.pause();
        }
    }

    public int getVideoWidth(){
        if(mMediaPlayer != null){
            return mMediaPlayer.getVideoWidth();
        }
        return 0;
    }

    public int getVideoHeight(){
        if(mMediaPlayer != null){
            return mMediaPlayer.getVideoHeight();
        }
        return 0;
    }

    public void setLooping(boolean looping) {
        if(mMediaPlayer != null){
            mMediaPlayer.setLooping(looping);
        }
    }

    public boolean isLooping() {
        if(mMediaPlayer != null){
            return mMediaPlayer.isLooping();
        }
        return false;
    }

    public class MyBinder extends Binder{
        public MediaPlayerTool getService(){
            return MediaPlayerTool.this;
        }
    }

    public void onDestroy() {
        reset();
        IjkMediaPlayer.native_profileEnd();
    }

    /**
     * @param volume 0-1
     */
    public void setVolume(float volume){
        if(mMediaPlayer != null) {
            this.mVolume = volume;
            mMediaPlayer.setVolume(volume, volume);
        }
    }

    public float getVolume(){
        return mVolume;
    }

    public long getDuration(){
        return mDuration;
    }

    public long getCurrentPosition(){
        if(mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void setPlayTextureView(PlayTextureView playTextureView){
        if(srPlayTextureView != null){
            srPlayTextureView.clear();
        }
        srPlayTextureView = new SoftReference(playTextureView);
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture){
        this.playSurfaceTexture = surfaceTexture;
        if(mMediaPlayer!=null && surfaceTexture!=null) {
            mMediaPlayer.setSurface(new Surface(surfaceTexture));
        }
    }

    public SurfaceTexture getAvailableSurfaceTexture(){
        cleanTextureViewParent();
        return playSurfaceTexture;
    }

    private void cleanTextureViewParent(){
        if(srPlayTextureView != null) {
            PlayTextureView playTextureView = srPlayTextureView.get();
            if (playTextureView != null) {
                playTextureView.resetTextureView();
            }
        }
    }

    public void seekTo(long msec){
        if(mMediaPlayer != null){
            mMediaPlayer.seekTo(msec);
        }
    }

    public void setDataSource(String url){
        try {
            mMediaDataSource = new CacheMediaDataSource(url);
            mMediaPlayer.setDataSource(mMediaDataSource);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void prepare(){
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.prepareAsync();
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    public int getRotation(){
        return mRotation;
    }

    public boolean isPlaying(){
        if(mMediaPlayer != null){
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    public void reset(){

        if(myHandler.hasMessages(PLAY_PROGRESS)) {
            myHandler.removeMessages(PLAY_PROGRESS);
        }

        if(srPlayTextureView != null){
            PlayTextureView playTextureView = srPlayTextureView.get();
            if(playTextureView != null){
                playTextureView.resetTextureView();
            }
            srPlayTextureView.clear();
            srPlayTextureView = null;
        }

        if(playSurfaceTexture != null) {
            playSurfaceTexture.release();
            playSurfaceTexture = null;
        }

        if(mVideoListener != null){
            mVideoListener.onStop();
            mVideoListener = null;
        }

        if (mMediaDataSource != null) {
            mMediaDataSource = null;
        }

        if(mMediaPlayer!=null && playHasCode.get()!=mMediaPlayer.hashCode()) {
            playHasCode.set(mMediaPlayer.hashCode());
            final IMediaPlayer releaseMediaPlay = mMediaPlayer;
            mMediaPlayer = null;
            RxJavaUtil.run(new RxJavaUtil.OnRxAndroidListener<Object>() {
                @Override
                public Object doInBackground() throws Throwable {
                    releaseMediaPlay.stop();
                    releaseMediaPlay.release();
                    return null;
                }
                @Override
                public void onFinish(Object result) {
                }
                @Override
                public void onError(Throwable e) {
                }
            });
        }
    }

    public void setVideoListener(VideoListener videoListener){
        this.mVideoListener = videoListener;
    }

    public static abstract class VideoListener {
        //视频开始播放
        public void onStart(){};
        //视频被停止播放
        public void onStop(){};
        //视频播放完成
        public void onCompletion(){};
        //视频旋转角度参数初始化完成
        public void onRotationInfo(int rotation){};
        //播放进度 0-1
        public void onPlayProgress(long currentPosition){};
        //缓存速度 1-100
        public void onBufferProgress(int progress){};
    }

    public void initMediaPLayer(){

        if(loadIjkSucc){
            IjkMediaPlayer ijkMediaPlayer = new IjkMediaPlayer();
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);

            mMediaPlayer = ijkMediaPlayer;
        }else{
            mMediaPlayer = new AndroidMediaPlayer();
        }

        try {
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnInfoListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setOnPreparedListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
        switch (what){
            case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED://播放旋转
                if(mVideoListener != null){
                    mRotation = extra;
                    mVideoListener.onRotationInfo(extra);
                }
                break;
        }
        return true;
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        if(mVideoListener != null){
            mVideoListener.onCompletion();
        }
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int what, int extra) {
        if(mMediaDataSource != null) {
            mMediaDataSource.onError();
        }
        reset();
        return true;
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {
        if(mVideoListener!=null){
            mVideoListener.onBufferProgress(percent);
        }
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        if(mMediaPlayer != null) {
            mMediaPlayer.start();
            mDuration = iMediaPlayer.getDuration();
            myHandler.sendEmptyMessage(PLAY_PROGRESS);
            if (mVideoListener != null) {
                mVideoListener.onStart();
            }
        }
    }

    private Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case PLAY_PROGRESS:
                    if(mVideoListener!=null && mMediaPlayer!=null && mMediaPlayer.isPlaying()){
                        mVideoListener.onPlayProgress(mMediaPlayer.getCurrentPosition());
                    }
                    myHandler.sendEmptyMessageDelayed(PLAY_PROGRESS, 100);
                    break;
            }
        }
    };
}
