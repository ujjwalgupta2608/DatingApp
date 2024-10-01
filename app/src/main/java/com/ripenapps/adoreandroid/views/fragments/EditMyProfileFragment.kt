package com.ripenapps.adoreandroid .views.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.bumptech.glide.Glide
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentEditMyProfileBinding
import com.ripenapps.adoreandroid.models.request_models.updateuserprofile.Lifestyle
import com.ripenapps.adoreandroid.models.request_models.updateuserprofile.WhatMakesYou
import com.ripenapps.adoreandroid.models.response_models.carddetails.MediaList
import com.ripenapps.adoreandroid.models.response_models.getInterestList.InterestData
import com.ripenapps.adoreandroid.models.response_models.getquestions.GenderData
import com.ripenapps.adoreandroid.models.response_models.getquestions.QuestionsDataResponse
import com.ripenapps.adoreandroid.preferences.FCM_TOKEN
import com.ripenapps.adoreandroid.preferences.NAME
import com.ripenapps.adoreandroid.preferences.PROFILE
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.TOKEN
import com.ripenapps.adoreandroid.preferences.USER_ID
import com.ripenapps.adoreandroid.preferences.USER_NAME
import com.ripenapps.adoreandroid.utils.AppDialogListener
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.ImageUtils
import com.ripenapps.adoreandroid.utils.ProcessDialog
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.utils.createInfoDialog
import com.ripenapps.adoreandroid.utils.createYesNoDialog
import com.ripenapps.adoreandroid.view_models.MyProfileViewModel
import com.ripenapps.adoreandroid.views.adapters.AdapterInterestInMyProfile
import com.ripenapps.adoreandroid.views.adapters.AdapterMyProfileMedia
import com.ripenapps.adoreandroid.views.adapters.AdapterPersonalDetailsItem
import com.github.tomeees.scrollpicker.ScrollPicker
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.ripenapps.adoreandroid.preferences.IS_WELCOME_DONE
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.views.activities.MainActivity
import com.ripenapps.adoreandroid.views.fragments.EditMyProfileFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.utils.ContentUriUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@AndroidEntryPoint
class EditMyProfileFragment : BaseFragment<FragmentEditMyProfileBinding>(),
    AdapterInterestInMyProfile.InterestSelector/*, SetPermissionListener*/,
    AdapterMyProfileMedia.ProfileMediaSelector {
    private var selectGalleryImage=true
    private val CAMERA_STORAGE_PERMISSION_REQUEST_CODE: Int=111
    private var selectedCountryCode=""
    private var name = ""
    private var selectedAddress = ""
    private var relationshipGoal = ""
    private var bioDescription = ""
    private var mobileNumber = ""
    private var interestedIn = ""
    private var selectedHeight = ""
    private var selectedAge = ""
    private var selectedLanguage = mutableListOf<String>()
    private var selectedComplexion = ""
    private var selectedSexualOrientation = ""
    private var selectedZodiacSign: String = ""
    private var selectedEducationLevel: String = ""
    private var selectedCommunicationStyle: String = ""
    private var selectedPet: String = ""
    private var selectedItem = ""
    private var selectedSmokeType: String = ""
    private var selectedDrinkType: String = ""
    private var selectedGender: String = ""
    private var selectedProfession = ""
    private var listCheck: String = ""
    private var email: String = ""
    var isPersonalDetailsVisible = true
    lateinit var adapterMyProfileMedia: AdapterMyProfileMedia
    private lateinit var adapterInterests: AdapterInterestInMyProfile
    lateinit var adapterLanguage:AdapterPersonalDetailsItem
    private lateinit var adapterInterestsInBottomSheet: AdapterInterestInMyProfile
    lateinit var layoutManager: FlexboxLayoutManager
    lateinit var bottomSheetLayoutManager: FlexboxLayoutManager
    lateinit var interestBottomSheetLayoutManager: FlexboxLayoutManager
    var userSelectedInterests: MutableList<InterestData?>? = mutableListOf()
    var userMedia: MutableList<com.ripenapps.adoreandroid.models.response_models.carddetails.Media>? =
        mutableListOf()

    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var ageHeightBottomSheetDialog: BottomSheetDialog
    lateinit var view1: View
    lateinit var view2: View
    lateinit var title: TextView
    private lateinit var cancelBottomsheet: ImageView
    private lateinit var cancelAgeHeightBottomSheet: ImageView
    private lateinit var title2: TextView
    lateinit var numberPicker: ScrollPicker
    lateinit var bottomsheetRecyclerView: RecyclerView
    lateinit var bottomSheetSelectButton: AppCompatTextView
    lateinit var ageHeightSelectButton: AppCompatTextView
    val myProfileViewModel by viewModels<MyProfileViewModel>()
    lateinit var adapterPersonalDetailsItem: AdapterPersonalDetailsItem
    var allListsDataResponse = QuestionsDataResponse()
    var whatElseYouLike: MutableList<InterestData> = mutableListOf()


    private var saveAt = ""
    private var vdo_path = ""
    private val VideoRequestCode = 122
    private var path = ""
    private var capturedImageUri: Uri? = null
    private var cameraGalleryCheck = 0   // 1 for camera, 2 for gallery
    private var profileImagePath: String = ""
    private var isImageChanged = false
    private var galleryImagePathList = arrayListOf<String>()
    private var galleryVideoPathList = arrayListOf<Uri>()
    var isLongPressActive = false
    private var imagerequestcode: Int = 0
    private var imagePath: String = ""

    override fun setLayout(): Int {
        return R.layout.fragment_edit_my_profile
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }


    override fun initView(savedInstanceState: Bundle?) {
        hitGetUserProfileDetailsApi()
        initBottomSheet()
        initAgeHeightBottomsheet()
        initRecyclerViewManagers()
        onClick()
        profileImagePath = Preferences.getStringPreference(requireContext(), PROFILE).toString()
        adapterMyProfileMedia =
            AdapterMyProfileMedia(mutableListOf(), this, requireContext())
        Preferences.getStringPreference(requireContext(), USER_ID)
            ?.let { myProfileViewModel.hitGetAllListsDataApi(it) }
        Glide.with(requireContext())
            .load(Preferences.getStringPreference(requireContext(), PROFILE))
            .into(binding.profileImage)
    }

    private fun hitGetUserProfileDetailsApi() {
        Preferences.getStringPreference(requireContext(), TOKEN)
            ?.let { myProfileViewModel.hitGetUserProfileDetailsApi(it) }
    }

    private fun initRecyclerViewManagers() {
        layoutManager = FlexboxLayoutManager(requireContext())
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.alignItems = AlignItems.CENTER
        binding.interestsRecycler.layoutManager = layoutManager

        bottomSheetLayoutManager = FlexboxLayoutManager(requireContext())
        bottomSheetLayoutManager.flexDirection = FlexDirection.ROW
        bottomSheetLayoutManager.alignItems = AlignItems.CENTER
        bottomsheetRecyclerView.layoutManager = bottomSheetLayoutManager

        interestBottomSheetLayoutManager = FlexboxLayoutManager(requireContext())
        interestBottomSheetLayoutManager.flexDirection = FlexDirection.ROW;
        interestBottomSheetLayoutManager.alignItems = AlignItems.CENTER;
        bottomsheetRecyclerView.layoutManager = interestBottomSheetLayoutManager

        (bottomsheetRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false
        (binding.interestsRecycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false
        (bottomsheetRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false
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

    fun initAgeHeightBottomsheet() {
        ageHeightBottomSheetDialog =
            BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        view2 = LayoutInflater.from(requireContext()) //init bottomSheet
            .inflate(R.layout.bottomsheet_age_height_myprofile, null)
        ageHeightBottomSheetDialog.setContentView(view2)
        title2 = view2.findViewById<View>(R.id.title) as TextView
        cancelAgeHeightBottomSheet = view2.findViewById<View>(R.id.cancelAgeHeightBottomSheet) as ImageView
        cancelAgeHeightBottomSheet.isVisible = true
        numberPicker =
            view2.findViewById<View>(R.id.numberPicker) as ScrollPicker
        numberPicker.setShownItemCount(7)
        numberPicker.setSelectedTextSize(130F)
        numberPicker.setSelectorColor(binding.root.context.getColor(R.color.theme))
        numberPicker.setSelectedTextColor(binding.root.context.getColor(R.color.theme))
        numberPicker.setTextColor(binding.root.context.getColor(R.color.grey_boulder))
        numberPicker.setTextBold(true)
        numberPicker.setSelectorLineWidth(5F)
        ageHeightSelectButton =
            view2.findViewById<View>(R.id.button) as AppCompatTextView
    }

    private fun setObserver() {
        myProfileViewModel.getUpdateGalleryMediaLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            it.data?.message?.let { it1 ->
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            userMedia = it.data?.data?.updatedUser?.media
                            isLongPressActive = false
                            adapterMyProfileMedia.inactiveLongPress()

                            if (galleryVideoPathList.size>1){
                                galleryVideoPathList.removeAt(0)
                                uploadVideo()
                            }else{
                                hitGetUserProfileDetailsApi()
                            }
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
        myProfileViewModel.getUpdateProfileLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            it.data?.message?.let { it1 ->
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            Preferences.setStringPreference(
                                requireContext(),
                                PROFILE,
                                profileImagePath
                            )
                            Preferences.setStringPreference(
                                requireContext(),
                                NAME,
                                name
                            )
                            ProcessDialog.dismissDialog(true)
                            requireActivity().onBackPressed()
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
        myProfileViewModel.getInterestListLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            adapterInterestsInBottomSheet = AdapterInterestInMyProfile(
                                userSelectedInterests,
                                it.data?.data?.toMutableList(),
                                "bottomsheet",
                                this
                            )
                            bottomsheetRecyclerView.adapter = adapterInterestsInBottomSheet
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
        myProfileViewModel.getUserProfileDetailLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            binding.name.setText(it.data?.data?.user?.name)
                            binding.userId.setText(it.data?.data?.user?.userName)
                            binding.email.setText(it.data?.data?.user?.email)
                            email = it.data?.data?.user?.email.toString()
                            if (!it.data?.data?.user?.countryCode.isNullOrEmpty()){
                                selectedCountryCode= it.data?.data?.user?.countryCode.toString()
                                it.data?.data?.user?.countryCode?.substring(1)
                                    ?.let { it1 -> binding.countryCode.setCountryForPhoneCode(it1.toInt()) }
                            }
                            binding.mobileNumber.setText(it.data?.data?.user?.mobile)
                            mobileNumber = it.data?.data?.user?.mobile.toString()
                            if (!it.data?.data?.user?.gender.isNullOrEmpty() && it.data?.data?.user?.gender?.length!! > 1) {
                                binding.gender.text = it.data?.data?.user?.gender?.substring(0, 1)
                                    ?.toUpperCase() + it.data?.data?.user?.gender?.substring(1)
                            }
                            binding.userBio.setText(it.data?.data?.user?.bioDescription)
                            selectedGender = it.data?.data?.user?.gender.toString()
                            if (it.data?.data?.user?.age.toString()
                                    .isNullOrEmpty() || it.data?.data?.user?.age.toString() == "null"
                            ) {
                                binding.age.text = getString(R.string.select)
                            } else {
                                binding.age.text = it.data?.data?.user?.age.toString()
                            }
                            selectedAge = it.data?.data?.user?.age.toString()
                            if (!it.data?.data?.user?.sexualOrientation.isNullOrEmpty() && !it.data?.data?.user?.sexualOrientation.equals("null", ignoreCase = true) ) {
                                selectedSexualOrientation =
                                    it.data?.data?.user?.sexualOrientation.toString()
                                if (it.data?.data?.user?.sexualOrientation?.length!! > 1){
                                    binding.sexualOrientation.text =
                                    it.data?.data?.user?.sexualOrientation?.substring(0, 1)
                                        ?.toUpperCase() + it.data?.data?.user?.sexualOrientation?.substring(
                                        1
                                    )
                                }else{
                                    binding.sexualOrientation.text =
                                        it.data?.data?.user?.sexualOrientation?.toUpperCase()
                                }
                            } else{
                                binding.sexualOrientation.text =getString(R.string.select)
                            }
                            if (it.data?.data?.user?.height.toString().isNullOrEmpty() || it.data?.data?.user?.height.toString() == "null"|| it.data?.data?.user?.height == "0") {
                                binding.height.text = getString(R.string.select)
                            } else {
                                binding.height.text = it.data?.data?.user?.height
                            }
                            selectedHeight = it.data?.data?.user?.height.toString()
                            if (it.data?.data?.user?.complexion.isNullOrEmpty() || it.data?.data?.user?.complexion == "null") {
                                binding.yourComplexion.text = getString(R.string.select)
                            } else {
                                binding.yourComplexion.text = it.data?.data?.user?.complexion
                            }
                            selectedComplexion = it.data?.data?.user?.complexion.toString()
                            if (it.data?.data?.user?.relationshipGoal.isNullOrEmpty() || it.data?.data?.user?.relationshipGoal == "null") {
                                binding.relationshipGoal.text = getString(R.string.select)
                            } else {
                                binding.relationshipGoal.text = it.data?.data?.user?.relationshipGoal
                            }
                            relationshipGoal = it.data?.data?.user?.relationshipGoal.toString()
                            if (it.data?.data?.user?.language.isNullOrEmpty() /*|| it.data?.data?.user?.language == "null"*/) {
                                binding.language.text = getString(R.string.select)
                            } else {
                                selectedLanguage = it.data?.data?.user?.language!!
                                if (it.data?.data?.user?.language?.size!! == 1){
                                    binding.language.text = it.data?.data?.user?.language?.get(0)
                                } else{
                                    binding.language.text = it.data?.data?.user?.language?.get(0)+"  +"+(it.data?.data?.user?.language?.size!! -1).toString()
                                }
                            }
                            if (!it.data?.data?.user?.interestedIn.isNullOrEmpty() && !it.data?.data?.user?.interestedIn.equals("null", ignoreCase = true) ) {
                                if (it.data?.data?.user?.interestedIn.toString().equals("male", ignoreCase = true)) {
                                    interestedIn = getString(R.string.male)
                                } else if (it.data?.data?.user?.interestedIn.toString().equals("feMale", ignoreCase = true)) {
                                    interestedIn = getString(R.string.female)
                                } else if(it.data?.data?.user?.interestedIn.toString().equals("everyOne", ignoreCase = true)){
                                    interestedIn = getString(R.string.everyone)
                                }
                                binding.interestedIn.text =interestedIn
                                /*if (it.data?.data?.user?.interestedIn?.length!! > 1){
                                    binding.interestedIn.text =
                                        it.data?.data?.user?.interestedIn?.substring(0, 1)
                                            ?.toUpperCase() + it.data?.data?.user?.interestedIn?.substring(
                                            1
                                        )
                                }else{
                                    binding.interestedIn.text =
                                        it.data?.data?.user?.interestedIn?.toUpperCase()
                                }*/
                            } else{
                                binding.interestedIn.text = getString(R.string.select)
                            }
                            whatElseYouLike = it.data?.data?.user?.whatElseYouLike!!
                            bioDescription = it.data?.data?.user?.bioDescription.toString()
                            profileImagePath = it.data?.data?.user?.profileUrl.toString()
                            selectedAddress = it.data?.data?.user?.address.toString()
                            userMedia = it.data?.data?.userMedia
                            if (it.data?.data?.userMedia != null && it.data?.data?.userMedia.size >= 1) {
                                adapterMyProfileMedia = AdapterMyProfileMedia(
                                    userMedia,
                                    this,
                                    requireContext()
                                )
                            } else {
                                adapterMyProfileMedia = AdapterMyProfileMedia(
                                    mutableListOf(),
                                    this,
                                    requireContext()
                                )
                            }

                            binding.myProfileGalleryRecycler.adapter = adapterMyProfileMedia
                            if (!it.data?.data?.user?.lifestyle?.drink.isNullOrEmpty()) {
                                selectedDrinkType = it.data?.data?.user?.lifestyle?.drink.toString()
                                binding.howOftenDrink.text = it.data?.data?.user?.lifestyle?.drink
                            } else {
                                binding.howOftenDrink.text = getString(R.string.select)
                            }
                            if (!it.data?.data?.user?.lifestyle?.smoke.isNullOrEmpty()) {
                                selectedSmokeType = it.data?.data?.user?.lifestyle?.smoke.toString()
                                binding.howOftenSmoke.text = it.data?.data?.user?.lifestyle?.smoke
                            } else {
                                binding.howOftenSmoke.text = getString(R.string.select)
                            }
                            if (!it.data?.data?.user?.lifestyle?.profession.isNullOrEmpty()) {
                                selectedProfession =
                                    it.data?.data?.user?.lifestyle?.profession.toString()
                                binding.profession.text = it.data?.data?.user?.lifestyle?.profession
                            } else {
                                binding.profession.text = getString(R.string.select)
                            }
                            if (!it.data?.data?.user?.lifestyle?.petes.isNullOrEmpty()) {
                                selectedPet = it.data?.data?.user?.lifestyle?.petes.toString()
                                binding.pets.text = it.data?.data?.user?.lifestyle?.petes
                            } else {
                                binding.pets.text = getString(R.string.select)
                            }
                            if (!it.data?.data?.user?.whatMakesYou?.communication.isNullOrEmpty()) {
                                selectedCommunicationStyle =
                                    it.data?.data?.user?.whatMakesYou?.communication.toString()
                                binding.communicationStyle.text =
                                    it.data?.data?.user?.whatMakesYou?.communication
                            } else {
                                binding.communicationStyle.text = getString(R.string.select)
                            }
                            if (!it.data?.data?.user?.whatMakesYou?.educationLevel.isNullOrEmpty()) {
                                selectedEducationLevel =
                                    it.data?.data?.user?.whatMakesYou?.educationLevel.toString()
                                binding.educationalLevel.text =
                                    it.data?.data?.user?.whatMakesYou?.educationLevel
                            } else {
                                binding.educationalLevel.text = getString(R.string.select)
                            }
                            if (!it.data?.data?.user?.whatMakesYou?.zodiacSign.isNullOrEmpty()) {
                                selectedZodiacSign =
                                    it.data?.data?.user?.whatMakesYou?.zodiacSign.toString()
                                binding.zodiacSign.text =
                                    it.data?.data?.user?.whatMakesYou?.zodiacSign
                            } else {
                                binding.zodiacSign.text = getString(R.string.select)
                            }
                            userSelectedInterests = it.data?.data?.user?.topInterests
                            adapterInterests = AdapterInterestInMyProfile(
                                it.data?.data?.user?.topInterests,
                                mutableListOf(), "fragment", this
                            )
                            binding.interestsRecycler.adapter = adapterInterests
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
        myProfileViewModel.getAllListsLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            allListsDataResponse = it?.data
                        }

                        else -> {}
                    }
//                    binding.idLoadar.root.visibility = View.GONE
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
//                    binding.idLoadar.root.visibility = View.GONE
                }

            }
        }
    }
    private fun askCameraStoragePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val cameraPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            val storageWritePermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val storageReadPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)

            if (cameraPermission != PackageManager.PERMISSION_GRANTED ||
                storageWritePermission != PackageManager.PERMISSION_GRANTED/* ||
                storageReadPermission != PackageManager.PERMISSION_GRANTED*/
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE/*,
                        Manifest.permission.READ_EXTERNAL_STORAGE*/
                    ),
                    CAMERA_STORAGE_PERMISSION_REQUEST_CODE
                )
            } else{
                openCameraGalleryBottomSheet()
            }
        }
    }
        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_STORAGE_PERMISSION_REQUEST_CODE -> {
                // Check if all permissions are granted
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    openCameraGalleryBottomSheet()
                    // Permissions granted, handle accordingly
                } else {
                    // Permissions denied, show a toast
                    Toast.makeText(requireContext(), getString(R.string.permission_denied_cannot_access_camera_or_storage), Toast.LENGTH_SHORT).show()
                }
            }
            // Handle other permission requests if any
        }
    }
    private fun askPermission() {
        val permission: Array<String?> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.CAMERA
            )
            else arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)

        if (CommonUtils.requestPermissionsNew(requireActivity(), 100, permission)) {
            imagerequestcode = 1
            if (isPersonalDetailsVisible){
                openProfileImageUpdateBottomsheet()
            }else{
                openCameraGalleryBottomSheet()
            }
        }
    }
    private fun onClick() {
        cancelAgeHeightBottomSheet.setOnClickListener {
            ageHeightBottomSheetDialog.dismiss()
        }
        cancelBottomsheet.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        binding.backButton.setOnClickListener {
            if (isLongPressActive && !isPersonalDetailsVisible) {
                isLongPressActive = false
                adapterMyProfileMedia.inactiveLongPress()
            } else {
                requireActivity().onBackPressed()
            }
        }
        binding.mainLayout.setOnClickListener {
            if (isLongPressActive && !isPersonalDetailsVisible) {
                isLongPressActive = false
                adapterMyProfileMedia.inactiveLongPress()
            }
        }
        binding.cameraImage.setOnClickListener {
            if (isPersonalDetailsVisible) {
                profileImagePath = ""
                askPermission()
            }
        }
        binding.selectPersonalDetail.setOnClickListener {
            if (!isPersonalDetailsVisible) {
                binding.cameraImage.isVisible = true
                isPersonalDetailsVisible = true
                binding.personalDetailScrollview.isVisible = true
                binding.GalleryScrollview.isVisible = false
                binding.viewPerDet.setBackgroundResource(R.color.theme)
                binding.viewGallery.setBackgroundResource(R.color.white_whisper)
                binding.personalDetailText.setTextColor(requireContext().resources.getColor(R.color.black_mine_shaft))
                binding.galleyText.setTextColor(requireContext().resources.getColor(R.color.grey_boulder))
                binding.updateProfileButton.text =
                    requireContext().resources.getString(R.string.update_profile)
            }
        }
        binding.selectGallery.setOnClickListener {
            if (isPersonalDetailsVisible) {
                binding.cameraImage.isVisible = false
                isPersonalDetailsVisible = false
                binding.personalDetailScrollview.isVisible = false
                binding.GalleryScrollview.isVisible = true
                binding.viewPerDet.setBackgroundResource(R.color.white_whisper)
                binding.viewGallery.setBackgroundResource(R.color.theme)
                binding.personalDetailText.setTextColor(requireContext().resources.getColor(R.color.grey_boulder))
                binding.galleyText.setTextColor(requireContext().resources.getColor(R.color.black_mine_shaft))
                binding.updateProfileButton.text =
                    requireContext().resources.getString(R.string.delete)
//                adapterMyProfileMedia.updateMediaList(userMedia)
            }
        }
        binding.uploadImage.setOnClickListener {
            askPermission()
        }
        binding.gender.setOnClickListener {
            selectedItem = ""
            listCheck = "gender"
            bottomSheetDialog.show()
            title.setText(getString(R.string.select_gender))
            adapterPersonalDetailsItem = AdapterPersonalDetailsItem(
                allListsDataResponse.data.genderData,
                ::getSelectedItem,
                selectedGender,
                listCheck
            )
            bottomsheetRecyclerView.adapter = adapterPersonalDetailsItem
        }
        binding.howOftenDrink.setOnClickListener {
            selectedItem = ""
            var list = mutableListOf<GenderData>()
            for ((index, value) in allListsDataResponse.data.lifestyleData.get(0).drink?.withIndex()!!) {
                list.add(GenderData(0, "", value, false, false))
            }
            listCheck = "drink"
            bottomSheetDialog.show()
            title.text = requireContext().getString(R.string.how_often_do_you_drink)
            adapterPersonalDetailsItem =
                AdapterPersonalDetailsItem(list, ::getSelectedItem, selectedDrinkType, listCheck)
            bottomsheetRecyclerView.adapter = adapterPersonalDetailsItem
        }
        binding.howOftenSmoke.setOnClickListener {
            selectedItem = ""
            var list = mutableListOf<GenderData>()
            for ((index, value) in allListsDataResponse.data.lifestyleData.get(0).smoke?.withIndex()!!) {
                list.add(GenderData(0, "", value, false, false))
            }
            listCheck = "smoke"
            bottomSheetDialog.show()
            title.text = requireContext().getString(R.string.how_often_do_you_smoke)
            adapterPersonalDetailsItem =
                AdapterPersonalDetailsItem(list, ::getSelectedItem, selectedSmokeType, listCheck)
            bottomsheetRecyclerView.adapter = adapterPersonalDetailsItem
        }
        binding.profession.setOnClickListener {
            selectedItem = ""
            var list = mutableListOf<GenderData>()
            for ((index, value) in allListsDataResponse.data.lifestyleData.get(0).profession?.withIndex()!!) {
                list.add(GenderData(0, "", value, false, false))
            }
            listCheck = "profession"
            bottomSheetDialog.show()
            title.text = requireContext().getString(R.string.your_profession)
            adapterPersonalDetailsItem =
                AdapterPersonalDetailsItem(list, ::getSelectedItem, selectedProfession, listCheck)
            bottomsheetRecyclerView.adapter = adapterPersonalDetailsItem
        }
        binding.pets.setOnClickListener {
            selectedItem = ""
            var list = mutableListOf<GenderData>()
            for ((index, value) in allListsDataResponse.data.lifestyleData.get(0).pets?.withIndex()!!) {
                list.add(GenderData(0, "", value, false, false))
            }
            listCheck = "pets"
            bottomSheetDialog.show()
            title.text = requireContext().getString(R.string.do_you_have_any_pets)
            adapterPersonalDetailsItem =
                AdapterPersonalDetailsItem(list, ::getSelectedItem, selectedPet, listCheck)
            bottomsheetRecyclerView.adapter = adapterPersonalDetailsItem
        }
        binding.communicationStyle.setOnClickListener {
            selectedItem = ""
            var list = mutableListOf<GenderData>()
            for ((index, value) in allListsDataResponse.data.makesYouData.get(0).communication?.withIndex()!!) {
                list.add(GenderData(0, "", value, false, false))
            }
            listCheck = "communication"
            bottomSheetDialog.show()
            title.text = requireContext().getString(R.string.what_is_your_communication_style)
            adapterPersonalDetailsItem =
                AdapterPersonalDetailsItem(
                    list,
                    ::getSelectedItem,
                    selectedCommunicationStyle,
                    listCheck
                )
            bottomsheetRecyclerView.adapter = adapterPersonalDetailsItem
        }
        binding.educationalLevel.setOnClickListener {
            selectedItem = ""
            var list = mutableListOf<GenderData>()
            for ((index, value) in allListsDataResponse.data.makesYouData.get(0).education?.withIndex()!!) {
                list.add(GenderData(0, "", value, false, false))
            }
            listCheck = "education"
            bottomSheetDialog.show()
            title.text = requireContext().getString(R.string.what_is_your_education_level)
            adapterPersonalDetailsItem =
                AdapterPersonalDetailsItem(
                    list,
                    ::getSelectedItem,
                    selectedEducationLevel,
                    listCheck
                )
            bottomsheetRecyclerView.adapter = adapterPersonalDetailsItem
        }
        binding.zodiacSign.setOnClickListener {
            selectedItem = ""
            var list = mutableListOf<GenderData>()
            for ((index, value) in allListsDataResponse.data.makesYouData.get(0).zodiac?.withIndex()!!) {
                list.add(GenderData(0, "", value, false, false))
            }
            listCheck = "zodiac"
            bottomSheetDialog.show()
            title.text = requireContext().getString(R.string.what_is_your_zodiac_sign)
            adapterPersonalDetailsItem =
                AdapterPersonalDetailsItem(list, ::getSelectedItem, selectedZodiacSign, listCheck)
            bottomsheetRecyclerView.adapter = adapterPersonalDetailsItem
        }
        binding.sexualOrientation.setOnClickListener {
            selectedItem = ""
            listCheck = "sexualOrientation"
            bottomSheetDialog.show()
            title.text = requireContext().getString(R.string.your_sexual_orientation)
            adapterPersonalDetailsItem =
                AdapterPersonalDetailsItem(
                    allListsDataResponse.data.orientationData,
                    ::getSelectedItem,
                    selectedSexualOrientation,
                    listCheck
                )
            bottomsheetRecyclerView.adapter = adapterPersonalDetailsItem
        }
        binding.relationshipGoal.setOnClickListener {
            selectedItem = ""
            listCheck = "relationshipGoal"
            bottomSheetDialog.show()
            title.text = requireContext().getString(R.string.right_now_i_m_looking_for)
            adapterPersonalDetailsItem =
                AdapterPersonalDetailsItem(
                    allListsDataResponse.data.lookingForData,
                    ::getSelectedItem,
                    relationshipGoal,
                    listCheck
                )
            bottomsheetRecyclerView.adapter = adapterPersonalDetailsItem
        }
        binding.interestedIn.setOnClickListener {
            selectedItem = ""
            listCheck = "interestedIn"
            bottomSheetDialog.show()
            title.text = requireContext().getString(R.string.who_are_you_interested_in)
            adapterPersonalDetailsItem =
                AdapterPersonalDetailsItem(
                    /*allListsDataResponse.data.lookingForData*/interestedInList(),
                    ::getSelectedItem,
                    interestedIn,
                    listCheck
                )
            bottomsheetRecyclerView.adapter = adapterPersonalDetailsItem
        }
        binding.yourComplexion.setOnClickListener {
            selectedItem = ""
            listCheck = "complexion"
            bottomSheetDialog.show()
            title.text = requireContext().getString(R.string.your_complexion)
            adapterPersonalDetailsItem =
                AdapterPersonalDetailsItem(
                    allListsDataResponse.data.complexionData,
                    ::getSelectedItem,
                    selectedComplexion,
                    listCheck
                )
            bottomsheetRecyclerView.adapter = adapterPersonalDetailsItem
        }
        binding.language.setOnClickListener {
            selectedItem = ""
            listCheck = "language"
            bottomSheetDialog.show()
            title.text = requireContext().getString(R.string.language_you_speak)
            adapterLanguage = AdapterPersonalDetailsItem(
                allListsDataResponse.data.languageData,
                ::getSelectedItem,
                "",
                listCheck,
                "multiple",
                selectedLanguage
            )
            adapterPersonalDetailsItem = adapterLanguage

            bottomsheetRecyclerView.adapter = adapterPersonalDetailsItem
        }
        binding.height.setOnClickListener {
            selectedItem = ""
            listCheck = "height"
            title2.text = getText(R.string.height_in_cm)
            numberPicker.setItemsIntRange(90, 241)
            try {
                if (selectedHeight.toInt() > 90)
                    numberPicker.value = selectedHeight.toInt()
            } catch (e: NumberFormatException) {
                numberPicker.value = 170
                selectedItem = "170"
            }
            numberPicker.addOnValueChangedListener {
                selectedItem = numberPicker.selectedItemText
            }
            ageHeightBottomSheetDialog.show()

        }
        binding.age.setOnClickListener {
            selectedItem = ""
            listCheck = "age"
            title2.text = requireContext().getString(R.string.age)
            numberPicker.setItemsIntRange(18, 100)
            try {
                numberPicker.value = selectedAge.toInt()
            } catch (e: NumberFormatException) {
                numberPicker.value = 21
                selectedItem = "21"
            }
            numberPicker.addOnValueChangedListener {
                selectedItem = numberPicker.selectedItemText
            }
            ageHeightBottomSheetDialog.show()
        }
        binding.updateProfileButton.setOnClickListener {
            if (isPersonalDetailsVisible) {
                val builder = MultipartBody.Builder()
                builder.setType(MultipartBody.FORM)
                if (!binding.name.text.toString().trim()
                        .isNullOrEmpty() /*&& Regex("^[\\p{IsAlphabetic}\\p{IsDigit}]+\$").matches(
                        binding.name.text.toString().trim())*/
                ) {
                    name = binding.name.text.toString().trim()
                    try {
                        if (name.length == 1) {
                            name = name.toUpperCase()
                        } else {
                            name = name.substring(0, 1).toUpperCase() + name.substring(1)
                        }
                    } catch (e: NumberFormatException) {
                        Snackbar.make(
                            binding.name,
                            getString(R.string.number_format_exception),
                            Snackbar.LENGTH_SHORT
                        )
                            .show()
                    }

                    builder.addFormDataPart("name", name)
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_enter_a_valid_name),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (!binding.userBio.text?.trim().isNullOrEmpty()){
                    builder.addFormDataPart("bioDescription", binding.userBio.text?.trim().toString())
                }else{
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_enter_a_valid_bio),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (isImageChanged) {
                    val file = File(profileImagePath)
                    builder.addFormDataPart(
                        "profile",
                        "${System.currentTimeMillis()}_$file.name",
                        file.asRequestBody(getMimeType(file)?.toMediaTypeOrNull())
                    )
                }
                Preferences.getStringPreference(requireContext(), USER_NAME)
                    ?.let { it1 -> builder.addFormDataPart("userName", it1) }
                builder.addFormDataPart("email", email)
                builder.addFormDataPart("mobile", mobileNumber)
                builder.addFormDataPart("gender", selectedGender)
                builder.addFormDataPart("age", selectedAge)
                builder.addFormDataPart(
                    "sexualOrientation",
                    selectedSexualOrientation.toLowerCase()
                )
                builder.addFormDataPart("countryCode", selectedCountryCode)
                builder.addFormDataPart("height", selectedHeight)
                builder.addFormDataPart("complexion", selectedComplexion)
                builder.addFormDataPart("language", Gson().toJson(selectedLanguage))
                if(interestedIn.isNullOrEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.interestedin_field_is_mandatory),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (interestedIn.equals(getString(R.string.male), ignoreCase = true)) {
                    builder.addFormDataPart("interestedIn", "male")
                } else if (interestedIn.equals(getString(R.string.female), ignoreCase = true)) {
                    builder.addFormDataPart("interestedIn", "feMale")
                } else {
                    builder.addFormDataPart("interestedIn", "everyOne")
                }

//                builder.addFormDataPart("interestedIn", interestedIn)
                if(relationshipGoal.isNullOrEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.relationship_goal_field_is_mandatory),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                builder.addFormDataPart("relationshipGoal", relationshipGoal)
                builder.addFormDataPart("address", selectedAddress)
                builder.addFormDataPart("deviceType", "android")
                builder.addFormDataPart("deviceToken", Preferences.getStringPreference(requireContext(), FCM_TOKEN).toString())
                var lifestyle: Lifestyle? = Lifestyle()
                lifestyle?.drink = selectedDrinkType
                lifestyle?.smoke = selectedSmokeType
                lifestyle?.profession = selectedProfession
                lifestyle?.petes = selectedPet
                builder.addFormDataPart("lifestyle", Gson().toJson(lifestyle))
                var whatMakesYou: WhatMakesYou? = WhatMakesYou()
                whatMakesYou?.communication = selectedCommunicationStyle
                whatMakesYou?.educationLevel = selectedEducationLevel
                whatMakesYou?.zodiacSign = selectedZodiacSign
                builder.addFormDataPart("whatMakesYou", Gson().toJson(whatMakesYou))
                var topInterests = arrayListOf<String>()
                userSelectedInterests?.forEachIndexed { index, interestData ->
                    if (interestData != null) {
                        topInterests.add(interestData._id)
                    }
                }
                builder.addFormDataPart("topInterests", Gson().toJson(topInterests.toArray()))
                var whatElseYouLike = arrayListOf<String>()
                this.whatElseYouLike?.forEachIndexed { index, interestData ->
                    if (interestData != null) {
                        whatElseYouLike.add(interestData._id)
                    }
                }
                builder.addFormDataPart("whatElseYouLike", Gson().toJson(whatElseYouLike.toArray()))

                val requestBody = builder.build()
                Preferences.getStringPreference(requireContext(), TOKEN)
                    ?.let { it1 -> myProfileViewModel.hitUpdateProfileApi(it1, requestBody) }
            } else { //handle delete gallery media
                if (userMedia?.size==1||adapterMyProfileMedia.getMediaListToDelete()?.size == userMedia?.size){
                    createInfoDialog(
                        requireContext(),
                        getString(R.string.can_not_delete),
                        getString(R.string.you_cannot_delete_all_media),
                        true
                    )
                    return@setOnClickListener
                }
                if (adapterMyProfileMedia.getMediaListToDelete()?.size!! >0){
                    createYesNoDialog(
                        object : AppDialogListener {
                            override fun onPositiveButtonClickListener(dialog: Dialog) {
                                var deleteItemsIdList = arrayListOf<String>()
                                val builder = MultipartBody.Builder()
                                builder.setType(MultipartBody.FORM)
                                for ((_, item) in adapterMyProfileMedia.getMediaListToDelete()
                                    ?.withIndex()!!) {
                                    deleteItemsIdList.add(item?._id.toString())
                                }
                                builder.addFormDataPart("mediaDelete", Gson().toJson(deleteItemsIdList))
                                val requestBody = builder.build()
                                Preferences.getStringPreference(requireActivity(), TOKEN)
                                    ?.let {
                                        myProfileViewModel.hitUpdateGalleryMediaApi(it, requestBody)
                                    }
                                dialog.dismiss()
                            }

                            override fun onNegativeButtonClickListener(dialog: Dialog) {
                                dialog.dismiss()
                            }
                        },
                        requireContext(),
                        getString(R.string.delete_media),
                        getString(R.string.are_you_sure_you_want_to_delete_selected_media),
                        getString(R.string.yes),
                        getString(R.string.no)
                    )
                } else{
                    Snackbar.make(binding.profileImage, getString(R.string.please_select_a_media_to_delete), Snackbar.LENGTH_SHORT).show()
                }


            }
        }
        ageHeightSelectButton.setOnClickListener {
            when (listCheck) {
                "age" -> {
                    ageHeightBottomSheetDialog.dismiss()
                    selectedAge = selectedItem
                    binding.age.text = selectedItem
                }

                "height" -> {
                    ageHeightBottomSheetDialog.dismiss()
                    selectedHeight = selectedItem
                    binding.height.text = selectedItem
                }
            }
        }
        bottomSheetSelectButton.setOnClickListener {
            when (listCheck) {
                "gender" -> {
                    bottomSheetDialog.dismiss()
                    selectedGender = selectedItem
                    if (!selectedItem.isNullOrEmpty() && selectedItem.length > 1) {
                        binding.gender.text =
                            selectedItem.substring(0, 1)?.toUpperCase() + selectedItem?.substring(1)
                    }
                }

                "drink" -> {
                    bottomSheetDialog.dismiss()
                    selectedDrinkType = selectedItem
                    if (selectedItem == "") {
                        binding.howOftenDrink.text = getString(R.string.select)
                    } else {
                        binding.howOftenDrink.text = selectedItem
                    }
                }

                "smoke" -> {
                    bottomSheetDialog.dismiss()
                    selectedSmokeType = selectedItem
                    if (selectedItem == "") {
                        binding.howOftenSmoke.text = getString(R.string.select)
                    } else {
                        binding.howOftenSmoke.text = selectedItem
                    }
                }

                "profession" -> {
                    bottomSheetDialog.dismiss()
                    selectedProfession = selectedItem
                    if (selectedItem == "") {
                        binding.profession.text = getString(R.string.select)
                    } else {
                        binding.profession.text = selectedItem
                    }
                }

                "pets" -> {
                    bottomSheetDialog.dismiss()
                    selectedPet = selectedItem
                    if (selectedItem == "") {
                        binding.pets.text = getString(R.string.select)
                    } else {
                        binding.pets.text = selectedItem
                    }
                }

                "communication" -> {
                    bottomSheetDialog.dismiss()
                    selectedCommunicationStyle = selectedItem
                    if (selectedItem == "") {
                        binding.communicationStyle.text = getString(R.string.select)
                    } else {
                        binding.communicationStyle.text = selectedItem
                    }
                }

                "education" -> {
                    bottomSheetDialog.dismiss()
                    selectedEducationLevel = selectedItem
                    if (selectedItem == "") {
                        binding.educationalLevel.text = getString(R.string.select)
                    } else {
                        binding.educationalLevel.text = selectedItem
                    }
                }

                "zodiac" -> {
                    bottomSheetDialog.dismiss()
                    selectedZodiacSign = selectedItem
                    if (selectedItem == "") {
                        binding.zodiacSign.text = getString(R.string.select)
                    } else {
                        binding.zodiacSign.text = selectedItem
                    }
                }

                "sexualOrientation" -> {
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

                "interest" -> {
                    var list = adapterInterestsInBottomSheet.getSelectedItemsList()
                    if (list?.size==0){
                        Toast.makeText(requireContext(), getString(R.string.please_select_atleast_one_interest), Toast.LENGTH_SHORT).show()
                    }else{
                        userSelectedInterests = list
                        adapterInterests.updateList(userSelectedInterests)
                        bottomSheetDialog.dismiss()
                    }
                }

                "complexion" -> {
                    bottomSheetDialog.dismiss()
                    selectedComplexion = selectedItem
                    if (selectedItem == "") {
                        binding.yourComplexion.text = getString(R.string.select)
                    } else {
                        binding.yourComplexion.text = selectedItem
                    }
                }
                "relationshipGoal" -> {
                    bottomSheetDialog.dismiss()
//                    if (!relationshipGoal.isNullOrEmpty()){
//                        relationshipGoal = selectedItem
//                        binding.relationshipGoal.text = selectedItem
//                    }
                    relationshipGoal = selectedItem
                    if (selectedItem == "") {
                        binding.relationshipGoal.text = getString(R.string.select)
                    } else {
                        binding.relationshipGoal.text = selectedItem
                    }
                }

                "language" -> {
                    bottomSheetDialog.dismiss()
                    selectedLanguage = adapterLanguage.getSelectedItemList()
                    if (selectedLanguage.isNullOrEmpty()) {
                        binding.language.text = getString(R.string.select)
                    } else if (selectedLanguage.size==1){
                        binding.language.text = selectedLanguage[0]
                    } else{
                        binding.language.text = selectedLanguage[0] +"  +"+(selectedLanguage.size-1).toString()
                    }
                }
                "interestedIn"->{
                    if (selectedItem==""){
                        Toast.makeText(requireContext(), getString(R.string.please_select_your_interest), Toast.LENGTH_SHORT).show()
                    }else{
                        bottomSheetDialog.dismiss()
                        interestedIn = selectedItem
                        binding.interestedIn.text = selectedItem
                    }
                }
            }
        }
    }
    private fun getSelectedItem(selectedPosition: Int, selectedItem: String) {
        this.listCheck = listCheck
        this.selectedItem = selectedItem
    }
    private fun interestedInList():List<GenderData>{
        var list = listOf<GenderData>(
            GenderData(name = getString(R.string.male), status = false, isSelected = false),
            GenderData(name = getString(R.string.female), status = false, isSelected = false),
            GenderData(name = getString(R.string.everyone), status = false, isSelected = false), )
        return list
    }
    override fun openInterestBottomSheet() {
        listCheck = "interest"
        title.text = getString(R.string.select_interest)
        bottomSheetSelectButton.text = getString(R.string.select)
        adapterInterestsInBottomSheet = AdapterInterestInMyProfile(
            mutableListOf(),
            mutableListOf(), "bottomsheet", this
        )
        bottomSheetDialog.show()
        myProfileViewModel.hitGetInterestDataApi()
    }

    override fun getSelectedInterestInBottomSheet() {}


    /*override fun onPermissionGranted(requestCode: Int) {
        if (requestCode == cameraPermission.requestCode) {
            // selectMedia()
            if (isPersonalDetailsVisible) {
                openProfileImageUpdateBottomsheet()
            } else {
                openCameraGalleryBottomSheet()
            }
        } else {

            askForPermission(requireContext(), cameraPermission, this, showMessage = false)
        }
    }*/

    private fun openProfileImageUpdateBottomsheet() {
        val bottom = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        val view1: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_select_camera_gallery, null)
        bottom.setContentView(view1)
        val title = view1.findViewById<View>(R.id.title) as TextView
        val camera = view1.findViewById<View>(R.id.camera) as TextView
        val gallery = view1.findViewById<View>(R.id.gallery) as TextView
        title.setText(getString(R.string.update_profile_image))
        camera.setOnClickListener {
            bottom.dismiss()
            openCameraImages()
        }

        gallery.setOnClickListener {
            bottom.dismiss()
            selectGalleryImage=true
            openGalleryImage()
        }
        bottom.show()
    }
    private fun openCameraGalleryBottomSheet() {
        val bottom = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        val view1: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_select_camera_gallery, null)
        bottom.setContentView(view1)
        val title = view1.findViewById<View>(R.id.title) as TextView
        val camera = view1.findViewById<View>(R.id.camera) as TextView
        val gallery = view1.findViewById<View>(R.id.gallery) as TextView
        title.setText(getString(R.string.all_media))
        camera.setOnClickListener {
            cameraGalleryCheck = 1
            bottom.dismiss()
            openPhotoVideoBottomSheet()
        }

        gallery.setOnClickListener {
            cameraGalleryCheck = 2
            bottom.dismiss()
            openPhotoVideoBottomSheet()
        }
        bottom.show()
    }

    private fun openPhotoVideoBottomSheet() {
        val bottom = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        val view1: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_select_camera_gallery, null)
        bottom.setContentView(view1)
        val title = view1.findViewById<View>(R.id.title) as TextView
        val photo = view1.findViewById<View>(R.id.camera) as TextView
        val video = view1.findViewById<View>(R.id.gallery) as TextView
        if (cameraGalleryCheck == 1) {
            title.text = getString(R.string.click)
            photo.text = getString(R.string.photo)
            video.text = getString(R.string.video)
        } else {
            title.text = getString(R.string.open)
            photo.text = getString(R.string.photos)
            video.text = getString(R.string.videos)
        }
        photo.setOnClickListener {
            if (cameraGalleryCheck == 1) {
                openCameraImages()
            } else {
                selectGalleryImage=true
                openGalleryImage()
            }
            bottom.dismiss()
        }

        video.setOnClickListener {
            if (cameraGalleryCheck == 1) {
                openCameraVideos()
            } else {
                selectGalleryImage=false    //selected galleryvideo, so set it false
                openGalleryVideo()
            }
            bottom.dismiss()
        }
        bottom.show()
    }

    private fun openGalleryImage() {
        galleryImagePathList.clear()
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        if (!isPersonalDetailsVisible) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startForImageGallery.launch(intent)

    }

    private val startForImageGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data!!.clipData != null) {
                    for (i in 0 until data.clipData!!.itemCount) {
                        if (galleryImagePathList.size>=5||galleryVideoPathList.size>=5){
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.can_upload_upto_five_images_at_a_time),
                                Toast.LENGTH_SHORT
                            ).show()
                            break
                        }
                        val imageUri = data.clipData!!.getItemAt(i).uri
                        getMediaPath(imageUri, i)
                        if (isPersonalDetailsVisible){
                            Glide.with(requireContext()).load(imagePath).into(binding.profileImage)
                            profileImagePath = imagePath
                            this.path = imagePath
                            isImageChanged = true
                            break
                        }
                        val fileSize = CommonUtils.checkVideoFileSize(imagePath)

                        if (fileSize < 100) {
                            galleryImagePathList.add(imagePath)

                        }else{
                            Toast.makeText(
                                context,
                                getString(R.string.selected_file_size_is_too_big),
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    }
                    if (data!!.clipData != null) {
                        uploadMedia()

                    }
                } else {
                    val imageUri = data.data
                    /*if (fileUri!!.path!!.isNotEmpty()) {
                        imagePath =
                            CommonUtils.getRealPathFromURI(requireActivity(), fileUri)
                        Log.e("TAG", "datadata:${imagePath} ")
                        galleryImagePathList.add(imagePath)
//                        myStoryMediaPathList.add(imagePath)
                    }*/
                    getMediaPath(imageUri!!, 0)
                    if (isPersonalDetailsVisible){
                        Glide.with(requireContext()).load(imagePath).into(binding.profileImage)
                        profileImagePath = imagePath
                        this.path = imagePath
                        isImageChanged = true
                    }
                    val fileSize = CommonUtils.checkVideoFileSize(imagePath)

                    if (fileSize < 100) {
                        galleryImagePathList.add(imagePath)

                    }else{
                        Toast.makeText(
                            context,
                            getString(R.string.selected_file_size_is_too_big),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    if (data?.data != null) {
                        uploadMedia()

                    }
                }
            }
        }

    private fun uploadMedia() {
        if (isPersonalDetailsVisible) {
            Glide.with(requireContext()).load(path).into(binding.profileImage)
            profileImagePath = path
            isImageChanged = true
        } else {
            if (selectGalleryImage){
                val builder = MultipartBody.Builder()
                builder.setType(MultipartBody.FORM)
                for ((index, path) in galleryImagePathList?.withIndex()!!) {
                    val partName = "images"
                    val file = File(path)
                    builder.addFormDataPart(
                        partName,
                        "${System.currentTimeMillis()}_$index${file.name}",
                        file.asRequestBody(getMimeType(file)?.toMediaTypeOrNull())
                    )
                }
                val requestBody = builder.build()
                Preferences.getStringPreference(requireActivity(), TOKEN)
                    ?.let {
                        myProfileViewModel.hitUpdateGalleryMediaApi(it, requestBody)
                    }
            }else{
                uploadVideo()
            }

        }
    }

    private fun getMediaPath(imageUri: Uri, i:Int) {

        if (!selectGalleryImage){
            galleryVideoPathList.add(imageUri)
        }
        if (imageUri != null && CommonUtils.isImageUri(
                requireContext().contentResolver,
                imageUri
            )
        ) {
            imagePath = java.lang.String.valueOf(
                CommonUtils.createFileSmall(
                    CommonUtils.getRealPathFromDocumentUri(
                        requireContext(),
                        imageUri
                    ), requireContext()
                )
            )
        } else {
            imagePath = CommonUtils.getFilePath(requireContext(), imageUri)!!
        }
    }

    fun uploadVideo(){
        val mp = MediaPlayer.create(requireContext(), galleryVideoPathList[0])
        if (mp != null) {
            val duration = mp.duration
            if (duration > 30000) {
                Toast.makeText(
                    context,
                    getString(R.string.please_select_videos_less_than_30_secs),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                compressvideo(galleryVideoPathList[0])
            }
        } else {
            Snackbar.make(
                binding.headerLayout,
                getString(R.string.unsupported_file_format),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
    private fun openGalleryVideo() {
        galleryVideoPathList.clear()
        galleryImagePathList.clear()
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startForImageGallery.launch(intent)
    }

    private fun openCameraImages() {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val fileName = "temp.jpg"
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, fileName)
            capturedImageUri =
                requireActivity().contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri)
            startActivityForResult(takePictureIntent, 2)


//        }
    }

    private fun openCameraVideos() {
            val videoCapture = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            videoCapture.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
            // videoCapture.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 8491520L); // 8MB
            videoCapture.putExtra(MediaStore.Video.Thumbnails.HEIGHT, 320)
            videoCapture.putExtra(MediaStore.Video.Thumbnails.WIDTH, 240)
            val maxVideoSize = (50 * 1024 * 1024).toLong() // 50 MB
            videoCapture.putExtra(MediaStore.EXTRA_SIZE_LIMIT, maxVideoSize)
            startActivityForResult(videoCapture, VideoRequestCode)
//        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 2) {//camera_img
            if (resultCode == Activity.RESULT_OK) {
                path = ImageUtils.getInstant().createFileSmall(
                    ImageUtils.getInstant().getRealPathFromUri_(
                        requireContext(),
                        capturedImageUri
                    ), requireContext()
                ).absolutePath
                if (isPersonalDetailsVisible) {
                    Glide.with(requireContext()).load(path).into(binding.profileImage)
                    profileImagePath = path
                    isImageChanged = true
                } else {
                    val builder = MultipartBody.Builder()
                    builder.setType(MultipartBody.FORM)
                    val file = File(path)
                    builder.addFormDataPart(
                        "images",
                        "${System.currentTimeMillis()}_${file.name}",
                        file.asRequestBody(getMimeType(file)?.toMediaTypeOrNull())
                    )
                    val requestBody = builder.build()
                    Preferences.getStringPreference(requireActivity(), TOKEN)
                        ?.let {
                            myProfileViewModel.hitUpdateGalleryMediaApi(it, requestBody)
                        }
                }

            }
        } else if (requestCode == FilePickerConst.REQUEST_CODE_PHOTO) {//gallery_img
            val list = data?.extras?.getParcelableArrayList<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)
            list?.forEach {
                val path = ImageUtils.getInstant().createFileSmall(
                    ImageUtils.getInstant().getRealPathFromUri_(
                        requireContext(), it
                    ), requireContext()
                ).absolutePath
                if (isPersonalDetailsVisible)
                    this.path = path
                galleryImagePathList.add(path)
            }
            if (list != null && list.size >= 1) {
                if (isPersonalDetailsVisible) {
                    Glide.with(requireContext()).load(path).into(binding.profileImage)
                    profileImagePath = path
                    isImageChanged = true
                } else {
                    val builder = MultipartBody.Builder()
                    builder.setType(MultipartBody.FORM)
                    for ((index, path) in galleryImagePathList?.withIndex()!!) {
                        val partName = "images"
                        val file = File(path)
                        builder.addFormDataPart(
                            partName,
                            "${System.currentTimeMillis()}_$index${file.name}",
                            file.asRequestBody(getMimeType(file)?.toMediaTypeOrNull())
                        )
                    }
                    val requestBody = builder.build()
                    Preferences.getStringPreference(requireActivity(), TOKEN)
                        ?.let {
                            myProfileViewModel.hitUpdateGalleryMediaApi(it, requestBody)
                        }
                }
            }

        } else if (requestCode == VideoRequestCode) { //camera_video
            if (resultCode == Activity.RESULT_OK && data!!.data != null) {
                Log.d("MYVideo", Gson().toJson(data!!.data!!))

                val version = android.os.Build.VERSION.SDK_INT //version 30 camera video issue
                if (version > 29) {
                    saveAt = ""
                } else {
                    saveAt = Environment.DIRECTORY_DOWNLOADS
                }
//                showProgress()

                VideoCompressor.start(
                    context = requireContext(), // => This is required
                    uris = listOf(data!!.data!!), // => Source can be provided as content uris
                    isStreamable = true,
                    saveAt = saveAt,
                    listener = object : CompressionListener {
                        override fun onProgress(index: Int, percent: Float) {
                            // Update UI with progress value
                            requireActivity().runOnUiThread {
                                Log.d("MYVideo", "Progresss")
                            }
                            ProcessDialog.showDialog(requireActivity(), true)
//                            showProgress()
                        }

                        override fun onStart(index: Int) {
                            // Compression start
                            Log.d("MYVideo", "Start")
//                            showProgress()
                            ProcessDialog.showDialog(requireActivity(), true)
                        }

                        override fun onSuccess(index: Int, size: Long, path: String?) {
                            // On Compression success
                            Log.d("MYVideo", "Succeess  $size " + path)
//                            hideProgress()
                            vdo_path = path!!
                            val builder = MultipartBody.Builder()
                            builder.setType(MultipartBody.FORM)
                            val file = File(path)
                            builder.addFormDataPart(
                                "video",
                                "${System.currentTimeMillis()}_$index${file.name}",
                                file.asRequestBody(getMimeType(file)?.toMediaTypeOrNull())
                            )
                            val retriever = MediaMetadataRetriever()
                            try {
                                retriever.setDataSource(path)

                                // Get the first frame (thumbnail) of the video
                                val bitmap = retriever.frameAtTime

                                // Convert the Bitmap to a byte array
                                val byteArray =
                                    bitmap?.let { CommonUtils.bitmapToByteArray(it) }

                                // Now you can send the byteArray to your API
                                byteArray?.toRequestBody("multipart/form-data".toMediaTypeOrNull())
                                    ?.let {
                                        builder.addFormDataPart(
                                            "videoThumbnail",
                                            file.name,
                                            it
                                        )
                                    }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                retriever.release()
                            }
                            val requestBody = builder.build()
                            Preferences.getStringPreference(requireActivity(), TOKEN)
                                ?.let {
                                    myProfileViewModel.hitUpdateGalleryMediaApi(it, requestBody)
                                }
                            ProcessDialog.dismissDialog(true)

                        }

                        override fun onFailure(index: Int, failureMessage: String) {
                            // On Failure
//                            hideProgress()
                            ProcessDialog.dismissDialog(true)
                            Log.d("MYVideo", "Failuure")

                        }

                        override fun onCancelled(index: Int) {
                            // On Cancelled
//                            hideProgress()
                            ProcessDialog.dismissDialog(true)
                            Log.d("MYVideo", "Cancel")

                        }

                    },
                    configureWith = Configuration(
                        quality = VideoQuality.VERY_HIGH,
                        frameRate = 60, /*Int, ignore, or null*/
                        isMinBitrateCheckEnabled = true,

                        )
                )
            }

        } else if (requestCode.toString() == FilePickerConst.REQUEST_CODE_MEDIA_DETAIL.toString()) { //gallery_vdo
            val list = data?.extras?.getParcelableArrayList<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)
            list?.forEach {
                val path = ContentUriUtils.getFilePath(requireContext(), it)
                val mp = MediaPlayer.create(requireContext(), it)
                if (mp != null) {
                    val duration = mp.duration
                    if (duration > 30000) {
                        Toast.makeText(
                            context,
                            getString(R.string.please_select_videos_less_than_30_secs),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
//                    showProgress()
                        compressvideo(it)
                    }
                } else {
                    Snackbar.make(
                        binding.headerLayout,
                        getString(R.string.unsupported_file_format),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

            }


        }
    }

    private fun compressvideo(filePath: Uri) {
        VideoCompressor.start(
            context = requireContext(), // => This is required
            uris = listOf(filePath), // => Source can be provided as content uris
            isStreamable = true,
            saveAt = saveAt,

            listener = object : CompressionListener {
                override fun onProgress(index: Int, percent: Float) {
                    // Update UI with progress value
                    ProcessDialog.showDialog(requireActivity(), true)
                }

                override fun onStart(index: Int) {
                    // Compression start
                    Log.d("MYVideo", "Start")
//                    showProgress()
                    ProcessDialog.showDialog(requireActivity(), true)

                }

                override fun onSuccess(index: Int, size: Long, path: String?) {
                    // On Compression success
                    Log.d("MYVideoCompress", "Succeess  $size " + path)
                    val builder = MultipartBody.Builder()
                    builder.setType(MultipartBody.FORM)
                    val file = File(path)
                    builder.addFormDataPart(
                        "video",
                        "${System.currentTimeMillis()}_$index${file.name}",
                        file.asRequestBody(getMimeType(file)?.toMediaTypeOrNull())
                    )
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(path)

                        // Get the first frame (thumbnail) of the video
                        val bitmap = retriever.frameAtTime

                        // Convert the Bitmap to a byte array
                        val byteArray =
                            bitmap?.let { CommonUtils.bitmapToByteArray(it) }

                        // Now you can send the byteArray to your API
                        byteArray?.toRequestBody("multipart/form-data".toMediaTypeOrNull())
                            ?.let {
                                builder.addFormDataPart(
                                    "videoThumbnail",
                                    file.name,
                                    it
                                )
                            }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        retriever.release()
                    }
                    val requestBody = builder.build()
                    if (isAdded){
                        Preferences.getStringPreference(requireActivity(), TOKEN)
                            ?.let {
                                myProfileViewModel.hitUpdateGalleryMediaApi(it, requestBody)
                            }
                    }
                    ProcessDialog.dismissDialog(true)
                }

                override fun onFailure(index: Int, failureMessage: String) {
                    // On Failure
//                    hideProgress()
                    ProcessDialog.dismissDialog(true)
                    Log.d("MYVideo", "Failure")
                }

                override fun onCancelled(index: Int) {
                    // On Cancelled
//                    hideProgress()
                    ProcessDialog.dismissDialog(true)
                    Log.d("MYVideo", "Cancel")
                }

            },
            configureWith = Configuration(
                quality = VideoQuality.VERY_HIGH,
                frameRate = 60, //Int, ignore, or null
                isMinBitrateCheckEnabled = false,
                disableAudio = false, //Boolean, or ignore
                keepOriginalResolution = false, //Boolean, or ignore
            )

        )

    }

    fun getMimeType(file: File): String? =
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)

    override fun showMedia(position: Int) {
        findNavController().navigate(
            EditMyProfileFragmentDirections.editMyProfileToShowImageVideo(
                mediaList = MediaList(userMedia),
                "singleMedia",
                position.toString(),
                "editProfile"
            )
        )
    }

    override fun getSelectedMediaPosition(position: Int) {}

    override fun setLongPress(status: Boolean) {
        isLongPressActive = status
    }
}