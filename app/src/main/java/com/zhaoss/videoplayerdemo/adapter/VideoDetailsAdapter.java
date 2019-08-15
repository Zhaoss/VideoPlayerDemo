package com.zhaoss.videoplayerdemo.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zhaoss.videoplayerdemo.R;
import com.zhaoss.videoplayerdemo.bean.MainVideoBean;
import com.zhaoss.videoplayerdemo.view.PlayTextureView;
import com.zhaoss.videoplayerdemo.view.VideoTouchView;

import java.util.ArrayList;

/**
 * Created by zhaoshuang on 2018/11/12.
 */

public class VideoDetailsAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private ArrayList<MainVideoBean> mainVideoBeanList;

    public VideoDetailsAdapter(Context mContext, ArrayList<MainVideoBean> mainVideoBeanList) {
        this.mContext = mContext;
        this.mainVideoBeanList = mainVideoBeanList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(View.inflate(mContext, R.layout.item_video_details, null));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MyViewHolder vh = (MyViewHolder) holder;
        MainVideoBean mainVideoBean = mainVideoBeanList.get(position);

        vh.tv_progress.setText("");
        vh.pb_play_progress.setSecondaryProgress(0);
        vh.pb_play_progress.setProgress(0);

        Glide.with(mContext).load(mainVideoBean.getAvatarRes()).into(vh.iv_avatar);
        Glide.with(mContext).load(mainVideoBean.getCoverUrl()).into(vh.iv_cover);

        vh.tv_content.setText(mainVideoBean.getContent());
        vh.tv_name.setText(mainVideoBean.getUserName());
    }

    @Override
    public int getItemCount() {
        return mainVideoBeanList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        public VideoTouchView videoTouchView;
        public ImageView iv_cover;
        public PlayTextureView playTextureView;
        public ProgressBar pb_video;
        public ProgressBar pb_play_progress;
        public TextView tv_progress;
        public RelativeLayout rl_change_progress;
        public ImageView iv_change_progress;
        public TextView tv_change_progress;
        private ImageView iv_avatar;
        private TextView tv_name;
        private TextView tv_content;

        public MyViewHolder(View itemView) {
            super(itemView);

            videoTouchView = itemView.findViewById(R.id.videoTouchView);
            playTextureView = itemView.findViewById(R.id.playTextureView);
            iv_cover = itemView.findViewById(R.id.iv_cover);
            pb_video = itemView.findViewById(R.id.pb_video);
            pb_play_progress = itemView.findViewById(R.id.pb_play_progress);
            tv_progress = itemView.findViewById(R.id.tv_progress);
            rl_change_progress = itemView.findViewById(R.id.rl_change_progress);
            iv_change_progress = itemView.findViewById(R.id.iv_change_progress);
            tv_change_progress = itemView.findViewById(R.id.tv_change_progress);
            iv_avatar = itemView.findViewById(R.id.iv_avatar);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_content = itemView.findViewById(R.id.tv_content);

            itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            pb_play_progress.getProgressDrawable().setColorFilter(ContextCompat.getColor(mContext, R.color.white), PorterDuff.Mode.SRC_IN);
        }
    }
}
