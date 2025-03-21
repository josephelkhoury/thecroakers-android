package com.thecroakers.app.ActivitiesFragment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thecroakers.app.ActivitiesFragment.Profile.Setting.NoInternetA;
import com.thecroakers.app.Adapters.ViewPagerStatAdapter;
import com.thecroakers.app.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.thecroakers.app.Constants;
import com.volley.plus.interfaces.Callback;
import com.thecroakers.app.Interfaces.FragmentCallBack;
import com.thecroakers.app.Interfaces.InternetCheckCallback;
import com.thecroakers.app.Models.HomeModel;
import com.thecroakers.app.R;
import com.thecroakers.app.Services.UploadService;
import com.thecroakers.app.SimpleClasses.Functions;
import com.thecroakers.app.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import fr.castorflex.android.verticalviewpager.VerticalViewPager;


public class WatchVideosA extends AppCompatActivity implements View.OnClickListener,FragmentCallBack {

    Context context;
    ArrayList<HomeModel> dataList=new ArrayList<>();
    SwipeRefreshLayout swipeRefresh;
    int pageCount = 0;
    boolean isApiRunning = false;
    Handler handler;
    RelativeLayout uploadVideoLayout;
    ImageView uploadingThumb;
    UploadingVideoBroadCast mReceiver;
    String whereFrom = "";
    String userId = "";
    int currentPosition = 0;

    private static ProgressBar progressBar;
    private static TextView tvProgressCount;

    //this is use for use same class functionality from different activities
    int fragmentContainerId;

    @Override
    public void onResponse(Bundle bundle) {
        if (bundle.getString("action").equalsIgnoreCase("deleteVideo")) {
            dataList.remove(bundle.getInt("position"));
            Log.d(Constants.tag,"notify data : "+dataList.size());
            if (dataList.size() == 0) {
                onBackPressed();
            }
        }
    }

    private class UploadingVideoBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Functions.isMyServiceRunning(context, UploadService.class)) {
                uploadVideoLayout.setVisibility(View.VISIBLE);
                Bitmap bitmap = Functions.base64ToBitmap(Functions.getSharedPreference(context).getString(Variables.UPLOADING_VIDEO_THUMB, ""));
                if (bitmap != null)
                    uploadingThumb.setImageBitmap(bitmap);
            } else {
                uploadVideoLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try { getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); }catch (Exception e){}
        Functions.setLocale(Functions.getSharedPreference(WatchVideosA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, WatchVideosA.class,false);
        setContentView(R.layout.activity_watch_videos);
        fragmentContainerId = R.id.watchVideo_F;
        context = WatchVideosA.this;

        tvProgressCount = findViewById(R.id.tvProgressCount);
        progressBar = findViewById(R.id.progressBar);
        whereFrom = getIntent().getStringExtra("whereFrom");
        userId = getIntent().getStringExtra("userId");
        pageCount = getIntent().getIntExtra("pageCount",0);
        currentPosition = getIntent().getIntExtra("position",0);
        if (whereFrom.equalsIgnoreCase("IdVideo")) {
            dataList = new ArrayList<>();
            callApiForSingleVideos(getIntent().getStringExtra("video_id"));
        } else {
            dataList = (ArrayList<HomeModel>) getIntent().getSerializableExtra("arraylist");
        }

        handler = new Handler(Looper.getMainLooper());
        findViewById(R.id.goBack).setOnClickListener(this);
        swipeRefresh = findViewById(R.id.swiperefresh);
        swipeRefresh.setProgressViewOffset(false, 0, 200);

        swipeRefresh.setColorSchemeResources(R.color.black);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageCount = 0;
                dataList.clear();
                callVideoApi();
            }
        });

        uploadVideoLayout = findViewById(R.id.upload_video_layout);
        uploadingThumb = findViewById(R.id.uploading_thumb);
        mReceiver = new UploadingVideoBroadCast();
        registerReceiver(mReceiver, new IntentFilter("uploadVideo"));

        if (Functions.isMyServiceRunning(context, UploadService.class)) {
            uploadVideoLayout.setVisibility(View.VISIBLE);
            Bitmap bitmap = Functions.base64ToBitmap(Functions.getSharedPreference(context).getString(Variables.UPLOADING_VIDEO_THUMB, ""));
            if (bitmap != null)
                uploadingThumb.setImageBitmap(bitmap);
        }

        setTabs(false);
        setUpPreviousScreenData();
    }

    private void setUpPreviousScreenData() {
        for (HomeModel item : dataList) {
            pagerStatAdapter.addFragment(new VideosListF(false, item, menuPager, this,fragmentContainerId), "");
        }
        pagerStatAdapter.refreshStateSet(false);
        pagerStatAdapter.notifyDataSetChanged();

        menuPager.setCurrentItem(currentPosition,true);
    }

    public static FragmentCallBack uploadingCallback=new FragmentCallBack() {
        @Override
        public void onResponse(Bundle bundle) {
            if (bundle.getBoolean("isShow")) {
                int currentProgress=bundle.getInt("currentpercent",0);
                if (progressBar!=null && tvProgressCount!=null) {
                    progressBar.setProgress(currentProgress);
                    tvProgressCount.setText(currentProgress+"%");
                }
            }
        }
    };

    // set the fragments for all the videos list
    protected VerticalViewPager menuPager;
    ViewPagerStatAdapter pagerStatAdapter;

    public void setTabs(boolean isListSet) {

        if (isListSet) {
            dataList = new ArrayList<>();
        }

        pagerStatAdapter = new ViewPagerStatAdapter(getSupportFragmentManager(), menuPager, false, this);
        menuPager =  findViewById(R.id.viewpager);
        menuPager.setAdapter(pagerStatAdapter);
        menuPager.setOffscreenPageLimit(1);
        menuPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    swipeRefresh.setEnabled(true);
                } else {
                    swipeRefresh.setEnabled(false);
                }
                if (position == 0 && (pagerStatAdapter !=null && pagerStatAdapter.getCount()>0)) {
                    VideosListF fragment = (VideosListF) pagerStatAdapter.getItem(menuPager.getCurrentItem());
                    fragment.setData();
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fragment.setPlayer(true);
                        }
                    }, 200);
                }

                Log.d(Constants.tag,"Check : check "+(position+1)+"    "+(dataList.size()-1)+"      "+(dataList.size() > 2 && (dataList.size() - 1) == position));
                Log.d(Constants.tag,"Test : Test "+(position+1)+"    "+(dataList.size()-5)+"      "+(dataList.size() > 5 && (dataList.size() - 5) == (position+1)));
                if (dataList.size() > 5 && (dataList.size() -5) == (position+1)) {
                    if (!isApiRunning) {
                        pageCount++;
                        callVideoApi();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void callApiForSingleVideos(String videoId) {
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("user_id", userId);
            parameters.put("video_id", videoId);

            VolleyRequest.JsonPostRequest(this, ApiLinks.showVideoDetail, parameters, Functions.getHeaders(this), new Callback() {
                @Override
                public void onResponce(String resp) {
                    swipeRefresh.setRefreshing(false);
                    singleVideoParseData(resp);
                }
            });

        } catch (Exception e) {
            Functions.printLog(Constants.tag, e.toString());
        }
    }

    public void singleVideoParseData(String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONObject msg = jsonObject.optJSONObject("msg");
                ArrayList<HomeModel> temp_list = new ArrayList<>();

                JSONObject video = msg.optJSONObject("Video");
                JSONObject user = msg.optJSONObject("User");
                JSONObject sound = msg.optJSONObject("Sound");
                JSONObject topic = msg.optJSONObject("Topic");
                JSONObject country = msg.optJSONObject("Country");
                JSONObject userPrivacy = user.optJSONObject("PrivacySetting");
                JSONObject pushNotification = user.optJSONObject("PushNotification");

                {
                    HomeModel item = Functions.parseVideoData(user, sound, video, topic, country, userPrivacy, pushNotification);

                    temp_list.add(item);

                    if (dataList.isEmpty()) {
                        setTabs(true);
                    }
                    dataList.addAll(temp_list);
                }

                for (HomeModel item : temp_list) {
                    pagerStatAdapter.addFragment(new VideosListF(false, item, menuPager, this,fragmentContainerId), "");
                }
                pagerStatAdapter.refreshStateSet(false);
                pagerStatAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            e.printStackTrace();

            if (pageCount > 0)
                pageCount--;

        } finally {
            isApiRunning = false;
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goBack:
                onBackPressed();
                break;
        }
    }

    public void callVideoApi() {
        isApiRunning = true;

        if (whereFrom.equalsIgnoreCase("userVideo")) {
            callApiForUserVideos();
        } else if (whereFrom.equalsIgnoreCase("likedVideo")) {
            callApiForLikedVideos();
        } else if (whereFrom.equalsIgnoreCase("privateVideo")) {
            callApiForPrivateVideos();
        } else if (whereFrom.equalsIgnoreCase("tagedVideo")) {
            callApiForTagedVideos();
        }
        else if (whereFrom.equalsIgnoreCase("videoSound")) {
            callApiForSoundVideos();
        }
        else if (whereFrom.equalsIgnoreCase("IdVideo")) {
            callApiForSingleVideos(getIntent().getStringExtra("video_id"));
        }
    }

    // api for get the videos list from server
    private void callApiForSoundVideos() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("sound_id", getIntent().getStringExtra("soundId"));
            parameters.put("device_id", getIntent().getStringExtra("deviceId"));
            parameters.put("starting_point", "" + pageCount);

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(WatchVideosA.this, ApiLinks.showVideosAgainstSound, parameters,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                swipeRefresh.setRefreshing(false);
                parseSoundVideoData(resp);
            }
        });
    }

    public void parseSoundVideoData(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");

                ArrayList<HomeModel> temp_list = new ArrayList<>();

                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);

                    JSONObject video = itemdata.optJSONObject("Video");
                    JSONObject user = itemdata.optJSONObject("User");
                    JSONObject sound = itemdata.optJSONObject("Sound");
                    JSONObject topic = itemdata.optJSONObject("Topic");
                    JSONObject country = itemdata.optJSONObject("Country");
                    JSONObject userPrivacy = user.optJSONObject("PrivacySetting");
                    JSONObject userPushNotification = user.optJSONObject("PushNotification");

                    HomeModel item = Functions.parseVideoData(user, sound, video, topic, country, userPrivacy, userPushNotification);

                    temp_list.add(item);
                }

                if (dataList.isEmpty()) {
                    setTabs(true);
                }
                dataList.addAll(temp_list);

                for (HomeModel item : temp_list) {
                    pagerStatAdapter.addFragment(new VideosListF(false, item, menuPager,this,fragmentContainerId), "");
                }
                pagerStatAdapter.refreshStateSet(false);
                pagerStatAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            e.printStackTrace();

            if (pageCount >0)
                pageCount--;

        } finally {
            isApiRunning = false;
        }
    }

    // api for get the videos list from server
    private void callApiForTagedVideos() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", userId);
            parameters.put("hashtag", getIntent().getStringExtra("hashtag"));
            parameters.put("starting_point", "" + pageCount);
        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(WatchVideosA.this, ApiLinks.showVideosAgainstHashtag, parameters,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                swipeRefresh.setRefreshing(false);
                parseHashtagVideoData(resp);
            }
        });
    }

    public void parseHashtagVideoData(String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                ArrayList<HomeModel> temp_list = new ArrayList<>();

                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);
                    JSONObject video = itemdata.optJSONObject("Video");
                    JSONObject user = video.optJSONObject("User");
                    JSONObject sound = video.optJSONObject("Sound");
                    JSONObject topic = itemdata.optJSONObject("Topic");
                    JSONObject country = itemdata.optJSONObject("Country");
                    JSONObject userPrivacy = user.optJSONObject("PrivacySetting");
                    JSONObject userPushNotification = user.optJSONObject("PushNotification");

                    HomeModel item = Functions.parseVideoData(user, sound, video, topic, country, userPrivacy, userPushNotification);

                    temp_list.add(item);
                }

                if(dataList.isEmpty()) {
                    setTabs(true);
                }
                dataList.addAll(temp_list);

                for (HomeModel item : temp_list) {
                    pagerStatAdapter.addFragment(new VideosListF(false, item, menuPager, this,fragmentContainerId), "");
                }
                pagerStatAdapter.refreshStateSet(false);
                pagerStatAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (pageCount > 0)
                pageCount--;
        } finally {
            isApiRunning = false;
        }
    }

    public void parsePrivateVideoData(String responce) {
        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONObject msg = jsonObject.optJSONObject("msg");
                JSONArray public_array = msg.optJSONArray("private");

                ArrayList<HomeModel> temp_list = new ArrayList<>();

                for (int i = 0; i < public_array.length(); i++) {
                    JSONObject itemdata = public_array.optJSONObject(i);

                    JSONObject video = itemdata.optJSONObject("Video");
                    JSONObject user = itemdata.optJSONObject("User");
                    JSONObject sound = itemdata.optJSONObject("Sound");
                    JSONObject topic = itemdata.optJSONObject("Topic");
                    JSONObject country = itemdata.optJSONObject("Country");
                    JSONObject userPrivacy = user.optJSONObject("PrivacySetting");
                    JSONObject userPushNotification = user.optJSONObject("PushNotification");

                    HomeModel item = Functions.parseVideoData(user, sound, video, topic, country, userPrivacy, userPushNotification);

                    temp_list.add(item);
                }

                if (dataList.isEmpty()) {
                    setTabs(true);
                }
                dataList.addAll(temp_list);

                for (HomeModel item : temp_list) {
                    pagerStatAdapter.addFragment(new VideosListF(false, item, menuPager,this,fragmentContainerId), "");
                }
                pagerStatAdapter.refreshStateSet(false);
                pagerStatAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (pageCount >0)
                pageCount--;

        } finally {
            isApiRunning = false;
        }
    }

    public void parseLikedVideoData(String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                ArrayList<HomeModel> temp_list = new ArrayList<>();

                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);

                    JSONObject video = itemdata.optJSONObject("Video");
                    JSONObject user = video.optJSONObject("User");
                    JSONObject sound = video.optJSONObject("Sound");
                    JSONObject topic = itemdata.optJSONObject("Topic");
                    JSONObject country = itemdata.optJSONObject("Country");
                    JSONObject userPrivacy = user.optJSONObject("PrivacySetting");
                    JSONObject userPushNotification = user.optJSONObject("PushNotification");

                    HomeModel item = Functions.parseVideoData(user, sound, video, topic, country, userPrivacy, userPushNotification);

                    temp_list.add(item);
                }
                if (dataList.isEmpty()) {
                    setTabs(true);
                }
                dataList.addAll(temp_list);

                for (HomeModel item : temp_list) {
                    pagerStatAdapter.addFragment(new VideosListF(false, item, menuPager,this,fragmentContainerId), "");
                }
                pagerStatAdapter.refreshStateSet(false);
                pagerStatAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            e.printStackTrace();

            if(pageCount >0)
                pageCount--;

        } finally {
            isApiRunning = false;
        }

    }

    public void parseMyVideoData(String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONObject msg = jsonObject.optJSONObject("msg");
                ArrayList<HomeModel> temp_list = new ArrayList<>();

                JSONArray public_array = msg.optJSONArray("public");

                for (int i = 0; i < public_array.length(); i++) {
                    JSONObject itemdata = public_array.optJSONObject(i);

                    JSONObject video = itemdata.optJSONObject("Video");
                    JSONObject user = itemdata.optJSONObject("User");
                    JSONObject sound = itemdata.optJSONObject("Sound");
                    JSONObject topic = itemdata.optJSONObject("Topic");
                    JSONObject country = itemdata.optJSONObject("Country");
                    JSONObject userPrivacy = user.optJSONObject("PrivacySetting");
                    JSONObject userPushNotification = user.optJSONObject("PushNotification");

                    HomeModel item = Functions.parseVideoData(user, sound, video, topic, country, userPrivacy, userPushNotification);

                    temp_list.add(item);
                }

                if (dataList.isEmpty()) {
                    setTabs(true);
                }
                dataList.addAll(temp_list);

                for (HomeModel item : temp_list) {
                    pagerStatAdapter.addFragment(new VideosListF(false, item, menuPager,this,fragmentContainerId), "");
                }
                pagerStatAdapter.refreshStateSet(false);
                pagerStatAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            e.printStackTrace();

            if (pageCount > 0)
                pageCount--;

        } finally {
            isApiRunning = false;
        }
    }

    // api for get the videos list from server
    private void callApiForUserVideos() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""));

            if (!(userId.equalsIgnoreCase(Functions.getSharedPreference(context).getString(Variables.U_ID, "")))) {
                parameters.put("other_user_id", userId);
            }
            parameters.put("starting_point", "" + pageCount);

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(WatchVideosA.this, ApiLinks.showVideosAgainstUserID, parameters,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                swipeRefresh.setRefreshing(false);
                parseMyVideoData(resp);
            }
        });
    }

    // api for get the videos list from server
    private void callApiForLikedVideos() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", userId);
            parameters.put("starting_point", "" + pageCount);

        } catch (Exception e) {
            e.printStackTrace();
        }
        VolleyRequest.JsonPostRequest(WatchVideosA.this, ApiLinks.showUserLikedVideos, parameters,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                swipeRefresh.setRefreshing(false);
                parseLikedVideoData(resp);
            }
        });
    }

    // api for get the videos list from server
    private void callApiForPrivateVideos() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", userId);
            parameters.put("starting_point", "" + pageCount);

        } catch (Exception e) {
            e.printStackTrace();
        }
        VolleyRequest.JsonPostRequest(WatchVideosA.this, ApiLinks.showVideosAgainstUserID, parameters,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                swipeRefresh.setRefreshing(false);
                parsePrivateVideoData(resp);
            }
        });
    }

    private static int callbackVideoLisCode=3292;
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(Constants.tag,"Callback check : "+requestCode);
        if (requestCode==callbackVideoLisCode) {
            Bundle bundle=new Bundle();
            bundle.putBoolean("isShow",true);
            VideosListF.videoListCallback.onResponse(bundle);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (pagerStatAdapter != null && pagerStatAdapter.getCount() > 0) {
            VideosListF fragment = (VideosListF) pagerStatAdapter.getItem(menuPager.getCurrentItem());
            fragment.mainMenuVisibility(true);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("isShow", true);
        intent.putExtra("arraylist", dataList);
        intent.putExtra("pageCount", pageCount);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        super.onDestroy();
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
        if (pagerStatAdapter !=null && pagerStatAdapter.getCount() > 0) {

            VideosListF fragment = (VideosListF) pagerStatAdapter.getItem(menuPager.getCurrentItem());
            fragment.mainMenuVisibility(false);
        }
        Functions.unRegisterConnectivity(getApplicationContext());
    }

    public void moveBack() {
        Intent intent = new Intent();
        intent.putExtra("isShow", true);
        setResult(RESULT_OK, intent);
        finish();
    }
}