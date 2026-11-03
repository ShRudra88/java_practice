
public class Employee {

    private int employeeId;
    private String employeeName;
    private double salary;

    private Department department;
    private Designation designation;

    // Constructor
    public Employee(int employeeId, String employeeName,
                    double salary, Department department,
                    Designation designation) {

        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.salary = salary;
        this.department = department;
        this.designation = designation;
    }

    // Display Employee Information
    public void displayEmployee() {

        System.out.println("Employee ID: " + employeeId);
        System.out.println("Employee Name: " + employeeName);
        System.out.println("Salary: " + salary);

        System.out.println("\n--- Department Details ---");
        department.displayDepartment();

        System.out.println("\n--- Designation Details ---");
        designation.displayDesignation();
    }
}
