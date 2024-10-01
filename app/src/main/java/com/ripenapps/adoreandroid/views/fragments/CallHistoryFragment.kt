package com.ripenapps.adoreandroid.views.fragments

import android.app.Dialog
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
import com.ripenapps.adoreandroid.databinding.FragmentCallHistoryBinding
import com.ripenapps.adoreandroid.models.response_models.notificationlist.NotificationData
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.TOKEN
import com.ripenapps.adoreandroid.preferences.USER_ID
import com.ripenapps.adoreandroid.utils.AppDialogListener
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.ProcessDialog
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.utils.SwipeHelper
import com.ripenapps.adoreandroid.utils.createYesNoDialog
import com.ripenapps.adoreandroid.view_models.HomeViewModel
import com.ripenapps.adoreandroid.views.adapters.AdapterCallHistory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CallHistoryFragment : BaseFragment<FragmentCallHistoryBinding>() {
    private var profileImage = ""
    private var profileName = ""
    private var isCompleteChatCleared = false
    lateinit var adapterCallHistory: AdapterCallHistory
    private var notificationList: MutableList<NotificationData>? = mutableListOf()
    private val homeViewModel by viewModels<HomeViewModel>()
    private var isLoading = false   //avoid hitting api again till it's response is retrieved successfully
    private var currentPage = 1
    private var userId = ""
    private var isEmptyListReceived = false   //avoid hitting api after all the pages(complete list) are retrieved
    private var roomId=""

    override fun setLayout(): Int {
        return R.layout.fragment_call_history
    }

    override fun initView(savedInstanceState: Bundle?) {
        Log.i("TAG", "initView:  saved id" + Preferences.getStringPreference(context, USER_ID))
        onClick()
        profileName = CallHistoryFragmentArgs.fromBundle(requireArguments()).name
        profileImage = CallHistoryFragmentArgs.fromBundle(requireArguments()).profile
        userId = CallHistoryFragmentArgs.fromBundle(requireArguments()).userId
        roomId = CallHistoryFragmentArgs.fromBundle(requireArguments()).roomId

        hitNotificationApi(currentPage)
        setPagination()
        Log.i("TAG", "initView123: " + userId)
//        initNotificationRecycler()
    }

    private fun setPagination() {
        binding.callHistoryRecycler.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (layoutManager!!.findLastVisibleItemPosition() == adapterCallHistory.itemCount - 3) {
                    if (isLoading && !isEmptyListReceived) {
                        currentPage++
                        hitNotificationApi(currentPage)
                    }
                    isLoading = false
                }
            }
        })
        initNotificationRecycler()
    }

    private fun hitNotificationApi(page: Int) {
        Preferences.getStringPreference(requireContext(), TOKEN)
            ?.let {
                homeViewModel.hitNotificationListApi(
                    it,
                    "Call",
                    page.toString(),
                    "40",
                    userId
                )
            }
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
                            if (isCompleteChatCleared) {
                                isCompleteChatCleared = false
                                findNavController().popBackStack()
                            } else {
                                currentPage = 1
                                hitNotificationApi(currentPage)
                                Log.i("TAG", "getNotificationListLiveData: " + it.data)
                                it.message?.let { it1 ->
                                    Toast.makeText(
                                        requireActivity(),
                                        it1,
                                        Toast.LENGTH_SHORT
                                    ).show()
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
        homeViewModel.getNotificationListLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            isLoading = true
                            if (it.data.data?.list?.size!! <= 0) {
                                isEmptyListReceived = true
                            }
                            if (currentPage == 1 && (notificationList?.size ?: 0) > 0) {
                                adapterCallHistory.clearList()
                            }
                            var tempList:MutableList<NotificationData> = mutableListOf()

                            it.data.data.list.forEach { item ->
                                if (item.message == "Incoming call") {
                                    tempList?.add(item)
                                }
                            }
                            Log.i("TAG", "setObserver123: " + tempList?.size)
                            if (currentPage == 1 && (tempList?.size ?: 0) > 0) {
                                adapterCallHistory.updateList(tempList!!)
                            } else {
                                adapterCallHistory.updateList(tempList!!)
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
        binding.deleteCallHistory.setOnClickListener {
            createYesNoDialog(
                object : AppDialogListener {
                    override fun onPositiveButtonClickListener(dialog: Dialog) {
                        Preferences.getStringPreference(requireContext(), TOKEN)
                            ?.let {
                                Preferences.getStringPreference(requireContext(), TOKEN)
                                    ?.let { it1 ->
                                        homeViewModel.hitDeleteNotificationApi(
                                            it1,
                                            "",
                                            "clearVideo",
                                            userId,
                                            roomId,
                                            ""
                                        )
                                    }
                            }
                        isCompleteChatCleared = true
                        dialog.dismiss()
                    }

                    override fun onNegativeButtonClickListener(dialog: Dialog) {
                        dialog.dismiss()
                    }
                },
                requireContext(),
                "${getString(R.string.call_history)}!",
                getString(R.string.do_you_really_want_to_clear_the_call_history),
                getString(R.string.yes),
                getString(R.string.no)
            )
        }
    }

    private fun initNotificationRecycler() {
        adapterCallHistory = AdapterCallHistory(notificationList!!, profileName, profileImage)
        binding.callHistoryRecycler.adapter = adapterCallHistory
        object : SwipeHelper(requireContext(), binding.callHistoryRecycler) {
            override fun instantiateUnderlayButton(
                viewHolder: RecyclerView.ViewHolder,
                underlayButtons: MutableList<UnderlayButton>,
                position: Int
            ) {
                underlayButtons.add(UnderlayButton(
                    requireContext(),
                    "",
                    R.drawable.clear_chat_image,
                    Color.parseColor("#FFFFFFFF")
                ) {
                    Preferences.getStringPreference(requireContext(), TOKEN)?.let { it1 ->
                        homeViewModel.hitDeleteNotificationApi(
                            it1,
                            adapterCallHistory?.getItem(position)?._id!!,
                            "normalDelete",
                            "",
                            roomId,
                            adapterCallHistory?.getItem(position)?.chatId!!
                        )
                    }
                })
            }
        }
    }
}