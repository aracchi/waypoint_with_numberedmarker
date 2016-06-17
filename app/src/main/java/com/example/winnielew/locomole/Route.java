package com.example.winnielew.locomole;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import javax.xml.datatype.Duration;

/**
 * Created by Winnie Lew on 6/8/2016.
 */
public class Route {

    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;
    public int distance;
    public int duration;

    public List<LatLng> points;
}
