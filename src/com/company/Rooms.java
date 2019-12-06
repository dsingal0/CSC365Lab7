package com.company;

import java.sql.*;

public class Rooms {
    private String RoomCode;
    private String RoomName;
    private int Beds;
    private String BedType;
    private int MaxOcc;
    private float basePrice;
    private String decor;
    private float popularity;

    public Rooms(String roomCode) {
        this.RoomCode = roomCode;
        this.RoomName = "";
        this.Beds = 0;
        this.BedType = "";
        this.MaxOcc = 0;
        this.basePrice = 0;
        this.decor = "";
        this.popularity = 0;
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
        this.popularity = 0;
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

    public float getPopularity(){
        if(this.popularity != 0)
        {
            return this.popularity;
        }
        else {
            String sql = "select roomName, (Sum(DATEDIFF(checkout,checkin))/180) as popular" +
                    "from rooms join reservations res on rooms.RoomCode = res.room" +
                    "where DATEDIFF(CURDATE(),checkin)<180 and roomName = " +this.RoomName+
                    "GROUP BY roomName;";

            try (Connection conn = DriverManager.getConnection(System.getenv("APP_JDBC_URL"),
                    System.getenv("APP_JDBC_USER"),
                    System.getenv("APP_JDBC_PW"))) {

                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {

                        this.popularity=rs.getFloat("popular");

                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    return 0;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return 0;
            }
        }
        return this.popularity;
    }
}
