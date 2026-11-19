
const API_URL = "/api/employees";
const DEPARTMENTS_API_URL = "/api/departments";

const form = document.getElementById("employeeForm");
const transferForm = document.getElementById("transferForm");
const employeeSearchForm = document.getElementById("employeeSearchForm");
let currentPage = 0;
let pageSize = 10;
let totalPages = 0;
let activeNameFilter = "";
let activeDepartmentFilter = "";

document.addEventListener("DOMContentLoaded", function() {
    loadEmployees(0);
    loadDepartments();
});

employeeSearchForm.addEventListener("submit", searchEmployees);
document.getElementById("clearFiltersButton").addEventListener(
    "click", clearEmployeeFilters);
document.getElementById("pageSize").addEventListener("change", function(event) {
    pageSize = Number(event.target.value);
    loadEmployees(0);
});
document.getElementById("previousPageButton").addEventListener("click", function() {
    if (currentPage > 0) {
        loadEmployees(currentPage - 1);
    }
});
document.getElementById("nextPageButton").addEventListener("click", function() {
    if (currentPage + 1 < totalPages) {
        loadEmployees(currentPage + 1);
    }
});

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
        loadEmployees(0);

    } catch (error) {
        showMessage(error.message);
    }
});


async function loadEmployees(page = currentPage) {
    try {
        const parameters = new URLSearchParams({
            page: String(page),
            size: String(pageSize)
        });
        if (activeNameFilter) {
            parameters.set("name", activeNameFilter);
        }
        if (activeDepartmentFilter) {
            parameters.set("departmentId", activeDepartmentFilter);
        }
        const response = await fetch(`${API_URL}/search?${parameters}`);

        if (!response.ok) {
            throw new Error("Unable to load employees");
        }

        const result = await response.json();
        currentPage = result.page;
        totalPages = result.totalPages;
        displayEmployees(result.content, result.totalElements);
        updatePagination();
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
        const departmentFilter = document.getElementById("departmentFilter");
        departmentFilter.replaceChildren(new Option("All departments", ""));
        departments.forEach(department => {
            const label = `${department.name} (ID: ${department.id})`;
            select.add(new Option(label, department.id));
            departmentFilter.add(new Option(label, department.id));
        });
    } catch (error) {
        showMessage(error.message);
    }
}


function displayEmployees(employees, matchingEmployees) {
    const table = document.getElementById("employeeTable");
    table.replaceChildren();

    document.getElementById("totalEmployees").textContent = matchingEmployees;
    const firstResult = matchingEmployees === 0
        ? 0
        : currentPage * pageSize + 1;
    const lastResult = Math.min((currentPage + 1) * pageSize, matchingEmployees);
    document.getElementById("employeeSummary").textContent =
        `Showing ${firstResult}-${lastResult} of ${matchingEmployees} employees`;

    employees.forEach(employee => {
        const row = document.createElement("tr");

        const values = [
            employee.id,
            `${employee.firstName} ${employee.lastName}`,
            employee.email,
            employee.departmentName || "",
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


function editEmployee(employee) {
    document.getElementById("employeeId").value = employee.id;
    document.getElementById("firstName").value = employee.firstName;
    document.getElementById("lastName").value = employee.lastName;
    document.getElementById("email").value = employee.email;
    document.getElementById("phone").value = employee.phone || "";
    document.getElementById("department").value = employee.departmentName || "";
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
        await loadEmployees(0);
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

        loadEmployees(0);

    } catch (error) {
        showMessage(error.message);
    }
}


function searchEmployees(event) {
    event.preventDefault();
    activeNameFilter = document.getElementById("searchInput").value.trim();
    activeDepartmentFilter =
        document.getElementById("departmentFilter").value;
    loadEmployees(0);
}

function clearEmployeeFilters() {
    document.getElementById("searchInput").value = "";
    document.getElementById("departmentFilter").value = "";
    activeNameFilter = "";
    activeDepartmentFilter = "";
    loadEmployees(0);
}

function updatePagination() {
    document.getElementById("pageInfo").textContent =
        `Page ${totalPages === 0 ? 0 : currentPage + 1} of ${totalPages}`;
    document.getElementById("previousPageButton").disabled =
        currentPage === 0 || totalPages === 0;
    document.getElementById("nextPageButton").disabled =
        currentPage + 1 >= totalPages;
}


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


function showMessage(message) {
    document.getElementById("message").textContent = message;
}
