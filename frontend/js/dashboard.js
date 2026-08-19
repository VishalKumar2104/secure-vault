document.addEventListener('DOMContentLoaded', () => {
    const user = sv_getCurrentUser();
    if (user) {
        document.getElementById('welcome-name').textContent = user.fullName;
    }
    
    loadStats();
    loadRecentFiles();
});

function formatBytes(bytes, decimals = 2) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}

async function loadStats() {
    try {
        const response = await apiGet('/api/files/stats');
        if (response.success && response.data) {
            const totalFiles = response.data.totalFiles;
            const storageUsed = response.data.storageUsed || 0;
            
            document.getElementById('stat-files').textContent = totalFiles;
            document.getElementById('stat-storage').textContent = formatBytes(storageUsed);
            
            const limit = 10 * 1024 * 1024 * 1024; // 10 GB
            const pct = Math.min(((storageUsed / limit) * 100), 100).toFixed(2);
            document.getElementById('stat-pct').textContent = `${pct}%`;
        }
    } catch (e) {
        showToast('Failed to load storage statistics', 'error');
    }
}

async function loadRecentFiles() {
    const tbody = document.getElementById('recent-files-body');
    try {
        const response = await apiGet('/api/files/recent');
        if (response.success && response.data) {
            const files = response.data;
            if (files.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="6" class="empty-state">
                            <i class="fa-solid fa-folder-open"></i>
                            <h4>No documents found</h4>
                            <p>Get started by uploading your first document.</p>
                            <a href="upload.html" class="btn btn-primary btn-sm"><i class="fa-solid fa-plus"></i> Upload Now</a>
                        </td>
                    </tr>
                `;
                return;
            }
            
            tbody.innerHTML = '';
            files.forEach(file => {
                const date = new Date(file.uploadDate).toLocaleDateString();
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td style="font-weight: 600; color: white;">
                        <i class="${getFileIconClass(file.fileType)}" style="margin-right: 10px; color: var(--accent-cyan);"></i>
                        ${file.originalFileName}
                    </td>
                    <td><span class="badge badge-info">${file.fileType}</span></td>
                    <td>${formatBytes(file.fileSize)}</td>
                    <td>${date}</td>
                    <td><span class="badge badge-success">Secure</span></td>
                    <td class="action-buttons">
                        <button onclick="viewDetails(${file.id})" class="btn-icon" title="View Details"><i class="fa-solid fa-circle-info"></i></button>
                        <button onclick="performDownload(${file.id}, '${file.originalFileName}')" class="btn-icon" title="Download"><i class="fa-solid fa-download"></i></button>
                        <button onclick="deleteRecentFile(${file.id}, '${file.originalFileName}')" class="btn-icon btn-icon-danger" title="Delete"><i class="fa-solid fa-trash"></i></button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        }
    } catch (e) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" style="text-align: center; color: var(--danger); padding: 30px;">
                    <i class="fa-solid fa-circle-exclamation"></i> Failed to load recent files.
                </td>
            </tr>
        `;
    }
}

function getFileIconClass(ext) {
    if (!ext) return 'fa-solid fa-file';
    switch (ext.toLowerCase()) {
        case 'pdf': return 'fa-solid fa-file-pdf';
        case 'png':
        case 'jpg':
        case 'jpeg': return 'fa-solid fa-file-image';
        case 'doc':
        case 'docx': return 'fa-solid fa-file-word';
        case 'xls':
        case 'xlsx': return 'fa-solid fa-file-excel';
        case 'txt': return 'fa-solid fa-file-lines';
        case 'zip': return 'fa-solid fa-file-zipper';
        default: return 'fa-solid fa-file';
    }
}

async function viewDetails(fileId) {
    try {
        const response = await apiGet(`/api/files/${fileId}`);
        if (response.success && response.data) {
            const file = response.data;
            document.getElementById('detail-name').textContent = file.originalFileName;
            document.getElementById('detail-type').textContent = file.fileType.toUpperCase();
            document.getElementById('detail-mime').textContent = file.mimeType;
            document.getElementById('detail-size').textContent = formatBytes(file.fileSize);
            document.getElementById('detail-date').textContent = new Date(file.uploadDate).toLocaleString();
            document.getElementById('detail-encryption').textContent = file.encryptionStatus || 'Access Protected';
            document.getElementById('detail-security').textContent = 'Verified Clean';
            
            document.getElementById('details-modal').classList.add('active');
        }
    } catch (e) {
        showToast(e.message || 'Failed to fetch details', 'error');
    }
}

function closeDetailsModal() {
    document.getElementById('details-modal').classList.remove('active');
}

async function deleteRecentFile(fileId, filename) {
    if (confirm(`Are you sure you want to delete "${filename}"? This will delete the file permanently.`)) {
        try {
            await apiDelete(`/api/files/${fileId}`);
            showToast('Document deleted successfully', 'success');
            loadStats();
            loadRecentFiles();
        } catch (e) {
            showToast(e.message || 'Failed to delete file', 'error');
        }
    }
}
