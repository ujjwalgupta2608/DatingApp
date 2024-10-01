package com.ripenapps.adoreandroid.views.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentNotificationBinding
import com.ripenapps.adoreandroid.models.response_models.notificationlist.NotificationData
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.TOKEN
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.ProcessDialog
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.utils.SwipeHelper
import com.ripenapps.adoreandroid.view_models.HomeViewModel
import com.ripenapps.adoreandroid.views.adapters.AdapterNotification
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationFragment : BaseFragment<FragmentNotificationBinding>() {
    private lateinit var adapterNotification: AdapterNotification
    var notificationList: MutableList<NotificationData>? = mutableListOf()
    private val homeViewModel by viewModels<HomeViewModel>()
    private var isLoading = false
    private var currentPage=1
    override fun setLayout(): Int {
        return R.layout.fragment_notification
    }

    override fun initView(savedInstanceState: Bundle?) {
        onClick()
        hitNotificationApi(currentPage)
        setPagination()
    }

    private fun setPagination() {
        binding.notificationRecycler.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (layoutManager!!.findLastVisibleItemPosition() == adapterNotification.itemCount - 3) {
                    if (isLoading) {
                        currentPage++
                        hitNotificationApi(currentPage)
                    }
                    isLoading = false
                }
            }
        })
        initNotificationRecycler()
    }

    private fun initNotificationRecycler() {
        adapterNotification = AdapterNotification(::getSelectedNotification, ArrayList())
        binding.notificationRecycler.adapter = adapterNotification
        object : SwipeHelper(requireContext(), binding.notificationRecycler) {
            override fun instantiateUnderlayButton(
                viewHolder: RecyclerView.ViewHolder,
                underlayButtons: MutableList<UnderlayButton>,
                position: Int
            ) {
                underlayButtons.add(UnderlayButton(
                    requireContext(),
                    "",
                    R.drawable.clear_icon,
                    Color.parseColor("#FFFFFFFF")
                ) {
                    Preferences.getStringPreference(requireContext(), TOKEN)?.let { it1 ->
                        homeViewModel.hitDeleteNotificationApi(
                            it1,
                            adapterNotification?.getItem(position)?._id!!,
                            "normalDelete",
                            "",
                            "",
                            ""
                        )
                    }
                })
            }
        }
    }

    private fun hitNotificationApi(page: Int) {
        Preferences.getStringPreference(requireContext(), TOKEN)
            ?.let { homeViewModel.hitNotificationListApi(it, "normal", page.toString(), "15", "") }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()

    }

    private fun setObserver() {
        homeViewModel.getDeleteNotificationLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            currentPage=1
                            hitNotificationApi(currentPage)
                            Log.i("TAG", "getNotificationListLiveData: "+it.data)
                            it.message?.let { it1 ->
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        else -> {}
                    }
//                    binding.idLoadar.root.visibility = View.GONE
                    ProcessDialog.dismissDialog(true)
                }

                Status.LOADING -> {
//                    binding.idLoadar.root.visibility = View.VISIBLE
                    ProcessDialog.showDialog(requireActivity(), true)
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
        homeViewModel.getNotificationListLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            if (it.data.data?.list!!.isNotEmpty()) {
                                isLoading = true
                                notificationList = it.data.data.list
                                if (currentPage == 1 && notificationList?.size!! > 0) {
                                   adapterNotification.clearList()
                                    adapterNotification.updateList(notificationList!!)
                                } else {
                                    adapterNotification.updateList(notificationList!!)
                                }
                            }

                        }

                        else -> {}
                    }
                    ProcessDialog.dismissDialog(true)
                }

                Status.LOADING -> {
                    ProcessDialog.showDialog(requireActivity(), true)
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
                }

            }
        }
    }

    private fun onClick() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }
    private fun getSelectedNotification(position:Int){
        if (adapterNotification.getItem(position)?.notificationType =="card"){
            findNavController().navigate(NotificationFragmentDirections.notificationToUserDetail("0","notification", adapterNotification?.getItem(position)?.sender?._id!!))
        }else if (adapterNotification?.getItem(position)?.notificationType =="chat"){
            findNavController().navigate(NotificationFragmentDirections.notificationToSpecificChat("notification",
                adapterNotification?.getItem(position)?.roomId!!, adapterNotification?.getItem(position)?.sender?._id!!, adapterNotification?.getItem(position)?.sender?.name!!, adapterNotification?.getItem(position)?.sender?.profileUrl!!))
        }
    }

}