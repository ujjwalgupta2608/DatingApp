package com.ripenapps.adoreandroid.views.bottomsheets

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.SimpleItemAnimator
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentFilterBinding
import com.ripenapps.adoreandroid.models.response_models.FilterCardListRequestKeys
import com.ripenapps.adoreandroid.models.response_models.getquestions.GenderData
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.views.adapters.AdapterFilterScreenLists
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.slider.LabelFormatter
import java.io.IOException

class FilterBottomsheet(val getCustomDismiss: (String, Double, Double) -> Unit) :BottomSheetDialogFragment() {
    lateinit var binding: FragmentFilterBinding
    private var autoCompleteSupportFragment: AutocompleteSupportFragment? = null
    private var interestedInList = mutableListOf<GenderData>()
    private var sortByList = mutableListOf<GenderData>()
    private var selectedCity=""
    private var minAge: Int = 18
    private var maxAge: Int = 30
    private var minDistance: Int = 0
    private var maxDistance: Int = 20
    private var filterMapListRequestKeys = FilterCardListRequestKeys()
    private var selectedInterestedIn: String = ""
    private var selectedSortBy = ""
    private var selectedItem: String = ""
    private var selectedLat:Double=0.0
    private var selectedLong:Double=0.0

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_filter,
            container,
            false
        )
        initUi()
        initValues()
        searchLocation()
        setageSeekbar()
        setdistanceSeekbar()
        initRecyclerview()
        seekbarsListeners()
        onClick()
        return binding.root
    }

    private fun initUi() {
        binding.nestedScrollView.setBackgroundDrawable(requireActivity().resources.getDrawable(R.drawable.background_top_corners_24dp))
        binding.additionalViews.isVisible = false
        binding.backButton.isVisible=false
    }

    private fun initRecyclerview() {
        (binding.genderRecycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false
        (binding.sortByRecycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false
        interestedInList.clear()
        interestedInList.addAll(listOf(GenderData(name = getString(R.string.women), isSelected =  false),GenderData(name = getString(R.string.men), isSelected = false) ,GenderData(name = getString(R.string.both), isSelected = false) ))
        sortByList.clear()
        sortByList.addAll(listOf(GenderData(name = getString(R.string.online), isSelected = false),GenderData(name = getString(R.string.popular), isSelected = false) ,GenderData(name = getString(R.string.new_user), isSelected = false)))

        binding.genderRecycler.adapter =
            AdapterFilterScreenLists(
                "interestedin",
                interestedInList,
                ::getSelected,
                selectedInterestedIn
            )
        binding.sortByRecycler.adapter =
            AdapterFilterScreenLists("sortby", sortByList, ::getSelected, selectedSortBy)

    }
    private fun getSelected(position: Int, selectedItem: String, recyclerType: String) {
        this.selectedItem = selectedItem
        Log.i("TAG", "getSelected: " + selectedItem)
        when (recyclerType) {
            "interestedin" -> {
                selectedInterestedIn = selectedItem
            }

            "sortby" -> {
                selectedSortBy = selectedItem
            }
        }

    }
    private fun initValues() {
        Log.i("TAG", "initValues: " + UserPreference.filterMapListRequestKeys)
        selectedCity=UserPreference.filterMapListRequestKeys.city
        if (UserPreference.filterMapListRequestKeys.interestedIn.equals("male", ignoreCase = true)){
            selectedInterestedIn = getString(R.string.men)
        } else if (UserPreference.filterMapListRequestKeys.interestedIn.equals("female", ignoreCase = true)){
            selectedInterestedIn = getString(R.string.women)
        } else if(UserPreference.filterMapListRequestKeys.interestedIn.equals("everyone", ignoreCase = true)){
            selectedInterestedIn = getString(R.string.both)
        }
        if (UserPreference.filterMapListRequestKeys.sortBy.equals("online", ignoreCase = true))
            selectedSortBy=getString(R.string.online)
        else if (UserPreference.filterMapListRequestKeys.sortBy.equals("popular", ignoreCase = true))
            selectedSortBy=getString(R.string.popular)
        else if (UserPreference.filterMapListRequestKeys.sortBy.equals("newUser", ignoreCase = true))
            selectedSortBy=getString(R.string.new_user)
//        selectedSortBy = UserPreference.filterMapListRequestKeys.sortBy
        if (UserPreference.filterMapListRequestKeys.minDistance==-1){
            minDistance=0
            maxDistance=20
        }else{
            minDistance = UserPreference.filterMapListRequestKeys.minDistance
            maxDistance = UserPreference.filterMapListRequestKeys.maxDistance
        }
        minAge = UserPreference.filterMapListRequestKeys.minAge
        maxAge = UserPreference.filterMapListRequestKeys.maxAge
        binding.distanceSeekbar.values = listOf(minDistance.toFloat(), maxDistance.toFloat())
        binding.ageSeekbar.values = listOf(minAge.toFloat(), maxAge.toFloat())
    }

    private fun seekbarsListeners() {
        binding.distanceSeekbar.addOnChangeListener { slider, value, fromUser ->
            Log.d("TAG", "distanceSeekbar: " + slider.values)
            minDistance = slider.values[0].toInt()
            maxDistance = slider.values[1].toInt()
        }
        binding.ageSeekbar.addOnChangeListener { slider, value, fromUser ->
            Log.d("TAG", "ageSeekbar: " + slider.values)
            minAge = slider.values[0].toInt()
            maxAge = slider.values[1].toInt()
        }
    }
    private fun setageSeekbar() {
        binding.ageSeekbar.thumbElevation = 0.0F
        binding.ageSeekbar.labelBehavior = LabelFormatter.LABEL_GONE
        binding.ageSeekbar.isTickVisible = false
        binding.ageSeekbar.stepSize = 1F
        binding.ageSeekbar.thumbRadius = 22
        binding.ageSeekbar.trackHeight = 20
        binding.ageSeekbar.thumbStrokeColor = resources.getColorStateList(R.color.white)
        binding.ageSeekbar.thumbStrokeWidth = 3F
        binding.ageSeekbar.trackActiveTintList = resources.getColorStateList(R.color.theme)
        binding.ageSeekbar.trackInactiveTintList = resources.getColorStateList(R.color.white_whisper)
    }

    private fun setdistanceSeekbar() {
        binding.distanceSeekbar.thumbElevation = 0.0F
        binding.distanceSeekbar.labelBehavior = LabelFormatter.LABEL_GONE
        binding.distanceSeekbar.isTickVisible = false
        binding.distanceSeekbar.stepSize = 1F
        binding.distanceSeekbar.thumbRadius = 22
        binding.distanceSeekbar.trackHeight = 20
        binding.distanceSeekbar.thumbStrokeColor = resources.getColorStateList(R.color.white)
        binding.distanceSeekbar.thumbStrokeWidth = 3F
        binding.distanceSeekbar.trackActiveTintList = resources.getColorStateList(R.color.theme)
        binding.distanceSeekbar.trackInactiveTintList = resources.getColorStateList(R.color.white_whisper)
    }
    @SuppressLint("CutPasteId")
    private fun searchLocation() {
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key))
        }
        autoCompleteSupportFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?
        autoCompleteSupportFragment?.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_search_button)?.visibility =
            View.GONE

        (autoCompleteSupportFragment?.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_search_input) as EditText).textSize =
            13f
        (autoCompleteSupportFragment?.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_search_input) as EditText).setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.down_arrow,
            0
        )
        (autoCompleteSupportFragment?.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_search_input) as EditText).compoundDrawablePadding =
            20
        (autoCompleteSupportFragment?.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_search_input) as EditText).typeface =
            Typeface.DEFAULT

        (autoCompleteSupportFragment?.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_search_input) as EditText).setTextColor(
            ContextCompat.getColor(
                requireContext(), R.color.black_mine_shaft
            )
        )
        autoCompleteSupportFragment?.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )
        )
        autoCompleteSupportFragment?.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_clear_button)?.visibility =
            View.GONE
        autoCompleteSupportFragment?.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_clear_button)?.setOnClickListener {
//            clear button callback
            selectedCity=""
            autoCompleteSupportFragment?.setText("")
            autoCompleteSupportFragment?.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_clear_button)?.visibility =
                View.GONE
            Log.i("TAG", "searchLocation: places_autocomplete_clear_button")
        }
        if (!selectedCity.isNullOrEmpty()){
            autoCompleteSupportFragment?.setText(selectedCity)
        }
        autoCompleteSupportFragment?.setTypeFilter(TypeFilter.CITIES)
        autoCompleteSupportFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val geocoder = Geocoder(requireContext())
                var addressList: List<Address> = ArrayList()
                try {
                    addressList = geocoder.getFromLocationName(place.toString(), 2)!!
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                if (addressList.isNotEmpty()) {
                    selectedCity=addressList[0].locality
                    val address: Address = addressList[0]
                    autoCompleteSupportFragment?.setText(address.getAddressLine(0))
                    selectedLat = addressList[0].latitude
                    selectedLong = addressList[0].longitude
                    Log.i("TAG", "onPlaceSelected: $address")
                    Log.i("TAG", "onPlaceSelected getAddressLine: ${address.getAddressLine(0)}")

                }
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
                Log.e("TAG", "onError: $status")
            }
        })

    }

    private fun onClick() {
        binding.applyButton.setOnClickListener {
            filterMapListRequestKeys.city=selectedCity
            if (selectedCity.isNullOrEmpty()){
                filterMapListRequestKeys.minDistance=minDistance
                filterMapListRequestKeys.maxDistance=maxDistance
            }else{
                filterMapListRequestKeys.minDistance=-1
                filterMapListRequestKeys.maxDistance=0
                filterMapListRequestKeys.lat=selectedLat
                filterMapListRequestKeys.long=selectedLong
            }
//            if (minAge!=0&&maxAge!=0){
            filterMapListRequestKeys.minAge = minAge
            filterMapListRequestKeys.maxAge = maxAge
//            }
            if (selectedInterestedIn.equals(getString(R.string.men), ignoreCase = true)){
                filterMapListRequestKeys.interestedIn = "male"
            } else if (selectedInterestedIn.equals(getString(R.string.women), ignoreCase = true)){
                filterMapListRequestKeys.interestedIn = "feMale"
            } else if(selectedInterestedIn.equals(getString(R.string.both), ignoreCase = true)){
                filterMapListRequestKeys.interestedIn = "everyOne"
            }
//            filterMapListRequestKeys.interestedIn = selectedInterestedIn
            if (selectedSortBy.equals(getString(R.string.online), ignoreCase = true))
                filterMapListRequestKeys.sortBy = "online"
            else if (selectedSortBy.equals(getString(R.string.popular), ignoreCase = true))
                filterMapListRequestKeys.sortBy = "popular"
            else if (selectedSortBy.equals(getString(R.string.new_user), ignoreCase = true))
                filterMapListRequestKeys.sortBy = "newUser"
            else
                filterMapListRequestKeys.sortBy = ""
            UserPreference.filterMapListRequestKeys = filterMapListRequestKeys
            UserPreference.isFilterApplied = true
//            Log.i("TAG", "onClick filterMapListRequestKeys: " + UserPreference.filterMapListRequestKeys)
            getCustomDismiss("1", selectedLat, selectedLong)
            dismiss()
        }
        binding.resetFilter.setOnClickListener {
            filterMapListRequestKeys.city=""
            filterMapListRequestKeys.sexualOrientation = ""
            filterMapListRequestKeys.relationshipGoal = ""
            filterMapListRequestKeys.complexion = ""
            filterMapListRequestKeys.minDistance = 0
            filterMapListRequestKeys.maxDistance = 20
            filterMapListRequestKeys.minAge = 18
            filterMapListRequestKeys.maxAge = 30
//            binding.distanceSeekbar.values = listOf(0F, 30F)    //reset them to full length bcoz if btmsheet is opened again from the same fragment slider values were not changed
//            binding.ageSeekbar.values = listOf(18F ,60F)
            filterMapListRequestKeys.interestedIn = ""
            filterMapListRequestKeys.sortBy = ""
            filterMapListRequestKeys.lat=0.0
            filterMapListRequestKeys.long=0.0
            UserPreference.filterMapListRequestKeys = filterMapListRequestKeys
            UserPreference.isFilterApplied = true
//            Log.i("TAG", "onClick filterMapListRequestKeys: " + UserPreference.filterMapListRequestKeys)
            getCustomDismiss("0", 0.0, 0.0)
            dismiss()
        }

    }
}