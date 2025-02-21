@file:Suppress("DEPRECATION")

package com.bangkit.aksesin.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bangkit.aksesin.R
import com.bangkit.aksesin.databinding.FragmentHomeBinding
import com.bangkit.aksesin.ui.base.BaseFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng


class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate),
    OnMapReadyCallback {

    private lateinit var map: GoogleMap

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var lastKnownLocation: Location? = null

    private var isLocationPermissionGranted = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.maps) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        binding.fabMyLocation.setOnClickListener {
            getDeviceLocation()
        }
    }

    override fun onResume() {
        super.onResume()
        getDeviceLocation()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))

        getLocationPermission()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_ACCESS_FINE_LOCATION -> {
                val isGrantResultProvided = grantResults.isNotEmpty()
                val isGrantResultGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED

                isLocationPermissionGranted = isGrantResultProvided && isGrantResultGranted
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        try {
            if (isLocationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        lastKnownLocation = location
                        lastKnownLocation?.let { currLocation ->
                            moveCamera(currLocation)
                        }
                    } else {
                        lastKnownLocation?.let { currLocation ->
                            moveCamera(currLocation)
                        }
                        requestPermission()
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun moveCamera(location: Location) {
        val currLocation = LatLng(location.latitude, location.longitude)
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                currLocation,
                DEFAULT_ZOOM.toFloat()
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLocationPermission() {
        val permissionGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            isLocationPermissionGranted = true
            map.isMyLocationEnabled = true
        } else {
            requestPermission()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_ACCESS_FINE_LOCATION
        )
    }

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1

        private const val DEFAULT_ZOOM = 15

        private val defaultLocation = LatLng(-6.21462, 106.84513)
    }
}