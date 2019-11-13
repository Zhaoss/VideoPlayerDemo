package com.zhaoss.videoplayerdemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.zhaoss.videoplayerdemo.R;
import com.zhaoss.videoplayerdemo.adapter.VideoDetailsAdapter;
import com.zhaoss.videoplayerdemo.bean.MainVideoBean;
import com.zhaoss.videoplayerdemo.util.IntentUtil;
import com.zhaoss.videoplayerdemo.util.MediaPlayerTool;
import com.zhaoss.videoplayerdemo.util.Util;
import com.zhaoss.videoplayerdemo.util.StatusBarUtil;
import com.zhaoss.videoplayerdemo.view.VideoTouchView;

import java.util.ArrayList;

/**
 * Created by zhaoshuang on 2018/11/12.
 */

public class VideoDetailsActivity extends BaseActivity {

    private RecyclerView rv_video_detail;
    private LinearLayoutManager linearLayoutManager;
    private PagerSnapHelper pagerSnapHelper;
    private MediaPlayerTool mMediaPlayerTool;
    private ArrayList<MainVideoBean> dataList;
    private ImageView iv_close;
    private int playPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_details);

        initIntent();
        initUI();

        mMediaPlayerTool = MediaPlayerTool.getInstance();
        rv_video_detail.post(new Runnable() {
            @Override
            public void run() {
                rv_video_detail.scrollToPosition(playPosition);
                rv_video_detail.post(new Runnable() {
                    @Override
                    public void run() {
                        playVisibleVideo(mMediaPlayerTool.isPlaying());
                    }
                });
            }
        });
    }

    private void initIntent() {

        Intent intent = getIntent();
        playPosition = intent.getIntExtra(IntentUtil.INTENT_PLAY_POSITION, 0);
        dataList = (ArrayList<MainVideoBean>) intent.getSerializableExtra(IntentUtil.INTENT_DATA_LIST);
    }

    private void initUI(){

        rv_video_detail = findViewById(R.id.rv_video_detail);
        iv_close = findViewById(R.id.iv_close);

        linearLayoutManager = new LinearLayoutManager(mContext);
        rv_video_detail.setLayoutManager(linearLayoutManager);

        pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(rv_video_detail);

        rv_video_detail.setAdapter(new VideoDetailsAdapter(mContext, dataList));

        rv_video_detail.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    if(pagerSnapHelper.findSnapView(linearLayoutManager) != playView){
                        playVisibleVideo(false);
                    }
                }
            }
        });

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    View playView;
    /**
     * @param isResumePlay 是否继续上个界面播放
     */
    private void playVisibleVideo(boolean isResumePlay){

        View snapView = pagerSnapHelper.findSnapView(linearLayoutManager);
        if(snapView == null){
            return ;
        }
        final int position = linearLayoutManager.getPosition(snapView);
        if(position < 0){
            return ;
        }

        if(!isResumePlay){
            //重置播放器要在前面
            mMediaPlayerTool.reset();
        }

        playView = snapView;
        final VideoDetailsAdapter.MyViewHolder vh = (VideoDetailsAdapter.MyViewHolder) rv_video_detail.getChildViewHolder(playView);

        if(isResumePlay){
            vh.pb_video.setVisibility(View.GONE);
            vh.iv_cover.setVisibility(View.GONE);
            vh.playTextureView.setRotation(mMediaPlayerTool.getRotation());
            vh.playTextureView.setVideoSize(mMediaPlayerTool.getVideoWidth(), mMediaPlayerTool.getVideoHeight());
            setVideoSize(vh, mMediaPlayerTool.getVideoWidth(), mMediaPlayerTool.getVideoHeight());
        }else{
            //显示正在加载的界面
            vh.pb_video.setVisibility(View.VISIBLE);
            vh.iv_cover.setVisibility(View.VISIBLE);

            mMediaPlayerTool.initMediaPLayer();
            mMediaPlayerTool.setDataSource(dataList.get(position).getVideoUrl());
        }

        vh.videoTouchView.setOnTouchSlideListener(new VideoTouchView.OnTouchSlideListener() {
            @Override
            public void onSlide(float distant) {
                if(mMediaPlayerTool == null){
                    return ;
                }
                if(!vh.rl_change_progress.isShown()){
                    vh.rl_change_progress.setVisibility(View.VISIBLE);
                    changeProgressTime = mMediaPlayerTool.getCurrentPosition();
                }
                changeProgressText(vh, distant);
            }
            @Override
            public void onUp() {
                if(vh.rl_change_progress.isShown()){
                    vh.rl_change_progress.setVisibility(View.GONE);
                }
                mMediaPlayerTool.seekTo(changeProgressTime);
            }
            @Override
            public void onClick() {
                mContext.onBackPressed();
            }
        });

        mMediaPlayerTool.setVolume(1);
        mMediaPlayerTool.setVideoListener(new MediaPlayerTool.VideoListener() {
            @Override
            public void onStart() {
                vh.pb_video.setVisibility(View.GONE);
                vh.iv_cover.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        vh.iv_cover.setVisibility(View.GONE);
                    }
                }, 200);
                vh.playTextureView.setVideoSize(mMediaPlayerTool.getVideoWidth(), mMediaPlayerTool.getVideoHeight());
                setVideoSize(vh, mMediaPlayerTool.getVideoWidth(), mMediaPlayerTool.getVideoHeight());
            }
            @Override
            public void onRotationInfo(int rotation) {
                vh.playTextureView.setRotation(rotation);
            }
            @Override
            public void onStop() {
                vh.pb_video.setVisibility(View.GONE);
                vh.iv_cover.setVisibility(View.VISIBLE);
                vh.pb_play_progress.setSecondaryProgress(0);
                vh.pb_play_progress.setProgress(0);
                vh.tv_progress.setText("");
                playView = null;
            }
            @Override
            public void onCompletion() {
                onStop();
                if(position+1 >= dataList.size()) {
                    rv_video_detail.smoothScrollToPosition(0);
                }else{
                    rv_video_detail.smoothScrollToPosition(position+1);
                }
            }
            @Override
            public void onPlayProgress(long currentPosition) {
                int pro = (int) (currentPosition*1f/ mMediaPlayerTool.getDuration()*100);
                vh.pb_play_progress.setProgress(pro);

                String currentPositionStr = Util.fromMMss(currentPosition);
                String videoDurationStr = Util.fromMMss(mMediaPlayerTool.getDuration());
                vh.tv_progress.setText(currentPositionStr + "/" + videoDurationStr);
            }
            @Override
            public void onBufferProgress(int progress) {
                vh.pb_play_progress.setSecondaryProgress(progress);
            }
        });

        if(isResumePlay){
            vh.playTextureView.resetTextureView(mMediaPlayerTool.getAvailableSurfaceTexture());
            mMediaPlayerTool.setPlayTextureView(vh.playTextureView);
            vh.playTextureView.postInvalidate();
        }else{
            vh.playTextureView.resetTextureView();
            mMediaPlayerTool.setPlayTextureView(vh.playTextureView);
            mMediaPlayerTool.setSurfaceTexture(vh.playTextureView.getSurfaceTexture());
            mMediaPlayerTool.prepare();
        }
    }

    private void setVideoSize(VideoDetailsAdapter.MyViewHolder vh, int videoWidth, int videoHeight){

        float videoRatio = videoWidth * 1f / videoHeight;
        int windowWidth = Util.getWindowWidth();
        int windowHeight = Util.getWindowHeight() + StatusBarUtil.getStatusHeight(mContext);
        float windowRatio = Util.getWindowWidth()*1f/ Util.getWindowHeight();
        ViewGroup.LayoutParams layoutParams = vh.videoTouchView.getLayoutParams();
        if (videoRatio >= windowRatio) {
            layoutParams.width = windowWidth;
            layoutParams.height = (int) (layoutParams.width / videoRatio);
        } else {
            layoutParams.height = windowHeight;
            layoutParams.width = (int) (layoutParams.height * videoRatio);
        }
        vh.videoTouchView.setLayoutParams(layoutParams);
    }

    long changeProgressTime;
    private void changeProgressText(VideoDetailsAdapter.MyViewHolder vh, float distant){

        float radio = distant/vh.pb_play_progress.getWidth();
        changeProgressTime += mMediaPlayerTool.getDuration()*radio;

        if(changeProgressTime < 0){
            changeProgressTime = 0;
        }
        if(changeProgressTime > mMediaPlayerTool.getDuration()){
            changeProgressTime = mMediaPlayerTool.getDuration();
        }

        String changeTimeStr = Util.fromMMss(changeProgressTime);
        String rawTime = Util.fromMMss(mMediaPlayerTool.getDuration());
        vh.tv_change_progress.setText(changeTimeStr+" / "+rawTime);

        if(changeProgressTime > mMediaPlayerTool.getCurrentPosition()){
            vh.iv_change_progress.setImageResource(R.mipmap.video_fast_forward);
        }else{
            vh.iv_change_progress.setImageResource(R.mipmap.video_fast_back);
        }
    }

    boolean isFirst = true;
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(isFirst){
            isFirst = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!isFirst && mMediaPlayerTool !=null && !mMediaPlayerTool.isPlaying()){
            playVisibleVideo(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mMediaPlayerTool!=null && mMediaPlayerTool.isPlaying()) {
            if (dontPause) {
                View snapView = pagerSnapHelper.findSnapView(linearLayoutManager);
                if(snapView!=null && linearLayoutManager.getPosition(snapView)!=playPosition){
                    mMediaPlayerTool.reset();
                }
            } else {
                mMediaPlayerTool.reset();
            }
        }
    }

    boolean dontPause;
    @Override
    public void finish() {
        super.finish();

        dontPause = true;
    }
}
