package com.khemlani.bustime;

import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.khemlani.bustime.route.RouteTask;

public class LocationHelper {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public static final String LOCATION_FAILED = "LOCATION_FAILED";
    private static Location CURRENT_LOCATION;
    
	private static LocationClient mLocationClient;
    private static Activity mActivity;
    private static TaskListener mListener;
    private final static ConnectionCallbacks mConnectionCallbacks = new ConnectionCallbacks() {

		@Override
		public void onConnected(Bundle arg0) {
			CURRENT_LOCATION = mLocationClient.getLastLocation();
			if (CURRENT_LOCATION == null) {
				System.out.println("Cannot connect");
				mListener.onTaskFinished(LOCATION_FAILED);
				return;
			}
			System.out.println(CURRENT_LOCATION.getLatitude());
			System.out.println(CURRENT_LOCATION.getLongitude());
			new RouteTask().newInstance(mListener, "&l=" + CURRENT_LOCATION.getLatitude() + "%2C" + CURRENT_LOCATION.getLongitude() + "&t=stops").execute();
//			System.out.println("&l=" + CURRENT_LOCATION.getLatitude() + "%2C" + CURRENT_LOCATION.getLongitude() + "&t=stops");
			mLocationClient.disconnect();
		}

		@Override
		public void onDisconnected() {
		}
    	
    };
    private final static OnConnectionFailedListener mOnConnectionFailedListener = new OnConnectionFailedListener() {

    	@Override
    	public void onConnectionFailed(ConnectionResult connectionResult) {
    		System.out.println("Connected Failed");
    		mListener.onTaskFinished(LOCATION_FAILED);
    		if (connectionResult.hasResolution()) {
                try {
                    // Start an Activity that tries to resolve the error
                    connectionResult.startResolutionForResult(
                            mActivity,
                            CONNECTION_FAILURE_RESOLUTION_REQUEST);
                    /*
                     * Thrown if Google Play services canceled the original
                     * PendingIntent
                     */
                } catch (IntentSender.SendIntentException e) {
                    // Log the error
                    e.printStackTrace();
                }
            } else {
                /*
                 * If no resolution is available, display a dialog to the
                 * user with the error.
                 */
//                showErrorDialog(connectionResult.getErrorCode());
            }
    	}
    	
    };
    		
	public static void getCurrentLocation(Activity activity, TaskListener listener) {
		mListener = listener;
		mActivity = activity;
		mLocationClient = new LocationClient(activity, mConnectionCallbacks, mOnConnectionFailedListener);
		if (servicesConnected()) {
			mLocationClient.connect();
		} else {
			listener.onTaskFinished(LOCATION_FAILED);
		}
	}

	private static boolean servicesConnected() {
		// Check that Google Play services is available
		int resultCode =
				GooglePlayServicesUtil.
				isGooglePlayServicesAvailable(mActivity);
		if (ConnectionResult.SUCCESS == resultCode)
			return true;
		else {
			try {
				GooglePlayServicesUtil.getErrorDialog(resultCode, mActivity, 1).show();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
	}

	public static Location getCurrentLocation() {
		return CURRENT_LOCATION;
	}

}
