
public interface EmployeeOperations {

    void addEmployee(Employee employee);

    void viewAllEmployees();

    void searchEmployee(int id);

    void updateEmployee(int id, String name,
                        double salary, String department);

    void deleteEmployee(int id);
}
