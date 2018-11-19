package com.zhaoss.videoplayerdemo.util;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zhaoshuang on 2018/7/13.
 * 封装一下RxJava, 更易用
 */

public class RxJavaUtil {

    public static <T> void run(final OnRxAndroidListener<T> onRxAndroidListener){

        Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<T> e){
                try {
                    T t = onRxAndroidListener.doInBackground();
                    if(t != null){
                        e.onNext(t);
                    }else{
                        e.onError(new NullPointerException("on doInBackground result not null"));
                    }
                }catch (Throwable throwable){
                    e.onError(throwable);
                }
            }
        })
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .safeSubscribe(new Observer<T>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }
            @Override
            public void onNext(@NonNull T result) {
                onRxAndroidListener.onFinish(result);
            }
            @Override
            public void onError(@NonNull Throwable e) {
                onRxAndroidListener.onError(e);
            }
            @Override
            public void onComplete() {
            }
        });
    }

    public interface OnRxAndroidListener <T>{
        //在子线程执行
        T doInBackground() throws Throwable;
        //事件执行成功, 在主线程回调
        void onFinish(T result);
        //事件执行失败, 在主线程回调
        void onError(Throwable e);
    }
}
