package com.thecroakers.app.ActivitiesFragment;

import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.thecroakers.app.MainMenu.RelateToFragmentOnBack.RootFragment;
import com.thecroakers.app.R;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class TrendingF extends RootFragment {

    View view;
    Context context;
    protected TabLayout tabLayout;
    protected ViewPager pager;
    private TrendingF.ViewPagerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_trending, container, false);
        context = getContext();

        return init();
    }

    private View init() {
        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        pager = view.findViewById(R.id.pager);
        pager.setOffscreenPageLimit(3);

        adapter = new TrendingF.ViewPagerAdapter(getChildFragmentManager());
        pager.setAdapter(adapter);
        tabLayout.setupWithViewPager(pager);

        setupTabIcons();

        return view;
    }

    private void setupTabIcons() {
        View view1 = LayoutInflater.from(context).inflate(R.layout.item_tabs_signup, null);
        TextView txtView1 = view1.findViewById(R.id.text_history);
        txtView1.setText(R.string.trending_tab_1);
        tabLayout.getTabAt(0).setCustomView(view1);

        View view2 = LayoutInflater.from(context).inflate(R.layout.item_tabs_signup, null);
        TextView txtView2 = view2.findViewById(R.id.text_history);
        txtView2.setText(R.string.trending_tab_2);
        tabLayout.getTabAt(1).setCustomView(view2);

        View view3 = LayoutInflater.from(context).inflate(R.layout.item_tabs_signup, null);
        TextView txtView3 = view3.findViewById(R.id.text_history);
        txtView3.setText(R.string.trending_tab_3);
        tabLayout.getTabAt(2).setCustomView(view3);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View v = tab.getCustomView();

                switch (tab.getPosition()) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
                tab.setCustomView(v);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v = tab.getCustomView();

                switch (tab.getPosition()) {
                    case 0:
                        break;
                    case 1:
                        break;

                    case 2:
                        break;
                }

                tab.setCustomView(v);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            final Fragment result;
            result = new DiscoverF(position);

            return result;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(final int position) {
            return null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        /**
         * Get the Fragment by position
         *
         * @param position tab position of the fragment
         * @return
         */
        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }
}
