package com.example.hpilitev3

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hpilitev3.databinding.ItemRoomSearchBinding
import com.example.hpilitev3.data.db.Sensor

/**
 * Created by Jaehyeon on 2022/09/26.
 */
class RoomSearchAdapter: RecyclerView.Adapter<RoomSearchAdapter.RoomSearchViewHolder>() {

    private val list = arrayListOf<Sensor>()

    inner class RoomSearchViewHolder(private val binding: ItemRoomSearchBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(sensor: Sensor, position: Int) {
            binding.sensor = sensor
            binding.position = position
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomSearchViewHolder =
        RoomSearchViewHolder(ItemRoomSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RoomSearchViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

    override fun getItemCount(): Int = list.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(_list: List<Sensor>) {
        list.clear()
        list.addAll(_list)
        notifyDataSetChanged()
    }
}