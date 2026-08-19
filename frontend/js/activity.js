let currentPage = 0;
const pageSize = 10;
let totalPages = 0;

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('btn-prev-page').addEventListener('click', () => {
        if (currentPage > 0) {
            currentPage--;
            loadActivities();
        }
    });

    document.getElementById('btn-next-page').addEventListener('click', () => {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadActivities();
        }
    });

    loadActivities();
});

async function loadActivities() {
    const tbody = document.getElementById('activities-list-body');
    const pagination = document.getElementById('activities-pagination');

    try {
        const response = await apiGet(`/api/activities?page=${currentPage}&size=${pageSize}`);
        if (response.success && response.data) {
            const activities = response.data.content;
            totalPages = response.data.totalPages;
            const totalElements = response.data.totalElements;

            if (activities.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="5" class="empty-state">
                            <i class="fa-solid fa-clock-rotate-left"></i>
                            <h4>No activities logged</h4>
                            <p>All operations performed on your account will appear here.</p>
                        </td>
                    </tr>
                `;
                pagination.style.display = 'none';
                return;
            }

            tbody.innerHTML = '';
            activities.forEach(act => {
                const dt = new Date(act.createdAt);
                const date = dt.toLocaleDateString();
                const time = dt.toLocaleTimeString();

                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td style="font-weight: 600; color: white;">
                        <i class="${getActivityIconClass(act.activityType)}" style="margin-right: 10px; color: var(--accent-cyan);"></i>
                        ${act.activityType}
                    </td>
                    <td>${act.description}</td>
                    <td>${date}</td>
                    <td>${time}</td>
                    <td><span class="badge badge-success">Success</span></td>
                `;
                tbody.appendChild(tr);
            });

            if (totalPages > 1) {
                pagination.style.display = 'flex';
                document.getElementById('page-info-text').textContent = `Page ${currentPage + 1} of ${totalPages} (Total: ${totalElements})`;
                document.getElementById('btn-prev-page').disabled = (currentPage === 0);
                document.getElementById('btn-next-page').disabled = (currentPage === totalPages - 1);
            } else {
                pagination.style.display = 'none';
            }
        }
    } catch (e) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" style="text-align: center; color: var(--danger); padding: 30px;">
                    <i class="fa-solid fa-circle-exclamation"></i> Failed to load activity logs: ${e.message}
                </td>
            </tr>
        `;
        pagination.style.display = 'none';
    }
}

function getActivityIconClass(type) {
    if (!type) return 'fa-solid fa-circle-info';
    switch (type.toUpperCase()) {
        case 'LOGIN': return 'fa-solid fa-right-to-bracket';
        case 'LOGOUT': return 'fa-solid fa-right-from-bracket';
        case 'REGISTER': return 'fa-solid fa-user-plus';
        case 'FILE_UPLOAD': return 'fa-solid fa-cloud-arrow-up';
        case 'FILE_DOWNLOAD': return 'fa-solid fa-download';
        case 'FILE_DELETE': return 'fa-solid fa-trash';
        case 'PASSWORD_CHANGE':
        case 'RESET_PASSWORD': return 'fa-solid fa-key';
        case 'PROFILE_UPDATE': return 'fa-solid fa-user-pen';
        default: return 'fa-solid fa-circle-info';
    }
}
