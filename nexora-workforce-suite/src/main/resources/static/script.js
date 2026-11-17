
const API_URL = "/api/employees";

const form = document.getElementById("employeeForm");

// Load employees when page opens
document.addEventListener("DOMContentLoaded", loadEmployees);

// CREATE OR UPDATE
form.addEventListener("submit", async function(event) {

    event.preventDefault();

    const id = document.getElementById("employeeId").value;

    const employee = {
        firstName: document.getElementById("firstName").value,
        lastName: document.getElementById("lastName").value,
        email: document.getElementById("email").value,
        phone: document.getElementById("phone").value,
        department: document.getElementById("department").value,
        jobTitle: document.getElementById("jobTitle").value,
        salary: Number(document.getElementById("salary").value || 0),
        hireDate: document.getElementById("hireDate").value || null,
        status: document.getElementById("status").value
    };

    const method = id ? "PUT" : "POST";

    const url = id ? `${API_URL}/${id}` : API_URL;

    try {

        const response = await fetch(url, {
            method: method,
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(employee)
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || "Request failed");
        }

        showMessage(
            id ? "Employee updated successfully!" :
                 "Employee added successfully!"
        );

        resetForm();
        loadEmployees();

    } catch (error) {
        showMessage(error.message);
    }
});


// READ ALL EMPLOYEES
async function loadEmployees() {

    try {

        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error("Unable to load employees");
        }

        const employees = await response.json();

        displayEmployees(employees);

    } catch (error) {
        showMessage(error.message);
    }
}


// DISPLAY EMPLOYEES
function displayEmployees(employees) {

    const table = document.getElementById("employeeTable");

    table.innerHTML = "";

    document.getElementById("totalEmployees").textContent =
        employees.length;

    employees.forEach(employee => {

        const row = document.createElement("tr");

        const values = [
            employee.id,
            `${employee.firstName} ${employee.lastName}`,
            employee.email,
            employee.department?.name || employee.department || "",
            employee.jobTitle || "",
            employee.salary ?? "",
            employee.status || ""
        ];

        values.forEach(value => {
            const cell = document.createElement("td");
            cell.textContent = value;
            row.appendChild(cell);
        });

        const actionCell = document.createElement("td");

        const editButton = document.createElement("button");
        editButton.textContent = "Edit";
        editButton.className = "edit-btn";
        editButton.onclick = () => editEmployee(employee);

        const deleteButton = document.createElement("button");
        deleteButton.textContent = "Delete";
        deleteButton.className = "delete-btn";
        deleteButton.onclick = () => deleteEmployee(employee.id);

        actionCell.append(editButton, deleteButton);
        row.appendChild(actionCell);

        table.appendChild(row);
    });
}


// GET EMPLOYEE FOR EDITING
function editEmployee(employee) {

    document.getElementById("employeeId").value = employee.id;
    document.getElementById("firstName").value = employee.firstName;
    document.getElementById("lastName").value = employee.lastName;
    document.getElementById("email").value = employee.email;
    document.getElementById("phone").value = employee.phone || "";
    document.getElementById("department").value =
        employee.department?.name || employee.department || "";
    document.getElementById("jobTitle").value = employee.jobTitle || "";
    document.getElementById("salary").value = employee.salary ?? "";
    document.getElementById("hireDate").value = employee.hireDate || "";
    document.getElementById("status").value = employee.status || "ACTIVE";

    document.getElementById("formTitle").textContent =
        "Update Employee";

    document.getElementById("submitButton").textContent =
        "Update Employee";

    document.getElementById("cancelButton").hidden = false;

    window.scrollTo({ top: 0, behavior: "smooth" });
}


// DELETE EMPLOYEE
async function deleteEmployee(id) {

    if (!confirm("Are you sure you want to delete this employee?")) {
        return;
    }

    try {

        const response = await fetch(`${API_URL}/${id}`, {
            method: "DELETE"
        });

        if (!response.ok) {
            throw new Error("Unable to delete employee");
        }

        showMessage("Employee deleted successfully!");

        loadEmployees();

    } catch (error) {
        showMessage(error.message);
    }
}


// SEARCH EMPLOYEES
async function searchEmployees() {

    const name = document.getElementById("searchInput").value.trim();

    if (!name) {
        loadEmployees();
        return;
    }

    try {

        const response = await fetch(
            `${API_URL}/search?name=${encodeURIComponent(name)}`
        );

        if (!response.ok) {
            throw new Error("Search failed");
        }

        const employees = await response.json();

        displayEmployees(employees);

    } catch (error) {
        showMessage(error.message);
    }
}


// RESET FORM
function resetForm() {

    form.reset();

    document.getElementById("employeeId").value = "";

    document.getElementById("formTitle").textContent =
        "Add New Employee";

    document.getElementById("submitButton").textContent =
        "Add Employee";

    document.getElementById("cancelButton").hidden = true;
}


// DISPLAY MESSAGE
function showMessage(message) {

    document.getElementById("message").textContent = message;
}
