public class Employee { private int id; private String name; private String department; private double basicSalary;

public Employee(int id, String name, String department, double basicSalary) {
    this.id = id;
    this.name = name;
    this.department = department;
    this.basicSalary = basicSalary;
}

public int getId() {
    return id;
}

public String getName() {
    return name;
}

public String getDepartment() {
    return department;
}

public double getBasicSalary() {
    return basicSalary;
}

public void display() {
    System.out.println("Employee ID: " + id);
    System.out.println("Name: " + name);
    System.out.println("Department: " + department);
    System.out.println("Basic Salary: " + basicSalary);
}


}