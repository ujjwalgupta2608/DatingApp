package com.ripenapps.adoreandroid.views.fragments

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentNewPasswordBinding
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.ProcessDialog
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.view_models.NewPasswordViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewPasswordFragment : BaseFragment<FragmentNewPasswordBinding>() {
    private var emailNumberCheck=""
    private var isPassHidden = true
    private var isConfirmPassHidden = true
    var previousScreen=""

    val viewModel by viewModels<NewPasswordViewModel>()
    override fun setLayout(): Int {
        return R.layout.fragment_new_password
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()

    }

    private fun setObserver() {
        viewModel.getResetPasswordLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when(it.data?.status){
                        "200"->{
                            it.data?.message?.let { it1 ->
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            if (previousScreen=="login"){
                                findNavController()
                                    .navigate(NewPasswordFragmentDirections.newPasswordToLogin())
                            } else if (previousScreen=="changePassword"){
//                                findNavController().popBackStack()
//                                findNavController().navigate(NewPasswordFragmentDirections.newPasswordToChangePassword())
                                findNavController().navigate(NewPasswordFragmentDirections.newPasswordToHome())
                            }else if (previousScreen=="settings"){
                                findNavController().navigate(NewPasswordFragmentDirections.newPasswordToSettings())
                            }
//                            else if (previousScreen=="deleteAccount"){
//                                findNavController().navigate(NewPasswordFragmentDirections.newPasswordToDeleteAccount())
//                            }

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
                    ProcessDialog.dismissDialog(true)
//                    binding.idLoadar.root.visibility = View.GONE

                }

                Status.LOADING -> {
//                    binding.idLoadar.root.visibility = View.VISIBLE
                    ProcessDialog.showDialog(requireActivity(), true)
                }

                Status.ERROR -> {
//                    binding.idLoadar.root.visibility = View.GONE
                    ProcessDialog.dismissDialog(true)
                }

            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        previousScreen = NewPasswordFragmentArgs.fromBundle(requireArguments()).previousScreen
        emailNumberCheck = NewPasswordFragmentArgs.fromBundle(requireArguments()).emailNumberCheck
        binding.viewModel = viewModel
        setPasswordToggle()
        setConfirmPasswordToggle()

    }
    private fun setPasswordToggle() {
        binding.password.transformationMethod =
            CommonUtils.DotPasswordTransformationMethod
        binding.passwordEye.setOnClickListener {
            isPassHidden = if (isPassHidden) {
                binding.passwordEye.setImageResource(R.drawable.open_eye)
                binding.password.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                false
            } else {
                binding.passwordEye.setImageResource(R.drawable.close_eye)
                binding.password.transformationMethod =
                    CommonUtils.DotPasswordTransformationMethod
                true
            }
            binding.password.setSelection(binding.password.text.toString().length)
        }
    }
    private fun setConfirmPasswordToggle() {
        binding.confirmPassword.transformationMethod =
            CommonUtils.DotPasswordTransformationMethod
        binding.passwordEye2.setOnClickListener {
            isConfirmPassHidden = if (isConfirmPassHidden) {
                binding.passwordEye2.setImageResource(R.drawable.open_eye)
                binding.confirmPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                false
            } else {
                binding.passwordEye2.setImageResource(R.drawable.close_eye)
                binding.confirmPassword.transformationMethod =
                    CommonUtils.DotPasswordTransformationMethod
                true
            }
            binding.confirmPassword.setSelection(binding.confirmPassword.text.toString().length)
        }
    }

}