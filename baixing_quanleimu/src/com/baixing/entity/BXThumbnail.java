package com.baixing.entity;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class BXThumbnail implements Parcelable {

	private String localPath;
	private Bitmap thumbnail;
	
	private BXThumbnail() {
		
	}
	
	private BXThumbnail(Parcel in) {
		this.localPath = in.readString();
		this.thumbnail = in.readParcelable(null);
    }
	
	private BXThumbnail(String path, Bitmap thumbnail) {
		this.localPath = path;
		this.thumbnail = thumbnail;
	}
	
	public static BXThumbnail createThumbnail(String path, Bitmap bitmap) {
		return new BXThumbnail(path, bitmap);
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof BXThumbnail) {
			return ((BXThumbnail) obj).getLocalPath().equals(this.getLocalPath());
		}
		
		return false;
	}

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	public Bitmap getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(Bitmap thumbnail) {
		this.thumbnail = thumbnail;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(localPath);
		dest.writeParcelable(thumbnail, flags);
	}
	
	public static final Parcelable.Creator<BXThumbnail> CREATOR = new Parcelable.Creator<BXThumbnail>() {
		public BXThumbnail createFromParcel(Parcel in) {
//			return BXThumbnail.createThumbnail(in.readString(), (Bitmap) in.readParcelable(null));
			return new BXThumbnail(in);
		}

		public BXThumbnail[] newArray(int size) {
			return new BXThumbnail[size];
		}
	};
}
