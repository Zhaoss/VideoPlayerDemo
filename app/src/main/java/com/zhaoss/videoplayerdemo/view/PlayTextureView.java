package com.zhaoss.videoplayerdemo.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by zhaoshuang on 2018/9/25.
 */

public class PlayTextureView extends FrameLayout {

    private TextureView mTextureView;
    private SimpleSurfaceTextureListener mSurfaceTextureListener;
    private int mVideoWidth;
    private int mVideoHeight;
    private SurfaceTexture mSurfaceTexture;

    public PlayTextureView(@NonNull Context context) {
        super(context);
        init();
    }

    public PlayTextureView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayTextureView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){

        initTextureView(null);
    }

    private void initTextureView(SurfaceTexture surfaceTexture) {

        mTextureView = new TextureView(getContext());
        mTextureView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if(surfaceTexture == null) {
            mSurfaceTexture = newSurfaceTexture();
        }else{
            mSurfaceTexture = surfaceTexture;
        }
        mTextureView.setSurfaceTexture(mSurfaceTexture);

        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                if(mSurfaceTextureListener != null){
                    mSurfaceTextureListener.onSurfaceTextureAvailable(surface, width, height);
                }
            }
            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                if(mSurfaceTextureListener != null){
                    mSurfaceTextureListener.onSurfaceTextureSizeChanged(surface, width, height);
                }
            }
            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                if(mSurfaceTextureListener != null){
                    return mSurfaceTextureListener.onSurfaceTextureDestroyed(surface);
                }
                //当view被销毁时 不销毁SurfaceTexture
                return false;
            }
            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                if(mSurfaceTextureListener != null){
                    mSurfaceTextureListener.onSurfaceTextureUpdated(surface);
                }
            }
        });

        addView(mTextureView, 0);
    }

    //视频居中播放
    private void setVideoCenter(float viewWidth, float viewHeight, float videoWidth, float videoHeight){

        Matrix matrix = new Matrix();
        float sx = viewWidth/videoWidth;
        float sy = viewHeight/videoHeight;
        float maxScale = Math.max(sx, sy);

        matrix.preTranslate((viewWidth - videoWidth) / 2, (viewHeight - videoHeight) / 2);
        matrix.preScale(videoWidth/viewWidth, videoHeight/viewHeight);
        matrix.postScale(maxScale, maxScale, viewWidth/2, viewHeight/2);

        mTextureView.setTransform(matrix);
        mTextureView.postInvalidate();
    }

    public void resetTextureView(){
        resetTextureView(null);
    }

    public void resetTextureView(SurfaceTexture surfaceTexture){

        removeView(mTextureView);
        initTextureView(surfaceTexture);
    }

    public void setVideoSize(int videoWidth, int videoHeight){
        this.mVideoWidth = videoWidth;
        this.mVideoHeight = videoHeight;
        mTextureView.post(new Runnable() {
            @Override
            public void run() {
                if(mVideoWidth!=0 && mVideoHeight!=0){
                    setVideoCenter(mTextureView.getWidth(), mTextureView.getHeight(), mVideoWidth, mVideoHeight);
                }
            }
        });
    }

    public void setSurfaceTextureListener(SimpleSurfaceTextureListener surfaceTextureListener){
        this.mSurfaceTextureListener = surfaceTextureListener;
    }

    //初始化SurfaceTexture
    public SurfaceTexture newSurfaceTexture(){

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int texName = textures[0];
        SurfaceTexture surfaceTexture = new SurfaceTexture(texName);
        surfaceTexture.detachFromGLContext();
        return surfaceTexture;
    }

    public SurfaceTexture getSurfaceTexture() {
        //mSurfaceTexture.isReleased() NoSuchMethodError No virtual method isReleased()Z in class Landroid/graphics/SurfaceTexture
        if(mSurfaceTexture != null){
            return mSurfaceTexture;
        }
        return null;
    }

    public abstract static class SimpleSurfaceTextureListener implements TextureView.SurfaceTextureListener {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    }
}