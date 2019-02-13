package io.euruapp.core.location

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import io.euruapp.core.BaseActivity
import io.euruapp.model.EuruGeoPoint
import io.euruapp.util.ConstantsUtils

class GPSTracker(private val mContext: BaseActivity) : Service(), LocationListener, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {
    // Declaring a Location Manager
    protected var locationManager: LocationManager? = null
    // Flag for GPS status
    internal var isGPSEnabled = false
    // Flag for network status
    internal var isNetworkEnabled = false
    // Flag for GPS status
    internal var canGetLocation = false
    internal var location: Location? = null // Location
    internal var latitude: Double = 0.toDouble() // Latitude
    internal var longitude: Double = 0.toDouble() // Longitude
    private val locationRequest: LocationRequest
    private val apiClient: GoogleApiClient

    init {

        apiClient = GoogleApiClient.Builder(mContext)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()

        locationRequest = LocationRequest()
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.numUpdates = 3000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.smallestDisplacement = 100f

        apiClient.connect()

        getLocation()
    }

    @SuppressLint("MissingPermission")
    fun getLocation(): Location? {
        try {
            locationManager = mContext
                .getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // Getting GPS status
            isGPSEnabled = locationManager!!
                .isProviderEnabled(LocationManager.GPS_PROVIDER)

            // Getting network status
            isNetworkEnabled = locationManager!!
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGPSEnabled && !isNetworkEnabled) {
                // No network provider is enabled
                ConstantsUtils.logResult("No network provider enabled")
            } else {
                this.canGetLocation = true
                if (isNetworkEnabled) {
                    locationManager!!.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                    )
                    ConstantsUtils.logResult("Network")
                    if (locationManager != null) {
                        location = locationManager!!
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (location != null) {
                            latitude = location!!.latitude
                            longitude = location!!.longitude
                        }
                    }
                }

                // If GPS enabled, get latitude/longitude using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager!!.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                        )
                        ConstantsUtils.logResult("GPS Enabled")
                        if (locationManager != null) {
                            location = locationManager!!
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            if (location != null) {
                                latitude = location!!.latitude
                                longitude = location!!.longitude
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ConstantsUtils.logResult(e.localizedMessage)
        }

        return location
    }


    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app.
     */
    fun stopUsingGPS() {
        if (locationManager != null) {
            locationManager!!.removeUpdates(this@GPSTracker)
        }
    }


    /**
     * Function to get latitude
     */
    fun getLatitude(): Double {
        if (location != null) {
            latitude = location!!.latitude
        }

        // return latitude
        return latitude
    }


    /**
     * Function to get longitude
     */
    fun getLongitude(): Double {
        if (location != null) {
            longitude = location!!.longitude
        }

        // return longitude
        return longitude
    }

    /**
     * Function to check GPS/Wi-Fi enabled
     *
     * @return boolean
     */
    fun canGetLocation(): Boolean {
        return this.canGetLocation
    }


    /**
     * Function to show settings alert dialog.
     * On pressing the Settings button it will launch Settings Options.
     */
    fun showSettingsAlert() {
        val alertDialog = AlertDialog.Builder(mContext)

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings")

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?")

        // On pressing the Settings button.
        alertDialog.setPositiveButton("Settings") { dialog, which ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            mContext.startActivity(intent)
        }

        // On pressing the cancel button
        alertDialog.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        // Showing Alert Message
        alertDialog.show()
    }


    override fun onLocationChanged(location: Location?) {
        if (mContext.auth.uid != null && location != null) {
            latitude = location.latitude
            longitude = location.longitude

            //Save locally
            mContext.database.address = EuruGeoPoint(GeoPoint(latitude, longitude))

            //Push to database
            val document = mContext.firestore.collection(ConstantsUtils.COLLECTION_USERS)
                .document(mContext.auth.uid!!)
            document.update("address.lat", location.latitude, "address.lng", location.longitude)
                .addOnCompleteListener { task -> }
                .addOnFailureListener { e -> }
        }
    }


    override fun onProviderDisabled(provider: String) {}


    override fun onProviderEnabled(provider: String) {}


    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onConnected(bundle: Bundle?) {
        ConstantsUtils.logResult("Connected to location services")
    }

    override fun onConnectionSuspended(i: Int) {
        apiClient.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        apiClient.connect()
    }

    companion object {

        // The minimum distance to change Updates in meters
        private val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // 10 meters
        // The minimum time between updates in milliseconds
        private val MIN_TIME_BW_UPDATES = (1000 * 60 * 1).toLong() // 1 minute
    }

}
