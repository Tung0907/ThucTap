// =================== CONFIG ===================
const API_BASE = "http://localhost:8080/api";
let currentUser = JSON.parse(localStorage.getItem("currentUser"));
let editId = null;
let currentPage = 0;
let totalPages = 0;
let filterStatus = "";
let sortBy = "createdAt";
let sortDir = "asc";

// =================== INIT ===================
document.addEventListener("DOMContentLoaded", () => {
    if (!currentUser) {
        window.location.href = "login.html";
        return;
    }

    if (currentUser.role === "ADMIN") {
        const userSection = document.getElementById("user-section");
        if (userSection) userSection.style.display = "block";
        loadUsers();
    }

    loadTasks();
    document.getElementById("addTaskBtn").addEventListener("click", saveTask);
});

// =================== AUTH HEADER ===================
function getAuthHeaders() {
    const token = localStorage.getItem("token");
    return {
        "Content-Type": "application/json",
        "Authorization": token ? `Bearer ${token}` : ""
    };
}

// =================== LOAD TASK (PHÂN TRANG, LỌC, SẮP XẾP) ===================
async function loadTasks(page = 0) {
    try {
        const query = new URLSearchParams({
            page,
            size: 5,
            status: filterStatus,
            sortBy,
            direction: sortDir
        });

        // ✅ Đã sửa: gọi đúng endpoint /paging
        const res = await fetch(`${API_BASE}/tasks/paging?${query}`, { headers: getAuthHeaders() });

        if (!res.ok) {
            if (res.status === 401 || res.status === 403) {
                alert("Bạn cần đăng nhập hoặc không có quyền.");
                logout();
                return;
            }
            throw new Error("Lỗi khi lấy danh sách task: " + res.status);
        }

        const result = await res.json();
        const pageData = result.data || result; // Phòng khi backend trả trực tiếp Page<>
        renderTaskTable(pageData.content || []);
        renderPagination(pageData.totalPages || 0, pageData.number || 0);
    } catch (err) {
        console.error("Lỗi loadTasks:", err);
        alert("Không thể tải danh sách task. Xem console để biết chi tiết.");
    }
}

// =================== HIỂN THỊ TASK TABLE ===================
function renderTaskTable(tasks) {
    const tbody = document.getElementById("taskTableBody");
    tbody.innerHTML = "";

    if (!tasks || tasks.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;">Không có dữ liệu</td></tr>`;
        return;
    }

    tasks.forEach(task => {
        const userLabel = task.userFullName || task.userName || "N/A";

        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${task.id}</td>
            <td>${escapeHtml(task.title)}</td>
            <td>${escapeHtml(task.description)}</td>
            <td>${escapeHtml(task.status)}</td>
            <td>${escapeHtml(userLabel)}</td>
            <td>${task.createdAt ? new Date(task.createdAt).toLocaleString() : "-"}</td>
            <td>${task.deadline ? new Date(task.deadline).toLocaleDateString() : "-"}</td>
            <td>
                <button class="btn-edit" data-id="${task.id}">✏️</button>
                <button class="btn-delete" data-id="${task.id}">🗑️</button>
            </td>
        `;
        tbody.appendChild(tr);
    });

    document.querySelectorAll(".btn-edit").forEach(b =>
        b.addEventListener("click", e => editTask(e.target.dataset.id))
    );

    document.querySelectorAll(".btn-delete").forEach(b =>
        b.addEventListener("click", e => deleteTask(e.target.dataset.id))
    );
}

// =================== PHÂN TRANG ===================
function renderPagination(total, current) {
    totalPages = total;
    currentPage = current;
    const container = document.getElementById("pagination");
    container.innerHTML = "";

    for (let i = 0; i < total; i++) {
        const btn = document.createElement("button");
        btn.textContent = i + 1;
        btn.disabled = i === current;
        btn.onclick = () => loadTasks(i);
        container.appendChild(btn);
    }
}

// =================== LỌC & SẮP XẾP ===================
function applyFilter() {
    filterStatus = document.getElementById("filterStatus").value;
    sortBy = document.getElementById("sortBy").value;
    sortDir = document.getElementById("sortDir").value;
    loadTasks(0);
}

// =================== THÊM / SỬA TASK ===================
async function saveTask() {
    const title = document.getElementById("title").value.trim();
    const description = document.getElementById("description").value.trim();
    const status = document.getElementById("status").value;
    const deadline = document.getElementById("deadline").value;

    if (!title || !description) {
        alert("Vui lòng nhập đủ tiêu đề và mô tả!");
        return;
    }

    const payload = { title, description, status, deadline };
    const method = editId ? "PUT" : "POST";
    const url = editId ? `${API_BASE}/tasks/${editId}` : `${API_BASE}/tasks`;

    try {
        const res = await fetch(url, {
            method,
            headers: getAuthHeaders(),
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            clearForm();
            loadTasks(currentPage);
            alert(editId ? "Cập nhật Task thành công!" : "Thêm Task mới thành công!");
        } else {
            const text = await res.text();
            console.error("Lỗi saveTask:", res.status, text);
            alert("Không thể lưu Task (mã lỗi: " + res.status + ")");
        }
    } catch (err) {
        console.error("Lỗi saveTask:", err);
        alert("Lỗi khi lưu Task. Xem console để biết thêm chi tiết.");
    }
}

// =================== CHỈNH SỬA TASK ===================
async function editTask(id) {
    try {
        const res = await fetch(`${API_BASE}/tasks/${id}`, { headers: getAuthHeaders() });
        const result = await res.json();
        const task = result.data;

        document.getElementById("title").value = task.title || "";
        document.getElementById("description").value = task.description || "";
        document.getElementById("status").value = task.status || "PENDING";
        document.getElementById("deadline").value = task.deadline ? task.deadline.substring(0, 10) : "";

        editId = id;
        document.getElementById("addTaskBtn").textContent = "Cập nhật Task";
    } catch (err) {
        console.error("Lỗi editTask:", err);
        alert("Không thể tải Task để chỉnh sửa.");
    }
}

// =================== XÓA TASK ===================
async function deleteTask(id) {
    if (!confirm("Bạn có chắc chắn muốn xóa Task này?")) return;

    try {
        const res = await fetch(`${API_BASE}/tasks/${id}`, {
            method: "DELETE",
            headers: getAuthHeaders()
        });

        if (res.ok) loadTasks(currentPage);
        else alert("Không thể xóa Task!");
    } catch (err) {
        console.error("Lỗi deleteTask:", err);
        alert("Lỗi khi xóa Task.");
    }
}

// =================== RESET FORM ===================
function clearForm() {
    document.getElementById("title").value = "";
    document.getElementById("description").value = "";
    document.getElementById("status").value = "PENDING";
    document.getElementById("deadline").value = "";
    editId = null;
    document.getElementById("addTaskBtn").textContent = "Thêm Task";
}

// =================== QUẢN LÝ NGƯỜI DÙNG (ADMIN) ===================
async function loadUsers() {
    try {
        const res = await fetch(`${API_BASE}/users`, { headers: getAuthHeaders() });
        const users = await res.json();
        const tbody = document.querySelector("#userTable tbody");
        tbody.innerHTML = "";

        users.forEach(u => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${u.id}</td>
                <td>${escapeHtml(u.username || "")}</td>
                <td>${escapeHtml(u.fullName || "")}</td>
                <td>${escapeHtml(u.email || "")}</td>
                <td>${escapeHtml(u.role || "USER")}</td>
                <td>
                    <button class="btn-edit" onclick="editUser(${u.id})">✏️</button>
                    <button class="btn-delete" onclick="deleteUser(${u.id})">🗑️</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        console.error("Lỗi loadUsers:", err);
    }
}

async function editUser(id) {
    const fullName = prompt("Nhập họ tên mới:");
    const email = prompt("Nhập email mới:");
    const role = prompt("Nhập vai trò (USER / ADMIN):");
    if (!fullName || !email || !role) return;

    await fetch(`${API_BASE}/users/${id}`, {
        method: "PUT",
        headers: getAuthHeaders(),
        body: JSON.stringify({ fullName, email, role })
    });
    loadUsers();
}

async function deleteUser(id) {
    if (!confirm("Bạn có chắc muốn xóa người dùng này không?")) return;
    await fetch(`${API_BASE}/users/${id}`, { method: "DELETE", headers: getAuthHeaders() });
    loadUsers();
}

// =================== ĐĂNG XUẤT ===================
function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("currentUser");
    window.location.href = "login.html";
}

// =================== ESCAPE HTML ===================
function escapeHtml(text) {
    if (!text && text !== 0) return "";
    return String(text).replace(/[&<>"']/g, c => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    }[c]));
}
