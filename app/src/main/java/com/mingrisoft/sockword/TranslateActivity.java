package com.mingrisoft.sockword;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mingrisoft.fragment.HistoryWordFragment;
import com.mingrisoft.fragment.WordDetailFrament;
import com.mingrisoft.util.AndroidStateDetection;

import org.json.JSONException;

import java.io.IOException;

public class TranslateActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "TranslateActivity";
    private ImageView backBtn,clearBtn,translationBtn;                   //返回按钮,清除按钮,翻译按钮
    private EditText editText;                  //单词句子编辑框
    private FragmentTransaction transaction;    //定义用于加载单词详情与历史记录的界面
    private WordDetailFrament wordDetailFragment;            //绑定单词详情界面
    private HistoryWordFragment historyWordFragment;         //绑定历史记录界面

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.translation_layout);
        init();

        historyWordFragment = new HistoryWordFragment();
        wordDetailFragment = new WordDetailFrament();
        setFragment(historyWordFragment);
    }

    /**
     * 初始化控件
     */
    private void init() {
        backBtn = (ImageView) findViewById(R.id.translate_back_btn);
        backBtn.setOnClickListener(this);
        clearBtn = (ImageView) findViewById(R.id.tran_clear);
        clearBtn.setOnClickListener(this);
        editText = (EditText) findViewById(R.id.tran_edit);
        translationBtn = (ImageView) findViewById(R.id.tran_btn);
        translationBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.translate_back_btn:
                finish();
                break;
            case R.id.tran_btn:
                translationWordFromEditText();
                break;
            case R.id.tran_clear:
                if (!editText.getText().toString().equals("")) {
                    editText.setText("");
                }
                historyWord(historyWordFragment);
                break;
        }
    }

    /**
     * @param fragment
     */
    private void setFragment(Fragment fragment) {
        //开启碎片交换
        transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.tran_frame_layout, fragment);
        transaction.commit();
    }

    //进入历史单词界面
    private void historyWord(HistoryWordFragment historyWordFragment) {
        if (historyWordFragment == null) {
            historyWordFragment = new HistoryWordFragment();
        }
        setFragment(historyWordFragment);
    }

    //进入单词详情页面
    private void wordDetail(WordDetailFrament wordDetailFragment) {
        if (wordDetailFragment == null) {
            wordDetailFragment = new WordDetailFrament();
        }
        setFragment(wordDetailFragment);
    }

    //核心方法，在线程中执行翻译方法
    private void translationWordFromEditText() {
        //首先判断是否有wifi或者移动数据
        if(AndroidStateDetection.isMobile(this)||AndroidStateDetection.isNetworkAvailable(this))
        {
            wordDetail(wordDetailFragment);      //进入单词详情页面
            if (wordDetailFragment != null && (AndroidStateDetection.isMobile(this)
                    || AndroidStateDetection.isNetworkAvailable(this))) {
                //开启翻译线程
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            wordDetailFragment.translationStrat(editText.getText().toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }else{
            Toast.makeText(this,"请打开wifi或移动数据",Toast.LENGTH_SHORT).show();
        }
    }

    //核心方法，从另一个碎片传来得数据来执行方法
    public void translationWordFromAnotherFragment(final String word) {
        //首先判断是否有wifi或者移动数据
        if (AndroidStateDetection.isMobile(this) || AndroidStateDetection.isNetworkAvailable(this)) {
            wordDetail(wordDetailFragment);
            editText.setText(word);
            editText.setSelection(word.length());
            if (wordDetailFragment != null) {
                //开启翻译线程
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            wordDetailFragment.translationStrat(word);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }else{
            Toast.makeText(this,"请打开wifi或移动数据",Toast.LENGTH_SHORT).show();
        }
    }

}

