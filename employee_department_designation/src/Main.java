
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        System.out.println("==================================");
        System.out.println("     EMPLOYEE MANAGEMENT SYSTEM");
        System.out.println("==================================");

        // Employee Information
        System.out.println("\nEnter Employee Information");

        System.out.print("Enter Employee ID: ");
        int employeeId = input.nextInt();
        input.nextLine();

        System.out.print("Enter Employee Name: ");
        String employeeName = input.nextLine();

        System.out.print("Enter Salary: ");
        double salary = input.nextDouble();

        // Department Information
        System.out.println("\nEnter Department Information");

        System.out.print("Enter Department ID: ");
        int departmentId = input.nextInt();
        input.nextLine();

        System.out.print("Enter Department Name: ");
        String departmentName = input.nextLine();

        // Designation Information
        System.out.println("\nEnter Designation Information");

        System.out.print("Enter Designation ID: ");
        int designationId = input.nextInt();
        input.nextLine();

        System.out.print("Enter Designation Name: ");
        String designationName = input.nextLine();

        // Creating Objects
        Department dept = new Department(
            departmentId, departmentName
        );

        Designation desig = new Designation(
            designationId, designationName
        );

        Employee emp = new Employee(
            employeeId,
            employeeName,
            salary,
            dept,
            desig
        );

        // Display Information
        System.out.println("\n==================================");
        System.out.println("       EMPLOYEE INFORMATION");
        System.out.println("==================================");

        emp.displayEmployee();

        System.out.println("\n==================================");
        System.out.println("       END OF PROGRAM");
        System.out.println("==================================");

        input.close();
    }
}
