
package com.nexora.workforce.service;

import com.nexora.workforce.dto.EmployeeDepartmentHistoryResponseDTO;
import com.nexora.workforce.dto.EmployeePageResponseDTO;
import com.nexora.workforce.dto.EmployeeRequestDTO;
import com.nexora.workforce.dto.EmployeeResponseDTO;
import com.nexora.workforce.dto.EmployeeTransferRequestDTO;
import com.nexora.workforce.entity.Department;
import com.nexora.workforce.entity.Employee;
import com.nexora.workforce.entity.EmployeeDepartmentHistory;
import com.nexora.workforce.exception.DuplicateResourceException;
import com.nexora.workforce.exception.ResourceNotFoundException;
import com.nexora.workforce.repository.DepartmentRepository;
import com.nexora.workforce.repository.EmployeeDepartmentHistoryRepository;
import com.nexora.workforce.repository.EmployeeRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeDepartmentHistoryRepository historyRepository;

    public EmployeeService(
            EmployeeRepository repository,
            DepartmentRepository departmentRepository,
            EmployeeDepartmentHistoryRepository historyRepository) {
        this.repository = repository;
        this.departmentRepository = departmentRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO request) {
        if (request.getId() != null) {
            if (repository.existsById(request.getId())) {
                throw new DuplicateResourceException(
                        "Employee ID already exists: " + request.getId());
            }
            throw new IllegalArgumentException(
                    "Employee ID is generated and must not be provided");
        }
        ensureEmailIsAvailable(request.getEmail(), null);

        Employee employee = toEmployee(request);
        employee.setDepartment(resolveDepartment(employee.getDepartment()));
        Employee savedEmployee = repository.save(employee);
        if (savedEmployee.getDepartment() != null) {
            saveDepartmentHistory(
                    savedEmployee,
                    null,
                    savedEmployee.getDepartment(),
                    savedEmployee.getHireDate() == null
                            ? LocalDate.now()
                            : savedEmployee.getHireDate());
        }
        return toResponse(savedEmployee);
    }

    public List<EmployeeResponseDTO> getAllEmployees() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public EmployeeResponseDTO getEmployeeById(Long id) {
        return toResponse(findEmployeeEntityById(id));
    }

    private Employee findEmployeeEntityById(Long id) {
        return repository.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Employee not found: " + id)
            );
    }

    @Transactional
    public EmployeeResponseDTO updateEmployee(
            Long id, EmployeeRequestDTO request) {

        Employee currentEmployee = findEmployeeEntityById(id);
        if (request.getId() != null && !request.getId().equals(id)) {
            if (repository.existsById(request.getId())) {
                throw new DuplicateResourceException(
                        "Employee ID already exists: " + request.getId());
            }
            throw new IllegalArgumentException(
                    "Employee ID in the request must match the path ID");
        }
        ensureEmailIsAvailable(request.getEmail(), id);

        Employee updatedEmployee = toEmployee(request);
        Department updatedDepartment = resolveDepartment(updatedEmployee.getDepartment());
        if (!sameDepartment(currentEmployee.getDepartment(), updatedDepartment)) {
            recordDepartmentChange(
                    currentEmployee, updatedDepartment, LocalDate.now());
        }
        currentEmployee.setFirstName(updatedEmployee.getFirstName());
        currentEmployee.setLastName(updatedEmployee.getLastName());
        currentEmployee.setEmail(updatedEmployee.getEmail());
        currentEmployee.setPhone(updatedEmployee.getPhone());
        currentEmployee.setDepartment(updatedDepartment);
        currentEmployee.setJobTitle(updatedEmployee.getJobTitle());
        currentEmployee.setSalary(updatedEmployee.getSalary());
        currentEmployee.setHireDate(updatedEmployee.getHireDate());
        currentEmployee.setStatus(updatedEmployee.getStatus());

        return toResponse(repository.save(currentEmployee));
    }

    @Transactional
    public EmployeeResponseDTO transferEmployee(
            Long employeeId, EmployeeTransferRequestDTO request) {
        return toResponse(transferEmployeeEntity(
                    employeeId, request.departmentId(), request.effectiveDate()));
    }

    @Transactional
    public Employee transferEmployeeEntity(
            Long employeeId, Long departmentId, LocalDate effectiveDate) {
        if (effectiveDate == null) {
            throw new IllegalArgumentException("Effective date is required");
        }
        if (effectiveDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Effective date must not be in the future");
        }

        Employee employee = findEmployeeEntityById(employeeId);
        Department destination = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found: " + departmentId));
        if (sameDepartment(employee.getDepartment(), destination)) {
            return employee;
        }
        recordDepartmentChange(employee, destination, effectiveDate);
        employee.setDepartment(destination);
        return repository.save(employee);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDepartmentHistoryResponseDTO> getDepartmentHistory(
            Long employeeId) {
        findEmployeeEntityById(employeeId);
        return historyRepository
                .findByEmployee_IdOrderByEffectiveDateAscIdAsc(employeeId)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private void ensureEmailIsAvailable(String email, Long employeeId) {
        boolean emailInUse = employeeId == null
                ? repository.existsByEmailIgnoreCase(email)
                : repository.existsByEmailIgnoreCaseAndIdNot(email, employeeId);
        if (emailInUse) {
            throw new DuplicateResourceException(
                    "Employee email already exists: " + email);
        }
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = findEmployeeEntityById(id);
        historyRepository.deleteByEmployee_Id(id);
        repository.delete(employee);
    }

    public EmployeePageResponseDTO searchEmployees(
            String name, Long departmentId, int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be zero or greater");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }

        String normalizedName = name == null || name.isBlank()
                ? null
                : name.trim();
        Page<EmployeeResponseDTO> employeePage = repository.searchEmployees(
                        normalizedName,
                        departmentId,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id")))
                .map(this::toResponse);

        return new EmployeePageResponseDTO(
                employeePage.getContent(),
                employeePage.getNumber(),
                employeePage.getSize(),
                employeePage.getTotalElements(),
                employeePage.getTotalPages(),
                employeePage.hasNext(),
                employeePage.hasPrevious());
    }

    private Employee toEmployee(EmployeeRequestDTO request) {
        Employee employee = new Employee();
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setDepartment(request.getDepartment());
        employee.setJobTitle(request.getJobTitle());
        employee.setSalary(request.getSalary());
        employee.setHireDate(request.getHireDate());
        employee.setStatus(request.getStatus());
        return employee;
    }

    private EmployeeResponseDTO toResponse(Employee employee) {
        Department department = employee.getDepartment();
        return new EmployeeResponseDTO(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPhone(),
                department == null ? null : department.getId(),
                department == null ? null : department.getName(),
                employee.getJobTitle(),
                employee.getSalary(),
                employee.getHireDate(),
                employee.getStatus());
    }

    private EmployeeDepartmentHistoryResponseDTO toHistoryResponse(
            EmployeeDepartmentHistory history) {
        Department fromDepartment = history.getFromDepartment();
        Department toDepartment = history.getToDepartment();
        return new EmployeeDepartmentHistoryResponseDTO(
                history.getId(),
                fromDepartment == null ? null : fromDepartment.getId(),
                fromDepartment == null ? null : fromDepartment.getName(),
                toDepartment == null ? null : toDepartment.getId(),
                toDepartment == null ? null : toDepartment.getName(),
                history.getEffectiveDate());
    }

    private void recordDepartmentChange(
            Employee employee, Department destination, LocalDate effectiveDate) {
        if (effectiveDate == null || effectiveDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Effective date must be today or earlier");
        }
        if (employee.getHireDate() != null
                && effectiveDate.isBefore(employee.getHireDate())) {
            throw new IllegalArgumentException(
                    "Effective date must not be before the employee's hire date");
        }
        historyRepository.findTopByEmployee_IdOrderByEffectiveDateDescIdDesc(
                        employee.getId())
                .ifPresent(previous -> {
                    if (effectiveDate.isBefore(previous.getEffectiveDate())) {
                        throw new IllegalArgumentException(
                                "Effective date must not be before the latest department history");
                    }
                });
        saveDepartmentHistory(
                employee, employee.getDepartment(), destination, effectiveDate);
    }

    private void saveDepartmentHistory(
            Employee employee,
            Department fromDepartment,
            Department toDepartment,
            LocalDate effectiveDate) {
        EmployeeDepartmentHistory history = new EmployeeDepartmentHistory();
        history.setEmployee(employee);
        history.setFromDepartment(fromDepartment);
        history.setToDepartment(toDepartment);
        history.setEffectiveDate(effectiveDate);
        historyRepository.save(history);
    }

    private boolean sameDepartment(Department first, Department second) {
        return first == second
                || (first != null && second != null
                        && first.getId() != null
                        && first.getId().equals(second.getId()));
    }

    private Department resolveDepartment(Department departmentReference) {
        if (departmentReference == null) {
            return null;
        }

        if (departmentReference.getId() != null) {
            return departmentRepository.findById(departmentReference.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found: " + departmentReference.getId()));
        }

        if (departmentReference.getName() != null) {
            if (departmentReference.getName().isBlank()) {
                return null;
            }

            List<Department> matches = departmentRepository.findAllByNameIgnoreCase(
                    departmentReference.getName().trim());
            if (matches.size() == 1) {
                return matches.get(0);
            }
            if (matches.isEmpty()) {
                throw new ResourceNotFoundException(
                        "Department not found: " + departmentReference.getName());
            }
            throw new IllegalArgumentException(
                    "Multiple departments match name: " + departmentReference.getName());
        }

        throw new IllegalArgumentException(
                "department must contain an id or a department name");
    }
}
