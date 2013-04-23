package com.mengzheli.imdbsearch;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class IMDBItem implements Parcelable {
	private Bitmap cover;
	private String coverUrl;
	private String title;
	private String year;
	private String director;
	private String detail;
	private String rating;
	private String error;
	
	public IMDBItem() {
		
	}
	
	public IMDBItem(String _coverUrl, String _title, String _year, String _director, String _detail, String _rating) {
		cover = null;
		coverUrl = _coverUrl;
		title = _title;
		year = _year;
		director = _director;
		detail = _detail;
		rating = _rating;
		error = null;
	}
	
	public IMDBItem(String _coverUrl, String _title, String _year, String _director, String _detail, String _rating, Bitmap _cover) {
		cover = _cover;
		coverUrl = _coverUrl;
		title = _title;
		year = _year;
		director = _director;
		detail = _detail;
		rating = _rating;
		error = null;
	}
	
	public IMDBItem(String _error) {
		cover = null;
		coverUrl = null;
		title = null;
		year = null;
		director = null;
		detail = null;
		rating = null;
		error = _error;
	}
	
	public Bitmap getCover() {
		return cover;
	}
	
	public void setCover(Bitmap _cover) {
		this.cover = _cover;
	}
	
	public String getCoverUrl() {
		return coverUrl;
	}
	
	public void setCoverUrl(String _coverUrl) {
		this.coverUrl = _coverUrl;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String _title) {
		this.title = _title;
	}
	
	public String getYear() {
		return year;
	}
	
	public void setYear(String _year) {
		this.year = _year;
	}
	
	public String getDirector() {
		return director;
	}
	
	public void setDirector(String _director) {
		this.director = _director;
	}
	
	public String getDetail() {
		return detail;
	}
	
	public void setDetail(String _detail) {
		this.detail = _detail;
	}
	
	public String getRating() {
		return rating;
	}
	
	public void setRating(String _rating) {
		this.rating = _rating;
	}
	
	public String getError() {
		return error;
	}
	
	public void setError(String _error) {
		this.error = _error;
	}
	
	public static final Parcelable.Creator<IMDBItem> CREATOR = new Creator<IMDBItem>() {
		@Override
		public IMDBItem createFromParcel(Parcel source) {
			IMDBItem imdbItem = new IMDBItem();
			imdbItem.coverUrl = source.readString();
			imdbItem.detail = source.readString();
			imdbItem.director = source.readString();
			imdbItem.error = source.readString();
			imdbItem.rating = source.readString();
			imdbItem.title = source.readString();
			imdbItem.year = source.readString();
			imdbItem.cover = source.readParcelable(getClass().getClassLoader());
			return imdbItem;
		}
		@Override
		public IMDBItem[] newArray(int size) {
			return new IMDBItem[size];
		}
	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		arg0.writeString(coverUrl);
		arg0.writeString(detail);
		arg0.writeString(director);
		arg0.writeString(error);
		arg0.writeString(rating);
		arg0.writeString(title);
		arg0.writeString(year);
		arg0.writeParcelable(cover, arg1);
	}
}
