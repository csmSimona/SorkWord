package com.mingrisoft.sockword;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mingrisoft.adapter.CardFragmentPagerAdapter;
import com.mingrisoft.fragment.CardFragment;
import com.mingrisoft.greendao.entity.greendao.CET4Entity;
import com.mingrisoft.greendao.entity.greendao.CET4EntityDao;
import com.mingrisoft.greendao.entity.greendao.DaoMaster;
import com.mingrisoft.greendao.entity.greendao.DaoSession;
import com.mingrisoft.transform.CardTransformer;


import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class WrongAcitivty extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    public static final String TAG = "WrongAcitivty";
    private SharedPreferences sharedPreferences;            //定义轻量级数据库
    private SharedPreferences.Editor editor;                //数据库编辑器
    private ViewPager viewpager;
    private CardFragmentPagerAdapter mAapter;
    private ImageView backBtn;  //“返回”按钮
    private Button iKnowBtn;  //“我会了”按钮
    private TextView currentItem,allItem,dispatch;  //当前页码和页码总数

    private SQLiteDatabase db;      //定义数据库
    private DaoMaster mDaoMaster;    // 数据库管理者
    private DaoSession mDaoSession;    // 与数据库进行会话
    private CET4EntityDao questionDao;   // 对应的表,由java代码生成的,对数据库内相应的表操作使用此对象

    List<CET4Entity> wrongData;         //定义一个list泛型为CET4Entity
    int wrongNum = 0;                   //当前是第几个
    int wnum = 0;                           //错题数目

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wrong_layout);      //绑定布局文件
        viewpager = (ViewPager) findViewById(R.id.viewpager);
        allItem = (TextView)findViewById(R.id.allItem);
        currentItem = (TextView)findViewById(R.id.currentItem);
        dispatch = (TextView)findViewById(R.id.dispatch);
        iKnowBtn = (Button) findViewById(R.id.i_know_btn);              //“我会了”按钮绑定id
        iKnowBtn.setOnClickListener(this);                              //“我会了”按钮设置监听事件
        backBtn = (ImageView) findViewById(R.id.back_btn);                  //返回按钮绑定id
        backBtn.setOnClickListener(this);                               //返回按钮设置监听事件
        viewpager.setOnPageChangeListener(this);

        sharedPreferences = getSharedPreferences("share", Context.MODE_PRIVATE);        //初始化数据库
        editor = sharedPreferences.edit();                          //初始化编辑器
        // 通过管理对象获取数据库
        // 对数据库进行操作
        // 此DevOpenHelper类继承自SQLiteOpenHelper,第一个参数Context,第二个参数数据库名字,第三个参数CursorFactory
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "wrong.db", null);
        db = helper.getWritableDatabase();
        mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
        questionDao = mDaoSession.getCET4EntityDao();

        setData(wrongNum);      //设置错题
        Log.d(TAG, "初始错题数："+String.valueOf(wnum));


    }

    /**
     * 设置错题
     */
        private void setData(int j) {
            iKnowBtn.setVisibility(View.VISIBLE);       //“我会了”按钮显示出来
            wrongData = new ArrayList<>();              //初始化list
            if (questionDao.queryBuilder().list() != null
                && questionDao.queryBuilder().list().size() > 0
                && j <= questionDao.queryBuilder().list().size()
                && j >= 0
                ) {            //判断如果数据库不为空
            for (int i = 0; i < questionDao.queryBuilder().list().size(); i++) {
                wrongData.add(i, questionDao.queryBuilder().list().get(i));         //把数据循环加到list里面
            }
        } else {
            wrongData.add(null);
            iKnowBtn.setVisibility(View.GONE);
            currentItem.setVisibility(View.GONE);
            allItem.setVisibility(View.GONE);
            dispatch.setVisibility(View.GONE);
        }
        wnum = questionDao.queryBuilder().list().size();   //错题数量
        Log.d(TAG, "错题数量为："+wnum);

        allItem.setText(wnum+"");

        mAapter = new CardFragmentPagerAdapter(getSupportFragmentManager(),wrongData);
        viewpager.setAdapter(mAapter);
        viewpager.setOffscreenPageLimit(3);
        viewpager.setCurrentItem(wrongNum);
        viewpager.setPageTransformer(true,new CardTransformer());


        }

    /**
     * 点击事件
     */
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.i_know_btn:           //我会了  按钮的点击操作
                questionDao.deleteByKey(questionDao.queryBuilder().list().get(wrongNum).getId());       //从数据库删除该条数据
                Log.d(TAG, "删除的是第几个："+String.valueOf(wrongNum));
                setData(wrongNum);        //刷新数据
                break;
            case R.id.back_btn:                 //返回按钮
                finish();                       //返回上一个页面
                break;
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        wrongNum = position;
        currentItem.setText(wrongNum+1+"");
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
