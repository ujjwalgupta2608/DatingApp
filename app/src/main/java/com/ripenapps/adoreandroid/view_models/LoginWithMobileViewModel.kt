package com.ripenapps.adoreandroid.view_models

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.ripenapps.adoreandroid.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginWithMobileViewModel @Inject constructor():ViewModel() {
    fun onClick(view: View){
        when(view.id){
            R.id.loginWithEmail->{
                view.findNavController().popBackStack()
            }
//            R.id.sendOtpFromLoginWithMobile->{
//                view.findNavController().navigate(LoginWithMobileFragmentDirections.loginWithMobileToLoginCodeVerification("loginWithMobile"))
//            }
            R.id.backFromLoginWithMobile->{
                view.findNavController().popBackStack()
            }
        }
    }
}