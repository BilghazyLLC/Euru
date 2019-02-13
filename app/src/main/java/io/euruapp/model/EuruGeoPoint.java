package io.euruapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;

/**
 * {@link GeoPoint} implementation that allows the Location coordinates to be parcelable
 */
public class EuruGeoPoint implements Parcelable {
	
	public Double lat;
	public Double lng;
	
	public EuruGeoPoint() {
	}
	
	public EuruGeoPoint(GeoPoint geoPoint) {
		this.lat = geoPoint.getLatitude();
		this.lng = geoPoint.getLongitude();
	}
	
	
	protected EuruGeoPoint(Parcel in) {
		if (in.readByte() == 0) {
			lat = null;
		} else {
			lat = in.readDouble();
		}
		if (in.readByte() == 0) {
			lng = null;
		} else {
			lng = in.readDouble();
		}
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if (lat == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeDouble(lat);
		}
		if (lng == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeDouble(lng);
		}
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Creator<EuruGeoPoint> CREATOR = new Creator<EuruGeoPoint>() {
		@Override
		public EuruGeoPoint createFromParcel(Parcel in) {
			return new EuruGeoPoint(in);
		}
		
		@Override
		public EuruGeoPoint[] newArray(int size) {
			return new EuruGeoPoint[size];
		}
	};
	
	@Override
	public String toString() {
		return "EuruGeoPoint{" +
				       "lat=" + lat +
				       ", lng=" + lng +
				       '}';
	}
}
