package io.euruapp.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import io.codelabs.sdk.glide.GlideApp;
import io.euruapp.R;
import io.euruapp.room.Converter;

import java.util.Date;

/**
 * {@link User} data model
 */
@Entity(tableName = "users")
@TypeConverters(Converter.class)
public class User extends DataModel implements Parcelable {
    public static final String TYPE_INDIVIDUAL = "TYPE_INDIVIDUAL";
    public static final String TYPE_BUSINESS = "TYPE_BUSINESS";
    public static final String TYPE_CUSTOMER = "TYPE_CUSTOMER";

    //Properties
    public String name;
    @PrimaryKey
    @NonNull
    public String key;
    public String type;
    public String profile;
    public String phone;
    public EuruGeoPoint address;
    public long timestamp;

    //Default constructor
    @Ignore
    public User() {
    }

    public User(String name, @NonNull String key, String type, String profile, String phone, EuruGeoPoint address) {
        this.name = name;
        this.key = key;
        this.type = type;
        this.profile = profile;
        this.phone = phone;
        this.address = address;
        this.timestamp = new Date(System.currentTimeMillis()).getTime();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", key='" + key + '\'' +
                ", type='" + type + '\'' +
                ", profile='" + profile + '\'' +
                ", phone='" + phone + '\'' +
                ", address=" + address +
                ", timestamp=" + timestamp +
                '}';
    }

    protected User(Parcel in) {
        name = in.readString();
        key = in.readString();
        type = in.readString();
        profile = in.readString();
        phone = in.readString();
        address = in.readParcelable(EuruGeoPoint.class.getClassLoader());
        timestamp = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(key);
        dest.writeString(type);
        dest.writeString(profile);
        dest.writeString(phone);
        dest.writeParcelable(address, flags);
        dest.writeLong(timestamp);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public EuruGeoPoint getAddress() {
        return address;
    }

    public void setAddress(EuruGeoPoint address) {
        this.address = address;
    }

    @BindingAdapter({"imageUrl"})
    public static void loadAvatar(ImageView imageView, String imageUrl) {
        GlideApp.with(imageView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.avatar_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .circleCrop()
                .into(imageView);
    }

}