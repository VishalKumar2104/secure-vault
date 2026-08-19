document.addEventListener('DOMContentLoaded', () => {
    loadLastLogin();
    loadSecurityEvents();
});

async function loadLastLogin() {
    const el = document.getElementById('last-login-details');
    try {
        const response = await apiGet('/api/users/me');
        if (response.success && response.data) {
            const user = response.data;
            if (user.lastLogin) {
                const lastLoginDate = new Date(user.lastLogin).toLocaleString();
                const browserName = getBrowserName();
                const osName = getOSName();
                el.innerHTML = `Your last login was on <strong>${lastLoginDate}</strong> from <strong>${browserName} (${osName})</strong>.`;
            } else {
                el.innerHTML = `First session initialized on this account.`;
            }
        }
    } catch (e) {
        el.textContent = 'Failed to load last login session audit details.';
    }
}

async function loadSecurityEvents() {
    const tbody = document.getElementById('security-events-body');
    try {
        const response = await apiGet('/api/security/events?page=0&size=10');
        if (response.success && response.data) {
            const events = response.data.content;
            if (events.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="4" class="empty-state">
                            <i class="fa-solid fa-shield"></i>
                            <h4>No security alerts</h4>
                            <p>Your account is secure. No suspicious activities have been recorded.</p>
                        </td>
                    </tr>
                `;
                return;
            }

            tbody.innerHTML = '';
            events.forEach(evt => {
                const date = new Date(evt.createdAt).toLocaleString();
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td style="font-weight: 600; color: white;">
                        <i class="fa-solid fa-triangle-exclamation" style="margin-right: 8px; color: ${getSeverityColor(evt.severity)};"></i>
                        ${evt.eventType}
                    </td>
                    <td><span class="badge ${getSeverityBadgeClass(evt.severity)}">${evt.severity}</span></td>
                    <td>${evt.description}</td>
                    <td>${date}</td>
                `;
                tbody.appendChild(tr);
            });
        }
    } catch (e) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" style="text-align: center; color: var(--danger); padding: 30px;">
                    <i class="fa-solid fa-circle-exclamation"></i> Failed to load security logs.
                </td>
            </tr>
        `;
    }
}

function getSeverityBadgeClass(sev) {
    if (!sev) return 'badge-info';
    switch (sev.toUpperCase()) {
        case 'LOW': return 'badge-info';
        case 'MEDIUM': return 'badge-warning';
        case 'HIGH':
        case 'CRITICAL': return 'badge-danger';
        default: return 'badge-info';
    }
}

function getSeverityColor(sev) {
    if (!sev) return 'var(--info)';
    switch (sev.toUpperCase()) {
        case 'LOW': return 'var(--info)';
        case 'MEDIUM': return 'var(--warning)';
        case 'HIGH':
        case 'CRITICAL': return 'var(--danger)';
        default: return 'var(--info)';
    }
}

function getBrowserName() {
    const userAgent = navigator.userAgent;
    if (userAgent.indexOf("Firefox") > -1) return "Mozilla Firefox";
    if (userAgent.indexOf("Chrome") > -1) return "Google Chrome";
    if (userAgent.indexOf("Safari") > -1) return "Apple Safari";
    if (userAgent.indexOf("Edge") > -1) return "Microsoft Edge";
    return "Web Browser";
}

function getOSName() {
    const userAgent = navigator.userAgent;
    if (userAgent.indexOf("Windows") > -1) return "Windows";
    if (userAgent.indexOf("Mac") > -1) return "macOS";
    if (userAgent.indexOf("Linux") > -1) return "Linux";
    return "Unknown OS";
}
