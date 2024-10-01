package com.ripenapps.adoreandroid.views.fragments

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentWalkthroughBinding
import com.ripenapps.adoreandroid.models.static_models.ViewPagerWalkthroughModel
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.view_models.WalkthroughViewModel
import com.ripenapps.adoreandroid.views.adapters.AdapterWalkthroughViewPager
import com.google.android.material.tabs.TabLayoutMediator

class WalkthroughFragment : BaseFragment<FragmentWalkthroughBinding>() {
    private val viewModel by viewModels<WalkthroughViewModel>()
    var viewPagerItemList = ArrayList<ViewPagerWalkthroughModel>()
    var viewPagerPosition = 0

    override fun setLayout(): Int {
        return R.layout.fragment_walkthrough
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        setVeiwPager()
        binding.clickEvents = ::onClick
   }

    private fun onClick(value:Int){
        when(value){
            0->{
                if (viewPagerPosition==0){}
                else if (viewPagerPosition==1){
                    binding.previousFromWalkthrough.setImageResource(R.drawable.previous_unselected)
                    binding.viewPager.currentItem = viewPagerPosition-1
                    viewPagerPosition--
                }else{
                    binding.previousFromWalkthrough.setImageResource(R.drawable.previous_selected)
                    binding.viewPager.currentItem = viewPagerPosition-1
                    viewPagerPosition--
                }
                /*when(viewPagerPosition){
                    2->{
                        binding.previousFromWalkthrough.setImageResource(R.drawable.previous_selected)
                        binding.viewPager.currentItem = viewPagerPosition-1
                        viewPagerPosition--
                    }
                    1->{
                        binding.previousFromWalkthrough.setImageResource(R.drawable.previous_unselected)
                        binding.viewPager.currentItem = viewPagerPosition-1
                        viewPagerPosition--
                    }
                }*/
            }
            1->{

                if (viewPagerPosition<viewPagerItemList.size-1){
                    binding.previousFromWalkthrough.setImageResource(R.drawable.previous_selected)
                    binding.viewPager.currentItem = viewPagerPosition+1
                    viewPagerPosition++
                } else if (viewPagerPosition==4){
                    findNavController().navigate(WalkthroughFragmentDirections.walkthroughToLogin())
                }
            }

        }
    }
    private fun onSkip(){
        findNavController().navigate(WalkthroughFragmentDirections.walkthroughToLogin())

    }


    private fun setVeiwPager() {
        viewPagerItemList.clear()
        viewPagerItemList.add(ViewPagerWalkthroughModel(getString(R.string.meeting_new_people_in_your_area), "", ""))
        viewPagerItemList.add(ViewPagerWalkthroughModel(getString(R.string.meeting_new_people_in_your_area), "", ""))
        viewPagerItemList.add(ViewPagerWalkthroughModel(getString(R.string.meeting_new_people_in_your_area), "", ""))
        viewPagerItemList.add(ViewPagerWalkthroughModel(getString(R.string.meeting_new_people_in_your_area), "", ""))
        viewPagerItemList.add(ViewPagerWalkthroughModel(getString(R.string.meeting_new_people_in_your_area), "", ""))
        binding.viewPager.adapter = AdapterWalkthroughViewPager(::onSkip,requireContext(), viewPagerItemList)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()
        binding.viewPager.isUserInputEnabled = false
    }

}