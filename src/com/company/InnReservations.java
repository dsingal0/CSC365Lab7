package com.company;

import java.util.Date;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

public class InnReservations {

    private static String date;

    public static void main(String[] args) {
	// write your code here
        Scanner reader = new Scanner(System.in);
        int selection = 0;
        while (selection != -1)
        {
            System.out.println("\n\nInn Reservation Manager: ");
            System.out.println("1: Rooms and Rates");
            System.out.println("2: Make a Reservation");
            System.out.println("3: Change Reservation");
            System.out.println("4: Cancel Reservation");
            System.out.println("5: About Reservation");
            System.out.println("6: Inn Revenue");
            System.out.println("7: Exit");
            System.out.print("Please Choose an Option: ");

            String sql = "";
            Reservation reservation;
            selection = reader.nextInt();

            // Skip the newline
            reader.nextLine();

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
                    reservation = requirement3();

                    if (execRequirement3(reservation)) {
                        System.out.println("Reservation updated");
                    } else {
                        System.out.println("An error occurred updating the reservation");
                    }

                    break;
                case 4:
                    System.out.println("Cancel Reservation");
                    if (!requirement4()) {
                        System.out.println("An error occurred canceling the reservation");
                    }
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

        Reservation resRequestInfo = getRequirement2Inputs();

        if (resRequestInfo == null) {
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
                            "      beds, \n" +
                            "      bedtype, \n" +
                            "      maxOcc \n" +
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
                            "    roomcode LIKE ? \n" +
                            "    OR bedtype LIKE ? \n" +
                            "), \n" +
                            "alloverlap AS (\n" +
                            "  SELECT \n" +
                            "    roomcode, \n" +
                            "    roomname, \n" +
                            "    checkin, \n" +
                            "    checkout, \n" +
                            "    baseprice, \n" +
                            "    beds, \n" +
                            "    bedtype, \n" +
                            "    maxOcc, \n" +
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
                            "    beds, \n" +
                            "    bedtype, \n" +
                            "    maxOcc, \n" +
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
                            "    r.beds, \n" +
                            "    r.bedtype, \n" +
                            "    r.maxOcc, \n" +
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
                            "    r.beds, \n" +
                            "    r.bedtype, \n" +
                            "    r.maxOcc, \n" +
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
                            "      beds, \n" +
                            "      bedtype, \n" +
                            "      maxocc, \n" +
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
            statement.setInt(1, resRequestInfo.getAdult());
            statement.setInt(2, resRequestInfo.getKids());

            String room = resRequestInfo.getRoomCode();
            String bedType = resRequestInfo.getBed();
            statement.setString(3, room.isEmpty() ? "%" : room);
            statement.setString(4, bedType.isEmpty() ? "%" : bedType);

            //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            //java.util.Date checkIn = formatter.parse(res.getCheckIn());


            statement.setString(5, resRequestInfo.getCheckIn());
            statement.setString(6, resRequestInfo.getCheckOut());
            statement.setString(7, resRequestInfo.getCheckOut());
            statement.setString(8, resRequestInfo.getCheckIn());
            statement.setString(9, resRequestInfo.getCheckIn());
            statement.setString(10, resRequestInfo.getCheckOut());
            statement.setString(11, resRequestInfo.getCheckOut());
            statement.setString(12, resRequestInfo.getCheckIn());

            statement.setString(13, resRequestInfo.getCheckIn());
            statement.setString(14, resRequestInfo.getCheckOut());

            statement.setString(15, resRequestInfo.getCheckIn());
            statement.setString(16, resRequestInfo.getCheckOut());

            statement.setString(17, resRequestInfo.getCheckOut());
            statement.setString(18, resRequestInfo.getCheckIn());

            ResultSet rs = statement.executeQuery();

            System.out.printf("Option\tRoomCode\t%-25sPrice\tBeds\tBedType\tMaxOcc\tStartDate\t\tEndDate\tMaxResCode\n",
                    "RoomName");


            Reservation possibleReservations[] = new Reservation[5];

            int count = 0;

            while(rs.next()) {
                count = count + 1;

                String roomCode = rs.getString("roomCode");
                String roomName = rs.getString("roomName");
                int basePrice = rs.getInt("basePrice");
                int roomBeds = rs.getInt("beds");
                String roomBedType = rs.getString("bedType");
                int maxOccupancy = rs.getInt("maxOcc");
                Date startDate = rs.getDate("startdate");
                Date endDate = rs.getDate("enddate");
                int maxResCode = rs.getInt("maxResCode");

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String checkIn = df.format(startDate);
                String checkOut = df.format(endDate);

                Reservation res = new Reservation(maxResCode+1,
                        roomCode,
                        roomName,
                        basePrice,
                        checkIn,
                        checkOut,
                        resRequestInfo.lastName,
                        resRequestInfo.firstName,
                        resRequestInfo.adult,
                        resRequestInfo.kids,
                        roomBedType);

                possibleReservations[count-1] = res;

                System.out.printf("%d\t%s\t\t%-25s%d\t%d\t%s\t%d\t%tF\t\t%tF\t%d\n",
                        count,
                        roomCode,
                        roomName,
                        basePrice,
                        roomBeds,
                        roomBedType,
                        maxOccupancy,
                        startDate,
                        endDate,
                        maxResCode);
            }

            if (count == 0) {
                System.out.println("no suitable rooms are available");
            } else {
                Scanner reader = new Scanner (System.in);

                int selectedOption;

                boolean canceled = false;

                while (true) {
                    selectedOption = -1;

                    System.out.print("Please select an option from above to reserve, or type 'Cancel' to exit: ");
                    String input = reader.nextLine();

                    if (input.equals("Cancel")) {
                        canceled = true;
                        break;
                    }

                    try {
                        selectedOption = Integer.parseInt(input);
                    } catch (Exception e) {
                        System.out.println("Invalid input provided, enter a reservation number or Cancel to exit");
                    }

                    if (selectedOption >= 1 && selectedOption <= 5) {
                        break;
                    }
                }

                if (!canceled) {
                    Reservation selected = possibleReservations[selectedOption];

                    PreparedStatement insertStatement = conn.prepareStatement(
                    "insert into lab7_reservations(code, " +
                            "room," +
                            "checkin, " +
                            "checkout, " +
                            "rate, " +
                            "lastname, " +
                            "firstname, " +
                            "adults, " +
                            "kids) " +
                        "values (?, ?, ?, ?, ?, ?, ?, ?, ?)");

                    insertStatement.setInt(1, selected.getCode());
                    insertStatement.setString(2, selected.getRoomCode());
                    insertStatement.setString(3, selected.getCheckIn());
                    insertStatement.setString(4, selected.getCheckOut());
                    insertStatement.setFloat(5, selected.getRate());
                    insertStatement.setString(6, selected.getLastName());
                    insertStatement.setString(7, selected.getFirstName());
                    insertStatement.setInt(8, selected.getAdult());
                    insertStatement.setInt(9, selected.getKids());

                    // Execute the insert statement
                    insertStatement.execute();

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

                    Date checkIn = new Date();
                    Date checkOut = new Date();

                    try {
                        checkIn = formatter.parse(selected.getCheckIn());
                        checkOut = formatter.parse(selected.getCheckOut());
                    } catch (Exception e) {
                        // Dont care
                    }

                    double totalPrice =
                            ((numWeekdays(checkIn, checkOut) * selected.getRate()) +
                            (numWeekendDays(checkIn, checkOut) * selected.getRate() * 1.10))
                             * 1.18;

                    System.out.println("\n\nReservation Confirmation");
                    System.out.printf("Name:       \t%s %s\n", selected.getFirstName(), selected.getLastName());
                    System.out.printf("Room Code:  \t%s\n", selected.getRoomCode());
                    System.out.printf("Room Name:  \t%s\n", selected.getRoomName());
                    System.out.printf("Bed Type:   \t%s\n", selected.getBed());
                    System.out.printf("Num Adults: \t%d\n", selected.getAdult());
                    System.out.printf("Num Kids:   \t%d\n", selected.getKids());
                    System.out.printf("Check in:   \t%s\n", selected.getCheckIn());
                    System.out.printf("Check in:   \t%s\n", selected.getCheckOut());
                    System.out.printf("Total cost: \t$%.2f\n\n", totalPrice);

                    conn.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static int numWeekendDays(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);

        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);

        int numWeekendDays = 0;

        while (! c1.after(c2)) {
            if (c1.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ){
                numWeekendDays++;
            }
            if(c1.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
                numWeekendDays++;
            }

            c1.add(Calendar.DATE, 1);
        }
        return numWeekendDays;
    }

    public static int numWeekdays(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);

        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);

        int numWeekdays = 0;

        while (! c1.after(c2)) {
            if (c1.get(Calendar.DAY_OF_WEEK) >= Calendar.MONDAY && c1.get(Calendar.DAY_OF_WEEK) <= Calendar.FRIDAY){
                numWeekdays++;
            }

            c1.add(Calendar.DATE, 1);
        }

        return numWeekdays;
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
                    res.roomCode = reader.nextLine();
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
        System.out.printf("\t3: Desired Room: %s\n", res.getRoomCode());
        System.out.printf("\t4: Desired Bed: %s\n", res.getBed());

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
            // Skip the newline
            reader.nextLine();
        } catch (Exception InputMismatchException) {
            System.out.println("Invalid Type");
        }

        return input;
    }

    //fetch a reservation based on reservation Code
    public static Reservation fetch_res(int code){
        Reservation res = new Reservation();
        String sql = "SELECT * FROM lab7_reservations";
        sql += " WHERE Code = " + code + ";";
        try (Connection conn = DriverManager.getConnection(System.getenv("APP_JDBC_URL"),
                System.getenv("APP_JDBC_USER"),
                System.getenv("APP_JDBC_PW"))) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while(rs.next()){ // Only returns the last reservation in  a list
                    res.code = rs.getInt("CODE");
                    res.roomCode = rs.getString("Room");
                    res.checkIn = rs.getString("CheckIn");
                    res.checkOut = rs.getString("CheckOut");
                    res.firstName = rs.getString("FirstName");
                    res.lastName = rs.getString("LastName");
                    res.adult = rs.getInt("Adults");
                    res.kids = rs.getInt("Kids");
                    res.rate = rs.getFloat("Rate");
//				    System.out.format("%d %s %s %s %s %s %n", res.code, res.room, res.first, res.last, res.checkIn, res.checkOut);
                }
            }
            catch(SQLException e){
                System.err.println("SQLException: " + e.getMessage());
                return null;
            }
        }
        catch(SQLException e){
            System.err.println("SQLException: " + e.getMessage());
            return null;
        }
        return res;
    }
    /* Requirement 3
     * Reservation Change
     */
    public static Reservation requirement3(){
        Reservation res;
        Scanner reader = new Scanner (System.in);

        System.out.print("Please enter your reservation code to edit:");
        int code = reader.nextInt();
        // Skip the newline
        reader.nextLine();

        res = fetch_res(code);
        while(true){
            int input = getRequirement3Inputs(res);
            switch(input){
                case 1: //First Name, inputs(0)
                    System.out.print("Enter a First Name: ");
                    res.firstName = reader.nextLine();
                    break;
                case 2: //Last Name, inputs(1)
                    System.out.print("Enter a Last Name: ");
                    res.lastName = reader.nextLine();
                    break;
                case 3: //Date Range, Start Date = inputs(2), End Date = inputs(3)
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
                case 4: //Numebr of Children,inputs(4)
                    System.out.print("Enter the number of Children: ");
                    try {
                        res.kids = reader.nextInt();
                    } catch (Exception InputMismatchException) {
                        System.out.println("Invalid Type");
                    }
                    break;
                case 5: //Numebr of Adults,inputs(5)
                    System.out.print("Enter the number of Adults: ");
                    try {
                        res.adult = reader.nextInt();
                    } catch (Exception InputMismatchException) {
                        System.out.println("Invalid Type");
                    }
                    break;
                case 6:
                    return res;
                case 7:
                    return null;
            }
        }
    }
    public static int getRequirement3Inputs(Reservation res){
        int input = 0;
        Scanner reader = new Scanner (System.in);
        System.out.println("Choose a field to edit:");
        System.out.printf("\tReservation: %d\n", res.getCode());
        System.out.printf("\t1: First Name: %s\n", res.getFirstName());
        System.out.printf("\t2: Last Name: %s\n", res.getLastName());
        if(res.getCheckIn() == ""){
            System.out.printf("\t3: Range of Dates: \n");
        }
        else{
            System.out.printf("\t3: Range of Dates: %s - %s\n", res.getCheckIn(), res.getCheckOut());
        }
        System.out.printf("\t4: Number of Children: %d\n", res.getKids());
        System.out.printf("\t5: Number of Adults: %d\n", res.getAdult());
        System.out.println("\t6: Confirm Reservation");
        System.out.println("\t7: Cancel Changes");
        System.out.print("\nChoose which option to enter: ");
        try {
            input = reader.nextInt();
        } catch (Exception InputMismatchException) {
            System.out.println("Invalid Type");
        }
        return input;
    }

    public static boolean execRequirement3(Reservation res){
        try (Connection conn = DriverManager.getConnection(System.getenv("APP_JDBC_URL"),
                System.getenv("APP_JDBC_USER"),
                System.getenv("APP_JDBC_PW"))) {

            PreparedStatement statement = conn.prepareStatement(
                    "UPDATE lab7_reservations SET " +
                            "FirstName = ?, " +
                            "LastName = ?, " +
                            "CheckIn = ?, " +
                            "CheckOut = ?, " +
                            "Kids = ?, " +
                            "Adults = ? " +
                            "WHERE Code = ?");

            statement.setString(1, res.getFirstName());
            statement.setString(2, res.getLastName());
            statement.setString(3, res.getCheckIn());
            statement.setString(4, res.getCheckOut());
            statement.setInt(5, res.getKids());
            statement.setInt(6, res.getAdult());
            statement.setInt(7, res.getCode());

            int result = statement.executeUpdate();

            // Check that we actually updated any rows
            return result >= 1;
        }
        catch(SQLException e){
            System.err.println("SQLException: " + e.getMessage());
            return false;
        }
    }

    // Requirement 4
    public static boolean requirement4(){
        Scanner reader = new Scanner (System.in);

        System.out.print("Please enter your reservation code to cancel: ");
        int code = reader.nextInt();

        // Skip the newline
        reader.nextLine();

        System.out.print("Please enter YES to confirm you want to delete the reservation: ");
        String confirm = reader.nextLine();

        if (confirm.equals("YES")) {
            try (Connection conn = DriverManager.getConnection(System.getenv("APP_JDBC_URL"),
                    System.getenv("APP_JDBC_USER"),
                    System.getenv("APP_JDBC_PW"))) {


                PreparedStatement statement = conn.prepareStatement(
                        "DELETE FROM lab7_reservations " +
                                "WHERE Code = ?");
                statement.setInt(1, code);
                int result = statement.executeUpdate();

                if (result >= 1) {
                    System.out.println("Successfully canceled the reservation");
                    return true;
                } else {
                    System.out.println("No reservation was found with that code");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        return false;
    }

}
