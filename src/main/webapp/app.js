/**
 * STUDENT HUB - APPLICATION LOGIC (Database API Connected)
 * Features: SPA Tab Navigation, Form Validation, AJAX Fetch CRUD, Search, Custom Toast
 */

document.addEventListener('DOMContentLoaded', () => {
  // --- DOM Elements ---
  const tabButtons = document.querySelectorAll('.nav-tab');
  const tabPanes = document.querySelectorAll('.tab-pane');
  const studentForm = document.getElementById('student-form');
  const studentTable = document.getElementById('student-table');
  const studentTbody = document.getElementById('student-tbody');
  const studentCountBadge = document.getElementById('student-count');
  const emptyState = document.getElementById('empty-state');
  const searchInput = document.getElementById('search-input');
  
  // Navigation shortcuts
  const emptyAddBtn = document.getElementById('empty-add-btn');

  // Edit Modal Elements
  const editModal = document.getElementById('edit-modal');
  const editForm = document.getElementById('edit-form');
  const closeModelBtn = document.getElementById('close-modal-btn');
  const cancelEditBtn = document.getElementById('cancel-edit-btn');
  const editIndexInput = document.getElementById('edit-index'); // Stores the DB ID of the student

  // Input fields for validation mapping
  const formFields = {
    fullName: {
      input: document.getElementById('fullName'),
      error: document.getElementById('fullName-error'),
      validate: (val) => val.trim().length >= 2
    },
    studentId: {
      input: document.getElementById('studentId'),
      error: document.getElementById('studentId-error'),
      validate: (val) => /^[a-zA-Z0-9]{4,15}$/.test(val.trim())
    },
    className: {
      input: document.getElementById('className'),
      error: document.getElementById('className-error'),
      validate: (val) => val.trim().length >= 2
    },
    email: {
      input: document.getElementById('email'),
      error: document.getElementById('email-error'),
      validate: (val) => val.trim() === "" || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val.trim())
    }
  };

  const editFields = {
    fullName: {
      input: document.getElementById('edit-fullName'),
      error: document.getElementById('edit-fullName-error'),
      validate: (val) => val.trim().length >= 2
    },
    studentId: {
      input: document.getElementById('edit-studentId'),
      error: document.getElementById('edit-studentId-error'),
      validate: (val) => /^[a-zA-Z0-9]{4,15}$/.test(val.trim())
    },
    className: {
      input: document.getElementById('edit-className'),
      error: document.getElementById('edit-className-error'),
      validate: (val) => val.trim().length >= 2
    },
    email: {
      input: document.getElementById('edit-email'),
      error: document.getElementById('edit-email-error'),
      validate: (val) => val.trim() === "" || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val.trim())
    }
  };

  // --- State ---
  let students = [];
  const API_URL = 'api/students';

  // --- Helper Functions ---

  // Fetch students from the Java Servlet API
  const fetchStudents = async () => {
    try {
      const response = await fetch(API_URL);
      if (!response.ok) {
        throw new Error('Không thể lấy danh sách sinh viên từ server.');
      }
      students = await response.json();
      studentCountBadge.textContent = students.length;
      renderStudents(searchInput.value);
    } catch (err) {
      console.error(err);
      showToast('Lỗi kết nối đến máy chủ: ' + err.message, 'error');
    }
  };
  
  // Switch tabs
  const switchTab = (targetTabId) => {
    tabButtons.forEach(btn => {
      if (btn.getAttribute('data-target') === targetTabId) {
        btn.classList.add('active');
      } else {
        btn.classList.remove('active');
      }
    });

    tabPanes.forEach(pane => {
      if (pane.id === targetTabId) {
        pane.classList.add('active');
      } else {
        pane.classList.remove('active');
      }
    });

    // If switching to list, refresh and focus search
    if (targetTabId === 'tab-list') {
      fetchStudents();
      setTimeout(() => searchInput.focus(), 150);
    }
  };

  // Show customized floating toast alerts
  const showToast = (message, type = 'success') => {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;

    let iconSvg = '';
    if (type === 'success') {
      iconSvg = `<svg class="toast-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>`;
    } else if (type === 'error') {
      iconSvg = `<svg class="toast-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>`;
    } else {
      iconSvg = `<svg class="toast-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>`;
    }

    toast.innerHTML = `
      ${iconSvg}
      <span class="toast-msg">${message}</span>
    `;

    container.appendChild(toast);

    // Auto fadeout and delete toast
    setTimeout(() => {
      toast.classList.add('fade-out');
      toast.addEventListener('animationend', () => {
        toast.remove();
      });
    }, 3200);
  };

  // Validate individual input group
  const validateField = (fieldObj) => {
    const value = fieldObj.input.value;
    const isValid = fieldObj.validate(value);
    const group = fieldObj.input.closest('.input-group');
    
    if (isValid) {
      group.classList.remove('invalid');
    } else {
      group.classList.add('invalid');
    }
    return isValid;
  };

  // Clear validation styling
  const clearValidation = (fields) => {
    Object.values(fields).forEach(f => {
      f.input.closest('.input-group').classList.remove('invalid');
    });
  };

  // --- CRUD Operations ---

  // Render Table rows
  const renderStudents = (filterQuery = '') => {
    studentTbody.innerHTML = '';
    const query = filterQuery.trim().toLowerCase();
    
    const filtered = students.filter(student => 
      student.fullName.toLowerCase().includes(query) || 
      student.studentId.toLowerCase().includes(query)
    );

    if (filtered.length === 0) {
      studentTable.style.display = 'none';
      emptyState.style.display = 'flex';
      return;
    }

    studentTable.style.display = 'table';
    emptyState.style.display = 'none';

    filtered.forEach((student, index) => {
      const tr = document.createElement('tr');
      tr.className = 'fade-in-row';
      tr.innerHTML = `
        <td>${index + 1}</td>
        <td><strong>${escapeHTML(student.studentId)}</strong></td>
        <td>${escapeHTML(student.fullName)}</td>
        <td>${escapeHTML(student.className)}</td>
        <td>${escapeHTML(student.email)}</td>
        <td>
          <div class="actions-cell">
            <button class="action-btn edit" data-id="${student.id}" title="Sửa thông tin">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
            </button>
            <button class="action-btn delete" data-id="${student.id}" title="Xóa sinh viên">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </td>
      `;
      studentTbody.appendChild(tr);
    });
  };

  // Helper to prevent XSS injection
  const escapeHTML = (str) => {
    if (!str) return '';
    return str.replace(/[&<>'"]/g, 
      tag => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        "'": '&#39;',
        '"': '&quot;'
      }[tag] || tag)
    );
  };

  // Add new student (POST API)
  studentForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    // Trigger validation on all fields
    let isFormValid = true;
    Object.values(formFields).forEach(field => {
      const isValid = validateField(field);
      if (!isValid) isFormValid = false;
    });

    if (!isFormValid) {
      showToast('Vui lòng sửa các lỗi nhập liệu trong biểu mẫu!', 'error');
      return;
    }

    const newStudent = {
      fullName: formFields.fullName.input.value.trim(),
      studentId: formFields.studentId.input.value.trim().toUpperCase(),
      className: formFields.className.input.value.trim(),
      email: formFields.email.input.value.trim()
    };

    try {
      const response = await fetch(API_URL, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json; charset=utf-8'
        },
        body: JSON.stringify(newStudent)
      });

      const result = await response.json();

      if (response.ok) {
        showToast(`Đã thêm thành công sinh viên: ${newStudent.fullName}`);
        studentForm.reset();
        clearValidation(formFields);
        
        // Auto switch to list tab
        setTimeout(() => {
          switchTab('tab-list');
        }, 600);
      } else {
        // Show error message returned by server (e.g. duplicate MSSV)
        showToast(result.error || 'Lỗi thêm sinh viên.', 'error');
        if (result.error && result.error.includes('MSSV')) {
          formFields.studentId.input.closest('.input-group').classList.add('invalid');
        }
      }
    } catch (err) {
      console.error(err);
      showToast('Không thể kết nối đến server để lưu dữ liệu.', 'error');
    }
  });

  // Handle Edit/Delete button clicks in table (Event delegation)
  studentTbody.addEventListener('click', (e) => {
    const btn = e.target.closest('.action-btn');
    if (!btn) return;

    const id = parseInt(btn.getAttribute('data-id'), 10);
    
    if (btn.classList.contains('edit')) {
      openEditModal(id);
    } else if (btn.classList.contains('delete')) {
      confirmDeleteStudent(id, btn.closest('tr'));
    }
  });

  // Delete student with confirmation and fade-out row transition (DELETE API)
  const confirmDeleteStudent = async (id, rowElement) => {
    const student = students.find(s => s.id === id);
    if (!student) return;

    if (confirm(`Bạn có chắc chắn muốn xóa sinh viên "${student.fullName}" (MSSV: ${student.studentId}) không?`)) {
      try {
        const response = await fetch(`${API_URL}?id=${id}`, {
          method: 'DELETE'
        });

        const result = await response.json();

        if (response.ok) {
          rowElement.classList.remove('fade-in-row');
          rowElement.classList.add('fade-out-row');
          
          // Wait for fadeout animation to finish
          rowElement.addEventListener('animationend', () => {
            fetchStudents(); // Refresh database state
            showToast(`Đã xóa sinh viên ${student.fullName} thành công.`, 'info');
          });
        } else {
          showToast(result.error || 'Không thể xóa sinh viên.', 'error');
        }
      } catch (err) {
        console.error(err);
        showToast('Lỗi khi gửi yêu cầu xóa đến máy chủ.', 'error');
      }
    }
  };

  // Open Edit Dialog modal
  const openEditModal = (id) => {
    const student = students.find(s => s.id === id);
    if (!student) return;

    editIndexInput.value = student.id; // Store the DB auto-increment ID
    
    // Populate form
    editFields.fullName.input.value = student.fullName;
    editFields.studentId.input.value = student.studentId;
    editFields.className.input.value = student.className;
    editFields.email.input.value = student.email;

    clearValidation(editFields);
    editModal.classList.add('open');
  };

  // Close Edit Dialog modal
  const closeEditModal = () => {
    editModal.classList.remove('open');
    editForm.reset();
  };

  // Save changes (PUT API)
  editForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = parseInt(editIndexInput.value, 10);

    // Validate edit fields
    let isFormValid = true;
    Object.values(editFields).forEach(field => {
      const isValid = validateField(field);
      if (!isValid) isFormValid = false;
    });

    if (!isFormValid) {
      showToast('Vui lòng sửa các lỗi nhập liệu trong biểu mẫu!', 'error');
      return;
    }

    const updatedStudent = {
      id: id,
      fullName: editFields.fullName.input.value.trim(),
      studentId: editFields.studentId.input.value.trim().toUpperCase(),
      className: editFields.className.input.value.trim(),
      email: editFields.email.input.value.trim()
    };

    try {
      const response = await fetch(API_URL, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json; charset=utf-8'
        },
        body: JSON.stringify(updatedStudent)
      });

      const result = await response.json();

      if (response.ok) {
        closeEditModal();
        fetchStudents(); // Reload list from backend
        showToast(`Đã cập nhật thông tin sinh viên ${updatedStudent.fullName} thành công!`);
      } else {
        showToast(result.error || 'Lỗi cập nhật sinh viên.', 'error');
        if (result.error && result.error.includes('MSSV')) {
          editFields.studentId.input.closest('.input-group').classList.add('invalid');
        }
      }
    } catch (err) {
      console.error(err);
      showToast('Không thể kết nối đến server để lưu dữ liệu.', 'error');
    }
  });

  // --- Event Listeners ---

  // Tab switching clicks
  tabButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const target = btn.getAttribute('data-target');
      switchTab(target);
    });
  });

  // Tab shortcut buttons
  emptyAddBtn.addEventListener('click', () => switchTab('tab-form'));

  // Search input typing
  searchInput.addEventListener('input', (e) => {
    renderStudents(e.target.value);
  });

  // Real-time Input validation listeners (blur event)
  Object.values(formFields).forEach(field => {
    field.input.addEventListener('blur', () => validateField(field));
    field.input.addEventListener('input', () => {
      field.input.closest('.input-group').classList.remove('invalid');
    });
  });

  Object.values(editFields).forEach(field => {
    field.input.addEventListener('blur', () => validateField(field));
    field.input.addEventListener('input', () => {
      field.input.closest('.input-group').classList.remove('invalid');
    });
  });

  // Close modal clicks
  closeModelBtn.addEventListener('click', closeEditModal);
  cancelEditBtn.addEventListener('click', closeEditModal);
  
  // Close modal when clicking outside modal-card content
  editModal.addEventListener('click', (e) => {
    if (e.target === editModal) {
      closeEditModal();
    }
  });

  // Esc key closes modal
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && editModal.classList.contains('open')) {
      closeEditModal();
    }
  });

  // --- Initial Setup ---
  // Initial fetch from Java Servlet API
  fetchStudents();
});
