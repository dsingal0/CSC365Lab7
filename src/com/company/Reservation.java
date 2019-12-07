package com.company;
import java.sql.*;


public class Reservation {
    public int code;
    public String room;
    public String checkIn;
    public String checkOut;
    public float rate;
    public String lastName;
    public String firstName;
    public int adult;
    public int kids;
    public float coast;
    public String bed;


    public Reservation() {
        this.code = -1;
        this.room = "";
        this.checkIn = "";
        this.checkOut = "";
        this.rate = -1;
        this.lastName = "";
        this.firstName = "";
        this.adult = 0;
        this.kids = 0;
        this.coast = -1;
        this.bed = "";
    }

    public int getCode() {
        return code;
    }

    public String getRoom() {
        return room;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public String getCheckOut() {
        return checkOut;
    }

    public float getRate() {
        return rate;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public int getAdult() {
        return adult;
    }

    public int getKids() {
        return kids;
    }

    public float getCoast() {
        return coast;
    }

    public String getBed() {
        return bed;
    }
}
