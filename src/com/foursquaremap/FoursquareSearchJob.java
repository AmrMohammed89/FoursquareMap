package com.foursquaremap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
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
	String strFilter1, strFilter2, strFilter3;

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

						strFilter1 = re.response.venues.get(i).categories
								.get(0).icon.prefix;
						strFilter2 = strFilter1.replaceAll(
								"https://ss3.4sqi.net/img/categories_v2/", "");
						strFilter3 = strFilter2.replaceAll("/", "_");

						downloadFile(
								re.response.venues.get(i).categories.get(0).icon.prefix
										+ "bg_64"
										+ re.response.venues.get(i).categories
												.get(0).icon.suffix,
								strFilter3);

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
		// {"venues":[{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/education/academicbuilding_","suffix":".png"},"id":"4bf58dd8d48988d198941735","name":"College Academic Building","pluralName":"College Academic Buildings","shortName":"Academic Building"}],"id":"51b986e6498e698aca7c7cd7","location":{"cc":"EG","country":"مصر","lat":27.179990768432617,"lng":31.194984436035156,"distance":104.0},"name":"كليه زراعه -  جامعه اسيوط faculty of agriculture  -  Assiut University"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/food/cafe_","suffix":".png"},"id":"4bf58dd8d48988d16d941735","name":"Café","pluralName":"Cafés","shortName":"Café"}],"id":"53655bcd498edaf24ecc1341","location":{"cc":"EG","country":"مصر","lat":27.187283439009477,"lng":31.193835919667443,"distance":864.0},"name":"Stephanie Nile Cruise"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/food/diner_","suffix":".png"},"id":"4bf58dd8d48988d147941735","name":"Diner","pluralName":"Diners","shortName":"Diner"}],"id":"4ef0f9109adf96dc903b7914","location":{"cc":"EG","country":"مصر","lat":27.178235,"lng":31.192224,"distance":231.0},"name":"فطاطري السلطان (عم حمادة)"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/parks_outdoors/neighborhood_","suffix":".png"},"id":"50aa9e094b90af0d42d5de0d","name":"City","pluralName":"Cities","shortName":"City"}],"id":"4f898836e4b076717addf721","location":{"cc":"EG","country":"مصر","lat":27.182900174411813,"lng":31.18951899529166,"distance":587.0},"name":"Assiut City"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/food/cafe_","suffix":".png"},"id":"4bf58dd8d48988d16d941735","name":"Café","pluralName":"Cafés","shortName":"Café"}],"id":"53c1d4df498e50b638bff80a","location":{"cc":"EG","country":"مصر","lat":27.18083333524434,"lng":31.19194029476079,"distance":256.0},"name":"Humpka Café"},{"categories":[],"id":"4e60ce2a1838ad3d0e0e2e0f","location":{"cc":"EG","country":"مصر","lat":27.1812737,"lng":31.1956879,"distance":252.0},"name":"RevathiMB1 busine"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/food/cafe_","suffix":".png"},"id":"4bf58dd8d48988d16d941735","name":"Café","pluralName":"Cafés","shortName":"Café"}],"id":"516fb014e4b0d738f11f1ce5","location":{"cc":"EG","country":"مصر","lat":27.18105259841421,"lng":31.196290680938453,"distance":278.0},"name":"Cafeteria Adwa\u0027 El-Madena"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/food/default_","suffix":".png"},"id":"4bf58dd8d48988d1c4941735","name":"Restaurant","pluralName":"Restaurants","shortName":"Restaurant"}],"id":"51fe78a0498eeea412073f4c","location":{"cc":"EG","country":"مصر","lat":27.181710658233204,"lng":31.191163023873486,"distance":377.0},"name":"Welatain"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/food/cafe_","suffix":".png"},"id":"4bf58dd8d48988d16d941735","name":"Café","pluralName":"Cafés","shortName":"Café"}],"id":"50d9a28ee4b0496accbdd4c7","location":{"cc":"EG","country":"مصر","lat":27.18915,"lng":31.191257,"distance":1107.0},"name":"Cilantro"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/building/default_","suffix":".png"},"id":"52e81612bcbc57f1066b7a37","name":"Distribution Center","pluralName":"Distribution Centers","shortName":"Distributor"}],"id":"53ce307c498e613150079019","location":{"cc":"EG","country":"مصر","lat":27.18173599243164,"lng":31.191627502441406,"distance":345.0},"name":"AT data"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/food/coffeeshop_","suffix":".png"},"id":"4bf58dd8d48988d1e0931735","name":"Coffee Shop","pluralName":"Coffee Shops","shortName":"Coffee Shop"}],"id":"54289884498e82f0e3bbbabe","location":{"cc":"EG","country":"مصر","lat":27.178175,"lng":31.191309,"distance":311.0},"name":"awlad elbalad"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/travel/trainstation_","suffix":".png"},"id":"4bf58dd8d48988d129951735","name":"Train Station","pluralName":"Train Stations","shortName":"Train Station"}],"id":"4e42a82e1838ada3db5fcf52","location":{"cc":"EG","country":"مصر","lat":27.18081061677073,"lng":31.187891720079254,"distance":628.0},"name":"Assuit Railway Station"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/food/cafe_","suffix":".png"},"id":"4bf58dd8d48988d16d941735","name":"Café","pluralName":"Cafés","shortName":"Café"}],"id":"4ebd1e6a82317d5cdaee122a","location":{"cc":"EG","country":"مصر","lat":27.185791746367215,"lng":31.19224225473908,"distance":721.0},"name":"Tekyat El Malek Farouq"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/food/cafe_","suffix":".png"},"id":"4bf58dd8d48988d16d941735","name":"Café","pluralName":"Cafés","shortName":"Café"}],"id":"53ea5bdf498e26df2fca0b62","location":{"cc":"EG","country":"مصر","lat":27.18685912947406,"lng":31.19357869800539,"distance":818.0},"name":"Fresco"},{"categories":[],"id":"4ead4d346da1a0e3a46b51a9","location":{"cc":"EG","country":"مصر","lat":27.18043848643672,"lng":31.19085546923845,"distance":334.0},"name":"City Mall"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/building/default_","suffix":".png"},"id":"4bf58dd8d48988d124941735","name":"Office","pluralName":"Offices","shortName":"Office"}],"id":"527aca5311d2ac98eb0dbf00","location":{"cc":"EG","country":"مصر","lat":27.181787630822267,"lng":31.19167149066925,"distance":346.0},"name":"Egyptian Social Democratic Party"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/building/default_","suffix":".png"},"id":"4bf58dd8d48988d124941735","name":"Office","pluralName":"Offices","shortName":"Office"}],"id":"527ace3e11d2ac98eb0f2258","location":{"cc":"EG","country":"مصر","lat":27.181883070389752,"lng":31.191591024398804,"distance":360.0},"name":"Egyptian Social Democratic Party"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/food/juicebar_","suffix":".png"},"id":"4bf58dd8d48988d112941735","name":"Juice Bar","pluralName":"Juice Bars","shortName":"Juice Bar"}],"id":"51556050e4b035355e663338","location":{"cc":"EG","country":"مصر","lat":27.179522857792826,"lng":31.19781160091999,"distance":370.0},"name":"عصير أللو"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/parks_outdoors/bridge_","suffix":".png"},"id":"4bf58dd8d48988d1df941735","name":"Bridge","pluralName":"Bridges","shortName":"Bridge"}],"id":"51940b8f498e61d610b2207a","location":{"cc":"EG","country":"مصر","lat":27.181296592095045,"lng":31.190833101597526,"distance":376.0},"name":"Kobry El Hillaly"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/food/streetfood_","suffix":".png"},"id":"4bf58dd8d48988d1cb941735","name":"Food Truck","pluralName":"Food Trucks","shortName":"Food Truck"}],"id":"509c24dfe4b070da0895541b","location":{"cc":"EG","country":"مصر","lat":27.182615280151367,"lng":31.192481994628906,"distance":379.0},"name":"Fteer El Reds"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/arts_entertainment/historicsite_","suffix":".png"},"id":"4deefb944765f83613cdba6e","name":"Historic Site","pluralName":"Historic Sites","shortName":"Historic Site"}],"id":"51940a9f498edbf89b2bf963","location":{"cc":"EG","country":"مصر","lat":27.182396390114995,"lng":31.192102458548398,"distance":375.0},"name":"El 7amama"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/building/militarybase_","suffix":".png"},"id":"4e52adeebd41615f56317744","name":"Military Base","pluralName":"Military Bases","shortName":"Military Base"}],"id":"52d323f6498ea862125d26a9","location":{"cc":"EG","country":"مصر","lat":27.179095,"lng":31.198097,"distance":401.0},"name":"modryat amn assuot"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/travel/busstation_","suffix":".png"},"id":"4bf58dd8d48988d1fe931735","name":"Bus Station","pluralName":"Bus Stations","shortName":"Bus Station"}],"id":"52d7bbf1498e4e9d6bd72d1b","location":{"cc":"EG","country":"مصر","lat":27.179103,"lng":31.188842,"distance":519.0},"name":"upper egypt bus station"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/arts_entertainment/movietheater_","suffix":".png"},"id":"4bf58dd8d48988d180941735","name":"Multiplex","pluralName":"Multiplexes","shortName":"Cineplex"}],"id":"4eb53cfedab4fe51118bdf85","location":{"cc":"EG","country":"مصر","lat":27.17826060083522,"lng":31.184989410963766,"distance":909.0},"name":"Cinema Renaissance Assiut"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/food/friedchicken_","suffix":".png"},"id":"4d4ae6fc7a7b7dea34424761","name":"Fried Chicken Joint","pluralName":"Fried Chicken Joints","shortName":"Fried Chicken"}],"id":"4e0661e714959022c8903a05","location":{"cc":"EG","country":"مصر","lat":27.1888452588921,"lng":31.19153208838303,"distance":1068.0},"name":"KFC - Assiut"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/travel/hotel_","suffix":".png"},"id":"4bf58dd8d48988d1fa931735","name":"Hotel","pluralName":"Hotels","shortName":"Hotel"}],"id":"504f846fe4b022569446a01a","location":{"cc":"EG","country":"مصر","lat":27.182185002052343,"lng":31.1970343685142,"distance":417.0},"name":"Nile Pioneer Hotel"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/food/coffeeshop_","suffix":".png"},"id":"4bf58dd8d48988d1e0931735","name":"Coffee Shop","pluralName":"Coffee Shops","shortName":"Coffee Shop"}],"id":"51560a9ce4b0e55d990dc396","location":{"cc":"EG","country":"مصر","lat":27.183176,"lng":31.193316,"distance":414.0},"name":"الدهبية"},{"categories":[{"icon":{"prefix":"https://ss3.4sqi.net/img/categories_v2/food/cafe_","suffix":".png"},"id":"4bf58dd8d48988d16d941735","name":"Café","pluralName":"Cafés","shortName":"Café"}],"id":"52b84c0c498e51bea885de95","location":{"cc":"EG","country":"مصر","lat":27.185438206905186,...

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

	@SuppressLint("NewApi")
	public void downloadFile(String uRl, String fileName) {
		File direct = new File(Environment.getExternalStorageDirectory()
				+ "/TestFoursquare");

		if (!direct.exists()) {
			direct.mkdirs();
		}
		File file = new File(Environment.getExternalStorageDirectory()
				+ "/TestFoursquare" + "/" + fileName+".png");
		if (!file.exists()) {

			DownloadManager mgr = (DownloadManager) context
					.getSystemService(Context.DOWNLOAD_SERVICE);

			Uri downloadUri = Uri.parse(uRl);
			DownloadManager.Request request = new DownloadManager.Request(
					downloadUri);

			request.setAllowedNetworkTypes(
					DownloadManager.Request.NETWORK_WIFI
							| DownloadManager.Request.NETWORK_MOBILE)
					.setAllowedOverRoaming(false)
					.setTitle("Demo")
					.setDescription("Something useful. No, really.")
					.setDestinationInExternalPublicDir("/TestFoursquare",
							fileName + ".png");
			// "fileName.png"

			mgr.enqueue(request);
		}

	}

}
