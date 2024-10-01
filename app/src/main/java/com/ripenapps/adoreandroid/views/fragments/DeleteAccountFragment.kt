package com.ripenapps.adoreandroid.views.fragments

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentDeleteAccountBinding
import com.ripenapps.adoreandroid.models.request_models.LoginRequest
import com.ripenapps.adoreandroid.models.response_models.CommonResponse
import com.ripenapps.adoreandroid.models.static_models.SelectGenderModel
import com.ripenapps.adoreandroid.preferences.FCM_TOKEN
import com.ripenapps.adoreandroid.preferences.IS_WELCOME_DONE
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.USER_ID
import com.ripenapps.adoreandroid.preferences.USER_NAME
import com.ripenapps.adoreandroid.repository.ApiRepository
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.Resources
import com.ripenapps.adoreandroid.utils.SingleLiveEvent
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.view_models.LoginViewModel
import com.ripenapps.adoreandroid.views.activities.MainActivity
import com.ripenapps.adoreandroid.views.adapters.AdapterDeleteReasons
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.IOException

@AndroidEntryPoint
class DeleteAccountFragment : BaseFragment<FragmentDeleteAccountBinding>() {
    val loginViewModel by viewModels<LoginViewModel>()
    var list = mutableListOf<SelectGenderModel>()
    var selectedPosition=0
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private var isPassHidden = true
    private var deleteAccountLiveData= SingleLiveEvent<Resources<CommonResponse>>()

    override fun setLayout(): Int {
        return R.layout.fragment_delete_account
    }


    fun hitDeleteAccountApi(token:String/*, request: DeleteRequestModel*/){
        try {
            deleteAccountLiveData.postValue(Resources.loading(null))
            lifecycleScope.launch {
                try {
                    deleteAccountLiveData.postValue(
                        Resources.success(
                            ApiRepository().deleteAccountApi(
                                "Bearer $token"/*, request*/
                            )
                        )
                    )
                }catch (ex: IOException) {
                    ex.printStackTrace()
                    deleteAccountLiveData.postValue(Resources.error(getString(R.string.unable_to_connect_please_check_your_internet), null))

                } catch (ex: Exception) {
                    deleteAccountLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }
    override fun initView(savedInstanceState: Bundle?) {
        onClick()
        bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        initDeleteRecycler()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }

    private fun setObserver() {
        deleteAccountLiveData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when(it.data?.status){  //flow is complete till password verification, only delete api is not hitting properly
                        200->{
                            it.data?.message?.let { it1 ->
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            Preferences.removeAllPreference(requireContext())
                            Preferences.setStringPreference(requireContext(), IS_WELCOME_DONE, "true")
                            val intent = Intent(requireContext(), MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        else->{
                            it.data?.message?.let { it1 ->
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    }
                }

                Status.LOADING -> {}

                Status.ERROR -> {}
            }
        }
        loginViewModel.getLoginLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.message?.let { it1 ->
                        when (it.data?.status) {
                            "200" -> {
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()
                                Preferences.getStringPreference(requireContext(), USER_ID)
                                    ?.let { it2 -> hitDeleteAccountApi(it2, /*DeleteRequestModel( reason= if(selectedPosition<list.size-1) list[selectedPosition].name else binding.otherReason.text.toString().trim() )*/) }
                            }

                            else -> {
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    }
                }

                Status.LOADING -> {}

                Status.ERROR -> {
                    it.message?.let { it1 ->
                        Toast.makeText(
                            requireActivity(),
                            it1,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }
        }
    }

    private fun onClick() {
        binding.deleteButton.setOnClickListener {
            openDeleteBottomsheet()
        }
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun openDeleteBottomsheet() {
        val view1: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottomsheet_delete_account, null)
        bottomSheetDialog.setContentView(view1)
        bottomSheetDialog.show()
        val passowrd = view1.findViewById<View>(R.id.password) as EditText
        val passwordEye = view1.findViewById<View>(R.id.password_eye) as ImageView
        val deleteButton = view1.findViewById<View>(R.id.deleteButton) as AppCompatTextView
        val forgotPassword = view1.findViewById<View>(R.id.forgotPassword) as TextView
        passowrd.transformationMethod =
            CommonUtils.DotPasswordTransformationMethod
        isPassHidden=true
//        set click actions of bottomsheet
        passowrd.setOnClickListener {

        }
        passwordEye.setOnClickListener {
            isPassHidden = if (isPassHidden) {
                passwordEye.setImageResource(R.drawable.open_eye)
                passowrd.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                false
            } else {
                passwordEye.setImageResource(R.drawable.close_eye)
                passowrd.transformationMethod =
                    CommonUtils.DotPasswordTransformationMethod
                true
            }
            passowrd.setSelection(passowrd.text.toString().length)
        }
        deleteButton.setOnClickListener {
            bottomSheetDialog.dismiss()
            val validationError = validateInput(passowrd.text.toString().trim())

            if (validationError != null) {
                Toast.makeText(requireContext(), validationError, Toast.LENGTH_SHORT).show()
            } else {
                var loginRequest = LoginRequest()
                loginRequest.userName =
                    Preferences.getStringPreference(requireContext(), USER_NAME).toString()
                loginRequest.password = passowrd.text.toString().trim()
//                    update device token later.
                loginRequest.deviceToken = Preferences.getStringPreference(requireContext(), FCM_TOKEN).toString()
                loginRequest.deviceType = "android"
                Log.i("TAG", "openDeleteBottomsheet: "+loginRequest)
                loginViewModel.hitUserLoginApi(loginRequest)
            }
        }
        forgotPassword.setOnClickListener {
            bottomSheetDialog.dismiss()
            findNavController().navigate(DeleteAccountFragmentDirections.deleteAccountToForgotPassword("deleteAccount"))
        }
    }

    fun validateInput(password:String): String? {
        val passwordValue = password

        if (passwordValue.isNullOrEmpty()) {
            return getString(R.string.please_enter_a_password)
        }

        if (passwordValue.length < 8) {
            return getString(R.string.password_length_can_not_be_less)
        }
        if (selectedPosition==list.size-1&&binding.otherReason.text?.trim().isNullOrEmpty())
            return getString(R.string.please_enter_the_reason)

        return null // No validation error
    }
    private fun initDeleteRecycler() {
//        by default first one is selected
        list.addAll(
            listOf(
                SelectGenderModel("I Couldn’t find what i wanted.", true),
                SelectGenderModel("The app was not what i expected."),
                SelectGenderModel("I”m busy at the moment."),
                SelectGenderModel("Other: Please specify.")
            )
        )
        binding.deleteReasonRecycler.adapter = AdapterDeleteReasons(list, ::getSelectedPosition)
    }
    private fun getSelectedPosition(position:Int){
        selectedPosition=position
        binding.otherReason.isVisible = position==list.size-1
    }
}