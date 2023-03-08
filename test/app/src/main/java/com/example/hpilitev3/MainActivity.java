package com.example.hpilitev3;

import android.annotation.SuppressLint;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.example.hpilitev3.databinding.ActivityMainBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends BluetoothLibrary {

    /**
     * view 관련
     */
    private ListView listview_device_info, listview_collect_env;
    private static Adapter_device_info listitemadapter_di;
    static Adapter_collect_env listitemadapter_ce;
    private ImageButton imagebutton_collect_env;
    private Button button_data_collect;
    private static Context context;
    private Toolbar tb;
    private ActivityMainBinding binding;
    static String Str_UserName;

    /**
     * bluetooth 관련
     */
    //static BluetoothDevice selected_device;
    static BluetoothAdapter mBluetoothAdapter;
    //static BluetoothAdapter.LeScanCallback mLeScanCallback;

    private static final int REQUEST_ENABLE_BT = 3054;
    // used to request fine location permission
    public final static int REQUEST_FINE_LOCATION = 3055;
    // scan period in milliseconds
    private static final long SCAN_PERIOD = 10000;


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        // 뷰 세팅
        Initialize_view();

        onCreateProcess();

        // Device Info 및 Collect env 리스트 뷰 어댑터 연결
        Initialize_listview_setting();
        // 데이터 수집 버튼 세팅
        Initialize_button_data_collect_start();
        //SubPage 버튼 세팅
        goToChartPage();
        // 퍼미션 체크
        checkPermissionsAll();
        // 데이터 수집 환경 체크
        Check_collect_data_env();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResultProcess(requestCode, resultCode, data);					//onActivityResult Process by BlunoLibrary
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onResumeProcess();
        binding.userName.setText(Str_UserName);
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPauseProcess();														//onPause Process by BlunoLibrary
    }

    protected void onDestroy() {
        super.onDestroy();
        onDestroyProcess();														//onDestroy Process by BlunoLibrary
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_nav_menu, menu) ;
        return true ;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_database :

                View DB_alert_view = getLayoutInflater().inflate(R.layout.dialog_db_preview, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(DB_alert_view);
                Spinner spinner = (Spinner) DB_alert_view.findViewById(R.id.DB_spinner);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                        R.array.DB_Spinner_list, android.R.layout.simple_spinner_item);
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                spinner.setAdapter(adapter);
                spinner.setSelection(0);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                        TableLayout tableLayout = (TableLayout) DB_alert_view.findViewById(R.id.DB_tablelayout);

                        String[] column_list;
                        int row_count = 5;
                        int index_count = 3;
                        if(i == 0) {
                            column_list = new String[] { "_id", "UserID", "WearDeviceID", "Date" };
                            row_count = 5;
                            index_count = 4;
                        }
                        else {
                            column_list = new String[] { " " };
                        }

                        // 제목 행 추가
                        TableRow row = new TableRow(getBaseContext());
                        //row.setPadding(3,3,3,3);
                        row.setLayoutParams(new TableRow.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                        for(int col=0; col< column_list.length; col++) {
                            TextView column = new TextView(getBaseContext());
                            column.setText(column_list[col]);
                            column.setGravity(Gravity.CENTER);
                            column.setPadding(3,3,3,3);
                            column.setBackgroundResource(R.drawable.table_title_background);
                            column.setTypeface(null, Typeface.BOLD);
                            row.addView(column);
                        }
                        tableLayout.addView(row);

                        if (tableLayout.getChildCount() >= 1)
                            tableLayout.removeViews(1, tableLayout.getChildCount()-1);

                        // 데이터 추가
                        try {
                            for(int count = 0 ; count < row_count ; count++) {
                                TableRow tableRow = new TableRow(getBaseContext());     // tablerow 생성
                                for (int index = 0; index < index_count; index++) {
                                    TextView textView = new TextView(getBaseContext());
                                    textView.setText(getDBdata(i, count, index));
                                    textView.setPadding(2,2,2,2);
                                    textView.setGravity(Gravity.CENTER);
                                    tableRow.addView(textView);        // tableRow에 view 추가
                                }
                                tableLayout.addView(tableRow);        // tableLayout에 tableRow 추가
                            }
                        }catch (Exception e) {

                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                builder.setTitle("저장된 데이터 확인");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                final AlertDialog dialog_db = builder.create();
                dialog_db.show();
                return true ;


            case R.id.action_settings :
                startActivity(new Intent(getApplicationContext(), RoomSearchActivity.class));
                return true ;
            default :
                return super.onOptionsItemSelected(item) ;
        }
    }

    private String getDBdata(int position, int row, int index) {
        switch (position) {
            case 0:
                return db_info_helper.GetRecentdata(row, index);
            default:
                return " "; // db_data_helper.get~~
        }
    }

    /**
     ********************************
     * 수집 데이터 환경 체크 (인터넷연결, 블루투스 연결, 웹캠 연결)
     ********************************
     */

    private void Check_collect_data_env() {
        // 1. 인터넷 연결 체크
        Check_internet();
        // 2. 반려동물 웨어러블 디바이스 연결 체크
        Check_bluetooth();
        // 3. 웹캠 연결
        Check_webcam();

        Check_dogname();
    }

    private void Check_internet() {
        // 인터넷 이용 가능 여부 체크
        boolean result_network = isNetworkAvailable(getApplication());
        Listitem_collect_env item_ce = (Listitem_collect_env) listitemadapter_ce.getItem(0);
        if (result_network) {
            item_ce.set_check(true);
            listitemadapter_ce.notifyDataSetChanged();
        }
        else {
            item_ce.set_check(false);
            listitemadapter_ce.notifyDataSetChanged();
        }
    }

    public Boolean isNetworkAvailable(Application application) {
        ConnectivityManager connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network nw = connectivityManager.getActiveNetwork();
            if (nw == null) return false;
            NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
            return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }
        else {
            NetworkInfo nwInfo = connectivityManager.getActiveNetworkInfo();

            return nwInfo != null && nwInfo.isConnected();
        }
    }

    static void Check_bluetooth() {
        // 블루투스 가능 여부 체크
        boolean bluetooth_check = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        Listitem_collect_env item_ce = (Listitem_collect_env) listitemadapter_ce.getItem(1);
        if (bluetooth_check && (mDeviceAddress != null)) {
            item_ce.set_check(true);
            listitemadapter_ce.notifyDataSetChanged();
        }
        else {
            item_ce.set_check(false);
            listitemadapter_ce.notifyDataSetChanged();
        }
    }


    private void Check_webcam() {
        // 일반적으로 FEATURE_CAMERA 혹은 FEATURE_CAMERA_ANY를 사용하나, 웹캠을 연결하지 않아도 true변환됨에 따라
        // FEATURE_CAMERA_CONCURRENT로 진행
        boolean camera_check = hasCamera2(context);
        Listitem_collect_env item_ce = (Listitem_collect_env) listitemadapter_ce.getItem(2);
        if (camera_check) {
            item_ce.set_check(true);
            listitemadapter_ce.notifyDataSetChanged();
        }
        else {
            item_ce.set_check(false);
            listitemadapter_ce.notifyDataSetChanged();
        }
    }

    // 카메라 유무 검사하는 함수
    private static boolean hasCamera2(Context context) {
        if (context == null) return false;
        try {
            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String[] idList = manager.getCameraIdList();
            int numberOfCameras = idList.length;

            if(numberOfCameras > 0)
                return true;
            else
                return false;
        } catch (Throwable ignore) {
            return false;
        }
    }

    private void Check_dogname() {
        if(binding.userName.getText().toString().trim().equals("")){
            binding.userName.setText(petName);
            Str_UserName = binding.userName.getText().toString().trim();
        }
        binding.userName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(binding.userName.getText().length() > 1) {
                    Str_UserName = binding.userName.getText().toString().trim();
                }
            }
        });
    }

    /**
     ********************************
     * View 설정
     ********************************
     */

    /**
     * 뷰 초기화
     */
    private void Initialize_view() {
        context = getApplicationContext();
        // 툴바 세팅
        tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb) ;

        listview_device_info = findViewById(R.id.listview_device_info);
        listview_collect_env = findViewById(R.id.listview_collect_env);
        button_data_collect = findViewById(R.id.button);
        // 이미지 버튼 (수집환경 새로고침 버튼) 세팅
        imagebutton_collect_env = findViewById(R.id.imagebutton_refresh);
        imagebutton_collect_env.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Check_collect_data_env();
            }
        });
    }

    /**
     * Device Info 및 Collect env 리스트 뷰 설정
     */
    private void Initialize_listview_setting() {
        //
        // Device Info 리스트 뷰 Setting
        //
        String ODROIDName = Settings.Global.getString(getContentResolver(), Settings.Global.DEVICE_NAME);
        String ODROIDNumber = ODROIDName.substring(ODROIDName.indexOf("ID")+2);
        listitemadapter_di = new Adapter_device_info();
        listitemadapter_di.addItem(new Listitem_device_info(0, getResources().getDrawable(R.drawable.ic_baseline_person_24),
                "사용자 ID", ("U"+ODROIDNumber)));
        listitemadapter_di.addItem(new Listitem_device_info(1, getResources().getDrawable(R.drawable.ic_baseline_watch_24),
                "반려동물 웨어러블 디바이스 정보", mDeviceName));
        listitemadapter_di.addItem(new Listitem_device_info(2, getResources().getDrawable(R.drawable.ic_baseline_perm_device_information_24),
                "데이터 수집기 (Odroid) 정보",  ODROIDName));
        listview_device_info.setAdapter(listitemadapter_di);

        //
        // Collect env 리스트 뷰 Setting
        //
        listitemadapter_ce = new Adapter_collect_env();
        listitemadapter_ce.addItem(new Listitem_collect_env(getResources().getDrawable(R.drawable.ic_baseline_network_check_24), "인터넷 연결", false));
        listitemadapter_ce.addItem(new Listitem_collect_env(getResources().getDrawable(R.drawable.ic_baseline_bluetooth_searching_24), "반려동물 웨어러블 디바이스 블루투스 연결", false));
        listitemadapter_ce.addItem(new Listitem_collect_env(getResources().getDrawable(R.drawable.ic_baseline_linked_camera_24), "웹캠 연결", false));
        listview_collect_env.setAdapter(listitemadapter_ce);
    }

    /**
     * 데이터 수집 버튼을 눌렀을 때 설정
     */

    private void Initialize_button_data_collect_start(){

        button_data_collect.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                // 데이터 수집환경이 모두다 true인지 확인

                if (listitemadapter_ce.check_env_all()) {

                    if (binding.userName.getText().toString().length() < 1) {
                        Toast.makeText(context, "댕댕이 이름 적어주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(context, "데이터 수집 시작", Toast.LENGTH_SHORT).show();

                    // 임시로 진행 (무조건 액티비티로 넘기기 위해서)
                    // DB 저장 (마지막 수집 전 세팅 기록을 INFOTABLE에 저장)
                    db_info_helper.Insert(
                            Str_UserName.trim(),
                            getString(R.string.user_id),
                            mDeviceName, mDeviceAddress,
                            getString(R.string.ODROIDNAME),
                            DeviceInfoUtil.getDeviceId(getBaseContext()),
                            System.currentTimeMillis());
                    //tring UserName, String UserID, String WearDeviceID, String OdroidID, String Mac, int Date)
                    // 액티비티 전환
                    Intent intent = new Intent(context, CollectActivity.class);
                    intent.putExtra("EXTRAS_DEVICE_NAME", mDeviceName);
                    intent.putExtra("EXTRAS_DEVICE_ADDRESS", mDeviceAddress);
                    //todo PET NAME
                    intent.putExtra("EXTRAS_DEVICE_PET_NAME", binding.userName.getText().toString().trim());
                    Log.e("EXTRAS_DEVICE_NAME", mDeviceName);
                    Log.e("EXTRAS_DEVICE_ADDRESS", mDeviceAddress);
                    startActivity(intent);

                } else
                    Toast.makeText(context, "데이터 수집 환경을 다시 확인해주세요", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void goToChartPage() {
        Button button_1 = findViewById(R.id.btn);
        button_1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                EditText editText = findViewById(R.id.user_name);
                String userName = editText.getText().toString();
                Intent intent = new Intent(context, ChartPage.class);
                intent.putExtra("user_name", userName);
                startActivity(intent);
            }
        });
    }

}