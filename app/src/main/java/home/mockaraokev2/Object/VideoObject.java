package home.mockaraokev2.Object;

import android.os.Parcel;
import android.os.Parcelable;

/**
 Created by admin on 5/24/2017.
 */

public class VideoObject implements Parcelable {
    private String name;
    private String img;
    private String id;

    public VideoObject() {
    }

    public VideoObject(String id, String name, String img) {
        this.name = name;
        this.img = img;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    @Override
    public String toString() {
        return id + "   " + name + "   " + img;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(id);
        out.writeString(name);
        out.writeString(img);
    }

    public static final Creator<VideoObject> CREATOR
            = new Creator<VideoObject>() {
        public VideoObject createFromParcel(Parcel in) {
            return new VideoObject(in);
        }

        public VideoObject[] newArray(int size) {
            return new VideoObject[size];
        }
    };

    private VideoObject(Parcel in) {
        id = in.readString();
        name = in.readString();
        img = in.readString();
    }
}
