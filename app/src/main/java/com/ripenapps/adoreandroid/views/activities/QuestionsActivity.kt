package com.ripenapps.adoreandroid.views.activities

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.ActivityQuestionsBinding
import com.ripenapps.adoreandroid.databinding.AdaptorAddImageBinding
import com.ripenapps.adoreandroid.databinding.ImagePickerLayoutBinding
import com.ripenapps.adoreandroid.models.OnboardingUiModel
import com.ripenapps.adoreandroid.models.OptionsList
import com.ripenapps.adoreandroid.models.StepSevenEightModel
import com.ripenapps.adoreandroid.models.request_models.user_register.LifeStyleHabbitData
import com.ripenapps.adoreandroid.models.request_models.user_register.WhatMakesYouData
import com.ripenapps.adoreandroid.models.response_models.registerdata.RegisterUserResponse
import com.ripenapps.adoreandroid.models.response_models.getInterestList.InterestListResponse
import com.ripenapps.adoreandroid.models.response_models.getquestions.GenderData
import com.ripenapps.adoreandroid.models.response_models.getquestions.LifestyleData
import com.ripenapps.adoreandroid.models.response_models.getquestions.QuestionsDataResponse
import com.ripenapps.adoreandroid.models.static_models.SelectGenderModel
import com.ripenapps.adoreandroid.preferences.FCM_TOKEN
import com.ripenapps.adoreandroid.preferences.IS_LOGIN
import com.ripenapps.adoreandroid.preferences.NAME
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.TOKEN
import com.ripenapps.adoreandroid.preferences.USER_ID
import com.ripenapps.adoreandroid.preferences.USER_NAME
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.repository.ApiRepository
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.ProcessDialog
import com.ripenapps.adoreandroid.utils.Resources
import com.ripenapps.adoreandroid.utils.SingleLiveEvent
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.utils.enums.AddImagesEnum
import com.ripenapps.adoreandroid.utils.enums.QuestionInterestedInEnum
import com.ripenapps.adoreandroid.utils.enums.QuestionSelectGenderEnum
import com.ripenapps.adoreandroid.utils.enums.QuestionSexualOrientationEnum
import com.ripenapps.adoreandroid.views.adapters.onboardingquestions.AdapterQuestionViewPager
import com.ripenapps.adoreandroid.views.bottomsheets.GenderListBottomsheet
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.ripenapps.adoreandroid.preferences.EMAIL
import com.ripenapps.adoreandroid.preferences.SOCIAL_TYPE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@AndroidEntryPoint
class QuestionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionsBinding
    var viewPagerPosition = 0

    private var mAdapter: AdapterQuestionViewPager? = null
    private var dialog: Dialog? = null
    private val REQUEST_VIDEO = 1
    private val REQUEST_CAMERA = 2
    private val MAX_SELECTIONS = 4

    var userId: String = ""
    var userBio: String = ""

    private val selectedMediaItems: ArrayList<String> = arrayListOf()
    private val questionListLiveData = SingleLiveEvent<Resources<QuestionsDataResponse>>()
    private val interestListLiveData = SingleLiveEvent<Resources<InterestListResponse>>()
    private val hobbyListLiveData = SingleLiveEvent<Resources<InterestListResponse>>()
    private val registerUserLiveData = SingleLiveEvent<Resources<RegisterUserResponse>>()

    private var onboardingUiModel = OnboardingUiModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initOnBoardingUiModel()
        observeRegisterLiveData()
        binding.clickEvents = ::onClick
    }

    private fun hitRegisterUserApi(requestBody: MultipartBody) {
        binding.nextInQuestions.isClickable=false

        try {
            registerUserLiveData.postValue(Resources.loading(null))
            lifecycleScope.launch {
                try {
                    registerUserLiveData.postValue(
                        Resources.success(
                            ApiRepository().registerUserApi(
                                requestBody
                            )
                        )
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun observeRegisterLiveData() {
        registerUserLiveData.observe(this, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it?.data?.status) {
                        "200" -> {
                            Log.i(
                                "TAG",
                                "observeRegisterLiveData: " + Gson().toJson(it.data?.message)
                            )
                            Toast.makeText(
                                this@QuestionsActivity,
                                it.data?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            Preferences.setStringPreference(this, IS_LOGIN, "true")
                            Preferences.setStringPreference(this, TOKEN, it.data.data?.token)
                            Preferences.setStringPreference(this, NAME, UserPreference.fullName)
                            Preferences.setStringPreference(this, USER_NAME, UserPreference.userName)
                            Preferences.setStringPreference(this, EMAIL, UserPreference.emailAddress)
                            Preferences.setStringPreference(this, USER_ID, it.data.data?.responseData?._id)
                            UserPreference.isNewUserRegistered=true
                                val intent = Intent(this@QuestionsActivity, HomeActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()

                        }

                        else -> {
                            binding.nextInQuestions.isClickable=true
                            Toast.makeText(
                                this@QuestionsActivity,
                                it.data?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    ProcessDialog.dismissDialog(true)
                }

                Status.LOADING -> {
                    ProcessDialog.showDialog(this, true)
                }

                Status.ERROR -> {
                    binding.nextInQuestions.isClickable=true
                    Toast.makeText(
                        this@QuestionsActivity,
                        it.data?.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    ProcessDialog.dismissDialog(true)
                }
            }
        })
    }

    private fun hitGetQuestionsDataApi() {
        try {
            questionListLiveData.postValue(Resources.loading(null))
            lifecycleScope.launch {
                try {
                    questionListLiveData.postValue(Resources.success(ApiRepository().questionListApi()))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun initOnBoardingUiModel() {
        hitGetQuestionsDataApi()
        questionListLiveData.observe(this, Observer { questionsList ->
            when (questionsList.status) {
                Status.SUCCESS -> {
                    if (questionsList.data?.status == 200) {
                        onboardingUiModel.stepOne = mutableListOf()
                        onboardingUiModel.stepTwo = mutableListOf()
                        onboardingUiModel.stepSix = mutableListOf()
                        onboardingUiModel.stepSeven = mutableListOf()
                        onboardingUiModel.stepEight = mutableListOf()
                        Log.i("TAG", "initOnBoardingUiModel: " + Gson().toJson(questionsList))
                        onboardingUiModel.stepOne?.addAll(questionsList.data.data.genderData)
                        var list= mutableListOf<SelectGenderModel>()
                        questionsList.data.data.orientationData.forEachIndexed { index, genderData ->
                            list.add(SelectGenderModel(questionsList.data.data.orientationData[index].name!!, false))
                        }
                        onboardingUiModel.stepTwo?.addAll(list)
                        onboardingUiModel.stepSix?.addAll(questionsList.data.data.lookingForData)

                        val modelForStepSeven =
                            createLifeStyleModel(questionsList.data.data.lifestyleData, 7)
                        onboardingUiModel.stepSeven?.addAll(modelForStepSeven)
                        val modelForStepEight =
                            createLifeStyleModel(questionsList.data.data.makesYouData, 8)
                        onboardingUiModel.stepEight?.addAll(modelForStepEight)

                        initViewPager()

                    }
                    ProcessDialog.dismissDialog(true)
                }

                Status.LOADING -> {
                    ProcessDialog.showDialog(this, true)
                }

                Status.ERROR -> {
                    ProcessDialog.dismissDialog(true)
                }
            }
        })
    }

    private fun createLifeStyleModel(
        lifestyleData: List<LifestyleData>,
        stepNumber: Int
    ): MutableList<StepSevenEightModel> {
        val listOfLifeStyle = mutableListOf<StepSevenEightModel>()
        if (stepNumber == 7) {
            for (index in 0 until 4) {
                listOfLifeStyle.add(
                    StepSevenEightModel(
                        title = getLifeStyleTitle(index, stepNumber),
                        isSelected = false,
                        optionsList = when (index) {
                            0 -> {
                                lifestyleData[0].drink?.let { mapOptionList(it) }
                            }

                            1 -> {
                                lifestyleData[0].smoke?.let { mapOptionList(it) }
                            }

                            2 -> {
                                lifestyleData[0].profession?.let { mapOptionList(it) }
                            }

                            else -> {
                                lifestyleData[0].pets?.let { mapOptionList(it) }
                            }
                        }
                    )
                )
            }

        } else {
            Log.i("TAG", "initOnBoardingUiModel8inside: " + lifestyleData.size)
            for (index in 0 until 3) {
                listOfLifeStyle.add(
                    StepSevenEightModel(
                        title = getLifeStyleTitle(index, stepNumber),
                        isSelected = false,
                        optionsList = when (index) {
                            0 -> {
                                lifestyleData[0].communication?.let { mapOptionList(it) }
                            }

                            1 -> {
                                lifestyleData[0].education?.let { mapOptionList(it) }
                            }

                            else -> {
                                lifestyleData[0].zodiac?.let { mapOptionList(it) }
                            }
                        }
                    )
                )
            }
        }

        return listOfLifeStyle
    }

    fun getLifeStyleTitle(index: Int, stepNumber: Int): String {
        if (stepNumber == 7) {
            return when (index) {
                0 -> {
                    getString(R.string.how_often_do_you_drink)
                }

                1 -> {
                    getString(R.string.how_often_do_you_smoke)
                }

                2 -> {
                    getString(R.string.what_is_your_profession)
                }

                3 -> {
                    getString(R.string.do_you_have_any_pets)
                }

                else -> {
                    ""
                }
            }

        } else
            return when (index) {
                0 -> {
                    getString(R.string.what_is_your_communication_style)
                }

                1 -> {
                    getString(R.string.what_is_your_education_level)
                }

                2 -> {
                    getString(R.string.what_is_your_zodiac_sign)
                }

                else -> {
                    ""
                }
            }


    }

    private fun mapOptionList(optionList: List<String>): MutableList<OptionsList> {
        val listOfOption = mutableListOf<OptionsList>()
        optionList.forEach {
            listOfOption.add(
                OptionsList(
                    title = it,
                    isOptionSelected = false
                )
            )
        }
        return listOfOption
    }

    /*private fun initSexualOrientationList(): ArrayList<SelectGenderModel> {
        val sexualOrientationList = arrayListOf<SelectGenderModel>()
        sexualOrientationList.addAll(
            listOf(
                SelectGenderModel("Heterosexual", false),
                SelectGenderModel("Homosexual", false),
                SelectGenderModel("Bisexual", false),
                SelectGenderModel("Pansexual", false),
                SelectGenderModel("Asexual", false),
                SelectGenderModel("Other", false)
            )
        )
        return sexualOrientationList
    }*/

    private fun initViewPager() {
        mAdapter = AdapterQuestionViewPager(
            { questionOneItemClickAction(it) },
            { questionTwoItemClickAction(it) },
            { selectAgeItemClick(it) },
            { questionThreeItemClickAction(it) },
            { selectInterestsItemClick(it) },
            { a, b, c -> addImagesItemClick(a, b, c) },
            { a, b, c -> completeProfileClick(a, b, c) }
        )
        binding.questionViewPager.isUserInputEnabled = false
        binding.questionViewPager.offscreenPageLimit = 2
        binding.questionViewPager.adapter = mAdapter
        binding.questionNumber.text = "1/10"
        mAdapter?.updateModel(onboardingUiModel)
        initInterestModel()
        initHobbyModel()

    }

    private fun initHobbyModel() {
        hitGetHobbyDataApi()
        hobbyListLiveData.observe(this, Observer { hobbyList ->
            when (hobbyList.status) {
                Status.SUCCESS -> {
                    if (hobbyList.data?.status == 200) {
                        mAdapter?.updateHobbyModel(hobbyList.data.data)
                    }
                }

                Status.LOADING -> {

                }

                Status.ERROR -> {

                }
            }
        })
    }

    private fun hitGetHobbyDataApi() {
        try {
            hobbyListLiveData.postValue(Resources.loading(null))
            lifecycleScope.launch {
                try {
                    hobbyListLiveData.postValue(Resources.success(ApiRepository().hobbyListApi()))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun initInterestModel() {
        hitGetInterestDataApi()
        interestListLiveData.observe(this, Observer { interestList ->
            when (interestList.status) {
                Status.SUCCESS -> {
                    if (interestList.data?.status == 200) {
                        mAdapter?.updateInterestsModel(interestList.data.data)
//                        mAdapter?.notifyItemChanged(4)
                    }
                }

                Status.LOADING -> {

                }

                Status.ERROR -> {

                }
            }
        })
    }

    private fun hitGetInterestDataApi() {
        try {
            interestListLiveData.postValue(Resources.loading(null))
            lifecycleScope.launch {
                try {
                    interestListLiveData.postValue(Resources.success(ApiRepository().interestListApi()))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun selectInterestsItemClick(clickedPosition: Int) {
        when (clickedPosition) {

        }
    }

    private fun selectAgeItemClick(selectedAge: String) {
        mAdapter?.selectedAge = selectedAge
    }


    private fun onClick(value: Int) {
        when (value) {
            0 -> {
                if (viewPagerPosition > 0 && viewPagerPosition < 10) {
                    binding.seekBar.progress = (viewPagerPosition) * 10
                    binding.questionNumber.text = "$viewPagerPosition/10"
                    binding.questionViewPager.currentItem = --viewPagerPosition
                } else if (viewPagerPosition == 10) {
                    binding.seekBar.visibility = View.VISIBLE
                    binding.questionNumber.visibility = View.VISIBLE
                    binding.questionViewPager.currentItem = --viewPagerPosition
                    binding.nextInQuestions.text = "Next"
                } else {
                    finish()
                }
//                common in all to manage skip visibility, readjust it later
                if (viewPagerPosition == 1 || viewPagerPosition == 6 || viewPagerPosition == 7) {
                    binding.skipInQuestions.visibility = View.VISIBLE
                } else {
                    binding.skipInQuestions.visibility = View.GONE
                }
            }

            1 -> {
                if (viewPagerPosition == 5) {
                    if (mAdapter?.selectedLookingForPosition == -1) {
                        Snackbar.make(
                            binding.questionViewPager,
                            getString(R.string.select_atleast_one_item_to_proceed),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        binding.questionNumber.text = (viewPagerPosition + 2).toString() + "/10"
                        binding.questionViewPager.currentItem = ++viewPagerPosition
                        binding.seekBar.progress = (viewPagerPosition + 1) * 10
                    }
                } else if (viewPagerPosition == 4) {
                    mAdapter?.interestDataModel?.let { interestDataList ->
                        var isInterestSelected = false

                        for ((index, interestData) in interestDataList.withIndex()) {
                            if (interestData.isSelected) {
                                isInterestSelected = true

                                binding.questionNumber.text =
                                    (viewPagerPosition + 2).toString() + "/10"
                                binding.questionViewPager.currentItem = ++viewPagerPosition
                                binding.seekBar.progress = (viewPagerPosition + 1) * 10

                                // Break out of the loop when isSelected is true
                                break
                            }
                        }

                        if (!isInterestSelected) {
                            Snackbar.make(
                                binding.questionViewPager,
                                getString(R.string.please_select_atleast_one_interest),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else if (viewPagerPosition == 8) {
                    mAdapter?.hobbyDataModel?.let { hobbyDataList ->
                        var isHobbySelected = false
                        for ((index, hobbyData) in hobbyDataList.withIndex()) {
                            if (hobbyData.isSelected) {
                                isHobbySelected = true
                                binding.questionNumber.text =
                                    (viewPagerPosition + 2).toString() + "/10"
                                binding.questionViewPager.currentItem = ++viewPagerPosition
                                binding.seekBar.progress = (viewPagerPosition + 1) * 10
                                break
                            }
                        }
                        if (!isHobbySelected) {
                            Snackbar.make(
                                binding.questionViewPager,
                                getString(R.string.please_select_atleast_one_interest),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else if (viewPagerPosition < 9) {
                    binding.questionNumber.text = (viewPagerPosition + 2).toString() + "/10"
                    binding.questionViewPager.currentItem = ++viewPagerPosition
                    binding.seekBar.progress = (viewPagerPosition + 1) * 10
                } else if (viewPagerPosition == 9) {
                    if (mAdapter?.imageUriList?.size!! < 1) {
                        Snackbar.make(
                            binding.questionViewPager,
                            getString(R.string.please_select_atleast_one_photo),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        if (!mAdapter?.isProfileImageSelected!!)
                            mAdapter?.completeProfileImageUri = mAdapter?.imageUriList!![0]
                        binding.seekBar.visibility = View.GONE
                        binding.questionNumber.visibility = View.GONE
                        binding.questionViewPager.currentItem = ++viewPagerPosition
                        binding.nextInQuestions.text = getString(R.string.complete_profile)
                        mAdapter?.notifyItemChanged(10)


                    }
                } else if (viewPagerPosition == 10) {
                    val validationError = validateCompleteProfile()

                    if (validationError != null) {
                        Toast.makeText(
                            this,
                            validationError,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val builder = MultipartBody.Builder()
                        builder.setType(MultipartBody.FORM)
                        var lifestyle: LifeStyleHabbitData? = LifeStyleHabbitData()
                        var whatMakesYou: WhatMakesYouData? = WhatMakesYouData()

                        val file = File(mAdapter?.completeProfileImageUri)
                        builder.addFormDataPart(
                            "profile",
                            file.name + System.currentTimeMillis(),
                            RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
                        )
                        for ((index, uri) in mAdapter?.imageUriList?.withIndex()!!) {
                            val file: File
                            if (CommonUtils.isImageFile(uri)) {
                                val partName = "images"
                                file = File(uri)
                                builder.addFormDataPart(
                                    partName,
                                    "${file.name}_${System.currentTimeMillis()}_$index",
                                    file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                                )
                            } else {
                                val partName = "video"
                                file = File(uri)
                                builder.addFormDataPart(
                                    partName,
                                    "${file.name}_${System.currentTimeMillis()}_$index",
                                    file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                                )
                                val retriever = MediaMetadataRetriever()
                                try {
                                    retriever.setDataSource(uri)

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

                            }

                        }
                        builder.addFormDataPart("name", UserPreference.fullName)
                        builder.addFormDataPart("email", UserPreference.emailAddress)
                        builder.addFormDataPart("mobile", UserPreference.mobileNumber)
                        builder.addFormDataPart("countryCode", UserPreference.countryCode)
                        if (Preferences.getStringPreference(this@QuestionsActivity, SOCIAL_TYPE)=="google"){
                            builder.addFormDataPart("socialId", UserPreference.socialId)
                            builder.addFormDataPart("socialType", Preferences.getStringPreference(this@QuestionsActivity, SOCIAL_TYPE)!!)
                        }else{
                            builder.addFormDataPart("password", UserPreference.password)
                        }
                        mAdapter?.selectedGenderName?.let { builder.addFormDataPart("gender", it) }
                        builder.addFormDataPart(
                            "genderStatus",
                            mAdapter?.isShowGenderChecked.toString()
                        )
                        if (mAdapter?.sexualOrientationSelectedPostion != -1) {
                            var sexualOrientation =
                                mAdapter?.sexualOrientationSelectedPostion?.let {
                                    mAdapter?.dataModel?.stepTwo?.get(it)?.name?.toLowerCase()
                                }
                            builder.addFormDataPart("sexualOrientation", sexualOrientation!!)
                        }
                        builder.addFormDataPart(
                            "orientationStatus",
                            mAdapter?.isShowOrientationChecked.toString()
                        )
                        if (mAdapter?.isEveryOneSelected == true) {
                            builder.addFormDataPart("interestedIn", "everyOne")
                        } else if (mAdapter?.isInterestedInMaleSelected == true) {
                            builder.addFormDataPart("interestedIn", "male")
                        } else {
                            builder.addFormDataPart("interestedIn", "feMale")
                        }
                        var whatElseYouLike = ArrayList<String>()
                        for (data in mAdapter?.hobbyDataModel!!) {
                            if (data.isSelected) {
                                whatElseYouLike.add(data._id)
                            }
                        }
                        builder.addFormDataPart("whatElseYouLike", Gson().toJson(whatElseYouLike))

                        var topInterests = ArrayList<String>()
                        for (data in mAdapter?.interestDataModel!!) {
                            if (data.isSelected) {
                                topInterests.add(data._id)
                            }
                        }
                        builder.addFormDataPart("topInterests", Gson().toJson(topInterests))

                        for (lookingForData: GenderData in mAdapter?.dataModel?.stepSix!!) {
                            if (lookingForData.isSelected == true) {
                                builder.addFormDataPart("relationshipGoal", lookingForData.name!!)
                            }
                        }
                        builder.addFormDataPart("userName", "@"+userId)
                        UserPreference.userName="@"+userId
                        builder.addFormDataPart("bioDescription", userBio)

//                        Address is saved static for now
                        builder.addFormDataPart("address", "Noida")
                        mAdapter?.selectedAge?.let { builder.addFormDataPart("age", it) }
                        builder.addFormDataPart("deviceToken", Preferences.getStringPreference(this, FCM_TOKEN).toString())
                        builder.addFormDataPart("deviceType", "android")
                        mAdapter?.dataModel?.stepSeven?.forEachIndexed { index, value ->
                            when (index) {
                                0 -> {
                                    if (mAdapter?.dataModel?.stepSeven!![index].selectedOptionPosition!! != -1)
                                        lifestyle?.drink =
                                            mAdapter?.dataModel?.stepSeven!![index].optionsList?.get(
                                                mAdapter?.dataModel?.stepSeven!![index].selectedOptionPosition!!
                                            )!!.title
                                }

                                1 -> {
                                    if (mAdapter?.dataModel?.stepSeven!![index].selectedOptionPosition!! != -1)
                                        lifestyle?.smoke =
                                            mAdapter?.dataModel?.stepSeven!![index].optionsList?.get(
                                                mAdapter?.dataModel?.stepSeven!![index].selectedOptionPosition!!
                                            )!!.title
                                }

                                2 -> {
                                    if (mAdapter?.dataModel?.stepSeven!![index].selectedOptionPosition!! != -1)
                                        lifestyle?.profession =
                                            mAdapter?.dataModel?.stepSeven!![index].optionsList?.get(
                                                mAdapter?.dataModel?.stepSeven!![index].selectedOptionPosition!!
                                            )!!.title
                                }

                                3 -> {
                                    if (mAdapter?.dataModel?.stepSeven!![index].selectedOptionPosition!! != -1) lifestyle?.pet =
                                        mAdapter?.dataModel?.stepSeven!![index].optionsList?.get(
                                            mAdapter?.dataModel?.stepSeven!![index].selectedOptionPosition!!
                                        )!!.title
                                }

                            }
                        }
                        builder.addFormDataPart("lifestyle", Gson().toJson(lifestyle))
                        mAdapter?.dataModel?.stepEight?.forEachIndexed { index, value ->
                            when (index) {
                                0 -> {
                                    if (mAdapter?.dataModel?.stepEight!![index].selectedOptionPosition!! != -1)
                                        whatMakesYou?.communication =
                                            mAdapter?.dataModel?.stepEight!![index].optionsList?.get(
                                                mAdapter?.dataModel?.stepEight!![index].selectedOptionPosition!!
                                            )!!.title
                                }

                                1 -> {
                                    if (mAdapter?.dataModel?.stepEight!![index].selectedOptionPosition!! != -1)
                                        whatMakesYou?.educationLevel =
                                            mAdapter?.dataModel?.stepEight!![index].optionsList?.get(
                                                mAdapter?.dataModel?.stepEight!![index].selectedOptionPosition!!
                                            )!!.title
                                }

                                2 -> {
                                    if (mAdapter?.dataModel?.stepEight!![index].selectedOptionPosition!! != -1)
                                        whatMakesYou?.zodiacSign =
                                            mAdapter?.dataModel?.stepEight!![index].optionsList?.get(
                                                mAdapter?.dataModel?.stepEight!![index].selectedOptionPosition!!
                                            )!!.title
                                }
                            }
                        }
                        builder.addFormDataPart("whatMakesYou", Gson().toJson(whatMakesYou))

                        val requestBody = builder.build()
                        hitRegisterUserApi(requestBody)
                    }

                }


                if (viewPagerPosition == 1 || viewPagerPosition == 6 || viewPagerPosition == 7) {
                    binding.skipInQuestions.visibility = View.VISIBLE
                } else {
                    binding.skipInQuestions.visibility = View.GONE
                }
            }

            2 -> {
                if (viewPagerPosition == 1) {
                    if (mAdapter?.sexualOrientationSelectedPostion != -1) {
                        mAdapter?.dataModel?.stepTwo?.get(mAdapter?.sexualOrientationSelectedPostion!!)?.isSelected =
                            false
                        mAdapter?.sexualOrientationSelectedPostion = -1
//                        mAdapter?.sexualOrientationSelectedForAPI = -1
                    }
                    if (mAdapter?.isShowOrientationChecked == true) {
                        mAdapter?.isShowOrientationChecked = false
                    }
                    binding.questionNumber.text = (viewPagerPosition + 2).toString() + "/10"
                    binding.questionViewPager.currentItem = ++viewPagerPosition
                    binding.seekBar.progress = (viewPagerPosition + 1) * 10
                    mAdapter?.notifyItemChanged(1)


                } else if (viewPagerPosition == 6) {
                    mAdapter?.dataModel?.stepSeven!!.forEachIndexed { index, model ->
                        if (model.selectedOptionPosition != -1) {
                            mAdapter?.dataModel?.stepSeven!![index].optionsList?.get(model.selectedOptionPosition!!)?.isOptionSelected =
                                false
                            mAdapter?.dataModel?.stepSeven!![index].selectedOptionPosition = -1
                        }
                    }
                    binding.questionNumber.text = (viewPagerPosition + 2).toString() + "/10"
                    binding.questionViewPager.currentItem = ++viewPagerPosition
                    binding.seekBar.progress = (viewPagerPosition + 1) * 10
                    mAdapter?.notifyItemChanged(6)
                } else if (viewPagerPosition == 7) {
                    mAdapter?.dataModel?.stepEight!!.forEachIndexed { index, model ->
                        if (model.selectedOptionPosition != -1) {
                            mAdapter?.dataModel?.stepEight!![index].optionsList?.get(model.selectedOptionPosition!!)?.isOptionSelected =
                                false
                            mAdapter?.dataModel?.stepEight!![index].selectedOptionPosition = -1
                        }
                    }
                    binding.questionNumber.text = (viewPagerPosition + 2).toString() + "/10"
                    binding.questionViewPager.currentItem = ++viewPagerPosition
                    binding.seekBar.progress = (viewPagerPosition + 1) * 10
                    mAdapter?.notifyItemChanged(7)
                } else if (viewPagerPosition < 9) {
                    binding.questionNumber.text = (viewPagerPosition + 2).toString() + "/10"
                    binding.questionViewPager.currentItem = ++viewPagerPosition
                    binding.seekBar.progress = (viewPagerPosition + 1) * 10
                } else if (viewPagerPosition == 9) {
                    binding.seekBar.visibility = View.GONE
                    binding.questionNumber.visibility = View.GONE
                    binding.questionViewPager.currentItem = ++viewPagerPosition
                    binding.nextInQuestions.text = getString(R.string.complete_profile)
                }
                if (viewPagerPosition == 1 || viewPagerPosition == 6 || viewPagerPosition == 7) {
                    binding.skipInQuestions.visibility = View.VISIBLE
                } else {
                    binding.skipInQuestions.visibility = View.GONE
                }
            }
        }
    }

    fun validateCompleteProfile(): String? {

        if (userId.isNullOrEmpty()) {
            return getString(R.string.please_enter_user_id)
        }
        if (!isValidUsername(userId)) {
            return getString(R.string.invalid_username_please_enter_a_valid_user_id)
        }
        if (userBio.isNullOrEmpty()) {
            return getString(R.string.please_enter_a_valid_bio)
        }

        return null // No validation error

    }

    fun isValidUsername(input: String): Boolean {
        val regex = Regex("[a-zA-Z0-9]+")
        return regex.matches(input)
    }

    private fun questionOneItemClickAction(clickedPosition: Int) {
        when (clickedPosition) {
            QuestionSelectGenderEnum.Male.value -> {
                if (mAdapter?.selectedGender != QuestionSelectGenderEnum.Male.value) {
                    mAdapter?.selectedGender = QuestionSelectGenderEnum.Male.value
                    mAdapter?.seletedGenderPosition = -1
                    mAdapter?.selectedGenderName = "male"
                    mAdapter?.notifyItemChanged(0)
                }
            }

            QuestionSelectGenderEnum.Female.value -> {
                if (mAdapter?.selectedGender != QuestionSelectGenderEnum.Female.value) {
                    mAdapter?.selectedGender = QuestionSelectGenderEnum.Female.value
                    mAdapter?.seletedGenderPosition = -1
                    mAdapter?.selectedGenderName = "feMale"
                    mAdapter?.notifyItemChanged(0)
                }
            }

            QuestionSelectGenderEnum.Others.value -> {
                Log.i("TAG", "getSelectedGender: " + mAdapter?.seletedGenderPosition)
                val bottomSheetFragment =
                    GenderListBottomsheet(
                        ::getSelectedGender,
                        mAdapter?.seletedGenderPosition!!,
                        mAdapter?.dataModel?.stepOne!!
                    )
                bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)

            }

            QuestionSelectGenderEnum.ShowGender.value -> {
                if (mAdapter?.isShowGenderChecked == true) {
                    mAdapter?.isShowGenderChecked = false
                    mAdapter?.notifyItemChanged(0)
                } else {
                    mAdapter?.isShowGenderChecked = true
                    mAdapter?.notifyItemChanged(0)
                }
            }

            else -> {

            }
        }
    }

    private fun questionTwoItemClickAction(clickedPosition: Int) {
        when (clickedPosition) {
            QuestionSexualOrientationEnum.ShowSexuality.value -> {
                if (mAdapter?.isShowOrientationChecked == true) {
                    mAdapter?.isShowOrientationChecked = false
//                    mAdapter?.dataModel?.stepTwo?.get(clickedPosition)?.isSelected =!()
                    mAdapter?.notifyItemChanged(1)
                } else {
                    mAdapter?.isShowOrientationChecked = true
                    mAdapter?.notifyItemChanged(1)
                }
            }
        }

    }

    private fun questionThreeItemClickAction(clickedPosition: Int) {
        when (clickedPosition) {
            QuestionInterestedInEnum.Male.value -> {
                if (mAdapter?.isInterestedInMaleSelected != true) {
                    mAdapter?.isInterestedInMaleSelected = true
                    mAdapter?.notifyItemChanged(3)
                }
            }

            QuestionInterestedInEnum.Female.value -> {
                if (mAdapter?.isInterestedInMaleSelected == true) {
                    mAdapter?.isInterestedInMaleSelected = false
                    mAdapter?.notifyItemChanged(3)
                }
            }

            QuestionInterestedInEnum.EveryOne.value -> {
                if (mAdapter?.isEveryOneSelected == true) {
                    mAdapter?.isEveryOneSelected = false
                    mAdapter?.notifyItemChanged(3)
                } else {
                    mAdapter?.isEveryOneSelected = true
                    mAdapter?.notifyItemChanged(3)
                }
            }
        }
    }

    private fun addImagesItemClick(
        clickPosition: Int,
        imagePosition: Int,
        binding: AdaptorAddImageBinding
    ) {
        when (clickPosition) {
            AddImagesEnum.Add.value -> {
                if (imagePosition >= mAdapter?.imageUriList!!.size)
                    openImagePicker()
            }

            AddImagesEnum.Cancel.value -> {
                if (mAdapter?.imageUriList?.size!! > imagePosition) {
                    if (mAdapter?.videoPos == -1) {
                        removePathFromList(imagePosition)
                        binding.unselectImage.visibility = View.GONE

                    } else {
                        if (imagePosition == mAdapter?.videoPos) {
                            removePathFromList(imagePosition)
                            binding.unselectImage.visibility = View.GONE
                            mAdapter?.videoPos = -1
                        } else if (imagePosition > mAdapter?.videoPos!!) {
                            removePathFromList(imagePosition)
                            binding.unselectImage.visibility = View.GONE
                        } else {
                            if (/*mAdapter?.imageUriList?.size == 2*/mAdapter?.videoPos==1&&imagePosition==0) {
                                Toast.makeText(
                                    this,
                                    getString(R.string.video_cannot_be_at_first_position),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                removePathFromList(imagePosition)
                                mAdapter?.videoPos = mAdapter?.videoPos!! - 1
                            }
                        }
                    }
                }
            }
        }
    }

    private fun removePathFromList(imagePosition: Int) {
        mAdapter?.imageUriList?.removeAt(imagePosition)
        mAdapter?.imageUriList(mAdapter?.imageUriList!!)
        mAdapter?.notifyItemChanged(9)
    }

    private fun completeProfileClick(clickedPosition: Int, userId: String, userBio: String) {
        if (clickedPosition == 1) {
            this.userId = userId
            this.userBio = userBio
        }

        if (clickedPosition == 0) {
            openImagePicker()
        }
    }

    private fun getSelectedGender(selectedGenderPosition: Int, selectedGenderName: String) {
        mAdapter?.seletedGenderPosition = selectedGenderPosition
        mAdapter?.selectedGenderName = selectedGenderName
        mAdapter?.selectedGender = QuestionSelectGenderEnum.Others.value
        mAdapter?.notifyItemChanged(0)
    }

    private fun openImagePicker() {
        dialog = Dialog(this, R.style.DialogTheme)
        val layoutInflater = LayoutInflater.from(this)
        val binding1: ImagePickerLayoutBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.image_picker_layout, null, false)
        dialog!!.setContentView(binding1.getRoot())
        val window: Window = dialog?.getWindow()!!
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        binding1.camera.setOnClickListener { view ->
            dialog!!.dismiss()
            var permission: List<String> = listOf(Manifest.permission.CAMERA)
            if (CommonUtils.requestPermissions(this, permission))
                openCamera()

        }
        binding1.gallery.setOnClickListener { view ->
            dialog!!.dismiss()
            var permission: List<String> =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    listOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO
                    )
                else
                    listOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
            if (CommonUtils.requestPermissions(this, permission))
                openGallery()
        }
        dialog!!.show()
    }

    private fun openGallery() {
        if (viewPagerPosition == 9 && selectedMediaItems.size < MAX_SELECTIONS) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.setType("*/*")
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf<String>("image/jpeg", "image/png", "video/mp4", "video/quicktime")
            )
            startActivityForResult(intent, REQUEST_VIDEO)
        } else if (viewPagerPosition == 10) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.setType("*/*")
            intent.putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf<String>("image/jpeg", "image/png")
            )
            startActivityForResult(intent, REQUEST_VIDEO)
        }
//            startForMultipleImage.launch(intent)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_VIDEO -> {
                handleGalleryResult(resultCode, data)
            }

            REQUEST_CAMERA -> {
                handleCameraResult(resultCode, data)
            }
        }
    }

    private fun handleGalleryResult(resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && data != null) {
            val clipData = data.clipData
            val selectedMediaUri = data.data
            var imagePath = ""

            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    if (mAdapter?.imageUriList!!.size >= MAX_SELECTIONS) {
                        Snackbar.make(
                            binding.questionViewPager,
                            getString(R.string.maximum_four_images_or_videos_or_images_can_be_uploaded),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        break
                    }
                    val item = clipData.getItemAt(i)
                    val uri = item.uri
                    /*uri != null && !uri.toString().toLowerCase().endsWith(".mp4")*/

                    if (isValidMedia(uri)) {
                        if (uri != null && CommonUtils.isImageUri(
                                contentResolver,
                                uri
                            )
                        ) {
                            imagePath = java.lang.String.valueOf(
                                CommonUtils.createFileSmall(
                                    CommonUtils.getRealPathFromDocumentUri(
                                        this,
                                        uri
                                    ), this
                                )
                            )
                            selectedMediaItems.add(imagePath)
                            mAdapter?.imageUriList(selectedMediaItems)
                            mAdapter?.notifyItemChanged(9)
                        } else {
                            imagePath = CommonUtils.getFilePath(this, uri)!!
                            if (mAdapter?.videoPos == -1) {
                                if (mAdapter?.imageUriList?.size == 0) {
                                    Toast.makeText(
                                        this,
                                        getString(R.string.video_cannot_be_at_first_position),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    addPathInList(imagePath)
                                    mAdapter?.videoPos = mAdapter?.imageUriList?.size!! - 1
                                }
                            } else {
                                Toast.makeText(
                                    this,
                                    getString(R.string.cannot_add_more_than_one_video),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    } else {
                        Snackbar.make(
                            binding.questionViewPager,
                            getString(R.string.please_select_videos_less_than_30_secs),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }

                }

            } else if (selectedMediaUri != null) {
                if (isValidMedia(selectedMediaUri)) {
                    if (selectedMediaUri != null && CommonUtils.isImageUri(
                            contentResolver,
                            selectedMediaUri
                        )
                    ) {
                        imagePath = java.lang.String.valueOf(
                            CommonUtils.createFileSmall(
                                CommonUtils.getRealPathFromDocumentUri(
                                    this,
                                    selectedMediaUri
                                ), this
                            )
                        )
                        if (viewPagerPosition == 9) {
                            addPathInList(imagePath)
                        } else if (viewPagerPosition == 10) {
                            mAdapter?.isProfileImageSelected = true
                            mAdapter?.completeProfileImageUri = imagePath
                            mAdapter?.notifyItemChanged(10)
                        }
                    } else {
                        imagePath = CommonUtils.getFilePath(this, selectedMediaUri)!!
                        if (viewPagerPosition == 9) {
                            if (mAdapter?.videoPos == -1) {
                                if (mAdapter?.imageUriList?.size == 0) {
                                    Toast.makeText(
                                        this,
                                        getString(R.string.video_cannot_be_at_first_position),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    addPathInList(imagePath)
                                    mAdapter?.videoPos = mAdapter?.imageUriList?.size!! - 1
                                }
                            } else {
                                Toast.makeText(
                                    this,
                                    getString(R.string.cannot_add_more_than_one_video),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                } else {
                    // Notify the user that the selected media is not valid
                    Toast.makeText(
                        this,
                        getString(R.string.please_select_videos_less_than_30_secs),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }
    }

    private fun addPathInList(imagePath: String) {
        selectedMediaItems.add(imagePath)
        mAdapter?.imageUriList(selectedMediaItems)
        mAdapter?.notifyItemChanged(9)
    }

    private fun openCamera() {
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(captureIntent, REQUEST_CAMERA)
    }

    private fun handleCameraResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                // Image captured and available in the Intent
                var imagePath = ""
                val imageBitmap = data.extras?.get("data") as Bitmap
                imagePath =
                    CommonUtils.getRealPathFromURI(this, CommonUtils.getImageUri(this, imageBitmap))
                if (viewPagerPosition == 9) {
                    selectedMediaItems.add(imagePath)
                    mAdapter?.imageUriList(selectedMediaItems)
                    mAdapter?.notifyItemChanged(9)
                } else if (viewPagerPosition == 10) {
                    mAdapter?.isProfileImageSelected = true
                    mAdapter?.completeProfileImageUri = imagePath
                    mAdapter?.notifyItemChanged(10)
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Image capture cancelled", Toast.LENGTH_SHORT).show()
            } else {
//                Toast.makeText(this, "Image capture failed", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, getString(R.string.some_error_occured), Toast.LENGTH_SHORT).show()
            }

        }
    }


    private fun isValidMedia(uri: Uri): Boolean {
        // Implement your validation logic here (e.g., check if it's an image or a video less than 30 seconds)
        val mimeType = contentResolver.getType(uri)
        Log.d("TAG", "MIME type: $mimeType")
        val isImage = isImage(uri)
        val isVideo = isVideo(uri)
        val duration = if (isVideo) CommonUtils.getMediaDuration(this, uri) else 0

        return (isImage || (isVideo && duration < 30))
    }

    private fun isImage(uri: Uri): Boolean {
        val mimeType = contentResolver.getType(uri)
        return mimeType?.startsWith("image/") == true
    }

    private fun isVideo(uri: Uri): Boolean {
        val mimeType = contentResolver.getType(uri)
        return mimeType?.startsWith("video/") == true
    }


    override fun onBackPressed() {
    }
}