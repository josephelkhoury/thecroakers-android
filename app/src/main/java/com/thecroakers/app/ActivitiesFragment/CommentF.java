package com.thecroakers.app.ActivitiesFragment;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.hendraanggrian.appcompat.widget.SocialView;
import com.thecroakers.app.ActivitiesFragment.Profile.ProfileA;
import com.thecroakers.app.Adapters.CommentsAdapter;
import com.thecroakers.app.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.APICallBack;
import com.volley.plus.interfaces.Callback;
import com.thecroakers.app.Interfaces.FragmentDataSend;
import com.thecroakers.app.MainMenu.MainMenuFragment;
import com.thecroakers.app.MainMenu.RelateToFragmentOnBack.RootFragment;
import com.thecroakers.app.Models.CommentModel;
import com.thecroakers.app.Models.HomeModel;
import com.thecroakers.app.Models.UserModel;
import com.thecroakers.app.R;
import com.thecroakers.app.SimpleClasses.DataParsing;
import com.thecroakers.app.SimpleClasses.Functions;
import com.thecroakers.app.SimpleClasses.SoftKeyboardStateHelper;
import com.thecroakers.app.SimpleClasses.Variables;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 */
public class CommentF extends RootFragment {

    View view;
    Context context;

    RecyclerView recyclerView;

    CommentsAdapter adapter;

    ArrayList<CommentModel> dataList;
    HomeModel item;
    String videoId;
    String userId;

    EditText messageEdit;
    ImageView sendBtn;
    ProgressBar sendProgress,noDataLoader;

    TextView commentCountTxt, noDataLayout;

    FrameLayout commentScreen;
    String commentId, parentCommentId, replyUserName;
    String replyStatus = null;
    public static int commentCount = 0;

    int pageCount = 0;
    boolean isPostFinish;
    ProgressBar loadMoreProgress;
    LinearLayoutManager linearLayoutManager;

    public CommentF() {
    }

    FragmentDataSend fragmentDataSend;

    RelativeLayout write_layout;

    @SuppressLint("ValidFragment")
    public CommentF(int count, FragmentDataSend fragmentDataSend ) {
        commentCount = count;
        this.fragmentDataSend = fragmentDataSend;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_comment, container, false);
        context = getContext();
        write_layout = view.findViewById(R.id.write_layout);
        commentScreen = view.findViewById(R.id.comment_screen);
        noDataLayout = view.findViewById(R.id.no_data_layout);
        commentScreen.setOnClickListener(v -> {
            getActivity().onBackPressed();
        });

        view.findViewById(R.id.goBack).setOnClickListener(v -> {
            getActivity().onBackPressed();
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            videoId = bundle.getString("video_id");
            userId = bundle.getString("user_id");
            item = (HomeModel) bundle.getSerializable("data");
        }

        SoftKeyboardStateHelper.SoftKeyboardStateListener softKeyboardStateListener = new SoftKeyboardStateHelper.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {
            }

            @Override
            public void onSoftKeyboardClosed() {
                if ((parentCommentId != null && !parentCommentId.equals("")) || replyStatus != null) {
                    messageEdit.setHint(context.getString(R.string.leave_a_comment));
                    replyStatus = null;
                    parentCommentId = null;
                }
            }
        };

        final SoftKeyboardStateHelper softKeyboardStateHelper = new SoftKeyboardStateHelper(context, commentScreen);
        softKeyboardStateHelper.addSoftKeyboardStateListener(softKeyboardStateListener);

        commentCountTxt = view.findViewById(R.id.comment_count);

        loadMoreProgress = view.findViewById(R.id.load_more_progress);
        recyclerView = view.findViewById(R.id.recylerview);
        linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(false);

        dataList = new ArrayList<>();
        adapter = new CommentsAdapter(context, dataList, new CommentsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, CommentModel commentModel, View view) {

                switch (view.getId()) {

                    case R.id.user_pic:
                        openProfile(commentModel);
                        break;

                    case R.id.message_layout:
                        replyUserName = commentModel.user_name;
                        messageEdit.setHint(context.getString(R.string.reply_to)+" " + commentModel.user_name);
                        messageEdit.setText("@" + replyUserName + " ");
                        messageEdit.requestFocus();
                        messageEdit.setSelection(messageEdit.getText().length());
                        replyStatus = "reply";
                        commentId = commentModel.id;
                        Functions.showKeyboard(getActivity());
                        break;

                    case R.id.like_layout:
                        if (Functions.checkLoginUser(getActivity())) {
                            likeComment(position, commentModel);
                        }
                        break;
                }
            }

            @Override
            public void onItemLongClick(int position, CommentModel item, View view) {
                deleteComment(position, item);
            }
        }, new CommentsAdapter.onReplyItemCLickListener() {
            @Override
            public void onItemClick(ArrayList<CommentModel> arrayList, int position, View view) {
                switch (view.getId()) {

                    case R.id.user_pic:
                        CommentModel item = arrayList.get(position);
                        openProfile(item);
                        break;

                    case R.id.reply_layout:
                        replyUserName = arrayList.get(position).user_name;
                        messageEdit.setHint(context.getString(R.string.reply_to)+" " + replyUserName);
                        messageEdit.setText("@" + replyUserName + " ");
                        messageEdit.requestFocus();
                        messageEdit.setSelection(messageEdit.getText().length());
                        replyStatus = "reply";
                        commentId = arrayList.get(position).id;
                        parentCommentId = arrayList.get(position).comment_id;
                        Functions.showKeyboard(getActivity());
                        break;

                    case R.id.like_layout:
                        if (Functions.checkLoginUser(getActivity())) {
                            likeCommentReply(position, arrayList.get(position));
                        }
                        break;
                }
            }

            @Override
            public void onItemLongClick(ArrayList<CommentModel> arrayList, int position, View view) {
                deleteComment(position, arrayList.get(position));
            }
        }, new CommentsAdapter.LinkClickListener() {
            @Override
            public void onLinkClicked(SocialView view, String matchedText) {
                Functions.hideSoftKeyboard(getActivity());
                openProfileByUsername(matchedText);
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

                    if (loadMoreProgress.getVisibility() != View.VISIBLE && !isPostFinish) {
                        loadMoreProgress.setVisibility(View.VISIBLE);
                        pageCount = pageCount + 1;
                        getAllComments();
                    }
                }
            }
        });

        messageEdit = view.findViewById(R.id.message_edit);

        noDataLoader = view.findViewById(R.id.noDataLoader);
        sendProgress = view.findViewById(R.id.send_progress);
        sendBtn = view.findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(v -> {
            String message = messageEdit.getText().toString();
            if (!TextUtils.isEmpty(message)) {
                if (Functions.checkLoginUser(getActivity())) {
                    if (replyStatus == null) {
                        sendComment(videoId, message, "0");
                    } else if (parentCommentId != null && !parentCommentId.equals("")) {
                        //message = "@" + replyUserName + " " + message;
                        sendCommentReply(videoId, message, parentCommentId);
                    } else {
                        //message = "@" + replyUserName + " " + message;
                        sendCommentReply(videoId, message, commentId);
                    }
                    messageEdit.setText(null);
                    sendProgress.setVisibility(View.VISIBLE);
                    sendBtn.setVisibility(View.GONE);
                }
            }
        });

        if (Functions.isShowContentPrivacy(context, item.apply_privacy_model.getVideo_comment(), item.follow_status_button.equalsIgnoreCase("friends"))) {
            sendBtn.setEnabled(true);
        } else
            sendBtn.setEnabled(false);


        if (item.apply_privacy_model.getVideo_comment().equalsIgnoreCase("everyone") ||
                (item.apply_privacy_model.getVideo_comment().equalsIgnoreCase("friend") &&
                        item.follow_status_button.equalsIgnoreCase("friends"))) {
            write_layout.setVisibility(View.VISIBLE);
            getAllComments();
        } else {
            noDataLoader.setVisibility(View.GONE);
            write_layout.setVisibility(View.GONE);
            noDataLayout.setText(view.getContext().getString(R.string.comments_are_turned_off));
            commentCountTxt.setText("0 "+context.getString(R.string.comments));
            noDataLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
        return view;
    }

    private void likeCommentReply(int position, CommentModel item) {
        String action = item.liked;

        if (action != null) {
            if (action.equals("1")) {
                action = "0";
                item.like_count = "" + (Functions.parseInterger(item.like_count) - 1);
            } else {
                action = "1";
                item.like_count = "" + (Functions.parseInterger(item.like_count) + 1);
            }
        }

        for (int i = 0; i < dataList.size(); i++) {
            if (!dataList.isEmpty() && !dataList.get(i).arrayList.isEmpty()) {
                if (item.comment_id.equals(dataList.get(i).id)) {
                    dataList.get(i).arrayList.remove(position);
                    item.liked = action;
                    dataList.get(i).arrayList.add(position, item);
                }
            }
        }

        adapter.notifyDataSetChanged();
        Functions.callApiForLikeComment(getActivity(), item.id, new APICallBack() {
            @Override
            public void arrayData(ArrayList arrayList) {

            }

            @Override
            public void onSuccess(String response) {

            }

            @Override
            public void onFail(String response) {

            }
        });
    }

    private void likeComment(int position, CommentModel item) {
        String action = item.liked;

        if (action != null) {
            if (action.equals("1")) {
                action = "0";
                item.like_count = "" + (Functions.parseInterger(item.like_count) - 1);
            } else {
                action = "1";
                item.like_count = "" + (Functions.parseInterger(item.like_count) + 1);
            }

            dataList.remove(position);
            item.liked = action;
            dataList.add(position, item);
            adapter.notifyDataSetChanged();
        }
        Functions.callApiForLikeComment(getActivity(), item.id, new APICallBack() {
            @Override
            public void arrayData(ArrayList arrayList) {
            }

            @Override
            public void onSuccess(String response) {
            }

            @Override
            public void onFail(String response) {
            }
        });
    }

    @Override
    public void onDetach() {
        Functions.hideSoftKeyboard(getActivity());
        super.onDetach();
    }

    // this function will get all the comments against post
    public void getAllComments() {
        if (dataList == null) {
            dataList = new ArrayList<>();
            noDataLoader.setVisibility(View.VISIBLE);
        }

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("video_id", videoId);
            if (Functions.getSharedPreference(view.getContext()).getBoolean(Variables.IS_LOGIN, false)) {
                parameters.put("user_id", Functions.getSharedPreference(view.getContext()).getString(Variables.U_ID, "0"));
            }
            parameters.put("starting_point", "" + pageCount);
        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showVideoComments, parameters, Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                noDataLoader.setVisibility(View.GONE);

                try {
                    JSONObject response = new JSONObject(resp);
                    String code = response.optString("code");
                    if (code.equals("200")) {
                        if (pageCount == 0) {
                            dataList.clear();
                            adapter.notifyDataSetChanged();
                        }

                        JSONArray msgArray = response.getJSONArray("msg");

                        for (int i = 0; i < msgArray.length(); i++) {
                            JSONObject itemdata = msgArray.optJSONObject(i);

                            JSONObject videoComment = itemdata.optJSONObject("VideoComment");
                            UserModel userDetailModel = DataParsing.getUserDataModel(itemdata.optJSONObject("User"));

                            JSONArray videoCommentReply = itemdata.optJSONArray("VideoCommentReply");

                            ArrayList<CommentModel> replyList = new ArrayList<>();
                            if (videoCommentReply.length() > 0) {
                                for (int j = 0; j < videoCommentReply.length(); j++) {
                                    JSONObject jsonObject = videoCommentReply.getJSONObject(j);

                                    CommentModel comment_model = DataParsing.getCommentDataModel(jsonObject, null);
                                    comment_model.parent_comment_id = videoComment.optString("id");

                                    replyList.add(comment_model);
                                }
                            }

                            CommentModel item = DataParsing.getCommentDataModel(videoComment, userDetailModel);

                            //item.arraylist_size = String.valueOf(videoCommentReply.length());
                            item.arrayList = replyList;

                            dataList.add(item);
                            adapter.notifyItemInserted(dataList.size());
                        }
                    } else if (pageCount == 0) {
                        dataList.clear();
                        adapter.notifyDataSetChanged();
                    }

                    if (dataList.isEmpty()) {
                        noDataLayout.setVisibility(View.VISIBLE);
                    } else {
                        noDataLayout.setVisibility(View.GONE);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    loadMoreProgress.setVisibility(View.GONE);
                }
            }
        });
    }

    // this function will call an api to upload your comment reply
    private void sendCommentReply(String video_id, final String comment, String comment_id) {
        Functions.callApiForSendComment(getActivity(), video_id, comment, comment_id, new APICallBack() {
            @Override
            public void arrayData(ArrayList arrayList) {
                sendProgress.setVisibility(View.GONE);
                sendBtn.setVisibility(View.VISIBLE);
                Functions.hideSoftKeyboard(getActivity());
                messageEdit.setHint(context.getString(R.string.leave_a_comment));

                ArrayList<CommentModel> arrayList1 = arrayList;

                for (int i = 0; i < dataList.size(); i++) {
                    if (dataList.get(i).arrayList == null)
                        dataList.get(i).arrayList = new ArrayList<>();

                    if (parentCommentId != null) {
                        if (parentCommentId.equals(dataList.get(i).id)) {
                            for (CommentModel item : arrayList1) {
                                dataList.get(i).arrayList.add(item);
                                dataList.get(i).item_count_replies = ""+(Integer.valueOf(dataList.get(i).item_count_replies) + 1);
                            }
                        }
                    } else {
                        if (commentId.equals(dataList.get(i).id)) {
                            for (CommentModel item : arrayList1) {
                                dataList.get(i).arrayList.add(item);
                                dataList.get(i).item_count_replies = ""+(Integer.valueOf(dataList.get(i).item_count_replies) + 1);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    adapter.commentsReplyAdapter.notifyDataSetChanged();
                }
                replyStatus = null;
                parentCommentId = null;
            }

            @Override
            public void onSuccess(String response) {
                // this will return a string response
            }

            @Override
            public void onFail(String response) {
                // this will return the failed response
            }
        });
    }

    // this function will call an api to upload your comment
    public void sendComment(String video_id, final String comment, String comment_id) {

        Functions.callApiForSendComment(getActivity(), video_id, comment, comment_id, new APICallBack() {
            @Override
            public void arrayData(ArrayList arrayList) {
                sendProgress.setVisibility(View.GONE);
                sendBtn.setVisibility(View.VISIBLE);
                noDataLayout.setVisibility(View.GONE);
                Functions.hideSoftKeyboard(getActivity());
                ArrayList<CommentModel> arrayList1 = arrayList;
                for (CommentModel item : arrayList1) {
                    dataList.add(0, item);
                    commentCount++;
                    commentCountTxt.setText(commentCount + " "+context.getString(R.string.comments));

                    if (fragmentDataSend != null)
                        fragmentDataSend.onDataSent("" + commentCount);

                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onSuccess(String response) {
                // this will return a string response
            }

            @Override
            public void onFail(String response) {
                // this will return the failed response
            }
        });
    }

    // get the profile data by sending the username instead of id
    private void openProfileByUsername(String username) {
        Intent intent = new Intent(view.getContext(), ProfileA.class);
        intent.putExtra("user_name", username);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    // this will open the profile of user which have uploaded the currently running video
    private void openProfile(CommentModel item) {
        Intent intent = new Intent(view.getContext(), ProfileA.class);
        intent.putExtra("user_id", item.user_id);
        intent.putExtra("user_name", item.user_name);
        intent.putExtra("user_pic", item.profile_pic);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
    }

    public void deleteComment(int position, CommentModel commentModel) {
        if (Functions.getSharedPreference(context).getString(Variables.U_ID, "0").equals(commentModel.user_id) || Functions.getSharedPreference(context).getString(Variables.U_ID, "0").equals(item.user_id)) {
            new AlertDialog.Builder(this.getContext())
                    .setTitle("Delete Comment?")
                    .setMessage("Are you sure you want to delete the comment?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            Functions.callApiForDeleteComment(getActivity(), commentModel.id, new APICallBack() {
                                @Override
                                public void arrayData(ArrayList arrayList) {
                                }

                                @Override
                                public void onSuccess(String response) {
                                    try {
                                        if (dataList.contains(commentModel))
                                            dataList.remove(commentModel);
                                        else {
                                            for (int i = 0; i < dataList.size(); i++) {
                                                if (!dataList.isEmpty() && !dataList.get(i).arrayList.isEmpty()) {
                                                    if (commentModel.id.equals(dataList.get(i).arrayList.get(position).id)) {
                                                        dataList.get(i).arrayList.remove(position);
                                                        dataList.get(i).item_count_replies = ""+(Integer.valueOf(dataList.get(i).item_count_replies) - 1);
                                                    }
                                                }
                                            }
                                        }
                                        adapter.notifyDataSetChanged();
                                        adapter.commentsReplyAdapter.notifyDataSetChanged();
                                    } catch(Exception e) {

                                    }
                                }

                                @Override
                                public void onFail(String response) {
                                }
                            });
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        }
    }
}
