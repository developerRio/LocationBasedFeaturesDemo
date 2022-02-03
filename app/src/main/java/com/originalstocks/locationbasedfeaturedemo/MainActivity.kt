package com.originalstocks.locationbasedfeaturedemo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.neosoft.locationbasedfeaturedemo.R
import com.neosoft.locationbasedfeaturedemo.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private val REQUEST_CODE = 101
    private lateinit var binding: ActivityMainBinding

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLastLocation: Location? = null
    private var addresses: List<Address> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*init fused location */
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    Log.i(TAG, "onCreate_BottomNavigationView 1 ")
                    binding.selectedTabTextView.text = "Home"
                    true
                }
                R.id.navigation_podcast -> {
                    Log.i(TAG, "onCreate_BottomNavigationView 2 ")
                    binding.selectedTabTextView.text = "Podcast"
                    true
                }
                R.id.navigation_cafe -> {
                    Log.i(TAG, "onCreate_BottomNavigationView 3 ")
                    binding.selectedTabTextView.text = "Cafe"
                    true
                }
                R.id.navigation_menu -> {
                    Log.i(TAG, "onCreate_BottomNavigationView 4 ")
                    binding.selectedTabTextView.text = "Menu"
                    true
                }
                else -> false
            }

        }

    }

    public override fun onStart() {
        super.onStart()
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            getLastLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        mFusedLocationClient!!.lastLocation
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful && task.result != null) {
                    mLastLocation = task.result

                    Log.e(
                        TAG,
                        "getLastLocation: lat = ${(mLastLocation)!!.latitude} lon : ${(mLastLocation)!!.longitude} "
                    )

                    val geocoder = Geocoder(this, Locale.getDefault())

                    addresses = geocoder.getFromLocation(
                        mLastLocation!!.latitude,
                        mLastLocation!!.longitude,
                        1
                    )
                    if (addresses.isNotEmpty()) {
                        optimizingBottomNavItems(addresses)
                    } else {
                        Log.e(TAG, "getLastLocation: Fused location not able to decode")
                    }


                } else {
                    Log.w(TAG, "getLastLocation:exception", task.exception)
                    showToast(this, "No location detected.")
                }
            }
    }

    private fun optimizingBottomNavItems(addresses: List<Address>) {
        val city = addresses[0].locality
        val state = addresses[0].adminArea
        binding.currentLocationTextView.text = "Current Location: $city, $state"

        /* inflating menu initially as to show all items.*/
        binding.bottomNavigationView.menu.clear()
        binding.bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu)

        /* removing item based on location*/
        if (city == "Mumbai") {
            binding.bottomNavigationView.menu.removeItem(R.id.navigation_cafe)
        } else if (city == "Pune") {
            binding.bottomNavigationView.menu.removeItem(R.id.navigation_podcast)
        }
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_CODE
        )
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")

            showSnackBar(binding.root, "Grant Permissions", "Ok") {
                // Request permission
                startLocationPermissionRequest()
            }

        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest()
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isEmpty()) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                getLastLocation()
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackBar(
                    binding.root, "Please grant Permissions", "Settings"
                ) {
                    // Build intent that displays the App settings screen.
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts(
                        "package",
                        BuildConfig.APPLICATION_ID, null
                    )
                    intent.data = uri
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }
        }

    }


}