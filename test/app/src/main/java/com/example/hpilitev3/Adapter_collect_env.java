package com.example.hpilitev3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;

public class Adapter_collect_env extends BaseAdapter {
    ArrayList<Listitem_collect_env> items = new ArrayList<Listitem_collect_env>();
    Context context;
    ImageView image;
    TextView subject;
    MaterialButtonToggleGroup materialButtonToggleGroup;
    MaterialButton button_off, button_on;

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
        Listitem_collect_env listitem = items.get(position);

        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view =inflater.inflate(R.layout.listview_item_collect_env, viewGroup, false);
        }

        // 클릭 비활성화
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        image = (ImageView) view.findViewById(R.id.listview_item_collect_env_image);
        subject = (TextView) view.findViewById(R.id.listview_item_collect_env_subject);
        materialButtonToggleGroup = (MaterialButtonToggleGroup) view.findViewById(R.id.listview_item_collect_env_toggle);
        button_off = (MaterialButton) view.findViewById(R.id.button_off);
        button_on = (MaterialButton) view.findViewById(R.id.button_on);

        image.setImageDrawable(listitem.getImage());
        subject.setText(listitem.getSubject());

        materialButtonToggleGroup.clearChecked();
        if (listitem.get_check()==true)
            button_on.setChecked(true);
        else
            button_off.setChecked(true);
        return view;
    }

    public void addItem(Listitem_collect_env item) {
        items.add(item);
    }

    public boolean check_env_all() {
        // 모두다 true이면 true 반환
        // 아니면 수집환경 문제가 있다고 생각하고 false 반환
        boolean check_1 = items.get(0).get_check();
        boolean check_2 = items.get(1).get_check();
        boolean check_3 = items.get(2).get_check();

        if(check_1 && check_2 && check_3 == true)
            return true;
        else return false;
    }

}
