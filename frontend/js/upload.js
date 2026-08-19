document.addEventListener('DOMContentLoaded', () => {
    const dropZone = document.getElementById('drop-zone');
    const fileInput = document.getElementById('file-input');

    ['dragenter', 'dragover'].forEach(eventName => {
        dropZone.addEventListener(eventName, (e) => {
            e.preventDefault();
            dropZone.classList.add('dragover');
        }, false);
    });

    ['dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, (e) => {
            e.preventDefault();
            dropZone.classList.remove('dragover');
        }, false);
    });

    dropZone.addEventListener('drop', (e) => {
        const dt = e.dataTransfer;
        const files = dt.files;
        handleFiles(files);
    });

    fileInput.addEventListener('change', () => {
        handleFiles(fileInput.files);
    });
});

const ALLOWED_EXTENSIONS = ['pdf', 'jpg', 'jpeg', 'png', 'doc', 'docx', 'xls', 'xlsx', 'txt', 'zip'];

function getFileExtension(filename) {
    if (!filename.includes('.')) return '';
    return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
}

function formatBytes(bytes, decimals = 2) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}

function handleFiles(files) {
    if (files.length === 0) return;
    
    document.getElementById('upload-queue-container').style.display = 'block';
    
    for (let i = 0; i < files.length; i++) {
        const file = files[i];
        
        if (file.size > 50 * 1024 * 1024) {
            showToast(`"${file.name}" is too large. Max upload size is 50MB.`, 'error');
            continue;
        }
        
        const ext = getFileExtension(file.name);
        if (!ALLOWED_EXTENSIONS.includes(ext)) {
            showToast(`"${file.name}" is not supported. Supported extensions: PDF, images, docs, spreadsheets, ZIP.`, 'error');
            continue;
        }
        
        uploadSingleFile(file);
    }
}

function uploadSingleFile(file) {
    const queue = document.getElementById('upload-queue');
    const cardId = 'upload-card-' + Date.now() + Math.floor(Math.random() * 1000);
    
    const card = document.createElement('div');
    card.className = 'upload-progress-card';
    card.id = cardId;
    card.innerHTML = `
        <div class="progress-info">
            <span class="progress-filename"><i class="fa-solid fa-file-arrow-up" style="margin-right: 8px;"></i> ${file.name}</span>
            <span class="progress-pct" id="${cardId}-pct">0%</span>
        </div>
        <div class="progress-bar-container">
            <div class="progress-bar-fill" id="${cardId}-bar" style="width: 0%;"></div>
        </div>
        <div style="display: flex; justify-content: space-between; font-size: 0.75rem; color: var(--text-muted);">
            <span>Size: ${formatBytes(file.size)}</span>
            <span id="${cardId}-status">Uploading...</span>
        </div>
    `;
    queue.appendChild(card);
    
    const formData = new FormData();
    formData.append('file', file);
    
    apiUpload('/api/files/upload', formData, (pct) => {
        document.getElementById(`${cardId}-pct`).textContent = `${pct}%`;
        document.getElementById(`${cardId}-bar`).style.width = `${pct}%`;
        if (pct === 100) {
            document.getElementById(`${cardId}-status`).textContent = 'Saving metadata...';
        }
    })
    .then(response => {
        document.getElementById(`${cardId}-status`).textContent = 'Completed';
        document.getElementById(`${cardId}-status`).style.color = 'var(--success)';
        document.getElementById(`${cardId}-pct`).style.color = 'var(--success)';
        showToast(`Uploaded "${file.name}" successfully`, 'success');
    })
    .catch(error => {
        document.getElementById(`${cardId}-status`).textContent = 'Failed';
        document.getElementById(`${cardId}-status`).style.color = 'var(--danger)';
        document.getElementById(`${cardId}-pct`).style.color = 'var(--danger)';
        document.getElementById(`${cardId}-bar`).style.background = 'var(--danger)';
        showToast(`Failed to upload "${file.name}": ${error.message}`, 'error');
    });
}
