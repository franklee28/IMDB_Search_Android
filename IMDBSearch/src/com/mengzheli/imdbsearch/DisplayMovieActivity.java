package com.mengzheli.imdbsearch;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;

public class DisplayMovieActivity extends Activity {

	private ListView imdbList = null;
	private ArrayList<IMDBItem> imdbArray = null;
	private IMDBItemAdapter imdbAdapter;
	private IMDBItem selectedItem;
	private final static int MOVIE_DIALOG = 1;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	private boolean flag = false;
	private boolean isPost = false;
	private boolean postT = false;
	private int menuChoose = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_movie);
		setTitle("Result Movies");
		
		Intent intent = getIntent();
		Session session = Session.getActiveSession();
		if (session == null) {
		    if (savedInstanceState != null) {
		        session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
		    }
		    if (session == null) { 
		        session = new Session(this);
		    }
		    Session.setActiveSession(session);
		    if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
		        session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
		    }
		}
		
		TextView et = (TextView)findViewById(R.id.resultText);
		imdbArray = intent.getParcelableArrayListExtra("resultArray");
		if (imdbArray != null) {
			if (imdbArray.get(0).getError() == null) {
				et.setVisibility(View.GONE);
				
				imdbAdapter = new IMDBItemAdapter(this, R.layout.imdb_list_item_view, imdbArray);
				
				imdbList = (ListView)findViewById(R.id.lv1);
				
				imdbList.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView _av, View _v, int _index, long arg3) {
						selectedItem = imdbArray.get(_index);
						showDialog(MOVIE_DIALOG);
					}
				});
				
				imdbList.setAdapter(imdbAdapter);
			}
			else {
				et.setVisibility(View.VISIBLE);
				et.setText(imdbArray.get(0).getError());
			}
		}
		else {
			et.setVisibility(View.VISIBLE);
			et.setText(R.string.no_result);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.activity_display_movie, menu);
		//super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, Menu.FIRST, Menu.FIRST, "Log Out").setIcon(android.R.drawable.ic_menu_delete);
		menuChoose = 2;
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.clear();
		Session session = Session.getActiveSession();
		if (session.isOpened()) {
			menu.add(Menu.NONE, Menu.FIRST, Menu.FIRST, "Log Out").setIcon(android.R.drawable.ic_menu_delete);
			menuChoose = 2;
		}
		else {
			menu.add(Menu.NONE, Menu.FIRST, Menu.FIRST, "Log In").setIcon(android.R.drawable.ic_menu_add);
			menuChoose = 1;
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST:
			if (menuChoose == 1) {
				facebookLogin();
			}
			else {
				facebookLogout();
			}
			return true;
		}
		
		return false;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);

	    Session session = Session.getActiveSession();
	    Session.saveSession(session, outState);
	}
	
	@Override
	public Dialog onCreateDialog(int id) {
		switch (id) {
		case (MOVIE_DIALOG):
			LayoutInflater li = LayoutInflater.from(this);
			View movieDetailsView = li.inflate(R.layout.movie_detail, null);
			
			AlertDialog.Builder movieDialog = new AlertDialog.Builder(this);
			movieDialog.setView(movieDetailsView);
			movieDialog.setTitle("Details");
			return movieDialog.create();
		}
		return null;
	}
	
	@Override
	public void onStart() {
	    super.onStart();

	    Session.getActiveSession().addCallback(statusCallback);
	}

	@Override
	public void onStop() {
	    super.onStop();

	    Session.getActiveSession().removeCallback(statusCallback);
	}
	
	@Override
	public void onPrepareDialog(int id, Dialog dialog) {
		switch(id) {
		case (MOVIE_DIALOG):
			Bitmap cover = selectedItem.getCover();
			String title = "Name: " + selectedItem.getTitle();
			String year = "Year: " + selectedItem.getYear();
			String rating = "Rating: " + selectedItem.getRating();
			String director = "Director: " + selectedItem.getDirector();
			String hint = "Would you like to post the movie information on Facebook?";
			AlertDialog movieDialog = (AlertDialog)dialog;
			movieDialog.setTitle("Details");
			ImageView iv = (ImageView)movieDialog.findViewById(R.id.iv2);
			iv.setImageBitmap(cover);
			TextView ttv = (TextView)movieDialog.findViewById(R.id.titleText);
			ttv.setText(title);
			ttv.setTextColor(Color.WHITE);
			TextView ytv = (TextView)movieDialog.findViewById(R.id.yearText);
			ytv.setText(year);
			ytv.setTextColor(Color.WHITE);
			TextView dtv = (TextView)movieDialog.findViewById(R.id.directorText);
			dtv.setText(director);
			dtv.setTextColor(Color.WHITE);
			TextView rtv = (TextView)movieDialog.findViewById(R.id.ratingText);
			rtv.setText(rating);
			rtv.setTextColor(Color.WHITE);
			TextView htv = (TextView)movieDialog.findViewById(R.id.hintText);
			htv.setText(hint);
			htv.setTextColor(Color.WHITE);
			break;
		}
	}
	
	private void onSessionStateChange(SessionState state, Exception exception) {
	    Session session = Session.getActiveSession();
	    
	    if (state.isOpened()) {
	    	if ((flag == true) && (isPost == true)) {
	    		publishFeedDialog();
	    	}
	    	flag = true;
	    	System.out.println("isOpened");
	    	System.out.println(flag);
		    System.out.println(isPost);
		    System.out.println(postT);
	    	System.out.println("logged in");
	    }
	    else {
	    	flag = false;
	    	if (isPost == true) {
	    		postT = true;
	    	}
	    	else {
	    		postT = false;
	    	}
	    	isPost = false;
	    	System.out.println("isClosed");
	    	System.out.println(flag);
		    System.out.println(isPost);
		    System.out.println(postT);
	    	System.out.println("logged out");
	    }
	}

	private class SessionStatusCallback implements Session.StatusCallback {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	        onSessionStateChange(state, exception);
	    }
	}
	
	public void postToFacebook(View view) {
		System.out.println(selectedItem.getTitle() + "\nPost to Facebook");
		Session session = Session.getActiveSession();
		isPost = true;
		if (session.isOpened()) {
			publishFeedDialog();
		}
		else {
			facebookLogin();
		}
	}

	private void facebookLogin() {
	    Session session = Session.getActiveSession();
	    if (!session.isOpened() && !session.isClosed()) {
	        session.openForRead(
	                new Session.OpenRequest(this)
	                .setCallback(statusCallback)
	                .setPermissions(Arrays.asList("user_likes", "user_status"))
	        );
	    }
	    else {
	        Session.openActiveSession(this, true, statusCallback);
	    }
	}

	private void facebookLogout() {
	    Session session = Session.getActiveSession();
	    session.closeAndClearTokenInformation();
	}
	
	private void publishFeedDialog() {
		String description = selectedItem.getTitle() + " released in " + selectedItem.getYear() + " has a rating of " + selectedItem.getRating();
		String reviews = selectedItem.getDetail() + "reviews";
		String properties = "{\"Look at user reviews\": {\"text\": \"here\", \"href\": \"" + reviews + "\"}}";
		
	    Bundle params = new Bundle();
	    params.putString("picture", selectedItem.getCoverUrl());
	    params.putString("name", selectedItem.getTitle());
	    params.putString("caption", "I am interested in this movie/series/game");
	    params.putString("description", description);
	    params.putString("link", selectedItem.getDetail());
	    params.putString("properties", properties);

	    WebDialog feedDialog = (
	        new WebDialog.FeedDialogBuilder(DisplayMovieActivity.this,
	            Session.getActiveSession(),
	            params))
	        .setOnCompleteListener(new OnCompleteListener() {
	            @Override
	            public void onComplete(Bundle values,
	                FacebookException error) {
	                // When the story is posted, echo the success
	                // and the post Id.
	                final String postId = values.getString("post_id");
	                if (postId != null) {
	                    Toast.makeText(DisplayMovieActivity.this, "Post Successful", Toast.LENGTH_SHORT).show();
	                }
	            }
	        })
	        .build();
//	    feedDialog.setCancelable(false);
	    feedDialog.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
				if (arg1 == KeyEvent.KEYCODE_BACK && arg2.getAction() == KeyEvent.ACTION_DOWN) {
			        arg0.dismiss();
			        return true;
			    }

			    return true;

			}
	    	
	    });
	    feedDialog.show();
	    isPost = false;
	    postT = false;
	    System.out.println("FeedDialog");
	    System.out.println(isPost);
	    System.out.println(postT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);

	    // Your existing onActivityResult code

	    if (requestCode == Session.DEFAULT_AUTHORIZE_ACTIVITY_CODE) {
	        Session.getActiveSession().addCallback(statusCallback);
	        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	        flag = true;
	        if (postT == true) {
	        	isPost = true;
	        }
	        else {
	        	isPost = false;
	        }
	        postT = false;
	        System.out.println("ActivityResult");
	        System.out.println(flag);
		    System.out.println(isPost);
		    System.out.println(postT);
	    }
	}

}
