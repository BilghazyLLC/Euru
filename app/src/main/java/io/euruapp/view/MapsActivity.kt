package io.euruapp.view

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.codelabs.util.bindView
import io.euruapp.R
import io.euruapp.core.BaseActivity
import io.euruapp.model.Business
import io.euruapp.util.ConstantsUtils
import java.util.*

class MapsActivity(override val layoutId: Int = R.layout.activity_maps) : BaseActivity(), OnMapReadyCallback {
    private val requestBtn: Button by bindView(R.id.request_service_button)
    private var mMap: GoogleMap? = null
    private var business: Business? = null

    override fun onViewCreated(intent: Intent, instanceState: Bundle?) {

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?

        if (intent.hasExtra(EXTRA_BUSINESS_MODEL)) {
            val extra = intent.getParcelableExtra<Parcelable>(EXTRA_BUSINESS_MODEL)
            if (extra is Business) {
                business = extra
            }
        } else {
            ConstantsUtils.showToast(this, "Cannot load business model")
            finishAfterTransition()
        }

        mapFragment?.getMapAsync(this)

        requestBtn.setOnClickListener { v -> requestService() }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (business != null) {
            val addresses = business!!.getAddresses()
            if (!addresses.isEmpty()) {
                for (i in addresses.indices) {
                    val euruGeoPoint = addresses[i]
                    val latLng = LatLng(euruGeoPoint.lat, euruGeoPoint.lng)
                    try {
                        mMap!!.addMarker(
                            MarkerOptions().position(latLng)
                                .snippet(business!!.category)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                                .title(String.format(Locale.getDefault(), "%s", business!!.name))
                        ).showInfoWindow()
                    } catch (e: Exception) {
                        if (e is OutOfMemoryError) {
                            ConstantsUtils.logResult(e.message)
                        }

                        //Send error message to server
                        ConstantsUtils.sendErrorMessage(firestore, database, e)
                    }
                }
                ConstantsUtils.logResult(addresses)
                val euruGeoPoint = addresses[0]
                mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(euruGeoPoint.lat, euruGeoPoint.lng), 16f))
                googleMap.setOnInfoWindowClickListener { marker -> requestService() }
                mMap!!.setOnMarkerClickListener { marker ->
                    requestService()
                    true
                }
            }
        }
    }

    private fun requestService() {
        val materialDialog = AlertDialog.Builder(this@MapsActivity)
        materialDialog.setTitle("Request service")
            .setMessage(String.format("Proceed: %s?", business!!.getName()))
            .setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
            .setPositiveButton("Yes") { dialog, which ->
                dialog.dismiss()
                if (isNetworkAvailable) {
                    val b = Bundle(0)
                    b.putParcelable(ProviderDetailsActivity.EXTRA_BUSINESS_MODEL, business)
                    ConstantsUtils.intentTo(this@MapsActivity, ProviderDetailsActivity::class.java, b)
                } else {
                    ConstantsUtils.showToast(applicationContext, "Please check your internet connection")
                }
            }.create().show()

    }

    companion object {

        const val EXTRA_BUSINESS_MODEL = "EXTRA_BUSINESS_MODEL"
    }

}
