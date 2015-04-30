package com.fatgyft.smartvelov.path;

/**
 * Created by Felipe on 29-Apr-15.
 */
import android.util.Pair;

import com.fatgyft.smartvelov.decoder.PolylineDecoder;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class Path {

    private Double distance;
    private Double time ;
    private Boolean points_encoded;
    private Double weight ;
    private ArrayList<Instruction> instructions;
    private Pair<GeoPoint,GeoPoint> bbox;
    private ArrayList<GeoPoint> points;
    private PolylineDecoder polylineDecoder;


    public Path(Double distance, Double time, Boolean points_encoded, Double weight, ArrayList<Instruction> instructions, ArrayList<Double> bbox, String points) {
        this.distance = distance;
        this.time = time;
        this.points_encoded = points_encoded;
        this.weight = weight;
        this.instructions = instructions;

        if (bbox.size()==4) {
            this.bbox = new Pair<GeoPoint,GeoPoint>(new GeoPoint(bbox.get(0),bbox.get(1)), new GeoPoint(bbox.get(2),bbox.get(3)));
        }

        polylineDecoder = new PolylineDecoder();

        this.points= polylineDecoder.decodePolyline(points);
        for(GeoPoint p : this.points){
            System.out.println(p.getLatitude()+"  "+p.getLongitude());
        }

    }

    @Override
    public String toString() {
        return "Path{" +
                "distance=" + distance +
                ", time=" + time +
                ", points_encoded='" + points_encoded + '\'' +
                ", weight=" + weight +
                ", instructions=" + instructions +
                ", bbox=" + bbox +
                ", points='" + points + '\'' +
                '}';
    }


    public Boolean getPoints_encoded() {
        return points_encoded;
    }

    public ArrayList<GeoPoint> getPoints() {
        return points;
    }
}
