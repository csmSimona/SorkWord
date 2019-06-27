package com.mingrisoft.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mingrisoft.sockword.R;

public class HistoryWordAdapter extends BaseAdapter {
    private String [] mData;
    private LayoutInflater mInflater;

    public HistoryWordAdapter(LayoutInflater inflater,String [] data) {
       mInflater=inflater;
       mData=data;
    }

    @Override
    public int getCount() {
        return mData.length;
    }

    @Override
    public Object getItem(int i) {
        return mData[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
    //ListView提升效率
    //convertView缓存之前加载过的View
    //ViewHolder存取之前加载过的对象
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //获得ListView中得View
        View view;
        String data=mData[position];
        String [] middelData=new String[2];       //用于存取data splite后的数据
        ViewHolder viewHolder;
        if(convertView==null){
            view=mInflater.inflate(R.layout.list_item_layout,null);
            viewHolder=new ViewHolder();
            viewHolder.wordText=(TextView) view.findViewById(R.id.history_word);
            viewHolder.basicText=(TextView) view.findViewById(R.id.history_basic);
            view.setTag(viewHolder);
        }else{
            view=convertView;
            viewHolder=(ViewHolder)view.getTag();
        }

        middelData=data.split("#");
        if(middelData.length==2 && position<mData.length) {
            viewHolder.wordText.setVisibility(View.VISIBLE);
            viewHolder.basicText.setVisibility(View.VISIBLE);
            viewHolder.wordText.setText(middelData[0]);
            viewHolder.basicText.setText("  " + middelData[1]);

        }else{     //解决convertView复用，使得数据混乱问题
            viewHolder.wordText.setVisibility(View.INVISIBLE);
            viewHolder.basicText.setVisibility(View.INVISIBLE);
        }

        return view;
    }
    class ViewHolder{
        TextView wordText;    //单词文本
        TextView basicText;   //释义文本
    }
}

