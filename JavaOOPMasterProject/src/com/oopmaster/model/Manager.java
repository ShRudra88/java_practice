package com.oopmaster.model;

/** Manager IS-A Employee: inheritance; overrides pay and role. */
public class Manager extends Employee {
    private double allowance;
    public Manager(int id, String name, String email, Department department, double salary, double allowance) {
        super(id, name, email, department, salary);
        setAllowance(allowance);
    }
    public void setAllowance(double allowance) {
        if (allowance < 0) throw new IllegalArgumentException("Allowance cannot be negative.");
        this.allowance = allowance;
    }
    @Override public String getRole() { return "Manager"; }
    @Override public double calculatePay() { return getBaseSalary() + allowance; }
    @Override public String toString() {
        return String.format("%-5d %-18s %-24s %-15s %10.2f", getId(), getName(), getEmail(), getDepartment().getName(), calculatePay());
    }
}
