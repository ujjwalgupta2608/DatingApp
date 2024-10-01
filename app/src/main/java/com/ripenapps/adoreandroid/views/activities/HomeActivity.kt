package com.ripenapps.adoreandroid.views.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.gson.Gson
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.ActivityHomeBinding
import com.ripenapps.adoreandroid.models.static_models.NotificationMessageData
import com.ripenapps.adoreandroid.preferences.UserPreference
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private var doubleBackToExitPressedOnce = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navController = this.findNavController(R.id.home_navigation)
        setUpBottomNavMenu(navController)
        binding.navigationBar.itemIconTintList = null

        val notificationData = intent?.getStringExtra("notificationData")

        if (notificationData != null) {
            UserPreference.NotificationData = Gson().fromJson(notificationData, NotificationMessageData::class.java)
            Log.i("TAG", "onNewIntent: "+ "received oncreate $notificationData")
        } else {
            UserPreference.NotificationData = NotificationMessageData()
            Log.i("TAG", "onNewIntent: "+ "not received oncreate  $notificationData")
        }
    }

    private fun setUpBottomNavMenu(navController: NavController) {
        val bottomNav = binding.navigationBar
        bottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.homeFragment -> binding.navigationBar.isVisible = true
                R.id.searchFragment -> binding.navigationBar.isVisible = true
                R.id.likeFragment -> binding.navigationBar.isVisible = true
                R.id.chatFragment -> binding.navigationBar.isVisible = true
                R.id.profileFragment -> binding.navigationBar.isVisible = true
                R.id.storyViewFragment -> binding.navigationBar.isVisible = false
                R.id.userDetailFragment -> binding.navigationBar.isVisible = false
                R.id.showImageVideoFragment->binding.navigationBar.isVisible = false
                R.id.editMyProfileFragment->binding.navigationBar.isVisible = false
                R.id.filterFragment->binding.navigationBar.isVisible = false
                R.id.settingsFragment->binding.navigationBar.isVisible = false
                R.id.changePasswordFragment->binding.navigationBar.isVisible = false
                R.id.webViewFragment->binding.navigationBar.isVisible = false
                R.id.FAQFragment->binding.navigationBar.isVisible = false
                R.id.notificationFragment->binding.navigationBar.isVisible = false
                R.id.inviteFriendsFragment->binding.navigationBar.isVisible = false
                R.id.locationFragment->binding.navigationBar.isVisible = false
                R.id.searchLocationFragment->binding.navigationBar.isVisible = false
                R.id.choosePlanFragment->binding.navigationBar.isVisible = false
                R.id.likesMatchFragment->binding.navigationBar.isVisible = false
                R.id.specificChatFragment->binding.navigationBar.isVisible = false
                R.id.searchUserFragment->binding.navigationBar.isVisible = false
                R.id.callHistoryFragment->binding.navigationBar.isVisible = false
                R.id.paymentFragment->binding.navigationBar.isVisible=false
                R.id.paymentCompletedFragment->binding.navigationBar.isVisible=false
                R.id.reviewSummaryFragment->binding.navigationBar.isVisible=false
            }
        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        val notificationData = intent?.getStringExtra("notificationData")
        if (notificationData != null) {
            UserPreference.NotificationData = Gson().fromJson(notificationData, NotificationMessageData::class.java)
            Log.i("TAG", "onNewIntent: "+ "received oncreate $notificationData")
        } else {
            UserPreference.NotificationData = NotificationMessageData()
            Log.i("TAG", "onNewIntent: "+ "not received oncreate  $notificationData")
        }
    }
    override fun onBackPressed() {
        when (this.findNavController(R.id.home_navigation).currentDestination?.id) {
            R.id.homeFragment -> {
                if (doubleBackToExitPressedOnce) {
                    finishAffinity()
                }
                this.doubleBackToExitPressedOnce = true
                Toast.makeText(this, getString(R.string.please_click_back_again_to_exit), Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 1500)
            }
            R.id.locationFragment -> {
                if (doubleBackToExitPressedOnce) {
                    finishAffinity()
                }
                this.doubleBackToExitPressedOnce = true
                Toast.makeText(this, getString(R.string.please_click_back_again_to_exit), Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 1500)
            }
            R.id.searchLocationFragment -> {}

            R.id.paymentCompletedFragment->{}

            else -> {
                super.onBackPressed()
            }

        }
    }
}