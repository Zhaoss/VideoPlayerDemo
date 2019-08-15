package com.zhaoss.videoplayerdemo.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zhaoss.videoplayerdemo.R;
import com.zhaoss.videoplayerdemo.activity.MainActivity;
import com.zhaoss.videoplayerdemo.bean.MainVideoBean;
import com.zhaoss.videoplayerdemo.util.IntentUtil;
import com.zhaoss.videoplayerdemo.view.PlayTextureView;

import java.util.ArrayList;

/**
 * Created by zhaoshuang on 2018/11/1.
 */

public class MainAdapter extends RecyclerView.Adapter{

    private MainActivity mContext;
    private ArrayList<MainVideoBean> mainVideoBeanList;

    public MainAdapter(MainActivity context, ArrayList<MainVideoBean> mainVideoBeanList){
        this.mContext = context;
        this.mainVideoBeanList = mainVideoBeanList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(View.inflate(mContext, R.layout.item_video, null));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

        final MyViewHolder vh = (MyViewHolder) holder;
        MainVideoBean mainVideoBean = mainVideoBeanList.get(position);

        Glide.with(mContext).load(mainVideoBean.getAvatarRes()).into(vh.iv_avatar);
        vh.tv_content.setText(mainVideoBean.getContent());
        vh.tv_name.setText(mainVideoBean.getUserName());

        Glide.with(mContext).load(mainVideoBean.getCoverUrl()).into(vh.iv_cover);

        vh.playTextureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.jumpNotCloseMediaPlay(position);
                IntentUtil.gotoVideoDetailsActivity(mContext, mainVideoBeanList, position, vh.playTextureView);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mainVideoBeanList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        public RelativeLayout rl_video;
        public PlayTextureView playTextureView;
        public ImageView iv_cover;
        public ProgressBar pb_video;
        public ImageView iv_play_icon;
        public TextView tv_play_time;

        private TextView tv_content;
        private ImageView iv_avatar;
        private TextView tv_name;

        public MyViewHolder(View itemView) {
            super(itemView);

            itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            rl_video = itemView.findViewById(R.id.rl_video);
            playTextureView = itemView.findViewById(R.id.playTextureView);
            iv_cover = itemView.findViewById(R.id.iv_cover);
            pb_video = itemView.findViewById(R.id.pb_video);
            iv_play_icon = itemView.findViewById(R.id.iv_play_icon);
            tv_content = itemView.findViewById(R.id.tv_content);
            iv_avatar = itemView.findViewById(R.id.iv_avatar);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_play_time = itemView.findViewById(R.id.tv_play_time);
        }
    }
}
