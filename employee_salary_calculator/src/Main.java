import java.util.Scanner;

public class Main {

public static void main(String[] args) {

    Scanner sc = new Scanner(System.in);

    SalaryCalculator calculator = new SalaryCalculator();

    System.out.println("===== Employee Salary Calculator =====");

    System.out.print("Enter Employee ID: ");
    int id = sc.nextInt();
    sc.nextLine();

    System.out.print("Enter Employee Name: ");
    String name = sc.nextLine();

    System.out.print("Enter Department: ");
    String department = sc.nextLine();

    System.out.print("Enter Basic Salary: ");
    double basicSalary = sc.nextDouble();

    if (basicSalary < 0) {
        System.out.println("Salary cannot be negative.");
        sc.close();
        return;
    }

    Employee employee = new Employee(
        id, name, department, basicSalary
    );

    calculator.displaySalaryDetails(employee);

    sc.close();
}


}