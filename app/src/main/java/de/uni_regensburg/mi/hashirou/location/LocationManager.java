package de.uni_regensburg.mi.hashirou.location;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by k3k5 on 28.07.17.
 *
 */

public class LocationManager {

    double altitude = 0.0;
    float distance = 0;

    public int calculatePoints(List altitudes, List locations_lat, List locations_lng) {

        for(int i=1; i<altitudes.size(); i++) {
            double difference = Math.abs((double) altitudes.get(i-1) - (double) altitudes.get(i));
            altitude += difference;
        }

        for(int i=1; i<locations_lat.size(); i++) {
            distance += computeDistance((double) locations_lat.get(i-1), (double) locations_lng.get(i-1), (double) locations_lat.get(i), (double) locations_lng.get(i));
        }

        return (int) (Math.round(altitude) * 2 + Math.round(distance));
    }

    private float computeDistance(double lat_start, double lng_start, double lat_end, double lng_end) {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_end-lat_start);
        double lngDiff = Math.toRadians(lng_end-lng_start);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(lat_start)) * Math.cos(Math.toRadians(lat_end)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return new Float(distance * meterConversion).floatValue();
    }

}
