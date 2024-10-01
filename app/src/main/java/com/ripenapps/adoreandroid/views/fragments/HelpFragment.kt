package com.ripenapps.adoreandroid.views.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentHelpBinding
import com.ripenapps.adoreandroid.models.request_models.SendMessageRequest
import com.ripenapps.adoreandroid.preferences.FCM_TOKEN
import com.ripenapps.adoreandroid.preferences.IS_WELCOME_DONE
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.TOKEN
import com.ripenapps.adoreandroid.preferences.USER_ID
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.view_models.HelpViewModel
import com.ripenapps.adoreandroid.views.activities.MainActivity
import com.ripenapps.adoreandroid.views.adapters.AdapterHelp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HelpFragment : BaseFragment<FragmentHelpBinding>() {
    lateinit var adapterHelp: AdapterHelp
    val helpViewModel by viewModels<HelpViewModel>()
    var dateToShow=""

    override fun setLayout(): Int {
        return R.layout.fragment_help
    }

    override fun initView(savedInstanceState: Bundle?) {
        onClick()
        Preferences.getStringPreference(requireContext(), TOKEN)
            ?.let { helpViewModel.hitHelpChatListApi(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CommonUtils.showKeyBoard(requireActivity(), binding.chatText)
        setObserver()
    }

    private fun setObserver() {
        helpViewModel.getHelpChatListLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.message?.let { it1 ->
                        when (it.data?.status) {
                            200 -> {
                                it.data?.data?.chatMessages?.forEach { chatMessage ->
                                    if (CommonUtils.convertTimestampToAndroidTime(chatMessage.createdAt!!, "dd MMM yyyy")!=dateToShow){
                                        dateToShow= CommonUtils.convertTimestampToAndroidTime(chatMessage.createdAt!!, "dd MMM yyyy")
                                        if (dateToShow== CommonUtils.getFormattedDateToday("dd MMM yyyy")){
                                            chatMessage.dateToShow=getString(R.string.today)
                                        } else if (dateToShow== CommonUtils.getFormattedDateYesterday("dd MMM yyyy")){
                                            chatMessage.dateToShow=getString(R.string.yesterday)
                                        } else{
                                            chatMessage.dateToShow=dateToShow
                                        }
                                    }
                                }
                                adapterHelp=AdapterHelp(it.data?.data?.chatMessages, Preferences.getStringPreference(requireContext(), USER_ID), ::closeKeyboard)
                                binding.helpRecycler.adapter=adapterHelp
                                if (it.data?.data?.chatMessages?.size!! >1){
                                    binding.helpRecycler.smoothScrollToPosition(it.data?.data?.chatMessages?.size!!-1)
                                }
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
                    Log.e("TAG", "initViewModel: ${it.message}")
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
        helpViewModel.getSendMessageLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.message?.let { it1 ->
                        when (it.data?.status) {
                            200 -> {
                                Preferences.getStringPreference(requireContext(), TOKEN)
                                    ?.let { helpViewModel.hitHelpChatListApi(it) }
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
                    Log.e("TAG", "initViewModel: ${it.message}")
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
    fun closeKeyboard(){
        CommonUtils.hideKeyBoard(requireActivity())
    }
    private fun onClick() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.sendMessage.setOnClickListener {
            if (binding.chatText.text.toString().trim().isNotEmpty()){
                hitSendMessageApi()
            } else{
                Toast.makeText(requireContext(), getString(R.string.please_enter_something), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hitSendMessageApi() {
        Preferences.getStringPreference(requireContext(), TOKEN)
            ?.let { helpViewModel.hitSendMessageApi(it, SendMessageRequest(binding.chatText.text.toString().trim())) }
        binding.chatText.setText("")
    }
}