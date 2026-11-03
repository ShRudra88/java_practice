
public class Department {

    private int departmentId;
    private String departmentName;

    // Constructor
    public Department(int departmentId, String departmentName) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
    }

    // Display Department Information
    public void displayDepartment() {
        System.out.println("Department ID: " + departmentId);
        System.out.println("Department: " + departmentName);
    }
}
