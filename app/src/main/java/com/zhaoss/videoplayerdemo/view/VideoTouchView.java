package com.zhaoss.videoplayerdemo.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.zhaoss.videoplayerdemo.R;

/**
 * Created by zhaoshuang on 2018/10/15.
 */

public class VideoTouchView extends FrameLayout {

    private OnTouchSlideListener onTouchSlideListener;
    private float slideMove;
    private float slideClick;

    public VideoTouchView(@NonNull Context context) {
        super(context);
        init();
    }

    public VideoTouchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoTouchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        slideMove = getResources().getDimension(R.dimen.dp10);
        slideClick = getResources().getDimension(R.dimen.dp5);
    }

    float downX;
    float downY;
    boolean isSlideing;
    boolean dontSlide;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getRawX();
                downY = event.getRawY();
            case MotionEvent.ACTION_MOVE:
                if(onTouchSlideListener!=null){
                    float moveX = event.getRawX();
                    float moveY = event.getRawY();
                    float slideX = moveX-downX;
                    float slideY = moveY-downY;
                    if(isSlideing){
                        onTouchSlideListener.onSlide(slideX);
                        downX = moveX;
                    }else{
                        if(Math.abs(slideX) > slideMove && !dontSlide){
                            requestDisallowInterceptTouchEvents(this, true);
                            isSlideing = true;
                            downX = moveX;
                        }else if(Math.abs(slideY) > slideMove){
                            dontSlide = true;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                requestDisallowInterceptTouchEvents(this, false);
                if(isSlideing){
                    if(onTouchSlideListener != null){
                        onTouchSlideListener.onUp();
                    }
                }else{
                    float upX = event.getRawX();
                    float upY = event.getRawY();

                    if (Math.abs(upX - downX) < slideClick && Math.abs(upY - downY) < slideClick) {
                        //单击事件
                        if(onTouchSlideListener != null){
                            onTouchSlideListener.onClick();
                        }
                    }
                }
                isSlideing = false;
                dontSlide = false;
                break;
        }
        return true;
    }

    //递归拦截所有父view的事件
    private void requestDisallowInterceptTouchEvents(ViewGroup viewGroup, boolean isIntercept){

        ViewParent parent = viewGroup.getParent();
        if(parent instanceof ViewGroup){
            ViewGroup parenViewGroup = (ViewGroup) parent;
            requestDisallowInterceptTouchEvents(parenViewGroup, isIntercept);
            parenViewGroup.requestDisallowInterceptTouchEvent(isIntercept);
        }
    }

    public interface OnTouchSlideListener{
        void onSlide(float distant);
        void onUp();
        void onClick();
    }

    public void setOnTouchSlideListener(OnTouchSlideListener onTouchSlideListener){
        this.onTouchSlideListener = onTouchSlideListener;
    }
}