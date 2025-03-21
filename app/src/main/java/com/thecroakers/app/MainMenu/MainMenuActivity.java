package com.thecroakers.app.MainMenu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.thecroakers.app.ActivitiesFragment.Chat.ChatA;
import com.thecroakers.app.ActivitiesFragment.DiscoverF;
import com.thecroakers.app.ActivitiesFragment.Profile.ProfileA;
import com.thecroakers.app.ActivitiesFragment.Profile.Setting.NoInternetA;
import com.thecroakers.app.ActivitiesFragment.WatchVideosA;
import com.thecroakers.app.Constants;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.thecroakers.app.Interfaces.InternetCheckCallback;
import com.thecroakers.app.Models.InviteFriendModel;
import com.thecroakers.app.Models.UserModel;
import com.thecroakers.app.R;
import com.thecroakers.app.ApiClasses.ApiLinks;
import com.thecroakers.app.SimpleClasses.DataParsing;
import com.thecroakers.app.SimpleClasses.Functions;
import com.thecroakers.app.SimpleClasses.PermissionUtils;
import com.thecroakers.app.SimpleClasses.Variables;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainMenuActivity extends AppCompatActivity {
    public static MainMenuActivity mainMenuActivity;
    private MainMenuFragment mainMenuFragment;
    long mBackPressed;
    PermissionUtils takePermissionUtils;
    Context context;
    public static Intent intent;

    ArrayList<InviteFriendModel> datalist = new ArrayList<>();
    CountryCodePicker ccp;
    EditText checkNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try { getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); }catch (Exception e){}
        Functions.setLocale(Functions.getSharedPreference(MainMenuActivity.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, MainMenuActivity.class,false);
        setContentView(R.layout.activity_main_menu);
        context = MainMenuActivity.this;
        mainMenuActivity = this;

        ccp = new CountryCodePicker(MainMenuActivity.this);
        checkNumber = new EditText(MainMenuActivity.this);
        intent = getIntent();
        checkDeepLink(intent);

        if (Functions.getSharedPreference(this).getBoolean(Variables.IS_LOGIN, false)) {
            getPublicIP();
        }

        if (!Functions.getSharedPreference(this).getBoolean(Variables.IsExtended,false))
            checkLicence();

        if (savedInstanceState == null) {
            initScreen();
        } else {
            Functions.printLog(Constants.tag, "savedInstanceState : null "+getSupportFragmentManager().getFragments().get(0));
            mainMenuFragment = (MainMenuFragment) getSupportFragmentManager().getFragments().get(0);
        }

        Functions.makeDirectry(Functions.getAppFolder(this)+Variables.APP_HIDED_FOLDER);
        Functions.makeDirectry(Functions.getAppFolder(this)+Variables.DRAFT_APP_FOLDER);

        setIntent(null);
    }

    private void checkDeepLink(Intent intent) {
        try {
            Uri uri = intent.getData();
            String linkUri = ""+uri;
            String link = linkUri.replaceAll(Constants.BASE_URL, "");

            if (link.equals(""))
                return;

            JSONObject parameters = new JSONObject();
            try {
                parameters.put("link", link);
            } catch (Exception e) {
                e.printStackTrace();
            }

            VolleyRequest.JsonPostRequest(MainMenuActivity.this, ApiLinks.showShareLink, parameters, Functions.getHeaders(this), new Callback() {
                @Override
                public void onResponce(String resp) {
                    Functions.checkStatus(MainMenuActivity.this, resp);
                    try {
                        JSONObject jsonObject = new JSONObject(resp);
                        String code = jsonObject.optString("code");
                        if (code.equals("200")) {
                            JSONObject msg = jsonObject.getJSONObject("msg");
                            JSONObject shareLink = msg.getJSONObject("ShareLink");

                            String type = shareLink.optString("type");
                            String entity_id = shareLink.optString("entity_id");

                            if (type.equals("user")) {
                                OpenProfileScreen(entity_id);
                            } else if (type.equals("video")) {
                                openWatchVideo(entity_id);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            /*String profileURL = Variables.http+"://"+getString(R.string.share_profile_domain_second)+getString(R.string.share_profile_endpoint_second);
            if (linkUri.contains(profileURL)) {
                String[] parts = linkUri.split(profileURL);
                userId = parts[1];

                OpenProfileScreen(userId);
            } else if (linkUri.contains(Constants.BASE_URL)) {
                String[] parts = linkUri.split(Constants.BASE_URL);
                videoId = parts[1].substring(4, (parts[1].length()-3));
                openWatchVideo(videoId);
            }*/
        }
        catch (Exception e) {
            Log.d(Constants.tag,"Exception Link : "+e);
        }
    }

    private void openWatchVideo(String videoId) {
        Intent intent = new Intent(MainMenuActivity.this, WatchVideosA.class);
        intent.putExtra("video_id", videoId);
        intent.putExtra("position", 0);
        intent.putExtra("pageCount", 0);
        intent.putExtra("userId",Functions.getSharedPreference(MainMenuActivity.this).getString(Variables.U_ID,""));
        intent.putExtra("whereFrom","IdVideo");
        startActivity(intent);
    }

    private void OpenProfileScreen(String userId) {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", userId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(MainMenuActivity.this, ApiLinks.showUserDetail, parameters,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(MainMenuActivity.this,resp);
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");
                    if (code.equals("200")) {
                        JSONObject msg = jsonObject.optJSONObject("msg");

                        UserModel userDetailModel= DataParsing.getUserDataModel(msg.optJSONObject("User"));

                        moveToProfile(userDetailModel.getId()
                                ,userDetailModel.getUsername()
                                ,userDetailModel.getProfilePic());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void moveToProfile(String id,String username,String pic) {
        Intent intent = new Intent(MainMenuActivity.this, ProfileA.class);
        intent.putExtra("user_id", id);
        intent.putExtra("user_name", username);
        intent.putExtra("user_pic", pic);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        checkDeepLink(intent);
        if (intent != null) {
            String type = intent.getStringExtra("type");
            if (type != null && type.equalsIgnoreCase("message")) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainMenuActivity.this,ChatA.class);
                        intent.putExtra("user_id", intent.getStringExtra("user_id"));
                        intent.putExtra("user_name", intent.getStringExtra("user_name"));
                        intent.putExtra("user_pic", intent.getStringExtra("user_pic"));
                        resultChatCallback.launch(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }
                }, 2000);
            }
        }
    }

    ActivityResultLauncher<Intent> resultChatCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data.getBooleanExtra("isShow",false)) {

                        }
                    }
                }
            }
    );

    private void initScreen() {
        mainMenuFragment = new MainMenuFragment();
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, mainMenuFragment)
                .commit();

        findViewById(R.id.container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    public void getPublicIP() {
        VolleyRequest.JsonGetRequest(this, "https://api.ipify.org/?format=json", new Callback() {
            @Override
            public void onResponce(String s) {
                try {
                    JSONObject response = new JSONObject(s);
                    String ip = response.optString("ip");
                    Functions.getSharedPreference(MainMenuActivity.this).edit().putString(Variables.DEVICE_IP, ip).commit();

                    if (Functions.getSharedPreference(MainMenuActivity.this).getString(Variables.DEVICE_TOKEN,"").equalsIgnoreCase("")) {
                        addFirebaseToken();
                    }
                } catch (Exception e) {
                    Log.d(Constants.tag,"Exception : "+e);
                }
            }
        });
     }

    public void addFirebaseToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        // Get new FCM registration token
                        String token = task.getResult();
                        Functions.getSharedPreference(MainMenuActivity.this).edit().putString(Variables.DEVICE_TOKEN, token).commit();
                    }
                });
    }

    public void checkLicence() {

        VolleyRequest.JsonPostRequest(MainMenuActivity.this, ApiLinks.showLicense, null,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(MainMenuActivity.this,resp);
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");
                    if (code != null && code.equals("200")){
                        Functions.getSharedPreference(MainMenuActivity.this).edit().putBoolean(Variables.IsExtended,true).commit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!mainMenuFragment.onBackPressed()) {
            int count = this.getSupportFragmentManager().getBackStackEntryCount();
            if (count == 0) {
                if (mBackPressed + 2000 > System.currentTimeMillis()) {
                    super.onBackPressed();
                    return;
                } else {
                    Functions.showToast(getBaseContext(), getString(R.string.tap_to_exist));
                    mBackPressed = System.currentTimeMillis();
                }
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Functions.deleteCache(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Functions.RegisterConnectivity(this, new InternetCheckCallback() {
            @Override
            public void GetResponse(String requestType, String response) {
                if (response.equalsIgnoreCase("disconnected")) {
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
