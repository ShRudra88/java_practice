
// Select the form and result container
const employeeForm = document.getElementById("employeeForm");
const employeeResult = document.getElementById("employeeResult");

// Handle form submission
employeeForm.addEventListener("submit", function(event) {

    // Prevent page reload
    event.preventDefault();

    // Get input values
    const employeeId = document.getElementById("employeeId").value.trim();
    const employeeName = document.getElementById("employeeName").value.trim();
    const email = document.getElementById("email").value.trim();
    const department = document.getElementById("department").value;
    const salary = Number(document.getElementById("salary").value);

    // Validate information
    if (!employeeId || !employeeName || !email || !department || salary < 0) {
        alert("Please enter valid employee information.");
        return;
    }

    // Create employee object
    const employee = {
        id: employeeId,
        name: employeeName,
        email: email,
        department: department,
        salary: salary
    };

    // Display employee information
    employeeResult.replaceChildren();

    const card = document.createElement("div");
    card.className = "employee-card";

    const heading = document.createElement("h3");
    heading.textContent = "Employee Information";

    const details = document.createElement("p");
    details.textContent =
        `Employee ID: ${employee.id}
         | Name: ${employee.name}
         | Email: ${employee.email}
         | Department: ${employee.department}
         | Salary: ${employee.salary}`;

    card.append(heading, details);
    employeeResult.appendChild(card);

    // Clear form
    employeeForm.reset();

    console.log(employee);

});
