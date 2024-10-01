package com.ripenapps.adoreandroid.views.fragments

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentLikesMatchBinding
import com.ripenapps.adoreandroid.models.request_models.LikeDislikeRequest
import com.ripenapps.adoreandroid.preferences.PROFILE
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.TOKEN
import com.ripenapps.adoreandroid.utils.AppDialogListener
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.utils.createYesNoDialog
import com.ripenapps.adoreandroid.view_models.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LikesMatchFragment : BaseFragment<FragmentLikesMatchBinding>() {
    private var name=""
    private var roomId=""
    private var receiverId=""
    private var profileImageUrl=""
    private var distance=""
    val homeViewModel by viewModels<HomeViewModel>()


    override fun setLayout(): Int {
        return R.layout.fragment_likes_match
    }

    override fun initView(savedInstanceState: Bundle?) {
        name=LikesMatchFragmentArgs.fromBundle(requireArguments()).profileName
        profileImageUrl=LikesMatchFragmentArgs.fromBundle(requireArguments()).profileUrl
        roomId=LikesMatchFragmentArgs.fromBundle(requireArguments()).roomId
        receiverId=LikesMatchFragmentArgs.fromBundle(requireArguments()).receiverId
        distance = LikesMatchFragmentArgs.fromBundle(requireArguments()).distance
        setupUi()
        setTitleColors()
        onClick()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }

    private fun setObserver() {
        homeViewModel.getLikeDislikeLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            findNavController().popBackStack()
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

    private fun onClick() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.chatNowButton.setOnClickListener {
            val navOptions: NavOptions = NavOptions.Builder()
                .setPopUpTo(R.id.likesMatchFragment, true)
                .build()
            findNavController().navigate(LikesMatchFragmentDirections.likesMatchToSpecificChat("likeMatch", roomId.trim(), receiverId, name, profileImageUrl), navOptions)
        }
        binding.unlike.setOnClickListener {
            createYesNoDialog(
                object : AppDialogListener {
                    override fun onPositiveButtonClickListener(dialog: Dialog) {
                        Preferences.getStringPreference(requireContext(), TOKEN)
                            ?.let {
                                homeViewModel.hitLikeDislikeUserApi(
                                    it,
                                    LikeDislikeRequest("dislike"),
                                    receiverId
                                )
                            }
                        dialog.dismiss()
                    }

                    override fun onNegativeButtonClickListener(dialog: Dialog) {
                        dialog.dismiss()
                    }
                },
                requireContext(),
                getString(R.string.dislike),
                getString(R.string.are_you_sure_you_want_to_dislike_the_user),
                getString(R.string.yes),
                getString(R.string.no)
            )
        }
    }

    private fun setupUi() {
        var matchText =
            "<font color=#242424>It’s a </font> <font color=#6D53F4>Match</font>"
        binding.textView2.text = Html.fromHtml(matchText, Html.FROM_HTML_MODE_LEGACY)
        binding.unlike.text = "${getString(R.string.unlike)} $name"
        Glide.with(requireContext()).load(Preferences.getStringPreference(requireContext(), PROFILE))
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image).into(binding.leftImage)
        Glide.with(requireContext()).load(profileImageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image).into(binding.rightImage)
        binding.distance.text = formatNumber(distance)+"km"
    }
    private fun setTitleColors() {
        val ss = SpannableString("${getString(R.string.you_and)} $name ${getString(R.string.liked_each_other)}")
        val value = "${getString(R.string.you_and)} $name ${getString(R.string.liked_each_other)}"
        val firstString = name
        val firstIndex = value.indexOf(firstString)
        val firstLastCharIndex = firstIndex + firstString.length
        val clickableSpan1: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {}

            @RequiresApi(Build.VERSION_CODES.Q)
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = resources.getColor(R.color.theme)
                ds.isUnderlineText = false
            }
        }
        ss.setSpan(clickableSpan1, firstIndex, firstLastCharIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.likedEachOtherText.text = ss
        binding.likedEachOtherText.movementMethod = LinkMovementMethod.getInstance()
    }

    fun formatNumber(input: String): String {
        if (input.isEmpty()) return ""
        val parts = input.split(".")
        if (parts.size == 1) {
            return parts[0]
        } else if (parts.size > 2) {
            return input
        }
        val integerPart = parts[0]
        val fractionalPart = parts[1]

        // Take only the first digit of the fractional part
        val fractionalDigit = if (fractionalPart.isNotEmpty()) fractionalPart.substring(0, 1) else ""

        // Combine the integer part and fractional digit with a decimal point
        return if (fractionalDigit.isNotEmpty()) "$integerPart.$fractionalDigit" else integerPart
    }

}