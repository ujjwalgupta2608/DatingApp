package com.ripenapps.adoreandroid.views.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentSearchLocationBinding
import com.ripenapps.adoreandroid.models.request_models.UpdateLocationRequest
import com.ripenapps.adoreandroid.models.static_models.SavedAddressModel
import com.ripenapps.adoreandroid.preferences.CITY
import com.ripenapps.adoreandroid.preferences.COUNTRY
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.SAVED_ADDRESS
import com.ripenapps.adoreandroid.preferences.SAVED_LAT
import com.ripenapps.adoreandroid.preferences.SAVED_LONG
import com.ripenapps.adoreandroid.preferences.TOKEN
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.createInfoDialog
import com.ripenapps.adoreandroid.utils.permissionTitle
import com.ripenapps.adoreandroid.view_models.SearchViewModel
import com.ripenapps.adoreandroid.views.adapters.AdapterLocationSearch
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.ripenapps.adoreandroid.preferences.SELECTED_LANGUAGE_CODE
import com.ripenapps.adoreandroid.utils.AppDialogListener
import com.ripenapps.adoreandroid.utils.createYesNoDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

@AndroidEntryPoint
class SearchLocationFragment : BaseFragment<FragmentSearchLocationBinding>() {
    private var autoCompleteSupportFragment: AutocompleteSupportFragment? = null
    private var permissionId: Int = 2
    lateinit var adapterLocationSearch: AdapterLocationSearch
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var savedAddressList: MutableList<SavedAddressModel> = mutableListOf()
    private var isSetCurrentLocationClicked: Boolean = false
    private val searchViewModel by viewModels<SearchViewModel>()
    private lateinit var locationRequest:UpdateLocationRequest


    override fun setLayout(): Int {
        return R.layout.fragment_search_location
    }

    companion object {
        const val REQUEST_LOCATION_SETTINGS = 123
        const val REQUEST_PERMISSIONS_LOCATION_ONLY_REQUEST_CODE = 111
    }
    override fun initView(savedInstanceState: Bundle?) {
        initAdderssAdapter()
        getSavedAddress()
        autoCompleteSupportFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?
        onClick()
        searchLocation()
        binding.backButton.isVisible = !UserPreference.isNewUserRegistered
        UserPreference.isNewUserRegistered = false
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
                            Preferences.setStringPreference(requireContext(), CITY, locationRequest.city)
                            Preferences.setStringPreference(requireContext(), COUNTRY, locationRequest.country)
                            Preferences.setStringPreference(
                                requireContext(),
                                SAVED_LAT,
                                locationRequest.latitude.toString()
                            )
                            Preferences.setStringPreference(
                                requireContext(),
                                SAVED_LONG,
                                locationRequest.longitude.toString()
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
                            UserPreference.isManualLocSelected=false
                        }

                    }
                }

                com.ripenapps.adoreandroid.utils.Status.LOADING -> {}

                com.ripenapps.adoreandroid.utils.Status.ERROR -> {
                    UserPreference.isManualLocSelected=false
                }

            }
        }
    }

    private fun initAdderssAdapter() {
        adapterLocationSearch = AdapterLocationSearch(::getSelected)
        binding.searchResultRecycler.adapter = adapterLocationSearch
    }

    private fun getSelected(address: SavedAddressModel) {
        locationRequest = UpdateLocationRequest(
            address.latitude?.toDouble(),
            address.longitude?.toDouble(),
            address.locality,
            address.fullAddress,
            address.country
        )
        Preferences.getStringPreference(requireContext(), TOKEN)
            ?.let {
                searchViewModel.hitUpdateLocationApi(
                    it,
                    locationRequest
                )
            }
        UserPreference.isManualLocSelected=true
    }

    private fun getSavedAddress() {
        savedAddressList.clear()
        if (!Preferences.getObjectListPreference(requireContext(), SAVED_ADDRESS).isNullOrEmpty()) {
            savedAddressList = Preferences.getObjectListPreference(
                requireContext(),
                SAVED_ADDRESS
            ) as MutableList<SavedAddressModel>
            Log.i("TAG", "getRecentSearch: " + savedAddressList.size)
            if (savedAddressList != null) {
                if (savedAddressList.size > 0) {
                    adapterLocationSearch.updateList(savedAddressList)
                }
            }
        }

    }

    private fun addAddress(address: SavedAddressModel) {
        if (savedAddressList != null) {
            if (savedAddressList?.size!! == 5) {
                if (!savedAddressList.any { it.fullAddress == address.fullAddress}) {
                    savedAddressList.removeAt(0)
                    savedAddressList.add(address)
                    Preferences.setObjectListPreference(
                        requireContext(),
                        SAVED_ADDRESS,
                        savedAddressList
                    )
                }
            } else {
                if (!savedAddressList.any { it.fullAddress ==address.fullAddress}) {
                    savedAddressList.add(address)
                    Preferences.setObjectListPreference(
                        requireContext(),
                        SAVED_ADDRESS,
                        savedAddressList
                    )
                }
            }
        } else {
            Preferences.setObjectListPreference(
                requireContext(),
                SAVED_ADDRESS,
                mutableListOf(address)
            )
        }
    }

    private fun searchLocation() {
        if (!Places.isInitialized()) {
            val locale = Locale(Preferences.getStringPreference(requireContext(), SELECTED_LANGUAGE_CODE)) // English locale
            Places.initialize(requireContext(), getString(R.string.google_maps_key), locale)
        }
        autoCompleteSupportFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autoCompleteSupportFragment!!.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_search_button)?.visibility =
            View.GONE

        (autoCompleteSupportFragment!!.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_search_input) as EditText).textSize =
            13f
        (autoCompleteSupportFragment!!.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_search_input) as EditText).setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.search_icon,
            0,
            0,
            0
        )
        (autoCompleteSupportFragment!!.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_search_input) as EditText).compoundDrawablePadding =
            20

        (autoCompleteSupportFragment!!.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_search_input) as EditText).typeface =
            Typeface.DEFAULT

        (autoCompleteSupportFragment!!.view?.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_search_input) as EditText).setTextColor(
            ContextCompat.getColor(
                requireContext(), R.color.grey_boulder
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
        autoCompleteSupportFragment!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {

            override fun onPlaceSelected(place: Place) {
                if (isAdded){
                    CoroutineScope(Dispatchers.IO).launch {
                        val geocoder = Geocoder(requireContext())
                        var addressList: List<Address> = emptyList()
                        try {
                            addressList = geocoder.getFromLocationName(place.toString(), 2)!!
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        withContext(Dispatchers.Main) {
                            if (addressList.isNotEmpty()) {
                                val address: Address = addressList[0]
                                autoCompleteSupportFragment!!.setText(address.getAddressLine(0))
                                addAddress(
                                    SavedAddressModel(
                                        address.locality,
                                        address.countryName,
                                        address.latitude.toString(),
                                        address.longitude.toString(),
                                        address.featureName,
                                        address.getAddressLine(0)
                                    )
                                )
                                locationRequest = UpdateLocationRequest(
                                    address.latitude,
                                    address.longitude,
                                    address.locality,
                                    address.getAddressLine(0),
                                    address.countryName
                                )
                                Preferences.getStringPreference(requireContext(), TOKEN)
                                    ?.let {
                                        searchViewModel.hitUpdateLocationApi(
                                            it,
                                           locationRequest
                                        )
                                    }
                                UserPreference.isManualLocSelected=true
                            }
                        }
                    }
                }
            }

            override fun onError(status: Status) {
                Log.e("TAG", "onError: $status")
            }
        })
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
                            var city=""
                            if (list[0].locality.isNullOrEmpty()) {
                                city=list[0].adminArea
                            } else {
                                city=list[0].locality
                            }
                            locationRequest = UpdateLocationRequest(
                                list[0].latitude,
                                list[0].longitude,
                                city,
                                list[0].getAddressLine(0),
                                list[0].countryName
                            )
                            Preferences.getStringPreference(requireContext(), TOKEN)
                                ?.let {
                                    searchViewModel.hitUpdateLocationApi(
                                        it,
                                        locationRequest
                                    )
                                }
                            if (isSetCurrentLocationClicked) {
                                isSetCurrentLocationClicked = false
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
                startActivityForResult(intent, REQUEST_LOCATION_SETTINGS)
            }
        } else {
            requestPermissions()
           /* createInfoDialog(
                requireContext(),
                permissionTitle,
                getString(R.string.you_need_to_unable_the_location_permission_from_settings),
                true
            )*/
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
            Toast.makeText(requireContext(), "User Denied!", Toast.LENGTH_SHORT).show()

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
        if (requestCode == REQUEST_LOCATION_SETTINGS) {
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
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.currentLocation.setOnClickListener {
//            checkLocationPermission()
            isSetCurrentLocationClicked = true
            getLocation()
        }
    }
}