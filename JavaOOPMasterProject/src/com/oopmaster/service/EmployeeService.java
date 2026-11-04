package com.oopmaster.service;

import com.oopmaster.exception.EmployeeNotFoundException;
import com.oopmaster.model.Employee;
import com.oopmaster.model.Manager;
import com.oopmaster.repository.EmployeeRepository;
import java.util.List;

/** Business layer depends on an abstraction (repository interface). */
public class EmployeeService {
    private final EmployeeRepository repository;
    public EmployeeService(EmployeeRepository repository) { this.repository = repository; }

    public void register(Employee employee) { repository.add(employee); }
    public Employee getEmployee(int id) throws EmployeeNotFoundException {
        return repository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
    }
    public List<Employee> getAllEmployees() { return repository.findAll(); }
    public void deleteEmployee(int id) throws EmployeeNotFoundException {
        if (!repository.remove(id)) throw new EmployeeNotFoundException(id);
    }
    public double calculateMonthlyPayroll() {
        // Runtime polymorphism: Employee reference can point to Employee or Manager.
        return repository.findAll().stream().mapToDouble(Employee::calculatePay).sum();
    }
    public void addSampleData() {
        var engineering = new com.oopmaster.model.Department("Engineering");
        var hr = new com.oopmaster.model.Department("Human Resources");
        register(new Employee(101, "Ayesha Rahman", "ayesha@example.com", engineering, 45000));
        register(new Manager(102, "Karim Hasan", "karim@example.com", engineering, 70000, 15000));
        register(new Employee(103, "Nadia Islam", "nadia@example.com", hr));
    }
}
