package com.fatgyft.smartvelov;

/**
 * Created by Felipe on 28-Apr-15.
 */
public class VeloVStation {

    private  Integer number;
    private  String name;
    private  String address;
    private  Double latitude;
    private  Double longitude;

    public VeloVStation(Integer number, String name, String address, Double latitude, Double longitude) {
        this.number = number;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
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
