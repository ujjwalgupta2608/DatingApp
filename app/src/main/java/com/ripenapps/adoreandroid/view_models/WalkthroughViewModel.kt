package com.ripenapps.adoreandroid.view_models

import android.view.View
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WalkthroughViewModel @Inject constructor():ViewModel() {
    fun onClick(view: View){
        when(view.id){

        }

    }
}