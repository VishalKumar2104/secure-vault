let currentPage = 0;
const pageSize = 10;
let totalPages = 0;

let searchTerm = '';
let filterType = 'all';
let sortBy = 'newest';

document.addEventListener('DOMContentLoaded', () => {
    const searchInput = document.getElementById('search-input');
    let debounceTimer;
    searchInput.addEventListener('input', () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            searchTerm = searchInput.value;
            currentPage = 0;
            loadFiles();
        }, 400);
    });

    const filterSelect = document.getElementById('filter-type');
    filterSelect.addEventListener('change', () => {
        filterType = filterSelect.value;
        currentPage = 0;
        loadFiles();
    });

    const sortSelect = document.getElementById('sort-by');
    sortSelect.addEventListener('change', () => {
        sortBy = sortSelect.value;
        currentPage = 0;
        loadFiles();
    });

    document.getElementById('btn-prev-page').addEventListener('click', () => {
        if (currentPage > 0) {
            currentPage--;
            loadFiles();
        }
    });

    document.getElementById('btn-next-page').addEventListener('click', () => {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadFiles();
        }
    });

    loadFiles();
});

function formatBytes(bytes, decimals = 2) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}

async function loadFiles() {
    const tbody = document.getElementById('files-list-body');
    const pagination = document.getElementById('files-pagination');
    
    try {
        const response = await apiGet(`/api/files?page=${currentPage}&size=${pageSize}&search=${encodeURIComponent(searchTerm)}&filter=${filterType}&sortBy=${sortBy}`);
        if (response.success && response.data) {
            const files = response.data.content;
            totalPages = response.data.totalPages;
            const totalElements = response.data.totalElements;

            if (files.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="7" class="empty-state">
                            <i class="fa-solid fa-folder-open"></i>
                            <h4>No documents found</h4>
                            <p>Try modifying your search or filters.</p>
                        </td>
                    </tr>
                `;
                pagination.style.display = 'none';
                return;
            }

            tbody.innerHTML = '';
            files.forEach(file => {
                const uploadDate = new Date(file.uploadDate).toLocaleDateString();
                const modifiedDate = new Date(file.updatedAt).toLocaleDateString();
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td style="font-weight: 600; color: white;">
                        <i class="${getFileIconClass(file.fileType)}" style="margin-right: 10px; color: var(--accent-cyan);"></i>
                        ${file.originalFileName}
                    </td>
                    <td><span class="badge badge-info">${file.fileType}</span></td>
                    <td>${formatBytes(file.fileSize)}</td>
                    <td>${uploadDate}</td>
                    <td>${modifiedDate}</td>
                    <td><span class="badge badge-success">Secure</span></td>
                    <td class="action-buttons">
                        <button onclick="viewDetails(${file.id})" class="btn-icon" title="View Details"><i class="fa-solid fa-circle-info"></i></button>
                        <button onclick="performDownload(${file.id}, '${file.originalFileName}')" class="btn-icon" title="Download"><i class="fa-solid fa-download"></i></button>
                        <button onclick="deleteFile(${file.id}, '${file.originalFileName}')" class="btn-icon btn-icon-danger" title="Delete"><i class="fa-solid fa-trash"></i></button>
                    </td>
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
                <td colspan="7" style="text-align: center; color: var(--danger); padding: 30px;">
                    <i class="fa-solid fa-circle-exclamation"></i> Failed to load files: ${e.message}
                </td>
            </tr>
        `;
        pagination.style.display = 'none';
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

async function deleteFile(fileId, filename) {
    if (confirm(`Are you sure you want to delete "${filename}"? This action cannot be undone.`)) {
        try {
            await apiDelete(`/api/files/${fileId}`);
            showToast('Document deleted successfully', 'success');
            loadFiles();
        } catch (e) {
            showToast(e.message || 'Failed to delete file', 'error');
        }
    }
}
