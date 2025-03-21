package com.thecroakers.app.Adapters;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import com.thecroakers.app.ActivitiesFragment.VideosListF;
import com.thecroakers.app.Constants;
import com.thecroakers.app.Interfaces.FragmentCallBack;
import com.thecroakers.app.Models.HomeModel;
import com.thecroakers.app.R;
import com.thecroakers.app.SimpleClasses.Variables;

import java.util.ArrayList;
import java.util.List;

import fr.castorflex.android.verticalviewpager.VerticalViewPager;
import io.paperdb.Paper;

public class ViewPagerStatAdapter extends FragmentStatePagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();
    private static int PAGE_REFRESH_STATE=PagerAdapter.POSITION_UNCHANGED;
    VerticalViewPager menuPager;

    FragmentCallBack callBack;

    public ViewPagerStatAdapter(@NonNull FragmentManager fm,VerticalViewPager menuPager,boolean isFirstTime,FragmentCallBack callBack) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.menuPager = menuPager;
        this.callBack = callBack;

        if (isFirstTime) {
            Log.d(Constants.tag,"Check : init ");
            if (Paper.book(Variables.PromoAds).contains(Variables.PromoAdsModel)) {
                HomeModel initItem = Paper.book(Variables.PromoAds).read(Variables.PromoAdsModel);
                if (initItem != null)
                    addFragment(new VideosListF(true,initItem,menuPager,callBack, R.id.mainMenuFragment), "");
            }
        }
    }

    public void refreshStateSet(boolean isRefresh) {
       if (isRefresh) {
           PAGE_REFRESH_STATE = PagerAdapter.POSITION_NONE;
       } else {
           PAGE_REFRESH_STATE = PagerAdapter.POSITION_UNCHANGED;
       }
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    public void removeFragment(int position) {
        mFragmentList.remove(position);
        mFragmentTitleList.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object) {
        // refresh all fragments when data set changed
        Log.d(Constants.tag,"Check : Refresh "+PAGE_REFRESH_STATE);

        return PAGE_REFRESH_STATE;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }
}