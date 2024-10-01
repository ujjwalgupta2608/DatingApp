package com.ripenapps.adoreandroid.views.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentLocationBinding
import com.ripenapps.adoreandroid.models.request_models.UpdateLocationRequest
import com.ripenapps.adoreandroid.preferences.CITY
import com.ripenapps.adoreandroid.preferences.COUNTRY
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.TOKEN
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.createInfoDialog
import com.ripenapps.adoreandroid.utils.permissionTitle
import com.ripenapps.adoreandroid.view_models.SearchViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.ripenapps.adoreandroid.preferences.SAVED_LAT
import com.ripenapps.adoreandroid.preferences.SAVED_LONG
import com.ripenapps.adoreandroid.preferences.USER_ID
import com.ripenapps.adoreandroid.utils.AppDialogListener
import com.ripenapps.adoreandroid.utils.createYesNoDialog
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import java.util.Locale

@AndroidEntryPoint
class LocationFragment : BaseFragment<FragmentLocationBinding>() {
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var permissionId: Int = 2
    private val searchViewModel by viewModels<SearchViewModel>()
    private lateinit var savedAddress:Address
    private var REQUEST_PERMISSIONS_LOCATION_ONLY_REQUEST_CODE = 111



    override fun setLayout(): Int {
        return R.layout.fragment_location
    }

    override fun onResume() {
        super.onResume()
        if (UserPreference.isManualLocSelected){
            UserPreference.isManualLocSelected=false
            findNavController().popBackStack()
        }
    }
    override fun initView(savedInstanceState: Bundle?) {
        onClick()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }
    private fun setObserver() {
        searchViewModel.getUpdateLocationLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                com.ripenapps.adoreandroid.utils.Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            Log.i("TAG", "setObserver: " + it.data)
                            if (savedAddress.locality.isNullOrEmpty()) {
                                Preferences.setStringPreference(
                                    requireContext(),
                                    CITY,
                                    savedAddress.adminArea
                                )
                            } else {
                                Preferences.setStringPreference(
                                    requireContext(),
                                    CITY,
                                    savedAddress.locality
                                )
                            }
                            Preferences.setStringPreference(
                                requireContext(),
                                SAVED_LAT,
                                savedAddress.latitude.toString()
                            )
                            Preferences.setStringPreference(
                                requireContext(),
                                SAVED_LONG,
                                savedAddress.longitude.toString()
                            )
                            Preferences.setStringPreference(
                                requireContext(),
                                COUNTRY,
                                savedAddress.countryName
                            )
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

                com.ripenapps.adoreandroid.utils.Status.LOADING -> {}

                com.ripenapps.adoreandroid.utils.Status.ERROR -> {}

            }
        }
    }
    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (CommonUtils.checkPermissions(requireContext())) {
            if (CommonUtils.isLocationEnabled(requireActivity())) {
                mFusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(requireContext())
                Log.i("TAG", "getLocation: mFusedLocationClient " + mFusedLocationClient)
                mFusedLocationClient?.lastLocation?.addOnCompleteListener(requireActivity()) { task ->
                    val location: Location? = task.result
                    Log.i("TAG", "getLocation: " + task)
                    Log.i("TAG", "getLocation: " + task.result)

                    if (location != null) {
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)!!
                        binding.apply {
                            if (list!=null&&list[0]!=null){
                                savedAddress=list[0]
                                Preferences.getStringPreference(requireContext(), TOKEN)
                                    ?.let {
                                        searchViewModel.hitUpdateLocationApi(
                                            it,
                                            UpdateLocationRequest(
                                                list[0].latitude,
                                                list[0].longitude,
                                                list[0].locality,
                                                list[0].getAddressLine(0),
                                                list[0].countryName
                                            )
                                        )
                                    }
                            }

                        }
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.fetching_current_location) , Toast.LENGTH_SHORT).show()
                        forceFetchLocation()
                    }
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.please_turn_on_location), Toast.LENGTH_LONG)
                    .show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(intent, SearchLocationFragment.REQUEST_LOCATION_SETTINGS)
            }
        } else {
            requestPermissions()
            createYesNoDialog(
                object : AppDialogListener {
                    override fun onPositiveButtonClickListener(dialog: Dialog) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", context?.packageName, null)
                        intent.data = uri
                        context?.startActivity(intent)
                        dialog.dismiss()
                    }

                    override fun onNegativeButtonClickListener(dialog: Dialog) {
                        dialog.dismiss()
                    }
                },
                requireContext(),
                getString(R.string.permission),
                getString(R.string.you_need_to_unable_the_location_permission_from_settings),
                getString(R.string.ok),
                getString(R.string.cancel)
            )
            /*createInfoDialog(
                requireContext(),
                permissionTitle,
                getString(R.string.you_need_to_unable_the_location_permission_from_settings),
                true
            )*/
        }
    }

    private fun forceFetchLocation() {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 2500
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val mostRecentLocation = locationResult.lastLocation
                if (mostRecentLocation != null) {
                    // We found a location, so we can now stop further location updates.
                    if (isAdded) {
                        LocationServices.getFusedLocationProviderClient(requireActivity())
                            .removeLocationUpdates(this)

                        // Now that the device has a location, request it from FusedLocationProvider.
                        requestLocationFromFusedLocationProvider()
                    }

                }
            }
        }
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return
        }
        LocationServices.getFusedLocationProviderClient(requireActivity())
            .requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun requestLocationFromFusedLocationProvider() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (shouldProvideRationale) {
            Toast.makeText(requireContext(), getString(R.string.user_denied), Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_LOCATION_ONLY_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SearchLocationFragment.REQUEST_LOCATION_SETTINGS) {
            Log.i("TAG", "onActivityResult: " + data)
            // User has returned from location settings, check location again
            getLocation()
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }
    private fun onClick() {
        binding.enterLocationManually.setOnClickListener {
            findNavController().navigate(LocationFragmentDirections.locationToSearchLocation())
        }
        binding.allowLocationAccess.setOnClickListener {
            getLocation()
        }
    }
}