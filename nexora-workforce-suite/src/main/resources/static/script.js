
const API_URL = "/api/employees";
const DEPARTMENTS_API_URL = "/api/departments";

const form = document.getElementById("employeeForm");
const transferForm = document.getElementById("transferForm");

// Load employees when page opens
document.addEventListener("DOMContentLoaded", function() {
    loadEmployees();
    loadDepartments();
});

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

async function loadDepartments() {
    try {
        const response = await fetch(DEPARTMENTS_API_URL);
        if (!response.ok) {
            throw new Error("Unable to load departments");
        }

        const departments = await response.json();
        const select = document.getElementById("transferDepartment");
        select.replaceChildren(new Option("Select a department", ""));
        departments.forEach(department => {
            select.add(new Option(
                `${department.name} (ID: ${department.id})`, department.id));
        });
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
            employee.departmentName || employee.department?.name || employee.department || "",
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

        const transferButton = document.createElement("button");
        transferButton.textContent = "Transfer";
        transferButton.className = "transfer-btn";
        transferButton.onclick = () => openTransferForm(employee);

        const historyButton = document.createElement("button");
        historyButton.textContent = "History";
        historyButton.className = "history-btn";
        historyButton.onclick = () => loadDepartmentHistory(employee);

        actionCell.append(editButton, transferButton, historyButton, deleteButton);
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
        employee.departmentName || employee.department?.name || employee.department || "";
    document.getElementById("department").readOnly = true;
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

function openTransferForm(employee) {
    const now = new Date();
    const today = [
        now.getFullYear(),
        String(now.getMonth() + 1).padStart(2, "0"),
        String(now.getDate()).padStart(2, "0")
    ].join("-");
    document.getElementById("transferEmployeeId").value = employee.id;
    document.getElementById("transferEmployeeName").textContent =
        `Transfer ${employee.firstName} ${employee.lastName}`;
    document.getElementById("transferEffectiveDate").value = today;
    document.getElementById("transferEffectiveDate").max = today;
    document.getElementById("transferDepartment").value = "";
    document.getElementById("transferSection").hidden = false;
    document.getElementById("historySection").hidden = true;
    document.getElementById("transferSection").scrollIntoView({ behavior: "smooth" });
}

transferForm.addEventListener("submit", async function(event) {
    event.preventDefault();

    const employeeId = document.getElementById("transferEmployeeId").value;
    const request = {
        departmentId: Number(document.getElementById("transferDepartment").value),
        effectiveDate: document.getElementById("transferEffectiveDate").value
    };

    try {
        const response = await fetch(`${API_URL}/${employeeId}/transfers`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(request)
        });
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || "Unable to transfer employee");
        }

        showMessage("Employee transferred successfully!");
        closeTransferForm();
        await loadEmployees();
    } catch (error) {
        showMessage(error.message);
    }
});

async function loadDepartmentHistory(employee) {
    try {
        const response = await fetch(
            `${API_URL}/${employee.id}/department-history`
        );
        if (!response.ok) {
            throw new Error("Unable to load department history");
        }

        const history = await response.json();
        const table = document.getElementById("departmentHistoryTable");
        table.replaceChildren();
        history.forEach(entry => {
            const row = document.createElement("tr");
            [
                entry.effectiveDate,
                entry.fromDepartmentName || "Unassigned",
                entry.toDepartmentName || "Unassigned"
            ].forEach(value => {
                const cell = document.createElement("td");
                cell.textContent = value;
                row.appendChild(cell);
            });
            table.appendChild(row);
        });
        document.getElementById("historyTitle").textContent =
            `Department History - ${employee.firstName} ${employee.lastName}`;
        document.getElementById("historySection").hidden = false;
        document.getElementById("transferSection").hidden = true;
        document.getElementById("historySection").scrollIntoView({ behavior: "smooth" });
    } catch (error) {
        showMessage(error.message);
    }
}

function closeTransferForm() {
    transferForm.reset();
    document.getElementById("transferSection").hidden = true;
}

document.getElementById("cancelTransferButton").addEventListener(
    "click", closeTransferForm);


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
    document.getElementById("department").readOnly = false;

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
