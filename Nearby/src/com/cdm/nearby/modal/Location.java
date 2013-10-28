package com.cdm.nearby.modal;

/**
 * Created with IntelliJ IDEA.
 * User: cdm
 * Date: 2/14/13
 * Time: 11:51 PM
 */
public class Location {
    private String address;
    private String city;
    private double longitude;
    private double latitude;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
