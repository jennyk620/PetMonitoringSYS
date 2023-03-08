package com.example.hpilitev3;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

public class Adapter_device_info extends BaseAdapter {
    private ArrayList<Listitem_device_info> items = new ArrayList<Listitem_device_info>();

    private Context context;

    /**
     * 블루투스 관련
     */
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        context = viewGroup.getContext();
        Listitem_device_info listitem = items.get(position);

        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view =inflater.inflate(R.layout.listview_item_device_info, viewGroup, false);
        }

        // 클릭 비활성화
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        ImageButton button  = (ImageButton) view.findViewById(R.id.button_bluetooth_set); // 블루투스 연결 버튼

        if (listitem.getNum()==1) {
            button.setVisibility(view.VISIBLE);
            // 블루투스 연결 버튼 클릭했을 때
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //mLeDeviceListAdapter = new Adapter_bluetooth_device(
                    //        context, new ArrayList<BluetoothDevice>());

                    // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
                    // BluetoothAdapter through BluetoothManager.
                    // 블루투스 목록 추가
                    MainActivity.scanLeDevice(true);
                    MainActivity.mBluetoothLeService.disconnect();
                    // AlertDialog 띄우기
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("연결할 디바이스를 선택해주세요 (디바이스 스캔 중)");
                    builder.setSingleChoiceItems(MainActivity.mLeDeviceListAdapter, -1, new DialogInterface.OnClickListener() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // 블루투스 기기 스캔 멈춤
                            MainActivity.scanLeDevice(false);

                            MainActivity.mDeviceName=MainActivity.mLeDeviceListAdapter.getItem(i).getName().toString();
                            MainActivity.mDeviceAddress=MainActivity.mLeDeviceListAdapter.getItem(i).toString();
                            Toast.makeText(context, MainActivity.mDeviceName + "와 연결합니다", Toast.LENGTH_SHORT).show();

                            // 블루투스 기기 내용 바꾸기
                            items.get(1).setContents(MainActivity.mDeviceName);
                            notifyDataSetChanged();
                            MainActivity.Check_bluetooth();
                            //MainActivity.mBluetoothLeService.connect(MainActivity.mDeviceAddress, false);

                            // Dialog 닫기
                            dialogInterface.dismiss();
                        }
                    });
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            //MainActivity.mConnectionState = BluetoothLibrary.connectionStateEnum.isToScan;
                            //MainActivity.onConectionStateChange(MainActivity.mConnectionState);

                            MainActivity.scanLeDevice(false);
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            });

        }
        else
            button.setVisibility(view.INVISIBLE);

        ImageView image = (ImageView) view.findViewById(R.id.listview_item_device_info_img);
        TextView subject = (TextView) view.findViewById(R.id.listview_item_device_info_subject);
        TextView contents = (TextView) view.findViewById(R.id.listview_item_device_info_contents);

        image.setImageDrawable(listitem.getImage());
        subject.setText(listitem.getSubject());
        contents.setText(listitem.getContents());

        return view;
    }

    public void addItem(Listitem_device_info item) {
        items.add(item);
    }




}
