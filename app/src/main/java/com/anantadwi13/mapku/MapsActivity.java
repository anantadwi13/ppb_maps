package com.anantadwi13.mapku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, LocationListener {

    private static final int REQUEST_PERMISSION_GPS = 321;
    private GoogleMap mMap;

    private EditText etLat, etLong, etZoom, etSearch;
    private Button btnJump, btnSearch;

    private Double lat = -7.280002, lng = 112.797485;
    private Marker marker, markerPosition;
    private Float zoom = 15f;

    private LocationManager locationManager;
    private FusedLocationProviderClient fusedLocation;

    private ArrayList<Marker> savedMarker = new ArrayList<>();

    private static boolean firstLaunch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        etLat = findViewById(R.id.etLat);
        etLong = findViewById(R.id.etLong);
        etZoom = findViewById(R.id.etZoom);
        etSearch = findViewById(R.id.etSearch);
        btnJump = findViewById(R.id.btnJump);
        btnSearch = findViewById(R.id.btnSearch);

        btnJump.setOnClickListener(this);
        btnSearch.setOnClickListener(this);

        etLat.setText(String.valueOf(lat));
        etLong.setText(String.valueOf(lng));
        etZoom.setText(String.valueOf(zoom));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fusedLocation = LocationServices.getFusedLocationProviderClient(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng latLng = new LatLng(lat, lng);
        marker = mMap.addMarker(new MarkerOptions().position(latLng).title("My Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_GPS);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 200, this);
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()){
                    MapsActivity.this.onLocationChanged(location);
                }
            }
        };
        fusedLocation.requestLocationUpdates(mLocationRequest, locationCallback, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.normal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.terrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.sattelite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.hybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.no:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnJump:
                try{
                    double lat = Double.parseDouble(etLat.getText().toString());
                    double lng = Double.parseDouble(etLong.getText().toString());
                    float zoom = Float.parseFloat(etZoom.getText().toString());
                    jumpToLocation(lat, lng, zoom);
                } catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case R.id.btnSearch:
                if (etSearch.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Harap mengisi isian search!", Toast.LENGTH_SHORT).show();
                    return;
                }
                try{
                    Geocoder geocoder = new Geocoder(getBaseContext());
                    List<Address> addressList = geocoder.getFromLocationName(etSearch.getText().toString(), 1);
                    Address address = addressList.get(0);

                    Toast.makeText(this, "Alamat : "+address.getAddressLine(0), Toast.LENGTH_SHORT).show();
                    jumpToLocation(address.getLatitude(), address.getLongitude(), zoom);
                } catch (Exception e){
                    e.printStackTrace();
                }
                break;
        }
    }

    private void jumpToLocation(double mLat, double mLng, float mZoom){
        try {
            lat = mLat;
            lng = mLng;
            zoom = mZoom;
            LatLng newLatLng = new LatLng(lat, lng);
            if (marker != null)
                marker.remove();
            marker = mMap.addMarker(new MarkerOptions().position(newLatLng).title("My Marker"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, zoom));
            etLat.setText(String.valueOf(mLat));
            etLong.setText(String.valueOf(mLng));
            etZoom.setText(String.valueOf(mZoom));

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(newLatLng);
            if (markerPosition!=null) {
                builder.include(markerPosition.getPosition());
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), (int) (104 * Resources.getSystem().getDisplayMetrics().density)));
            } else
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, zoom));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        // Toast.makeText(this, String.format(Locale.US,"%f %f", location.getLatitude(), location.getLongitude()), Toast.LENGTH_SHORT).show();

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        Drawable icon = ContextCompat.getDrawable(this, R.drawable.ic_my_location_24px);
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        icon.draw(canvas);

        if (markerPosition != null) {
            boolean free = true;
            for (Marker marker : savedMarker)
                if (distance(latLng.latitude, marker.getPosition().latitude, latLng.longitude, marker.getPosition().longitude) < 5.0) {
                    free = false;
                    break;
                }
            if (free)
                savedMarker.add(mMap.addMarker(new MarkerOptions().position(markerPosition.getPosition())
                        .title(String.format(Locale.US, "Saved Position %d", savedMarker.size()))));
            markerPosition.remove();
        }
        markerPosition = mMap.addMarker(new MarkerOptions().position(latLng).title("My Position")
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));

        if (firstLaunch) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(latLng);
            if (marker != null) {
                builder.include(marker.getPosition());
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), (int) (104 * Resources.getSystem().getDisplayMetrics().density)));
            } else
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
            firstLaunch = false;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public double distance(double lat1, double lat2, double lon1,
                                  double lon2) {
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        return distance;
    }

}
