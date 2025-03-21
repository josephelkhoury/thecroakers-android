package com.thecroakers.app.ActivitiesFragment;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.facebook.drawee.view.SimpleDraweeView;
import com.thecroakers.app.Adapters.MyVideosAdapter;
import com.thecroakers.app.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.thecroakers.app.Interfaces.AdapterClickListener;
import com.volley.plus.interfaces.Callback;
import com.thecroakers.app.Models.HomeModel;
import com.thecroakers.app.R;
import com.thecroakers.app.SimpleClasses.Functions;
import com.thecroakers.app.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class TagedVideosA extends AppCompatActivity implements View.OnClickListener {
    Context context;
    RecyclerView recyclerView;
    ArrayList<HomeModel> dataList;
    MyVideosAdapter adapter;

    String tagId, tagTxt, favourite = "0";

    SimpleDraweeView image;
    TextView tagTxtView, tagTitleTxt, videoCountTxt;
    ProgressBar progressBar;
    Toolbar toolbarLayout;
    TextView tvMessageNoData;

    TextView favTxt;
    ImageView favBtn;
    SwipeRefreshLayout refreshLayout;

    String main_video_id = "0";
    int pageCount = 0;
    boolean ispostFinish;
    ProgressBar loadMoreProgress;
    GridLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(TagedVideosA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, TagedVideosA.class,false);
        setContentView(R.layout.activity_taged_videos);
        context = TagedVideosA.this;

        tagTxt = getIntent().getStringExtra("title");
        main_video_id = getIntent().getStringExtra("main_video_id");
        refreshLayout = findViewById(R.id.refreshLayout);
        tagTxtView = findViewById(R.id.tag_txt_view);
        tagTitleTxt = findViewById(R.id.tag_title_txt);
        tvMessageNoData = findViewById(R.id.tvMessageNoData);

        if (main_video_id != null) {
            tagTitleTxt.setText(R.string.croaks);
            toolbarLayout = findViewById(R.id.toolbar_layout);
            toolbarLayout.setVisibility(View.GONE);
            tvMessageNoData.setText(context.getString(R.string.no_croaks));
        } else {
            tagTitleTxt.setText(tagTxt);
            tagTxtView.setText(tagTxt);
            videoCountTxt = findViewById(R.id.video_count_txt);
            image = findViewById(R.id.hashtag_icon);
            image.setController(Functions.frescoImageLoad(getIntent().getStringExtra("image"), image,false));
            favBtn = findViewById(R.id.fav_btn);
            favTxt = findViewById(R.id.fav_txt);
            findViewById(R.id.fav_layout).setOnClickListener(this::onClick);
            tvMessageNoData.setText(context.getString(R.string.no_videos));
        }

        loadMoreProgress = findViewById(R.id.load_more_progress);
        recyclerView = findViewById(R.id.recylerview);
        linearLayoutManager = new GridLayoutManager(context, 3);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        dataList = new ArrayList<>();
        adapter = new MyVideosAdapter(context, dataList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                openWatchVideo(pos);
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean userScrolled;
            int scrollOutitems;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                scrollOutitems = linearLayoutManager.findLastVisibleItemPosition();

                Functions.printLog("resp", "" + scrollOutitems);
                if (userScrolled && (scrollOutitems == dataList.size() - 1)) {
                    userScrolled = false;

                    if (loadMoreProgress.getVisibility() != View.VISIBLE && !ispostFinish) {
                        loadMoreProgress.setVisibility(View.VISIBLE);
                        pageCount = pageCount + 1;
                        callApiForGetAllVideos(false);
                    }
                }
            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(false);
                pageCount=0;
                callApiForGetAllVideos(false);
            }
        });


        progressBar = findViewById(R.id.progress_bar);

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               TagedVideosA.super.onBackPressed();
            }
        });

        callApiForGetAllVideos(true);
    }


    //this will get the all videos data of user and then parse the data
    private void callApiForGetAllVideos(boolean isProgressShow) {
        if (isProgressShow) {
            progressBar.setVisibility(View.VISIBLE);
        }

        if (dataList == null)
            dataList = new ArrayList<>();

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""));
            parameters.put("starting_point", "" + pageCount);
            if (main_video_id != null) {
                parameters.put("video_id", main_video_id);
            } else {
                parameters.put("hashtag", tagTxt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String apiStr;
        if (main_video_id != null) {
            apiStr = ApiLinks.showVideoReplies;
        } else {
            apiStr = ApiLinks.showVideosAgainstHashtag;
        }

        VolleyRequest.JsonPostRequest(TagedVideosA.this, apiStr, parameters,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(TagedVideosA.this, resp);
                if (isProgressShow) {
                    progressBar.setVisibility(View.GONE);
                }

                parseData(resp);
            }
        });
    }

    // parse the data of video list
    public void parseData(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                if (pageCount == 0) {
                    dataList.clear();
                    adapter.notifyDataSetChanged();
                }

                JSONArray msgArray = jsonObject.getJSONArray("msg");

                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);

                    if (main_video_id == null) {
                        JSONObject hashtag = itemdata.optJSONObject("Hashtag");
                        tagId = hashtag.optString("id");
                        favourite = hashtag.optString("favourite");
                    }

                    JSONObject video = itemdata.optJSONObject("Video");
                    JSONObject user;
                    JSONObject sound;

                    if (main_video_id == null) {
                        user = video.optJSONObject("User");
                        sound = video.optJSONObject("Sound");
                    } else {
                        user = itemdata.optJSONObject("User");
                        sound = itemdata.optJSONObject("Sound");
                    }
                    JSONObject topic = video.optJSONObject("Topic");
                    JSONObject country = video.optJSONObject("Country");
                    JSONObject userPrivacy = user.optJSONObject("PrivacySetting");
                    JSONObject userPushNotification = user.optJSONObject("PushNotification");

                    HomeModel item = Functions.parseVideoData(user, sound, video, topic, country, userPrivacy, userPushNotification);

                    dataList.add(item);
                    adapter.notifyItemInserted(dataList.size());
                }

                if (main_video_id == null) {
                    if (favourite != null && favourite.equalsIgnoreCase("1")) {
                        favBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_fav_fill));
                        favTxt.setText(getString(R.string.added_to_favourite));
                    } else {
                        favBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_fav));
                        favTxt.setText(getString(R.string.add_to_favourite));
                    }
                    videoCountTxt.setText(jsonObject.optString("videos_count") + " "+getString(R.string.videos));
                }
            } else if (pageCount == 0) {
                dataList.clear();
                adapter.notifyDataSetChanged();
            }

            if (dataList.isEmpty()) {
               findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.no_data_layout).setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            loadMoreProgress.setVisibility(View.GONE);
        }
    }

    private void openWatchVideo(int position) {
        Intent intent = new Intent(TagedVideosA.this, WatchVideosA.class);
        intent.putExtra("arraylist", dataList);
        intent.putExtra("position", position);
        intent.putExtra("pageCount", pageCount);
        intent.putExtra("hashtag", tagTxt);
        intent.putExtra("userId", Functions.getSharedPreference(TagedVideosA.this).getString(Variables.U_ID,""));
        intent.putExtra("whereFrom","tagedVideo");
        resultCallback.launch(intent);
    }

    ActivityResultLauncher<Intent> resultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data.getBooleanExtra("isShow",false)) {
                            dataList= (ArrayList<HomeModel>) data.getSerializableExtra("arraylist");
                            pageCount=data.getIntExtra("pageCount",0);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            });

    public void callApiFavHashtag() {
        JSONObject params = new JSONObject();
        try {
            params.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""));
            params.put("hashtag_id", tagId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(context, false, false);
        VolleyRequest.JsonPostRequest(TagedVideosA.this, ApiLinks.addHashtagFavourite, params,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                 Functions.checkStatus(TagedVideosA.this,resp);

                 Functions.cancelLoader();
                 if (favourite != null && favourite.equalsIgnoreCase("0")) {
                    favourite = "1";
                    favBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_fav_fill));
                    favTxt.setText(getString(R.string.added_to_favourite));
                } else {
                    favourite = "0";
                    favBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_fav));
                    favTxt.setText(getString(R.string.add_to_favourite));
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fav_layout:
                if (Functions.checkLoginUser(TagedVideosA.this)) {
                    callApiFavHashtag();
                }
                break;
        }
    }
}
