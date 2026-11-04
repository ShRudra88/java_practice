package com.oopmaster.model;

public class Employee extends Person implements Payable {
    private final Department department; 
    private double baseSalary;
    private static int employeeCount = 0; 

    public Employee(int id, String name, String email, Department department, double baseSalary) {
        super(id, name, email);
        if (department == null) throw new IllegalArgumentException("Department is required.");
        this.department = department;
        setBaseSalary(baseSalary);
        employeeCount++;
    }

    // Constructor overloading
    public Employee(int id, String name, String email, Department department) {
        this(id, name, email, department, 30000.0);
    }

    public Department getDepartment() { return department; }
    public double getBaseSalary() { return baseSalary; }
    public void setBaseSalary(double baseSalary) {
        if (baseSalary < 0) throw new IllegalArgumentException("Salary cannot be negative.");
        this.baseSalary = baseSalary;
    }
    public static int getEmployeeCount() { return employeeCount; }

    @Override public String getRole() { return "Employee"; }
    @Override public double calculatePay() { return baseSalary; }

    // Method overloading
    public double calculatePay(double bonus) { return baseSalary + Math.max(0, bonus); }

    @Override public String toString() {
        return String.format("%-5d %-18s %-24s %-15s %10.2f", getId(), getName(), getEmail(), department.getName(), calculatePay());
    }
}
