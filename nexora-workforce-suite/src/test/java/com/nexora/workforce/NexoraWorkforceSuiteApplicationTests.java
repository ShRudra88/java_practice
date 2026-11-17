package com.nexora.workforce;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nexora.workforce.controller.DepartmentController;
import com.nexora.workforce.repository.DepartmentRepository;
import com.nexora.workforce.repository.EmployeeRepository;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootTest
class NexoraWorkforceSuiteApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

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
	}
}
