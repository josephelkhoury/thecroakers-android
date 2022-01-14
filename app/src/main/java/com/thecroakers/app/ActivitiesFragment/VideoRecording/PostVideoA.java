package com.thecroakers.app.ActivitiesFragment.VideoRecording;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.hendraanggrian.appcompat.widget.SocialEditText;
import com.thecroakers.app.ActivitiesFragment.Profile.ProfileA;
import com.thecroakers.app.Constants;
import com.thecroakers.app.MainMenu.MainMenuActivity;
import com.thecroakers.app.Models.FollowingModel;
import com.thecroakers.app.Models.PrivacyPolicySettingModel;
import com.thecroakers.app.Models.PushNotificationSettingModel;
import com.thecroakers.app.Models.UserModel;
import com.thecroakers.app.R;
import com.thecroakers.app.Models.HashTagModel;
import com.thecroakers.app.Adapters.HashTagAdapter;
import com.thecroakers.app.Models.UsersModel;
import com.thecroakers.app.Services.UploadService;
import com.thecroakers.app.Interfaces.AdapterClickListener;
import com.thecroakers.app.ApiClasses.ApiLinks;
import com.thecroakers.app.SimpleClasses.DataParsing;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.thecroakers.app.SimpleClasses.Functions;
import com.thecroakers.app.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class PostVideoA extends AppCompatActivity implements View.OnClickListener {

    Context context;
    String userId;
    ImageView videoThumbnail;
    String videoPath;

    SocialEditText descriptionEdit;

    String draftFile, duetVideoId, duetVideoUsername, duetOrientation;

    String topic_id = "0";
    String country_id = "0";
    String privacyType = "Public";
    TextView topicTxt, countryTxt, privacyTypeTxt, duetUsername, additionalDetailsTextCount;
    Switch commentSwitch, duetSwitch;

    Bitmap bmThumbnail;

    int counter = -1;
    RelativeLayout duetLayoutUsername;
    ArrayList<JSONObject> topics = new ArrayList<>();
    ArrayList<String> topicsStr = new ArrayList<String>();
    ArrayList<JSONObject> countries = new ArrayList<>();
    ArrayList<String> countriesStr = new ArrayList<String>();
    ArrayList<UsersModel> taggedUsers = new ArrayList<>();

    String main_video_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(PostVideoA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, PostVideoA.class,false);
        setContentView(R.layout.activity_post_video);
        context = PostVideoA.this;
        if (getIntent() == null) {
            userId = Functions.getSharedPreference(context).getString(Variables.U_ID, "0");
        }
        duetUsername = findViewById(R.id.duet_username);
        duetLayoutUsername = findViewById(R.id.duet_layout_username);

        Intent intent = getIntent();
        if (intent != null) {
            draftFile = intent.getStringExtra("draft_file");
            duetVideoId = intent.getStringExtra("duet_video_id");
            duetOrientation = intent.getStringExtra("duet_orientation");
            duetVideoUsername = intent.getStringExtra("duet_video_username");
            if (duetVideoUsername != null && !duetVideoUsername.equals("")) {
                duetLayoutUsername.setVisibility(View.VISIBLE);
                duetUsername.setText(duetVideoUsername);
            }
            main_video_id = intent.getStringExtra("main_video_id");
        }

        getTopics();
        getCountries();

        videoPath = Functions.getAppFolder(this)+Variables.output_filter_file;
        videoThumbnail = findViewById(R.id.video_thumbnail);

        descriptionEdit = findViewById(R.id.description_edit);
        additionalDetailsTextCount = findViewById(R.id.aditional_details_text_count);

        // this will get the thumbnail of video and show them in imageview

        bmThumbnail = ThumbnailUtils.createVideoThumbnail(videoPath,
                MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);

        if (bmThumbnail != null && duetVideoId != null) {
            Bitmap duet_video_bitmap = null;
            if (duetVideoId != null) {
                duet_video_bitmap = ThumbnailUtils.createVideoThumbnail(Functions.getAppFolder(this) + duetVideoId + ".mp4",
                        MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
            }
            Bitmap combined = combineImages(bmThumbnail, duet_video_bitmap);
            Bitmap bitmap = Bitmap.createScaledBitmap(combined, (combined.getWidth() / 6), (combined.getHeight() / 6), false);

            bmThumbnail.recycle();
            combined.recycle();

            videoThumbnail.setImageBitmap(bitmap);
            Functions.getSharedPreference(this).edit().putString(Variables.UPLOADING_VIDEO_THUMB, Functions.bitmapToBase64(bitmap)).commit();


        } else if (bmThumbnail != null) {
            Bitmap bitmap = Bitmap.createScaledBitmap(bmThumbnail, (bmThumbnail.getWidth() / 6), (bmThumbnail.getHeight() / 6), false);

            bmThumbnail.recycle();

            videoThumbnail.setImageBitmap(bitmap);
            Functions.getSharedPreference(this).edit().putString(Variables.UPLOADING_VIDEO_THUMB, Functions.bitmapToBase64(bitmap)).commit();

        }

        topicTxt = findViewById(R.id.topic_txt);
        countryTxt = findViewById(R.id.country_txt);
        privacyTypeTxt = findViewById(R.id.privcy_type_txt);
        commentSwitch = findViewById(R.id.comment_switch);
        duetSwitch = findViewById(R.id.duet_switch);

        setAdapterForHashtag();
        findViewById(R.id.goBack).setOnClickListener(this);

        findViewById(R.id.topic_layout).setOnClickListener(this);
        findViewById(R.id.country_layout).setOnClickListener(this);
        findViewById(R.id.privacy_type_layout).setOnClickListener(this);
        findViewById(R.id.post_btn).setOnClickListener(this);
        findViewById(R.id.save_draft_btn).setOnClickListener(this);

        findViewById(R.id.hashtag_btn).setOnClickListener(this);
        findViewById(R.id.tag_user_btn).setOnClickListener(this);

        if (duetVideoId != null) {
            findViewById(R.id.duet_layout).setVisibility(View.GONE);
            findViewById(R.id.save_draft_btn).setVisibility(View.GONE);
            duetSwitch.setChecked(false);
        }
        else if (Functions.getSharedPreference(this).getBoolean(Variables.IsExtended,false)) {
            findViewById(R.id.duet_layout).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.duet_layout).setVisibility(View.GONE);
            duetSwitch.setChecked(false);
        }

        additionalDetailsTextCount.setText("0" + "/" + Constants.VIDEO_DESCRIPTION_CHAR_LIMIT);

        descriptionEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constants.VIDEO_DESCRIPTION_CHAR_LIMIT)});

        descriptionEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {

                if (descriptionEdit.length() > counter) {
                    counter = descriptionEdit.length();
                    if (descriptionEdit.length() > 0) {
                        String lastChar = charSequence.toString().substring(charSequence.length() - 1);
                        if (lastChar.equals(" ")) {
                            findViewById(R.id.hashtag_layout).setVisibility(View.GONE);
                        } else if (lastChar.equals("#")) {
                            findViewById(R.id.hashtag_layout).setVisibility(View.VISIBLE);
                        } else if (lastChar.equals("@")) {
                            openFriends();
                        }

                        String hash_tags = descriptionEdit.getText().toString();
                        String[] separated = hash_tags.split("#");
                        for (String item : separated) {
                            if (item != null && !item.equals("")) {
                                if (item.contains(" ")) {
                                    //stop calling api
                                } else {
                                    String string1 = item.replace("#", "");
                                    pageCount=0;
                                    callApiForHashTag(string1);
                                }
                            }
                        }
                    } else {
                        findViewById(R.id.hashtag_layout).setVisibility(View.GONE);
                    }
                } else {
                    if (descriptionEdit.length() == 1) {
                        counter = -1;
                    } else {
                        counter--;
                    }
                }

                additionalDetailsTextCount.setText(descriptionEdit.getText().length() + "/" + Constants.VIDEO_DESCRIPTION_CHAR_LIMIT);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    public Bitmap combineImages(Bitmap c, Bitmap s) { // can add a 3rd parameter 'String loc' if you want to save the new image - left some code to do that at the bottom
        Bitmap cs = null;

        int width, height = 0;

        if (c.getWidth() > s.getWidth()) {
            width = c.getWidth() + s.getWidth();
            height = c.getHeight();
        } else {
            width = s.getWidth() + s.getWidth();
            height = c.getHeight();
        }

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, c.getWidth(), 0f, null);

        return cs;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.goBack:
                onBackPressed();
                break;

            case R.id.topic_layout:
                topicDialog();
                break;

            case R.id.country_layout:
                countryDialog();
                break;

            case R.id.privacy_type_layout:
                privacyDialog();
                break;

            case R.id.save_draft_btn:
                saveFileInDraft();
                break;

            case R.id.post_btn:
                if (topic_id == "0") {
                    Functions.showToast(context, getString(R.string.please_choose_a_topic));
                    break;
                }

                if (country_id == "0") {
                    Functions.showToast(context, getString(R.string.please_choose_a_country));
                    break;
                }
                makeMentionArrays();
                startService();
                break;

            case R.id.hashtag_btn:
                findViewById(R.id.hashtag_layout).setVisibility(View.VISIBLE);
                descriptionEdit.setText(descriptionEdit.getText().toString() + " #");
                descriptionEdit.setSelection(descriptionEdit.getText().length());
                pageCount=0;
                callApiForHashTag("");
                break;

            case R.id.tag_user_btn:
                openFriends();
                break;
        }
    }

    ArrayList<HashTagModel> hashList = new ArrayList<>();
    RecyclerView recyclerView;
    int pageCount = 0;
    boolean ispostFinish;
    ProgressBar loadMoreProgress;
    LinearLayoutManager linearLayoutManager;
    HashTagAdapter hashtag_adapter;

    // call api for get all the hash tag that can be selected before post the video
    void callApiForHashTag(String lastChar) {
        JSONObject params = new JSONObject();
        try {
            params.put("type", "hashtag");
            params.put("keyword", lastChar.toString());
            params.put("starting_point", ""+pageCount);
        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(this, ApiLinks.search, params, Functions.getHeaders(this),new Callback() {
                    @Override
                    public void onResponce(String resp) {
                Functions.checkStatus(PostVideoA.this,resp);
                        Functions.cancelLoader();
                        try {
                            JSONObject response = new JSONObject(resp);
                            String code = response.optString("code");
                            if (code.equalsIgnoreCase("200")) {
                                JSONArray msgArray = response.optJSONArray("msg");
                                ArrayList<HashTagModel> temp_list = new ArrayList<>();

                                for (int i = 0; i < msgArray.length(); i++) {
                                    JSONObject itemdata = msgArray.optJSONObject(i);
                                    JSONObject Hashtag = itemdata.optJSONObject("Hashtag");

                                    HashTagModel item = new HashTagModel();
                                    item.id = Hashtag.optString("id");
                                    item.name = Hashtag.optString("name");
                                    item.videos_count = Hashtag.optString("videos_count");
                                    temp_list.add(item);
                                }

                                if (pageCount == 0) {
                                    hashList.clear();
                                    hashList.addAll(temp_list);
                                } else {
                                    hashList.addAll(temp_list);
                                }

                                hashtag_adapter.notifyDataSetChanged();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            loadMoreProgress.setVisibility(View.GONE);
                        }
                    }
                });
    }

    // set the hashtag adapter to recycler view
    private void setAdapterForHashtag() {
        loadMoreProgress = findViewById(R.id.load_more_progress);
        recyclerView = findViewById(R.id.hashtag_recylerview);
        linearLayoutManager = new LinearLayoutManager(PostVideoA.this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        hashtag_adapter = new HashTagAdapter(PostVideoA.this, hashList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                HashTagModel item = (HashTagModel) object;

                findViewById(R.id.hashtag_layout).setVisibility(View.GONE);
                StringBuilder sb = new StringBuilder();
                String desc = descriptionEdit.getText().toString();
                String[] bits = desc.split(" ");
                String lastOne = bits[bits.length - 1];
                String newString = lastOne.replace(lastOne, item.name);
                for (int i = 0; i < bits.length - 1; i++) {
                    sb.append(bits[i] + " ");
                }
                sb.append("#" + newString + " ");
                descriptionEdit.setText(sb);
                descriptionEdit.setSelection(descriptionEdit.getText().length());
            }
        });
        recyclerView.setAdapter(hashtag_adapter);
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
                if (userScrolled && (scrollOutitems == hashList.size() - 1)) {
                    userScrolled = false;

                    if (loadMoreProgress.getVisibility() != View.VISIBLE && !ispostFinish) {
                        loadMoreProgress.setVisibility(View.VISIBLE);
                        pageCount = pageCount + 1;
                        callApiForHashTag("");
                    }
                }
            }
        });
    }

    // open the follower list of the profile for mention them during post video
    public void openFriends() {
        Intent intent=new Intent(PostVideoA.this,FriendsA.class);
        intent.putExtra("id", Functions.getSharedPreference(this).getString(Variables.U_ID, ""));
        resultFriendsCallback.launch(intent);
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
    }

    ActivityResultLauncher<Intent> resultFriendsCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data.getBooleanExtra("isShow",false)) {
                            UsersModel item = (UsersModel) data.getSerializableExtra("data");
                            taggedUsers.add(item);

                            String lastChar = null;
                            if (!TextUtils.isEmpty(descriptionEdit.getText().toString()))
                                lastChar = descriptionEdit.getText().toString().substring(descriptionEdit.getText().length() - 1);

                            if (lastChar != null && lastChar.contains("@"))
                                descriptionEdit.setText(descriptionEdit.getText().toString() + item.username + " ");
                            else
                                descriptionEdit.setText(descriptionEdit.getText().toString() + "@" + item.username + " ");

                            descriptionEdit.setSelection(descriptionEdit.getText().length());
                        }
                    }
                }
            });

    private void getTopics() {
        JSONObject parameters = new JSONObject();

        VolleyRequest.JsonPostRequest(PostVideoA.this, ApiLinks.showFeaturedHashtags, parameters, Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(PostVideoA.this,resp);
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");
                    if (code.equals("200")) {
                        JSONArray msgArray = jsonObject.getJSONArray("msg");
                        for (int i = 0; i < msgArray.length(); i++) {
                            JSONObject hashtagObj = msgArray.optJSONObject(i);
                            JSONObject hashtag = hashtagObj.optJSONObject("Hashtag");
                            topics.add(hashtag);
                            topicsStr.add(hashtag.optString("name"));
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

    private void getCountries() {
        JSONObject parameters = new JSONObject();

        VolleyRequest.JsonPostRequest(PostVideoA.this, ApiLinks.showCountries, parameters, Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(PostVideoA.this,resp);
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

    private void topicDialog() {
        final CharSequence[] options = topicsStr.toArray(new CharSequence[topicsStr.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);

        builder.setTitle(null);

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                topicTxt.setText(options[i]);
                JSONObject topic = topics.get(i);
                topic_id = topic.optString("id");
            }
        });

        builder.show();
    }

    private void countryDialog() {
        final CharSequence[] options = countriesStr.toArray(new CharSequence[countriesStr.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);

        builder.setTitle(null);

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                countryTxt.setText(options[i]);
                JSONObject country = countries.get(i);
                country_id = country.optString("id");
            }
        });

        builder.show();
    }

    // show the option that is you want to make video public or private
    private void privacyDialog() {
        final CharSequence[] options = new CharSequence[]{getString(R.string.public_), getString(R.string.private_)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);

        builder.setTitle(null);

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {
                privacyTypeTxt.setText(options[item]);
                if (item == 0) {
                    privacyType = "Public";
                } else {
                    privacyType = "Private";
                }
            }
        });

        builder.show();
    }

    JSONArray hashTag, friendsTag;

    public void makeMentionArrays() {
        hashTag = new JSONArray();
        friendsTag = new JSONArray();

        String[] separated = descriptionEdit.getText().toString().split(" ");
        for (String item : separated) {
            if (item != null && !item.equals("")) {
                if (item.contains("#")) {
                    String string1 = item.replace("#", "");
                    JSONObject tag_object = new JSONObject();
                    try {
                        tag_object.put("name", string1);
                        hashTag.put(tag_object);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (item.contains("@")) {
                    String string1 = item.replace("@", "");
                    JSONObject tag_object = new JSONObject();
                    try {
                        for (UsersModel user_model : taggedUsers) {
                            if (user_model.username.contains(string1)) {
                                tag_object.put("user_id", user_model.fb_id);
                                friendsTag.put(tag_object);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        Functions.printLog(Constants.tag, hashTag.toString());
        Functions.printLog(Constants.tag, friendsTag.toString());
    }

    // this will start the service for uploading the video into database
    public void startService() {

        UploadService mService = new UploadService();
        if (!Functions.isMyServiceRunning(this, mService.getClass())) {
            Intent mServiceIntent = new Intent(this.getApplicationContext(), mService.getClass());
            mServiceIntent.setAction("startservice");
            mServiceIntent.putExtra("draft_file", draftFile);
            mServiceIntent.putExtra("duet_video_id", duetVideoId);
            mServiceIntent.putExtra("uri", "" + videoPath);
            mServiceIntent.putExtra("desc", "" + descriptionEdit.getText().toString());
            mServiceIntent.putExtra("privacy_type", privacyType);
            mServiceIntent.putExtra("hashtags_json", hashTag.toString());
            mServiceIntent.putExtra("mention_users_json", friendsTag.toString());
            mServiceIntent.putExtra("duet_orientation", duetOrientation);
            mServiceIntent.putExtra("main_video_id", main_video_id);

            if (commentSwitch.isChecked())
                mServiceIntent.putExtra("allow_comment", "true");
            else
                mServiceIntent.putExtra("allow_comment", "false");

            if (duetSwitch.isChecked())
                mServiceIntent.putExtra("allow_duet", "1");
            else
                mServiceIntent.putExtra("allow_duet", "0");

            startService(mServiceIntent);

           PostVideoA.this.runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   sendBroadcast(new Intent("uploadVideo"));
                   startActivity(new Intent(PostVideoA.this, MainMenuActivity.class));
               }
           });
        } else {
            Toast.makeText(PostVideoA.this, getString(R.string.please_wait_video_uploading_is_already_in_progress), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {

        int count = this.getSupportFragmentManager().getBackStackEntryCount();
        if (count > 0) {
            super.onBackPressed();
        } else {
            super.onBackPressed();
            finish();
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        }
    }

    @Override
    protected void onDestroy() {
        if (bmThumbnail != null) {
            bmThumbnail.recycle();
        }
        super.onDestroy();
    }

    // save the file into the draft
    public void saveFileInDraft() {
        File source = new File(videoPath);
        File destination = new File(Functions.getAppFolder(this)+Variables.DRAFT_APP_FOLDER + Functions.getRandomString() + ".mp4");
        try {
            if (source.exists()) {

                InputStream in = new FileInputStream(source);
                OutputStream out = new FileOutputStream(destination);

                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();

                Toast.makeText(PostVideoA.this, getString(R.string.file_save_in_draft), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(PostVideoA.this, MainMenuActivity.class));

            } else {
                Toast.makeText(PostVideoA.this, getString(R.string.file_save_in_draft), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
