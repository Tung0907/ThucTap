// ====================== script.js ======================
const API_BASE = "http://localhost:8080/api";
let currentUser = JSON.parse(localStorage.getItem("currentUser"));
let editId = null;

document.addEventListener("DOMContentLoaded", () => {
    if (!currentUser) {
        window.location.href = "login.html";
        return;
    }

    // Hiển thị phần người dùng nếu là admin
    if (currentUser.role === "ADMIN") {
        const userSection = document.getElementById("user-section");
        if (userSection) userSection.style.display = "block";
    }

    loadTasks();
    document.getElementById("addTaskBtn").addEventListener("click", saveTask);
});

// ======== Headers kèm JWT token ========
function getAuthHeaders() {
    const token = localStorage.getItem("token");
    return {
        "Content-Type": "application/json",
        "Authorization": token ? `Bearer ${token}` : ""
    };
}

// ======== Load danh sách Task ========
async function loadTasks() {
    try {
        const res = await fetch(`${API_BASE}/tasks`, { headers: getAuthHeaders() });
        if (!res.ok) {
            if (res.status === 401 || res.status === 403) {
                alert("Bạn cần đăng nhập hoặc không có quyền.");
                logout();
                return;
            }
            throw new Error("Lỗi khi lấy danh sách task: " + res.status);
        }

        const result = await res.json();  // sửa ở đây
        renderTaskTable(result.data);      // lấy mảng data

    } catch (err) {
        console.error("Lỗi loadTasks:", err);
        alert("Không thể tải danh sách task. Xem console để biết chi tiết.");
    }
}


// ======== Hiển thị bảng Task ========
function renderTaskTable(tasks) {
    const tbody = document.getElementById("taskTableBody");
    tbody.innerHTML = "";

    tasks.forEach(task => {
        const userLabel = task.userFullName || task.userName || "N/A";

        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${task.id}</td>
            <td>${escapeHtml(task.title)}</td>
            <td>${escapeHtml(task.description)}</td>
            <td>${escapeHtml(task.status)}</td>
            <td>${escapeHtml(userLabel)}</td>
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


// ======== Tạo mới hoặc Cập nhật Task ========
async function saveTask() {
    const title = document.getElementById("title").value.trim();
    const description = document.getElementById("description").value.trim();
    const status = document.getElementById("status").value;

    if (!title || !description) {
        alert("Vui lòng nhập đủ tiêu đề và mô tả!");
        return;
    }

    const payload = { title, description, status };
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
            loadTasks();
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

// ======== Sửa Task ========
async function editTask(id) {
    try {
        const res = await fetch(`${API_BASE}/tasks/${id}`, { headers: getAuthHeaders() });
        if (!res.ok) {
            alert("Không tải được Task (mã lỗi: " + res.status + ")");
            return;
        }

        const result = await res.json();
        const task = result.data; //  lấy đúng phần data

        document.getElementById("title").value = task.title || "";
        document.getElementById("description").value = task.description || "";
        document.getElementById("status").value = task.status || "PENDING";
        editId = id;
        document.getElementById("addTaskBtn").textContent = "Cập nhật Task";

    } catch (err) {
        console.error("Lỗi editTask:", err);
        alert("Không thể tải Task để chỉnh sửa.");
    }
}


// ======== Xóa Task ========
async function deleteTask(id) {
    if (!confirm("Bạn có chắc chắn muốn xóa Task này?")) return;

    try {
        const res = await fetch(`${API_BASE}/tasks/${id}`, {
            method: "DELETE",
            headers: getAuthHeaders()
        });

        if (res.ok) {
            loadTasks();
        } else {
            const text = await res.text();
            console.error("Lỗi deleteTask:", res.status, text);
            alert("Không thể xóa Task (mã lỗi: " + res.status + ")");
        }
    } catch (err) {
        console.error("Lỗi deleteTask:", err);
        alert("Lỗi khi xóa Task. Xem console để biết thêm chi tiết.");
    }
}

// ======== Xóa form sau khi lưu ========
function clearForm() {
    document.getElementById("title").value = "";
    document.getElementById("description").value = "";
    document.getElementById("status").value = "PENDING";
    editId = null;
    document.getElementById("addTaskBtn").textContent = "Thêm Task";
}

// ======== Admin: Tải danh sách người dùng ========
async function loadUsers() {
    try {
        const res = await fetch(`${API_BASE}/admin/users`, { headers: getAuthHeaders() });
        if (!res.ok) {
            alert("Không thể tải danh sách người dùng (mã lỗi: " + res.status + ")");
            return;
        }

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
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        console.error("Lỗi loadUsers:", err);
        alert("Không thể tải danh sách người dùng.");
    }
}

// ======== Đăng xuất ========
function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("currentUser");
    window.location.href = "login.html";
}

// ======== Helper: escape HTML tránh XSS ========
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
