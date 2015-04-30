package com.fatgyft.smartvelov.decoder;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

/**
 * Created by Felipe on 30-Apr-15.
 */
public class PolylineDecoder {

    public static ArrayList<GeoPoint> decodePolyline( String encoded)
    {
        ArrayList<GeoPoint> poly = new ArrayList<GeoPoint>();
        int index = 0;
        int len = encoded.length();
        int lat = 0, lng = 0, ele = 0;
        while (index < len)
        {
            // latitude
            int b, shift = 0, result = 0;
            do
            {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int deltaLatitude = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += deltaLatitude;

            // longitute
            shift = 0;
            result = 0;
            do
            {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int deltaLongitude = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += deltaLongitude;

            poly.add(new GeoPoint((double) lat / 1e5, (double) lng / 1e5));
        }
        return poly;
    }

}
