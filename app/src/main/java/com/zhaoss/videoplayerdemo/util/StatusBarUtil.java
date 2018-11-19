package com.zhaoss.videoplayerdemo.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.zhaoss.videoplayerdemo.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class StatusBarUtil {

    //是否沉浸式
    private static boolean immersiveMode;
    //是否状态栏白底黑字
    private static boolean blackext;

    /**
     * 设置状态栏颜色
     * @param color 状态栏颜色值
     * @param isFontColorDark 深色字体模式
     */
    public static void setColor(Activity activity, int color, boolean isFontColorDark) {

        if(isFullScreen(activity)) {
            return ;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            immersiveMode = true;

            setStatusBarColor(activity, color, isFontColorDark);
        }
    }

    /**
     * 覆盖状态栏模式
     * @param isFontColorDark 深色字体模式
     */
    public static void setCoverStatus(Activity activity, boolean isFontColorDark) {

        if(isFullScreen(activity)) {
            return ;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            immersiveMode = true;
            setStatusBarColor(activity, 0, isFontColorDark);
        }
    }

    public static boolean isImmersiveMode(){
        return immersiveMode;
    }

    public static boolean isBlackext(){
        return blackext;
    }

    /**
     * 生成一个和状态栏大小相同的矩形条
     * @return 状态栏矩形条
     */
    private static View createStatusBarView(Activity activity, int color) {
        // 绘制一个和状态栏一样高的矩形
        View statusBarView = new View(activity);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusHeight(activity));
        statusBarView.setLayoutParams(params);
        statusBarView.setBackgroundColor(color);
        return statusBarView;
    }

    /**
     * 设置根布局参数
     */
    private static void setRootView(Activity activity) {
        ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        rootView.setFitsSystemWindows(true);
        rootView.setClipToPadding(true);
    }

    /**
     * 黑色字体
     */
    public static void setStatusBarColor(Activity activity, int BgColor, boolean isFontColorDark) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (setMIUIStatusBarLightMode(activity, isFontColorDark)) {//MIUI
                //MIUI9以上api废除, 要调用系统的
                setAndroidStatusTextColor(activity, isFontColorDark);
                blackext = true;
                //miui设置成功
            } else if (setFLYMEStatusBarLightMode(activity, isFontColorDark)) {//Flyme
                blackext = true;
                //魅族设置成功
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0
                //系统6.0设置成功
                blackext = true;
                setAndroidStatusTextColor(activity, isFontColorDark);
            }else{
                //黑色字体设置失败, 背景颜色默认
                BgColor = activity.getResources().getColor(R.color.my_black);
            }

            activity.getWindow().setStatusBarColor(BgColor);
        }
    }

    //android 6.0以上设置状态栏黑色
    private static void setAndroidStatusTextColor(Activity activity, boolean isFontColorDark){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    isFontColorDark ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
    }

    /**
     * 设置状态栏字体图标为深色，需要MIUI6以上
     * @param isFontColorDark 是否把状态栏字体及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    private static boolean setMIUIStatusBarLightMode(Activity activity, boolean isFontColorDark) {
        Window window = activity.getWindow();
        boolean result = false;
        if (window != null) {
            Class clazz = window.getClass();
            try {
                int darkModeFlag = 0;
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                if (isFontColorDark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag);//状态栏透明且黑色字体
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag);//清除黑色字体
                }
                result = true;
            } catch (Exception e) {
                //not MIUI
            }
        }
        return result;
    }

    /**
     * 设置状态栏字体图标为深色，魅族4.4
     * @param isFontColorDark 是否把状态栏字体及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    private static boolean setFLYMEStatusBarLightMode(Activity activity, boolean isFontColorDark) {
        Window window = activity.getWindow();
        boolean result = false;
        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (isFontColorDark) {
                value |= bit;
            } else {
                value &= ~bit;
            }
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
                result = true;
            } catch (Exception e) {
                //not meizu
            }
        }
        return result;
    }


    /**
     * 状态栏高度
     */
    public static int getStatusHeight(Context context) {

        int statusBarHeight = -1;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    /**
     * @param activity
     * @return 判断当前手机是否是全屏
     */
    public static boolean isFullScreen(Activity activity) {
        int flag = activity.getWindow().getAttributes().flags;
        return (flag & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN;
    }

    /** 增加View的paddingTop,增加的值为状态栏高度 */
    public static void setPadding(Context context, View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setPadding(view.getPaddingLeft(), view.getPaddingTop() + getStatusHeight(context),
                    view.getPaddingRight(), view.getPaddingBottom());
        }
    }
}
