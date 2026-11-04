package com.oopmaster.repository;

import com.oopmaster.model.Employee;
import java.util.List;
import java.util.Optional;

/** Repository interface keeps storage details separate from business logic. */
public interface EmployeeRepository {
    void add(Employee employee);
    Optional<Employee> findById(int id);
    List<Employee> findAll();
    boolean remove(int id);
}
