
package com.nexora.workforce.repository;

import com.nexora.workforce.entity.Employee;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

    @Override
    @EntityGraph(attributePaths = "department")
    List<Employee> findAll();

    @Override
    @EntityGraph(attributePaths = "department")
    Optional<Employee> findById(Long id);

    @EntityGraph(attributePaths = "department")
    @Query(
            """
                    SELECT e FROM Employee e
                    WHERE (:name IS NULL OR :name = ''
                        OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
                        OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
                        OR LOWER(CONCAT(CONCAT(e.firstName, ' '), e.lastName))
                            LIKE LOWER(CONCAT('%', :name, '%')))
                    AND (:departmentId IS NULL OR e.department.id = :departmentId)
                    """)
    Page<Employee> searchEmployees(
            @Param("name") String name,
            @Param("departmentId") Long departmentId,
            Pageable pageable);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    Optional<Employee> findByEmailIgnoreCaseAndPasswordHashIsNotNull(String email);
}
