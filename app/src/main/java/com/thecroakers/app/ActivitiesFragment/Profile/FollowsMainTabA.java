package com.thecroakers.app.ActivitiesFragment.Profile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.thecroakers.app.ActivitiesFragment.Profile.FollowTab.FollowerUserF;
import com.thecroakers.app.ActivitiesFragment.Profile.FollowTab.FollowingUserF;
import com.thecroakers.app.ActivitiesFragment.Profile.FollowTab.SuggestedUserF;
import com.thecroakers.app.ActivitiesFragment.Profile.Setting.NoInternetA;
import com.thecroakers.app.Adapters.ViewPagerAdapter;
import com.thecroakers.app.Interfaces.InternetCheckCallback;
import com.thecroakers.app.R;
import com.thecroakers.app.SimpleClasses.Functions;
import com.thecroakers.app.SimpleClasses.Variables;

public class FollowsMainTabA extends AppCompatActivity {

    Context context;
    TextView tvTitle;
    String userName="",userId="",followerCount="",followingCount="";
    boolean isSelf=false;
    String fromWhere="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(FollowsMainTabA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, FollowsMainTabA.class,false);
        setContentView(R.layout.activity_follows_main_tab_);

        context = FollowsMainTabA.this;
        tvTitle=findViewById(R.id.tvTitle);

        followingCount=getIntent().getStringExtra("followingCount");
        followerCount=getIntent().getStringExtra("followerCount");
        userId=getIntent().getStringExtra("id");
        userName=getIntent().getStringExtra("userName");
        fromWhere=getIntent().getStringExtra("from_where");
        tvTitle.setText(Functions.showUsername(userName));
        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FollowsMainTabA.super.onBackPressed();
            }
        });

        if (userId.equalsIgnoreCase(Functions.getSharedPreference(context).getString(Variables.U_ID, "")))
        {
            isSelf=true;
        }
        else
        {
            isSelf=false;
        }

        Set_tabs();
    }

    protected TabLayout tabLayout;
    protected ViewPager menuPager;
    ViewPagerAdapter adapter;

    public void Set_tabs() {

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        menuPager = (ViewPager) findViewById(R.id.viewpager);
        menuPager.setOffscreenPageLimit(3);
        tabLayout = (TabLayout) findViewById(R.id.tabs);


        adapter.addFrag(new FollowingUserF(userId,userName,isSelf), getString(R.string.following)+" "+followingCount);
        adapter.addFrag(new FollowerUserF(userId,isSelf), getString(R.string.followers)+" "+followerCount);
        adapter.addFrag(new SuggestedUserF(userId,isSelf), getString(R.string.suggested));


        menuPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(menuPager);


        if (fromWhere.equalsIgnoreCase("following"))
        {
            tabLayout.getTabAt(0).select();
        }
        else
        if (fromWhere.equalsIgnoreCase("fan"))
        {
            tabLayout.getTabAt(1).select();
        }
        else
        {
            tabLayout.getTabAt(2).select();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        Functions.RegisterConnectivity(this, new InternetCheckCallback() {
            @Override
            public void GetResponse(String requestType, String response) {
                if(response.equalsIgnoreCase("disconnected")) {
                    startActivity(new Intent(getApplicationContext(), NoInternetA.class));
                    overridePendingTransition(R.anim.in_from_bottom,R.anim.out_to_top);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Functions.unRegisterConnectivity(getApplicationContext());
    }

}