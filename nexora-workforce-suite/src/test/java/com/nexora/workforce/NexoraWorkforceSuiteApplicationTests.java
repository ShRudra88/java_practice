package com.nexora.workforce;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexora.workforce.controller.DepartmentController;
import com.nexora.workforce.controller.EmployeeController;
import com.nexora.workforce.entity.Department;
import com.nexora.workforce.entity.Employee;
import com.nexora.workforce.repository.DepartmentRepository;
import com.nexora.workforce.repository.EmployeeRepository;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootTest
@AutoConfigureMockMvc
class NexoraWorkforceSuiteApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

	@Test
    void contextLoads() {
    }

	@Test
    void repositoriesAndDepartmentEndpointsAreRegistered() {
        assertNotNull(applicationContext.getBean(EmployeeRepository.class));
        assertNotNull(applicationContext.getBean(DepartmentRepository.class));

        Set<String> departmentPaths = requestMappingHandlerMapping
                .getHandlerMethods()
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().getBeanType()
                        .equals(DepartmentController.class))
                .flatMap(entry -> entry.getKey().getPatternValues().stream())
                .collect(Collectors.toSet());

        assertTrue(departmentPaths.contains("/api/departments"));
        assertTrue(departmentPaths.contains("/api/departments/{id}"));
        assertTrue(departmentPaths.contains(
                "/api/departments/{departmentId}/employees/{employeeId}"));

        Set<String> employeePaths = requestMappingHandlerMapping
                .getHandlerMethods()
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().getBeanType()
                        .equals(EmployeeController.class))
                .flatMap(entry -> entry.getKey().getPatternValues().stream())
                .collect(Collectors.toSet());

        assertTrue(employeePaths.contains("/api/employees/{id}/transfers"));
        assertTrue(employeePaths.contains("/api/employees/{id}/department-history"));
        assertTrue(employeePaths.contains("/api/employees/search"));
    }

	@Test
	@Transactional
    void transferEndpointUpdatesDepartmentAndReturnsHistory() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Department oldDepartment = new Department();
        oldDepartment.setName("Previous-" + suffix);
        oldDepartment = departmentRepository.saveAndFlush(oldDepartment);

        Department newDepartment = new Department();
        newDepartment.setName("Next-" + suffix);
        newDepartment = departmentRepository.saveAndFlush(newDepartment);

        Employee employee = new Employee();
        employee.setFirstName("Transfer");
        employee.setLastName("Test");
        employee.setEmail("transfer-" + suffix + "@example.com");
        employee.setDepartment(oldDepartment);
        employee.setHireDate(LocalDate.of(2020, 1, 1));
        employee = employeeRepository.saveAndFlush(employee);

        String request = String.format(
                "{\"departmentId\":%d,\"effectiveDate\":\"%s\"}",
                newDepartment.getId(), LocalDate.now());

        mockMvc.perform(post("/api/employees/{id}/transfers", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departmentId")
                        .value(newDepartment.getId()));

        mockMvc.perform(get("/api/employees/{id}/department-history", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromDepartmentName")
                        .value(oldDepartment.getName()))
                .andExpect(jsonPath("$[0].toDepartmentName")
                        .value(newDepartment.getName()))
                .andExpect(jsonPath("$[0].effectiveDate")
                        .value(LocalDate.now().toString()));
    }

	@Test
	@Transactional
    void employeeSearchFiltersByDepartmentAndPaginates() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Department department = new Department();
        department.setName("Search-" + suffix);
        department = departmentRepository.saveAndFlush(department);

        Employee first = createSearchEmployee(
                "Filter-" + suffix, "First", "first-" + suffix, department);
        Employee second = createSearchEmployee(
                "Filter-" + suffix, "Second", "second-" + suffix, department);
        Department otherDepartment = new Department();
        otherDepartment.setName("Other-" + suffix);
        otherDepartment = departmentRepository.saveAndFlush(otherDepartment);
        createSearchEmployee(
                "Filter-" + suffix, "Other", "other-" + suffix, otherDepartment);

        mockMvc.perform(get("/api/employees/search")
                        .param("name", "Filter-" + suffix)
                        .param("departmentId", department.getId().toString())
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(first.getId()))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.hasNext").value(true));

        mockMvc.perform(get("/api/employees/search")
                        .param("name", "Filter-" + suffix)
                        .param("departmentId", department.getId().toString())
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(second.getId()))
                .andExpect(jsonPath("$.hasPrevious").value(true));
    }

    private Employee createSearchEmployee(
            String firstName, String lastName, String emailPrefix, Department department) {
        Employee employee = new Employee();
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(emailPrefix + "@example.com");
        employee.setDepartment(department);
        return employeeRepository.saveAndFlush(employee);
    }
}
