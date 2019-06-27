package com.mingrisoft.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iflytek.cloud.speech.SpeechConstant;
import com.iflytek.cloud.speech.SpeechError;
import com.iflytek.cloud.speech.SpeechListener;
import com.iflytek.cloud.speech.SpeechSynthesizer;
import com.iflytek.cloud.speech.SpeechUser;
import com.iflytek.cloud.speech.SynthesizerListener;
import com.mingrisoft.greendao.entity.greendao.CET4Entity;
import com.mingrisoft.sockword.R;

import java.io.Serializable;

public class ReviewFragment extends Fragment implements View.OnClickListener,SynthesizerListener {

    private View mRootView;
    private CET4Entity review;

    private TextView chinaText, wordText, englishText;      //用来显示单词和音标的
    private ImageView playVioce;                 //播放声音
    private SpeechSynthesizer speechSynthesizer;    //语音 合成对象


    public static ReviewFragment newInstance(CET4Entity info) {
        ReviewFragment fragment = new ReviewFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("review", (Serializable) info);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.review_fragment,container,false);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chinaText = mRootView.findViewById(R.id.china_text);           //汉语绑定id
        englishText = mRootView.findViewById(R.id.english_text);           //音标绑定id
        wordText = mRootView.findViewById(R.id.word_text);             //单词绑定id
        playVioce = mRootView.findViewById(R.id.play_vioce);          //播放声音按钮绑定id
        playVioce.setOnClickListener(this);                             // 播放声音设置监听事件
        review = (CET4Entity) getArguments().getSerializable("review");
        setData();
        setParam();             //初始化语音播报
        SpeechUser.getUser().login(this.getActivity(),null,null,"appid=5cb7368a",listener);

    }


    /**
     * 设置错题
     * */
    private void setData() {
        if (review != null) {            //判断如果数据库不为空
            /**
             * 分别将list里面的数据取出数据设置单词音标以及汉语
             * */
            wordText.setText(review.getWord());
            englishText.setText(review.getEnglish());
            chinaText.setText(review.getChina());

        } else {
            /**
             * 如果数据库为空
             * 隐藏“我会了”按钮
             * */
            wordText.setText("好厉害");
            englishText.setText("[今天的单词]");
            chinaText.setText("都复习完啦");
        }

    }

    /**
     * 点击事件
     */
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.play_vioce:                   //播放单词声音
                String text = wordText.getText().toString();    //获取文本
                speechSynthesizer.startSpeaking(text, this);        //传给后台
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
