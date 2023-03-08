package com.example.hpilitev3.presentation.event

sealed class CollectActivityEvent(
    val message: String = "",
) {

    class Loading(message: String): CollectActivityEvent(message)
    class Success(message: String): CollectActivityEvent(message)
    object Collect: CollectActivityEvent()

}