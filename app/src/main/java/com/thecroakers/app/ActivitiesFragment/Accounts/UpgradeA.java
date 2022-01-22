package com.thecroakers.app.ActivitiesFragment.Accounts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.thecroakers.app.Constants;
import com.thecroakers.app.R;

import com.thecroakers.app.SimpleClasses.Functions;
import com.thecroakers.app.SimpleClasses.Variables;

public class UpgradeA extends AppCompatActivity implements View.OnClickListener {

    SharedPreferences sharedPreferences;

    View topView;
    long mBackPressed;
    TextView loginTitleTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(UpgradeA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, UpgradeA.class,false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (Build.VERSION.SDK_INT == 26) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
        }

        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_upgrade);

        sharedPreferences = getSharedPreferences(Variables.PREF_NAME, MODE_PRIVATE);

        findViewById(R.id.croaker_btn).setOnClickListener(this::onClick);
        findViewById(R.id.publisher_btn).setOnClickListener(this::onClick);
        findViewById(R.id.goBack).setOnClickListener(this::onClick);

        topView = findViewById(R.id.top_view);

        loginTitleTxt = findViewById(R.id.login_title_txt);
        loginTitleTxt.setText(getString(R.string.apply_as_croaker_or_publisher));

        Functions.PrintHashKey(UpgradeA.this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goBack:
                onBackPressed();
                break;

            case R.id.croaker_btn:
            case R.id.publisher_btn:
                openURL();
                break;
        }

    }

    public void openURL() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.apply_as_croaker_or_publisher));
        startActivity(browserIntent);
    }

    // this method will be call when activity animation will complete
    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();

        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(200);
        topView.startAnimation(anim);
        topView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        int count = this.getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            if (mBackPressed + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();
                return;
            } else {
                topView.setVisibility(View.GONE);
                finish();
                overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom);
            }
        } else {
            super.onBackPressed();
        }
    }
}
