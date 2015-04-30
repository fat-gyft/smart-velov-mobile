package com.fatgyft.smartvelov;

/**
 * Created by Felipe on 28-Apr-15.
 */
public class VeloVStation {

    //STATIC
    private Integer number;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private Boolean banking;
    private Boolean bonus;

    //DYNAMIC
    private String status;
    private Integer bike_stands;
    private Integer available_bike_stands;
    private Integer available_bikes;


    public VeloVStation(Integer number, String name, String address, Double latitude, Double longitude) {
        this.number = number;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public void updateDynamicInfo(String status, Integer bike_stands, Integer available_bike_stands, Integer available_bikes){
        this.status=status;
        this.bike_stands=bike_stands;
        this.available_bike_stands=available_bike_stands;
        this.available_bikes=available_bikes;
    }


    public Integer getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
