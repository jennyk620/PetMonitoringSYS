package com.example.hpilitev3;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import com.example.hpilitev3.databinding.ActivityCollectBinding;
import com.example.hpilitev3.data.db.SensorDatabase;
import com.example.hpilitev3.presentation.event.CollectActivityEvent;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CollectActivity extends AppCompatActivity {

    /**
     * view 관련
     */
    private TextView mConnection;
    private TextView acc_x, acc_y, acc_z, gyro_x, gyro_y, gyro_z, mag_x, mag_y, mag_z, roll, pitch, yaw;
    private Button Finishbutton;
    private ActivityCollectBinding binding;

    /**
     * bluetooth 관련
     */
    ////////////
    // int mBaudrate=115200;	//set the default baud rate to 115200
    //private String mPassword="AT+PASSWOR=DFRobot\r\n";
    //private String mBaudrateBuffer = "AT+CURRUART="+mBaudrate+"\r\n";
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private String mDeviceName;
    private String mDeviceAddress;



    /**
     * 기타 관련
     */
    private final static String TAG = CollectActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "EXTRAS_DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "EXTRAS_DEVICE_ADDRESS";

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    //자동동작 timer
    static int timeroncount = 0;
    static Timer timermaster, timer ;
    TimerTask TT;

    Intent Notiintent;
    static int noticount = 0;

    /**
     * Room DB 관련.
     */
    private CollectViewModel viewModel;
    private String petName;

    /**
     * Camera 관련.
     */

    private static final String CAM_WHAT = "2";
    private static final String CAM_FRONT = "1";
    private static final String CAM_REAR = "0";
    private String mCamId;
    private static final String DETAIL_PATH = "DCIM/HPILite/";
    CameraCaptureSession mCameraCaptureSession;
    CameraDevice mCameraDevice;
    CameraManager mCameraManager;
    Size mVideoSize;
    Size mPreviewSize;
    CaptureRequest.Builder mCaptureRequestBuilder;
    int mSensorOrientation;
    Semaphore mSemaphore = new Semaphore(1);
    HandlerThread mBackgroundThread;
    Handler mBackgroundHandler;
    MediaRecorder mMediaRecorder;
    private String mNextVideoAbsolutePath;
    private boolean mIsRecordingVideo;

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            startPreview();
            mSemaphore.release();
            configureTransform(binding.preview.getWidth(), binding.preview.getHeight());
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mSemaphore.release();
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mSemaphore.release();
            camera.close();
            mCameraDevice = null;
            finish();
        }
    };

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(binding.preview.getWidth(), binding.preview.getHeight());
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            if (!mIsRecordingVideo) configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };


    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_collect);

        // MainActivity 정보 받기
        final Intent intent = getIntent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.setPackage(null);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        petName = intent.getStringExtra("EXTRAS_DEVICE_PET_NAME");
        Log.i("CollectAvctivity DEVICE_NAME", mDeviceName);
        Log.i("CollectAvctivity DEVICE_ADDRESS", mDeviceAddress);

        // view 설정
        Initialize_view();

        // Bind service with the activity
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        ///////////////////////////////////////
        //serialBegin(115200);

//        try {
//            mdbhelper.open();
//        } catch (SQLException throwables) {
//            throwables.printStackTrace();
//        }
//        mdbhelper.create();

        Notiintent = intent;

        //db init
        viewModel = new ViewModelProvider(this).get(CollectViewModel.class);
        viewModel.setDeviceAddress(mDeviceAddress);
        viewModel.setPetName(petName);
        viewModel.setPath(getFilesDir());

        //camera
        mCamId = CAM_REAR;

        if (binding.preview.isAvailable()) {
            openCamera(binding.preview.getWidth(), binding.preview.getHeight());
            startRecordingVideo();
        } else {
            binding.preview.setSurfaceTextureListener(mSurfaceTextureListener);
        }

//        binding.buttonRec.setOnClickListener(view -> {
//            if (mIsRecordingVideo) stopRecordingVideo();
//            else startRecordingVideo();
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        startBackgroundThread();
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress, false);
            Log.d(TAG, "Connect request result=" + result);
        }

        viewModel.getState().observe(this, state -> {
            switch (state) {
                case 0:
                    binding.buttonFinish.setEnabled(false);
                    binding.progressBar2.setVisibility(View.VISIBLE);
                    binding.getRoot().setEnabled(false);
                   break;
                case 1:
                    Log.e(TAG, "onResume: su");
                    binding.progressBar2.setVisibility(View.GONE);
                    binding.getRoot().setEnabled(false);
                    binding.buttonFinish.setEnabled(true);
                    finish();
                    break;
                case -1:
                    Log.e(TAG, "onResume: collect");
                    break;
                default:
            }
//            if (state instanceof CollectActivityEvent.Loading) {
//                binding.progressBar2.setVisibility(View.VISIBLE);
//                binding.getRoot().setEnabled(false);
//                Toast.makeText(getApplicationContext(), state.getMessage(), Toast.LENGTH_SHORT).show();
//
//            } else if (state instanceof CollectActivityEvent.Success) {
//                Log.e(TAG, "onResume: su");
//                binding.progressBar2.setVisibility(View.GONE);
//                binding.getRoot().setEnabled(false);
//                Toast.makeText(getApplicationContext(), state.getMessage(), Toast.LENGTH_SHORT).show();
//                finish();
//            } else {
//                Log.e(TAG, "onResume: collect");
//            }
        });

        viewModel.getMessage().observe(this, s -> {
            if (!s.isEmpty())
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
        });
//
//        viewModel.getLoading().observe(this, state -> {
//            if (state) {
//                binding.progressBar2.setVisibility(View.VISIBLE);
//                binding.getRoot().setEnabled(false);
//            } else {
//                binding.progressBar2.setVisibility(View.GONE);
//                binding.getRoot().setEnabled(true);
//            }
//        });
//
//        viewModel.getResponse().observe(this, response -> {
//            if(!response.isEmpty())
//                Snackbar.make(binding.getRoot(), response, Snackbar.LENGTH_SHORT).show();
//        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
        stopBackgroundThread();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Disconnect from the device and close the connection
        // Try/Catch because Androids Bluetooth implementation may crash on some devices.
        try {
            //timermaster.cancel();
            // Unregister the receiver
            unregisterReceiver(mGattUpdateReceiver);
            // Unbind from the service so that it can shutdown properly
            unbindService(mServiceConnection);

            mBluetoothLeService.disconnect();
            mBluetoothLeService.close();
            mConnected = false;
        } catch (Exception ignore) {}

        /*
        unbindService(mServiceConnection);
        unregisterReceiver(mGattUpdateReceiver);
        mBluetoothLeService.close();
        mBluetoothLeService = null;
        mConnected = false;
        if (timeroncount == 1) {
            timermaster.cancel();
            timeroncount = 0;
        }
        if (noticount == 1) {
            //showNoti(false);
            //Notimanager.cancelAll();
        }
        Log.e("destory", "des");*/
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                // 액티비티 이동
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     ********************************
     * 블루투스 관련
     ********************************
     */
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e("CollectActivity ", "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress, false);
            Log.e("Collect Activity ", "Bluetooth ServiceConnection");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnection.setText(resourceId);
            }
        });
    }

    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
//            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            //System.out.println("mGattUpdateReceiver->onReceive->action="+action);

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                startRecordingVideo();
                updateConnectionState(R.string.connected);

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                //timermaster.cancel();
                updateConnectionState(R.string.disconnected);
                clearUI();
                mBluetoothLeService.connect(mDeviceAddress, false);

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

                // 디바이스로 부터 읽은 데이터
                String get_data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                String[] get_data_split = get_data.split("#");

                if(get_data_split.length==6) {
                    // 디바이스의 cnt
                    try {
                        int device_time = Integer.parseInt(get_data_split[0].substring(1));

                        // 가속도
                        String[] acc = get_data_split[1].split("/");
                        // 자이로
                        String[] gyro = get_data_split[2].split("/");
                        // 지자계
                        String[] mag = get_data_split[3].split("/");
                        //
                        String[] rotate = get_data_split[4].split("/");

                        /*
                         *  디비 저장 작성 필요!!!!!
                         */
                        if(device_time % 10 == 0)
                        displayData(device_time, acc, gyro, mag, rotate);
                        // 디비 저장.
                        viewModel.saveData(device_time, acc, gyro, mag, rotate, petName);
//                        viewModel.saveData(device_time, acc, gyro, mag);
                    } catch (Exception t) {
                        Log.e(TAG, "onReceive: " + t.getLocalizedMessage());
                    }
                }

            }
            else {
                Log.e("divdata", "TEETET");
            }
        }
    };

    /**
     * This is were we create the intent filter that will tell the service what we are interested in.
     * @return IntentFilter for handling the BLEService
     */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_RSSI_UPDATE);
        return intentFilter;
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();

            if (uuid.equals(SampleGattAttributes.UUID_SERVICE_SERIAL_PORT)) {
                currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
                currentServiceData.put(LIST_UUID, uuid);
                gattServiceData.add(currentServiceData);

                ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    uuid = gattCharacteristic.getUuid().toString();
                    HashMap<String, String> currentCharaData = new HashMap<String, String>();
                    charas.add(gattCharacteristic);
                    currentCharaData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                    currentCharaData.put(LIST_UUID, uuid);
                    gattCharacteristicGroupData.add(currentCharaData);
                }

                mGattCharacteristics.add(charas);
                gattCharacteristicData.add(gattCharacteristicGroupData);
            }
        }
        Log.e("displayGattServices", gattCharacteristicData.toString());

    }

    /**
     ********************************
     * view 설정
     ********************************
     */
    private void Initialize_view() {
        // Actionbar
        Toolbar toolbar = findViewById(R.id.next_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("데이터 수집 (" + mDeviceName + ")");

        // Sets up UI references.
        //((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        //mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
//        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnection = (TextView) findViewById(R.id.connection_state);
        acc_x = (TextView) findViewById(R.id.acc_x);
        acc_y = (TextView) findViewById(R.id.acc_y);
        acc_z = (TextView) findViewById(R.id.acc_z);
        gyro_x = (TextView) findViewById(R.id.gyro_x);
        gyro_y = (TextView) findViewById(R.id.gyro_y);
        gyro_z = (TextView) findViewById(R.id.gyro_z);
        mag_x = (TextView) findViewById(R.id.mag_x);
        mag_y = (TextView) findViewById(R.id.mag_y);
        mag_z = (TextView) findViewById(R.id.mag_z);
        roll = (TextView) findViewById(R.id.roll);
        pitch = (TextView) findViewById(R.id.pitch);
        yaw = (TextView) findViewById(R.id.yaw);


        // 데이터 수집 종료 버튼 설정
        Finishbutton = findViewById(R.id.button_finish);
        Finishbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //timermaster.cancel();

                    //디바이스 연결 종료
                    // Unregister the receiver
                    unregisterReceiver(mGattUpdateReceiver);
                    // Unbind from the service so that it can shutdown properly
                    unbindService(mServiceConnection);

                    mBluetoothLeService.disconnect();
                    mBluetoothLeService.close();
                    mConnected = false;

                    //비디오 연결 종료
                    stopRecordingVideo();

//                    viewModel.postData();
                } catch (Exception ignore) {}
//                finish();
            }
        });

//        //동영상 녹화버튼 설정
//            btn_rec = findViewById(R.id.button_Rec);
//            btn_rec.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    final Intent intent = new Intent(CollectActivity.this, Camera_Main.class);
//                    startActivity(intent);
//                }
//            });

    }

    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        //mDataField.setText(R.string.no_data);
        /*
        if (noticount == 1) {
            showNoti(false);
        }*/
    }
    private void displayData(int device_time, String[] acc, String[] gyro,String[] mag, String[] rotate) {

        acc_x.setText(acc[0]);
        acc_y.setText(acc[1]);
        acc_z.setText(acc[2]);

        gyro_x.setText(gyro[0]);
        gyro_y.setText(gyro[1]);
        gyro_z.setText(gyro[2]);

        mag_x.setText(mag[0]);
        mag_y.setText(mag[1]);
        mag_z.setText(mag[2]);

        roll.setText(rotate[0]);
        pitch.setText(rotate[1]);
        yaw.setText(rotate[2]);

        //if (data != null) {
        //    mDataField.setText(data);
        //}
        /*
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDataField.setText(data);
            }
        });*/
    }

    //메세지 보내기 버튼
    View.OnClickListener click_rec = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final Intent intent = new Intent(CollectActivity.this, Camera_Fragment.class);
            startActivity(intent);
        }
    };

        /*
    public void serialBegin(int baud){
        mBaudrate=baud;
        mBaudrateBuffer = "AT+CURRUART="+mBaudrate+"\r\n";
    }*/

    /**
     * Camera Function
     */

    private void openCamera(int width, int height) {

        mCameraManager = (CameraManager) getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mSemaphore.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            CameraCharacteristics cc = mCameraManager.getCameraCharacteristics(mCamId);
            StreamConfigurationMap scm = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mSensorOrientation = cc.get(CameraCharacteristics.SENSOR_ORIENTATION);
            if (scm == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }
            mVideoSize = chooseVideoSize(scm.getOutputSizes(MediaRecorder.class));
            mPreviewSize = chooseOptimalSize(scm.getOutputSizes(SurfaceTexture.class), width, height, mVideoSize);
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                binding.preview.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
                binding.preview.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }
            configureTransform(width, height);
            mMediaRecorder = new MediaRecorder();
            mCameraManager.openCamera(mCamId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException | SecurityException | NullPointerException | InterruptedException e) {
            e.printStackTrace();
            finish();
        }
    }

//    private static Size chooseVideoSize(Size[] choices) {
//        for (Size size : choices) { // 해상도에 맞게 설정하면 될듯?
//
//            Log.d("VIDEO SIZE LIST ", size.toString());
//            if(size.getWidth() >= 1920)
//                return size;
//            //if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1920) {
//            //    return size;
//            //}
//        }
//        return choices[choices.length - 1];
//    }

    private static Size chooseVideoSize(Size[] choices) {
        /*
        for (Size size : choices) { // 해상도에 맞게 설정하면 될듯?
            Log.e("VIDEO SIZE LIST", size.toString());
            //if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
            //    return size;
            //}
        }*/
        return choices[0];//[choices.length - 1];
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size ops : choices) {
            if (ops.getHeight() == ops.getWidth() * h / w && ops.getWidth() >= width && ops.getHeight() >= height) {
                bigEnough.add(ops);
            }
        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new Camera_Fragment.CompareSizesByArea());
        } else {
            return choices[0];
        }
    }    // 카메라 닫기

    private void closeCamera() {
        try {
            mSemaphore.acquire();
            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        } finally {
            mSemaphore.release();
        }
    }    //미리보기 기능

    private void startPreview() {
//        if (null == mCameraDevice || !binding.preview.isAvailable() || null == mPreviewSize) {
//            return;
//        }
        try {
            closePreviewSession();
            SurfaceTexture texture = binding.preview.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            Surface previewSurface = new Surface(texture);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCameraCaptureSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "startPreview: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mCaptureRequestBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mPreviewSize) {
            return;
        }
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getWidth(), mPreviewSize.getHeight());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) viewHeight / mPreviewSize.getHeight(), (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        this.runOnUiThread(() -> binding.preview.setTransform(matrix));
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closePreviewSession() {
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
    }

    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }    //영상녹화 설정

    private void setUpMediaRecorder() throws IOException {
        final Activity activity = this;
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
//        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
            mNextVideoAbsolutePath = getVideoFilePath();
        }
        mMediaRecorder.setProfile(CamcorderProfile.get(0, CamcorderProfile.QUALITY_1080P));
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);

//        mMediaRecorder.setVideoEncodingBitRate(10000000);
//        mMediaRecorder.setCaptureRate(30);
//        mMediaRecorder.setVideoFrameRate(30);
//        mMediaRecorder.setVideoSize(1920, 1080);
//        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (mSensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                break;
        }
        mMediaRecorder.prepare();
    }    //파일 이름 및 저장경로를 만듭니다.

    private String getVideoFilePath() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss.SSS");
        final File dir = Environment.getExternalStorageDirectory().getAbsoluteFile();
        String path = dir.getPath() + "/" + DETAIL_PATH;
        File dst = new File(path);
        if (!dst.exists()) dst.mkdirs();
        return path + dateFormat.format(System.currentTimeMillis()) +petName+ ".mp4";
    }    //녹화시작

    private void startRecordingVideo() {

        try {
            closePreviewSession();
            setUpMediaRecorder();
            SurfaceTexture texture = binding.preview.getSurfaceTexture();
            assert texture != null;
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mCaptureRequestBuilder.addTarget(previewSurface);
            Surface recordSurface = mMediaRecorder.getSurface();
            surfaces.add(recordSurface);
            mCaptureRequestBuilder.addTarget(recordSurface);
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCameraCaptureSession = session;
                    updatePreview();
                    runOnUiThread(() -> {
                        mIsRecordingVideo = true;
                        mMediaRecorder.start();
                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                }
            }, mBackgroundHandler);
//            timer();
        } catch (CameraAccessException | IOException e) {
            e.printStackTrace();
        }
    }    //녹화 중지

    private void stopRecordingVideo() {
        try {
            mIsRecordingVideo = false;
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            Activity activity = this;
            Toast.makeText(activity, "Video saved: " + mNextVideoAbsolutePath, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Video saved: " + mNextVideoAbsolutePath);
            File file = new File(mNextVideoAbsolutePath);            // 아래 코드가 없으면 갤러리 저장 적용이 안됨.
            if (!file.exists()) file.mkdir();
            getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            mNextVideoAbsolutePath = null;
            startPreview();
            //비디오 종료 후, 센서 데이터 전송 시작
            viewModel.postData();
        } catch (Exception e) {
            Log.e(TAG, "stopRecordingVideo: " + e.getLocalizedMessage());
            finish();
        }
    }    //카메라 전, 후, 광각 변경    // 본인 카메라에 맞게 적용하면 됨.

}