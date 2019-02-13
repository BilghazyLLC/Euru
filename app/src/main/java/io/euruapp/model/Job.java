package io.euruapp.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;

public class Job implements Parcelable {

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

    public Job() {
    }

    //Cannot be used to create a new instance of the request model outside of this context
    public Job(String title, String userKey, String dataKey, @Nullable String image, String category, String providerId, EuruGeoPoint location) {
        this.title = title;
        this.timestamp = System.currentTimeMillis();
        this.userKey = userKey;
        this.dataKey = dataKey;
        this.image = image;
        this.category = category;
        this.providerId = providerId;
        this.location = location;
    }

    protected Job(Parcel in) {
        timestamp = in.readLong();
        userKey = in.readString();
        image = in.readString();
        dataKey = in.readString();
        location = in.readParcelable(EuruGeoPoint.class.getClassLoader());
        title = in.readString();
        category = in.readString();
        providerId = in.readString();
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

    @Override
    public String toString() {
        return "Job{" +
                "timestamp=" + timestamp +
                ", userKey='" + userKey + '\'' +
                ", image='" + image + '\'' +
                ", dataKey='" + dataKey + '\'' +
                ", location=" + location +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", providerId='" + providerId + '\'' +
                '}';
    }
}
