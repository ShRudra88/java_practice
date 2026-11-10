
package main.java.com.hr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
        "jdbc:oracle:thin:@//localhost:1521/FREEPDB1";

    private static final String USERNAME = "HR_PRACTICE";

    private static final String PASSWORD = "HrPractice#2026"; // pass removed for security reasons

    public static Connection getConnection() throws SQLException {

        return DriverManager.getConnection(
            URL,
            USERNAME,
            PASSWORD
        );
    }
}
