package com.ripenapps.adoreandroid.view_models

import android.view.View
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.views.fragments.SignupVerifyMobileFragmentDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignupVerifyMobileViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    fun onClick(view: View){
        when(view.id){
            R.id.sendOtpFromVerifyMobileSignup->{
                view.findNavController().navigate(SignupVerifyMobileFragmentDirections.signupVerifyMobileToSignupCodeVerification("mobile"))
            }
            R.id.backFromVerifyMobileSignup->{
                view.findNavController().popBackStack()
            }

        }
    }
}