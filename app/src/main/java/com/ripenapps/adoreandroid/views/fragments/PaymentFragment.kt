package com.ripenapps.adoreandroid.views.fragments

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentPaymentBinding
import com.ripenapps.adoreandroid.utils.BaseFragment

class PaymentFragment : BaseFragment<FragmentPaymentBinding>() {
    override fun setLayout(): Int {
        return R.layout.fragment_payment
    }

    override fun initView(savedInstanceState: Bundle?) {
        onClick()
    }
    private fun onClick() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}