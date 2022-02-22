package com.thecroakers.app.ActivitiesFragment.Profile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.thecroakers.app.Adapters.ProfileSharingAdapter;
import com.thecroakers.app.ApiClasses.ApiLinks;
import com.thecroakers.app.Constants;
import com.thecroakers.app.Interfaces.AdapterClickListener;
import com.thecroakers.app.Interfaces.FragmentCallBack;
import com.thecroakers.app.Models.ShareAppModel;
import com.thecroakers.app.R;
import com.thecroakers.app.SimpleClasses.Functions;
import com.thecroakers.app.SimpleClasses.Variables;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;

import org.json.JSONObject;

import java.util.ArrayList;


public class ShareAndViewProfileF extends BottomSheetDialogFragment implements View.OnClickListener {

    View view;
    Context context;
    FragmentCallBack callback;
    RecyclerView recyclerView;
    String userId;
    boolean fromSetting;
    TextView bottomBtn;
    String picUrl="";
    SimpleDraweeView userImage;
    String link;

    public ShareAndViewProfileF() {
    }

    public ShareAndViewProfileF(String id, boolean fromSetting, String picUrl, FragmentCallBack callback) {
        userId = id;
        this.fromSetting=fromSetting;
        this.callback = callback;
        this.picUrl = picUrl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_share_and_view_profile, container, false);
        context = getContext();

        userImage = view.findViewById(R.id.user_image);
        userImage.setOnClickListener(this);

        bottomBtn = view.findViewById(R.id.bottom_btn);
        bottomBtn.setOnClickListener(this);

        try {
            userImage.setController(Functions.frescoImageLoad(picUrl, userImage,false));
        } catch (Exception e) {
        }

        if(Functions.getSharedPreference(context).getBoolean(Variables.IS_LOGIN,false)) {
            getSharedApp();
        }

        return view;
    }


    ProfileSharingAdapter adapter;
    public void getSharedApp() {
        recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);
        adapter = new ProfileSharingAdapter(context, getAppShareDataList(), new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                ShareAppModel item = (ShareAppModel) object;
                generateShareLink(item);
            }
        });recyclerView.setAdapter(adapter);
    }

    public void generateShareLink(ShareAppModel item) {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("type", "user");
            parameters.put("entity_id", userId);
            parameters.put("user_id", Functions.getSharedPreference(getActivity()).getString(Variables.U_ID,""));
            parameters.put("device_id", Functions.getSharedPreference(getActivity()).getString(Variables.DEVICE_ID,""));
        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(ShareAndViewProfileF.this.getActivity(), ApiLinks.generateShareLink, parameters, Functions.getHeaders(context), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(ShareAndViewProfileF.this.getActivity(), resp);
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");
                    if (code.equals("200")) {
                        String shareLink = jsonObject.optString("msg");
                        link = Constants.BASE_URL+shareLink;
                        shareProfile(item);
                    } else {
                        Functions.showToast(context, jsonObject.optString("msg"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void shareProfile(ShareAppModel item) {
        //String profileLink = Variables.http+"://"+getString(R.string.share_profile_domain_second)+getString(R.string.share_profile_endpoint_second) + Functions.getSharedPreference(getActivity()).getString(Variables.U_ID,"");
        if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.whatsapp))) {
            try {
                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, link);
                sendIntent.setPackage("com.whatsapp");
                startActivity(sendIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        } else if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.facebook))) {
            try {
                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, link);
                sendIntent.setPackage("com.facebook.katana");
                startActivity(sendIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        } else if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.messenger))) {
            try {
                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, link);
                sendIntent.setPackage("com.facebook.orca");
                startActivity(sendIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        } else if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.sms))) {
            try {
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("sms_body",""+link);
                startActivity(smsIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        } else if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.copy_link))) {
            try {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Text", link);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context, context.getString(R.string.link_copy_in_clipboard), Toast.LENGTH_SHORT).show();
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        } else if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.email))) {
            try {
                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, link);
                sendIntent.setPackage("com.google.android.gm");
                startActivity(sendIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        } else if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.other))) {
            try {
                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, link);
                startActivity(sendIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }
    }

    private ArrayList<ShareAppModel> getAppShareDataList() {
        ArrayList<ShareAppModel> dataList=new ArrayList<>();
        {
            if (Functions.appInstalledOrNot(view.getContext(),"com.whatsapp")) {
                ShareAppModel item=new ShareAppModel();
                item.setName(getString(R.string.whatsapp));
                item.setIcon(R.drawable.ic_share_whatsapp);
                dataList.add(item);
            }
        }
        {
            if (Functions.appInstalledOrNot(view.getContext(),"com.facebook.katana")) {
                ShareAppModel item=new ShareAppModel();
                item.setName(getString(R.string.facebook));
                item.setIcon(R.drawable.ic_share_facebook);
                dataList.add(item);
            }
        }
        {
            if (Functions.appInstalledOrNot(view.getContext(),"com.facebook.orca")) {
                ShareAppModel item=new ShareAppModel();
                item.setName(getString(R.string.messenger));
                item.setIcon(R.drawable.ic_share_messenger);
                dataList.add(item);
            }
        }
        {
            ShareAppModel item=new ShareAppModel();
            item.setName(getString(R.string.sms));
            item.setIcon(R.drawable.ic_share_sms);
            dataList.add(item);
        }
        {
            ShareAppModel item=new ShareAppModel();
            item.setName(getString(R.string.copy_link));
            item.setIcon(R.drawable.ic_share_copy_link);
            dataList.add(item);
        }
        {
            if (Functions.appInstalledOrNot(view.getContext(),"com.whatsapp")) {
                ShareAppModel item=new ShareAppModel();
                item.setName(getString(R.string.email));
                item.setIcon(R.drawable.ic_share_email);
                dataList.add(item);
            }
        }
        {
            ShareAppModel item=new ShareAppModel();
            item.setName(getString(R.string.other));
            item.setIcon(R.drawable.ic_share_other);
            dataList.add(item);
        }
        return dataList;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.send_message_layout:
            {
                Bundle bundle = new Bundle();
                bundle.putString("action", "profileShareMessage");
                dismiss();

                if (callback != null)
                    callback.onResponse(bundle);
            }
            break;
            case R.id.user_image:
            {
                Intent intent=new Intent(view.getContext(), SeeFullImageA.class);
                intent.putExtra("image_url", picUrl);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
            break;
            case R.id.bottom_btn:
                dismiss();
                break;
        }
    }
}
