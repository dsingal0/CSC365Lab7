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

                    System.out.printf("%s\t\t%-25s%d\t%s\t%d\t%.2f\t%-15s%.2f\t\t%tF\t%d\t\t%tF\n",
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
    public static void requirement2() {
        System.out.println("Make a Reservation: ");

        Reservation res = getRequirement2Inputs();

        if (res == null) {
            return;
        }

        try(Connection conn = DriverManager.getConnection(System.getenv("APP_JDBC_URL"),
                System.getenv("APP_JDBC_USER"),
                System.getenv("APP_JDBC_PW"))) {

            PreparedStatement statement = conn.prepareStatement(
                    "WITH bigenoughrooms AS (\n" +
                            "  (\n" +
                            "    SELECT \n" +
                            "      roomcode, \n" +
                            "      roomname, \n" +
                            "      baseprice, \n" +
                            "      decor, \n" +
                            "      bedtype \n" +
                            "    FROM \n" +
                            "      lab7_rooms r \n" +
                            "    WHERE \n" +
                            "      r.maxocc >= (? + ?)\n" +
                            "  )\n" +
                            "), \n" +
                            "decorcoderooms AS (\n" +
                            "  SELECT \n" +
                            "    roomcode \n" +
                            "  FROM \n" +
                            "    bigenoughrooms \n" +
                            "  WHERE \n" +
                            "    roomcode LIKE '?' \n" +
                            "    OR bedtype LIKE '?' \n" +
                            "), \n" +
                            "alloverlap AS (\n" +
                            "  SELECT \n" +
                            "    roomcode, \n" +
                            "    roomname, \n" +
                            "    checkin, \n" +
                            "    checkout, \n" +
                            "    baseprice, \n" +
                            "    Least(\n" +
                            "      Datediff(checkout, checkin), \n" +
                            "      Datediff(checkout, ?), \n" +
                            "      Datediff(?, checkin), \n" +
                            "      Datediff(?, ?)\n" +
                            "    ) AS overlap \n" +
                            "  FROM \n" +
                            "    bigenoughrooms b \n" +
                            "    INNER JOIN lab7_reservations res ON res.room = b.roomcode\n" +
                            "), \n" +
                            "roomoverlap AS (\n" +
                            "  SELECT \n" +
                            "    roomcode, \n" +
                            "    roomname, \n" +
                            "    baseprice, \n" +
                            "    Max(\n" +
                            "      Least(\n" +
                            "        Datediff(checkout, checkin), \n" +
                            "        Datediff(checkout, ?), \n" +
                            "        Datediff(?, checkin), \n" +
                            "        Datediff(?, ?)\n" +
                            "      )\n" +
                            "    ) AS overlap \n" +
                            "  FROM \n" +
                            "    bigenoughrooms b \n" +
                            "    INNER JOIN lab7_reservations res ON res.room = b.roomcode \n" +
                            "  GROUP BY \n" +
                            "    roomcode\n" +
                            "), \n" +
                            "idealrooms AS (\n" +
                            "  SELECT \n" +
                            "    r.roomcode, \n" +
                            "    r.roomname, \n" +
                            "    r.baseprice, \n" +
                            "    ? AS startdate, \n" +
                            "    ? AS enddate \n" +
                            "  FROM \n" +
                            "    decorcoderooms d \n" +
                            "    INNER JOIN roomoverlap r ON r.roomcode = d.roomcode \n" +
                            "  WHERE \n" +
                            "    overlap <= 0\n" +
                            "), \n" +
                            "nonidealroomsnooverlap AS (\n" +
                            "  SELECT \n" +
                            "    r.roomcode, \n" +
                            "    r.roomname, \n" +
                            "    r.baseprice, \n" +
                            "    ? AS startdate, \n" +
                            "    ? AS enddate \n" +
                            "  FROM \n" +
                            "    bigenoughrooms b \n" +
                            "    INNER JOIN roomoverlap r ON r.roomcode = b.roomcode \n" +
                            "  WHERE \n" +
                            "    overlap <= 0\n" +
                            ") (\n" +
                            "  SELECT \n" +
                            "    *, \n" +
                            "    (\n" +
                            "      select \n" +
                            "        max(code) \n" +
                            "      from \n" +
                            "        lab7_reservations\n" +
                            "    ) as maxResCode \n" +
                            "  FROM \n" +
                            "    idealrooms\n" +
                            ") \n" +
                            "UNION ALL \n" +
                            "  (\n" +
                            "    SELECT \n" +
                            "      *, \n" +
                            "      (\n" +
                            "        select \n" +
                            "          max(code) \n" +
                            "        from \n" +
                            "          lab7_reservations\n" +
                            "      ) as maxResCode \n" +
                            "    FROM \n" +
                            "      nonidealroomsnooverlap\n" +
                            "  ) \n" +
                            "UNION ALL \n" +
                            "  (\n" +
                            "    SELECT \n" +
                            "      roomcode, \n" +
                            "      roomname, \n" +
                            "      baseprice, \n" +
                            "      Max(checkout) AS startdate, \n" +
                            "      date_add(\n" +
                            "        Max(checkout), \n" +
                            "        interval datediff(?, ?) day\n" +
                            "      ) AS enddate, \n" +
                            "      (\n" +
                            "        select \n" +
                            "          max(code) \n" +
                            "        from \n" +
                            "          lab7_reservations\n" +
                            "      ) as maxResCode \n" +
                            "    FROM \n" +
                            "      bigenoughrooms b \n" +
                            "      INNER JOIN lab7_reservations r ON r.room = b.roomcode \n" +
                            "    GROUP BY \n" +
                            "      roomcode\n" +
                            "  ) \n" +
                            "limit 5;"
            );
            statement.setInt(1, res.getAdult());
            statement.setInt(2, res.getKids());

            String room = res.getRoom();
            String bedType = res.getBed();
            statement.setString(3, room.isEmpty() ? "%" : room);
            statement.setString(4, bedType.isEmpty() ? "%" : bedType);

            //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            //java.util.Date checkIn = formatter.parse(res.getCheckIn());


            statement.setString(5, res.getCheckIn());
            statement.setString(6, res.getCheckOut());
            statement.setString(7, res.getCheckOut());
            statement.setString(8, res.getCheckIn());
            statement.setString(9, res.getCheckIn());
            statement.setString(10, res.getCheckOut());
            statement.setString(11, res.getCheckOut());
            statement.setString(12, res.getCheckIn());

            statement.setString(13, res.getCheckIn());
            statement.setString(14, res.getCheckOut());

            statement.setString(15, res.getCheckIn());
            statement.setString(16, res.getCheckOut());

            statement.setString(17, res.getCheckOut());
            statement.setString(18, res.getCheckIn());

            ResultSet rs = statement.executeQuery();

            System.out.printf("RoomCode\t%-25sPrice\tStartDate\tEndDate\tMaxResCode\n",
                    "RoomName");

            while(rs.next()) {
                String roomCode = rs.getString("roomCode");
                String roomName = rs.getString("roomName");
                int basePrice = rs.getInt("basePrice");
                Date startDate = rs.getDate("startdate");
                Date endDate = rs.getDate("enddate");
                int maxResCode = rs.getInt("maxRedCode");

                System.out.printf("%s\t%-25s%d\t%tF\t%tF\t%d\n",
                        roomCode,
                        roomName,
                        basePrice,
                        startDate,
                        endDate,
                        maxResCode);
            }

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
