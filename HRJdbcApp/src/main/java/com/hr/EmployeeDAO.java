package main.java.com.hr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeDAO {

    public void addEmployee(
            int id,
            String firstName,
            String lastName,
            String email,
            double salary,
            int departmentId,
            int designationId) {

        String sql = "INSERT INTO EMPLOYEES " +
            "(EMPLOYEE_ID, FIRST_NAME, LAST_NAME, EMAIL, " +
            "SALARY, DEPARTMENT_ID, DESIGNATION_ID) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, firstName);
            ps.setString(3, lastName);
            ps.setString(4, email);
            ps.setDouble(5, salary);
            ps.setInt(6, departmentId);
            ps.setInt(7, designationId);

            int rows = ps.executeUpdate();
            System.out.println(rows + " employee added successfully!");
        } catch (SQLException e) {
            System.out.println("Insert error: " + e.getMessage());
        }
    }

    public void viewEmployees() {
        String sql = "SELECT EMPLOYEE_ID, FIRST_NAME, LAST_NAME, EMAIL, " +
            "SALARY, DEPARTMENT_ID, DESIGNATION_ID " +
            "FROM EMPLOYEES ORDER BY EMPLOYEE_ID";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n--- EMPLOYEE LIST ---");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("EMPLOYEE_ID"));
                System.out.println(
                    "Name: " + rs.getString("FIRST_NAME") + " " +
                    rs.getString("LAST_NAME"));
                System.out.println("Email: " + rs.getString("EMAIL"));
                System.out.println("Salary: " + rs.getDouble("SALARY"));
                System.out.println(
                    "Department ID: " + rs.getInt("DEPARTMENT_ID"));
                System.out.println(
                    "Designation ID: " + rs.getInt("DESIGNATION_ID"));
                System.out.println("--------------------");
            }
        } catch (SQLException e) {
            System.out.println("Read error: " + e.getMessage());
        }
    }

    public void updateEmployee(
            int id,
            String firstName,
            String lastName,
            String email,
            double salary) {

        String sql = "UPDATE EMPLOYEES SET FIRST_NAME = ?, LAST_NAME = ?, " +
            "EMAIL = ?, SALARY = ? WHERE EMPLOYEE_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, email);
            ps.setDouble(4, salary);
            ps.setInt(5, id);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Employee updated successfully!");
            } else {
                System.out.println("Employee not found!");
            }
        } catch (SQLException e) {
            System.out.println("Update error: " + e.getMessage());
        }
    }

    public void deleteEmployee(int id) {
        String sql = "DELETE FROM EMPLOYEES WHERE EMPLOYEE_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Employee deleted successfully!");
            } else {
                System.out.println("Employee not found!");
            }
        } catch (SQLException e) {
            System.out.println("Delete error: " + e.getMessage());
        }
    }
}
