package com.example.hpilitev3;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.hpilitev3.Camera_Fragment;
import com.example.hpilitev3.R;
import com.example.hpilitev3.databinding.ActivityCameraMainBinding;

public class Camera_Main extends AppCompatActivity {
    ActivityCameraMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera_main);

        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, Camera_Fragment.newInstance()).commit();
        }
    }
}
