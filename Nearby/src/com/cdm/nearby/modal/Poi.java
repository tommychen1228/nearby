package com.cdm.nearby.modal;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: cdm
 * Date: 2/15/13
 * Time: 12:31 AM
 */
public class Poi implements Serializable{
    private String name;
    private String address;
    private double longitude;
    private double latitude;
    private int distance;
    private String phone;

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
