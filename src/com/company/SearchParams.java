package com.company;

public class SearchParams {
    public String lastName;
    public String firstName;
    public String reservationCode;
    public String roomCode;
    public String checkInStartRange;
    public String checkInEndRange;

    public SearchParams() {
        lastName = "%";
        firstName = "%";
        reservationCode = "%";
        roomCode = "%";
        checkInStartRange = "1901-00-00";
        checkInEndRange = "2155-00-00";
    }

    public SearchParams(String lastName,
                        String firstName,
                        String reservationCode,
                        String roomCode,
                        String checkInStartRange,
                        String checkInEndRange) {

        this.lastName = lastName;
        this.firstName = firstName;
        this.reservationCode = reservationCode;
        this.roomCode = roomCode;
        this.checkInStartRange = checkInStartRange;
        this.checkInEndRange = checkInEndRange;
    }
}
