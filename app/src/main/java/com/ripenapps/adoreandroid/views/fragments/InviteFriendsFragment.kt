package com.ripenapps.adoreandroid.views.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.navigation.fragment.findNavController
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentInviteFriendsBinding
import com.ripenapps.adoreandroid.preferences.NAME
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.utils.BaseFragment

class InviteFriendsFragment : BaseFragment<FragmentInviteFriendsBinding>() {
    override fun setLayout(): Int {
        return R.layout.fragment_invite_friends
    }

    override fun initView(savedInstanceState: Bundle?) {
        onClick()
    }
    private fun createFirebaseLink() {
        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(
                Uri.parse("https://adoredatingapp.com").buildUpon()
                .appendQueryParameter("", "")
                .build())
            .setDomainUriPrefix("https://adoredatingapp.page.link")
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder("com.ripenapps.adoreandroid")
                    .setMinimumVersion(0)
                    .build()
            ).setIosParameters(DynamicLink.IosParameters.Builder("com.ripenapps.adoreios").setAppStoreId("6497716227").build())
            .setSocialMetaTagParameters(
                DynamicLink.SocialMetaTagParameters.Builder()
                    .setTitle("Adore")
                    .setDescription("Your friend ${Preferences.getStringPreference(requireContext(), NAME)} sends you an invitation to join Adore, a dating app.")
                    .setImageUrl(Uri.parse("https://ride-chef-dev.s3.ap-south-1.amazonaws.com/datingApp/community/photo/1713438711390.png"))
                    .build()
            )
            .buildShortDynamicLink()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Short link generated
                    val shortLink: Uri = task.result.shortLink!!
                    // Share the short link using the sharing mechanism of your choice
                    if (isAdded){
                        shareDynamicLink(shortLink)
                    }
                } else {
                    // Error handling
                }
            }
    }
    private fun shareDynamicLink(dynamicLink: Uri) {
        Log.i("TAG", "shareDynamicLink: ${dynamicLink.toString()}")
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("text/plain")
        shareIntent.putExtra(Intent.EXTRA_TEXT, dynamicLink.toString())
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_your_friends)))
    }
    private fun onClick() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.shareButton.setOnClickListener {
            createFirebaseLink()
        }
    }
}