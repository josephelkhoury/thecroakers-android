package com.thecroakers.app.SimpleClasses;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

import com.thecroakers.app.Interfaces.FragmentCallBack;
import com.thecroakers.app.R;
import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {
    Activity activity;
    ActivityResultLauncher<String[]> mPermissionResult;

    public PermissionUtils(Activity activity,ActivityResultLauncher<String[]> mPermissionResult) {
        this.activity = activity;
        this.mPermissionResult=mPermissionResult;
    }

    public void takeStorageCameraRecordingPermission()
    {
        if (Build.VERSION.SDK_INT==Build.VERSION_CODES.R || Build.VERSION.SDK_INT==Build.VERSION_CODES.Q)
        {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
            mPermissionResult.launch(permissions);

        }
        else
        {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
            mPermissionResult.launch(permissions);
        }
    }

    public void takeStorageCameraPermission()
    {
        if (Build.VERSION.SDK_INT==Build.VERSION_CODES.R || Build.VERSION.SDK_INT==Build.VERSION_CODES.Q)
        {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
            mPermissionResult.launch(permissions);
        }
        else
        {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
            mPermissionResult.launch(permissions);
        }
    }

    public void takeStorageRecordingPermission()
    {
        if (Build.VERSION.SDK_INT==Build.VERSION_CODES.R || Build.VERSION.SDK_INT==Build.VERSION_CODES.Q)
        {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO};
            mPermissionResult.launch(permissions);
        }
        else
        {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO};
            mPermissionResult.launch(permissions);
        }
    }

    public void takeStoragePermission()
    {
        if (Build.VERSION.SDK_INT==Build.VERSION_CODES.R || Build.VERSION.SDK_INT==Build.VERSION_CODES.Q)
        {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            mPermissionResult.launch(permissions);
        }
        else
        {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
            mPermissionResult.launch(permissions);
        }
    }

    public void takeCameraPermission()
    {
        String[] permissions = {Manifest.permission.CAMERA};
        mPermissionResult.launch(permissions);
    }

    public void takeCameraRecordingPermission()
    {
        String[] permissions = {Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
        mPermissionResult.launch(permissions);
    }

    public void takeContactPermission()
    {
        String[] permissions = {Manifest.permission.READ_CONTACTS};
        mPermissionResult.launch(permissions);
    }


    public void showCameraPermissionDialog(String message)
    {
        List<String> permissionStatusList=new ArrayList<>();
        String[] permissions = {Manifest.permission.CAMERA};
        for (String keyStr:permissions)
        {
            permissionStatusList.add(Functions.getPermissionStatus(activity,keyStr));
        }

        if (permissionStatusList.contains("denied"))
        {
            Functions.showDoubleButtonAlert(activity, activity.getString(R.string.permission_alert),message,
                    activity.getString(R.string.cancel_), activity.getString(R.string.permission), false, new FragmentCallBack() {
                        @Override
                        public void onResponse(Bundle bundle) {
                            if (bundle.getBoolean("isShow",false))
                            {
                                takeCameraPermission();
                            }
                        }
                    });
            return;
        }
        takeCameraPermission();

    }

    public boolean isCameraPermissionGranted()
    {
        int cameraPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        return (cameraPermission== PackageManager.PERMISSION_GRANTED);
    }

    public void showStorageCameraPermissionDialog(String message)
    {
        List<String> permissionStatusList=new ArrayList<>();
        String[] permissions;
        if (Build.VERSION.SDK_INT==Build.VERSION_CODES.R || Build.VERSION.SDK_INT==Build.VERSION_CODES.Q)
        {
            permissions =new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
        }
        else
        {
            permissions =new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
        }

        for (String keyStr:permissions)
        {
            permissionStatusList.add(Functions.getPermissionStatus(activity,keyStr));
        }

        if (permissionStatusList.contains("denied"))
        {
            Functions.showDoubleButtonAlert(activity, activity.getString(R.string.permission_alert),message,
                    activity.getString(R.string.cancel_), activity.getString(R.string.permission), false, new FragmentCallBack() {
                        @Override
                        public void onResponse(Bundle bundle) {
                            if (bundle.getBoolean("isShow",false))
                            {
                                takeStorageCameraPermission();
                            }
                        }
                    });
            return;
        }
        takeStorageCameraPermission();

    }

    public boolean isStorageCameraPermissionGranted()
    {
        if (Build.VERSION.SDK_INT==Build.VERSION_CODES.R || Build.VERSION.SDK_INT==Build.VERSION_CODES.Q)
        {
            int readExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            int cameraPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
            return (readExternalStoragePermission== PackageManager.PERMISSION_GRANTED && cameraPermission== PackageManager.PERMISSION_GRANTED);
        }
        else
        {
            int readExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            int writeExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int cameraPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
            return (readExternalStoragePermission== PackageManager.PERMISSION_GRANTED && writeExternalStoragePermission== PackageManager.PERMISSION_GRANTED && cameraPermission== PackageManager.PERMISSION_GRANTED);
        }
    }

    public void showStoragePermissionDialog(String message)
    {
        List<String> permissionStatusList=new ArrayList<>();
        String[] permissions ;
        if (Build.VERSION.SDK_INT==Build.VERSION_CODES.R || Build.VERSION.SDK_INT==Build.VERSION_CODES.Q)
        {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
        else
        {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }
        for (String keyStr:permissions)
        {
            permissionStatusList.add(Functions.getPermissionStatus(activity,keyStr));
        }

        if (permissionStatusList.contains("denied"))
        {
            Functions.showDoubleButtonAlert(activity, activity.getString(R.string.permission_alert),message,
                    activity.getString(R.string.cancel_), activity.getString(R.string.permission), false, new FragmentCallBack() {
                        @Override
                        public void onResponse(Bundle bundle) {
                            if (bundle.getBoolean("isShow",false))
                            {
                                takeStoragePermission();
                            }
                        }
                    });
            return;
        }
        takeStoragePermission();

    }

    public boolean isStoragePermissionGranted()
    {
        if (Build.VERSION.SDK_INT==Build.VERSION_CODES.R || Build.VERSION.SDK_INT==Build.VERSION_CODES.Q)
        {
            int readExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            return (readExternalStoragePermission== PackageManager.PERMISSION_GRANTED);
        }
        else
        {
            int readExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            int writeExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return (readExternalStoragePermission== PackageManager.PERMISSION_GRANTED && writeExternalStoragePermission== PackageManager.PERMISSION_GRANTED );
        }
    }

    public void showCameraRecordingPermissionDialog(String message)
    {
        List<String> permissionStatusList=new ArrayList<>();
        String[] permissions = {Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
        for (String keyStr:permissions)
        {
            permissionStatusList.add(Functions.getPermissionStatus(activity,keyStr));
        }

        if (permissionStatusList.contains("denied"))
        {
            Functions.showDoubleButtonAlert(activity, activity.getString(R.string.permission_alert),message,
                    activity.getString(R.string.cancel_), activity.getString(R.string.permission), false, new FragmentCallBack() {
                        @Override
                        public void onResponse(Bundle bundle) {
                            if (bundle.getBoolean("isShow",false))
                            {
                                takeCameraRecordingPermission();
                            }
                        }
                    });
            return;
        }
        takeCameraRecordingPermission();

    }

    public boolean isCameraRecordingPermissionGranted()
    {
        int cameraPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        int recordAudioPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
        return (cameraPermission== PackageManager.PERMISSION_GRANTED && recordAudioPermission== PackageManager.PERMISSION_GRANTED );
    }

    public void showContactPermissionDialog(String message)
    {
        List<String> permissionStatusList=new ArrayList<>();
        String[] permissions = {Manifest.permission.READ_CONTACTS};
        for (String keyStr:permissions)
        {
            permissionStatusList.add(Functions.getPermissionStatus(activity,keyStr));
        }

        if (permissionStatusList.contains("denied"))
        {
            Functions.showDoubleButtonAlert(activity, activity.getString(R.string.permission_alert),message,
                    activity.getString(R.string.cancel_), activity.getString(R.string.permission), false, new FragmentCallBack() {
                        @Override
                        public void onResponse(Bundle bundle) {
                            if (bundle.getBoolean("isShow",false))
                            {
                                takeContactPermission();
                            }
                        }
                    });
            return;
        }
        takeContactPermission();

    }

    public boolean isContactPermissionGranted()
    {
        int contactPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS);
        return (contactPermission== PackageManager.PERMISSION_GRANTED);
    }

    public void showStorageRecordingPermissionDialog(String message)
    {
        List<String> permissionStatusList=new ArrayList<>();
        String[] permissions;
        if (Build.VERSION.SDK_INT==Build.VERSION_CODES.R || Build.VERSION.SDK_INT==Build.VERSION_CODES.Q)
        {
            permissions =new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO};
        }
        else
        {
            permissions =new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO};
        }

        for (String keyStr:permissions)
        {
            permissionStatusList.add(Functions.getPermissionStatus(activity,keyStr));
        }

        if (permissionStatusList.contains("denied"))
        {
            Functions.showDoubleButtonAlert(activity, activity.getString(R.string.permission_alert),message,
                    activity.getString(R.string.cancel_), activity.getString(R.string.permission), false, new FragmentCallBack() {
                        @Override
                        public void onResponse(Bundle bundle) {
                            if (bundle.getBoolean("isShow",false))
                            {
                                takeStorageRecordingPermission();
                            }
                        }
                    });
            return;
        }
        takeStorageRecordingPermission();

    }

    public boolean isStorageRecordingPermissionGranted()
    {
        if (Build.VERSION.SDK_INT==Build.VERSION_CODES.R || Build.VERSION.SDK_INT==Build.VERSION_CODES.Q)
        {
            int readExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            int recordingPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
            return (readExternalStoragePermission== PackageManager.PERMISSION_GRANTED && recordingPermission== PackageManager.PERMISSION_GRANTED);
        }
        else
        {
            int readExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            int writeExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int recordingPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
            return (readExternalStoragePermission== PackageManager.PERMISSION_GRANTED && writeExternalStoragePermission== PackageManager.PERMISSION_GRANTED && recordingPermission== PackageManager.PERMISSION_GRANTED);
        }
    }

    public void showStorageCameraRecordingPermissionDialog(String message)
    {
        List<String> permissionStatusList=new ArrayList<>();
        String[] permissions ;
        if (Build.VERSION.SDK_INT==Build.VERSION_CODES.R || Build.VERSION.SDK_INT==Build.VERSION_CODES.Q)
        {
            permissions =new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
        }
        else
        {
             permissions =new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
        }

        for (String keyStr:permissions)
        {
            permissionStatusList.add(Functions.getPermissionStatus(activity,keyStr));
        }

        if (permissionStatusList.contains("denied"))
        {
            Functions.showDoubleButtonAlert(activity, activity.getString(R.string.permission_alert),message,
                    activity.getString(R.string.cancel_), activity.getString(R.string.permission), false, new FragmentCallBack() {
                        @Override
                        public void onResponse(Bundle bundle) {
                            if (bundle.getBoolean("isShow",false))
                            {
                                takeStorageCameraRecordingPermission();
                            }
                        }
                    });
            return;
        }
        takeStorageCameraRecordingPermission();

    }

    public boolean isStorageCameraRecordingPermissionGranted()
    {
        if (Build.VERSION.SDK_INT==Build.VERSION_CODES.R || Build.VERSION.SDK_INT==Build.VERSION_CODES.Q)
        {
            int readExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            int recordingPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
            int cameraPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
            return (readExternalStoragePermission== PackageManager.PERMISSION_GRANTED && cameraPermission== PackageManager.PERMISSION_GRANTED && recordingPermission== PackageManager.PERMISSION_GRANTED);
        }
        else
        {
            int readExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            int writeExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int recordingPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
            int cameraPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
            return (readExternalStoragePermission== PackageManager.PERMISSION_GRANTED && writeExternalStoragePermission== PackageManager.PERMISSION_GRANTED && cameraPermission== PackageManager.PERMISSION_GRANTED && recordingPermission== PackageManager.PERMISSION_GRANTED);
        }
    }

}
