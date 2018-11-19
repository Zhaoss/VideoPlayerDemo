package com.zhaoss.videoplayerdemo.activity;

import android.Manifest;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.zhaoss.videoplayerdemo.R;
import com.zhaoss.videoplayerdemo.adapter.MainAdapter;
import com.zhaoss.videoplayerdemo.bean.MainVideoBean;
import com.zhaoss.videoplayerdemo.util.DataUtil;
import com.zhaoss.videoplayerdemo.util.MediaPlayerTool;
import com.zhaoss.videoplayerdemo.util.MyUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private RecyclerView rv_video;
    private MediaPlayerTool mMediaPlayerTool;
    private ArrayList<MainVideoBean> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv_video = findViewById(R.id.rv_video);

        rv_video.setLayoutManager(new LinearLayoutManager(mContext));
        dataList = DataUtil.createData();
        rv_video.setAdapter(new MainAdapter(this, dataList));

        rv_video.addItemDecoration(new RecyclerView.ItemDecoration(){
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

                int position = parent.getChildAdapterPosition(view);
                if (position != 0) {
                    outRect.top = (int) getResources().getDimension(R.dimen.activity_margin2);
                }
            }
        });

        rv_video.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(currentPlayView!=null){
                    boolean playRange = isPlayRange(currentPlayView, recyclerView);
                    if(!playRange){
                        mMediaPlayerTool.reset();
                    }
                }
            }
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //检测播放视频
                    checkPlayVideo();
                    if(currentPlayView == null){
                        playVideoByPosition(-1);
                    }
                }
            }
        });

        mMediaPlayerTool = MediaPlayerTool.getInstance();

        AndPermission.with(mContext).permission(Manifest.permission.WRITE_EXTERNAL_STORAGE).callback(new PermissionListener() {
            @Override
            public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
            }
            @Override
            public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {

            }
        }).start();
    }

    boolean isFirst = true;
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(isFirst){
            isFirst = false;
            refreshVideo();
        }
    }

    //检测是否播放视频
    private void checkPlayVideo(){

        currentPlayIndex = 0;
        videoPositionList.clear();

        int childCount = rv_video.getChildCount();
        for (int x = 0; x < childCount; x++) {
            View childView = rv_video.getChildAt(x);
            boolean playRange = isPlayRange(childView.findViewById(R.id.rl_video), rv_video);
            if(playRange){
                int position = rv_video.getChildAdapterPosition(childView);
                if(position>=0 && !videoPositionList.contains(position)){
                    videoPositionList.add(position);
                }
            }
        }
    }

    //检查子view是否在父view显示布局里面
    private boolean isPlayRange(View childView, View parentView){

        if(childView==null || parentView==null){
            return false;
        }

        int[] childLocal = new int[2];
        childView.getLocationOnScreen(childLocal);

        int[] parentLocal = new int[2];
        parentView.getLocationOnScreen(parentLocal);

        boolean playRange = childLocal[1]>=parentLocal[1] &&
                childLocal[1]<=parentLocal[1]+parentView.getHeight()-childView.getHeight();

        return playRange;
    }

    MediaPlayerTool.VideoListener myVideoListener;
    //当前播放的视频角标
    int currentPlayIndex;
    //可以播放的视频集合
    ArrayList<Integer> videoPositionList = new ArrayList<>();
    View currentPlayView;
    /**
     * 播放视频
     * @param resumePosition 是否继续播放 否则可以传-1
     */
    private void playVideoByPosition(int resumePosition){

        boolean isResumePlay = resumePosition >= 0;

        if(!isResumePlay && (videoPositionList.size()==0 || mMediaPlayerTool ==null)){
            return ;
        }

        if(!isResumePlay){
            //一定要先重置播放器
            mMediaPlayerTool.reset();
        }

        int playPosition = 0;
        if(isResumePlay){
            playPosition = resumePosition;
        }else{
            if(currentPlayIndex >= videoPositionList.size()){
                currentPlayIndex = 0;
            }
            playPosition = videoPositionList.get(currentPlayIndex);
        }

        //根据传进来的position找到对应的ViewHolder
        final MainAdapter.MyViewHolder vh = (MainAdapter.MyViewHolder) rv_video.findViewHolderForAdapterPosition(playPosition);
        if(vh == null){
            return ;
        }

        currentPlayView = vh.rl_video;

        //初始化一些播放状态, 如进度条,播放按钮,加载框等
        if(isResumePlay){
            vh.pb_video.setVisibility(View.GONE);
            vh.iv_play_icon.setVisibility(View.GONE);
            vh.iv_cover.setVisibility(View.GONE);
        }else{
            //显示正在加载的界面
            vh.iv_play_icon.setVisibility(View.GONE);
            vh.pb_video.setVisibility(View.VISIBLE);
            vh.iv_cover.setVisibility(View.VISIBLE);
            vh.tv_play_time.setText("");

            mMediaPlayerTool.initMediaPLayer();

            String videoUrl = dataList.get(playPosition).getVideoUrl();
            mMediaPlayerTool.setDataSource(videoUrl);
        }

        mMediaPlayerTool.setVolume(0);
        myVideoListener = new MediaPlayerTool.VideoListener() {
            @Override
            public void onStart() {
                vh.iv_play_icon.setVisibility(View.GONE);
                vh.pb_video.setVisibility(View.GONE);
                //防止闪屏
                vh.iv_cover.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        vh.iv_cover.setVisibility(View.GONE);
                    }
                }, 300);
            }
            @Override
            public void onStop() {
                vh.pb_video.setVisibility(View.GONE);
                vh.iv_cover.setVisibility(View.VISIBLE);
                vh.iv_play_icon.setVisibility(View.VISIBLE);
                vh.tv_play_time.setText("");
                currentPlayView = null;
            }
            @Override
            public void onCompletion() {
                currentPlayIndex++;
                playVideoByPosition(-1);
            }
            @Override
            public void onRotationInfo(int rotation) {
                vh.playTextureView.setRotation(rotation);
            }
            @Override
            public void onPlayProgress(long currentPosition) {
                String date = MyUtil.fromMMss(mMediaPlayerTool.getDuration() - currentPosition);
                vh.tv_play_time.setText(date);
            }
        };
        mMediaPlayerTool.setVideoListener(myVideoListener);

        if(isResumePlay){
            //把播放器当前绑定的SurfaceTexture取出起来, 设置给当前界面的TextureView
            vh.playTextureView.resetTextureView(mMediaPlayerTool.getAvailableSurfaceTexture());
            mMediaPlayerTool.setPlayTextureView(vh.playTextureView);
            vh.playTextureView.postInvalidate();
        }else {
            vh.playTextureView.resetTextureView();
            mMediaPlayerTool.setPlayTextureView(vh.playTextureView);
            mMediaPlayerTool.setSurfaceTexture(vh.playTextureView.getSurfaceTexture());
            mMediaPlayerTool.prepare();
        }
    }

    //跳转页面时是否关闭播放器
    private int jumpVideoPosition = -1;
    public void jumpNotCloseMediaPlay(int position){
        jumpVideoPosition = position;
    }

    public void refreshVideo(){

        if(mMediaPlayerTool !=null) {
            mMediaPlayerTool.reset();
            checkPlayVideo();
            playVideoByPosition(-1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //检测是否继续播放视频
        if(jumpVideoPosition!=-1 &&
                (videoPositionList.size()>currentPlayIndex && jumpVideoPosition==videoPositionList.get(currentPlayIndex))
                && mMediaPlayerTool!=null && mMediaPlayerTool.isPlaying()){
            playVideoByPosition(jumpVideoPosition);
        }else{
            refreshVideo();
        }
        jumpVideoPosition = -1;

    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mMediaPlayerTool != null) {
            //如果要跳转播放, 那么不关闭播放器
            if (videoPositionList.size()>currentPlayIndex && jumpVideoPosition==videoPositionList.get(currentPlayIndex)) {
                rv_video.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (myVideoListener != null) {
                            myVideoListener.onStop();
                        }
                    }
                }, 300);
            } else {
                mMediaPlayerTool.reset();
                if (!videoPositionList.contains(jumpVideoPosition)) {
                    videoPositionList.add(jumpVideoPosition);
                }
                currentPlayIndex = videoPositionList.indexOf(jumpVideoPosition);
            }
        }
    }
}
