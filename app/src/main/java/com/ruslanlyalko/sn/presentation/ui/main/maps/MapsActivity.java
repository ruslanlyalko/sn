package com.ruslanlyalko.sn.presentation.ui.main.maps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ruslanlyalko.sn.R;
import com.ruslanlyalko.sn.common.Keys;
import com.ruslanlyalko.sn.presentation.base.BaseActivity;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback {

    private LatLng mLocation;

    public static Intent getLaunchIntent(final Activity launchIntent, final LatLng location) {
        Intent intent = new Intent(launchIntent, MapsActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_LOCATION, location);
        return intent;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_maps;
    }

    @Override
    protected void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mLocation = bundle.getParcelable(Keys.Extras.EXTRA_LOCATION);
        }
    }

    @Override
    protected void setupView() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney and move the camera
        Marker marker = googleMap.addMarker(new MarkerOptions().position(mLocation).title(getString(R.string.text_maps_marker_title)));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
    }
}
