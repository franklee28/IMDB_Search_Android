package com.mengzheli.imdbsearch;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IMDBItemAdapter extends ArrayAdapter<IMDBItem> {
	int resource;
	
	public IMDBItemAdapter(Context context, int textViewResourceId,
			List<IMDBItem> objects) {
		super(context, textViewResourceId, objects);
		resource = textViewResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout imdbView;
		
		IMDBItem item = getItem(position);
		
		Bitmap cover = item.getCover();
		String title = item.getTitle();
		String year = item.getYear();
		String rating = item.getRating();
		
		if (convertView == null) {
			imdbView = new LinearLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
			vi.inflate(resource, imdbView, true);
		}
		else {
			imdbView = (LinearLayout)convertView;
		}
		
		ImageView coverView = (ImageView)imdbView.findViewById(R.id.iv1);
		TextView titleView = (TextView)imdbView.findViewById(R.id.titleTextView);
		TextView ratingView = (TextView)imdbView.findViewById(R.id.ratingTextView);
		
		String titleYear = title + "(" + year +")";
		String ratingSet = "Rating: " + rating;
		
		coverView.setImageBitmap(cover);
		titleView.setText(titleYear);
		ratingView.setText(ratingSet);
		
		return imdbView;
		
	}
}
