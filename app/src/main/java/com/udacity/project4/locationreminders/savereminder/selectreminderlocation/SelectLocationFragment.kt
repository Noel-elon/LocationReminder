package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.LOCATION_PERMISSION_CODE
import com.udacity.project4.utils.TURN_DEVICE_LOCATION_ON_CODE
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.showToast
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var locationProviderClient: FusedLocationProviderClient
    var prevLocation: Location? = null
    private lateinit var map: GoogleMap
    internal var currentMarker: Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        locationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location

        binding.saveLocationButton.setOnClickListener {
            if (_viewModel.latitude.value != null)
                onLocationSelected()
            else {
                _viewModel.showSnackBar.postValue("Choose a location")
            }
        }

        return binding.root
    }

    private fun onLocationSelected() {
        _viewModel.navigationCommand.value = NavigationCommand.Back
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }

        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(p0: GoogleMap?) {
        p0?.let {
            map = it
        }
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationProviderClient.requestLocationUpdates(
                LocationRequest(), callback, Looper.myLooper()
            )
            map.isMyLocationEnabled = true
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        }
        setMapStyle(map)
        poiClicked(map)
        onMapLongClicked(map)
    }

    private fun poiClicked(map: GoogleMap) {
        map.setOnPoiClickListener {
            _viewModel.saveLocationDetails(
                lat = it.latLng.latitude,
                lon = it.latLng.longitude,
                name = it.name,
                poi = it
            )
            map.clear()
            val markerOptions = MarkerOptions()
            markerOptions.position(it.latLng)
            markerOptions.title(it.name)
            markerOptions.icon(
                BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
            )
            currentMarker = map.addMarker(markerOptions)
        }
    }

    private fun onMapLongClicked(map: GoogleMap) {
        map.setOnMapLongClickListener { latlon ->
            _viewModel.saveLocationDetails(
                lat = latlon.latitude,
                lon = latlon.longitude,
                name = "Selected Position",
                poi = null
            )
            map.clear()
            map.clear()
            val markerOptions = MarkerOptions()
            markerOptions.position(latlon)
            markerOptions.title("Selected Position")
            markerOptions.icon(
                BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
            )
            currentMarker = map.addMarker(markerOptions)
        }
    }


    private val callback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locations = locationResult.locations

            if (locations.isNotEmpty()) {
                val currentLocation = locations.last()
                prevLocation = currentLocation
                currentMarker?.let {
                    it.remove()
                }
                val latAndLong = LatLng(currentLocation.latitude, currentLocation.longitude)
                // TODO: zoom to the user location after taking his permission
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latAndLong, 12f))
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    checkDeviceLocationSettings()
                    locationProviderClient.requestLocationUpdates(
                        LocationRequest(), callback, Looper.myLooper()
                    )
                    map.isMyLocationEnabled = true
                } else {
                    checkDeviceLocationSettings()
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_CODE
                    )
                }
            } else {
                _viewModel.showSnackBar.postValue("Grant location permission please")
            }
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
        } catch (e: Exception) {
            Log.d("Not Found", e.message!!)
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask = settingsClient
            .checkLocationSettings(locationSettingsRequest)

        locationSettingsResponseTask.addOnFailureListener { ex ->
            if (ex is ResolvableApiException && resolve) {
                try {
                    this.startIntentSenderForResult(
                        ex.resolution.intentSender,
                        TURN_DEVICE_LOCATION_ON_CODE, null, 0, 0, 0, null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {

                }
            } else {
                requireContext().showToast("Enable Location Services Please")
                checkDeviceLocationSettings()

            }
        }

        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                locationProviderClient.requestLocationUpdates(
                    LocationRequest(), callback, Looper.myLooper()
                )
                map.isMyLocationEnabled = true
            }
        }
    }


}
