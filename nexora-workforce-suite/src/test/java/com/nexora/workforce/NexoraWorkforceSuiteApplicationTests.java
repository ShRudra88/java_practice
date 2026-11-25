package com.nexora.workforce;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.nexora.workforce.controller.DepartmentController;
import com.nexora.workforce.controller.EmployeeController;
import com.nexora.workforce.entity.Department;
import com.nexora.workforce.entity.Employee;
import com.nexora.workforce.repository.DepartmentRepository;
import com.nexora.workforce.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootTest(properties = {
        "nexora.security.username=admin",
        "nexora.security.password=change-me"
})
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
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

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @WithAnonymousUser
    void apiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication required"));
    }

    @Test
    void staticPagesRemainPublic() throws Exception {
        mockMvc.perform(get("/signin.html"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void workforcePagesRequireAdminSignIn() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/signin.html"));
    }

    @Test
    @WithAnonymousUser
    void adminCanSignInWithConfiguredCredentials() throws Exception {
        mockMvc.perform(formLogin().user("admin").password("change-me"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @Transactional
    @WithAnonymousUser
    void employeeCanSignInWithEmailAndStoredPassword() throws Exception {
        Employee employee = new Employee();
        employee.setFirstName("Employee");
        employee.setLastName("Account");
        employee.setEmail("account-" + UUID.randomUUID() + "@example.com");
        employee.setPasswordHash(passwordEncoder.encode("employee-password"));
        employeeRepository.saveAndFlush(employee);

        mockMvc.perform(formLogin()
                        .user(employee.getEmail())
                        .password("employee-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @Transactional
    @WithMockUser(username = "employee@example.com", roles = "EMPLOYEE")
    void employeeCanOnlyReadTheEmployeeCountAndTheirOwnProfile() throws Exception {
        Employee ownEmployee = new Employee();
        ownEmployee.setFirstName("Own");
        ownEmployee.setLastName("Account");
        ownEmployee.setEmail("employee@example.com");
        ownEmployee.setPasswordHash("{noop}employee-password");
        employeeRepository.saveAndFlush(ownEmployee);

        Employee otherEmployee = new Employee();
        otherEmployee.setFirstName("Other");
        otherEmployee.setLastName("Employee");
        otherEmployee.setEmail("other-" + UUID.randomUUID() + "@example.com");
        employeeRepository.saveAndFlush(otherEmployee);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));
        mockMvc.perform(get("/api/employees/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(employeeRepository.count()));
        mockMvc.perform(get("/api/employees/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("employee@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/employees/search"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/employees/{id}", otherEmployee.getId()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/employee-registration.html"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/employees/{id}", otherEmployee.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/employees/{id}", otherEmployee.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/departments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Employee Restricted\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void adminCanReadAllEmployeeRecordsAndCount() throws Exception {
        Employee employee = new Employee();
        employee.setFirstName("Admin");
        employee.setLastName("Visible");
        employee.setEmail("admin-visible-" + UUID.randomUUID() + "@example.com");
        employeeRepository.saveAndFlush(employee);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email == '" + employee.getEmail() + "')]")
                        .isNotEmpty());
        mockMvc.perform(get("/api/employees/{id}", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(employee.getEmail()));
        mockMvc.perform(get("/api/employees/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(employeeRepository.count()));
    }

    @Test
    @Transactional
    void adminCanCreateEmployeeWithEncodedSignInPassword() throws Exception {
        String email = "new-employee-" + UUID.randomUUID() + "@example.com";
        String request = objectMapper.writeValueAsString(java.util.Map.of(
                "firstName", "New",
                "lastName", "Employee",
                "email", email,
                "password", "employee-password"));

        var result = mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn();

        Long employeeId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asLong();
        Employee savedEmployee = employeeRepository.findById(employeeId).orElseThrow();
        assertTrue(
                passwordEncoder.matches("employee-password", savedEmployee.getPasswordHash()));
        assertNotEquals(
                "employee-password", savedEmployee.getPasswordHash());
    }

    @Test
    void employeeCreateRequestValidationReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fields.firstName").exists())
                .andExpect(jsonPath("$.fields.password").exists());
    }

    @Test
    @Transactional
    void csrfTokenCanAuthorizeApiWriteRequests() throws Exception {
        var csrfResult = mockMvc.perform(get("/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode tokenResponse = objectMapper.readTree(
                csrfResult.getResponse().getContentAsString());
        String headerName = tokenResponse.get("headerName").asText();
        String token = tokenResponse.get("token").asText();
        String suffix = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/departments")
                        .session((org.springframework.mock.web.MockHttpSession)
                                csrfResult.getRequest().getSession(false))
                        .header(headerName, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"CSRF-" + suffix + "\"}"))
                .andExpect(status().isCreated());
    }

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
                        .with(csrf())
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
