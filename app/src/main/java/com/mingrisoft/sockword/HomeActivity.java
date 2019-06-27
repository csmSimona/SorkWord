package com.mingrisoft.sockword;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.mingrisoft.fragment.SetFragment;
import com.mingrisoft.fragment.StudyFragment;
import com.mingrisoft.util.BaseApplication;
import com.mingrisoft.util.ScreenListener;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private ScreenListener screenListener;        //绑定此页面与手机屏幕状态的监听
    private SharedPreferences sharedPreferences; //定义轻量级数据库
    private FragmentTransaction transaction;    //定义用于加载复习与设置的界面
    private StudyFragment studyFragment;       //绑定复习界面
    private SetFragment setFragment;           //绑定设置界面
    private Button wrongBtn;                   //定义错词本按钮
    private Button translateBtn;               //定义翻译按钮


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);
        init();
    }

    private void init(){
        sharedPreferences=getSharedPreferences("share",Context.MODE_PRIVATE);

        wrongBtn=(Button) findViewById(R.id.wrong_btn);
        wrongBtn.setOnClickListener(this);
        translateBtn=(Button) findViewById(R.id.translation);
        translateBtn.setOnClickListener(this);

        final  SharedPreferences.Editor edit=sharedPreferences.edit();

        //屏幕状态进行监听
        screenListener=new ScreenListener(this);

        screenListener.begin(new ScreenListener.ScreenStateListener() {
            @Override
            public void onScreenOn() {
                //判断是否在设置界面开启了锁屏按钮,如果开启则启动单词锁屏界面
                if (sharedPreferences.getBoolean("btnTf", false)) {
                    //判断屏幕是否解锁
                    Log.d("HomeActivity","解锁了");
                    if (sharedPreferences.getBoolean("tf", false)) {
                        Log.d("HomeActivity","屏幕解锁了");
                        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onScreenOff() {             //手机已锁屏操作
                /**
                 *如果手机已经锁了
                 * 就把数据库中的tf字段改成true
                 */
                Log.d("HomeActivity","手机锁屏了");
                edit.putBoolean("tf",true);
                edit.commit();
                //销毁锁屏界面
                BaseApplication.destroyActivity("mainActivity");
            }

            @Override
            public void onUserPresent() {
                /**
                 * 如果手机已经解锁了
                 * 就把数据库中的tf字段改成false
                 */
                Log.d("HomeActivity","手机解锁了");
                edit.putBoolean("tf",false);
                edit.commit();

            }
        });

        //当此界面加载时，就显示复习界面的fragment
         studyFragment=new StudyFragment();
         setFragment(studyFragment);

    }

    public void setFragment(Fragment fragment) {
        //开启碎片交易
        transaction=getFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout,fragment);
        transaction.commit();
    }

    //单击进入复习界面
    public void study(View view){
        if(studyFragment==null){
            studyFragment=new StudyFragment();
        }
        setFragment(studyFragment);
    }

    //单击进入设置界面
    public void set(View view){
        if(setFragment==null){
            setFragment=new SetFragment();
        }
        setFragment(setFragment);
    }

    @Override
    public void onClick(View view) {
     switch (view.getId()){
         case R.id.wrong_btn:
             Intent intent=new Intent(HomeActivity.this,WrongAcitivty.class);
             startActivity(intent);
             break;
         case R.id.translation:
             Intent intent1=new Intent(HomeActivity.this,TranslateActivity.class);
             startActivity(intent1);
             break;
     }
    }

    @Override
    protected void onDestroy() {
        screenListener.unregisterListener();
        super.onDestroy();
    }
}
