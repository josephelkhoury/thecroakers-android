package com.thecroakers.app.ActivitiesFragment.Search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.thecroakers.app.ActivitiesFragment.TagedVideosA;
import com.thecroakers.app.Adapters.HashTagFavouriteAdapter;
import com.thecroakers.app.MainMenu.RelateToFragmentOnBack.RootFragment;
import com.thecroakers.app.Models.HashTagModel;
import com.thecroakers.app.R;
import com.thecroakers.app.Interfaces.AdapterClickListener;
import com.thecroakers.app.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.thecroakers.app.SimpleClasses.Functions;
import com.thecroakers.app.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;



// search the hash tag
public class SearchHashTagsF extends RootFragment {

    View view;
    Context context;
    String type;
    ShimmerFrameLayout shimmerFrameLayout;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    ProgressBar loadMoreProgress;
    int pageCount = 0;
    boolean ispostFinish;
    ArrayList<HashTagModel> dataList;
    HashTagFavouriteAdapter adapter;
    SwipeRefreshLayout refreshLayout;

    public SearchHashTagsF(String type) {
        this.type = type;
    }

    public SearchHashTagsF() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_search, container, false);
        context = getContext();

        shimmerFrameLayout = view.findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();
        refreshLayout=view.findViewById(R.id.refreshLayout);
        recyclerView = view.findViewById(R.id.recylerview);
        linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        dataList = new ArrayList<>();
        adapter = new HashTagFavouriteAdapter(context, dataList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {

                switch (view.getId()) {
                    default:
                        HashTagModel item = (HashTagModel) object;
                        openHashtag(item);
                        break;
                }

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

                            if (type != null && type.equalsIgnoreCase("favourite")) {
                                callApiGetFavourite();
                            } else
                                callApiSearch();
                    }
                }
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(false);
                pageCount=0;
                if (type != null && type.equalsIgnoreCase("favourite")) {
                    callApiGetFavourite();
                } else
                    callApiSearch();
            }
        });


        loadMoreProgress = view.findViewById(R.id.load_more_progress);
        pageCount = 0;

        if (type != null && type.equalsIgnoreCase("favourite")) {
            callApiGetFavourite();
        } else
            callApiSearch();

        return view;
    }

    // get the hashtage that a user search for
    public void callApiSearch() {

        JSONObject params = new JSONObject();
        try {
            if (Functions.getSharedPreference(context).getString(Variables.U_ID, null) != null) {
                params.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, "0"));
            }

            params.put("type", type);
            params.put("keyword", SearchMainA.searchEdit.getText().toString());
            params.put("starting_point", "" + pageCount);

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.search, params,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);

                parseData(resp);
            }
        });

    }


    // get the hash tag that a user is favourite it
    public void callApiGetFavourite() {

        JSONObject params = new JSONObject();
        try {
            params.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, "0"));
            params.put("starting_point", "" + pageCount);

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showFavouriteHashtags, params,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);

                parseData(resp);
            }
        });

    }

    // parse the data of hashtag list
    public void parseData(String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {

                JSONArray msgArray = jsonObject.getJSONArray("msg");
                ArrayList<HashTagModel> temp_list = new ArrayList<>();
                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);

                    JSONObject hashtag = itemdata.optJSONObject("Hashtag");

                    HashTagModel item = new HashTagModel();

                    item.id = hashtag.optString("id");
                    item.name = hashtag.optString("name");
                    item.image = hashtag.optString("image");
                    item.views = hashtag.optString("views");
                    item.videos_count = hashtag.optString("videos_count");
                    item.fav = hashtag.optString("favourite", "1");
                    temp_list.add(item);
                }

                if (pageCount == 0) {
                    dataList.clear();
                }

                dataList.addAll(temp_list);
                adapter.notifyDataSetChanged();

                if (dataList.isEmpty()) {
                    view.findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);
                } else {
                    view.findViewById(R.id.no_data_layout).setVisibility(View.GONE);
                }

            } else {
                if (dataList.isEmpty())
                    view.findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {

            e.printStackTrace();
        } finally {
            loadMoreProgress.setVisibility(View.GONE);
        }
    }

    // open the video list against the hashtags
    private void openHashtag(HashTagModel model) {

        Intent intent = new Intent(view.getContext(), TagedVideosA.class);
        intent.putExtra("id", model.id);
        intent.putExtra("title", model.name);
        intent.putExtra("image", model.image);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);

    }


}
