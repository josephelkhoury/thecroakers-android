package com.thecroakers.app.ActivitiesFragment;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ProgressBar;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.thecroakers.app.ActivitiesFragment.Profile.ProfileA;
import com.thecroakers.app.ActivitiesFragment.Search.SearchMainA;
import com.thecroakers.app.ActivitiesFragment.VideoRecording.PostVideoA;
import com.thecroakers.app.Adapters.DiscoverAdapter;
import com.thecroakers.app.Adapters.SlidingAdapter;
import com.thecroakers.app.Constants;
import com.thecroakers.app.Models.DiscoverModel;
import com.thecroakers.app.Models.HomeModel;
import com.thecroakers.app.MainMenu.RelateToFragmentOnBack.RootFragment;
import com.thecroakers.app.Models.SliderModel;
import com.thecroakers.app.R;
import com.thecroakers.app.Interfaces.AdapterClickListener;
import com.thecroakers.app.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.thecroakers.app.SimpleClasses.Functions;
import com.thecroakers.app.SimpleClasses.Variables;
import com.rd.PageIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class DiscoverF extends RootFragment implements View.OnClickListener {

    View view;
    Context context;
    RecyclerView recyclerViewDiscover;
    SwipeRefreshLayout swiperefresh;

    ArrayList<DiscoverModel> datalist = new ArrayList<>();
    ArrayList<JSONObject> countries = new ArrayList<>();
    ArrayList<String> countriesStr = new ArrayList<String>();
    DiscoverAdapter adapter;
    PageIndicatorView pageIndicatorView;
    ViewPager viewPager;

    ShimmerFrameLayout shimmerFrameLayout;
    CoordinatorLayout dataContainer;
    int pageCount = 0;
    boolean ispostFinish;
    ProgressBar loadMoreProgress;
    LinearLayoutManager linearLayoutManager;
    Button countryBtn;

    boolean isDiscoverAPiCall = false;
    boolean isSliderApiCall = false;

    int section = 0;
    String country_id = "0";

    public DiscoverF(int section) {
        this.section = section;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_discover, container, false);
        context = getContext();

        datalist = new ArrayList<>();
        shimmerFrameLayout = view.findViewById(R.id.shimmer_view_container);
        dataContainer = view.findViewById(R.id.dataContainer);
        loadMoreProgress = view.findViewById(R.id.load_more_progress);
        recyclerViewDiscover = view.findViewById(R.id.recylerview);
        linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerViewDiscover.setLayoutManager(linearLayoutManager);
        recyclerViewDiscover.setHasFixedSize(true);
        countryBtn = view.findViewById(R.id.country_btn);

        getCountries();

        adapter = new DiscoverAdapter(context, datalist, new DiscoverAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, ArrayList<HomeModel> home_models, int parent_position, int child_position) {
               if (view.getId() == R.id.hashtag_layout || home_models.get(child_position).thum == null) {
                    openEntity(datalist.get(parent_position));
                } else {
                    openWatchVideo(child_position, home_models, datalist.get(parent_position).title);
                }
            }
        });
        recyclerViewDiscover.setAdapter(adapter);

        recyclerViewDiscover.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean userScrolled;
            int scrollOutitems,scrollInItem;

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

                scrollInItem = linearLayoutManager.findFirstVisibleItemPosition();
                scrollOutitems = linearLayoutManager.findLastVisibleItemPosition();

//                if (scrollInItem == 0)
//                {
//                    recyclerViewDiscover.setNestedScrollingEnabled(true);
//                }
//                else
//                {
//                    recyclerViewDiscover.setNestedScrollingEnabled(false);
//                }
                if (userScrolled && (scrollOutitems == datalist.size() - 1)) {
                    userScrolled = false;

                    if (loadMoreProgress.getVisibility() != View.VISIBLE && !ispostFinish) {
                        loadMoreProgress.setVisibility(View.VISIBLE);
                        pageCount = pageCount + 1;
                        callApiForAllVideos();
                    }
                }
            }
        });

        //viewPager = view.findViewById(R.id.viewPager);
        //pageIndicatorView = view.findViewById(R.id.pageIndicatorView);
        swiperefresh = view.findViewById(R.id.swiperefresh);
        swiperefresh.setColorSchemeResources(R.color.black);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (datalist.size()<1) {
                    dataContainer.setVisibility(View.GONE);
                    shimmerFrameLayout.setVisibility(View.VISIBLE);
                    shimmerFrameLayout.startShimmer();
                }
                //callApiSlider();
                pageCount = 0;
                callApiForAllVideos();
            }
        });

        view.findViewById(R.id.search_layout).setOnClickListener(this);
        view.findViewById(R.id.search_edit).setOnClickListener(this);
        countryBtn.setOnClickListener(this);

        return view;
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible && datalist.size()<1) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    shimmerFrameLayout.startShimmer();
                    callApiForAllVideos();
                    //callApiSlider();
                }
            }, 200);
        }
    }

    // get the image of the upper slider in the discover screen
    private void callApiSlider() {
        if (isSliderApiCall) {
            return;
        }
        isSliderApiCall=true;
        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showAppSlider, new JSONObject(),Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                isSliderApiCall = false;
                parseSliderData(resp);
            }
        });
    }

    ArrayList<SliderModel> slider_list = new ArrayList<>();

    public void parseSliderData(String resp) {
        try {
            JSONObject jsonObject = new JSONObject(resp);

            String code = jsonObject.optString("code");
            if (code.equals("200")) {

                slider_list.clear();

                JSONArray msg = jsonObject.optJSONArray("msg");
                for (int i = 0; i < msg.length(); i++) {
                    JSONObject object = msg.optJSONObject(i);
                    JSONObject AppSlider = object.optJSONObject("AppSlider");

                    SliderModel sliderModel = new SliderModel();
                    sliderModel.id = AppSlider.optString("id");
                    sliderModel.image = AppSlider.optString("image");
                    sliderModel.url = AppSlider.optString("url");

                    slider_list.add(sliderModel);
                }
                setSliderAdapter();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSliderAdapter() {
        pageIndicatorView.setCount(slider_list.size());
        pageIndicatorView.setSelection(0);

        viewPager.setAdapter(new SlidingAdapter(getActivity(), slider_list, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                String slider_url = slider_list.get(pos).url;
                if (slider_url != null && !slider_url.equals("")) {
                    Intent intent=new Intent(view.getContext(),WebviewA.class);
                    intent.putExtra("url", slider_url);
                    intent.putExtra("title", "Link");
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                }
            }
        }));

        pageIndicatorView.setViewPager(viewPager);
    }

    // Bottom two function will get the Discover videos
    // from api and parse the json data which is shown in Discover tab

    private void callApiForAllVideos() {
        if (isDiscoverAPiCall) {
            return;
        }
        isDiscoverAPiCall = true;
        if (datalist == null)
            datalist = new ArrayList<>();

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("starting_point", "" + pageCount);
            parameters.put("section", this.section);
            parameters.put("country_id", country_id);
            if (Functions.getSharedPreference(view.getContext()).getBoolean(Variables.IS_LOGIN,false)) {
                parameters.put("user_id", Functions.getSharedPreference(view.getContext()).getString(Variables.U_ID,""));
            }
        } catch (Exception e) {
            Log.d(Constants.tag,"Exception : "+e);
        }
        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showDiscoverySections, parameters,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                isDiscoverAPiCall = false;
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                dataContainer.setVisibility(View.VISIBLE);
                parseData(resp);
                swiperefresh.setRefreshing(false);
            }
        });
    }

    public void parseData(String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {

                ArrayList<DiscoverModel> temp_list = new ArrayList<>();

                JSONArray msgArray = jsonObject.getJSONArray("msg");
                for (int d = 0; d < msgArray.length(); d++) {

                    JSONObject discover_object = msgArray.optJSONObject(d);
                    JSONObject entity_obj;
                    String entity_name = "";
                    String entity_img = "";

                    if (section == 0) {
                        entity_obj = discover_object.optJSONObject("Hashtag");
                        entity_name = entity_obj.optString("name");
                        entity_img = entity_obj.optString("image");
                    } else {
                        entity_obj = discover_object.optJSONObject("User");
                        entity_name = entity_obj.optString("username");
                    }

                    DiscoverModel discover_model = new DiscoverModel();
                    discover_model.id = entity_obj.optString("id");
                    discover_model.title = entity_name;
                    discover_model.views = "";
                    discover_model.videos_count = entity_obj.optString("videos_count");
                    discover_model.fav = entity_obj.optString("favourite", "0");

                    JSONArray video_array = entity_obj.optJSONArray("Videos");

                    ArrayList<HomeModel> video_list = new ArrayList<>();
                    for (int i = 0; i < video_array.length(); i++) {
                        JSONObject itemdata = video_array.optJSONObject(i);

                        JSONObject video = itemdata.optJSONObject("Video");

                        JSONObject user;
                        JSONObject sound;
                        JSONObject topic;
                        JSONObject country;

                        if (section == 0) {
                            user = video.optJSONObject("User");
                            sound = video.optJSONObject("Sound");
                            topic = video.optJSONObject("Topic");
                            country = video.optJSONObject("Country");
                        } else {
                            user = itemdata.optJSONObject("User");
                            sound = itemdata.optJSONObject("Sound");
                            topic = itemdata.optJSONObject("Topic");
                            country = itemdata.optJSONObject("Country");
                        }

                        if (section != 0)
                            entity_img = user.optString("profile_pic");

                        JSONObject userPrivacy = user.optJSONObject("PrivacySetting");
                        JSONObject userPushNotification = user.optJSONObject("PushNotification");

                        HomeModel item = Functions.parseVideoData(user, sound, video, topic, country, userPrivacy, userPushNotification);

                        video_list.add(item);
                    }

                    if (video_list.size() >= 5)
                        video_list.add(new HomeModel());

                    discover_model.image = entity_img;
                    discover_model.arrayList = video_list;

                    temp_list.add(discover_model);
                }

                if (pageCount == 0) {
                    datalist.clear();
                    datalist.addAll(temp_list);
                } else {
                    datalist.addAll(temp_list);
                }

                adapter.notifyDataSetChanged();
            } else if (code.equals("201") && pageCount == 0) {
                datalist.clear();
            }

            if (datalist.isEmpty()) {
                view.findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.no_data_layout).setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            loadMoreProgress.setVisibility(View.GONE);
        }
    }

    private void getCountries() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("worldwide", "1");
        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(DiscoverF.this.getActivity(), ApiLinks.showCountries, parameters, Functions.getHeaders(this.getContext()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(DiscoverF.this.getActivity(), resp);
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");
                    if (code.equals("200")) {
                        JSONArray msgArray = jsonObject.getJSONArray("msg");
                        for (int i = 0; i < msgArray.length(); i++) {
                            JSONObject countryObj = msgArray.optJSONObject(i);
                            JSONObject country = countryObj.optJSONObject("Country");
                            countries.add(country);
                            countriesStr.add(country.optString("emoji")+" "+country.optString("name"));
                        }
                    } else {
                        Functions.showToast(context, jsonObject.optString("msg"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // When you click on any Video a new activity is open which will play the Clicked video
    private void openWatchVideo(int position, ArrayList<HomeModel> data_list, String hashtag) {
        if (data_list.size()>5)
            data_list.remove(data_list.size()-1);

        Intent intent = new Intent(view.getContext(), WatchVideosA.class);
        intent.putExtra("arraylist", data_list);
        intent.putExtra("position", position);
        intent.putExtra("pageCount", 0);
        intent.putExtra("hashtag", hashtag);
        intent.putExtra("userId", Functions.getSharedPreference(view.getContext()).getString(Variables.U_ID,""));
        intent.putExtra("whereFrom","tagedVideo");
        startActivity(intent);
    }

    public void openSearch() {
        Intent intent=new Intent(context, SearchMainA.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_layout:
                openSearch();
                break;
            case R.id.search_edit:
                openSearch();
                break;
            case R.id.country_btn:
                countryDialog();
                break;
            default:
                return;

        }
    }

    private void countryDialog() {
        final CharSequence[] options = countriesStr.toArray(new CharSequence[countriesStr.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext(), R.style.AlertDialogCustom);

        builder.setTitle(getString(R.string.country));

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                JSONObject country = countries.get(i);
                country_id = country.optString("id");
                countryBtn.setText(country.optString("emoji"));
                pageCount = 0;
                callApiForAllVideos();
            }
        });

        builder.show();
    }

    private void openEntity(DiscoverModel entity) {
        if (section == 0) {
            Intent intent = new Intent(view.getContext(), TagedVideosA.class);
            intent.putExtra("id", entity.id);
            intent.putExtra("title", entity.title);
            intent.putExtra("image", entity.image);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
        } else {
            Intent intent = new Intent(view.getContext(), ProfileA.class);
            intent.putExtra("user_id", entity.id);
            intent.putExtra("user_name", entity.title);
            intent.putExtra("user_pic", entity.image);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
        }
    }
}
