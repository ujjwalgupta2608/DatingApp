package com.ripenapps.adoreandroid.views.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.navigation.findNavController
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.preferences.IS_WELCOME_DONE
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.UserPreference
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseDynamicLinks.getInstance().getDynamicLink(intent).addOnSuccessListener {
            if (it!=null){
                var uri = it.link
                Log.i("TAG", "onCreate: uri link${uri.toString()}")
                Log.i("TAG", "onCreate: userIdParameter ${uri?.getQueryParameter("profileId")}")
                if (!uri?.getQueryParameter("profileId").isNullOrBlank())
                    UserPreference.deepLinkProfileId = uri?.getQueryParameter("profileId").toString()
            }
        }

        /*// Check if this activity was launched via a deep link
        val intent = intent
        val action = intent.action
        val data = intent.data

        if (action != null && action == Intent.ACTION_VIEW && data != null) {
            // Handle the deep link
            val userId = data.getQueryParameter("userId")
            if (userId != null) {
                Log.i("TAG", "onCreate: userId $userId")
                // Use the userId as needed
                // For example, you can pass it to another activity or fragment
                // Or perform actions based on the user ID
            }
        }else if(action != null && action == Intent.ACTION_VIEW ){
            Log.i("TAG", "onCreate: App was opened without data")
            // App was opened without a deep link, proceed as usual
            // ...
        }else{
            Log.i("TAG", "onCreate: App was opened without a deep link")

        }*/
    }

    override fun onBackPressed() {
        when (this.findNavController(R.id.main_navigation_container).currentDestination?.id) {
            R.id.selectLanguageFragment -> {
                finishAffinity()
            }
            R.id.signUpFragment -> {
                if (doubleBackToExitPressedOnce) {
                    finishAffinity()
                }
                this.doubleBackToExitPressedOnce = true
//                Toast.makeText(this, getString(R.string.please_click_back_again_to_exit), Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 1500)
            }
            R.id.loginFragment -> {
                if (Preferences.getStringPreference(this, IS_WELCOME_DONE)=="true")
                    finishAffinity()
                else
                    super.onBackPressed()
            }

            else -> {
                super.onBackPressed()
            }

        }
    }

}