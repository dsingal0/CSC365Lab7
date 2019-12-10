# Inn Reservations

## Credits
Ahmed Shalan

Mohammed Aichouri

Dhruv Singal


## Compilation Instructions
From the root of the project directory, enter the following commands.
Make sure that the mysql driver is in the lib directory.

$ source config/envsetup.sh
$ javac -d out src/com/company/*.java
$ java -cp $CLASSPATH com.company.InnReservations


## ENV Variables
The envsetup.sh, located in the config directory, should be sufficient to loading all environment variables.
The names are:
  CLASSPATH
  APP_JDBC_URL
  APP_JDBC_USER
  APP_JDBC_PW



