package com.ripenapps.adoreandroid.views.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.SimpleItemAnimator
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.BottomsheetGenderListBinding
import com.ripenapps.adoreandroid.models.response_models.getquestions.GenderData
import com.ripenapps.adoreandroid.view_models.GenderListBottomsheetViewModel
import com.ripenapps.adoreandroid.views.adapters.AdapterSelectGender
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GenderListBottomsheet(
    var selectedGender: (Int, String) -> Unit,
    var selectedGenderPosition: Int, var gendersList:MutableList<GenderData>
) :
    BottomSheetDialogFragment() {
    lateinit var binding: BottomsheetGenderListBinding
    val viewModel by viewModels<GenderListBottomsheetViewModel>()
    lateinit var adapterSelectGender: AdapterSelectGender


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.bottomsheet_gender_list,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        onClick()
        binding.cancelBottomSheet.isVisible=true
        val layoutManager = FlexboxLayoutManager(requireContext())
        layoutManager.flexDirection = FlexDirection.ROW;
        layoutManager.justifyContent = JustifyContent.CENTER;
        layoutManager.alignItems = AlignItems.CENTER;
        binding.selectGenderRecycler.layoutManager = layoutManager
        adapterSelectGender =
            AdapterSelectGender(gendersList, selectedGenderPosition, ::selectedGenderPosition)
        binding.selectGenderRecycler.adapter = adapterSelectGender
        (binding.selectGenderRecycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false
        return binding.root
    }


    private fun onClick() {
        binding.selectInSelectGender.setOnClickListener {
            if (selectedGenderPosition != -1)
                selectedGender(selectedGenderPosition, gendersList[selectedGenderPosition].name!!)
            dismiss()
        }
        binding.cancelBottomSheet.setOnClickListener {
            dismiss()
        }
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

    private fun selectedGenderPosition(selectedGenderPosition: Int) {
        this.selectedGenderPosition = selectedGenderPosition
    }
}