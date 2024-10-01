package com.ripenapps.adoreandroid.view_models

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.views.fragments.WelcomeFragmentDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor() : ViewModel(){

    fun onClick(view: View){
        when(view.id){
            R.id.welcomeStartButton->{
                view.findNavController().navigate(WelcomeFragmentDirections.welcomeToWalkthrough())
            }
            R.id.loginFromWelcome->{
                view.findNavController().navigate(WelcomeFragmentDirections.welcomeToLogin())
//                temporarily started question activity on this tap
            }
        }
    }

}