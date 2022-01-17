package com.thecroakers.app.ActivitiesFragment.Accounts;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.thecroakers.app.ActivitiesFragment.VideoRecording.PostVideoA;
import com.thecroakers.app.ApiClasses.ApiLinks;
import com.thecroakers.app.Models.UserRegisterModel;
import com.thecroakers.app.R;
import com.thecroakers.app.SimpleClasses.Functions;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.ycuwq.datepicker.date.DatePicker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

public class DateOfBirthF extends Fragment implements View.OnClickListener {
    Context context;
    View view;
    DatePicker datePicker;
    Button btnDobNext;
    String currentDate, stYear;
    Date c;
    String fromWhere;
    UserRegisterModel userRegisterModel = new UserRegisterModel();
    TextView countryTxt;
    ArrayList<JSONObject> countries = new ArrayList<>();
    ArrayList<String> countriesStr = new ArrayList<String>();
    String country_id = "";

    public DateOfBirthF() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        context = DateOfBirthF.this.getContext();
        view = inflater.inflate(R.layout.fragment_dob_fragment, container, false);
        c = Calendar.getInstance().getTime();
        datePicker = view.findViewById(R.id.datePicker);
        btnDobNext = view.findViewById(R.id.btn_dob_next);
        datePicker.setMaxDate(System.currentTimeMillis() - 1000);

        Bundle bundle = getArguments();
        userRegisterModel = (UserRegisterModel) bundle.getSerializable("user_model");
        fromWhere = bundle.getString("fromWhere");
        datePicker.setOnDateSelectedListener(new DatePicker.OnDateSelectedListener() {
            @Override
            public void onDateSelected(int year, int month, int day) {
                // select the date from datepicker
                stYear = String.valueOf(year);
                currentDate = year + "-" + month + "-" + day;
                userRegisterModel.dateOfBirth = currentDate;
            }
        });
        datePicker.getYearPicker().setEndYear(2020);

        view.findViewById(R.id.btn_dob_next).setOnClickListener(this::onClick);
        view.findViewById(R.id.goBack).setOnClickListener(this::onClick);

        view.findViewById(R.id.country_layout).setOnClickListener(this);
        countryTxt = view.findViewById(R.id.country_txt);

        getCountries();

        return view;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.goBack:
                getActivity().onBackPressed();
                break;

            case R.id.btn_dob_next:
                checkDobDate();
                break;
            case R.id.country_layout:
                countryDialog();
                break;

            default:
                break;
        }
    }

    public void checkDobDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyy", Locale.ENGLISH);
        String formattedDate = df.format(c);
        Date dob = null;
        Date currentdate = null;
        try {
            dob = df.parse(formattedDate);
            currentdate = df.parse(currentDate);
        } catch (Exception e) {

        }

        int value = getDiffYears(currentdate, dob);
        // check that a user select the date greater then 17 year
        //if (value > 17) {
            //get the email or phone if a user want to signup
            if (fromWhere.equals("signup")) {
                EmailPhoneF emailPhoneF = new EmailPhoneF(fromWhere);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
                Bundle bundle = new Bundle();
                bundle.putSerializable("user_model", userRegisterModel);
                emailPhoneF.setArguments(bundle);
                transaction.addToBackStack(null);
                transaction.replace(R.id.dob_fragment, emailPhoneF).commit();
            }
            else if (fromWhere.equals("social") || fromWhere.equals("fromPhone")) {
                //if user from facebook or phone number the get the username
                CreateUsernameF signUp_fragment = new CreateUsernameF(fromWhere);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
                Bundle bundle = new Bundle();
                bundle.putSerializable("user_model", userRegisterModel);
                signUp_fragment.setArguments(bundle);
                transaction.addToBackStack(null);
                transaction.replace(R.id.dob_fragment, signUp_fragment).commit();
            }
        //} else {
        //    Toast.makeText(getActivity(), view.getContext().getString(R.string.age_must_be_over_eighteen), Toast.LENGTH_SHORT).show();
        //}
    }

    // this method will return the years difference
    public int getDiffYears(Date first, Date last) {
        Calendar a = getCalendar(first);
        Calendar b = getCalendar(last);
        int diff = b.get(YEAR) - a.get(YEAR);
        if (a.get(MONTH) > b.get(MONTH) ||
                (a.get(MONTH) == b.get(MONTH) && a.get(DATE) > b.get(DATE))) {
            diff--;
        }
        return diff;
    }

    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(date);
        return cal;
    }

    private void getCountries() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("worldwide", "0");
        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(DateOfBirthF.this.getActivity(), ApiLinks.showCountries, parameters, Functions.getHeaders(context), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(DateOfBirthF.this.getActivity(), resp);
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

    private void countryDialog() {
        final CharSequence[] options = countriesStr.toArray(new CharSequence[countriesStr.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);

        builder.setTitle(getString(R.string.country));

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                countryTxt.setText(options[i]);
                JSONObject country = countries.get(i);
                country_id = country.optString("id");
                userRegisterModel.countryId = country_id;
                btnDobNext.setEnabled(true);
                btnDobNext.setClickable(true);
            }
        });

        builder.show();
    }
}