package com.example.landmarkremark.map

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import com.example.landmarkremark.base.BaseActivity
import com.example.landmarkremark.remote.entity.MapEntity
import com.example.landmarkremark.utils.Constants
import kotlinx.android.synthetic.main.activity_map.*
import android.widget.SearchView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.support.v4.app.ActivityCompat
import com.google.android.libraries.places.compat.PlaceDetectionClient
import com.google.android.libraries.places.compat.Places
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import android.location.Location
import android.provider.SettingsSlicesContract.KEY_LOCATION
import android.widget.EditText
import com.google.android.gms.maps.model.CameraPosition
import android.content.Intent
import android.view.MenuItem
import com.example.landmarkremark.R
import com.example.landmarkremark.main.MainActivity


class MapActivity : BaseActivity(), MapContract.View, OnMapReadyCallback {

    private lateinit var mMap: GoogleMap


    private lateinit var mUserName: String

    //Global Variable the map data
    private lateinit var mMapData: List<MapEntity>

    // become true when user give the app permission for location
    private var mLocationPermissionGranted: Boolean = false

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private lateinit var mLastKnownLocation: Location

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private val mDefaultLocation = LatLng(-33.8523341, 151.2106085)

    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null

    // Camera map position
    private lateinit var mCameraPosition: CameraPosition

    // This is the Presenter for the MVP architecture
    private val mPresenter: MapContract.Presenter by lazy {
        val presenter = MapPresenter()
        presenter.attachView(this)
        presenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            mCameraPosition = savedInstanceState.getParcelable(Constants.Map.KEY_CAMERA_POSITION)
        }

        // get the user name from the MainActivity
        mUserName = intent.getStringExtra(Constants.IntentExtras.USER_NAME)

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getUserLocation()
        // it will setup a listener to update the app if any chance occurs on the Database
        mPresenter.setRealTimeUpdates()
        setupListeners()

    }

    override fun onStop() {
        super.onStop()
        mPresenter.detachView()
    }

    // Setup the view listeners
    private fun setupListeners() {
        saveLocationButton.setOnClickListener {
            getLocationPermission()
            if (mLocationPermissionGranted) {
                showNotesDialog()
            }
        }
        // Listener for search Edit Text
        searchMapEt.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    mPresenter.processQuery(query, mMapData)
                    searchMapEt.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    mPresenter.getMapData()
                    searchMapEt.clearFocus()
                }
                return false
            }
        })
    }

    // This function will found the device location and zoom the camera on that
    private fun getUserLocation() {
        getLocationPermission()
        try {
            if (mLocationPermissionGranted) {
                val locationResult = mFusedLocationProviderClient?.lastLocation
                locationResult?.addOnCompleteListener(
                    this
                ) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.result!!
                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    mLastKnownLocation.latitude,
                                    mLastKnownLocation.longitude
                                ), Constants.Map.DEFAULT_ZOOM.toFloat()
                            )
                        )
                        mMap.isMyLocationEnabled = true
                    } else {
                        mMap.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(mDefaultLocation, Constants.Map.DEFAULT_ZOOM.toFloat())
                        )
                        mMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            showToast(e.message!!)
        }
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun getLocationPermission() {
        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                Constants.Map.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mPresenter.getMapData()
    }

    // This is a return function for queries
    override fun showMapData(mapData: List<MapEntity>) {
        mMapData = mapData
        mMapData.forEach { mapDetail ->
            val userLatLng = LatLng(mapDetail.latitude, mapDetail.longitude)
            mMap.addMarker(
                MarkerOptions().position(userLatLng).title(mapDetail.userName)
                    .snippet(mapDetail.notes)
            )

        }
        mapProgressBar.visibility = View.GONE

    }

    override fun showMapDataSearch(mapData: List<MapEntity>) {
        mMap.clear()
        mapData.forEach { mapDetail ->
            val userLatLng = LatLng(mapDetail.latitude, mapDetail.longitude)
            mMap.addMarker(
                MarkerOptions().position(userLatLng).title(mapDetail.userName)
                    .snippet(mapDetail.notes)
            )
        }
        mapProgressBar.visibility = View.GONE
    }

    /*
    This is a response for live data and for the first load,
    that means wherever the database is updated, it will call and also update the view
     */
    override fun showRealTimeUpdates(userUpdatedList: ArrayList<MapEntity>) {
        mMap.clear()
        mMapData = userUpdatedList

        userUpdatedList.forEach { mapDetail ->
            val userLatLng = LatLng(mapDetail.latitude, mapDetail.longitude)
            mMap.addMarker(
                MarkerOptions().position(userLatLng).title(mapDetail.userName)
                    .snippet(mapDetail.notes)
            )
        }
    }

    private fun showNotesDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle(getString(R.string.dialog_title))
        val dialogLayout = inflater.inflate(R.layout.dialog_notes, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.notesEt)
        builder.setView(dialogLayout)
        builder.setPositiveButton(android.R.string.ok) { dialogInterface, i ->
            val v = editText.text.toString()
//            if (v.isNullOrBlank()) {
//                showToast(getString(R.string.validation_notes))
//            } else {
            mPresenter.saveMark(
                MapEntity(
                    mUserName, editText.text.toString(),
                    mLastKnownLocation.latitude, mLastKnownLocation.longitude
                )
            )
            mapProgressBar.visibility = View.VISIBLE
//            }

        }
        builder.setNegativeButton(android.R.string.cancel) { dialog, p1 ->
            dialog.cancel()
        }
        builder.show()

    }

    override fun showSuccessDataSaved() {
        showToast(getString(com.example.landmarkremark.R.string.insert_data_successfully_message))
        mPresenter.getMapData()

    }

    override fun showFailureMessage() {
        showToast(getString(com.example.landmarkremark.R.string.insert_data_failed_message))
        mapProgressBar.visibility = View.GONE
    }

    override fun queriedDataNotFound() {
        showToast(getString(com.example.landmarkremark.R.string.no_data_found_message))
        mapProgressBar.visibility = View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val myIntent = Intent(applicationContext, MainActivity::class.java)
        startActivityForResult(myIntent, 0)
        return true
    }

}
