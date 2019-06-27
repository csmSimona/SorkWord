package com.mingrisoft.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class AndroidStateDetection {

    //判断网络连接是否可用
    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager!=null){
            NetworkInfo [] networkInfos=connectivityManager.getAllNetworkInfo();

            if(networkInfos!=null&&networkInfos.length>0){
                for(int i=0;i<networkInfos.length;i++){
                    if(networkInfos[i].getState()==NetworkInfo.State.CONNECTED){
                        return true;
                    }
                }
            }
        }
        return false;
    }


    //判断WIFI是否打开
    public static  boolean isWifi(Context context){
        ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null&&networkInfo.getType()==ConnectivityManager.TYPE_WIFI){
            return true;
        }
        return false;
    }


    //判断移动数据是否打开
    public static boolean isMobile(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null&&networkInfo.getType()==ConnectivityManager.TYPE_MOBILE){
            return  true;
        }
        return false;

    }
}
