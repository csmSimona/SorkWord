package com.mingrisoft.fragment;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.speech.SpeechConstant;
import com.iflytek.cloud.speech.SpeechError;
import com.iflytek.cloud.speech.SpeechListener;
import com.iflytek.cloud.speech.SpeechSynthesizer;
import com.iflytek.cloud.speech.SpeechUser;
import com.iflytek.cloud.speech.SynthesizerListener;
import com.mingrisoft.sockword.R;
import com.mingrisoft.util.TranslationService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class WordDetailFrament extends Fragment implements View.OnClickListener, SynthesizerListener {
        //变量定义
        public static  final String  TAG="WordDetailFrament";
        private TextView wordText, englishText,       //单词文本，音标文本，基本释义，网络释义
                basicChineseText, webChineseText;
        private TextView didText, doneText, doingText; //过去式文本，过去分词文本，现在分词文本
        private TextView wordClassText;              //词性类别
        private ImageView playVoice;                  //播放单词发音
        private SpeechSynthesizer speechSynthesizer;        //语音合成
        private Set<String>  wordsRecord;                //查询过的单词记录
        private List<String> workList;                   //处理字符
        private static Map<String,String> resultMap;      //保存查询的结果
        private ImageView tranWord;
        private SharedPreferences sharedPreferences;      //数据库操作
        private SharedPreferences.Editor editor;          //编辑者

        //转主线程处理获取的数据
        private  Handler mHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case TranslationService.SUCCEE_RESULT:
                        //更新单词历史记录
                        update(resultMap);
                        //设置query
                        wordText.setText(resultMap.get("query"));
                        //设置音标
                        englishText.setText("["+resultMap.get("phonetic")+"]");
                        //设置基本释义
                        basicChineseText.setText(resultMap.get("explains"));
                        //设置网络释义
                        webChineseText.setText(resultMap.get("web"));
                        //设置过去式过去分词和现在分词
                        if(resultMap.containsKey("value0")){
                            didText.setText(resultMap.get("value0"));
                            doneText.setText(resultMap.get("value1"));
                            doingText.setText(resultMap.get("value2"));
                        }else{
                            didText.setText("暂不支持");
                            doneText.setText("暂不支持");
                            doingText.setText("暂不支持");
                        }
                        //设置词性类别
                        if(resultMap.containsKey("exam_type")){
                            wordClassText.setText(resultMap.get("exam_type"));
                        }else{
                            wordClassText.setText("暂不支持");
                        }
                        break;
                    case TranslationService.ERROR_INVALID_APPKEY:
                        Toast.makeText(getActivity(),"appKey无效",Toast.LENGTH_SHORT).show();
                        break;
                    case TranslationService.ERROR_PARAMAT_DISCARD:
                        Toast.makeText(getActivity(),"参数不齐全或书写不正确",Toast.LENGTH_SHORT).show();
                        break;
                    case TranslationService.ERROR_PROBLEM_CODE:
                        Toast.makeText(getActivity(),"编码问题。请确保 q 为UTF-8编码.",Toast.LENGTH_SHORT).show();
                        break;
                    case TranslationService.ERROR_UNBINDING_INSTANCE:
                        Toast.makeText(getActivity(),"没有绑定服务实例",Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getActivity(),"出现未知错误",Toast.LENGTH_SHORT).show();
                }
                super.handleMessage(msg);
            }
        };

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            //绑定布局文件
            View view=inflater.inflate(R.layout.word_detail_fragment,null);
            //初始化控件
            init(view);
            sharedPreferences=getActivity().getSharedPreferences("share", Context.MODE_PRIVATE);
            editor=sharedPreferences.edit();
            //开启线程执行
            return view;
        }

        /**
         * 初始化控件
         */
        public void init(View view){
            resultMap=new HashMap<>();
            wordText = (TextView) view.findViewById(R.id.tran_word);
            englishText = (TextView) view.findViewById(R.id.tran_en);
            basicChineseText = (TextView) view.findViewById(R.id.tran_basic_chinese);
            webChineseText = (TextView) view.findViewById(R.id.tran_web_chinese);
            didText = (TextView) view.findViewById(R.id.tran_did);
            doneText = (TextView) view.findViewById(R.id.tran_done);
            doingText = (TextView) view.findViewById(R.id.tran_doing);
            wordClassText = (TextView) view.findViewById(R.id.tran_word_class);
            playVoice= (ImageView) view.findViewById(R.id.tran_play_voice);
            playVoice.setOnClickListener(this);
        }

        //确保与碎片相关联的活动一定已经创建完毕的时候调用
        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            setParam();
            SpeechUser.getUser().login(this.getActivity(),null,null,"appid=5cb7368a",listener);
        }


        /**
         * 此方法耗时，必循在线程中执行，要不然会报错
         */
        //获取翻译结果并解析存储到resultMap

        public  void translationStrat(String word) throws IOException, JSONException {

            String queryResult = TranslationService.start(word); //翻译结果
            JSONArray jsonArray=new JSONArray(queryResult);
            for(int i=0;i<jsonArray.length();i++){
                //获取一个JSON数据对象
                JSONObject jsonObject=jsonArray.getJSONObject(i);
                if(jsonObject!=null){
                    String errorCode=jsonObject.getString("errorCode");
                    if(errorCode.equals("110")){
                        mHandler.sendEmptyMessage(TranslationService.ERROR_UNBINDING_INSTANCE);
                    }else if(errorCode.equals("108")){
                        mHandler.sendEmptyMessage(TranslationService.ERROR_INVALID_APPKEY);
                    }else if(errorCode.equals("101")){
                        mHandler.sendEmptyMessage(TranslationService.ERROR_PARAMAT_DISCARD);
                    }else if(errorCode.equals("202")){
                        mHandler.sendEmptyMessage(TranslationService.ERROR_PROBLEM_CODE);
                    }else{
                        //清除数据
                        resultMap.clear();
                        //从消息池中获取一个消息对象
                        Message msg=Message.obtain();
                        msg.what=TranslationService.SUCCEE_RESULT;
                        //获取单词
                        String query=jsonObject.getString("query");
                        resultMap.put("query",query);
                        //获取音标，基本释义，
                        if(jsonObject.has("basic")) {
                            JSONObject basicJsonObject = jsonObject.getJSONObject("basic");
                            //获取音标,基本释义，单词所属类别
                            if (basicJsonObject.has("phonetic")) {
                                String phonetic = basicJsonObject.getString("phonetic");
                                resultMap.put("phonetic", phonetic);
                            }
                            if (basicJsonObject.has("explains")) {
                                //获取基本释义
                                String message="";
                                Gson gson = new Gson();
                                String[] basic = gson.fromJson(basicJsonObject.getString("explains"), new TypeToken<String[]>() {
                                }.getType());
                                Log.e(TAG,"explain");
                                for (String item : basic) {
                                    message += (item+"\n");
                                    Log.e(TAG,item);
                                }
                                resultMap.put("explains", message);
                            }

                            if(basicJsonObject.has("exam_type")){
                                //单词所属类别
                                String examMessage="";
                                Gson gson = new Gson();
                                String[] examType = gson.fromJson(basicJsonObject.getString("exam_type"), new TypeToken<String[]>() {
                                }.getType());
                                for (String item : examType) {
                                    examMessage +=(item+"/");
                                }
                                //去掉最后的反斜杠
                                examMessage=examMessage.substring(0,examMessage.length()-1);
                                resultMap.put("exam_type", examMessage);
                            }

                            if(basicJsonObject.has("wfs")){
                                String wfs="";
                                JSONArray jsonArray1=new JSONArray(basicJsonObject.getString("wfs"));
                                for(int j=0;j<jsonArray1.length();j++){
                                    JSONObject wfsJsonObject=jsonArray1.getJSONObject(j);
                                    JSONObject wfJsonObject=wfsJsonObject.getJSONObject("wf");
                                    if(wfJsonObject.has("value")){
                                        Log.e(TAG,wfJsonObject.getString("value"));
                                        resultMap.put("value"+j,wfJsonObject.getString("value"));
                                    }
                                }
                            }
                        }

                        //如果有网络释义
                        if(jsonObject.has("web")){
                            String  webmsg="";
                            String web=jsonObject.getString("web");
                            JSONArray webString=new JSONArray("["+web+"]");
                            JSONArray webArray=webString.getJSONArray(0);
                            if(!webArray.isNull(0)){
                                if (webArray.getJSONObject(0).has("value")) {
                                    Gson webGson=new Gson();
                                    String[] values = webGson.fromJson(webArray.getJSONObject(0).getString("value"),
                                            new TypeToken<String []>(){}.getType());
                                    for (int j = 0; j < values.length; j++) {
                                        String value = values[j];
                                        webmsg += value;
                                        if (j < values.length - 1) {
                                            webmsg += ";";
                                        }
                                    }
                                }
                            }
                            resultMap.put("web",webmsg);
                        }
                        //如果有过去式，过去分词，现在分词
                        if(jsonObject.has("basic")){
                        }
                        msg.obj=resultMap;
                        mHandler.sendMessage(msg);
                    }

                }
            }
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case  R.id.tran_play_voice:
                    String text=wordText.getText().toString();
                    speechSynthesizer.startSpeaking(text,this);
                    break;
            }
        }

        //更新单词列表的记录
        private void update(Map<String,String> map) {
            if(sharedPreferences.getStringSet("wordrecord",null)==null){
                wordsRecord=new LinkedHashSet<>();
            }else{
                wordsRecord=new LinkedHashSet<>(sharedPreferences.getStringSet("wordrecord",null));
            }
            //当历史浏览数据大于20个时，删除最后一个加入新的一个
            if(wordsRecord.size()>=20){
                workList=new LinkedList<>();
                Iterator<String> iterator=wordsRecord.iterator();
                //循环存入字符串
                while (iterator.hasNext()){
                    workList.add(iterator.next());
                }
                //先移除末尾数据
                workList.remove(0);
                //再将新数据添加
                workList.add(workList.size()-1,resultMap.get("query")+"#"+resultMap.get("explains"));
                wordsRecord.clear();
                for(int i=workList.size()-1;i>=0;i--){
                    wordsRecord.add(workList.get(i));
                }
                editor.putStringSet("wordrecord",wordsRecord);
                editor.commit();
            }else {
                wordsRecord.add(resultMap.get("query") + "#" + resultMap.get("explains"));
                editor.putStringSet("wordrecord", wordsRecord);
                editor.commit();
            }
        }


    //接口回调
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

    @Override
    public void onDestroy() {
        super.onDestroy();

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

    //设置参数
    @TargetApi(Build.VERSION_CODES.M)
    private void setParam(){
        speechSynthesizer = SpeechSynthesizer.createSynthesizer(this.getActivity());
        //设置发音人名(
        speechSynthesizer.setParameter(SpeechConstant.VOICE_NAME,"xiaoyan");
        //语速
        speechSynthesizer.setParameter(SpeechConstant.SPEED,"50");
        //声音大小
        speechSynthesizer.setParameter(SpeechConstant.VOLUME,"80");
        //音调
        speechSynthesizer.setParameter(SpeechConstant.PITCH,"50");
    }

}
