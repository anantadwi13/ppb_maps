package com.anantadwi13.mapku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;

    private EditText etLat, etLong, etZoom, etSearch;
    private Button btnJump, btnSearch;

    private Double lat = -7.280002, lng = 112.797485;
    private Marker marker;
    private Float zoom = 15f;

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
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
