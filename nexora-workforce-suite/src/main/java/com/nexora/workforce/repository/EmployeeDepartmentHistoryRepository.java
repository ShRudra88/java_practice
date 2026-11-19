package com.nexora.workforce.repository;

import com.nexora.workforce.entity.EmployeeDepartmentHistory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeDepartmentHistoryRepository
        extends JpaRepository<EmployeeDepartmentHistory, Long> {

    @EntityGraph(attributePaths = {"fromDepartment", "toDepartment"})
    List<EmployeeDepartmentHistory> findByEmployee_IdOrderByEffectiveDateAscIdAsc(
            Long employeeId);

    Optional<EmployeeDepartmentHistory> findTopByEmployee_IdOrderByEffectiveDateDescIdDesc(
            Long employeeId);

    void deleteByEmployee_Id(Long employeeId);
}
