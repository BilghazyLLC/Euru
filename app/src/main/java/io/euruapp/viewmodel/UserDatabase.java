package io.euruapp.viewmodel;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.google.firebase.firestore.GeoPoint;
import io.euruapp.model.EuruGeoPoint;
import io.euruapp.model.User;
import io.euruapp.util.ConstantsUtils;

import javax.inject.Inject;

/**
 * {@linkplain User} database class
 */
public class UserDatabase {

    //Key values for the shared preferences
    private static final String KEY_USER_PROFILE = "KEY_USER_PROFILE";
    private static final String KEY_NAME = "KEY_NAME";
    private static final String KEY_PHONE = "KEY_PHONE";
    private static final String KEY_USER_KEY = "KEY_USER_KEY";
    private static final String KEY_TYPE = "KEY_TYPE";
    private static final String KEY_LNG = "KEY_LNG";
    private static final String KEY_LAT = "KEY_LAT";
    private static final String KEY_PENDING = "KEY_PENDING";
    private final SharedPreferences prefs;
    //Login state
    private boolean isLoggedIn = false;
    private boolean isPending = false;

    //Properties
    private String name;
    private String key;
    private String profile;
    private String type;
    private String phone;

    private Float lat = 0.0f;
    private Float lng = 0.0f;
    private EuruGeoPoint address;

    @Inject
    public UserDatabase(Context context) {
        this.prefs = context.getSharedPreferences(ConstantsUtils.DATABASE_NAME, Context.MODE_PRIVATE);

        //User props
        name = prefs.getString(KEY_NAME, null);
        key = prefs.getString(KEY_USER_KEY, null);
        profile = prefs.getString(KEY_USER_PROFILE, null);
        phone = prefs.getString(KEY_PHONE, null);
        type = prefs.getString(KEY_TYPE, null);

        isPending = prefs.getBoolean(KEY_PENDING, false);

        //Can store lat and lng but not EuruGeoPoint
        lat = prefs.getFloat(KEY_LAT, 0.0f);
        lng = prefs.getFloat(KEY_LNG, 0.0f);

        //Create new instance of the GeoPoint
        address = new EuruGeoPoint(new GeoPoint(lat, lng));

        //Check login state by verifying the user key property
        isLoggedIn = key != null && !TextUtils.isEmpty(key);

        if (isLoggedIn) {
            //User props
            name = prefs.getString(KEY_NAME, null);
            key = prefs.getString(KEY_USER_KEY, null);
            profile = prefs.getString(KEY_USER_PROFILE, null);
            phone = prefs.getString(KEY_PHONE, null);
            type = prefs.getString(KEY_TYPE, null);

            //Can store lat and lng but not EuruGeoPoint
            lat = prefs.getFloat(KEY_LAT, 0.0f);
            lng = prefs.getFloat(KEY_LNG, 0.0f);

            //Create new instance of the GeoPoint
            address = new EuruGeoPoint(new GeoPoint(lat, lng));

            isPending = prefs.getBoolean(KEY_PENDING, false);
        }

    }

    public void logout() {
        isLoggedIn = false;

        //User props
        this.name = null;
        this.key = null;
        this.profile = null;
        this.phone = null;
        this.type = null;
        this.address = null;
        this.lat = 0.0f;
        this.lng = 0.0f;

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_USER_KEY, key);
        editor.putString(KEY_TYPE, type);
        editor.putString(KEY_USER_PROFILE, profile);
        editor.putString(KEY_PHONE, phone);
        editor.putFloat(KEY_LAT, lat);
        editor.putFloat(KEY_LNG, lng);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public EuruGeoPoint getAddress() {
        return new EuruGeoPoint(new GeoPoint(lat, lng));
    }

    public void setAddress(EuruGeoPoint address) {
        this.address = address;
        this.lat = address.lat.floatValue();
        this.lng = address.lng.floatValue();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(KEY_LAT, lat);
        editor.putFloat(KEY_LNG, lng);
        editor.apply();
    }

    public User getUser() {
        return new User(name, key, type, profile, phone, getAddress());
    }

    public void setUser(User user) {
        isLoggedIn = user != null;
        ConstantsUtils.logResult("Local storage contains: " + String.valueOf(user));

        if (isLoggedIn) {

            //User props
            assert user != null;
            this.name = user.name;
            this.key = user.key;
            this.profile = user.profile;
            this.phone = user.phone;
            this.type = user.type;
            this.address = user.address;

            //Address of User
            if (address != null) {
                lat = address.lat.floatValue();
                lng = address.lng.floatValue();
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_NAME, name);
            editor.putString(KEY_USER_KEY, key);
            editor.putString(KEY_TYPE, type);
            editor.putString(KEY_USER_PROFILE, profile);
            editor.putString(KEY_PHONE, phone);
            editor.putFloat(KEY_LAT, lat);
            editor.putFloat(KEY_LNG, lng);
            editor.apply();
        }
    }

    public void updateName(String username) {
        this.name = username;
        prefs.edit().putString(KEY_NAME, username).apply();
    }

    public void updateProfile(String profile) {
        this.profile = profile;
        prefs.edit().putString(KEY_USER_PROFILE, profile).apply();
    }

    public boolean isPending() {
        return prefs.getBoolean(KEY_PENDING, false);
    }

    public void setPending(boolean pending) {
        isPending = pending;
        prefs.edit().putBoolean(KEY_PENDING, pending).apply();
    }
}
