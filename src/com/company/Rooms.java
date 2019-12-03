package com.company;

public class Rooms {
    public String RoomCode;
    public String RoomName;
    public int Beds;
    public String BedType;
    public int MaxOcc;
    public float basePrice;
    public String decor;

    public Rooms(String roomCode) {
        this.RoomCode = roomCode;
        this.RoomName = "";
        this.Beds = 0;
        this.BedType = "";
        this.MaxOcc = 0;
        this.basePrice = 0;
        this.decor = "";
    }

    public Rooms(String roomCode, String roomName, int roomBeds, String bedType, int maxOccu,
                 float basePrice, String decor) {
        this.RoomCode = roomCode;
        this.RoomName = roomName;
        this.Beds = roomBeds;
        this.BedType = bedType;
        this.MaxOcc = maxOccu;
        this.basePrice = basePrice;
        this.decor = decor;
    }

    public String getRoomCode() {
        return RoomCode;
    }

    public String getRoomName() {
        return RoomName;
    }

    public int getBeds() {
        return Beds;
    }

    public String getBedType() {
        return BedType;
    }

    public int getMaxOcc() {
        return MaxOcc;
    }

    public float getBasePrice() {
        return basePrice;
    }

    public String getDecor() {
        return decor;
    }
}
