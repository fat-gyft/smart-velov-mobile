package com.fatgyft.smartvelov.decoder;

import android.util.Pair;

import com.fatgyft.smartvelov.VeloVStation;
import com.fatgyft.smartvelov.path.Instruction;
import com.fatgyft.smartvelov.path.Path;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Felipe on 28-Apr-15.
 */
public class JSONParser {

    //VeloV Station Static Constants
    private static final String VELOV_POST_NUMBER = "number";
    private static final String VELOV_POST_NAME = "name";
    private static final String VELOV_POST_ADDRESS = "address";
    private static final String VELOV_POST_LATITUDE = "latitude";
    private static final String VELOV_POST_LONGITUDE = "longitude";

    //VeloV Station Dynamic Constants
    private static final String VELOV_DYNAMIC_STATUS = "status";
    private static final String VELOV_DYNAMIC_BIKE_STANDS = "bike_stands";
    private static final String VELOV_DYNAMIC_AVAILABLE_BIKE_STANDS = "available_bike_stands";
    private static final String VELOV_DYNAMIC_AVAILABLE_BIKES = "available_bikes";


    //Path Constants
    private static final String PATHS = "paths";
    private static final String PATH_DISTANCE = "distance";
    private static final String PATH_TIME = "time";
    private static final String PATH_POINTS_ENCODED = "points_encoded";
    private static final String PATH_WEIGHT = "weight";
    private static final String PATH_INSTRUCTIONS = "instructions";
    private static final String PATH_BBOX = "bbox";
    private static final String PATH_POINTS = "points";

    //Instruction Constants
    private static final String INSTRUCTION_SIGN = "sign";
    private static final String INSTRUCTION_TURN_ANGLE = "turn_angle";
    private static final String INSTRUCTION_INTERVAL = "interval";


    //_______________________________Public Methods_______________________________

    /**
     * Method responsible to parse the JSONArray that contains all the static information from the velov stations, that is saved locally
     *
     * @param velovStations JSONArray with velov stations info
     * @return ArrayList<VeloVStation> ArrayList with all the velov stations
     */
    public ArrayList<VeloVStation> parseVELOVPostes(JSONArray velovStations){

        ArrayList<VeloVStation> veloVStationsList = new ArrayList<VeloVStation>();

        try {

            System.out.println(velovStations);

            // looping through All velov stations
            for(int i = 0; i < velovStations.length(); i++) {

                JSONObject n = velovStations.getJSONObject(i);

                Integer number = n.getInt(VELOV_POST_NUMBER);
                String name = n.getString(VELOV_POST_NAME);
                String address = n.getString(VELOV_POST_ADDRESS);
                Double latitude = n.getDouble(VELOV_POST_LATITUDE);
                Double longitude = n.getDouble(VELOV_POST_LONGITUDE);

                veloVStationsList.add(new VeloVStation(number,name,address,latitude,longitude));

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return veloVStationsList;
    }

    /**
     *Method responsible to parse the JSONObject that contains all the information to go from a destination to another
     *
     * @param pathsObject JSONObject of the list of available paths
     * @return ArrayList<Path> with the available paths
     */
    public ArrayList<Path> parsePath(JSONObject pathsObject){



        ArrayList<Path> pathList = new ArrayList<Path>();

        try {
            // looping through All paths
            JSONArray paths = pathsObject.getJSONArray(PATHS);
            for(int i = 0; i < paths.length(); i++) {

                JSONObject n = paths.getJSONObject(i);
                JSONArray instructionList = n.getJSONArray(PATH_INSTRUCTIONS);

                Double distance = n.getDouble(PATH_DISTANCE);
                Double time = n.getDouble(PATH_TIME);
                Boolean points_encoded = n.getBoolean(PATH_POINTS_ENCODED);
                Double weight = n.getDouble(PATH_WEIGHT);
                JSONArray bbox = n.getJSONArray(PATH_BBOX);
                String points = n.getString(PATH_POINTS);

                Path path = new Path(distance,time, points_encoded,weight,parseInstruction(instructionList), parsePathBbox(bbox), points);
                System.out.println(path);
                pathList.add(path);


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return pathList;
    }

    /**
     * Method responsible to parse the JSONObject that contains all the dynamic information from one velov station, that is on JCDECAUX server
     *
     * @param velovStationDynamicInfo JSONArray with velov dynamic info
     * @param veloVStation velov station that needs the information updated
     * @return updated velov station
     */
    public VeloVStation parseVELOVDynamicInfo(JSONObject velovStationDynamicInfo, VeloVStation veloVStation){

        try {

            System.out.println(velovStationDynamicInfo);

            Integer number = velovStationDynamicInfo.getInt(VELOV_POST_NUMBER);
            String status = velovStationDynamicInfo.getString(VELOV_DYNAMIC_STATUS);
            Integer bike_stands= velovStationDynamicInfo.getInt(VELOV_DYNAMIC_BIKE_STANDS);
            Integer available_bike_stands= velovStationDynamicInfo.getInt(VELOV_DYNAMIC_AVAILABLE_BIKE_STANDS);
            Integer available_bikes= velovStationDynamicInfo.getInt(VELOV_DYNAMIC_AVAILABLE_BIKES);

            if (number.equals(veloVStation.getNumber())) {
                veloVStation.updateDynamicInfo(status,bike_stands,available_bike_stands,available_bikes);
            }else{
                System.err.println("Error in dynamic info recovery");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return veloVStation;
    }


    //_______________________________Private Methods_______________________________

    private ArrayList<Instruction> parseInstruction(JSONArray instructions){

        ArrayList<Instruction> instructionList = new ArrayList<Instruction>();

        try {
            // looping through All paths
            for(int i = 0; i < instructions.length(); i++) {

                JSONObject n = instructions.getJSONObject(i);
                Integer sign = n.getInt(INSTRUCTION_SIGN);
                JSONArray JSONInterval = n.getJSONArray(INSTRUCTION_INTERVAL);
                ArrayList<Integer> intervalPoints = parseInstructionInterval(JSONInterval);
                if (intervalPoints.size()==2) {

                    Pair<Integer, Integer> interval = new Pair<Integer, Integer>(intervalPoints.get(0),intervalPoints.get(1));

                    Instruction instruction = new Instruction(sign, interval);

                    instructionList.add(instruction);

                    System.out.println(instruction);

                }else{
                    System.err.println("Wrong interval on JSON");
                }


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return instructionList;
    }

    private ArrayList<Integer> parseInstructionInterval(JSONArray interval){

        ArrayList<Integer> intervalList = new ArrayList<Integer>();

        try {
            // looping through All paths
            for(int i = 0; i < interval.length(); i++) {

                intervalList.add(interval.getInt(i));

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return intervalList;
    }


    private ArrayList<Double> parsePathBbox(JSONArray bbox){

        ArrayList<Double> bboxList = new ArrayList<Double>();

        try {
            // looping through All paths
            for(int i = 0; i < bbox.length(); i++) {

                bboxList.add(bbox.getDouble(i));

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return bboxList;
    }

}

