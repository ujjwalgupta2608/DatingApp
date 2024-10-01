package com.ripenapps.adoreandroid.views.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.SimpleItemAnimator
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.DialogSubscriptionBinding
import com.ripenapps.adoreandroid.databinding.FragmentLikeBinding
import com.ripenapps.adoreandroid.models.OptionsList
import com.ripenapps.adoreandroid.preferences.*
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.ProcessDialog
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.view_models.LikeViewModel
import com.ripenapps.adoreandroid.view_models.PlanViewModel
import com.ripenapps.adoreandroid.views.activities.MainActivity
import com.ripenapps.adoreandroid.views.adapters.AdapterSearchSpecificUsers
import com.ripenapps.adoreandroid.views.adapters.AdapterUsersListInLike
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LikeFragment : BaseFragment<FragmentLikeBinding>() {
    private val likeViewModel by viewModels<LikeViewModel>()
    var usersTypeList = arrayListOf<OptionsList>()
    lateinit var adapterUsersList: AdapterUsersListInLike
    lateinit var adapterSearchUsers: AdapterSearchSpecificUsers
    private val planViewModel by viewModels<PlanViewModel>()
    private var usersType = 0
    override fun setLayout(): Int {
        return R.layout.fragment_like
    }

    override fun initView(savedInstanceState: Bundle?) {
        usersTypeList.addAll(
            listOf(
                OptionsList(getString(R.string.all), true),
                OptionsList(getString(R.string.my_connection), false),
                OptionsList(getString(R.string.pending), false),
                OptionsList(getString(R.string.requests), false),
                OptionsList(getString(R.string.dislike), false)
            )
        )
        (binding.usersTypeRecycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false
        adapterSearchUsers = AdapterSearchSpecificUsers(usersTypeList, ::getUsersType)
        binding.usersTypeRecycler.adapter = adapterSearchUsers
//        initialize adapterUsersList for all
        adapterUsersList = AdapterUsersListInLike(mutableListOf(), ::getSelectedUser, ::openSubscriptionPopup, usersType)
        binding.usersListRecycler.adapter = adapterUsersList
        onClick()
    }
    private fun openSubscriptionPopup(){
        Preferences.getStringPreference(requireContext(), TOKEN)
            ?.let { planViewModel.hitPlanListApi(it) }
        showGetSubscriptionDialog()
    }
    private fun showGetSubscriptionDialog() {
        /*var dialog= Dialog(requireContext())
        val dialogBinding: DialogSubscriptionBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_subscription,
            null,
            false
        )!!
        dialog?.let {
            it.setContentView(dialogBinding.root)
            it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.setCancelable(false)
            it.show()
        }

        dialogBinding.isFreeSelected=true
        dialogBinding.crossButton.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.noThanks.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.freeOuterLayout.setOnClickListener {
            if (dialogBinding.isFreeSelected==false){
                dialogBinding.isFreeSelected=true
            }
        }
        dialogBinding.popularOuterLayout.setOnClickListener {
            if (dialogBinding.isFreeSelected==true){
                dialogBinding.isFreeSelected=false
            }
        }*/

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }

    override fun onResume() {
        super.onResume()
        setLocation()
        when (usersType) {
            0 -> {
                hitGetConnectionApi("all")
            }

            1 -> {
                hitGetConnectionApi("myConnection")
            }

            2 -> {
                hitGetConnectionApi("pending")
            }

            3 -> {
                hitGetConnectionApi("requests")
            }

            4 -> {
                hitGetConnectionApi("dislike")
            }
        }    }
    private fun setObserver() {
        planViewModel.getPlanListLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.message?.let { it1 ->
                        when (it.data?.status) {
                            200 -> {
                                var dialog= Dialog(requireContext())
                                val dialogBinding: DialogSubscriptionBinding = DataBindingUtil.inflate(
                                    LayoutInflater.from(context),
                                    R.layout.dialog_subscription,
                                    null,
                                    false
                                )!!
                                dialog?.let {
                                    it.setContentView(dialogBinding.root)
                                    it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                    it.setCancelable(false)
                                    it.show()
                                }
                                dialogBinding.isFreeSelected=true
                                if(!Preferences.getStringPreference(requireContext(), PLAN_AMOUNT).isNullOrEmpty()){
                                    dialogBinding.subscriptionAmount=/*it.data.data?.plan?.get(1)?.discountPrice.toString()*/Preferences.getStringPreference(requireContext(), PLAN_AMOUNT)
                                }else{
                                    dialogBinding.subscriptionAmount=it.data.data?.plan?.get(1)?.discountPrice.toString()
                                }
                                dialogBinding.crossButton.setOnClickListener {
                                    dialog.dismiss()
                                }
                                dialogBinding.noThanks.setOnClickListener {
                                    dialog.dismiss()
                                }
                                dialogBinding.freeOuterLayout.setOnClickListener {
                                    if (dialogBinding.isFreeSelected==false){
                                        dialogBinding.isFreeSelected=true
                                    }
                                }
                                dialogBinding.popularOuterLayout.setOnClickListener {
                                    if (dialogBinding.isFreeSelected==true){
                                        dialogBinding.isFreeSelected=false
                                    }
                                }
                                dialogBinding.subscribeNowButton.setOnClickListener {
                                    dialog.dismiss()
                                    findNavController().navigate(LikeFragmentDirections.likeToChoosePlan())
                                }
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
                    ProcessDialog.dismissDialog(true)
                }

                Status.LOADING -> {
                    ProcessDialog.showDialog(requireActivity(), true)
                }

                Status.ERROR -> {
                    Log.e("TAG", "initViewModel: ${it.message}")
                    it.message?.let { it1 ->
                        Toast.makeText(
                            requireActivity(),
                            it1,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    ProcessDialog.dismissDialog(true)
                }

            }
        }
        likeViewModel.getConnectionListLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            adapterUsersList.updateUsersList(it.data?.data?.listing, usersType)
                            binding.noUserLayout.isVisible = it.data?.data?.listing?.size!! <= 0
                        }
                        403->{
                            it.data.message?.let { it1 ->
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            UserPreference.clear()
                            UserPreference.deviceToken = Preferences.getStringPreference(requireContext(), FCM_TOKEN)!!
                            Preferences.removeAllPreference(requireContext())
                            Preferences.setStringPreference(requireContext(), FCM_TOKEN, UserPreference.deviceToken)
                            Preferences.setStringPreference(requireContext(), IS_WELCOME_DONE, "true")
                            val intent = Intent(requireContext(), MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        else -> {
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
                    ProcessDialog.dismissDialog(true)
                }

                Status.LOADING -> {
                    ProcessDialog.showDialog(requireActivity(), true)
//                    binding.idLoadar.root.visibility = View.VISIBLE

                }

                Status.ERROR -> {
                    it.message?.let { it1 ->
                        Toast.makeText(
                            requireActivity(),
                            it1,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    ProcessDialog.dismissDialog(true)
//                    binding.idLoadar.root.visibility = View.GONE
                }

            }
        }
    }

    override fun onPause() {
        super.onPause()
        ProcessDialog.dismissDialog(true)
    }
    private fun onClick() {
        binding.notification.setOnClickListener {
            findNavController().navigate(LikeFragmentDirections.likeToNotification())
//            findNavController().navigate(LikeFragmentDirections.likeToLikesMatch())
        }
        binding.locationText.setOnClickListener {
            findNavController().navigate(LikeFragmentDirections.likeToSearchLocation())
        }
    }
    private fun setLocation() {
        binding.locationText.text = Preferences.getStringPreference(requireContext(), CITY)+", "+Preferences.getStringPreference(requireContext(), COUNTRY)
    }
    fun hitGetConnectionApi(status: String){
        Preferences.getStringPreference(requireContext(), TOKEN)
            ?.let { likeViewModel.hitConnectionListApi(it, status = status) }
    }
    private fun getSelectedUser(position: Int, userId:String) {
        findNavController().navigate(
            LikeFragmentDirections.likeToUserDetail(
                usersType.toString(),
                previousScreen = "like",
                userId = userId
            )
        )
    }

    private fun getUsersType(position: Int) {
        usersType = position
        when (position) {
            0 -> {
                hitGetConnectionApi("all")
            }

            1 -> {
                hitGetConnectionApi("myConnection")
            }

            2 -> {
                hitGetConnectionApi("pending")
            }

            3 -> {
                hitGetConnectionApi("requests")
            }

            4 -> {
                hitGetConnectionApi("dislike")
            }
        }
    }
}