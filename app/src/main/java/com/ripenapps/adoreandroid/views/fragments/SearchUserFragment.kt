package com.ripenapps.adoreandroid.views.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentSearchUserBinding
import com.ripenapps.adoreandroid.models.response_models.FilterCardListRequestKeys
import com.ripenapps.adoreandroid.models.response_models.userSearchListResponse.User
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.TOKEN
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.view_models.SearchViewModel
import com.ripenapps.adoreandroid.view_models.UserDetailViewModel
import com.ripenapps.adoreandroid.views.adapters.AdapterSearchUserList
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer
import java.util.TimerTask

@AndroidEntryPoint
class SearchUserFragment : BaseFragment<FragmentSearchUserBinding>() {
    private var recentSearchList: MutableList<String> = mutableListOf()
    var recentSearchListPosition=0
    lateinit var adapterRecentSearch:AdapterSearchUserList
    lateinit var adapterCurrentSearch:AdapterSearchUserList
    private val searchViewModel by viewModels<SearchViewModel>()
    private val userDetailViewModel by viewModels<UserDetailViewModel>()

    override fun setLayout(): Int {
        return R.layout.fragment_search_user
    }
    override fun initView(savedInstanceState: Bundle?) {
        initRecyclerAdapter()
        binding.view.isVisible = false
        onClick()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        addRecentSearch("123")
        getRecentSearch()
        setObserver()
        setTextwatcher()
        onPressingEnterInKeyboard()
    }
    private fun onPressingEnterInKeyboard() {
        binding.search.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                CommonUtils.hideKeyBoard(requireActivity())
                // Consume the Enter key press event
                return@setOnKeyListener true
            }
            // Return false to indicate that the event has not been consumed and should continue to be processed
            return@setOnKeyListener false
        }
    }
    private fun getRecentSearch() {
        recentSearchList.clear()
        recentSearchListPosition=0
        if (Preferences.getStringListPreference(requireContext(), "recent_search")!=null){
            recentSearchList = Preferences.getStringListPreference(requireContext(), "recent_search")!!
            Log.i("TAG", "getRecentSearch: "+recentSearchList.size)
            if (recentSearchList!=null){
                if (recentSearchList.size>0){
                    Preferences.getStringPreference(requireContext(), TOKEN)
                        ?.let { userDetailViewModel.hitUserDetailApi(it, recentSearchList[recentSearchListPosition]) }
                    recentSearchListPosition++
                    Log.i("TAG", "getRecentSearch: $recentSearchList")
                }
            }
        }

    }

    private fun addRecentSearch(id: String) {
        Log.i("TAG", "setRecentSearch: "+Preferences.getStringListPreference(requireContext(), "recent_search"))
        adapterRecentSearch.clearList() //no use seems
//        var list = Preferences.getStringListPreference(requireContext(), "recent_search")
        if (recentSearchList!=null){
            if (recentSearchList?.size!! == 5){
                if (!recentSearchList.contains(id)){
                    recentSearchList.removeAt(0)
                    recentSearchList.add(id)
                    Preferences.setStringListPreference(requireContext(), "recent_search", recentSearchList)
                }

            } else{
                if (!recentSearchList.contains(id)){
                    recentSearchList.add(id)
                    Preferences.setStringListPreference(requireContext(), "recent_search", recentSearchList)
                }
            }
        } else{
            Preferences.setStringListPreference(requireContext(), "recent_search", mutableListOf(id))
        }
        Log.i("TAG", "setRecentSearch: "+Preferences.getStringListPreference(requireContext(), "recent_search"))
    }

    private fun setTextwatcher() {
        binding.search.addTextChangedListener(object : TextWatcher{
            private var timer = Timer()
            private val DELAY: Long = 400
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                timer.cancel()
                timer = Timer()
                timer.schedule(
                    object : TimerTask() {
                        override fun run() {
                            hitSearchUserListApi(binding.search.text.toString())
                        }
                    },
                    DELAY
                )
            }
        })
    }

    private fun setObserver() {
        searchViewModel.getUserSearchListLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when(it.data?.status){
                        200->{
                            Log.i("TAG", "setObserver: "+"getUserSearchListLiveData")
                            it.data?.message?.let { it1 ->
                                if(it.data?.data?.user?.size!! >0){
                                    binding.view.isVisible = true
                                } else{
                                    binding.view.isVisible = false
                                }
                                adapterCurrentSearch.updateList(it.data?.data?.user!!)
                                /*if (recentSearchList.size>recentSearchListPosition){
                                    Preferences.getStringPreference(requireContext(), TOKEN)
                                        ?.let { userDetailViewModel.hitUserDetailApi(it, recentSearchList[recentSearchListPosition]) }
                                    recentSearchListPosition++
                                }*/
                            }
                            Log.i("TAG", "setObserver: getUserSearchListLiveData"+it.data?.data)
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
        userDetailViewModel.getUserDetailLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            var user=User()
                            user.userName = it.data?.data?.user?.userName.toString()
                            user.name = it.data?.data?.user?.name.toString()
                            user._id = it.data?.data?.user?._id.toString()
                            user.profileUrl = it.data?.data?.user?.profile.toString()
                            adapterRecentSearch.addItem(user)
                            if (recentSearchList.size>recentSearchListPosition){
                                Preferences.getStringPreference(requireContext(), TOKEN)
                                    ?.let { userDetailViewModel.hitUserDetailApi(it, recentSearchList[recentSearchListPosition]) }
                                recentSearchListPosition++
                            }
//                            addRecentSearch(it.data?.data?.user?._id.toString())
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

                }

                Status.LOADING -> {
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
//                    binding.idLoadar.root.visibility = View.GONE
                }

            }
        }
    }

    fun getSelected(id:String){
        addRecentSearch(id)
        findNavController().navigate(SearchUserFragmentDirections.searchUserToUserDetail("0", "searchUser", id))
    }
    private fun initRecyclerAdapter() {
        adapterCurrentSearch = AdapterSearchUserList("currentSearch", ::getSelected, ::deleteItem)
        binding.currentSearchRecycler.adapter = adapterCurrentSearch
        adapterRecentSearch = AdapterSearchUserList("recentSearch", ::getSelected, ::deleteItem)
        binding.recentSearchRecycler.adapter = adapterRecentSearch
    }

    private fun onClick() {
        binding.backButton.setOnClickListener {
            requireActivity().finish()
        }
    }
    fun hitSearchUserListApi(search:String){
        if (search.isNotEmpty()){
            Preferences.getStringPreference(requireContext(), TOKEN)
                ?.let { searchViewModel.hitUserSearchListApi(it, search, FilterCardListRequestKeys(), "") }
        }
        else{
            requireActivity().runOnUiThread {
                binding.view.isVisible = false
                adapterCurrentSearch.clearList()
            }
        }
    }
    fun deleteItem(position:Int){
//        var list = Preferences.getStringListPreference(requireContext(), "recent_search")
        recentSearchList.removeAt(position)
//        list?.removeAt(position)
        Preferences.setStringListPreference(requireContext(), "recent_search", recentSearchList)
        adapterRecentSearch.removeItem(position)
    }
}