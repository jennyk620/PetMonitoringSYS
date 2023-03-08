package com.example.hpilitev3;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public abstract class BluetoothLibrary extends AppCompatActivity  {

    private Context mainContext = this;

    ////////////////////////
    // PERMISSION
    ////////////////////////
    private static final int REQUEST_ENABLE_BT = 3054;
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private String[] REQUIRED_PERMISSIONS = new String[]{
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION",
            //"android.permission.ACCESS_BACKGROUND_LOCATION",
            "android.permission.CHANGE_WIFI_STATE",
            "android.permission.ACCESS_WIFI_STATE",
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.BLUETOOTH",
            "android.permission.BLUETOOTH_ADMIN",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"};

    ////////////////////////
    // BLUETHOOTH
    ////////////////////////
    // 블루투스 관련 어댑터, 서비스 등
    static BluetoothAdapter.LeScanCallback mLeScanCallback;
    static BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    static Adapter_bluetooth_device mLeDeviceListAdapter=null;
    private static BluetoothAdapter mBluetoothAdapter;

    // 블루투스 관련 정보
    protected static String mDeviceName;
    protected static String mDeviceAddress;
    private static boolean mScanning =false;

    /**
     * DB 관련
     */
    DB_INFO_Helper db_info_helper;
    protected String petName;


    /**
     * Main Activity의 Oncreate에서 가장먼저 호출하는 Process :
     * 서비스 시작
     */
    public void onCreateProcess()
    {
        startService(new Intent(this, ForecdTerminationService.class));


        ////////////////////
        // 블루투스 서비스 시작
        ////////////////////
        if(!Initialize_bluetooth()){
            Toast.makeText(mainContext, R.string.error_bluetooth_not_supported,Toast.LENGTH_SHORT).show();
            ((Activity) mainContext).finish();
        }

        // DB 열기
        db_info_helper = new DB_INFO_Helper(mainContext);
        // 가장 마지막에 연결한 WearDeviceinfo 가져오기
        String LastWearDeviceName = db_info_helper.GetLastWearDeviceName();
        if(!LastWearDeviceName.equals("")) {
            mDeviceName = LastWearDeviceName;
            mDeviceAddress = db_info_helper.GetLastWearDeviceID();
            petName = db_info_helper.getPetName();
        }

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        // activity가 죽은 후에도 service가 살아있도록 설정 (startservice는 stopservice()가 호출되어야 종료함)
        startService(gattServiceIntent);
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);


        // Initializes list view adapter.
        mLeDeviceListAdapter = new Adapter_bluetooth_device(
                mainContext, new ArrayList<BluetoothDevice>());


    }

    /**
     * Main Activity의 OnResume에서 가장먼저 호출하는 Process
     */
    @SuppressLint("MissingPermission")
    public void onResumeProcess() {
        Log.i("BluetoothLibrary", "onResumeProcess()");
        // Ensures Bluetooth is enabled on the device. If Bluetooth is not
        // currently enabled,
        // fire an intent to display a dialog asking the user to grant
        // permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((Activity) mainContext).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        mainContext.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    /**
     * Main Activity의 OnPause에서 가장먼저 호출하는 Process
     */
    public void onPauseProcess() {
        Log.i("BluetoothLibrary", "onPauseProcess()");

        // 블루투스 디바이스 스캔 false
        scanLeDevice(false);
        if(mBluetoothLeService!=null) {
            mainContext.unregisterReceiver(mGattUpdateReceiver);
            mLeDeviceListAdapter.clear();
            mBluetoothLeService.disconnect();
        }


        /*
        if(mBluetoothLeService!=null)
        {
            mBluetoothLeService.disconnect();
            //mHandler.postDelayed(mDisonnectingOverTimeRunnable, 10000);
			mBluetoothLeService.close();
        }*/
    }

    /**
     * Main Activity의 OnDestroy에서 가장먼저 호출하는 Process
     */
    public void onDestroyProcess() {
        mainContext.unbindService(mServiceConnection);
        mBluetoothLeService.close(); // 블루투스 서비스 종료
        mBluetoothLeService = null;
    }

    public void onActivityResultProcess(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT
                && resultCode == Activity.RESULT_CANCELED) {
            ((Activity) mainContext).finish();
            return;
        }
    }

    @SuppressLint("MissingPermission")
    boolean Initialize_bluetooth()
    {
        // Use this check to determine whether BLE is supported on the device.
        // Then you can
        // selectively disable BLE-related features.
        if (!mainContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }

        Whitelist_Check();

        // Initializes a Bluetooth adapter. For API level 18 and above, get a
        // reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) mainContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "블루투스를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.e("Bluetooth paring list : ", deviceName + " " + deviceHardwareAddress);
            }
        }


        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(final BluetoothDevice device, int rssi,
                                 byte[] scanRecord) {

                ((Activity) mainContext).runOnUiThread(new Runnable() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void run() {
                        if (device.getName() != null)
                            // "Pedo(.*)")
                            if (device.getName().matches("(.*)")) {//if (device.getName().matches("(.*)")|| device.getName().matches("NINA(.*)")) {
                                mLeDeviceListAdapter.addDevice(device);
                                mLeDeviceListAdapter.notifyDataSetChanged();
                            } else
                                mLeDeviceListAdapter.notifyDataSetChanged();
                    }
                });
            }
        };
        return true;
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.i("BluetoothLibrary  ", "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            //mBluetoothLeService.connect(mDeviceAddress, false);
            Log.e("BluetoothLibrary ", "Bluetooth ServiceConnection");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.

    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            System.out.println("mGattUpdateReceiver->onReceive->action="+action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d("BluetoothLibrary", "ACTION_GATT_CONNECTED");
                scanLeDevice(false);

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d("BluetoothLibrary", "ACTION_GATT_DISCONNECTED");
            }
        }
    };

    @SuppressLint("MissingPermission")
    public static void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            if(mLeDeviceListAdapter != null)
            {
                mLeDeviceListAdapter.clear();
                mLeDeviceListAdapter.notifyDataSetChanged();
            }

            if(!mScanning)
            {
                mScanning = true;
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }
        } else {
            if(mScanning)
            {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        return intentFilter;
    }

    /**
     * Permission 설정
     */

    protected boolean checkPermissionsAll(){
        boolean result = true;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            REQUIRED_PERMISSIONS = new String[]{
                    "android.permission.ACCESS_COARSE_LOCATION",
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_BACKGROUND_LOCATION",
                    "android.permission.CHANGE_WIFI_STATE",
                    "android.permission.ACCESS_WIFI_STATE",
                    "android.permission.CAMERA",
                    "android.permission.RECORD_AUDIO",
                    "android.permission.INTERNET",
                    "android.permission.ACCESS_NETWORK_STATE",
                    "android.permission.BLUETOOTH",
                    "android.permission.BLUETOOTH_ADMIN",
                    "android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE",
                    "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"};
        }

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(mainContext, permission) !=
                    PackageManager.PERMISSION_GRANTED){
                Log.d("permission_check", permission + "권한이 없습니다");
                result = false;
            }
        }
        if (!result) {
            // 권한 요청
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
        //Log.d("permission_check", "권한이 허가되어있습니다");
        return result;
    }


    // 권한 체크 이후로직
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // READ_PHONE_STATE의 권한 체크 결과를 불러온다
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE_PERMISSIONS){
            boolean check_result = true;

            // 모든 퍼미션 허용했는지 체크
            if(checkPermissionsAll()){
                //startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    public interface OnPermissionsResult{
        void OnSuccess();
        void OnFail(List<String> noPermissions);
    }

    /**
     * 블루투스 관련 설정
     */
    public void Whitelist_Check(){
        /**
         * 안드로이드 6.0 이상 (API23) 부터는 Doze모드가 추가됨.
         * 일정시간 화면이꺼진 상태로 디바이스를 이용하지 않을 시 일부 백그라운드 서비스 및 알림서비스가 제한됨.
         * 6.0이상의 버전이라면 화이트리스트에 등록이 됐는지 Check
         */
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean WhiteCheck = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            /**
             * 등록이 되어있따면 TRUE
             * 등록이 안되있다면 FALSE
             */
            WhiteCheck = powerManager.isIgnoringBatteryOptimizations(getPackageName());
            /** 만약 화이트리스트에 등록이 되지않았다면 등록을 해줍니다. **/
            if(!WhiteCheck){
                Log.e("화이트리스트","화이트리스트에 등록되지않았습니다.");
                Intent intent  = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:"+ getPackageName()));
                startActivity(intent);
            }
            else Log.e("화이트리스트","화이트리스트에 등록되어있습니다.");
        }
    }
}