
import java.util.ArrayList;
import java.util.HashMap;

public class EmployeeService implements EmployeeOperations {

    // ArrayList stores all employees
    private ArrayList<Employee> employeeList = new ArrayList<>();

    // HashMap stores employees using their ID
    private HashMap<Integer, Employee> employeeMap = new HashMap<>();

    // ADD EMPLOYEE
    @Override
    public void addEmployee(Employee employee) {

        int id = employee.getId();

        try {
            if (employeeMap.containsKey(id)) {
                throw new IllegalArgumentException(
                    "Employee ID already exists!"
                );
            }

            employeeList.add(employee);
            employeeMap.put(id, employee);

            System.out.println("Employee added successfully!");

        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // VIEW ALL EMPLOYEES
    @Override
    public void viewAllEmployees() {

        if (employeeList.isEmpty()) {
            System.out.println("No employees found.");
            return;
        }

        System.out.println("\n===== EMPLOYEE LIST =====");

        for (Employee employee : employeeList) {
            System.out.println("-------------------------");
            employee.displayDetails();
        }
    }

    // SEARCH EMPLOYEE
    @Override
    public void searchEmployee(int id) {

        try {
            Employee employee = employeeMap.get(id);

            if (employee == null) {
                throw new IllegalArgumentException(
                    "Employee not found!"
                );
            }

            System.out.println("\nEmployee Found:");
            employee.displayDetails();

        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // UPDATE EMPLOYEE
    @Override
    public void updateEmployee(int id, String name,
                               double salary, String department) {

        try {
            Employee employee = employeeMap.get(id);

            if (employee == null) {
                throw new IllegalArgumentException(
                    "Employee not found!"
                );
            }

            employee.setDetails(name, salary, department);

            System.out.println("Employee updated successfully!");

        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // DELETE EMPLOYEE
    @Override
    public void deleteEmployee(int id) {

        try {
            Employee employee = employeeMap.get(id);

            if (employee == null) {
                throw new IllegalArgumentException(
                    "Employee not found!"
                );
            }

            employeeList.remove(employee);
            employeeMap.remove(id);

            System.out.println("Employee deleted successfully!");

        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
