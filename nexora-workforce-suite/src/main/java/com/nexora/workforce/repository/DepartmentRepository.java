package com.nexora.workforce.repository;

import com.nexora.workforce.entity.Department;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @Override
    @EntityGraph(attributePaths = {"employees", "employees.department"})
    List<Department> findAll();

    @EntityGraph(attributePaths = {"employees", "employees.department"})
    Optional<Department> findWithEmployeesById(Long id);

    List<Department> findAllByNameIgnoreCase(String name);
}
