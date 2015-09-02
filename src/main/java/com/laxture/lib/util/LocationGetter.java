package com.laxture.lib.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;

public final class LocationGetter {

    private static final String LOCATION_PROVIDER_MOCK = "mock";

    public static final float DEFAULT_ACCEPTED_ACCURACY = 50f;
    public static final long DEFAULT_KEEP_ACCURACY_DURATION = 10*1000;
    public static final long DEFAULT_LOCATION_UPDATE_INTERVAL_MILLIS = 100;
    public static final long DEFAULT_LOCATION_UPDATE_DURATION_LIMIT = 30*1000;

    // config fields
    private ArrayList<LocationHandler> mLocationHandlers = new ArrayList<>();
    private LocationListener mListener;
    private float mAcceptedAccuracy;
    private long mKeepAccuracyDuration;
    private long mLocationUpdateIntervalMillis;

    // state fields
    private long mDuration;
    private Location mLastBestLocation;
    private CountDownTimer mLocationStopUpdatingTimer;
    private CountDownTimer mLocationDeterminedTimer;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private boolean mUpdatingLocation;
    public boolean isUpdatingLocation() { return mUpdatingLocation; }

    private LocationGetter(Context context) {
        LocationHandler googleLocationHandler = new GoogleLocationHandlerImpl(context);
        mLocationHandlers.add(googleLocationHandler);
    }

    //*************************************************************************
    // Public Methods
    //*************************************************************************

    /**
     * Start updating location. If location updating has been started already,
     * check mUpdatingLocation to prevent register listener or timer duplicated.
     */
    public void startUpdatingLocation() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!mUpdatingLocation) {
                    for (LocationHandler locationHandler : mLocationHandlers) {
                        LLog.i("Start location service %s", locationHandler.getName());
                        locationHandler.startUpdatingLocation();
                    }

                    mLocationStopUpdatingTimer = new LocationStopUpdatingTimer(mDuration, mDuration);
                    mLocationStopUpdatingTimer.start();
                }

                mListener.onStartUpdatingLocation(LocationGetter.this);
                mUpdatingLocation = true;
            }
        });
    }

    /**
     * Stop updating location. If location updating has been stopped already,
     * check mUpdatingLocation to prevent register listener or timer duplicately.
     */
    public void stopUpdatingLocation() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mUpdatingLocation) {
                    for (LocationHandler locationHandler : mLocationHandlers) {
                        LLog.i("Stop location service %s", locationHandler.getName());
                        locationHandler.stopUpdatingLocation();
                    }

                    if (mLocationStopUpdatingTimer != null) {
                        mLocationStopUpdatingTimer.cancel();
                        mLocationStopUpdatingTimer = null;
                    }

                    if (mLocationDeterminedTimer != null) {
                        mLocationDeterminedTimer.cancel();
                        mLocationDeterminedTimer = null;
                    }
                }

                mListener.onStopUpdatingLocation(LocationGetter.this);
                mUpdatingLocation = false;
                mLastBestLocation = null;
            }
        });
    }

    public void setMockLocation(final double latitude, final double longitude, final float accuracy) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mUpdatingLocation) {
                    for (LocationHandler locationHandler : mLocationHandlers) {
                        LLog.i("Set mock location service %s", locationHandler.getName());
                        locationHandler.setMockLocation(latitude, longitude, accuracy);
                    }
                }
            }
        });
    }

    public static Location getLastKnownLocation(Context context) {
        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) != null)
            return locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

        return sLastKnownLocation;
    }

    private static Location sLastKnownLocation;

    public interface LocationListener {
        void onLocationUpdated(LocationGetter getter, Location location);
        void onGPSDisabled(LocationGetter getter);
        void onStartUpdatingLocation(LocationGetter getter);
        void onStopUpdatingLocation(LocationGetter getter);
        void onLocationDetermined(LocationGetter getter);
    }

    //*************************************************************************
    // Internal Stuff
    //*************************************************************************

    private void receiveLocationUpdate(final Location location) {
        LLog.i("LocationGetter get updated location: "
                + location.getLatitude() + ","
                + location.getLongitude()
                + " with accuracy " + location.getAccuracy()
                + " by " + location.getProvider());

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!mUpdatingLocation) return;

                if (mLastBestLocation != null
                        && mLastBestLocation.getAccuracy() <= location.getAccuracy()) {
                    LLog.i("Updated location is not more accreted than the previous location");
                    return;
                }

                mLastBestLocation = location;
                sLastKnownLocation = location;

                mListener.onLocationUpdated(LocationGetter.this, location);

                // If the location accuracy is BETTER than 50m, register a timer for 10 seconds.
                // In 10 seconds, if the location is STILL better than 50m, then stop the update,
                // else, cancel the timer.
                if (location.getAccuracy() <= mAcceptedAccuracy) {
                    if (mLocationDeterminedTimer == null) {
                        mLocationDeterminedTimer = new LocationStopUpdatingTimer(
                                mKeepAccuracyDuration, mKeepAccuracyDuration);
                        mLocationDeterminedTimer.start();
                        LLog.i("Get qulified location, will stop updating location in %s seconds",
                                (int)mKeepAccuracyDuration/1000);
                    }
                } else {
                    // cancel the timer as location is not qualified.
                    if (mLocationDeterminedTimer != null) {
                        mLocationDeterminedTimer.cancel();
                        mLocationDeterminedTimer = null;
                    }
                }
            }
        });
    }

    class LocationStopUpdatingTimer extends CountDownTimer {

        public LocationStopUpdatingTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {} // do nothing.

        @Override
        public void onFinish() {
            LLog.i("Stop updating location by timer.");
            stopUpdatingLocation();
            mListener.onLocationDetermined(LocationGetter.this);
        }
    }

    //*************************************************************************
    // LocationHandler Implementation
    //*************************************************************************

    interface LocationHandler {
        String getName();
        void startUpdatingLocation();
        void stopUpdatingLocation();
        void setMockLocation(double latitude, double longitude, float accuracy);
    }

    public class GoogleLocationHandlerImpl implements LocationHandler {

        private LocationManager mLocationManager;

        public GoogleLocationHandlerImpl(Context context) {
            mLocationManager = (LocationManager)
                    context.getSystemService(Context.LOCATION_SERVICE);
        }

        @Override
        public String getName() {
            return "Google Location Provider";
        }

        @Override
        public void startUpdatingLocation() {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    mLocationUpdateIntervalMillis, 1, mGoogleLocationListener);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    mLocationUpdateIntervalMillis, 1, mGoogleLocationListener);
            mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
                    mLocationUpdateIntervalMillis, 1, mGoogleLocationListener);
        }

        @Override
        public void stopUpdatingLocation() {
            mLocationManager.removeUpdates(mGoogleLocationListener);
            if (mLocationManager.getProvider(LOCATION_PROVIDER_MOCK) != null)
                mLocationManager.removeTestProvider(LOCATION_PROVIDER_MOCK);
        }

        private android.location.LocationListener mGoogleLocationListener =
            new android.location.LocationListener() {

            @Override
            public void onLocationChanged(final Location location) {
                receiveLocationUpdate(location);
            }

            @Override
            public void onProviderDisabled(String provider) {
                if (provider.equals(LocationManager.GPS_PROVIDER))
                        mListener.onGPSDisabled(LocationGetter.this);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // do nothing
            }

            @Override
            public void onProviderEnabled(String provider) {
                // do nothing
            }
        };

        /**
         * Mock location on device (not emulator) for dev purpose.
         * Example: setMockLocation(-120.08495, 37.422006, (float)11.0);
         */
        @Override
        public void setMockLocation(double latitude, double longitude, float accuracy) {
            if (mLocationManager.getProvider(LOCATION_PROVIDER_MOCK) == null) {
                mLocationManager.addTestProvider (LOCATION_PROVIDER_MOCK,
                        "requiresNetwork" == "",
                        "requiresSatellite" == "",
                        "requiresCell" == "",
                        "hasMonetaryCost" == "",
                        "supportsAltitude" == "",
                        "supportsSpeed" == "",
                        "supportsBearing" == "",
                        android.location.Criteria.POWER_LOW,
                        android.location.Criteria.ACCURACY_FINE
                      );
            }

            Location newLocation = new Location(LocationManager.GPS_PROVIDER);
            newLocation.setLatitude(latitude);
            newLocation.setLongitude(longitude);
            newLocation.setAccuracy(accuracy);
            newLocation.setTime(System.currentTimeMillis());

            mLocationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
            mLocationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER,
                    LocationProvider.AVAILABLE, null, System.currentTimeMillis());
            mLocationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, newLocation);
        }
    }

    //*************************************************************************
    // Builder
    //*************************************************************************

    public static class LocationGetterBuilder {

        public long updatingDuration = DEFAULT_LOCATION_UPDATE_DURATION_LIMIT;
        public float acceptedAccuracy = DEFAULT_ACCEPTED_ACCURACY;
        public long keepAccuracyDuration = DEFAULT_KEEP_ACCURACY_DURATION;
        public long locationUpdateIntervalMillis = DEFAULT_LOCATION_UPDATE_INTERVAL_MILLIS;

        public LocationGetter build(Context context, LocationListener listener) {
            LocationGetter locationGetter = new LocationGetter(context);
            locationGetter.mDuration = updatingDuration;
            locationGetter.mAcceptedAccuracy = acceptedAccuracy;
            locationGetter.mKeepAccuracyDuration = keepAccuracyDuration;
            locationGetter.mLocationUpdateIntervalMillis = locationUpdateIntervalMillis;

            locationGetter.mListener = listener;

            return locationGetter;
        }
    }

}
