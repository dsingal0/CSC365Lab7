package com.company;

import java.sql.*;
import java.util.Scanner;




public class InnReservations {

    private static String date;

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
                    Requirement2();
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

    private static void requirement1() {
        String sqlStatement =
                "WITH ThreeMonthOverlap\n" +
                "    AS (SELECT code,\n" +
                "               room,\n" +
                "               rate,\n" +
                "               checkin,\n" +
                "               checkout,\n" +
                "               Least(Datediff(checkout, checkin),\n" +
                "               Datediff(checkout, Date_sub('2010-10-23',\n" +
                "                                  interval 180 day)),\n" +
                "               Datediff('2010-10-23', checkin),\n" +
                "               Datediff('2010-10-23', Date_sub('2010-10-23',\n" +
                "                                      interval 180 day))) AS\n" +
                "               overlap\n" +
                "        FROM   reservations res\n" +
                "        WHERE  checkin <= '2010-10-23'\n" +
                "               AND checkout >= Date_sub('2010-10-23', interval 180 day)),\n" +
                "    RoomPopularity\n" +
                "    AS (SELECT room,\n" +
                "               Round(SUM(overlap) / 180, 2) AS popularity\n" +
                "        FROM   ThreeMonthOverlap t\n" +
                "        GROUP  BY room),\n" +
                "    RoomAvailability\n" +
                "    AS (SELECT room,\n" +
                "               Greatest('2010-10-23', Max(checkout)) AS nextAvailableCheckin\n" +
                "        FROM   reservations res\n" +
                "        WHERE  checkin <= '2010-10-23'\n" +
                "               AND checkout >= Date_sub('2010-10-23', interval 180 day)\n" +
                "        GROUP  BY room),\n" +
                "    LatestDuration\n" +
                "    AS (SELECT room,\n" +
                "               Max(checkout) as lastCheckout,\n" +
                "               Datediff(Max(checkout), Max(checkin)) AS latestDuration\n" +
                "        FROM   reservations res\n" +
                "        GROUP  BY room)\n" +
                "SELECT roomId,\n" +
                "      roomName,\n" +
                "      beds,\n" +
                "      bedType,\n" +
                "      maxOccupancy,\n" +
                "      basePrice,\n" +
                "      decor,\n" +
                "      popularity,\n" +
                "      nextAvailableCheckin,\n" +
                "      latestDuration as lastStayLength,\n" +
                "      lastCheckout\n" +
                "FROM  RoomPopularity rp\n" +
                "      inner join rooms\n" +
                "              ON rp.room = rooms.roomId\n" +
                "      inner join RoomAvailability ra\n" +
                "              ON rp.room = ra.room\n" +
                "      inner join LatestDuration ld\n" +
                "              ON ld.room = ra.room\n" +
                "ORDER  BY popularity DESC;";

        System.out.printf("RoomId\t%-25sBeds\tBedType\tMaxOcc\tPrice\t%-15sPopularity\tNextCheckin\tLastStayLength\tLastCheckout\n",
                "RoomName", "Decor");

        try(Connection conn = DriverManager.getConnection(System.getenv("APP_JDBC_URL"),
                System.getenv("APP_JDBC_USER"),
                System.getenv("APP_JDBC_PW"))) {

            try(Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sqlStatement)){
                while (rs.next()){
                    String roomCode = rs.getString("roomId");
                    String RoomName = rs.getString("roomName");
                    int roomBeds = rs.getInt("beds");
                    String bedType = rs.getString("bedType");
                    int maxOccupancy = rs.getInt("maxOccupancy");
                    float basePrice = rs.getFloat("basePrice");
                    String decor = rs.getString("decor");
                    float popularity = rs.getFloat("popularity");
                    Date nextDate = rs.getDate("nextAvailableCheckin");
                    int lastStayDuration = rs.getInt("lastStayLength");
                    Date lastCheckout = rs.getDate("lastCheckout");

                    Rooms room = new Rooms(roomCode, RoomName, roomBeds, bedType, maxOccupancy, basePrice, decor);

                    System.out.printf("%s\t%-25s%d\t%s\t%d\t%.2f\t%-15s%.2f\t\t%tF\t%d\t\t%tF\n",
                            room.getRoomCode(),
                            room.getRoomName(),
                            room.getBeds(),
                            room.getBedType(),
                            room.getMaxOcc(),
                            room.getBasePrice(),
                            room.getDecor(),
                            popularity,
                            nextDate,
                            lastStayDuration,
                            lastCheckout);
                }
            }catch (SQLException e){
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Checks the date to confirm YYYY-MM-DD Format
    public static boolean checkDate(String date){
        InnReservations.date = date;
        if(date.indexOf('-') == 4 && date.lastIndexOf('-') == 7){
            String temp[] = date.split("-");
            int year = Integer.parseInt(temp[0]);
            int month = Integer.parseInt(temp[1]);
            int day = Integer.parseInt(temp[2]);
            if(year < 0 || year >  3000 || month < 0 || month > 12 || day < 0 || day > 31){
                return true;
            }
            return false;
        }
        return true;
    }

    /* Requirement 2
     * Reservation Creation
     */
    public static Reservation Requirement2(){
        Reservation res = new Reservation();

        Scanner reader = new Scanner (System.in);
        String userInput;

        System.out.println("Make a Reservation: ");

        while(true){
            int input = Requirement2_helper(res);

            switch(input){
                case 1: //First Name, inputs(0)
                    System.out.print("Enter a First Name: ");
                    res.firstName = reader.nextLine();
                    break;
                case 2: //Last Name, inputs(1)
                    System.out.print("Enter a Last Name: ");
                    res.lastName = reader.nextLine();
                    break;
                case 3: //Desired Room
                    System.out.print("Enter a Desired Room: ");
                    res.room = reader.nextLine();
                    break;
                case 4: //Desired Bed
                    System.out.print("Enter a Desired Bed: ");
                    res.bed = reader.nextLine();
                    break;
                case 5: //Date Range, Start Date = inputs(2), End Date = inputs(3)
                    System.out.print("Enter a Start Date (YYYY-MM-DD): ");
                    res.checkIn = reader.nextLine();

                    System.out.print("Enter a End Date (YYYY-MM-DD): ");
                    res.checkOut = reader.nextLine();

                    if(checkDate(res.checkIn) || checkDate(res.checkOut)){
                        res.checkIn = "";
                        res.checkOut = "";
                        System.out.println("Please enter the date in YYYY-MM-DD Format");
                    }
                    break;
                case 6: //Numebr of Children,inputs(4)
                    System.out.print("Enter the number of Children: ");
                    try {
                        res.kids = reader.nextInt();
                    } catch (Exception InputMismatchException) {
                        System.out.println("Invalid Type");
                    }
                    break;
                case 7: //Number of Adults,inputs(5)
                    System.out.print("Enter the number of Adults: ");
                    try {
                        res.adult = reader.nextInt();
                    } catch (Exception InputMismatchException) {
                        System.out.println("Invalid Type");
                    }
                    break;
                case 8:
                    return res;
                case 9:
                    return null;

            }
        }
    }

    public static int Requirement2_helper(Reservation res){
        int input = 0;
        Scanner reader = new Scanner (System.in);

        System.out.println("Choose a feild to edit:");
        System.out.printf("\t1: First Name: %s\n", res.getFirstName());
        System.out.printf("\t2: Last Name: %s\n", res.getLastName());
        System.out.printf("\t3: Desired Room: %s\n", res.room);
        System.out.printf("\t4: Desired Bed: %s\n", res.bed);

        if(res.getCheckIn() == ""){
            System.out.printf("\t5: Range of Dates: \n");
        }
        else{
            System.out.printf("\t5: Range of Dates: %s - %s\n", res.getCheckIn(), res.getCheckOut());
        }

        System.out.printf("\t6: Number of Children: %d\n", res.getKids());
        System.out.printf("\t7: Number of Adults: %d\n", res.getAdult());
        System.out.println("\t8: Confirm Reservation");
        System.out.println("\t9: Cancel");

        System.out.print("\nChoose which option to enter: ");
        try {
            input = reader.nextInt();
        } catch (Exception InputMismatchException) {
            System.out.println("Invalid Type");
        }

        return input;
    }

}
