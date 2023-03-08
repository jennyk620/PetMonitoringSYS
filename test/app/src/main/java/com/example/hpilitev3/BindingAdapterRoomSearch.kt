package com.example.hpilitev3

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@BindingAdapter("setLocalTime")
fun bindSetLocalTime(tv: TextView, date: LocalDateTime) {

    tv.text = date.format(DateTimeFormatter.ISO_LOCAL_TIME)

}