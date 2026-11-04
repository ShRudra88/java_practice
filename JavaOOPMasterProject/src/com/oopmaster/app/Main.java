package com.oopmaster.app;

import com.oopmaster.exception.EmployeeNotFoundException;
import com.oopmaster.model.Department;
import com.oopmaster.model.Employee;
import com.oopmaster.model.Manager;
import com.oopmaster.repository.EmployeeRepository;
import com.oopmaster.repository.InMemoryEmployeeRepository;
import com.oopmaster.service.EmployeeService;
import java.util.Scanner;


public class Main {
    private static final Scanner INPUT = new Scanner(System.in);
    private static final EmployeeService SERVICE;
    static {
        EmployeeRepository repository = new InMemoryEmployeeRepository(); 
        SERVICE = new EmployeeService(repository);
        SERVICE.addSampleData();
    }

    public static void main(String[] args) {
        System.out.println("=== OOP Employee Management System ===");
        boolean running = true;
        while (running) {
            printMenu();
            try {
                System.out.print("Choose: ");
                int choice = Integer.parseInt(INPUT.nextLine().trim());
                switch (choice) {
                    case 1 -> listEmployees();
                    case 2 -> addEmployee(false);
                    case 3 -> addEmployee(true);
                    case 4 -> searchEmployee();
                    case 5 -> deleteEmployee();
                    case 6 -> System.out.printf("Total monthly payroll: %.2f%n", SERVICE.calculateMonthlyPayroll());
                    case 0 -> running = false;
                    default -> System.out.println("Invalid menu option.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (IllegalArgumentException e) {
                System.out.println("Input error: " + e.getMessage());
            } catch (EmployeeNotFoundException e) {
                System.out.println(e.getMessage());
            }
            System.out.println();
        }
        System.out.println("Goodbye!");
    }

    private static void printMenu() {
        System.out.println("1. List employees\n2. Add employee\n3. Add manager\n4. Search by ID\n5. Delete by ID\n6. Show monthly payroll\n0. Exit");
    }
    private static void listEmployees() {
        System.out.printf("%-5s %-18s %-24s %-15s %10s%n", "ID", "NAME", "EMAIL", "DEPARTMENT", "MONTHLY PAY");
        SERVICE.getAllEmployees().forEach(System.out::println);
        System.out.println("Employee records: " + SERVICE.getAllEmployees().size());
    }
    private static void addEmployee(boolean manager) {
        int id = readInt("ID: ");
        System.out.print("Name: "); String name = INPUT.nextLine();
        System.out.print("Email: "); String email = INPUT.nextLine();
        System.out.print("Department: "); Department department = new Department(INPUT.nextLine());
        double salary = readDouble("Base salary: ");
        Employee employee;
        if (manager) employee = new Manager(id, name, email, department, salary, readDouble("Manager allowance: "));
        else employee = new Employee(id, name, email, department, salary);
        SERVICE.register(employee);
        System.out.println(employee.getRole() + " added successfully.");
    }
    private static void searchEmployee() throws EmployeeNotFoundException {
        int id = readInt("Employee ID: ");
        Employee employee = SERVICE.getEmployee(id);
        employee.introduce();
        System.out.printf("Monthly pay: %.2f%n", employee.calculatePay());
    }
    private static void deleteEmployee() throws EmployeeNotFoundException {
        SERVICE.deleteEmployee(readInt("Employee ID to delete: "));
        System.out.println("Employee deleted.");
    }
    private static int readInt(String prompt) {
        System.out.print(prompt); return Integer.parseInt(INPUT.nextLine().trim());
    }
    private static double readDouble(String prompt) {
        System.out.print(prompt); return Double.parseDouble(INPUT.nextLine().trim());
    }
}
