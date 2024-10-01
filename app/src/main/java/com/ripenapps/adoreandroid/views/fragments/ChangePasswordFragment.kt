package com.ripenapps.adoreandroid.views.fragments

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentChangePasswordBinding
import com.ripenapps.adoreandroid.models.request_models.ChangePasswordRequest
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.TOKEN
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.view_models.ChangePasswordViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePasswordFragment : BaseFragment<FragmentChangePasswordBinding>() {
    val viewModel by viewModels<ChangePasswordViewModel>()
    private var isCurrentPassHidden = true
    private var isNewPassHidden = true
    private var isConfirmPassHidden = true
    override fun setLayout(): Int {
        return R.layout.fragment_change_password
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        onClick()
        setCurrentPasswordToggle()
        setNewPasswordToggle()
        setConfirmPasswordToggle()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }

    private fun setObserver() {
        viewModel.getChangePasswordLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when(it.data?.status){
                        200->{
                            it.data?.message?.let { it1 ->
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            findNavController().navigate(ChangePasswordFragmentDirections.changePasswordToHome())
//                            findNavController().popBackStack()
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


//                    binding.idLoadar.root.visibility = View.GONE

                }

                Status.LOADING -> {
//                    binding.idLoadar.root.visibility = View.VISIBLE

                }

                Status.ERROR -> {
//                    binding.idLoadar.root.visibility = View.GONE


                }

            }
        }
    }

    private fun setCurrentPasswordToggle() {
        binding.currentPassword.transformationMethod =
            CommonUtils.DotPasswordTransformationMethod
        binding.currentPasswordEye.setOnClickListener {
            isCurrentPassHidden = if (isCurrentPassHidden) {
                binding.currentPasswordEye.setImageResource(R.drawable.open_eye)
                binding.currentPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                false
            } else {
                binding.currentPasswordEye.setImageResource(R.drawable.close_eye)
                binding.currentPassword.transformationMethod =
                    CommonUtils.DotPasswordTransformationMethod
                true
            }
            binding.currentPassword.setSelection(binding.currentPassword.text.toString().length)
        }
    }
    private fun setNewPasswordToggle() {
        binding.newPassword.transformationMethod =
            CommonUtils.DotPasswordTransformationMethod
        binding.newPasswordEye.setOnClickListener {
            isNewPassHidden = if (isNewPassHidden) {
                binding.newPasswordEye.setImageResource(R.drawable.open_eye)
                binding.newPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                false
            } else {
                binding.newPasswordEye.setImageResource(R.drawable.close_eye)
                binding.newPassword.transformationMethod =
                    CommonUtils.DotPasswordTransformationMethod
                true
            }
            binding.newPassword.setSelection(binding.newPassword.text.toString().length)
        }
    }
    private fun setConfirmPasswordToggle() {
        binding.confirmPassword.transformationMethod =
            CommonUtils.DotPasswordTransformationMethod
        binding.confirmPasswordEye.setOnClickListener {
            isConfirmPassHidden = if (isConfirmPassHidden) {
                binding.confirmPasswordEye.setImageResource(R.drawable.open_eye)
                binding.confirmPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                false
            } else {
                binding.confirmPasswordEye.setImageResource(R.drawable.close_eye)
                binding.confirmPassword.transformationMethod =
                    CommonUtils.DotPasswordTransformationMethod
                true
            }
            binding.confirmPassword.setSelection(binding.confirmPassword.text.toString().length)
        }
    }

    private fun onClick() {
        binding.changePasswordButton.setOnClickListener {
            val validationError = viewModel.validateInput()

            if (validationError != null) {
                Snackbar.make(binding.changePasswordButton, validationError, Snackbar.LENGTH_SHORT).show()
            } else {
//                    just hit the change password api like in new password fragment here
                var changePasswordRequest = ChangePasswordRequest()
                changePasswordRequest.oldPassword = viewModel.currentPassword.get()
                changePasswordRequest.newPassword = viewModel.newPassword.get()
                changePasswordRequest.confirmPassword = viewModel.confirmPassword.get()
                Preferences.getStringPreference(context, TOKEN)
                    ?.let { it1 -> viewModel.hitChangePasswordApi(it1, changePasswordRequest) }
            }
        }
        binding.forgotPassword.setOnClickListener {
            findNavController().navigate(ChangePasswordFragmentDirections.changePasswordToForgotPassword("changePassword"))
        }
    }
}