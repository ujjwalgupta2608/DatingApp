package com.ripenapps.adoreandroid.views.adapters.onboardingquestions

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdaptorAddImageBinding
import com.ripenapps.adoreandroid.models.OnboardingUiModel
import com.ripenapps.adoreandroid.models.response_models.getInterestList.InterestData
import com.ripenapps.adoreandroid.utils.enums.QuestionSelectGenderEnum
import kotlin.math.absoluteValue

class AdapterQuestionViewPager(
    private val questionOneItemClick: (Int) -> Unit,
    private val questionTwoItemClick: (Int) -> Unit,
    private val selectAgeItemClick: (String) -> Unit,
    private val questionThreeItemClick: (Int) -> Unit,
    private val selectInterestsItemClick: (Int) -> Unit,
    private val addImagesItemClick: (Int, Int, AdaptorAddImageBinding) -> Unit,
    private val completeProfileClick: (Int, String, String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateModel(model: OnboardingUiModel) {
        this.dataModel = model
        notifyDataSetChanged()
    }

    fun updateInterestsModel(model: List<InterestData>) {
        interestDataModel.clear()
        interestDataModel.addAll(model)
        notifyItemChanged(4)

    }

    fun updateHobbyModel(model: List<InterestData>) {
        hobbyDataModel.clear()
        hobbyDataModel.addAll(model)
        notifyItemChanged(8)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun imageUriList(list: ArrayList<String>) {
        imageUriList = list
    }

    var dataModel: OnboardingUiModel = OnboardingUiModel()
    var interestDataModel: MutableList<InterestData> = mutableListOf()
    var hobbyDataModel: MutableList<InterestData> = mutableListOf()

    var selectedGender = QuestionSelectGenderEnum.Male.value
    var seletedGenderPosition = -1
    var selectedGenderName = "male"
    var isShowGenderChecked = false
    var isShowOrientationChecked = false
    var isInterestedInMaleSelected = true
    var isEveryOneSelected = false
    var imageUriList = ArrayList<String>()
    var videoPos = -1
    var sexualOrientationSelectedPostion = -1
    var selectedAge: String = "21"
    var selectedLookingForPosition = -1
    var completeProfileImageUri: String? = ""
    var isProfileImageSelected: Boolean? = false

    private val SELECT_GENDER = 0
    private val SEXUAL_ORIENTATION = 1
    private val SELECT_AGE = 2
    private val INTERESTED_IN = 3
    private val SELECT_INTERESTS = 4
    private val LOOKING_FOR = 5
    private val SELECT_HABITS = 6
    private val OTHER_DETAILS = 7
    private val SELECT_OTHER_INTERESTS = 8
    private val ADD_PHOTOS_VIDEOS = 9
    private val COMPLETE_PROFILE = 10
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            SELECT_GENDER -> {
                SelectGenderVH(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context), R.layout.question_select_gender_layout,
                        parent,
                        false
                    )
                )
            }

            SEXUAL_ORIENTATION -> {
                SexualOrientationVH(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.question_sexual_orientation_layout,
                        parent,
                        false
                    )

                )
            }

            SELECT_AGE -> {
                SelectAgeVH(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.question_select_age_layout,
                        parent,
                        false
                    )

                )
            }

            INTERESTED_IN -> {
                InterestedInVH(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.question_select_gender_layout,
                        parent,
                        false
                    )
                )
            }

            SELECT_INTERESTS -> {
                SelectInterestsVH(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.question_select_interests_layout,
                        parent,
                        false
                    )

                )
            }

            LOOKING_FOR -> {
                LookingForVH(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.question_looking_for_layout,
                        parent,
                        false
                    )
                )
            }

            SELECT_HABITS -> {
                SelectHabitsVH(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.question_habits_layout,
                        parent,
                        false
                    )
                )
            }

            OTHER_DETAILS -> {
                SelectOtherDetailsVH(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.question_habits_layout,
                        parent,
                        false
                    )
                )
            }

            SELECT_OTHER_INTERESTS -> {
                SelectOtherInterestsVH(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.question_select_interests_layout,
                        parent,
                        false
                    )
                )
            }

            ADD_PHOTOS_VIDEOS -> {
                AddPhotosVH(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.question_add_photos_layout,
                        parent,
                        false
                    )
                )
            }

            COMPLETE_PROFILE -> {
                CompleteProfileVH(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.layout_complete_profile,
                        parent,
                        false
                    )
                )
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int {
        return 11
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SelectGenderVH -> {
                holder.bind(
                    selectedGender,
                    isShowGenderChecked,
                    selectedGenderName,
                    position
                ) { newStr -> questionOneItemClick.invoke(newStr) }
            }

            is SexualOrientationVH -> {
                holder.bind(
                    dataModel?.stepTwo!!,
                    isShowOrientationChecked,
                    position,
                    ::sexualOrientationAdapterClick,
                    sexualOrientationSelectedPostion
                ) { newStr ->
                    questionTwoItemClick.invoke(newStr)
                }
            }

            is SelectAgeVH -> {
                holder.bind(position, selectedAge.toInt()) { newStr ->
                    selectAgeItemClick.invoke(
                        newStr
                    )
                }
            }

            is InterestedInVH -> {
                holder.bind(
                    isInterestedInMaleSelected,
                    isEveryOneSelected,
                    position
                ) { newStr ->
                    questionThreeItemClick.invoke(newStr)
                }
            }

            is SelectInterestsVH -> {
                holder.bind(position, interestDataModel, ::selectInterestsAdapterClick) { newStr ->
                    selectInterestsItemClick.invoke(newStr)

                }
            }

            is LookingForVH -> {
                holder.bind(
                    position,
                    dataModel.stepSix!!,
                    ::lookingForAdapterClick,
                    selectedLookingForPosition
                )
            }

            is SelectOtherInterestsVH -> {
                holder.bind(position, hobbyDataModel, ::otherInterestsAdapterClick)
            }

            is SelectHabitsVH -> {
                dataModel?.stepSeven?.let { holder.bind(it, position) }
            }

            is SelectOtherDetailsVH -> {
                dataModel?.stepEight?.let { holder.bind(it, position) }
            }

            is AddPhotosVH -> {
                holder.bind(position, imageUriList) { newStr, pos, binding ->
                    addImagesItemClick.invoke(newStr, pos, binding)

                }
            }

            is CompleteProfileVH -> {
                holder.bind(
                    completeProfileImageUri!!,
                    position
                ) { a, b, c -> completeProfileClick.invoke(a, b, c) }
            }

            else -> {

            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position.absoluteValue) {
            0 -> SELECT_GENDER
            1 -> SEXUAL_ORIENTATION
            2 -> SELECT_AGE
            3 -> INTERESTED_IN
            4 -> SELECT_INTERESTS
            5 -> LOOKING_FOR
            6 -> SELECT_HABITS
            7 -> OTHER_DETAILS
            8 -> SELECT_OTHER_INTERESTS
            9 -> ADD_PHOTOS_VIDEOS
            10 -> COMPLETE_PROFILE
            else -> -1
        }
    }

    private fun sexualOrientationAdapterClick(selectedPosition: Int) {
        sexualOrientationSelectedPostion = selectedPosition
//        sexualOrientationSelectedForAPI = selectedPosition
    }

    private fun lookingForAdapterClick(selectedPosition: Int) {
        selectedLookingForPosition = selectedPosition
    }

    private fun selectInterestsAdapterClick(list: List<InterestData>) {
        interestDataModel = list.toMutableList()
    }

    private fun otherInterestsAdapterClick(list: List<InterestData>) {
        hobbyDataModel = list.toMutableList()
    }


}