package com.oopmaster.repository;

import com.oopmaster.model.Employee;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** In-memory implementation; uses a collection and interface polymorphism. */
public class InMemoryEmployeeRepository implements EmployeeRepository {
    private final List<Employee> employees = new ArrayList<>();

    @Override public void add(Employee employee) {
        if (findById(employee.getId()).isPresent()) throw new IllegalArgumentException("That ID already exists.");
        employees.add(employee);
    }
    @Override public Optional<Employee> findById(int id) {
        return employees.stream().filter(e -> e.getId() == id).findFirst();
    }
    @Override public List<Employee> findAll() { return new ArrayList<>(employees); }
    @Override public boolean remove(int id) { return employees.removeIf(e -> e.getId() == id); }
}
