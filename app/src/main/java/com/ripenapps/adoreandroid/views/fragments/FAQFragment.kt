package com.ripenapps.adoreandroid.views.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentFAQBinding
import com.ripenapps.adoreandroid.models.response_models.faqResponse.Faq
import com.ripenapps.adoreandroid.models.response_models.faqResponse.GetFaqResponse
import com.ripenapps.adoreandroid.preferences.FCM_TOKEN
import com.ripenapps.adoreandroid.preferences.IS_WELCOME_DONE
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.TOKEN
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.repository.ApiRepository
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.Resources
import com.ripenapps.adoreandroid.utils.SingleLiveEvent
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.views.activities.MainActivity
import com.ripenapps.adoreandroid.views.adapters.FreqAskedQuesAdaptor
import kotlinx.coroutines.launch
import java.io.IOException

class FAQFragment : BaseFragment<FragmentFAQBinding>() {
    lateinit var adapterFAQList:FreqAskedQuesAdaptor
    var faqLiveData= SingleLiveEvent<Resources<GetFaqResponse>>()
    var faqList:MutableList<Faq> = mutableListOf()
    var tempFaqList:MutableList<Faq> = mutableListOf()
    override fun setLayout(): Int {
        return R.layout.fragment_f_a_q
    }

    override fun initView(savedInstanceState: Bundle?) {
        Preferences.getStringPreference(requireContext(), TOKEN)?.let { hitFaqApi(it) }
        onClick()
        searchBarListener()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }

    private fun hitFaqApi(token:String) {
        try {
            faqLiveData.postValue(Resources.loading(null))
            lifecycleScope.launch {
                try {
                    faqLiveData.postValue(
                        Resources.success(
                            ApiRepository().getFaqApi(
                                "Bearer $token"
                            )
                        )
                    )
                }catch (ex: IOException) {
                    ex.printStackTrace()
                    faqLiveData.postValue(Resources.error(getString(R.string.unable_to_connect_please_check_your_internet), null))

                } catch (ex: Exception) {
                    faqLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun setObserver() {
        faqLiveData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when(it.data?.status){
                        200->{

                            faqList = it.data?.data?.faq!!
                            adapterFAQList = FreqAskedQuesAdaptor(it.data?.data?.faq)
                            binding.questionRecycler.adapter = adapterFAQList

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

                Status.ERROR -> {
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
    }

    private fun onClick() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun searchBarListener() {
        binding.search.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun afterTextChanged(s: Editable) {

                filter(binding.search.text.toString().lowercase())
            }
        })

    }

    fun filter(text: String) {
        tempFaqList.clear()
        for (d in faqList) {
            if (d.title?.lowercase()?.contains(text) == true) {
                tempFaqList.add(d)
            }
        }
        adapterFAQList.updateList(tempFaqList)
    }

}