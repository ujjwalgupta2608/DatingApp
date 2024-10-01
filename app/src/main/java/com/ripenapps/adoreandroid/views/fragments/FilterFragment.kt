package com.ripenapps.adoreandroid.views.fragments

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentFilterBinding
import com.ripenapps.adoreandroid.models.response_models.FilterCardListRequestKeys
import com.ripenapps.adoreandroid.models.response_models.getquestions.GenderData
import com.ripenapps.adoreandroid.models.response_models.getquestions.QuestionsDataResponse
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.USER_ID
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.view_models.MyProfileViewModel
import com.ripenapps.adoreandroid.views.adapters.AdapterFilterScreenLists
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.slider.LabelFormatter.LABEL_GONE
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException

@AndroidEntryPoint
class FilterFragment : BaseFragment<FragmentFilterBinding>() {
    private var selectedSortBy=""
    private var selectedCity=""
    var minAge:Int=18
    var maxAge:Int=30
    var minDistance:Int=0
    var maxDistance:Int=20
    private var selectedComplexion=""
    private var selectedLookingFor=""
    private var selectedItem: String=""
    private var recyclerType: String=""
    private var selectedInterestedIn: String=""


    private lateinit var commonAdapter: AdapterFilterScreenLists
    val myProfileViewModel by viewModels<MyProfileViewModel>()
    var allListsDataResponse = QuestionsDataResponse()
    private lateinit var bottomSheetDialog: BottomSheetDialog
    lateinit var view1: View
    lateinit var title: TextView
    lateinit var bottomsheetRecyclerView: RecyclerView
    lateinit var bottomSheetSelectButton: AppCompatTextView
    private lateinit var cancelBottomsheet: ImageView
    lateinit var bottomSheetLayoutManager: FlexboxLayoutManager


    var interestedInList= mutableListOf<GenderData>()
    var sortByList = mutableListOf<GenderData>()
    lateinit var interestAdapter:AdapterFilterScreenLists
    private lateinit var layoutManager: FlexboxLayoutManager
    var isUserSubscribed=true
    var selectedSexualOrientation=""
    var filterCardListRequestKeys=FilterCardListRequestKeys()
    private var autoCompleteSupportFragment: AutocompleteSupportFragment? = null


    override fun setLayout(): Int {
        return R.layout.fragment_filter
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.view.isVisible = false
        initBottomSheet()
        setdistanceSeekbar()
        setageSeekbar()
        seekbarsListeners()
        initValues()
        initRecyclerview()
        onClick()
        hitGetAllDataApi()
        searchLocation()
    }
    @SuppressLint("CutPasteId")
    private fun searchLocation() {
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key))
        }
        autoCompleteSupportFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autoCompleteSupportFragment!!.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_search_button)?.visibility =
            View.GONE

        (autoCompleteSupportFragment!!.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_search_input) as EditText).textSize =
            13f
        (autoCompleteSupportFragment!!.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_search_input) as EditText).setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.down_arrow,
            0
        )
        (autoCompleteSupportFragment!!.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_search_input) as EditText).compoundDrawablePadding =
            20
        (autoCompleteSupportFragment!!.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_search_input) as EditText).typeface =
            Typeface.DEFAULT

        (autoCompleteSupportFragment!!.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_search_input) as EditText).setTextColor(
            ContextCompat.getColor(
                requireContext(), R.color.black_mine_shaft
            )
        )
        autoCompleteSupportFragment!!.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )
        )
        autoCompleteSupportFragment!!.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_clear_button)?.visibility =
            View.GONE
        autoCompleteSupportFragment!!.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_clear_button)?.setOnClickListener {
//            clear button callback
            selectedCity=""
            autoCompleteSupportFragment!!.setText("")
            autoCompleteSupportFragment!!.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_clear_button)?.visibility =
                View.GONE
            Log.i("TAG", "searchLocation: places_autocomplete_clear_button")
        }
        if (!selectedCity.isNullOrEmpty()){
            autoCompleteSupportFragment?.setText(selectedCity)
        }
        autoCompleteSupportFragment?.setTypeFilter(TypeFilter.CITIES)
        autoCompleteSupportFragment!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
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
                    autoCompleteSupportFragment!!.setText(address.getAddressLine(0))
                    Log.i("TAG", "onPlaceSelected: $address")
                    Log.i("TAG", "onPlaceSelected getAddressLine: ${address.getAddressLine(0)}")

                }
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
                Log.e("TAG", "onError: $status")
            }
        })

    }
    private fun initValues() {
        Log.i("TAG", "initValues: "+UserPreference.filterCardListRequestKeys)
        selectedCity = UserPreference.filterCardListRequestKeys.city
        if (UserPreference.filterCardListRequestKeys.interestedIn.equals("male", ignoreCase = true)){
            selectedInterestedIn = getString(R.string.men)
        } else if (UserPreference.filterCardListRequestKeys.interestedIn.equals("female", ignoreCase = true)){
            selectedInterestedIn = getString(R.string.women)
        } else if(UserPreference.filterCardListRequestKeys.interestedIn.equals("everyone", ignoreCase = true)){
            selectedInterestedIn = getString(R.string.both)
        }
        if (UserPreference.filterCardListRequestKeys.sortBy.equals("online", ignoreCase = true))
            selectedSortBy=getString(R.string.online)
        else if (UserPreference.filterCardListRequestKeys.sortBy.equals("popular", ignoreCase = true))
            selectedSortBy=getString(R.string.popular)
        else if (UserPreference.filterCardListRequestKeys.sortBy.equals("newUser", ignoreCase = true))
            selectedSortBy=getString(R.string.new_user)
        Log.i("TAG", "selectedSortBy1: "+UserPreference.filterCardListRequestKeys.sortBy.equals("newUser", ignoreCase = true))
        Log.i("TAG", "selectedSortBy2: "+selectedSortBy)
//        selectedSortBy = UserPreference.filterCardListRequestKeys.sortBy
        if (UserPreference.filterCardListRequestKeys.minDistance==-1){
            minDistance=0
            maxDistance=20
        }else{
            minDistance = UserPreference.filterCardListRequestKeys.minDistance
            maxDistance = UserPreference.filterCardListRequestKeys.maxDistance
        }
            minAge = UserPreference.filterCardListRequestKeys.minAge
            maxAge = UserPreference.filterCardListRequestKeys.maxAge
        selectedSexualOrientation = UserPreference.filterCardListRequestKeys.sexualOrientation
        selectedLookingFor = UserPreference.filterCardListRequestKeys.relationshipGoal
        selectedComplexion = UserPreference.filterCardListRequestKeys.complexion
        binding.distanceSeekbar.values = listOf(minDistance.toFloat(), maxDistance.toFloat())
        binding.ageSeekbar.values = listOf(minAge.toFloat(), maxAge.toFloat())
    }

    private fun hitGetAllDataApi() {
        Preferences.getStringPreference(requireContext(), USER_ID)
            ?.let { myProfileViewModel.hitGetAllListsDataApi(it) }
        Preferences.getStringPreference(requireContext(), USER_ID)
            ?.let { myProfileViewModel.hitWhatElseLikeApi() }
    }

    private fun initBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        view1 = LayoutInflater.from(requireContext()) //init bottomSheet
            .inflate(R.layout.bottomsheet_gender_list, null)
        bottomSheetDialog.setContentView(view1)
        title = view1.findViewById<View>(R.id.title) as TextView
        cancelBottomsheet = view1.findViewById<View>(R.id.cancelBottomSheet) as ImageView
        cancelBottomsheet.isVisible = true
        bottomsheetRecyclerView =
            view1.findViewById<View>(R.id.select_gender_recycler) as RecyclerView
        bottomSheetSelectButton =
            view1.findViewById<View>(R.id.selectInSelectGender) as AppCompatTextView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }

    private fun setObserver() {
        myProfileViewModel.getWhatElseLikeLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            interestAdapter=AdapterFilterScreenLists("interest", it.data?.data!!)
                            binding.interestsRecycler.adapter = interestAdapter
                        }

                        else -> {}
                    }
                }

                Status.LOADING -> {}

                Status.ERROR -> {
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
        myProfileViewModel.getAllListsLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            allListsDataResponse = it?.data
                            if (isUserSubscribed){
                                if (selectedSexualOrientation.isNullOrEmpty()){
                                    binding.sexualOrientation.text = getString(R.string.select)
                                } else{
                                    binding.sexualOrientation.text = selectedSexualOrientation
                                }
                                if (selectedLookingFor.isNullOrEmpty()){
                                    binding.lookingFor.text = getString(R.string.select)
                                } else{
                                    binding.lookingFor.text = selectedLookingFor
                                }
                                if (selectedComplexion.isNullOrEmpty()){
                                    binding.complexion.text = getString(R.string.select)
                                } else{
                                    binding.complexion.text = selectedComplexion
                                }
                            }else{

                            }
                        }

                        else -> {}
                    }
                }

                Status.LOADING -> {}

                Status.ERROR -> {
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

    private fun seekbarsListeners() {
        binding.distanceSeekbar.addOnChangeListener { slider, value, fromUser ->
            Log.d("TAG", "distanceSeekbar: "+slider.values)
            minDistance= slider.values[0].toInt()
            maxDistance=slider.values[1].toInt()
        }
        binding.ageSeekbar.addOnChangeListener { slider, value, fromUser ->
            Log.d("TAG", "ageSeekbar: "+slider.values)
            minAge= slider.values[0].toInt()
            maxAge=slider.values[1].toInt()
        }
    }

    private fun setageSeekbar() {
        binding.ageSeekbar.thumbElevation = 0.0F
        binding.ageSeekbar.labelBehavior = LABEL_GONE
        binding.ageSeekbar.isTickVisible=false
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
        binding.distanceSeekbar.labelBehavior = LABEL_GONE
        binding.distanceSeekbar.isTickVisible=false
        binding.distanceSeekbar.stepSize = 1F
        binding.distanceSeekbar.thumbRadius = 22
        binding.distanceSeekbar.trackHeight = 20
        binding.distanceSeekbar.thumbStrokeColor = resources.getColorStateList(R.color.white)
        binding.distanceSeekbar.thumbStrokeWidth = 3F
        binding.distanceSeekbar.trackActiveTintList = resources.getColorStateList(R.color.theme)
        binding.distanceSeekbar.trackInactiveTintList = resources.getColorStateList(R.color.white_whisper)
    }

    private fun onClick() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        cancelBottomsheet.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        binding.sexualOrientation.setOnClickListener {
            selectedItem = ""
            recyclerType = "sexualOrientation"
            bottomSheetDialog.show()
            title.text = requireContext().getString(R.string.my_sexual_orientation)
            Log.i("TAG", "onClick: "+allListsDataResponse.data.orientationData)
            commonAdapter =
                AdapterFilterScreenLists(
                    "sexualOrientation",
                    allListsDataResponse.data.orientationData,
                    ::getSelected,
                    selectedSexualOrientation
                )
            bottomsheetRecyclerView.adapter = commonAdapter
        }
        binding.lookingFor.setOnClickListener {
            selectedItem = ""
            recyclerType = "lookingFor"
            bottomSheetDialog.show()
            title.text = requireContext().getString(R.string.right_now_i_m_looking_for)
            Log.i("TAG", "onClick: "+allListsDataResponse.data.lookingForData)
            commonAdapter =
                AdapterFilterScreenLists(
                    "lookingFor",
                    allListsDataResponse.data.lookingForData,
                    ::getSelected,
                    selectedLookingFor
                )
            bottomsheetRecyclerView.adapter = commonAdapter
        }
        binding.complexion.setOnClickListener {
            selectedItem = ""
            recyclerType = "complexion"
            bottomSheetDialog.show()
            title.text = requireContext().getString(R.string.complexion)
            Log.i("TAG", "onClick: "+allListsDataResponse.data.complexionData)
            commonAdapter =
                AdapterFilterScreenLists(
                    "complexion",
                    allListsDataResponse.data.complexionData,
                    ::getSelected,
                    selectedComplexion
                )
            bottomsheetRecyclerView.adapter = commonAdapter
        }
        binding.applyButton.setOnClickListener {
            filterCardListRequestKeys.sexualOrientation=selectedSexualOrientation
            filterCardListRequestKeys.relationshipGoal=selectedLookingFor
            filterCardListRequestKeys.complexion=selectedComplexion
            if (selectedCity.isNullOrEmpty()){
                filterCardListRequestKeys.minDistance=minDistance
                filterCardListRequestKeys.maxDistance=maxDistance
            }else{
                filterCardListRequestKeys.minDistance=-1
                filterCardListRequestKeys.maxDistance=0
            }
            filterCardListRequestKeys.city=selectedCity
                filterCardListRequestKeys.minAge=minAge
                filterCardListRequestKeys.maxAge=maxAge
            if (selectedInterestedIn.equals(getString(R.string.men), ignoreCase = true)){
                filterCardListRequestKeys.interestedIn = "male"
            } else if (selectedInterestedIn.equals(getString(R.string.women), ignoreCase = true)){
                filterCardListRequestKeys.interestedIn = "feMale"
            } else if(selectedInterestedIn.equals(getString(R.string.both), ignoreCase = true)){
                filterCardListRequestKeys.interestedIn = "everyOne"
            }
//            filterCardListRequestKeys.interestedIn=selectedInterestedIn
//            Log.i("TAG", "selectedSortBy1: "+selectedSortBy.equals(getString(R.string.new_user), ignoreCase = true))
//            Log.i("TAG", "selectedSortBy2: "+selectedSortBy)

            if (selectedSortBy.equals(getString(R.string.online), ignoreCase = true))
                filterCardListRequestKeys.sortBy="online"
            else if (selectedSortBy.equals(getString(R.string.popular), ignoreCase = true))
                filterCardListRequestKeys.sortBy="popular"
            else if (selectedSortBy.equals(getString(R.string.new_user), ignoreCase = true))
                filterCardListRequestKeys.sortBy="newUser"
            else
                filterCardListRequestKeys.sortBy=""
            if (::interestAdapter.isInitialized)
                filterCardListRequestKeys.whatElseYouLike = interestAdapter.selectedWhatAreYouIntoList()
            UserPreference.filterCardListRequestKeys = filterCardListRequestKeys
            UserPreference.isFilterApplied=true
            Log.i("TAG", "onClick filterCardListRequestKeys: "+UserPreference.filterCardListRequestKeys)
            findNavController().popBackStack()
        }
        binding.resetFilter.setOnClickListener {
            filterCardListRequestKeys.city=""
            filterCardListRequestKeys.sexualOrientation=""
            filterCardListRequestKeys.relationshipGoal=""
            filterCardListRequestKeys.complexion=""
            filterCardListRequestKeys.minDistance=0
            filterCardListRequestKeys.maxDistance=20
            filterCardListRequestKeys.minAge=18
            filterCardListRequestKeys.maxAge=30
            filterCardListRequestKeys.interestedIn=""
            filterCardListRequestKeys.sortBy=""
            UserPreference.filterCardListRequestKeys = filterCardListRequestKeys
            UserPreference.isFilterApplied=true
            Log.i("TAG", "onClick filterCardListRequestKeys: "+UserPreference.filterCardListRequestKeys)
            findNavController().popBackStack()
        }
        bottomSheetSelectButton.setOnClickListener {
            when(recyclerType){
                "sexualOrientation"->{
                    bottomSheetDialog.dismiss()
                    selectedSexualOrientation = selectedItem
                    if (selectedItem == "") {
                        binding.sexualOrientation.text = getString(R.string.select)
                    } else {
                        if (selectedItem.length > 1) {
                            binding.sexualOrientation.text = selectedItem.substring(0, 1)?.toUpperCase() + selectedItem?.substring(1)
                        } else{
                            binding.sexualOrientation.text = selectedItem.toUpperCase()
                        }
                    }
                }
                "lookingFor"->{
                    bottomSheetDialog.dismiss()
                    selectedLookingFor = selectedItem
                    if (selectedItem == "") {
                        binding.lookingFor.text = getString(R.string.select)
                    } else {
                        if (selectedItem.length > 1) {
                            binding.lookingFor.text = selectedItem.substring(0, 1)?.toUpperCase() + selectedItem?.substring(1)
                        } else{
                            binding.lookingFor.text = selectedItem.toUpperCase()
                        }
                    }
                }
                "complexion"->{
                    bottomSheetDialog.dismiss()
                    selectedComplexion = selectedItem
                    if (selectedItem == "") {
                        binding.complexion.text = getString(R.string.select)
                    } else {
                        if (selectedItem.length > 1) {
                            binding.complexion.text = selectedItem.substring(0, 1)?.toUpperCase() + selectedItem?.substring(1)
                        } else{
                            binding.complexion.text = selectedItem.toUpperCase()
                        }
                    }
                }
                else -> {}
            }
        }
    }

    private fun initRecyclerview() {
        (binding.genderRecycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false
        (binding.sortByRecycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false
        (binding.interestsRecycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false
        interestedInList.addAll(listOf(GenderData(name = getString(R.string.women), isSelected =  false),GenderData(name = getString(R.string.men), isSelected = false) ,GenderData(name = getString(R.string.both), isSelected = false) ))
        sortByList.addAll(listOf(GenderData(name = getString(R.string.online), isSelected = false),GenderData(name = getString(R.string.popular), isSelected = false) ,GenderData(name = getString(R.string.new_user), isSelected = false)))
        binding.genderRecycler.adapter=AdapterFilterScreenLists("interestedin", interestedInList, ::getSelected,selectedInterestedIn)
        binding.sortByRecycler.adapter = AdapterFilterScreenLists("sortby", sortByList, ::getSelected,selectedSortBy)
        layoutManager = FlexboxLayoutManager(requireContext())
        layoutManager.flexDirection = FlexDirection.ROW;
        layoutManager.alignItems = AlignItems.CENTER;
        binding.interestsRecycler.layoutManager = layoutManager

        bottomSheetLayoutManager = FlexboxLayoutManager(requireContext())
        bottomSheetLayoutManager.flexDirection = FlexDirection.ROW
        bottomSheetLayoutManager.alignItems = AlignItems.CENTER
        bottomsheetRecyclerView.layoutManager = bottomSheetLayoutManager
        (bottomsheetRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false
    }
    private fun getSelected(position: Int, selectedItem:String, recyclerType:String){
        this.selectedItem = selectedItem
        Log.i("TAG", "getSelected: $selectedItem")
        when(recyclerType){
            "interestedin"->{
                selectedInterestedIn=selectedItem
            }
            "sortby"->{
                selectedSortBy=selectedItem
            }
        }

    }

}