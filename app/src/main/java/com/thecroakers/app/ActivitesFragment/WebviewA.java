package com.thecroakers.app.ActivitesFragment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.thecroakers.app.ActivitesFragment.Profile.Setting.NoInternetA;
import com.thecroakers.app.Constants;
import com.thecroakers.app.Interfaces.InternetCheckCallback;
import com.thecroakers.app.R;
import com.thecroakers.app.SimpleClasses.Functions;
import com.thecroakers.app.SimpleClasses.Variables;

public class WebviewA extends AppCompatActivity implements View.OnClickListener{



    ProgressBar progressBar;
    WebView webView;
    String url = "www.google.com";
    String title;
    TextView titleTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(WebviewA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, WebviewA.class,false);
        setContentView(R.layout.activity_webview);

        url = getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");
        if (title.equals(getString(R.string.promote_video))) {
            findViewById(R.id.toolbar).setVisibility(View.GONE);
        }

        Functions.printLog(Constants.tag,url);


        findViewById(R.id.goBack).setOnClickListener(this::onClick);

        titleTxt = findViewById(R.id.title_txt);
        titleTxt.setText(title);

        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progress_bar);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress >= 80) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });


        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                if (url.equalsIgnoreCase("closePopup")) {
                    WebviewA.super.onBackPressed();
                }
                return false;
            }
        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goBack:
                WebviewA.super.onBackPressed();
                break;
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
