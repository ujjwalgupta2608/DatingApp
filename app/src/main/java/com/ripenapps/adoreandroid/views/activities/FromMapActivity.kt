package com.ripenapps.adoreandroid.views.activities

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navArgs
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.utils.enums.SelectedView
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FromMapActivity : AppCompatActivity() {
    private val args: FromMapActivityArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_from_map)
        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.map_navigation_container) as NavHostFragment)
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.map_nav)

        when (args.type) {
           SelectedView.SEARCH_USER ->{
               graph.setStartDestination(R.id.searchUserFragment)
               Log.e(TAG, "initViews: ${Gson().toJson(args)}", )
               navHostFragment.navController.setGraph(graph, args.toBundle())
           }

            SelectedView.USER_DETAIL -> {
                graph.setStartDestination(R.id.userDetailFragment)
                Log.e(TAG, "initViews: ${Gson().toJson(args)}", )
                navHostFragment.navController.setGraph(graph, args.toBundle())
            }
        }
    }
}