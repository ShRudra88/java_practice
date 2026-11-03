
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        EmployeeService service = new EmployeeService();

        int choice = 0;

        do {

            System.out.println("\n================================");
            System.out.println("      EMPLOYEE MANAGEMENT SYSTEM");
            System.out.println("================================");
            System.out.println("1. Add Employee");
            System.out.println("2. View All Employees");
            System.out.println("3. Search Employee");
            System.out.println("4. Update Employee");
            System.out.println("5. Delete Employee");
            System.out.println("6. Exit");
            System.out.println("================================");

            try {

                System.out.print("Enter your choice: ");
                choice = Integer.parseInt(input.nextLine());

                switch (choice) {

                    case 1:

                        System.out.print("Enter Employee ID: ");
                        int id = Integer.parseInt(input.nextLine());

                        System.out.print("Enter Employee Name: ");
                        String name = input.nextLine();

                        System.out.print("Enter Salary: ");
                        double salary = Double.parseDouble(input.nextLine());

                        System.out.print("Enter Department: ");
                        String department = input.nextLine();

                        if (id <= 0 || salary < 0 ||
                            name.trim().isEmpty() ||
                            department.trim().isEmpty()) {
                            System.out.println("Invalid employee information!");
                            break;
                        }

                        Employee employee = new Employee(
                            id, name, department, salary
                        );

                        service.addEmployee(employee);
                        break;

                    case 2:

                        service.viewAllEmployees();
                        break;

                    case 3:

                        System.out.print("Enter Employee ID to search: ");
                        int searchId = Integer.parseInt(input.nextLine());

                        service.searchEmployee(searchId);
                        break;

                    case 4:

                        System.out.print("Enter Employee ID to update: ");
                        int updateId = Integer.parseInt(input.nextLine());

                        System.out.print("Enter New Name: ");
                        String newName = input.nextLine();

                        System.out.print("Enter New Salary: ");
                        double newSalary = Double.parseDouble(input.nextLine());

                        System.out.print("Enter New Department: ");
                        String newDepartment = input.nextLine();

                        if (newSalary < 0 ||
                            newName.trim().isEmpty() ||
                            newDepartment.trim().isEmpty()) {
                            System.out.println("Invalid employee information!");
                            break;
                        }

                        service.updateEmployee(
                            updateId, newName,
                            newSalary, newDepartment
                        );
                        break;

                    case 5:

                        System.out.print("Enter Employee ID to delete: ");
                        int deleteId = Integer.parseInt(input.nextLine());

                        service.deleteEmployee(deleteId);
                        break;

                    case 6:

                        System.out.println("Thank you for using the system!");
                        break;

                    default:

                        System.out.println("Invalid choice! Try again.");
                }

            } catch (NumberFormatException e) {

                System.out.println("Error: Please enter a valid number.");

            }

        } while (choice != 6);

        input.close();
    }
}
