package com.mingrisoft.sockword;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.assetsbasedata.AssetsDatabaseManager;
import com.mingrisoft.adapter.CardFragmentPagerAdapter;
import com.mingrisoft.adapter.ReviewFragmentPagerAdapter;
import com.mingrisoft.greendao.entity.greendao.CET4Entity;
import com.mingrisoft.greendao.entity.greendao.CET4EntityDao;
import com.mingrisoft.greendao.entity.greendao.DaoMaster;
import com.mingrisoft.greendao.entity.greendao.DaoSession;
import com.mingrisoft.transform.CardTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReviewAcitivty extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    public static final String TAG = "ReviewAcitivty";
    private SharedPreferences sharedPreferences;            //定义轻量级数据库
    private SharedPreferences.Editor editor;                //数据库编辑器
    private ViewPager viewpager;
    private ReviewFragmentPagerAdapter mAapter;

    private ImageView back;
    private Button review_yes,review_no;    //记住了，没记住按钮
    private TextView currentItem,allItem,dispatch;  //当前页码和页码总数

    private SQLiteDatabase db,db2;      //定义数据库
    private DaoMaster mDaoMaster,dbMaster,rMaster;    // 数据库管理者
    private DaoSession mDaoSession,dbSession,rSession;    // 与数据库进行会话
    private CET4EntityDao questionDao,dbDao,rDao;   // 对应的表,由java代码生成的,对数据库内相应的表操作使用此对象

    List<Integer> list;                                   //判断题的数目
    List<CET4Entity> datas;                               //用于从数据库读取相应的词库
    List<CET4Entity> reviewData;         //定义一个list泛型为CET4Entity
    int reviewNum = 0;                   //当前是第几个
    int rnum = 0;                           //错题数目
    List<Integer> wrong = new ArrayList<Integer>();                            //把错题id（即k）存进来


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_layout);      //绑定布局文件
        viewpager = (ViewPager) findViewById(R.id.viewpager);
        allItem = (TextView)findViewById(R.id.allItem);
        currentItem = (TextView)findViewById(R.id.currentItem);
        dispatch = (TextView)findViewById(R.id.dispatch);

        back = (ImageView)findViewById(R.id.review_back_btn);
        back.setOnClickListener(this);
        review_yes = (Button) findViewById(R.id.review_yes);
        review_no = (Button) findViewById(R.id.review_no);
        review_yes.setOnClickListener(this);                               //记住按钮设置监听事件
        review_no.setOnClickListener(this);

        viewpager.setOnPageChangeListener(this);

        sharedPreferences = getSharedPreferences("share", Context.MODE_PRIVATE);        //初始化数据库
        editor = sharedPreferences.edit();                          //初始化编辑器

        //初始化list  判断题的数目
        list=new ArrayList<Integer>();

        /**
         * 添加十个20以内的随机数
         */
        Random r=new Random();
        int i;
        while (list.size()<10){
            i=r.nextInt(20);
            if(!list.contains(i)){
                list.add(i);
            }
        }

        AssetsDatabaseManager.initManager(this);
        AssetsDatabaseManager mg = AssetsDatabaseManager.getManager();
        SQLiteDatabase db1 = mg.getDatabase("word.db");
        mDaoMaster=new DaoMaster(db1);
        mDaoSession=mDaoMaster.newSession();
        questionDao=mDaoSession.getCET4EntityDao();
        datas=questionDao.queryBuilder().list();
        //第一个参数为Context  第二个参数为数据库名字  第三参数为CursorFactory
        DaoMaster.DevOpenHelper helper=new DaoMaster.DevOpenHelper(this,"wrong.db",null);
        /**
         * 初始化数据库
         */
        //第一步获得一个Dao类：
        db=helper.getWritableDatabase();
        dbMaster=new DaoMaster(db);
        dbSession=dbMaster.newSession();
        dbDao=dbSession.getCET4EntityDao();

        DaoMaster.DevOpenHelper helper1=new DaoMaster.DevOpenHelper(this,"review.db",null);
        /**
         * 初始化数据库
         */
        //第一步获得一个Dao类：
        db2=helper1.getWritableDatabase();
        rMaster=new DaoMaster(db2);
        rSession=rMaster.newSession();
        rDao=rSession.getCET4EntityDao();

        if(rDao.queryBuilder().list().size()==0){
            copyData();
        }

        if(wrong.size()!=dbDao.queryBuilder().list().size()){
            copyWrongId();
        }

        setData(reviewNum);      //设置复习题
        Log.d(TAG, "初始复习题数："+String.valueOf(rnum));


    }

    /**
     * 将复习题复制到数据库
     */
    private void copyData(){
        int a = Integer.parseInt(sharedPreferences.getString("reviewNum","20"));
        for(int i=0;i<a;i++){
            String word=datas.get(i).getWord();
            String english=datas.get(i).getEnglish();
            String china=datas.get(i).getChina();
            String sign=datas.get(i).getSign();
            CET4Entity data=new CET4Entity(Long.valueOf(i+1),word,english,china,sign);
            rDao.insert(data);
        }
    }

    private void copyWrongId(){
        for(int i=0;i<dbDao.queryBuilder().list().size();i++){
            wrong.add(dbDao.queryBuilder().list().get(i).getId().intValue());
        }
    }

    /**
     * 将没记住的存到数据库
     */
    private  void saveWrongData(int k){
        String word=datas.get(k-1).getWord();
        String english=datas.get(k-1).getEnglish();
        String china=datas.get(k-1).getChina();
        String sign=datas.get(k-1).getSign();
        //创建一个数据项
        CET4Entity data=new CET4Entity(Long.valueOf(dbDao.count()+1),word,english,china,sign);
        //保存数据项
        dbDao.insert(data);
        Toast.makeText(ReviewAcitivty.this,"已加入生词本",Toast.LENGTH_SHORT).show();
    }

    /**
     * 设置错题
     */
    private void setData(int j) {

        reviewData = new ArrayList<>();              //初始化list
        if (rDao.queryBuilder().list() != null
                && rDao.queryBuilder().list().size() > 0
                && j <= rDao.queryBuilder().list().size()
                && j >= 0
                ) {            //判断如果数据库不为空
            for (int i = 0; i < rDao.queryBuilder().list().size(); i++) {
                reviewData.add(i, rDao.queryBuilder().list().get(i));         //把数据循环加到list里面
            }
        } else {
            reviewData.add(null);
            review_yes.setVisibility(View.GONE);
            review_no.setVisibility(View.GONE);
            currentItem.setVisibility(View.GONE);
            allItem.setVisibility(View.GONE);
            dispatch.setVisibility(View.GONE);
        }
        rnum = rDao.queryBuilder().list().size();   //错题数量
        Log.d(TAG, "复习题数量为："+rnum);

        allItem.setText(rnum+"");

        mAapter = new ReviewFragmentPagerAdapter(getSupportFragmentManager(),reviewData);
        viewpager.setAdapter(mAapter);
        viewpager.setOffscreenPageLimit(3);
        viewpager.setCurrentItem(reviewData.size()-1);
        viewpager.setPageTransformer(true,new CardTransformer());

    }

    /**
     * 点击事件
     */
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.review_yes:           //记住了  按钮的点击操作
                rDao.deleteByKey(rDao.queryBuilder().list().get(reviewNum).getId());       //从数据库删除该条数据
                Log.d(TAG, "删除的是第几个："+String.valueOf(reviewNum));
                setData(reviewNum);        //刷新数据
                break;
            case R.id.review_no:           //没记住  按钮的点击操作
                //加入生词本
                if(!wrong.contains(rDao.queryBuilder().list().get(reviewNum).getId().intValue())){
                    wrong.add(rDao.queryBuilder().list().get(reviewNum).getId().intValue());
                    saveWrongData(rDao.queryBuilder().list().get(reviewNum).getId().intValue());
                }
                rDao.deleteByKey(rDao.queryBuilder().list().get(reviewNum).getId());       //从数据库删除该条数据
                Log.d(TAG, "删除的是第几个："+String.valueOf(reviewNum));
                setData(reviewNum);        //刷新数据
                break;
            case R.id.review_back_btn:                 //返回按钮
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
        reviewNum = position;
        currentItem.setText(reviewNum+1+"");
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
