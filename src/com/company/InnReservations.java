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
                    requirement2();
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
        String sqlStatement = "WITH SixMonthOverlap\n" +
                "    AS (SELECT code,\n" +
                "               room,\n" +
                "               rate,\n" +
                "               checkin,\n" +
                "               checkout,\n" +
                "               Least(Datediff(checkout, checkin),\n" +
                "               Datediff(checkout, Date_sub(curdate(),\n" +
                "                                  interval 180 day)),\n" +
                "               Datediff(curdate(), checkin),\n" +
                "               Datediff(curdate(), Date_sub(curdate(),\n" +
                "                                      interval 180 day))) AS\n" +
                "               overlap\n" +
                "        FROM   lab7_reservations res\n" +
                "        WHERE  checkin <= curdate()\n" +
                "               AND checkout >= Date_sub(curdate(), interval 180 day)),\n" +
                "    RoomPopularity\n" +
                "    AS (SELECT room,\n" +
                "               Round(SUM(overlap) / 180, 2) AS popularity\n" +
                "        FROM   SixMonthOverlap s\n" +
                "        GROUP  BY room),\n" +
                "    RoomAvailability\n" +
                "    AS (SELECT room,\n" +
                "               Greatest(curdate(), Max(checkout)) AS nextAvailableCheckin\n" +
                "        FROM   lab7_reservations res\n" +
                "        GROUP  BY room),\n" +
                "    LatestDuration\n" +
                "    AS (SELECT room,\n" +
                "               Max(checkout) as lastCheckout,\n" +
                "               Datediff(Max(checkout), Max(checkin)) AS latestDuration\n" +
                "        FROM   lab7_reservations res\n" +
                "        GROUP  BY room)\n" +
                "SELECT roomCode,\n" +
                "      roomName,\n" +
                "      beds,\n" +
                "      bedType,\n" +
                "      maxOcc,\n" +
                "      basePrice,\n" +
                "      decor,\n" +
                "      IFNULL(popularity, 0) as popularity,\n" +
                "      nextAvailableCheckin,\n" +
                "      latestDuration as lastStayLength,\n" +
                "      lastCheckout as lastCheckout\n" +
                "FROM  lab7_rooms rooms\n" +
                "      left outer join RoomPopularity rp\n" +
                "              ON rp.room = rooms.roomCode\n" +
                "      left outer join RoomAvailability ra\n" +
                "              ON rooms.roomCode = ra.room\n" +
                "      left outer join LatestDuration ld\n" +
                "              ON ld.room = rooms.roomCode\n" +
                "ORDER  BY popularity DESC;";

        System.out.printf("RoomCode\t%-25sBeds\tBedType\tMaxOcc\tPrice\t%-15sPopularity\tNextCheckin\tLastStayLength\tLastCheckout\n",
                "RoomName", "Decor");

        try(Connection conn = DriverManager.getConnection(System.getenv("APP_JDBC_URL"),
                System.getenv("APP_JDBC_USER"),
                System.getenv("APP_JDBC_PW"))) {

            try(Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sqlStatement)){
                while (rs.next()){
                    String roomCode = rs.getString("roomCode");
                    String RoomName = rs.getString("roomName");
                    int roomBeds = rs.getInt("beds");
                    String bedType = rs.getString("bedType");
                    int maxOccupancy = rs.getInt("maxOcc");
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
    public static void requirement2(){
        System.out.println("Make a Reservation: ");

        Reservation res = getRequirement2Inputs();

        if (res == null) {
            return;
        }

        /*
        -- find rooms by totalCapacity
                -- replace 2 with number of adults and
                -- 1 with number of children
        -- room, bed type, beginDate, endDate
                -- when passing in the roomcode and decor, pass them as '%' if Any
                -- replace '2010-10-23' with startDate and '2010-10-25' with endDate
        -- for the insert statement to add a reservation, have the new reservation code be maxResCode, which is the last column of the returned table from the query +1. Therefore,
                -- rescode of new reservation = maxRescode+1*/

        try(Connection conn = DriverManager.getConnection(System.getenv("APP_JDBC_URL"),
                System.getenv("APP_JDBC_USER"),
                System.getenv("APP_JDBC_PW"))) {

            // First is num adults
            // Second is num children
            // Third is roomcode (pass as a string or '%' for any)
            // Fourth is decor (pass as a string or '%' for any)
            // Fifth is startDate
            // Sixth is endDate
            // enddate
            // startdate
            // startdate
            // enddate
            // enddate
            // startdate
            // startdate
            // enddate

            PreparedStatement statement = conn.prepareStatement("WITH bigenoughrooms AS ( \n" +
                    "( \n" +
                    "       SELECT roomcode, \n" +
                    "              roomname, \n" +
                    "              baseprice, \n" +
                    "              decor, \n" +
                    "              bedtype \n" +
                    "       FROM   rooms r \n" +
                    "       WHERE  r.maxocc >= (? + ?) ) ), decorcoderooms AS \n" +
                    "( \n" +
                    "       SELECT roomcode \n" +
                    "       FROM   bigenoughrooms \n" +
                    "       WHERE  roomcode LIKE ? \n" +
                    "       OR     decor LIKE ? ), alloverlap AS \n" +
                    "( \n" +
                    "           SELECT     roomcode, \n" +
                    "                      roomname, \n" +
                    "                      checkin, \n" +
                    "                      checkout, \n" +
                    "                      baseprice, \n" +
                    "                      Least( Datediff(checkout, checkin), Datediff(checkout, ?), Datediff(?, checkin), Datediff(?, ?) ) AS overlap \n" +
                    "           FROM       bigenoughrooms b \n" +
                    "           INNER JOIN reservations res \n" +
                    "           ON         res.room = b.roomcode ), roomoverlap AS \n" +
                    "( \n" +
                    "           SELECT     roomcode, \n" +
                    "                      roomname, \n" +
                    "                      baseprice, \n" +
                    "                      Max( Least( Datediff(checkout, checkin), Datediff(checkout, ?), Datediff(?, checkin), Datediff(?, ?) ) ) AS overlap \n" +
                    "           FROM       bigenoughrooms b \n" +
                    "           INNER JOIN reservations res \n" +
                    "           ON         res.room = b.roomcode \n" +
                    "           GROUP BY   roomcode ), idealrooms AS \n" +
                    "( \n" +
                    "           SELECT     r.roomcode, \n" +
                    "                      r.roomname, \n" +
                    "                      r.baseprice, \n" +
                    "                      ? AS startdate, \n" +
                    "                      ? AS enddate \n" +
                    "           FROM       decorcoderooms d \n" +
                    "           INNER JOIN roomoverlap r \n" +
                    "           ON         r.roomcode = d.roomcode \n" +
                    "           WHERE      overlap <= 0 ), nonidealroomsnooverlap AS \n" +
                    "( \n" +
                    "           SELECT     r.roomcode, \n" +
                    "                      r.roomname, \n" +
                    "                      r.baseprice, \n" +
                    "                      '2010-10-23' AS startdate, \n" +
                    "                      '2010-10-25' AS enddate \n" +
                    "           FROM       bigenoughrooms b \n" +
                    "           INNER JOIN roomoverlap r \n" +
                    "           ON         r.roomcode = b.roomcode \n" +
                    "           WHERE      overlap <= 0 ) \n" +
                    "( \n" +
                    "       SELECT *, \n" +
                    "              ( \n" +
                    "                     SELECT Max(code) \n" +
                    "                     FROM   reservations ) AS maxrescode \n" +
                    "       FROM   idealrooms ) \n" +
                    "UNION ALL \n" +
                    "          ( \n" +
                    "                 SELECT *, \n" +
                    "                        ( \n" +
                    "                               SELECT Max(code) \n" +
                    "                               FROM   reservations ) AS maxrescode \n" +
                    "                 FROM   nonidealroomsnooverlap ) \n" +
                    "   UNION ALL \n" +
                    "             ( \n" +
                    "                        SELECT     roomcode, \n" +
                    "                                   roomname, \n" +
                    "                                   baseprice, \n" +
                    "                                   Max(checkout)                                                                AS startdate,\n" +
                    "                                   date_add( Max(checkout), interval datediff('2010-10-25', '2010-10-23') day ) AS enddate, \n" +
                    "                                   ( \n" +
                    "                                          SELECT max(code) \n" +
                    "                                          FROM   reservations ) AS maxrescode \n" +
                    "                        FROM       bigenoughrooms b \n" +
                    "                        INNER JOIN reservations r \n" +
                    "                        ON         r.room = b.roomcode \n" +
                    "                        GROUP BY   roomcode ) limit 5;");

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static Reservation getRequirement2Inputs() {
        Reservation res = new Reservation();

        Scanner reader = new Scanner (System.in);

        while(true){
            int input = reservationEditor(res);

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

    public static int reservationEditor(Reservation res){
        int input = 0;
        Scanner reader = new Scanner (System.in);

        System.out.println("Choose a field to edit:");
        System.out.printf("\t1: First Name: %s\n", res.getFirstName());
        System.out.printf("\t2: Last Name: %s\n", res.getLastName());
        System.out.printf("\t3: Desired Room: %s\n", res.room);
        System.out.printf("\t4: Desired Bed: %s\n", res.bed);

        if(res.getCheckIn().equals("")){
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
