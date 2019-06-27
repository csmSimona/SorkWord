package com.mingrisoft.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mingrisoft.fragment.CardFragment;
import com.mingrisoft.greendao.entity.greendao.CET4Entity;

import java.util.List;

public class CardFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private List<CET4Entity> wrongData;

    public CardFragmentPagerAdapter(FragmentManager fm, List<CET4Entity> list) {
        super(fm);
        this.wrongData = list;
    }

    @Override
    public Fragment getItem(int position) {
            return CardFragment.newInstance(wrongData.get(position));
    }

    @Override
    public int getCount() {
        return this.wrongData.size();
    }

}
