package com.controller;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;
import retrofit.http.QueryMap;

import com.model.FoursquareAddCheckIn;
import com.model.FoursquareSearchModel;

public class Controller {
	public interface getNearPlace {
		@GET("/venues/search?filters[0][operator]=equals")
		FoursquareSearchModel getNearPlace(@Query("ll") String ll,
				@Query("oauth_token") String oauth_token, @Query("v") String v,
				@Query("radius") String radius);
	}

	public interface AddCheck_in {
		@POST("/checkins/add")
		void AddCheck_in(@QueryMap Map<String, String> checkIn,
				Callback<FoursquareAddCheckIn> callback);

	}

}
