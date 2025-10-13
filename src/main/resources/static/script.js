// =========================
// 📌 script.js - FIXED VERSION
// =========================

const API_BASE = "http://localhost:8080/api";

document.addEventListener("DOMContentLoaded", () => {
    checkLogin(); // kiểm tra token hợp lệ
    loadTasks();
    document.getElementById("addTaskBtn").addEventListener("click", saveTask);
});

// ------------------- Kiểm tra login -------------------
function checkLogin() {
    const token = localStorage.getItem("token");
    if (!token) {
        alert("Bạn cần đăng nhập trước!");
        window.location.href = "login.html";
    }
}

// ------------------- Hàm tạo header có token -------------------
function getAuthHeaders() {
    const token = localStorage.getItem("token");
    return {
        "Content-Type": "application/json",
        "Authorization": token ? `Bearer ${token}` : ""
    };
}

let editId = null;

// ------------------- Load Tasks -------------------
async function loadTasks() {
    try {
        const res = await fetch(`${API_BASE}/tasks`, { headers: getAuthHeaders() });
        if (res.status === 403 || res.status === 401) {
            logout();
            return;
        }

        const tasks = await res.json();
        const tbody = document.getElementById("taskTableBody");
        tbody.innerHTML = "";

        tasks.forEach(task => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${task.id}</td>
                <td>${task.title}</td>
                <td>${task.description}</td>
                <td>${task.status}</td>
                <td>${task.user ? task.user.fullName || task.user.username : "Không có"}</td>
                <td>
                    <button onclick="editTask(${task.id})">Sửa</button>
                    <button onclick="deleteTask(${task.id})">Xóa</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        console.error("Lỗi loadTasks:", err);
    }
}

// ------------------- Thêm hoặc cập nhật Task -------------------
async function saveTask() {
    const title = document.getElementById("title").value.trim();
    const description = document.getElementById("description").value.trim();
    const status = document.getElementById("status").value;

    if (!title || !description) {
        alert("Vui lòng nhập tiêu đề và mô tả!");
        return;
    }

    // ❌ Không gửi user nữa - backend tự lấy từ token
    const taskData = { title, description, status };

    let method = "POST";
    let url = `${API_BASE}/tasks`;

    if (editId) {
        method = "PUT";
        url = `${API_BASE}/tasks/${editId}`;
    }

    try {
        const res = await fetch(url, {
            method,
            headers: getAuthHeaders(),
            body: JSON.stringify(taskData)
        });

        if (res.ok) {
            alert(editId ? "Cập nhật thành công!" : "Thêm Task thành công!");
            clearForm();
            loadTasks();
        } else {
            alert("❌ Lỗi khi lưu Task!");
        }
    } catch (err) {
        console.error("Lỗi saveTask:", err);
    }
}

// ------------------- Sửa Task -------------------
async function editTask(id) {
    const res = await fetch(`${API_BASE}/tasks/${id}`, { headers: getAuthHeaders() });
    if (res.ok) {
        const task = await res.json();
        document.getElementById("title").value = task.title;
        document.getElementById("description").value = task.description;
        document.getElementById("status").value = task.status;
        editId = task.id;
        document.getElementById("addTaskBtn").textContent = "Cập nhật Task";
    } else {
        alert("Không tải được Task!");
    }
}

// ------------------- Xóa Task -------------------
async function deleteTask(id) {
    if (!confirm("Bạn có chắc chắn muốn xóa task này không?")) return;

    const res = await fetch(`${API_BASE}/tasks/${id}`, {
        method: "DELETE",
        headers: getAuthHeaders()
    });

    if (res.ok) {
        alert("Xóa thành công!");
        loadTasks();
    } else {
        alert("Lỗi khi xóa Task!");
    }
}

// ------------------- Xóa form -------------------
function clearForm() {
    document.getElementById("title").value = "";
    document.getElementById("description").value = "";
    document.getElementById("status").value = "PENDING";
    editId = null;
    document.getElementById("addTaskBtn").textContent = "Thêm Task";
}

// ------------------- Logout -------------------
function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("currentUser");
    window.location.href = "login.html";
}
