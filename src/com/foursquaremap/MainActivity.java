package com.foursquaremap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.foursquare.android.nativeoauth.FoursquareCancelException;
import com.foursquare.android.nativeoauth.FoursquareDenyException;
import com.foursquare.android.nativeoauth.FoursquareInvalidRequestException;
import com.foursquare.android.nativeoauth.FoursquareOAuth;
import com.foursquare.android.nativeoauth.FoursquareOAuthException;
import com.foursquare.android.nativeoauth.FoursquareUnsupportedVersionException;
import com.foursquare.android.nativeoauth.model.AccessTokenResponse;
import com.foursquare.android.nativeoauth.model.AuthCodeResponse;
import com.utilities.TokenStore;

public class MainActivity extends FragmentActivity {

	private static final int REQUEST_CODE_FSQ_CONNECT = 200;
	private static final int REQUEST_CODE_FSQ_TOKEN_EXCHANGE = 201;

	private static final String CLIENT_ID = "O15E0GPIVSDCG4DO50HRASQAUKJ3OXSINITOFYCAY0O0GR05";
	private static final String CLIENT_SECRET = "VSEIYNE43FUAOHCPK2M3BLBGMNVZDZHEOIIB3HLH1PLIR3BD";

	private final String ACCESSTOKEN = "accessTokenShared";

	SharedPreferences.Editor editor;
	SharedPreferences prefs;

	String accessToken;

	Button btnLogin;

	AccessTokenResponse tokenResponse;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (getIntent().getBooleanExtra("EXIT", false)) {
			finish();
			return;
		}
		ensureUi();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_FSQ_CONNECT:
			onCompleteConnect(resultCode, data);

			break;

		case REQUEST_CODE_FSQ_TOKEN_EXCHANGE:
			onCompleteTokenExchange(resultCode, data);
			break;

		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void ensureUi() {

		prefs = getSharedPreferences(ACCESSTOKEN, MODE_PRIVATE);
		accessToken = prefs.getString("accessToken", "");
		boolean isAuthorized = !TextUtils.isEmpty(accessToken);
		if (isAuthorized) {
			Intent intent = new Intent(this, MapActivity.class);
			startActivity(intent);
		}
		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnLogin.setVisibility(isAuthorized ? View.GONE : View.VISIBLE);
		btnLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Start the native auth flow.
				Intent intent = FoursquareOAuth.getConnectIntent(
						MainActivity.this, CLIENT_ID);

				// If the device does not have the Foursquare app installed,
				// we'd
				// get an intent back that would open the Play Store for
				// download.
				// Otherwise we start the auth flow.
				if (FoursquareOAuth.isPlayStoreIntent(intent)) {
					toastMessage(MainActivity.this,
							getString(R.string.app_not_installed_message));
					startActivity(intent);
				} else {
					startActivityForResult(intent, REQUEST_CODE_FSQ_CONNECT);
				}
			}
		});
	}

	private void onCompleteConnect(int resultCode, Intent data) {
		AuthCodeResponse codeResponse = FoursquareOAuth.getAuthCodeFromResult(
				resultCode, data);
		Exception exception = codeResponse.getException();

		if (exception == null) {
			// Success.
			String code = codeResponse.getCode();
			performTokenExchange(code);

		} else {
			if (exception instanceof FoursquareCancelException) {
				// Cancel.
				toastMessage(this, "Canceled");

			} else if (exception instanceof FoursquareDenyException) {
				// Deny.
				toastMessage(this, "Denied");

			} else if (exception instanceof FoursquareOAuthException) {
				// OAuth error.
				String errorMessage = exception.getMessage();
				String errorCode = ((FoursquareOAuthException) exception)
						.getErrorCode();
				toastMessage(this, errorMessage + " [" + errorCode + "]");

			} else if (exception instanceof FoursquareUnsupportedVersionException) {
				// Unsupported Fourquare app version on the device.
				toastError(this, exception);

			} else if (exception instanceof FoursquareInvalidRequestException) {
				// Invalid request.
				toastError(this, exception);

			} else {
				// Error.
				toastError(this, exception);
			}
		}
	}

	private void onCompleteTokenExchange(int resultCode, Intent data) {
		tokenResponse = FoursquareOAuth.getTokenFromResult(resultCode, data);
		Exception exception = tokenResponse.getException();

		if (exception == null) {
			String accessToken = tokenResponse.getAccessToken();
			// Success.
			editor = getSharedPreferences(ACCESSTOKEN, MODE_PRIVATE).edit();
			editor.putString("accessToken", accessToken);
			editor.commit();
			// Persist the token for later use.
			// i save it to shared prefs.
			TokenStore.get().setToken(accessToken);

			// Refresh UI.
			ensureUi();

		} else {
			if (exception instanceof FoursquareOAuthException) {
				// OAuth error.
				String errorMessage = ((FoursquareOAuthException) exception)
						.getMessage();
				String errorCode = ((FoursquareOAuthException) exception)
						.getErrorCode();
				toastMessage(this, errorMessage + " [" + errorCode + "]");

			} else {
				// Other exception type.
				toastError(this, exception);
			}
		}
	}

	private void performTokenExchange(String code) {
		Intent intent = FoursquareOAuth.getTokenExchangeIntent(this, CLIENT_ID,
				CLIENT_SECRET, code);
		startActivityForResult(intent, REQUEST_CODE_FSQ_TOKEN_EXCHANGE);
	}

	public static void toastMessage(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	public static void toastError(Context context, Throwable t) {
		Toast.makeText(context, "Check the connection ! ", Toast.LENGTH_SHORT)
				.show();
	}
}
