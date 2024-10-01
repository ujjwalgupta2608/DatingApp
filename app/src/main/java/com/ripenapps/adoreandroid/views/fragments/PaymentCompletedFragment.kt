package com.ripenapps.adoreandroid.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentPaymentCompletedBinding
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.view_models.PlanViewModel

class PaymentCompletedFragment : BaseFragment<FragmentPaymentCompletedBinding>() {

    override fun setLayout(): Int {
        return R.layout.fragment_payment_completed
    }

    override fun initView(savedInstanceState: Bundle?) {
        onClick()
    }

    private fun onClick() {
        binding.goToHome.setOnClickListener {
            findNavController().navigate(PaymentCompletedFragmentDirections.paymentCompletedToHome())
        }
    }

}