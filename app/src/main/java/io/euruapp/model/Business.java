package io.euruapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;
import java.util.Objects;

public class Business extends DataModel implements Parcelable {
    public static final Creator<Business> CREATOR = new Creator<Business>() {
        @Override
        public Business createFromParcel(Parcel in) {
            return new Business(in);
        }

        @Override
        public Business[] newArray(int size) {
            return new Business[size];
        }
    };

    public String userUID;
    public String key;
    public String name;
    public String phone;
    public String category;
    public String desc;
    public String image;
    public List<EuruGeoPoint> addresses;

    public Business() {
    }

    //Each user can own the same business in different locations
    public Business(String userUID, String key, String name, String phone,
                    String category, String desc, String image,
                    List<EuruGeoPoint> addresses) {
        this.userUID = userUID;
        this.key = key;
        this.name = name;
        this.phone = phone;
        this.category = category;
        this.desc = desc;
        this.image = image;
        this.addresses = addresses;
    }

    protected Business(Parcel in) {
        userUID = in.readString();
        key = in.readString();
        name = in.readString();
        phone = in.readString();
        category = in.readString();
        desc = in.readString();
        image = in.readString();
        addresses = in.createTypedArrayList(EuruGeoPoint.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userUID);
        dest.writeString(key);
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(category);
        dest.writeString(desc);
        dest.writeString(image);
        dest.writeTypedList(addresses);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public List<EuruGeoPoint> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<EuruGeoPoint> addresses) {
        this.addresses = addresses;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Business{" +
                "userUID='" + userUID + '\'' +
                ", key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", category='" + category + '\'' +
                ", desc='" + desc + '\'' +
                ", image='" + image + '\'' +
                ", addresses=" + addresses +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Business business = (Business) o;
        return Objects.equals(userUID, business.userUID) &&
                Objects.equals(key, business.key) &&
                Objects.equals(name, business.name) &&
                Objects.equals(phone, business.phone) &&
                Objects.equals(category, business.category) &&
                Objects.equals(desc, business.desc) &&
                Objects.equals(image, business.image) &&
                Objects.equals(addresses, business.addresses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userUID, key, name, phone, category, desc, image, addresses);
    }
}
