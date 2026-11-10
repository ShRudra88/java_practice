
package main.java.com.hr;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        EmployeeDAO dao = new EmployeeDAO();

        while (true) {

            System.out.println("\n===== HR MANAGEMENT SYSTEM =====");

            System.out.println("1. Add Employee");
            System.out.println("2. View Employees");
            System.out.println("3. Update Employee");
            System.out.println("4. Delete Employee");
            System.out.println("5. Exit");

            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1:

                    System.out.print("Employee ID: ");
                    int id = sc.nextInt();
                    sc.nextLine();

                    System.out.print("First Name: ");
                    String firstName = sc.nextLine();

                    System.out.print("Last Name: ");
                    String lastName = sc.nextLine();

                    System.out.print("Email: ");
                    String email = sc.nextLine();

                    System.out.print("Salary: ");
                    double salary = sc.nextDouble();

                    System.out.print("Department ID: ");
                    int deptId = sc.nextInt();

                    System.out.print("Designation ID: ");
                    int desigId = sc.nextInt();

                    dao.addEmployee(
                        id, firstName, lastName,
                        email, salary, deptId, desigId
                    );

                    break;

                case 2:

                    dao.viewEmployees();

                    break;

                case 3:

                    System.out.print("Employee ID: ");
                    int updateId = sc.nextInt();
                    sc.nextLine();

                    System.out.print("New First Name: ");
                    String newFirst = sc.nextLine();

                    System.out.print("New Last Name: ");
                    String newLast = sc.nextLine();

                    System.out.print("New Email: ");
                    String newEmail = sc.nextLine();

                    System.out.print("New Salary: ");
                    double newSalary = sc.nextDouble();

                    dao.updateEmployee(
                        updateId,
                        newFirst,
                        newLast,
                        newEmail,
                        newSalary
                    );

                    break;

                case 4:

                    System.out.print("Employee ID: ");
                    int deleteId = sc.nextInt();

                    dao.deleteEmployee(deleteId);

                    break;

                case 5:

                    System.out.println("Application closed.");

                    sc.close();
                    return;

                default:

                    System.out.println("Invalid choice!");
            }
        }
    }
}
