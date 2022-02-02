package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.Constants.REQUEST_LOCATION_PERMISSION
import com.udacity.project4.utils.fadeIn
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment() {
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap

    private var lastKnownLocation: Location? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        prepareMapFragment()

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.mapLocationConfirmButton.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        _viewModel.selectedPOI.postValue(selectedPOI)
        _viewModel.reminderSelectedLocationStr.postValue(selectedPOI?.name)
        _viewModel.latitude.postValue(selectedPOI?.latLng?.latitude)
        _viewModel.longitude.postValue(selectedPOI?.latLng?.longitude)
        findNavController().navigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
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
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private var selectedPOI: PointOfInterest? = null

    private fun prepareMapFragment() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragmentContainer) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            map = googleMap
            setMapStyle()
            map.setOnPoiClickListener {
                addMarker(it)
            }

            enableMapLocation()
        }

        Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            getString(R.string.touch_map_marker_hint),
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun addMarker(pointOfInterest: PointOfInterest) {
        selectedPOI = pointOfInterest
        map.clear()
        map.addMarker(MarkerOptions().position(pointOfInterest.latLng))

        binding.mapLocationConfirmButton.fadeIn()
    }

    private fun setMapStyle() {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e("setMapStyle", "setMapStyle failed")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("setMapStyle", "setMapStyle failed: $e")
        }
    }

    private fun enableMapLocation() {
        if (hasLocationPermission()) {
            updateMapUI()
            getDeviceLocation()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun updateMapUI() {
        try {
            if (hasLocationPermission()) {
                map.isMyLocationEnabled = true
                map.uiSettings?.isMyLocationButtonEnabled = true
                map.uiSettings?.isMapToolbarEnabled = false
            } else {
                map.isMyLocationEnabled = false
                map.uiSettings?.isMyLocationButtonEnabled = false
                map.uiSettings?.isMapToolbarEnabled = false
                lastKnownLocation = null
            }
        } catch (e: SecurityException) {
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMapLocation()
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getDeviceLocation() {
        try {
            if (hasLocationPermission()) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        task.result?.let {
                            lastKnownLocation = it
                            moveMapTo(LatLng(it.latitude, it.longitude))
                        }
                    } else {
                        moveMapTo(LatLng(-29.7201263, -53.7744837))
                        map.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("MapException", "Exception: ${e.message}")
        }
    }

    private fun moveMapTo(latLng: LatLng) {
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                latLng,
                16f
            )
        )
    }
}
