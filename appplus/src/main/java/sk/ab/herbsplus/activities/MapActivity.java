package sk.ab.herbsplus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import sk.ab.herbsbase.AndroidConstants;
import sk.ab.herbsplus.R;

/**
 * This shows how to create a simple activity with a map and a marker on the map.
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    private LatLng position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        FloatingActionButton saveButton = (FloatingActionButton) findViewById(R.id.fab_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent();
                data.putExtra(AndroidConstants.STATE_LATITUDE, position.latitude);
                data.putExtra(AndroidConstants.STATE_LONGITUDE, position.longitude);
                setResult(RESULT_OK, data);
                finish();
            }
        });

        if (getIntent().getExtras() != null) {
            double latitude = getIntent().getExtras().getDouble(AndroidConstants.STATE_LATITUDE);
            double longitude = getIntent().getExtras().getDouble(AndroidConstants.STATE_LONGITUDE);
            position = new LatLng(latitude, longitude);
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.addMarker(new MarkerOptions().position(position).draggable(true).title("Marker"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
        map.setOnMarkerDragListener(this);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        position = marker.getPosition();
    }
}
