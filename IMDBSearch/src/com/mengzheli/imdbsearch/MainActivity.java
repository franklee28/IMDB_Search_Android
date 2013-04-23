package com.mengzheli.imdbsearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;


public class MainActivity extends Activity {

	private EditText title;
	private Spinner type;
	private ArrayAdapter<CharSequence> typeAdapter;
	private static final String DEBUG_TAG = "IMDBSearch";
	private Context context = MainActivity.this;
	private ProgressDialog progressDialog;
	
	private Bitmap cover = null;
	String coverUrl;
	String mtitle;
	String year;
	String director;
	String detail;
	String rating;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        title = (EditText)findViewById(R.id.inputTitle);
        type = (Spinner)findViewById(R.id.inputType);
        
        typeAdapter = ArrayAdapter.createFromResource(this, R.array.type, android.R.layout.simple_spinner_item);
        
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        type.setAdapter(typeAdapter);
        
        type.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void searchRequest(View view) {
    	String titleValue = title.getText().toString();
    	String typeValue = type.getSelectedItem().toString();
    	if (typeValue.equals("All Types")) {
    		typeValue = "feature,tv_series,game";
    	}
    	else if (typeValue.equals("Feature Film")) {
    		typeValue = "feature";
    	}
    	else if (typeValue.equals("TV Series")) {
    		typeValue = "tv_series";
    	}
    	else if (typeValue.equals("Video Game")) {
    		typeValue = "game";
    	}
    	try {
    		titleValue = titleValue.trim();
    		titleValue = URLEncoder.encode(titleValue, "UTF-8");
    	}
    	catch (Exception e) {
    	}
    	String url = "http://cs-server.usc.edu:12691/examples/servlet/IMDBSearch?title=" + titleValue + "&title_type=" + typeValue;
    	ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    	if (networkInfo != null && networkInfo.isConnected()) {
    	    // fetch data
    		progressDialog = new ProgressDialog(context);
			progressDialog.setMessage("Loading...");
			progressDialog.setCancelable(false);
			progressDialog.show();
    		new RespondRequest().execute(url);
    	}
    	else {
    	    String atitle = "ERROR";
    	    String amessage = "No network connection available.";
    	    String button1 = "Back";
    	    AlertDialog.Builder ad = new AlertDialog.Builder(context);
    	    ad.setTitle(atitle);
    	    ad.setMessage(amessage);
    	    ad.setNegativeButton(button1, new OnClickListener() {
    	    	public void onClick(DialogInterface dialog, int arg1) {
    	    		dialog.cancel();
    	    	}
    	    });
    	    ad.setCancelable(true);
    	    ad.show();
    	}
    }
    
    private class RespondRequest extends AsyncTask<String, Integer, ArrayList<IMDBItem>> {

		@Override
		protected ArrayList<IMDBItem> doInBackground(String... urls) {
			ArrayList<IMDBItem> resultArray = null;
			try {
				resultArray = respondRequest(urls[0]);
			}
			catch (Exception e)
			{
				Log.d(DEBUG_TAG, e.toString());
			}
			return resultArray;
		}
    	
		@Override
		protected void onPostExecute(ArrayList<IMDBItem> result) {
			progressDialog.dismiss();
			Intent intent = new Intent(context, DisplayMovieActivity.class);
			intent.putExtra("resultArray", result);
			startActivity(intent);
		}
		
		private ArrayList<IMDBItem> respondRequest(String myurl) throws IOException, JSONException {
		    InputStream is = null;
		    ArrayList<IMDBItem> imdbArray = new ArrayList<IMDBItem>();
		        
		    try {
		        URL url = new URL(myurl);
		        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		        conn.setReadTimeout(10000);
		        conn.setConnectTimeout(15000);
		        conn.setRequestMethod("GET");
		        conn.setDoInput(true);

		        conn.connect();
		        int response = conn.getResponseCode();
		        Log.d(DEBUG_TAG, "The response is: " + response);
		        is = conn.getInputStream();

		        String json = readJSON(is);
		        System.out.println(json);
		        
		        JSONTokener parser = new JSONTokener(json);
		        JSONObject jsonObject = new JSONObject(json);
				JSONObject results = jsonObject.getJSONObject("results");
				JSONArray result = results.getJSONArray("result");
				JSONObject t = result.getJSONObject(0);
				if (t.isNull("Error") != true) {
					IMDBItem imdb = new IMDBItem(t.getString("Error"));
					imdbArray.add(imdb);
				}
				else if (t.isNull("Exception") != true) {
					IMDBItem imdb = new IMDBItem(t.getString("Exception"));
					imdbArray.add(imdb);
				}
				else {
					for (int i=0; i < result.length(); i++) {
						JSONObject temp = (JSONObject) result.get(i);
						coverUrl = temp.getString("cover");
						mtitle = temp.getString("title");
						year = temp.getString("year");
						director = temp.getString("director");
						detail = temp.getString("details");
						rating = temp.getString("rating");
						cover = loadImage(coverUrl);
						IMDBItem imdb = new IMDBItem(coverUrl, mtitle, year, director, detail, rating, cover);
						imdbArray.add(imdb);
					}
				}
		        
		        return imdbArray;
		        
		    }
		    finally {
		        if (is != null) {
		            is.close();
		        } 
		    }
		}
		
		private Bitmap loadImage(String imageUrl) throws IOException	{
		    Bitmap map = null;
		    InputStream is = new URL(imageUrl).openStream();
		    try {
		        map = BitmapFactory.decodeStream(is);
		    }
		    finally {
		        if (is != null) {
		            is.close();
		        } 
		    }
		    if (map == null) {
		        Log.d(DEBUG_TAG, "null drawable");
		    } else {
		        Log.d(DEBUG_TAG, "not null drawable");
		    }

		    return map;
		}
		
		private String readJSON(InputStream stream) throws IOException, UnsupportedEncodingException {
		    BufferedReader reader = null;
		    reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));        
		    StringBuilder sb = new StringBuilder();
		    String line = null;
		    while ((line = reader.readLine()) != null) {
		    	sb.append(line);
		    }
		    return sb.toString();
		}
    }
}
