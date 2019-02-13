package io.euruapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import androidx.annotation.Nullable;
import com.google.firebase.iid.FirebaseInstanceId;

public class RequestModel/* extends DataModel*/ implements Parcelable {

    //Time request was sent
    public long timestamp;
    //User's current user id
    public String userKey;
    //User's current user id
    @Nullable
    public String image;
    //Reference to current request
    public String dataKey;
    //Location of user making the request
    public EuruGeoPoint location;
    //title of request
    private String title;
    //type of category provided
    private String category;
    //type of category provided
    private String providerId;
    private String deviceToken;

    //Default constructor
    public RequestModel() {
    }

    //Cannot be used to create a new instance of the request model outside of this context
    public RequestModel(String title, String userKey, String dataKey, @Nullable String image, String category, String providerId, EuruGeoPoint location) {
        this.title = title;
        this.timestamp = System.currentTimeMillis();
        this.userKey = userKey;
        this.dataKey = dataKey;
        this.image = image;
        this.category = category;
        this.providerId = providerId;
        this.location = location;
        this.deviceToken = FirebaseInstanceId.getInstance().getToken();
    }

    protected RequestModel(Parcel in) {
        timestamp = in.readLong();
        userKey = in.readString();
        image = in.readString();
        dataKey = in.readString();
        location = in.readParcelable(EuruGeoPoint.class.getClassLoader());
        title = in.readString();
        category = in.readString();
        providerId = in.readString();
        deviceToken = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(timestamp);
        dest.writeString(userKey);
        dest.writeString(image);
        dest.writeString(dataKey);
        dest.writeParcelable(location, flags);
        dest.writeString(title);
        dest.writeString(category);
        dest.writeString(providerId);
        dest.writeString(deviceToken);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RequestModel> CREATOR = new Creator<RequestModel>() {
        @Override
        public RequestModel createFromParcel(Parcel in) {
            return new RequestModel(in);
        }

        @Override
        public RequestModel[] newArray(int size) {
            return new RequestModel[size];
        }
    };

    public long getTimestamp() {
        return timestamp;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    @Nullable
    public String getImage() {
        return image;
    }

    public void setImage(@Nullable String image) {
        this.image = image;
    }

    public String getDataKey() {
        return dataKey;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    public EuruGeoPoint getLocation() {
        return location;
    }

    public void setLocation(EuruGeoPoint location) {
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    @Override
    public String toString() {
        return "RequestModel{" +
                "timestamp=" + timestamp +
                ", userKey='" + userKey + '\'' +
                ", image='" + image + '\'' +
                ", dataKey='" + dataKey + '\'' +
                ", location=" + location +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", providerId='" + providerId + '\'' +
                ", deviceToken='" + deviceToken + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestModel that = (RequestModel) o;
        return timestamp == that.timestamp &&
                Objects.equals(userKey, that.userKey) &&
                Objects.equals(image, that.image) &&
                Objects.equals(dataKey, that.dataKey) &&
                Objects.equals(location, that.location) &&
                Objects.equals(title, that.title) &&
                Objects.equals(category, that.category) &&
                Objects.equals(providerId, that.providerId) &&
                Objects.equals(deviceToken, that.deviceToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, userKey, image, dataKey, location, title, category, providerId, deviceToken);
    }

    //Create a new Request from here
    public static class Builder {
        //title of request
        private String title;
        //User's current user id
        private String userKey;
        //Reference to current request
        private String dataKey;
        //Reference to current request
        private String image;
        //Location of user making the request
        private EuruGeoPoint location;
        //Service requested
        private String category;
        //Service provider's ID
        private String providerId;

        public Builder setDataKey(String dataKey) {
            this.dataKey = dataKey;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setLocation(EuruGeoPoint location) {
            this.location = location;
            return this;
        }

        public Builder setUserKey(String userKey) {
            this.userKey = userKey;
            return this;
        }

        public Builder setImage(String image) {
            this.image = image;
            return this;
        }

        public Builder setCategory(String category) {
            this.category = category;
            return this;
        }

        public Builder setProviderId(String providerId) {
            this.providerId = providerId;
            return this;
        }

        //Build new request
        public RequestModel build() {
            return new RequestModel(title, userKey, dataKey, image, category, providerId, location);
        }

        //Update an existing request
        public RequestModel from(RequestModel model) {
            return new RequestModel(model.title, model.userKey, model.dataKey, model.image, model.category, model.providerId, model.location);
        }
    }

}
