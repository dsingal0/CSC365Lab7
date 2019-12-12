# Inn Reservations

## Credits
Ahmed Shalan

Mohammed Aichouri

Dhruv Singal


## Compilation Instructions
From the root of the project directory, enter the following commands.
Make sure that the mysql driver is in the lib directory. Prior to running,
load the environment variables in using a bash script.

```
$ javac -d out src/com/company/*.java
$ java -cp $CLASSPATH com.company.InnReservations
```

## Bash Script
The bash script has not been included in this repo due to username and password.

Create a bash script using the following code. Make sure the CLASSPATH is exactly as below.

```
#!/bin/bash
export CLASSPATH=./out:./lib/mysql-connector-java-8.0.16.jar
export APP_JDBC_URL=jdbc:mysql://db.labthreesixfive.com/'ENTER_USERNAME'?autoReconnect=true\&useSSL=false
export APP_JDBC_USER='ENTER_USERNAME'
export APP_JDBC_PW='ENTER_PASSWORD'
```

## ENV Variables
Add a bash script with the following environment variables:
  - CLASSPATH
  - APP_JDBC_URL
  - APP_JDBC_USER
  - APP_JDBC_PW
  
## Notes
The database used for testing this project is hosted under @ashalan.
This project expects a database named lab7_rooms and lab7_reservations.


