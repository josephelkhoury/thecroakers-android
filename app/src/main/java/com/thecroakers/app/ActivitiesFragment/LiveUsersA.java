package com.thecroakers.app.ActivitiesFragment;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thecroakers.app.ActivitiesFragment.LiveStreaming.LiveUserAdapter;
import com.thecroakers.app.ActivitiesFragment.LiveStreaming.LiveUserModel;
import com.thecroakers.app.ActivitiesFragment.LiveStreaming.activities.LiveActivity;
import com.thecroakers.app.ActivitiesFragment.Profile.Setting.NoInternetA;
import com.thecroakers.app.Interfaces.AdapterClickListener;
import com.thecroakers.app.Interfaces.InternetCheckCallback;
import com.thecroakers.app.R;
import com.thecroakers.app.SimpleClasses.Functions;
import com.thecroakers.app.SimpleClasses.PermissionUtils;
import com.thecroakers.app.SimpleClasses.TheCroakers;
import com.thecroakers.app.SimpleClasses.Variables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.agora.rtc.Constants;

public class LiveUsersA extends AppCompatActivity implements View.OnClickListener {

    Context context;
    ArrayList<LiveUserModel> dataList = new ArrayList<>();
    RecyclerView recyclerView;
    LiveUserAdapter adapter;
    ImageView btnBack;
    DatabaseReference rootref;
    TextView noDataFound;

    PermissionUtils takePermissionUtils;
    LiveUserModel selectLiveModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(LiveUsersA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, LiveUsersA.class,false);
        setContentView(R.layout.activity_live_users);
        context = LiveUsersA.this;
        rootref = FirebaseDatabase.getInstance().getReference();
        takePermissionUtils=new PermissionUtils(LiveUsersA.this,mPermissionResult);
        btnBack = findViewById(R.id.back_btn);
        btnBack.setOnClickListener(this);


        recyclerView = (RecyclerView) findViewById(R.id.recylerview);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));

        recyclerView.setHasFixedSize(true);

        adapter = new LiveUserAdapter(context, dataList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                LiveUserModel itemUpdate = (LiveUserModel) object;
                selectLiveModel=itemUpdate;
                if(Functions.checkLoginUser(LiveUsersA.this)) {

                    if (takePermissionUtils.isCameraRecordingPermissionGranted())
                    {
                        goLive();
                    }
                    else
                    {
                        takePermissionUtils.showCameraRecordingPermissionDialog(getString(R.string.we_need_camera_and_recording_permission_for_live_streaming));
                    }

                }
            }
        });

        recyclerView.setAdapter(adapter);


        getData();
        noDataFound = findViewById(R.id.no_data_found);
    }

    private void goLive() {
        openTicTicLive(selectLiveModel.getUser_id(),
                selectLiveModel.getUser_name(), selectLiveModel.getUser_picture(), Constants.CLIENT_ROLE_AUDIENCE);
    }


    // get the list of all live user from the firebase
    ChildEventListener valueEventListener;
    public void getData() {

        valueEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                LiveUserModel model = dataSnapshot.getValue(LiveUserModel.class);
                dataList.add(model);
                adapter.notifyDataSetChanged();
                noDataFound.setVisibility(View.GONE);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                LiveUserModel model = dataSnapshot.getValue(LiveUserModel.class);

                for (int i = 0; i < dataList.size(); i++) {
                    if (model.getUser_id().equals(dataList.get(i).getUser_id())) {
                        dataList.remove(i);
                    }
                }
                adapter.notifyDataSetChanged();

                if (dataList.isEmpty()) {
                    noDataFound.setVisibility(View.VISIBLE);
                }


            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };


        rootref.child("LiveUsers").addChildEventListener(valueEventListener);
    }


    @Override
    public void onDestroy() {
        mPermissionResult.unregister();
        if (rootref != null && valueEventListener != null)
            rootref.child("LiveUsers").removeEventListener(valueEventListener);
        super.onDestroy();

    }


    // watch the streaming of user which will be live
    public void openTicTicLive(String userId, String userName, String userImage, int role) {
        final Intent intent = new Intent();
        intent.putExtra("user_id", userId);
        intent.putExtra("user_name", userName);
        intent.putExtra("user_picture", userImage);
        intent.putExtra("user_role", role);
        intent.putExtra(com.thecroakers.app.ActivitiesFragment.LiveStreaming.Constants.KEY_CLIENT_ROLE, role);
        intent.setClass(LiveUsersA.this, LiveActivity.class);
        TheCroakers thecroakers = (TheCroakers)getApplication();
        thecroakers.engineConfig().setChannelName(userId);
        startActivity(intent);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_btn:
                LiveUsersA.super.onBackPressed();
                break;
            default:
            {}
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


    private ActivityResultLauncher<String[]> mPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onActivityResult(Map<String, Boolean> result) {

                    boolean allPermissionClear=true;
                    List<String> blockPermissionCheck=new ArrayList<>();
                    for (String key : result.keySet())
                    {
                        if (!(result.get(key)))
                        {
                            allPermissionClear=false;
                            blockPermissionCheck.add(Functions.getPermissionStatus(LiveUsersA.this,key));
                        }
                    }
                    if (blockPermissionCheck.contains("blocked"))
                    {
                        Functions.showPermissionSetting(LiveUsersA.this,getString(R.string.we_need_camera_and_recording_permission_for_live_streaming));
                    }
                    else
                    if (allPermissionClear)
                    {
                        goLive();
                    }

                }
            });


}
