package com.foursquaremap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.widget.Toast;

import com.controller.Controller.AddCheck_in;
import com.eventbus.OnError;
import com.eventbus.OnSuccess;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.model.FoursquareAddCheckIn;
import com.model.FoursquareSearchModel;
import com.model.FoursquareSearchModel.response;
import com.path.android.jobqueue.JobManager;
import com.utilities.GPSTracker;
import com.utilities.MapStateManager;

import de.greenrobot.event.EventBus;

public class MapActivity extends FragmentActivity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

	private static final int GPS_ERRORDIALOG_REQUEST = 9001;
	private static final float DEFAULTZOOM = 15;
	private static RestAdapter RESTADAPTER;
	static Marker MARKER;

	LatLng lL;
	LatLng llCurrent;

	String accessToken;
	GoogleMap mMap;
	LocationClient mLocationClient;
	LocationManager locationManager;

	GPSTracker gps;

	response response;

	Bitmap returnedBMP;

	FoursquareSearchModel bitmap;
	FoursquareSearchModel re;
	JobManager jobManager;

	Map<String, String> CheckInData;

	Context context;

	public OnSuccess reo;

	Bitmap btmImageSdCard;

	String getCatID;

	public void onEventMainThread(OnError error) {
		Toast.makeText(context, "check the network please", Toast.LENGTH_LONG)
				.show();
	}

	public void onEventMainThread(OnSuccess success) {
		reo = success;
		mMap.clear();
		llCurrent = MARKER.getPosition();
		setMarker(llCurrent.latitude, llCurrent.longitude);

		for (int i = 0; i < success.getSuccess().venues.size(); i++) {
			try {
				getCatID = success.getSuccess().venues.get(i).categories.get(0).id;
				btmImageSdCard = getImage(getCatID);
				mMap.addMarker(new MarkerOptions()
						.position(
								new LatLng(
										success.getSuccess().venues.get(i).location.lat,
										success.getSuccess().venues.get(i).location.lng))
						.flat(false)
						.title(success.getSuccess().venues.get(i).name)
						.snippet(
								success.getSuccess().venues.get(i).categories
										.get(0).name)
										.icon(BitmapDescriptorFactory
												.fromBitmap(btmImageSdCard)));

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		if (servicesOK()) {
			setContentView(R.layout.activity_map);
			if (initMap()) {
				gps = new GPSTracker(this);

				if (gps.canGetLocation()) {

					mLocationClient = new LocationClient(this, this, this);
					mLocationClient.connect();
				} else {
					gps.showSettingsAlert();
				}

			} else {
				Toast.makeText(this, "Map not available!", Toast.LENGTH_SHORT)
						.show();
			}
		} else {
			setContentView(R.layout.activity_main);
		}
		RESTADAPTER = new RestAdapter.Builder().setEndpoint(
				"https://api.foursquare.com/v2").build();
		final SharedPreferences prefs = getSharedPreferences(
				"accessTokenShared", MODE_PRIVATE);
		accessToken = prefs.getString("accessToken", "");
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, this);

		Location locationNet = locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		try {
			jobManager = new JobManager(context);
			jobManager.addJobInBackground(new FoursquareSearchJob(locationNet
					.getLatitude() + "," + locationNet.getLongitude(),
					accessToken, timeMilisToString(System.currentTimeMillis()),
					"100", context, 1));
		} catch (Exception e) {
			e.printStackTrace();
		}

		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {
				if (checkConnection()) {
					CheckInData = new HashMap<String, String>();
					CheckInData.put("oauth_token",
							prefs.getString("accessToken", ""));

					CheckInData.put(
							"venueId",
							getVenueId(marker.getPosition().latitude,
									marker.getPosition().longitude));
					CheckInData.put("v",
							timeMilisToString(System.currentTimeMillis()));
					CheckInData.put("broadcast", "public");
					AddCheck_in retrofitAddUser = RESTADAPTER
							.create(AddCheck_in.class);
					retrofitAddUser.AddCheck_in(CheckInData,
							new Callback<FoursquareAddCheckIn>() {

								@Override
								public void failure(RetrofitError arg0) {
									Toast.makeText(context, arg0.getMessage(),
											Toast.LENGTH_LONG).show();
								}

								@Override
								public void success(FoursquareAddCheckIn arg0,
										Response arg1) {
									Toast.makeText(context,
											"Check in success !",
											Toast.LENGTH_LONG).show();

								}
							});
				} else {
					Toast.makeText(context, "check the connection ",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		EventBus.getDefault().register(this);
	}

	public String getVenueId(double lat, double lan) {
		for (int i = 0; i < reo.getSuccess().venues.size(); i++) {
			if (reo.getSuccess().venues.get(i).location.lat == lat
					&& reo.getSuccess().venues.get(i).location.lng == lan) {
				return reo.getSuccess().venues.get(i).id;
			}
		}
		return "";
	}

	public boolean servicesOK() {
		int isAvailable = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);

		if (isAvailable == ConnectionResult.SUCCESS) {
			return true;
		} else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable,
					this, GPS_ERRORDIALOG_REQUEST);
			dialog.show();
		} else {
			Toast.makeText(this, "Can't connect to Google Play services",
					Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	@SuppressLint("NewApi")
	private boolean initMap() {
		if (!checkConnection()) {
			Toast.makeText(this, "Check internet connection !",
					Toast.LENGTH_SHORT).show();
		}
		if (mMap == null) {
			SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map);
			mMap = mapFrag.getMap();
			String saved = read("savefile");
			if (saved != null) {
				Gson gson = new Gson();
				response = gson.fromJson(saved, response.class);
				for (int i = 0; i < response.venues.size(); i++) {
					try {
						getCatID = response.venues.get(i).categories.get(0).id;
						btmImageSdCard = getImage(getCatID);
						mMap.addMarker(new MarkerOptions()
								.position(
										new LatLng(
												response.venues.get(i).location.lat,
												response.venues.get(i).location.lng))
								.flat(false)
								.title(response.venues.get(i).name)
								.snippet(
										response.venues.get(i).categories
												.get(0).name)
								.icon(BitmapDescriptorFactory
										.fromBitmap(btmImageSdCard)));

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		return (mMap != null);
	}

	private String timeMilisToString(long milis) {
		SimpleDateFormat sd = new SimpleDateFormat("yyyyMMdd");
		Calendar calendar = Calendar.getInstance();

		calendar.setTimeInMillis(milis);

		return sd.format(calendar.getTime());
	}

	@Override
	protected void onStop() {
		super.onStop();
		MapStateManager mgr = new MapStateManager(this);
		mgr.saveMapState(mMap);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MapStateManager mgr = new MapStateManager(this);
		CameraPosition position = mgr.getSavedCameraPosition();
		if (position != null) {
			CameraUpdate update = CameraUpdateFactory
					.newCameraPosition(position);
			mMap.moveCamera(update);
			mMap.setMapType(mgr.getSavedMapType());
		}

	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
	}

	@Override
	public void onConnected(Bundle arg0) {
		final Location currentLocation = mLocationClient.getLastLocation();
		if (currentLocation == null) {
			Toast.makeText(this, "Current location isn't available",
					Toast.LENGTH_SHORT).show();
		} else {
			lL = new LatLng(currentLocation.getLatitude(),
					currentLocation.getLongitude());
			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(lL,
					DEFAULTZOOM);
			mMap.animateCamera(update);

			if (MARKER != null) {
				MARKER.remove();
			}

			setMarker(currentLocation.getLatitude(),
					currentLocation.getLongitude());

		}
	}

	@Override
	public void onDisconnected() {
	}

	@Override
	public void onLocationChanged(Location location) {

	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	public String read(String fileName) {
		FileInputStream fis;
		String getObject = null;
		try {
			fis = context.openFileInput(fileName);

			ObjectInputStream is = null;
			try {
				is = new ObjectInputStream(fis);
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				getObject = (String) is.readObject();
			} catch (OptionalDataException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return getObject;
	}

	public void onBakPressed() {
		finish();
	}

	private void setMarker(double lat, double lng) {

		if (MARKER != null) {
			MARKER.remove();
		}

		MarkerOptions options = new MarkerOptions().position(
				new LatLng(lat, lng)).icon(
				BitmapDescriptorFactory.defaultMarker());
		MARKER = mMap.addMarker(options);

	}

	private boolean checkConnection() {

		final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
		if (!(activeNetwork != null && activeNetwork.isConnected())) {
			return false;
		} else {
			return true;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(MapActivity.this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("EXIT", true);
			startActivity(intent);
		}
		return super.onKeyDown(keyCode, event);
	}

	public Bitmap getImage(String fileName) {
		File f = new File(Environment.getExternalStorageDirectory()
				+ "/TestFoursquare" + "/" + fileName + ".png");
		Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
		return bmp;
	}

}
