
package com.nexora.workforce.repository;

import com.nexora.workforce.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

    List<Employee> findByFirstNameContainingIgnoreCase(
        String firstName
    );

    List<Employee> findByDepartmentIgnoreCase(
        String department
    );
}
