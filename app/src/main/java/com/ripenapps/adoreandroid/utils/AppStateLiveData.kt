package com.ripenapps.adoreandroid.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class AppStateLiveData {
    private val isForeground = MutableLiveData<Boolean>()
    fun getIsForeground(): LiveData<Boolean> {
        return isForeground
    }

    fun setForegroundState(isForeground: Boolean) {
        this.isForeground.value = isForeground
    }

    companion object {
        val instance = AppStateLiveData()
    }
}