
package com.nexora.workforce.repository;

import com.nexora.workforce.entity.Employee;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

    @Override
    @EntityGraph(attributePaths = "department")
    List<Employee> findAll();

    @Override
    @EntityGraph(attributePaths = "department")
    Optional<Employee> findById(Long id);

    @EntityGraph(attributePaths = "department")
    List<Employee> findByFirstNameContainingIgnoreCase(
        String firstName
    );

    @EntityGraph(attributePaths = "department")
    List<Employee> findByDepartment_NameIgnoreCase(
        String departmentName
    );

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
