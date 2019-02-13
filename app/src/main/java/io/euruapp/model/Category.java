package io.euruapp.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Category implements Parcelable {
	public String name;
	public String image;
	
	public Category(String name, String image) {
		this.name = name;
		this.image = image;
	}
	
	protected Category(Parcel in) {
		name = in.readString();
		image = in.readString();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(image);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Creator<Category> CREATOR = new Creator<Category>() {
		@Override
		public Category createFromParcel(Parcel in) {
			return new Category(in);
		}
		
		@Override
		public Category[] newArray(int size) {
			return new Category[size];
		}
	};
	
	//CATEGORIES
	public static final String ARCHITECT = "Architect";
	public static final String AUTO_SERVICE = "Auto Service";
	public static final String BEAUTY = "Beauty & fashion";
	public static final String CLEANERS = "Cleaners";
	public static final String ELECTRICIAN = "Electrician";
	public static final String HOTELS = "Hotels";
	public static final String EVENT_PLANNING = "Event Planning";
	public static final String PIZZA = "Pizza";
	public static final String PLUMBERS = "Plumbers";
	public static final String PAINTER = "Painter";
	public static final String RESTAURANTS = "Restaurants";
	public static final String SEAMSTRESS = "Seamstress / Tailor";
	public static final String DJ = "DJ";
	public static final String AIR_CONDITION_REPAIRS = "AC Repairs";
	public static final String BAKER = "Baker";
	public static final String BARBER = "Barber";
	public static final String CAR_SPRAYER = "Car Sprayer";
	public static final String CAR_WASH = "Car Wash";
	public static final String CARPENTER = "Carpenter";
	public static final String CATERER = "Caterer";
	public static final String TEACHER = "Teacher";
	public static final String TAXI_DRIVERS = "Taxi Drivers";
	public static final String CAR_RENTALS ="Car Rentals";
	public static final String SHOE_MAKER = "Shoe Maker";

}
