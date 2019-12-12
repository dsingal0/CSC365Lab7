package com.company;
import java.sql.*;


public class Reservation {
    public int code;
    public String roomCode;
    public String roomName;
    public String checkIn;
    public String checkOut;
    public float rate;
    public String lastName;
    public String firstName;
    public int adult;
    public int kids;
    public String bed;

    public Reservation() {
        this.code = -1;
        this.roomCode = "";
        this.roomName = "";
        this.checkIn = "";
        this.checkOut = "";
        this.rate = -1;
        this.lastName = "";
        this.firstName = "";
        this.adult = 0;
        this.kids = 0;
        this.bed = "";
    }

    public Reservation(int resCode,
                       String roomCode,
                       String roomName,
                       float rate,
                       String checkIn,
                       String checkOut,
                       String lastName,
                       String firstName,
                       int numAdults,
                       int numKids,
                       String bedType ) {

        this.code = resCode;
        this.roomCode = roomCode;
        this.roomName = roomName;
        this.rate = rate;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.lastName = lastName;
        this.firstName = firstName;
        this.adult = numAdults;
        this.kids = numKids;
        this.bed = bedType;
    }

    public int getCode() {
        return code;
    }

    public String getRoomCode() {
        return roomCode;
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

    public String getBed() {
        return bed;
    }
}
