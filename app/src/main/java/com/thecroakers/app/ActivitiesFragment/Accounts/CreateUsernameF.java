package com.thecroakers.app.ActivitiesFragment.Accounts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.thecroakers.app.ActivitiesFragment.SplashA;
import com.thecroakers.app.Constants;
import com.thecroakers.app.Models.UserModel;
import com.thecroakers.app.Models.UserRegisterModel;
import com.thecroakers.app.MainMenu.MainMenuActivity;
import com.thecroakers.app.R;
import com.thecroakers.app.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.thecroakers.app.SimpleClasses.DataParsing;
import com.thecroakers.app.SimpleClasses.Functions;
import com.thecroakers.app.SimpleClasses.Variables;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.paperdb.Paper;

// This fragment will get the username from the users
public class CreateUsernameF extends Fragment {
    View view;
    EditText usernameEdit;
    Button signUpBtn;
    UserRegisterModel userRegisterModel;
    SharedPreferences sharedPreferences;
    String fromWhere;
    TextView usernameCountTxt;

    public CreateUsernameF(String fromWhere) {
        this.fromWhere = fromWhere;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_user_name, container, false);

        Bundle bundle = getArguments();
        userRegisterModel = (UserRegisterModel) bundle.getSerializable("user_model");

        view.findViewById(R.id.goBack).setOnClickListener(v->{
                getActivity().onBackPressed();
        });

        sharedPreferences = Functions.getSharedPreference(getContext());
        usernameEdit = view.findViewById(R.id.username_edit);
        signUpBtn = view.findViewById(R.id.btn_sign_up);

        usernameCountTxt = view.findViewById(R.id.username_count_txt);

        InputFilter[] username_filters = new InputFilter[1];
        username_filters[0] = new InputFilter.LengthFilter(Constants.USERNAME_CHAR_LIMIT);
        usernameEdit.setFilters(username_filters);
        usernameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // check the username field length

                usernameCountTxt.setText(usernameEdit.getText().length() + "/" + Constants.USERNAME_CHAR_LIMIT);
                String txtName = usernameEdit.getText().toString();
                if (txtName.length() > 0) {
                    signUpBtn.setEnabled(true);
                    signUpBtn.setClickable(true);
                } else {
                    signUpBtn.setEnabled(false);
                    signUpBtn.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        signUpBtn.setOnClickListener(v -> {
            // check validation and then call the signup api
                if (checkValidation()) {
                    call_api_for_signup();
                }
        });

        return view;
    }

    private void call_api_for_signup() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("dob", "" + userRegisterModel.dateOfBirth);
            parameters.put("username", "" + usernameEdit.getText().toString());
            parameters.put("country_id", "" + userRegisterModel.countryId);

            if (fromWhere.equals("fromEmail")) {
                parameters.put("email", "" + userRegisterModel.email);
                parameters.put("password", userRegisterModel.password);
            } else if (fromWhere.equals("fromPhone")) {
                parameters.put("phone", "" + userRegisterModel.phoneNo);
            } else if (fromWhere.equals("social")) {
                parameters.put("email", "" + userRegisterModel.email);
                parameters.put("social_id", "" + userRegisterModel.socailId);
                parameters.put("profile_pic", "" + userRegisterModel.picture);
                parameters.put("social", "" + userRegisterModel.socailType);
                parameters.put("first_name", "" + userRegisterModel.fname);
                parameters.put("last_name", "" + userRegisterModel.lname);
                parameters.put("auth_token", "" + userRegisterModel.authTokon);
                parameters.put("device_token", Variables.DEVICE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(getActivity(), false, false);
        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.registerUser, parameters, Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);

                Functions.cancelLoader();
                parseSignupData(resp);
            }
        });
    }

    // if the signup successful then this method will call and it store the user info in local
    public void parseSignupData(String loginData) {
        try {
            JSONObject jsonObject = new JSONObject(loginData);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONObject jsonObj = jsonObject.getJSONObject("msg");
                UserModel userDetailModel = DataParsing.getUserDataModel(jsonObj.optJSONObject("User"));
                if (fromWhere.equals("social")) {
                    userDetailModel.setAuthToken(userRegisterModel.authTokon);
                }
                Functions.storeUserLoginDataIntoDb(view.getContext(),userDetailModel);

                Functions.setUpMultipleAccount(view.getContext());

                getActivity().sendBroadcast(new Intent("newVideo"));

                Variables.reloadMyVideos = true;
                Variables.reloadMyVideosInner = true;
                Variables.reloadMyLikesInner = true;
                Variables.reloadMyNotification = true;

                if (Paper.book(Variables.MultiAccountKey).getAllKeys().size()>1) {
                    Intent intent=new Intent(view.getContext(), SplashA.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    view.getContext().startActivity(intent);
                } else {
                    Intent intent=new Intent(view.getContext(), MainMenuActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    view.getContext().startActivity(intent);
                }
            } else {
                Toast.makeText(getActivity(), "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // check the username validation here
    public boolean checkValidation() {
        String uname = usernameEdit.getText().toString();
        if (TextUtils.isEmpty(uname)) {
            usernameEdit.setError(view.getContext().getString(R.string.username_cant_empty));
            usernameEdit.setFocusable(true);
            return false;
        }
        if (uname.length() < 4 || uname.length() > 20) {
            usernameEdit.setError(view.getContext().getString(R.string.username_length_between_valid));
            usernameEdit.setFocusable(true);
            return false;
        }
        if (!(UserNameTwoCaseValidate(uname))) {
            usernameEdit.setError(view.getContext().getString(R.string.username_must_contain_alphabet));
            usernameEdit.setFocusable(true);
            return false;
        }

        return true;
    }

    private boolean UserNameTwoCaseValidate(String name) {
        Pattern let_p = Pattern.compile("[a-zA-Z0-9_]", Pattern.CASE_INSENSITIVE);
        Matcher let_m = let_p.matcher(name);
        boolean let_str = let_m.find();

        if (let_str) {
            return true;
        }
        return false;
    }
}