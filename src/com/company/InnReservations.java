package com.company;

import java.sql.*;
import java.util.Scanner;




public class InnReservations {

    public static void main(String[] args) {
	// write your code here
        Scanner reader = new Scanner(System.in);
        int selection = 0;
        while (selection != -1)
        {
            System.out.println("Inn Reservation Manager: ");
            System.out.println("1: Rooms and Rates");
            System.out.println("2: Make a Reservation");
            System.out.println("3: Change Reservation");
            System.out.println("4: Cancel Reservation");
            System.out.println("5: About Reservation");
            System.out.println("6: Inn Revenue");
            System.out.println("7: Exit");
            System.out.print("Please Choose an Option: ");

            selection = reader.nextInt();

            switch (selection)
            {
                case 1:
                    System.out.println("Rooms and Rates");
                    requirement1();
                    break;
                case 2:
                    System.out.println("Make a Reservation");
                    break;
                case 3:
                    System.out.println("Change Reservation");
                    break;
                case 4:
                    System.out.println("Cancel Reservation");
                    break;
                case 5:
                    System.out.println("About Reservation");
                    break;
                case 6:
                    System.out.println("Inn Revenue");
                    break;
                case 7:
                    System.out.println("Thank you! Come Back Again");
                    selection = -1;
                    break;
            }
        }
    }

    /*
    (Connection conn = DriverManager.getConnection("jdbc:mysql://db.labthreesixfive.com/maichour?autoReconnect=true\\&useSSL=false",
                "maichour",
               "CSC365-F2019_014396846"))


    try(Connection conn = DriverManager.getConnection(System.getenv("APP_JDBC_URL"),
                                                      System.getenv("APP_JDBC_USER"),
                                                      System.getenv("APP_JDBC_PW")))
     */

    private static void requirement1() {
        String sql = "select * from rooms";
        System.out.println();
        System.out.println(System.getenv("APP_JDBC_URL"));
        System.out.println(System.getenv("APP_JDBC_USER"));
        System.out.println(System.getenv("APP_JDBC_PW"));
        System.out.println();

        System.out.println("RoomCode\tRoomName\tBeds\tbedType\tMaxOccup\tbasePrice\tdecor\t");



        try(Connection conn = DriverManager.getConnection(System.getenv("APP_JDBC_URL"),
                System.getenv("APP_JDBC_USER"),
                System.getenv("APP_JDBC_PW"))) {

            try(Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)){
                while (rs.next()){
                    String RoomCode = rs.getString("RoomCode");
                    String RoomName = rs.getString("RoomName");
                    int roomBeds = rs.getInt("Beds");
                    String BedType = rs.getString("bedType");
                    int MaxOccu = rs.getInt("maxOcc");
                    float basePrice = rs.getFloat("basePrice");
                    String Decor = rs.getString("decor");

                    Rooms room = new Rooms(RoomCode,RoomName,roomBeds,BedType,MaxOccu,basePrice,Decor);
                    System.out.printf("%s\t%s\t%d\t%s\t%d\t%.2f\t%s",
                            room.getRoomCode(),
                            room.getRoomName(),
                            room.getBeds(),
                            room.getBedType(),
                            room.getMaxOcc(),
                            room.getBasePrice(),
                            room.getDecor());
                }
            }catch (SQLException e){
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
