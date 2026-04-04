const API = 'http://localhost:8081';
let profileData = {};

// ── Init ──────────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/login';
        return;
    }
    const role = localStorage.getItem('role');
    if (role && role !== 'customer') {
        // Vendors/admins use their own dashboards
        window.location.href = role === 'vendor' ? '/vendor/dashboard' : '/admin/dashboard';
        return;
    }
    loadProfile();
});

// ── Tab switching ─────────────────────────────────────────────────────────────
function switchTab(id, btn) {
    document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    document.getElementById(id).classList.add('active');
    btn.classList.add('active');
}

// ── Load profile ──────────────────────────────────────────────────────────────
async function loadProfile() {
    showLoading(true);
    try {
        const res = await fetch(`${API}/api/customer/profile`, {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        if (!res.ok) throw new Error('Unauthorized');
        profileData = await res.json();
        renderProfile(profileData);
    } catch (e) {
        showToast('Failed to load profile. Please log in again.', 'error');
        setTimeout(() => window.location.href = '/login', 2000);
    } finally {
        showLoading(false);
    }
}

function renderProfile(d) {
    const fullName = [d.firstName, d.lastName].filter(Boolean).join(' ');
    const initial  = d.firstName ? d.firstName.charAt(0).toUpperCase() : '?';

    // Photo initials
    const photoInitials = document.getElementById('photoInitials');
    if (photoInitials) photoInitials.textContent = initial;

    // Profile image
    if (d.profileImage) {
        const img = document.createElement('img');
        img.src = d.profileImage;
        img.alt = 'Profile';
        const circle = document.getElementById('photoCircle');
        const initialsEl = circle.querySelector('.photo-initials');
        if (initialsEl) initialsEl.style.display = 'none';
        circle.insertBefore(img, circle.querySelector('.photo-upload-icon'));
    }

    // Display values
    setDisplay('displayFullName', fullName || '—');
    setDisplay('displayEmail',    d.email   || '—');
    setDisplay('displayAddress',  d.address || '—');
    setDisplay('displayPhone',    d.phoneNumber || '—');
    setDisplay('displayDob',      d.dob ? formatDate(d.dob) : '—');
    setDisplay('displayGender',   d.gender  || '—');

    // Input defaults (for edit mode)
    setInput('inputFirstName', fullName);
    setInput('inputAddress',   d.address   || '');
    setInput('inputPhone',     d.phoneNumber || '');
    setInput('inputDob',       d.dob || '');
    setSelectValue('inputGender', d.gender || '');
}

function setDisplay(id, value) {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = value;
    if (!value || value === '—') el.classList.add('empty');
    else el.classList.remove('empty');
}

function setInput(id, value) {
    const el = document.getElementById(id);
    if (el) el.value = value;
}

function setSelectValue(id, value) {
    const el = document.getElementById(id);
    if (!el) return;
    for (const opt of el.options) {
        if (opt.value === value) { opt.selected = true; break; }
    }
}

function formatDate(dob) {
    if (!dob) return '—';
    const d = new Date(dob);
    return d.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
}

// ── Edit mode ─────────────────────────────────────────────────────────────────
function enableEditing() {
    document.getElementById('profileCard').classList.add('edit-mode');
}

function cancelEditing() {
    document.getElementById('profileCard').classList.remove('edit-mode');
    renderProfile(profileData); // restore original values
}

// ── Save profile ──────────────────────────────────────────────────────────────
async function saveProfile() {
    const fullNameInput = document.getElementById('inputFirstName').value.trim();
    const nameParts = fullNameInput.split(' ');
    const firstName = nameParts[0] || '';
    const lastName  = nameParts.slice(1).join(' ') || profileData.lastName || '';

    const payload = {
        firstName:   firstName,
        lastName:    lastName,
        phoneNumber: document.getElementById('inputPhone').value.trim(),
        dob:         document.getElementById('inputDob').value || null,
        address:     document.getElementById('inputAddress').value.trim(),
        gender:      document.getElementById('inputGender').value
    };

    showLoading(true);
    try {
        const res = await fetch(`${API}/api/customer/profile`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            body: JSON.stringify(payload)
        });
        if (!res.ok) throw new Error('Failed');

        // Update localStorage first name
        if (firstName) {
            localStorage.setItem('firstName', firstName);
            const navName = document.getElementById('navUserName');
            const navInit = document.getElementById('navInitials');
            if (navName) navName.textContent = firstName;
            if (navInit) navInit.textContent = firstName.charAt(0).toUpperCase();
        }

        // Refresh profile data
        profileData = { ...profileData, ...payload, firstName, lastName };
        document.getElementById('profileCard').classList.remove('edit-mode');
        renderProfile(profileData);
        showToast('Profile updated successfully!', 'success');
    } catch (e) {
        showToast('Failed to save profile. Please try again.', 'error');
    } finally {
        showLoading(false);
    }
}

// ── Delete account ────────────────────────────────────────────────────────────
function confirmDelete() {
    if (!confirm('Are you sure you want to delete your account? This action cannot be undone.')) return;
    deleteAccount();
}

async function deleteAccount() {
    showLoading(true);
    try {
        const res = await fetch(`${API}/api/customer/account`, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        if (!res.ok) throw new Error('Failed');
        localStorage.clear();
        showToast('Account deleted. Redirecting...', 'success');
        setTimeout(() => window.location.href = '/home', 1500);
    } catch (e) {
        showToast('Failed to delete account.', 'error');
    } finally {
        showLoading(false);
    }
}

// ── Logout ────────────────────────────────────────────────────────────────────
function doLogout() {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('email');
    localStorage.removeItem('firstName');
    window.location.href = '/home';
}

// ── UI helpers ────────────────────────────────────────────────────────────────
function showLoading(on) {
    const el = document.getElementById('loadingOverlay');
    if (el) el.classList.toggle('show', on);
}

function showToast(msg, type = '') {
    const el = document.getElementById('toast');
    if (!el) return;
    el.textContent = msg;
    el.className = 'toast ' + type;
    void el.offsetWidth;
    el.classList.add('show');
    setTimeout(() => el.classList.remove('show'), 3000);
}
