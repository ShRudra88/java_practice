
const API_URL = "/api/employees";
const DEPARTMENTS_API_URL = "/api/departments";

const form = document.getElementById("employeeForm");
const transferForm = document.getElementById("transferForm");
const employeeSearchForm = document.getElementById("employeeSearchForm");
const employeeTable = document.getElementById("employeeTable");
let currentPage = 0;
let pageSize = 10;
let totalPages = 0;
let activeNameFilter = "";
let activeDepartmentFilter = "";

function apiFetch(url, options = {}) {
    const requestOptions = { ...options, credentials: "same-origin" };
    const method = (requestOptions.method || "GET").toUpperCase();
    if (["GET", "HEAD", "OPTIONS"].includes(method)) {
        return fetch(url, requestOptions);
    }

    return fetch("/csrf", { credentials: "same-origin" })
        .then(function(response) {
            if (!response.ok) {
                throw new Error("Unable to validate this request. Refresh and try again.");
            }
            return response.json();
        })
        .then(function(csrf) {
            const headers = new Headers(requestOptions.headers || {});
            headers.set(csrf.headerName, csrf.token);
            requestOptions.headers = headers;
            return fetch(url, requestOptions);
        });
}

document.addEventListener("DOMContentLoaded", function() {
    initializeLogoutForm();
    if (employeeTable) {
        loadEmployees(0);
        if (new URLSearchParams(window.location.search).has("updated")) {
            showMessage("Employee updated successfully!");
        }
    }
    loadDepartments().then(function() {
        if (form) {
            initializeEmployeeForm();
        }
    });

    function initializeLogoutForm() {
        const logoutToken = document.getElementById("logoutCsrfToken");
        if (!logoutToken) {
            return;
        }

        apiFetch("/csrf")
            .then(function(response) {
                if (!response.ok) {
                    throw new Error("Unable to prepare sign out.");
                }
                return response.json();
            })
            .then(function(csrf) {
                logoutToken.name = csrf.parameterName;
                logoutToken.value = csrf.token;
                logoutToken.form.querySelector("button").disabled = false;
            })
            .catch(function(error) {
                showMessage(error.message);
            });
    }
});

if (employeeSearchForm) {
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
}

if (form) {
    form.addEventListener("submit", submitEmployeeForm);
    document.getElementById("cancelButton").addEventListener("click", function() {
        resetForm();
        window.history.replaceState(null, "", "employee-registration.html");
    });
}

async function submitEmployeeForm(event) {
    event.preventDefault();

    const id = document.getElementById("employeeId").value;
    const departmentId = document.getElementById("department").value;
    const salaryValue = document.getElementById("salary").value;
    const employee = {
        firstName: document.getElementById("firstName").value.trim(),
        lastName: document.getElementById("lastName").value.trim(),
        email: document.getElementById("email").value.trim(),
        phone: document.getElementById("phone").value.trim(),
        department: departmentId ? { id: Number(departmentId) } : null,
        jobTitle: document.getElementById("jobTitle").value.trim(),
        salary: salaryValue === "" ? null : Number(salaryValue),
        hireDate: document.getElementById("hireDate").value || null,
        status: document.getElementById("status").value
    };

    try {
        const response = await apiFetch(
            id ? `${API_URL}/${encodeURIComponent(id)}` : API_URL, {
            method: id ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(employee)
        });

        if (!response.ok) {
            let errorMessage = "Unable to save employee";
            try {
                const error = await response.json();
                errorMessage = error.error || error.message || errorMessage;
            } catch (error) {
                errorMessage = `Unable to save employee (${response.status})`;
            }
            throw new Error(errorMessage);
        }

        showMessage(id ? "Employee updated successfully!" :
            "Employee registered successfully!");
        if (id) {
            window.location.href = "index.html?updated=1";
        } else {
            resetForm();
        }
    } catch (error) {
        showMessage(error.message);
    }
}

async function initializeEmployeeForm() {
    const id = new URLSearchParams(window.location.search).get("id");
    if (!id) {
        return;
    }

    try {
        const response = await apiFetch(`${API_URL}/${encodeURIComponent(id)}`);
        if (!response.ok) {
            throw new Error("Unable to load employee");
        }

        const employee = await response.json();
        document.getElementById("employeeId").value = employee.id;
        document.getElementById("firstName").value = employee.firstName;
        document.getElementById("lastName").value = employee.lastName;
        document.getElementById("email").value = employee.email;
        document.getElementById("phone").value = employee.phone || "";
        document.getElementById("department").value = employee.departmentId || "";
        document.getElementById("jobTitle").value = employee.jobTitle || "";
        document.getElementById("salary").value = employee.salary ?? "";
        document.getElementById("hireDate").value = employee.hireDate || "";
        document.getElementById("status").value = employee.status || "ACTIVE";
        document.getElementById("formTitle").textContent = "Update Employee";
        document.getElementById("submitButton").textContent = "Update Employee";
        document.getElementById("cancelButton").hidden = false;
    } catch (error) {
        showMessage(error.message);
    }
}


async function loadEmployees(page = currentPage) {
    try {
        const table = document.getElementById("employeeTable");
        table.replaceChildren();
        table.innerHTML =
            '<tr><td class="loading-row" colspan="8">Loading employees...</td></tr>';
        setPaginationLoading(true);
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
        const response = await apiFetch(`${API_URL}/search?${parameters}`);

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
        setPaginationLoading(false);
    }
}

async function loadDepartments() {
    try {
        const response = await apiFetch(DEPARTMENTS_API_URL);
        if (!response.ok) {
            throw new Error("Unable to load departments");
        }

        const departments = await response.json();
        const select = document.getElementById("transferDepartment");
        if (select) {
            select.replaceChildren(new Option("Select a department", ""));
        }
        const employeeDepartment = document.getElementById("department");
        if (employeeDepartment) {
            employeeDepartment.replaceChildren(new Option("No department", ""));
        }
        const departmentFilter = document.getElementById("departmentFilter");
        if (departmentFilter) {
            departmentFilter.replaceChildren(new Option("All departments", ""));
        }
        departments.forEach(department => {
            const label = `${department.name} (ID: ${department.id})`;
            if (select) {
                select.add(new Option(label, department.id));
            }
            if (employeeDepartment) {
                employeeDepartment.add(new Option(department.name, department.id));
            }
            if (departmentFilter) {
                departmentFilter.add(new Option(label, department.id));
            }
        });
    } catch (error) {
        showMessage(error.message);
    }
}


function displayEmployees(employees, matchingEmployees) {
    const table = document.getElementById("employeeTable");
    table.replaceChildren();
    setPaginationLoading(false);

    document.getElementById("totalEmployees").textContent = matchingEmployees;
    const firstResult = matchingEmployees === 0
        ? 0
        : currentPage * pageSize + 1;
    const lastResult = Math.min((currentPage + 1) * pageSize, matchingEmployees);
    document.getElementById("employeeSummary").textContent =
        `Showing ${firstResult}-${lastResult} of ${matchingEmployees} employees`;

    if (employees.length === 0) {
        const row = document.createElement("tr");
        const cell = document.createElement("td");
        cell.colSpan = 8;
        cell.className = "empty-row";
        cell.textContent = "No employees match the current filters.";
        row.appendChild(cell);
        table.appendChild(row);
        return;
    }

    employees.forEach(employee => {
        const row = document.createElement("tr");

        const values = [
            employee.id,
            `${employee.firstName} ${employee.lastName}`,
            employee.email,
            employee.departmentName || "",
            employee.jobTitle || "",
            formatSalary(employee.salary)
        ];

        values.forEach(value => {
            const cell = document.createElement("td");
            cell.textContent = value;
            row.appendChild(cell);
        });

        const statusCell = document.createElement("td");
        const status = employee.status || "INACTIVE";
        const statusBadge = document.createElement("span");
        statusBadge.className =
            `status-badge ${status === "ACTIVE" ? "status-active" : "status-inactive"}`;
        statusBadge.textContent =
            status.charAt(0) + status.slice(1).toLowerCase();
        statusCell.appendChild(statusBadge);
        row.appendChild(statusCell);

        const actionCell = document.createElement("td");
        actionCell.className = "action-cell";

        const editButton = document.createElement("button");
        editButton.textContent = "Edit";
        editButton.className = "edit-btn";
        editButton.type = "button";
        editButton.onclick = () => editEmployee(employee);

        const deleteButton = document.createElement("button");
        deleteButton.textContent = "Delete";
        deleteButton.className = "delete-btn";
        deleteButton.type = "button";
        deleteButton.onclick = () => deleteEmployee(employee.id);

        const transferButton = document.createElement("button");
        transferButton.textContent = "Transfer";
        transferButton.className = "transfer-btn";
        transferButton.type = "button";
        transferButton.onclick = () => openTransferForm(employee);

        const historyButton = document.createElement("button");
        historyButton.textContent = "History";
        historyButton.className = "history-btn";
        historyButton.type = "button";
        historyButton.onclick = () => loadDepartmentHistory(employee);

        actionCell.append(editButton, transferButton, historyButton, deleteButton);
        row.appendChild(actionCell);

        table.appendChild(row);
    });
}


function editEmployee(employee) {
    window.location.href =
        `employee-registration.html?id=${encodeURIComponent(employee.id)}`;
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

if (transferForm) {
    transferForm.addEventListener("submit", async function(event) {
        event.preventDefault();

        const employeeId = document.getElementById("transferEmployeeId").value;
        const request = {
            departmentId: Number(document.getElementById("transferDepartment").value),
            effectiveDate: document.getElementById("transferEffectiveDate").value
        };

        try {
            const response = await apiFetch(`${API_URL}/${employeeId}/transfers`, {
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

    document.getElementById("cancelTransferButton").addEventListener(
        "click", closeTransferForm);
}

async function loadDepartmentHistory(employee) {
    try {
        const response = await apiFetch(
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

async function deleteEmployee(id) {
    if (!confirm("Are you sure you want to delete this employee?")) {
        return;
    }

    try {

        const response = await apiFetch(`${API_URL}/${id}`, {
            method: "DELETE"
        });

        if (!response.ok) {
            throw new Error("Unable to delete employee");
        }

        showMessage("Employee deleted successfully!");
        const employeeRows = document.querySelectorAll("#employeeTable tr");
        const deletingOnlyPageRow =
            currentPage > 0 && employeeRows.length === 1;
        loadEmployees(deletingOnlyPageRow ? currentPage - 1 : currentPage);

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

function setPaginationLoading(isLoading) {
    document.getElementById("previousPageButton").disabled = isLoading;
    document.getElementById("nextPageButton").disabled = isLoading;
}

function formatSalary(salary) {
    if (salary === null || salary === undefined || salary === "") {
        return "Not set";
    }
    return new Intl.NumberFormat("en-US", {
        style: "currency",
        currency: "USD",
        maximumFractionDigits: 2
    }).format(Number(salary));
}


function resetForm() {
    form.reset();

    document.getElementById("employeeId").value = "";

    document.getElementById("formTitle").textContent =
        "Register Employee";

    document.getElementById("submitButton").textContent =
        "Register Employee";

    document.getElementById("cancelButton").hidden = true;
}


function showMessage(message) {
    document.getElementById("message").textContent = message;
}
