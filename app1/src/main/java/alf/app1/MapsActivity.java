package alf.app1;

import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import java.util.ArrayList;


public class MapsActivity extends FragmentActivity implements ConnectionCallbacks, OnConnectionFailedListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    boolean mRequestingLocationUpdates = true;
    String[] busStopList;
    String[] busStopName;
    String[] busStopCode;
    String[] busStopIndicator;
    ArrayList<LatLng> latlngList = new ArrayList<LatLng>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        createLocationRequest();
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        //setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //setUpMapIfNeeded();
        //createLocationRequest();
        //buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        mMap.setMyLocationEnabled(true);

        //BusStop busStop = new BusStop(51.49598,-0.14191,500);

        BusStop busStop = new BusStop(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 750);

        new GetBusStopLocation().execute(busStop);

        //mMap.setOnMarkerClickListener(GoogleMap.OnMarkerClickListener listen);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }//buildGoogleApiClient ends

    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if(mLastLocation!=null) {
            setUpMapIfNeeded();
            Log.d("tes", "Lat: " +mLastLocation.getLatitude()+ " Long: " +mLastLocation.getLongitude());
        }

    }//onConnected() ends

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
    }


    private class GetBusStopLocation extends AsyncTask<BusStop, Void, String[]>{

        @Override
        protected String[] doInBackground(BusStop... busStop) {

            busStopList = busStop[0].getGeoCodeList();
            busStopIndicator = busStop[0].getStopIndicatorList();
            busStopCode = busStop[0].getStopCodeList();

            busStopName = new String[busStopCode.length];

            for(int i=0; i<busStopCode.length; i++){
                busStopName[i] = busStop[0].getStopName(busStopCode[i]);
            }//for ends

            BusThrough busThrough = new BusThrough(busStopCode[1]);
            Log.d("tes", "StopCode=" + busStopCode[1]);
            for(int i=0; i<busThrough.busThroughArr.length; i++){
                Log.d("tes", busThrough.busThroughArr[i]);
            }

            return busStopList;
        }

        protected void onPostExecute(String[] busStopList) {

            String markerTitle = new String();

            if (busStopList.length > 0) {

                int idx = 0;

                for (int i = 0; i <= busStopList.length / 2; i = i + 2) {
                    markerTitle = busStopName[idx] + ", Stop " + busStopIndicator[idx];
                    //markerTitle = busStopIndicator[idx];

                    latlngList.add(new LatLng((Double.parseDouble(busStopList[i])), (Double.parseDouble(busStopList[i + 1]))));
                    mMap.addMarker(new MarkerOptions().position(new LatLng(latlngList.get(idx).latitude, latlngList.get(idx).longitude)).title(markerTitle));
                    idx++;
                }//for ends
            }//if ends

            else
                Toast.makeText(getApplicationContext(), R.string.notfound_toast, Toast.LENGTH_LONG).show();
        }

    }//GetStopLocation Class ends

}//MapsActivity Class ends
