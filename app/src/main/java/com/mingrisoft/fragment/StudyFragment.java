package com.mingrisoft.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.assetsbasedata.AssetsDatabaseManager;
import com.iflytek.cloud.speech.SpeechConstant;
import com.iflytek.cloud.speech.SpeechError;
import com.iflytek.cloud.speech.SpeechListener;
import com.iflytek.cloud.speech.SpeechSynthesizer;
import com.iflytek.cloud.speech.SpeechUser;
import com.iflytek.cloud.speech.SynthesizerListener;
import com.mingrisoft.greendao.entity.greendao.DaoMaster;
import com.mingrisoft.greendao.entity.greendao.DaoSession;
import com.mingrisoft.greendao.entity.greendao.WisdomEntity;
import com.mingrisoft.greendao.entity.greendao.WisdomEntityDao;
import com.mingrisoft.sockword.HomeActivity;
import com.mingrisoft.sockword.R;
import com.mingrisoft.sockword.ReviewAcitivty;
import com.mingrisoft.sockword.WrongAcitivty;

import java.util.List;
import java.util.Random;

public class StudyFragment extends Fragment  implements View.OnClickListener,SynthesizerListener {
    //名言(英语), 名言(汉语), 学习难度，总共学习, 掌握单词，答错题数
    private TextView wisdomEnglish, wisdomChina,
            difficultyTv, alreadyStudyText, alreadyMasteredText, wrongText;
    private ImageView playVioce;                 //播放声音
    private Button review_btn;              //开始复习按钮
    private SpeechSynthesizer speechSynthesizer;    //语音 合成对象
    private SharedPreferences sharedPreferences;     //定义轻量级数据库
    private DaoMaster mDaoMaster;                    //数据库管理者
    private DaoSession mDaoSession;                   //与数据库进行会话
    private WisdomEntityDao questionDao;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        //绑定布局文件
        View view = inflater.inflate(R.layout.study_fragment, null);
        //初始化数据库
        sharedPreferences = getActivity().getSharedPreferences("share", Context.MODE_PRIVATE);

        wisdomEnglish = (TextView) view.findViewById(R.id.wisdom_english);
        wisdomChina = (TextView) view.findViewById(R.id.wisdom_china);
        difficultyTv = (TextView) view.findViewById(R.id.difficulty_text);
        alreadyStudyText = (TextView) view.findViewById(R.id.already_study);
        alreadyMasteredText = (TextView) view.findViewById(R.id.already_mastered);
        wrongText = (TextView) view.findViewById(R.id.wrong_text);
        playVioce = (ImageView)view.findViewById(R.id.play_vioce);          //播放声音按钮绑定id
        playVioce.setOnClickListener(this);                             // 播放声音设置监听事件
        review_btn = (Button)view.findViewById(R.id.review_btn);
        review_btn.setOnClickListener(this);

        //初始化，只需要调用一次
        AssetsDatabaseManager.initManager(getActivity());
        //获取管理对象，因为数据库需要通过管理对象才能获取
        AssetsDatabaseManager mg = AssetsDatabaseManager.getManager();
        SQLiteDatabase db1 = mg.getDatabase("wisdom.db");
        mDaoMaster = new DaoMaster(db1);
        mDaoSession = mDaoMaster.newSession();
        //获取数据
        questionDao = mDaoSession.getWisdomEntityDao();

        setParam();             //初始化语音播报
        SpeechUser.getUser().login(this.getActivity(),null,null,"appid=5cb7368a",listener);

        return view;


    }

    @Override
    public void onStart() {
        super.onStart();
        difficultyTv.setText(sharedPreferences.getString("difficulty","四级")+"英语");
        List<WisdomEntity> datas=questionDao.queryBuilder().list();
        Random random=new Random();
        int i=random.nextInt(10);
        wisdomEnglish.setText(datas.get(i).getEnglish());
        wisdomChina.setText(datas.get(i).getChina());
        setText();
    }

    /**
     * 设置十字内的各个单词书(从轻量级数据库中获取数据)
     */
    private void setText(){
        alreadyMasteredText.setText(sharedPreferences.getInt("alreadyMastered",0)+"");
        alreadyStudyText.setText(sharedPreferences.getInt("alreadyStudy",0)+"");
        wrongText.setText(sharedPreferences.getInt("wrong",0)+"");
    }

    /**
     * 点击事件
     */
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.play_vioce:                   //播放单词声音
                String text = wisdomEnglish.getText().toString();    //获取文本
                speechSynthesizer.startSpeaking(text, this);        //传给后台
                break;
            case R.id.review_btn:
                Intent intent=new Intent(getActivity(),ReviewAcitivty.class);
                startActivity(intent);
                break;
        }
    }

    //缓冲进度回调通知
    @Override
    public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {
        // TODO Auto-generated method stub
    }

    //结束回调
    @Override
    public void onCompleted(SpeechError arg0) {
        // TODO Auto-generated method stub

    }

    //开始播放
    @Override
    public void onSpeakBegin() {
        // TODO Auto-generated method stub

    }

    //暂停播放
    @Override
    public void onSpeakPaused() {
        // TODO Auto-generated method stub

    }

    //播放进度
    @Override
    public void onSpeakProgress(int arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    //继续播放
    @Override
    public void onSpeakResumed() {
        // TODO Auto-generated method stub

    }

    /**
     * 通用回调接口
     */
    private SpeechListener listener = new SpeechListener() {

        //消息回调
        @Override
        public void onEvent(int arg0, Bundle arg1) {
            // TODO Auto-generated method stub

        }

        //数据回调
        @Override
        public void onData(byte[] arg0) {
            // TODO Auto-generated method stub

        }

        //结束回调（没有错误）
        @Override
        public void onCompleted(SpeechError arg0) {
            // TODO Auto-generated method stub

        }
    };

    /**
     * 初始化语音播报
     * */
    public void setParam() {
        speechSynthesizer = SpeechSynthesizer.createSynthesizer(this.getActivity());
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
