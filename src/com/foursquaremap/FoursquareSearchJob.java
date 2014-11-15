package com.foursquaremap;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.controller.Controller.getNearPlace;
import com.eventbus.OnError;
import com.eventbus.OnSuccess;
import com.google.gson.Gson;
import com.model.FoursquareSearchModel;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;

public class FoursquareSearchJob extends Job {
	String ll, oauth_token, date, radius;
	URL location;
	InputStream is;
	Bitmap returnedBMP;
	Context context;
	Bitmap b;
	private static RestAdapter restAdapter;

	public FoursquareSearchJob(String ll, String oauth_token, String date,
			String radius, Context con, int PRIORITY) {
		super(new Params(PRIORITY).requireNetwork());
		this.ll = ll;
		this.oauth_token = oauth_token;
		this.date = date;
		this.radius = radius;
		this.context = con;
	}

	@Override
	public void onAdded() {

	}

	@Override
	public void onRun() throws Throwable {
		restAdapter = new RestAdapter.Builder().setEndpoint(
				"https://api.foursquare.com/v2").build();
		getNearPlace retrofitNearPlaceList = restAdapter
				.create(getNearPlace.class);

		try {
			FoursquareSearchModel re = retrofitNearPlaceList.getNearPlace(ll,
					oauth_token, date, radius);
			if (re.meta.code != 200) {
				EventBus.getDefault().post(new OnError("error"));
			} else {

				Gson gson = new Gson();
				String json = gson.toJson(re.response);
				save("savefile", json);

				for (int i = 0; i < re.response.venues.size(); i++) {
					try {

						re.response.venues.get(i).bitmap = DownloadBMP(re.response.venues
								.get(i).categories.get(0).icon.prefix
								+ "bg_64"
								+ re.response.venues.get(i).categories.get(0).icon.suffix);

					} catch (Exception e) {
						e.printStackTrace();
						re.response.venues.get(i).bitmap = BitmapFactory
								.decodeResource(context.getResources(),
										R.drawable.ic_launcher);
						continue;
					}

				}

				EventBus.getDefault().post(new OnSuccess(re.response));
			}

		} catch (RetrofitError e) {
			Toast.makeText(context, e.getResponse().getStatus(),
					Toast.LENGTH_LONG).show();
		}
	}

	private Bitmap DownloadBMP(String url) {
		// create a URL object using the passed string
		try {
			returnedBMP = BitmapFactory.decodeStream((InputStream) new URL(url
					.toString()).getContent());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return returnedBMP;
	}

	@Override
	protected void onCancel() {

	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {
		return false;
	}

	public void save(String fileName, String re) {

		FileOutputStream fos = null;
		ObjectOutputStream os = null;
		try {
			fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			os = new ObjectOutputStream(fos);
			os.writeObject(re);
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
