package io.euruapp.room

import androidx.room.TypeConverter
import com.google.firebase.firestore.GeoPoint
import io.euruapp.model.EuruGeoPoint
import io.euruapp.util.ConstantsUtils

class Converter {

    @TypeConverter
    fun toAddress(address: String?): EuruGeoPoint? {
        return if (address.isNullOrEmpty()) EuruGeoPoint(
            GeoPoint(
                ConstantsUtils.DEFAULT_LAT,
                ConstantsUtils.DEFAULT_LNG
            )
        )
        else EuruGeoPoint(
            GeoPoint(
                address.split(",")[0].toDouble(),
                address.split(",")[1].toDouble()
            )
        )
    }

    @TypeConverter
    fun toString(address: EuruGeoPoint?): String? = address?.toString()

}