package com.example.winnielew.locomole;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();

    public static ArrayList<LatLng> HotspotLatLng = new ArrayList<LatLng>();
    public static ArrayList<CameraPosition> HotspotCamera = new ArrayList<>();
    public static ArrayList<Marker> MarkerList = new ArrayList<>();

    private Marker newMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        HotspotLatLng.add(new LatLng(1.2939, 103.8533));
        HotspotLatLng.add(new LatLng(1.2950, 103.8603));
        HotspotLatLng.add(new LatLng(1.2928, 103.8597));
        HotspotLatLng.add(new LatLng(1.2832, 103.8603));

        for (int numLatLng = 0; numLatLng < HotspotLatLng.size(); numLatLng++) {
            HotspotCamera.add(CameraPosition.fromLatLngZoom(HotspotLatLng.get(numLatLng), 14f));
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HotspotLatLng.get(0), 14f));

        mMap.addMarker(new MarkerOptions()
                        .position(HotspotLatLng.get(0))
                        .title("Start Location")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_red_1))
                        .alpha(0.4f)

        );

        /*loop to get the file name*/
        Resources r = getResources();
        int endPos = r.getIdentifier("map_marker_red_" + (HotspotLatLng.size()), "drawable", "com.example.winnielew.locomole");
        mMap.addMarker(new MarkerOptions()
                        .position(HotspotLatLng.get(HotspotLatLng.size() - 1))
                        .title("End Location")
                        .icon(BitmapDescriptorFactory.fromResource(endPos))
                        .alpha(0.4f)

        );




        for (int i = 1; i < HotspotLatLng.size() - 1; i++) {
            int markerPos = r.getIdentifier("map_marker_red_" + (i + 1), "drawable", "com.example.winnielew.locomole");
            newMarker = mMap.addMarker(new MarkerOptions()
                            .position(HotspotLatLng.get(i))
                            .title("Waypoint " + i)
                            .icon(BitmapDescriptorFactory.fromResource(markerPos))
                            .alpha(0.4f)
            );
            MarkerList.add(newMarker);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HotspotLatLng.get(0), 14f));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                for (int i = 0; i < HotspotLatLng.size(); i++) {
                    CameraPosition cp = HotspotCamera.get(i);
                    if (cp != null && cp.target != null
                            && cp.target.latitude == marker.getPosition().latitude
                            && cp.target.longitude == marker.getPosition().longitude) {
                        marker.setAlpha(1.0f);
                        marker.showInfoWindow();
                        for (int n = 0; n < MarkerList.size(); n++) {
                            if (n == i) {
                                continue;
                            }
                            MarkerList.get(n).setAlpha(0.4f);
                        }

                        return true;
                    }
                }
                return false;
            }
        });
        getDirections();
    }


    private void getDirections() {
        try {

//            for (int i = 0; i < locations.length-1; i++) {
            new DirectionFinder(this, HotspotLatLng.get(0).toString(), HotspotLatLng.get(HotspotLatLng.size() - 1).toString(), HotspotLatLng).execute();
            //}
//            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void onDirectionFinderStart() {

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    public void onDirectionFinderSuccess(List<Route> routes) {
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 14f));
            Log.i("test", route.distance + " " + route.duration + "");

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions)); //adding the route line
        }

    }
}
