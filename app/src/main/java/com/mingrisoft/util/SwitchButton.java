package com.mingrisoft.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.mingrisoft.sockword.R;

public class SwitchButton extends FrameLayout {
    private ImageView openImage;     //打开按钮图片
    private ImageView closeImage;    //关闭按钮图片
    public SwitchButton(Context context){
        this(context,null);
    }

    /**
     * 构造方法
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public SwitchButton(Context context, AttributeSet attrs,int defStyleAttr){
        this(context,attrs);
    }

    public SwitchButton(Context context,AttributeSet attrs){
        super(context,attrs);
        /**
         * context 通过调用obtainStyledAttributes方法获取一个TypeArray,然后由TypeArray
         * 对属性进行设置
         */
        TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.SwitchButton);
        //画出开关打开状态
        Drawable openDrawable=typedArray.getDrawable(R.styleable.SwitchButton_switchOpenImag);
        //画出开关为关闭状态
        Drawable closeDrawable=typedArray.getDrawable(R.styleable.SwitchButton_switchCloseImage);
        //获取开关状态，默认为打开0
        int switchStatus=typedArray.getInt(R.styleable.SwitchButton_switchStatus,0);
        //必须释放资源
        typedArray.recycle();
        //绑定布局文件
        LayoutInflater.from(context).inflate(R.layout.switch_button,this);

        openImage=(ImageView)findViewById(R.id.switch_open);
        closeImage=(ImageView)findViewById(R.id.switch_close);

        if(openDrawable!=null){
            openImage.setImageDrawable(openDrawable);
        }

        if(closeDrawable!=null){
            closeImage.setImageDrawable(closeDrawable);
        }

        if(switchStatus==1){
            closeSwitch();
        }

    }


    //判断开关状态
    public boolean isSwitchOpen(){
        return openImage.getVisibility()== View.VISIBLE;
    }

    public void openSwitch(){
        openImage.setVisibility(View.VISIBLE);
        closeImage.setVisibility(View.INVISIBLE);
    }

    public void closeSwitch(){
        openImage.setVisibility(View.INVISIBLE);
        closeImage.setVisibility(View.VISIBLE);
    }


}
