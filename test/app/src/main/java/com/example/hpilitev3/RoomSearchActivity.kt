package com.example.hpilitev3

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hpilitev3.databinding.ActivityRoomSerachBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

/**
 * Created by Jaehyeon on 2022/09/26.
 */
@AndroidEntryPoint
class RoomSearchActivity: AppCompatActivity() {

    private lateinit var binding: ActivityRoomSerachBinding
    private val viewModel: RoomSearchViewModel by viewModels()
    private val adapter = RoomSearchAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_room_serach)
        viewModel.getDatabaseData()
        with(binding.rv) {
            layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
            adapter = this@RoomSearchActivity.adapter
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launchWhenStarted {
            viewModel.data.collectLatest {
                adapter.updateData(it)
            }
        }
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        finish()
    }
}