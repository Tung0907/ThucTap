const API_BASE = "http://localhost:8080/api";

document.addEventListener("DOMContentLoaded", () => {
    loadUsers();
    loadTasks();

    document.getElementById("addTaskBtn").addEventListener("click", saveTask);
});

let editId = null; // để biết đang sửa task nào

async function loadUsers() {
    const res = await fetch(`${API_BASE}/users`);
    const users = await res.json();
    const userSelect = document.getElementById("user");
    userSelect.innerHTML = "";
    users.forEach(u => {
        const option = document.createElement("option");
        option.value = u.id;
        option.textContent = `${u.fullName} (${u.username})`;
        userSelect.appendChild(option);
    });
}

async function loadTasks() {
    const res = await fetch(`${API_BASE}/tasks`);
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
      <td>${task.user ? task.user.fullName : "Không có"}</td>
      <td>
        <button onclick="editTask(${task.id})">Sửa</button>
        <button onclick="deleteTask(${task.id})">Xóa</button>
      </td>
    `;
        tbody.appendChild(tr);
    });
}

// ------------------- Thêm hoặc cập nhật Task -------------------
async function saveTask() {
    const title = document.getElementById("title").value.trim();
    const description = document.getElementById("description").value.trim();
    const status = document.getElementById("status").value;
    const userId = document.getElementById("user").value;

    if (!title || !description) {
        alert("Vui lòng nhập tiêu đề và mô tả!");
        return;
    }

    const taskData = {
        title,
        description,
        status,
        user: { id: parseInt(userId) }
    };

    let method = "POST";
    let url = `${API_BASE}/tasks`;

    if (editId) {
        method = "PUT";
        url = `${API_BASE}/tasks/${editId}`;
    }

    const res = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(taskData)
    });

    if (res.ok) {
        alert(editId ? "Cập nhật thành công!" : "Thêm Task thành công!");
        document.getElementById("title").value = "";
        document.getElementById("description").value = "";
        document.getElementById("status").value = "PENDING";
        editId = null;
        document.getElementById("addTaskBtn").textContent = "Thêm Task";
        loadTasks();
    } else {
        alert("Lỗi khi lưu Task!");
    }
}

// ------------------- Xóa Task -------------------
async function deleteTask(id) {
    if (!confirm("Bạn có chắc chắn muốn xóa task này không?")) return;

    const res = await fetch(`${API_BASE}/tasks/${id}`, { method: "DELETE" });
    if (res.ok) {
        alert("Xóa thành công!");
        loadTasks();
    } else {
        alert("Lỗi khi xóa Task!");
    }
}

// ------------------- Sửa Task -------------------
async function editTask(id) {
    const res = await fetch(`${API_BASE}/tasks/${id}`);
    const task = await res.json();

    document.getElementById("title").value = task.title;
    document.getElementById("description").value = task.description;
    document.getElementById("status").value = task.status;
    document.getElementById("user").value = task.user ? task.user.id : "";

    editId = task.id;
    document.getElementById("addTaskBtn").textContent = "Cập nhật Task";
}
// Xử lý đăng ký
const registerForm = document.getElementById('registerForm');
if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        const fullName = document.getElementById('fullName').value;
        const email = document.getElementById('email').value;

        const newUser = {
            username,
            password,
            fullName,
            email,
            role: 'USER'
        };

        const res = await fetch('/api/users/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newUser)
        });

        if (res.ok) {
            alert('Đăng ký thành công! Mời bạn đăng nhập.');
            window.location.href = 'login.html';
        } else {
            alert('Tên đăng nhập đã tồn tại!');
        }
    });
}

