package com.mingrisoft.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mingrisoft.fragment.CardFragment;
import com.mingrisoft.fragment.ReviewFragment;
import com.mingrisoft.greendao.entity.greendao.CET4Entity;

import java.util.List;

public class ReviewFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private List<CET4Entity> reviewData;

    public ReviewFragmentPagerAdapter(FragmentManager fm, List<CET4Entity> list) {
        super(fm);
        this.reviewData = list;
    }

    @Override
    public Fragment getItem(int position) {
            return ReviewFragment.newInstance(reviewData.get(position));
    }

    @Override
    public int getCount() {
        return this.reviewData.size();
    }

}
