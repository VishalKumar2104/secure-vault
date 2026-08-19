document.addEventListener('DOMContentLoaded', () => {
    loadProfile();

    setupPasswordToggle('toggle-new-password', 'new-password');
    setupPasswordToggle('toggle-confirm-new-password', 'confirm-new-password');

    const newPasswordInput = document.getElementById('new-password');
    if (newPasswordInput) {
        newPasswordInput.addEventListener('input', () => {
            const val = newPasswordInput.value;
            const bar = document.getElementById('strength-bar');
            
            let rulesMet = 0;
            if (val.length >= 8) rulesMet++;
            if (/[A-Z]/.test(val)) rulesMet++;
            if (/[a-z]/.test(val)) rulesMet++;
            if (/\d/.test(val)) rulesMet++;
            if (/[@$!%*?&#]/.test(val)) rulesMet++;

            if (bar) {
                bar.className = 'password-strength-bar';
                if (val.length === 0) {}
                else if (rulesMet <= 2) bar.classList.add('strength-weak');
                else if (rulesMet <= 4) bar.classList.add('strength-medium');
                else bar.classList.add('strength-strong');
            }
        });
    }

    const profileForm = document.getElementById('profile-form');
    if (profileForm) {
        profileForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = document.getElementById('btn-save-profile');
            if (btn) btn.disabled = true;
            
            const fullName = document.getElementById('profile-name').value.trim();

            try {
                const response = await apiPut('/api/users/me', { fullName });
                if (response.success && response.data) {
                    const localUser = sv_getCurrentUser();
                    if (localUser) {
                        localUser.fullName = response.data.fullName;
                        localStorage.setItem('sv_user', JSON.stringify(localUser));
                        
                        const topbarName = document.querySelector('.topbar .user-info .name');
                        if (topbarName) topbarName.textContent = response.data.fullName;
                    }
                    showToast('Profile details saved successfully', 'success');
                }
            } catch (err) {
                showToast(err.message || 'Failed to save details', 'error');
            } finally {
                if (btn) btn.disabled = false;
            }
        });
    }

    const passwordForm = document.getElementById('password-form');
    if (passwordForm) {
        passwordForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = document.getElementById('btn-change-password');
            
            const oldPassword = document.getElementById('old-password')?.value;
            const newPassword = newPasswordInput?.value;
            const confirmNewPassword = document.getElementById('confirm-new-password')?.value;

            if (newPassword !== confirmNewPassword) {
                showToast('New passwords do not match', 'error');
                return;
            }

            if (btn) btn.disabled = true;
            try {
                await apiPut('/api/users/change-password', { oldPassword, newPassword, confirmNewPassword });
                showToast('Password updated successfully', 'success');
                passwordForm.reset();
                const bar = document.getElementById('strength-bar');
                if (bar) bar.className = 'password-strength-bar';
            } catch (err) {
                showToast(err.message || 'Failed to update password', 'error');
            } finally {
                if (btn) btn.disabled = false;
            }
        });
    }
});

async function loadProfile() {
    try {
        const response = await apiGet('/api/users/me');
        if (response.success && response.data) {
            const emailEl = document.getElementById('profile-email');
            const nameEl = document.getElementById('profile-name');
            if (emailEl) emailEl.value = response.data.email;
            if (nameEl) nameEl.value = response.data.fullName;
        }
    } catch (e) {
        showToast('Failed to load profile details', 'error');
    }
}

function setupPasswordToggle(toggleId, inputId) {
    const toggle = document.getElementById(toggleId);
    const input = document.getElementById(inputId);
    if (!toggle || !input) return;
    
    toggle.addEventListener('click', () => {
        const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
        input.setAttribute('type', type);
        toggle.classList.toggle('fa-eye');
        toggle.classList.toggle('fa-eye-slash');
    });
}
