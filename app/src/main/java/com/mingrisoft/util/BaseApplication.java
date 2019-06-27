package com.mingrisoft.util;

import android.app.Activity;
import android.app.Application;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 这个类在开发过程中常用到的类,它贯穿整个App的生命周期
 *主要功能就是添加和销毁Activity
 */
public class BaseApplication extends Application {
    //创建一个Map集合，把activity加到这个Map集合里
    private static Map<String, Activity> destroyMap=new HashMap<>();
    private static BaseApplication instance;                //全局实例

    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
    }

    public BaseApplication() {
        super();
    }

    /**
     * 添加到要销毁的列队
     * <p/>
     * 要销毁的activity
     */


    public static void addDestoryActivity(Activity activity,String activityName){
        destroyMap.put(activityName,activity);
    }

    /**
     * 销毁指定的activtiy
     */

    public static void destroyActivity(String activityName){
        //将Map转化成集合
        Set<String> keyset=destroyMap.keySet();
        for (String key:keyset) {
            destroyMap.get(key).finish();
        }
    }

        //获取实例
    public static BaseApplication getInstance(){
        return instance;
        }
}

