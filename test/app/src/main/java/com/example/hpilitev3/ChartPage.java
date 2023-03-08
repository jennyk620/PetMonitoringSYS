package com.example.hpilitev3;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ChartPage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_chart_page);

        Intent intent = getIntent();
        String userName = intent.getStringExtra("user_name");

        TextView textView = findViewById(R.id.textView);
        textView.setText(userName + "의 모니터링 결과입니다");
    }
}