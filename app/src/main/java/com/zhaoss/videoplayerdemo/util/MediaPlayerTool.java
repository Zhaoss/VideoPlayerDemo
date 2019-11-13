package com.zhaoss.videoplayerdemo.util;

import android.graphics.SurfaceTexture;
import android.text.TextUtils;
import android.view.Surface;

import com.zhaoss.videoplayerdemo.view.PlayTextureView;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by zhaoshuang on 2018/9/13.
 */

public class MediaPlayerTool implements IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnInfoListener {

    //是否使用自定义的缓存架构
    public static final boolean USE_MY_CHECK = false;

    //ijkio协议
    public static final String IJK_CACHE_HEAD = "ijkio:cache:ffio:";

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
    private boolean loadIjkSucc;

    private SoftReference<PlayTextureView> srPlayTextureView;
    private String mVideoUrl;
    private CacheMediaDataSource mMediaDataSource;

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

    private void checkPath(){
        //初始化缓存路径
        File file = new File(VideoLRUCacheUtil.CACHE_DIR_PATH);
        if(!file.exists()){
            file.mkdirs();
        }
    }

    public void setDataSource(String url){
        setDataSource(url, true);
    }

    public void setDataSource(String url, boolean isCache){
        try {
            if(USE_MY_CHECK){
                mMediaDataSource = new CacheMediaDataSource(url);
                mMediaPlayer.setDataSource(mMediaDataSource);
            }else{
                if(isCache){
                    mVideoUrl = url;
                    mMediaPlayer.setDataSource(mVideoUrl);
                }else{
                    mVideoUrl = IJK_CACHE_HEAD+url;
                    mMediaPlayer.setDataSource(mVideoUrl);
                    if(mMediaPlayer instanceof IjkMediaPlayer) {
                        checkPath();
                        IjkMediaPlayer ijkMediaPlayer = (IjkMediaPlayer) mMediaPlayer;
                        String name = Util.MD5(mVideoUrl);
                        String videoPath = VideoLRUCacheUtil.CACHE_DIR_PATH+name+".v";
                        String indexPath = VideoLRUCacheUtil.CACHE_DIR_PATH+name+".i";
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "cache_file_path", videoPath);
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "cache_map_path", indexPath);
                        VideoLRUCacheUtil.updateVideoCacheBean(name, videoPath, indexPath);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            onError(mMediaPlayer, 0, 0);
        }
    }

    public void prepare(){
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.prepareAsync();
            }
        }catch (Throwable e){
            e.printStackTrace();
            onError(mMediaPlayer, 0, 0);
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

        if(subscribe != null){
            subscribe.dispose();
            subscribe = null;
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
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "parse_cache_map", 1);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "auto_save_map", 1);
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
        if(mVideoUrl.startsWith(IJK_CACHE_HEAD)){
            String rawUrl = mVideoUrl.substring(mVideoUrl.indexOf(IJK_CACHE_HEAD));
            setDataSource(rawUrl, false);
        }else{
            if (mVideoListener != null) {
                mVideoListener.onStop();
                mVideoListener = null;
            }
            if(!TextUtils.isEmpty(mVideoUrl)){
                VideoLRUCacheUtil.deleteVideoBean(mVideoUrl);
            }
            if(mMediaDataSource != null) {
                mMediaDataSource.onError();
            }
        }
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
            loopPlayProgress();
            if (mVideoListener != null) {
                mVideoListener.onStart();
            }
        }
    }

    private Disposable subscribe;
    private void loopPlayProgress(){
        subscribe = Observable.interval(0, 100, TimeUnit.MILLISECONDS)
                .flatMap(new Function<Long, ObservableSource<Long>>() {
                    @Override
                    public ObservableSource<Long> apply(Long aLong) throws Exception {
                        return Observable.just(aLong + 1);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if(mVideoListener!=null && mMediaPlayer!=null && mMediaPlayer.isPlaying()){
                            mVideoListener.onPlayProgress(mMediaPlayer.getCurrentPosition());
                        }else{
                            if(subscribe != null){
                                subscribe.dispose();
                                subscribe = null;
                            }
                        }
                    }
                });
    }
}
