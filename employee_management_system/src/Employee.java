public class Employee extends Person {

    private double salary;
    private String department;

    public Employee(int id, String name,
                    double salary, String department) {

        super(id, name);
        this.salary = salary;
        this.department = department;
    }

    public double getSalary() {
        return salary;
    }

    public String getDepartment() {
        return department;
    }

    public void setDetails(String name, double salary,
                           String department) {

        setName(name);
        this.salary = salary;
        this.department = department;
    }

    @Override
    public void displayDetails() {

        System.out.println("Employee ID: " + getId());
        System.out.println("Employee Name: " + getName());
        System.out.println("Salary: " + salary);
        System.out.println("Department: " + department);
    }
}