package com.mingrisoft.sockword;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.assetsbasedata.AssetsDatabaseManager;
import com.iflytek.cloud.speech.SpeechConstant;
import com.iflytek.cloud.speech.SpeechError;
import com.iflytek.cloud.speech.SpeechListener;
import com.iflytek.cloud.speech.SpeechSynthesizer;
import com.iflytek.cloud.speech.SpeechUser;
import com.iflytek.cloud.speech.SynthesizerListener;
import com.mingrisoft.greendao.entity.greendao.CET4Entity;
import com.mingrisoft.greendao.entity.greendao.CET4EntityDao;
import com.mingrisoft.greendao.entity.greendao.DaoMaster;
import com.mingrisoft.greendao.entity.greendao.DaoSession;
import com.mingrisoft.util.BaseApplication;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, SynthesizerListener {

    public static final String TAG = "MainActivity";
    private TextView timeText,dateText,wordText,englishText;   //用来显示单词和音标
    private ImageView playVicoe;                                  //播放声音
    private String mMonth,mDay,mWay,mHour,mMinute,mSecond;                //用来显示时间
    private SpeechSynthesizer speechSynthesizer;                  //合成对象
    private KeyguardManager km;
    private KeyguardManager.KeyguardLock k1;
    private RadioGroup radioGroup;
    private RadioButton radioOne,radioTwo,radioThree;           //加载3个单词选项
    LinearLayout lockback;
    private SharedPreferences sharedPreferences;                //定义轻量级数据库
    SharedPreferences.Editor editor=null;                       //编辑数据库
          int j=0;                                              //用于记录答了几道题
    List<Integer> list;                                   //判断题的数目
    List<Integer> wrong = new ArrayList<Integer>();                            //把错题id（即k）存进来
    List<CET4Entity> datas;                               //用于从数据库读取相应的词库
    int k;

    /**
     * 手指按下时位置坐标为（x1,y1）
     * 手指离开屏幕时坐标为(x2,y2)
      * @param savedInstanceState
     */
    float x1=0;
    float y1=0;
    float x2=0;
    float y2=0;

    private SQLiteDatabase db;                               //创建数据库
    private DaoMaster mDaoMaster,dbMaster;                 //管理者
    private DaoSession mDaoSession,dbSession;             //和数据库进行对话
    private CET4EntityDao questionDao,dbDao;              //对应的表,由java代码生成的，对数据库内相应的表操作使用此对象


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //将锁屏界面的内容显示在手机屏幕的最上层(将Activity显示在锁屏界面上)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_main);
        init();
    }

    public  void init(){
              //初始化轻量级数据库
              // 第一个参数用于指定该文件的名称
              //第二个参数指定文件的操作模式
        sharedPreferences=getSharedPreferences("share", Context.MODE_PRIVATE);
        //初始化轻量级数据库编辑器
        editor=sharedPreferences.edit();
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

        /**
         * 得到键盘锁管理对象
         */
        km =(KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        k1 = km.newKeyguardLock("unLock");

        //初始化只需要调用一次
        AssetsDatabaseManager.initManager(this);
        //获取管理对象,因为数据库需要通过管理对象才能获取
        AssetsDatabaseManager mg = AssetsDatabaseManager.getManager();
        //通过管理对象获取数据库
        SQLiteDatabase db1 = mg.getDatabase("word.db");
        //对数据库进行操作
        mDaoMaster=new DaoMaster(db1);
        mDaoSession=mDaoMaster.newSession();
        questionDao=mDaoSession.getCET4EntityDao();

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

        lockback=(LinearLayout)findViewById(R.id.lockback);

        timeText=(TextView) findViewById(R.id.time_text);
        dateText=(TextView) findViewById(R.id.date_text);
        wordText=(TextView) findViewById(R.id.word_text);
        englishText=(TextView) findViewById(R.id.english_text);
        playVicoe=(ImageView) findViewById(R.id.play_voice);
        playVicoe.setOnClickListener(this);

        radioGroup=(RadioGroup) findViewById(R.id.choose_group);
        radioOne=(RadioButton) findViewById(R.id.choose_btn_one);
        radioTwo=(RadioButton) findViewById(R.id.choose_btn_two);
        radioThree=(RadioButton) findViewById(R.id.choose_btn_three);
        radioGroup.setOnCheckedChangeListener(this);

        //设置锁屏背景
        lockback.setBackgroundDrawable(Drawable.createFromPath(sharedPreferences.getString("lockback","/storage/emulated/0/Tencent/QQfile_recv/background.png")));

        //初始化播放语音
        setParam();
        SpeechUser.getUser().login(MainActivity.this,null,null,"appid=5cb7368a",listener);

    }

    @Override
    protected void onStart() {
        super.onStart();

        /**
         * 获取系统时间，并设置将其显示出来
         */

        //拿到Calendar对象即可获取系统当前时间
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));

        //月份是0-11所以获取当前月必须加1
        mMonth=String.valueOf(calendar.get(Calendar.MONTH)+1);
        mDay=String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        mWay=String.valueOf(calendar.get(Calendar.DAY_OF_WEEK));
        mSecond = String.valueOf(calendar.get(Calendar.SECOND));
        /**
         * 如果小时是个位数则在前面加一个0
         */

        if(calendar.get(Calendar.HOUR_OF_DAY)<10){
            mHour="0"+String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        }else{
            mHour=String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        }

        /**
         * 如果分钟是个位数则在前面加一个0
         */
        if(calendar.get(Calendar.MINUTE)<10){
            mMinute="0"+String.valueOf(calendar.get(Calendar.MINUTE));
        }else{
            mMinute=String.valueOf(calendar.get(Calendar.MINUTE));
        }

        /**
         * 获取星期，并设置出来
         */

        if("1".equals(mWay)){
            mWay="天";
        }else if("2".equals(mWay)){
            mWay="一";
        }else if("3".equals(mWay)){
            mWay="二";
        }else if("4".equals(mWay)){
            mWay="三";
        }else if("5".equals(mWay)){
            mWay="四";
        }else if("6".equals(mWay)){
            mWay="五";
        }else if("7".equals(mWay)){
            mWay="六";
        }

        timeText.setText(mHour+":"+mMinute);
        dateText.setText(mMonth+"月"+mDay+"日"+"  "+"星期"+mWay);

        //把mainActivity添加到销毁集合里
        BaseApplication.addDestoryActivity(this,"mainActivity");
        //随机获取锁屏界面单词
        getDBData();
    }


    /**
     * 判断是否是同一天
     * 是同一天返回false
     *
     * @return
     */
    private boolean isToday() {

        int oldminute = sharedPreferences.getInt("minute", -1);
        int oldmonth = sharedPreferences.getInt("month", -1);
        int olddate = sharedPreferences.getInt("date", -1);
        int oldsecond = sharedPreferences.getInt("second",-1);

        editor.putInt("second", Integer.parseInt(mSecond));
        editor.putInt("month", Integer.parseInt(mMonth));
        editor.putInt("date", Integer.parseInt(mDay));
        editor.putInt("minute", Integer.parseInt(mMinute));
        editor.commit();

        //the first
        if (oldminute == -1 || oldmonth == -1 || olddate == -1|| oldsecond == -1) {
            return true;
        }
        if (oldmonth < Integer.parseInt(mMonth)) {
            return true;
        } else if (olddate < Integer.parseInt(mDay)) {
            return true;
        } else if (oldminute < Integer.parseInt(mMinute)) {
            return true;
        } else if (oldsecond < Integer.parseInt(mSecond)){
            return true;
        } else {
            return false;
        }
    }

    /**
     * 将错题存到数据库
     */
    private  void saveWrongData(){
        String word=datas.get(k).getWord();
        String english=datas.get(k).getEnglish();
        String china=datas.get(k).getChina();
        String sign=datas.get(k).getSign();
        CET4Entity data=new CET4Entity(Long.valueOf(dbDao.count()+1),word,english,china,sign);
        dbDao.insert(data);
    }

    private void btnGetText(String msg,RadioButton btn){
        /**
         * 答对设置绿色，答错设置红色
         */
        if(msg.equals(datas.get(k).getChina())){
            wordText.setTextColor(Color.GREEN);
            englishText.setTextColor(Color.GREEN);
            btn.setTextColor(Color.GREEN);
            Log.d(TAG, "回答正确: "+ k );
        }else{
            wordText.setTextColor(Color.RED);
            englishText.setTextColor(Color.RED);
            btn.setTextColor(Color.RED);
            Log.d(TAG, "错题id k为: "+ k );

            if(!wrong.contains(k)){
                Log.d(TAG, "不重复错题id k为: "+ k );
                wrong.add(k);
                saveWrongData();
                //保存到数据库
                int wrong=sharedPreferences.getInt("wrong",0);
                //修改错题数
                editor.putInt("wrong",wrong+1);
                //将错题Id存入sharedPreferences中
                editor.putString("wrongId",","+datas.get(j).getId());
                //提交修改
                editor.commit();
            }


        }
    }

    /**
     * 设置选项(A B C）
     */

    private void setChina(List<CET4Entity>datas,int j){
        /**
         * 随机产生几个随机数，是用于解锁单词
         * 因为数据库中只录入20个单词，所以产生的随机数都是20以内的
         */
        Random r=new Random();
        List<Integer>list=new ArrayList<Integer>();
        int i;
        while (list.size()<4){
            i=r.nextInt(20);
            if(!list.contains(i)){
                list.add(i);
            }
        }


        /**
         *设置单词中文意思选项，设置为正确，正确的前一个，正确的后一个
         */

        if(list.get(0)<7){
            radioOne.setText("A: "+datas.get(k).getChina());
            if(k-1>=0){
                radioTwo.setText("B: "+datas.get(k-1).getChina());
            }else{
                radioTwo.setText("B: "+datas.get(k+2).getChina());
            }

            if(k+1<20){
                radioThree.setText("C: "+datas.get(k+1).getChina());
            }else{
                radioThree.setText("C: "+datas.get(k-2).getChina());
            }
        }else if(list.get(0)<14){
            radioTwo.setText("B: "+datas.get(k).getChina());
            if(k-1>=0){
                radioOne.setText("A: "+datas.get(k-1).getChina());
            }else{
                radioOne.setText("A: "+datas.get(k+2).getChina());
            }

            if(k+1<20){
                radioThree.setText("C: "+datas.get(k+1).getChina());
            }else{
                radioThree.setText("C: "+datas.get(k-2).getChina());
            }
        }else{
            radioThree.setText("C: "+datas.get(k).getChina());
            if(k-1>=0){
                radioTwo.setText("B: "+datas.get(k-1).getChina());
            }else{
                radioTwo.setText("B: "+datas.get(k+2).getChina());
            }

            if(k+1<20){
                radioOne.setText("A: "+datas.get(k+1).getChina());
            }else{
                radioOne.setText("A: "+datas.get(k-2).getChina());
            }
        }
    }

    /**
     * 获取数据库数据
     *
     */
    private void getDBData(){
        //获取数据表中所有数据
        datas=questionDao.queryBuilder().list();
        //获取数据项ID,list中保存了10个20以内的数据项id，且不重复
        k=list.get(j);
        Log.d(TAG, "初始获取k为: "+k);
        wordText.setText(datas.get(k).getWord());
        englishText.setText(datas.get(k).getEnglish());
        //设置单词的三个词义选项
        setChina(datas,k);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //当手指按下时坐标（x,y）
                x1=event.getX();
                y1=event.getY();
                break;
            case MotionEvent.ACTION_UP:
                //当手指离开时坐标(x,y)
                x2=event.getX();
                y2=event.getY();
                //上划直接解锁
                if(y1-y2>200){
                    unlocked();
                }else if(y2-y1>200){            //下滑标记为已掌握
                    //已掌握单词数量加1
                    int num=sharedPreferences.getInt("alreadyMastered",0)+1;
                    editor.putInt("alreadyMastered",num);
                    editor.commit();
                    Toast.makeText(MainActivity.this,"已掌握",Toast.LENGTH_SHORT).show();
                    getMextData();
                }else if(x2-x1>200){          //右划下一题
                    //获取下一题
                    getMextData();
                }
                break;
        }

        return  super.onTouchEvent(event);
    }

    /**
     * 获取下一题
     */

    private void getMextData(){
        //当前已做题的数目
        j++;
        //解锁题目  默认为2道
        int i=sharedPreferences.getInt("allNum",2);
        if(i>j){
            //获取数据
            getDBData();
            //设置颜色(初始化全白)
            setTextColor();
        }else {
            //答题数超过2道即解锁屏幕
            unlocked();
        }

    }

    /**
     * 播放单词单击事件方法
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.play_voice:
                String text=wordText.getText().toString();
                speechSynthesizer.startSpeaking(text,this);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
       switch (i){
           case R.id.choose_btn_one:
               String msg=radioOne.getText().toString().substring(3);
               btnGetText(msg,radioOne);
               break;
           case R.id.choose_btn_two:
               String msg1=radioTwo.getText().toString().substring(3);
               btnGetText(msg1,radioTwo);
               break;
           case R.id.choose_btn_three:
               String msg2=radioThree.getText().toString().substring(3);
               btnGetText(msg2,radioThree);
               break;
       }
    }

    /**
     * 还原单词与选项选项
     */

    private  void setTextColor(){
        //3个单选按钮默认不被选中
        radioOne.setChecked(false);
        radioTwo.setChecked(false);
        radioThree.setChecked(false);

        //将选项按钮和单词音标设置为白色
        radioOne.setTextColor(Color.parseColor("#FFFFFF"));
        radioTwo.setTextColor(Color.parseColor("#FFFFFF"));
        radioThree.setTextColor(Color.parseColor("#FFFFFF"));
        wordText.setTextColor(Color.parseColor("#FFFFFF"));
        englishText.setTextColor(Color.parseColor("#FFFFFF"));
    }

    /**
     * 解锁
     */
    private void unlocked(){
        //隐式启动
        Intent intent=new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        //解锁
        k1.disableKeyguard();

        if(isToday()){
            int locknum=sharedPreferences.getInt("alreadyStudy",0)+1;
            editor.putInt("alreadyStudy",locknum);
            editor.commit();
        }else{
            int locknum=1;
            editor.putInt("alreadyStudy",locknum);
            editor.commit();
        }

        finish();
    }


    @Override
    public void onSpeakBegin() {

    }

    @Override
    public void onBufferProgress(int i, int i1, int i2, String s) {

    }

    @Override
    public void onSpeakPaused() {

    }

    @Override
    public void onSpeakResumed() {

    }

    @Override
    public void onSpeakProgress(int i, int i1, int i2) {

    }

    @Override
    public void onCompleted(SpeechError speechError) {

    }

    /**
     * 通用接口回调
     */
    private SpeechListener listener=new SpeechListener() {
        //消息回调
        @Override
        public void onEvent(int i, Bundle bundle) {

        }

        //数据回调
        @Override
        public void onData(byte[] bytes) {

        }

        //结束回调
        @Override
        public void onCompleted(SpeechError speechError) {

        }
    };

    /**
     * 初始化语音播报
     */
    public void setParam(){
        speechSynthesizer=SpeechSynthesizer.createSynthesizer(this);
        //设置发音人名(
        speechSynthesizer.setParameter(SpeechConstant.VOICE_NAME,"Catherine");
        //语速
        speechSynthesizer.setParameter(SpeechConstant.SPEED,"50");
        //声音大小
        speechSynthesizer.setParameter(SpeechConstant.VOLUME,"80");
        //音调
        speechSynthesizer.setParameter(SpeechConstant.PITCH,"50");
    }
}
