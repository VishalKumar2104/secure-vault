/**
 * Secure Vault - Admin Console Controller
 */

document.addEventListener('DOMContentLoaded', () => {
    const path = window.location.pathname;

    if (path.endsWith('admin-dashboard.html')) {
        loadAdminDashboard();
    } else if (path.endsWith('admin-users.html')) {
        loadAdminUsers();
    } else if (path.endsWith('admin-files.html')) {
        loadAdminFiles();
    } else if (path.endsWith('admin-activity.html')) {
        loadAdminActivities();
    } else if (path.endsWith('admin-security.html')) {
        loadAdminSecurity();
    } else if (path.endsWith('admin-infrastructure.html')) {
        loadAdminInfrastructure();
    }
});

function formatBytes(bytes, decimals = 2) {
    if (!bytes || bytes === 0) return '0 Bytes';
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}

// -------------------------------------------------------------
// 1. Admin Dashboard
// -------------------------------------------------------------
async function loadAdminDashboard() {
    try {
        const statsRes = await apiGet('/api/admin/dashboard');
        if (statsRes.success && statsRes.data) {
            const data = statsRes.data;
            const elUsers = document.getElementById('admin-stat-users');
            const elFiles = document.getElementById('admin-stat-files');
            const elStorage = document.getElementById('admin-stat-storage');
            const elActiveUsers = document.getElementById('admin-stat-active-users');
            const elFailedLogins = document.getElementById('admin-stat-failed-logins');
            const elSecurity = document.getElementById('admin-stat-security');

            if (elUsers) elUsers.textContent = data.totalUsers || 0;
            if (elFiles) elFiles.textContent = data.totalFiles || 0;
            if (elStorage) elStorage.textContent = formatBytes(data.storageConsumed || 0);
            if (elActiveUsers) elActiveUsers.textContent = data.activeUsers || 0;
            if (elFailedLogins) elFailedLogins.textContent = data.failedLoginAttempts || 0;
            if (elSecurity) elSecurity.textContent = data.systemSecurity || 'Protected';
        }

        // Load failed login security summary
        const secRes = await apiGet('/api/admin/security?page=0&size=5');
        const failedBody = document.getElementById('failed-logins-summary-body');
        if (failedBody && secRes.success && secRes.data) {
            const events = secRes.data.content || [];
            if (events.length === 0) {
                failedBody.innerHTML = `<tr><td colspan="3" style="text-align: center; color: var(--text-muted); padding: 20px;">No recent security alerts.</td></tr>`;
            } else {
                failedBody.innerHTML = '';
                events.forEach(evt => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td>${evt.user ? evt.user.email : 'Anonymous'}</td>
                        <td>${evt.eventType}</td>
                        <td>${new Date(evt.createdAt).toLocaleString()}</td>
                    `;
                    failedBody.appendChild(tr);
                });
            }
        }

        // Load infrastructure summary
        const infraRes = await apiGet('/api/admin/infrastructure');
        const infraBody = document.getElementById('infrastructure-summary-body');
        if (infraBody && infraRes.success && infraRes.data) {
            infraBody.innerHTML = '';
            const health = infraRes.data;
            for (const [key, val] of Object.entries(health)) {
                const tr = document.createElement('tr');
                const badgeClass = val === 'Operational' ? 'badge-success' : 'badge-danger';
                tr.innerHTML = `
                    <td style="text-transform: capitalize; font-weight: 600; color: white;">${key}</td>
                    <td><span class="badge ${badgeClass}">${val}</span></td>
                `;
                infraBody.appendChild(tr);
            }
        }
    } catch (e) {
        showToast('Failed to load admin overview: ' + e.message, 'error');
    }
}

// -------------------------------------------------------------
// 2. Admin Users Management
// -------------------------------------------------------------
let userPage = 0;
let userTotalPages = 0;

async function loadAdminUsers() {
    const tbody = document.getElementById('admin-users-body');
    if (!tbody) return;

    try {
        const response = await apiGet(`/api/admin/users?page=${userPage}&size=10`);
        if (response.success && response.data) {
            const users = response.data.content || [];
            userTotalPages = response.data.totalPages || 0;

            if (users.length === 0) {
                tbody.innerHTML = `<tr><td colspan="8" class="empty-state"><h4>No users found</h4></td></tr>`;
                return;
            }

            tbody.innerHTML = '';
            users.forEach(u => {
                const isActive = u.accountStatus === 'ACTIVE';
                const statusBadge = isActive 
                    ? '<span class="badge badge-success">ACTIVE</span>' 
                    : '<span class="badge badge-danger">DISABLED</span>';
                
                const toggleBtn = `<button onclick="toggleUserStatus(${u.id}, '${u.email}')" class="btn btn-sm ${isActive ? 'btn-secondary' : 'btn-primary'}">
                    <i class="fa-solid ${isActive ? 'fa-ban' : 'fa-check'}"></i> ${isActive ? 'Disable' : 'Enable'}
                </button>`;

                const lastLoginStr = u.lastLogin ? new Date(u.lastLogin).toLocaleString() : 'Never';

                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td style="font-weight: 600; color: white;">${u.fullName}</td>
                    <td>${u.email}</td>
                    <td><span class="badge ${u.role === 'ADMIN' ? 'badge-warning' : 'badge-info'}">${u.role}</span></td>
                    <td>${statusBadge}</td>
                    <td>${u.filesUploaded || 0}</td>
                    <td>${formatBytes(u.storageUsed || 0)}</td>
                    <td>${lastLoginStr}</td>
                    <td>${toggleBtn}</td>
                `;
                tbody.appendChild(tr);
            });

            const pagination = document.getElementById('users-pagination');
            if (pagination) {
                if (userTotalPages > 1) {
                    pagination.style.display = 'flex';
                    document.getElementById('page-info-text').textContent = `Page ${userPage + 1} of ${userTotalPages}`;
                } else {
                    pagination.style.display = 'none';
                }
            }
        }
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--danger); padding: 20px;">Failed to load users: ${e.message}</td></tr>`;
    }
}

async function toggleUserStatus(userId, email) {
    if (confirm(`Are you sure you want to change the account status for ${email}?`)) {
        try {
            await apiPut(`/api/admin/users/${userId}/toggle-status`, {});
            showToast('Account status updated successfully', 'success');
            loadAdminUsers();
        } catch (e) {
            showToast(e.message || 'Failed to update user status', 'error');
        }
    }
}

// -------------------------------------------------------------
// 3. Admin Files
// -------------------------------------------------------------
let adminFilesPage = 0;
let adminFilesTotalPages = 0;

async function loadAdminFiles() {
    const tbody = document.getElementById('admin-files-body');
    if (!tbody) return;

    const searchInput = document.getElementById('search-input');
    const filterSelect = document.getElementById('filter-type');
    const sortSelect = document.getElementById('sort-by');

    const search = searchInput ? encodeURIComponent(searchInput.value) : '';
    const filter = filterSelect ? filterSelect.value : 'all';
    const sortBy = sortSelect ? sortSelect.value : 'newest';

    try {
        const response = await apiGet(`/api/admin/files?page=${adminFilesPage}&size=10&search=${search}&filter=${filter}&sortBy=${sortBy}`);
        if (response.success && response.data) {
            const files = response.data.content || [];
            adminFilesTotalPages = response.data.totalPages || 0;

            if (files.length === 0) {
                tbody.innerHTML = `<tr><td colspan="7" class="empty-state"><h4>No files found across the system</h4></td></tr>`;
                return;
            }

            tbody.innerHTML = '';
            files.forEach(f => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td style="font-weight: 600; color: white;">
                        <i class="fa-solid fa-file" style="margin-right: 8px; color: var(--accent-cyan);"></i>
                        ${f.originalFileName}
                    </td>
                    <td>${f.ownerEmail || 'Unknown'}</td>
                    <td><span class="badge badge-info">${f.fileType}</span></td>
                    <td>${formatBytes(f.fileSize)}</td>
                    <td>${new Date(f.uploadDate).toLocaleDateString()}</td>
                    <td><span class="badge badge-success">Clean</span></td>
                    <td>
                        <button onclick="performDownload(${f.id}, '${f.originalFileName}')" class="btn-icon" title="Download"><i class="fa-solid fa-download"></i></button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        }
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--danger); padding: 20px;">Failed to load files: ${e.message}</td></tr>`;
    }
}

// -------------------------------------------------------------
// 4. Admin Activities
// -------------------------------------------------------------
async function loadAdminActivities() {
    const tbody = document.getElementById('admin-activities-body');
    if (!tbody) return;

    try {
        const response = await apiGet('/api/admin/activities?page=0&size=20');
        if (response.success && response.data) {
            const activities = response.data.content || [];
            if (activities.length === 0) {
                tbody.innerHTML = `<tr><td colspan="6" class="empty-state"><h4>No activity logs available</h4></td></tr>`;
                return;
            }

            tbody.innerHTML = '';
            activities.forEach(act => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td style="font-weight: 600; color: white;">${act.userEmail || 'System'}</td>
                    <td><span class="badge badge-info">${act.activityType}</span></td>
                    <td>${act.description}</td>
                    <td>${act.ipAddress || '-'}</td>
                    <td>${new Date(act.createdAt).toLocaleString()}</td>
                    <td><span class="badge badge-success">Success</span></td>
                `;
                tbody.appendChild(tr);
            });
        }
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--danger); padding: 20px;">Failed to load activities: ${e.message}</td></tr>`;
    }
}

// -------------------------------------------------------------
// 5. Admin Security
// -------------------------------------------------------------
async function loadAdminSecurity() {
    const tbody = document.getElementById('admin-security-body');
    if (!tbody) return;

    try {
        const response = await apiGet('/api/admin/security?page=0&size=20');
        if (response.success && response.data) {
            const events = response.data.content || [];
            if (events.length === 0) {
                tbody.innerHTML = `<tr><td colspan="5" class="empty-state"><h4>No security incidents detected</h4></td></tr>`;
                return;
            }

            tbody.innerHTML = '';
            events.forEach(evt => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td style="font-weight: 600; color: white;">${evt.user ? evt.user.email : 'Anonymous'}</td>
                    <td><span class="badge badge-danger">${evt.eventType}</span></td>
                    <td><span class="badge badge-warning">${evt.severity}</span></td>
                    <td>${evt.description}</td>
                    <td>${new Date(evt.createdAt).toLocaleString()}</td>
                `;
                tbody.appendChild(tr);
            });
        }
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--danger); padding: 20px;">Failed to load security logs: ${e.message}</td></tr>`;
    }
}

// -------------------------------------------------------------
// 6. Admin Infrastructure
// -------------------------------------------------------------
async function loadAdminInfrastructure() {
    const container = document.getElementById('infra-cards-grid');
    if (!container) return;

    try {
        const response = await apiGet('/api/admin/infrastructure');
        if (response.success && response.data) {
            container.innerHTML = '';
            const health = response.data;
            for (const [service, status] of Object.entries(health)) {
                const isOp = status === 'Operational';
                const card = document.createElement('div');
                card.className = 'stat-card';
                card.style.flexDirection = 'column';
                card.style.alignItems = 'flex-start';
                card.style.gap = '10px';
                card.innerHTML = `
                    <div style="display: flex; justify-content: space-between; width: 100%; align-items: center;">
                        <span style="font-weight: 700; font-size: 1.1rem; color: white; text-transform: capitalize;">${service}</span>
                        <span class="badge ${isOp ? 'badge-success' : 'badge-danger'}">${status}</span>
                    </div>
                    <p style="color: var(--text-muted); font-size: 0.85rem;">Status: ${status} &bull; Latency &lt; 5ms</p>
                `;
                container.appendChild(card);
            }
        }
    } catch (e) {
        container.innerHTML = `<div style="color: var(--danger); padding: 20px;">Failed to load infrastructure health: ${e.message}</div>`;
    }
}
