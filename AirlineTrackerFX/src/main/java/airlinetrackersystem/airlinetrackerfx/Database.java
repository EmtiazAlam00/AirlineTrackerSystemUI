package airlinetrackersystem.airlinetrackerfx;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {
    public static Connection connectDb(){
        try{
            Class.forName("org.postgresql.Driver");
            Connection connect = DriverManager.getConnection("jdbc:postgresql://localhost:5432/AirlineParts_DB", "postgres", "postgres");
            return connect;
        } catch (Exception e){e.printStackTrace();}
        return null;
    }
}
