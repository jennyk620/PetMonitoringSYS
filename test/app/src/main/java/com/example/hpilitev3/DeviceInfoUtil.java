package com.example.hpilitev3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;

public class DeviceInfoUtil {

    /**
     * device id 가져오기
     * @param context
     * @return
     */
    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * device 제조사 가져오기
     * @return
     */
    public static String getDeviceName() {
        return Build.DEVICE;
    }

    /**
     * device 브랜드 가져오기
     * @return
     */
    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    /**
     * device 모델명 가져오기
     * @return
     */
    public static String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * device Android OS 버전 가져오기
     * @return
     */
    public static String getDeviceOs() {
        return Build.VERSION.RELEASE;
    }

    /**
     * device SDK 버전 가져오기
     * @return
     */
    public static int getDeviceSdk() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * device MAC 주소 가져오기
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getDeviceMAC(Context context) {
        WifiManager mng = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiInfo info = mng.getConnectionInfo();
        String mac = info.getMacAddress();
        return mac;
    }
}