package com.mingrisoft.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;

/**
 * 设置屏幕的监听状态
 */
public class ScreenListener {
    private Context context;           //联系上下文
    private ScreenBroadcastReceiver mScreenReceiver;    //定义一个广播
    private ScreenStateListener mScreenStateListener;    //定义内部接口


    /**
     *初始化
     */

    public ScreenListener(Context context){
        this.context=context;
        mScreenReceiver=new ScreenBroadcastReceiver();   //初始化广播
    }

    /**
     * 自定义接口
     */

    public  interface ScreenStateListener{
        void onScreenOn();              //手机屏幕亮起时
        void onScreenOff();             //手机屏幕关闭
        void onUserPresent();           //手机屏幕解锁
    }

    /**
     * 获取screen的状态
     */

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR_MR1)
    private void getScreenState(){
        //初始化powerManager
        PowerManager manager= (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        //如果屏幕亮起，屏幕监听器调用onScreenOn()
        if(manager.isScreenOn()){
            mScreenStateListener.onScreenOn();
        }else {
            mScreenStateListener.onScreenOff();
        }
    }


    /**
     *
     * 一个内部广播，用于监听屏幕亮起时，屏幕关闭时，解锁时的状态
     *
     */

    private class ScreenBroadcastReceiver extends BroadcastReceiver {

        private String action = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {        //屏幕亮时操作
                mScreenStateListener.onScreenOn();
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) { //屏幕关闭时的操作
                mScreenStateListener.onScreenOff();
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {     //屏幕解锁
                mScreenStateListener.onUserPresent();
            }

        }
    }

        /**
         * 开始监听广播状态
         * @param listener
         */

        public void begin(ScreenStateListener listener){
            mScreenStateListener=listener;
            registerListener();
            getScreenState();

        }

        /**
         * 注册广播接收器
         */
        public void registerListener(){
            IntentFilter filter=new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);                    //屏幕亮起时开启的广博
            filter.addAction(Intent.ACTION_SCREEN_OFF);                   //屏幕关闭时开启的广播
            filter.addAction(Intent.ACTION_USER_PRESENT);                 //屏幕解锁时开启的广播
            context.registerReceiver(mScreenReceiver,filter);             //发送广播
        }


    /**
     * 解除注册广播接收器
     */
        public void unregisterListener(){
            context.unregisterReceiver(mScreenReceiver);                  //注销广播
        }




    }

