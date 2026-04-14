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
    if (id === 'reviews') loadMyReviews();
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
    const circle = document.getElementById('photoCircle');
    const existingImg = circle.querySelector('img');
    if (existingImg) existingImg.remove();
    const cameraIcon = circle.querySelector('.photo-camera-icon');
    if (d.profileImage) {
        const img = document.createElement('img');
        img.src = d.profileImage;
        img.alt = 'Profile';
        circle.appendChild(img);
        if (cameraIcon) cameraIcon.style.display = 'none';
    } else {
        if (cameraIcon) cameraIcon.style.display = 'flex';
    }

    // License image
    const licenseBox = document.getElementById('licensePhotoBox');
    const existingLicImg = licenseBox.querySelector('img');
    if (existingLicImg) existingLicImg.remove();
    const placeholder = document.getElementById('licensePlaceholder');
    if (d.licenseImage) {
        if (placeholder) placeholder.style.display = 'none';
        const img = document.createElement('img');
        img.src = d.licenseImage;
        img.alt = 'License';
        img.style.cssText = 'width:100%;height:100%;object-fit:cover;border-radius:10px;';
        licenseBox.insertBefore(img, placeholder);
    } else {
        if (placeholder) placeholder.style.display = 'flex';
    }

    // Display values
    setDisplay('displayFullName', fullName || '—');
    setDisplay('displayEmail',    d.email   || '—');
    setDisplay('displayAddress',  d.address || '—');
    setDisplay('displayPhone',    d.phoneNumber || '—');
    setDisplay('displayDob',      d.dob ? formatDate(d.dob) : '—');

    // Input defaults (for edit mode)
    setInput('inputFirstName', fullName);
    setInput('inputAddress',   d.address   || '');
    setInput('inputPhone',     d.phoneNumber || '');
    setInput('inputDob',       d.dob || '');
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
        address:     document.getElementById('inputAddress').value.trim()
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

// ── Photo uploads ─────────────────────────────────────────────────────────────
async function handleProfilePhotoChange(event) {
    const file = event.target.files[0];
    if (!file) return;
    const formData = new FormData();
    formData.append('file', file);
    showLoading(true);
    try {
        const res = await fetch(`${API}/api/customer/profile/photo`, {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') },
            body: formData
        });
        if (!res.ok) throw new Error('Failed');
        const data = await res.json();
        profileData.profileImage = data.profileImage;
        renderProfile(profileData);
        showToast('Profile photo updated!', 'success');
    } catch (e) {
        showToast('Failed to upload photo.', 'error');
    } finally {
        showLoading(false);
        event.target.value = '';
    }
}

async function handleLicensePhotoChange(event) {
    const file = event.target.files[0];
    if (!file) return;
    const formData = new FormData();
    formData.append('file', file);
    showLoading(true);
    try {
        const res = await fetch(`${API}/api/customer/profile/license-image`, {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') },
            body: formData
        });
        if (!res.ok) throw new Error('Failed');
        const data = await res.json();
        profileData.licenseImage = data.licenseImage;
        renderProfile(profileData);
        showToast('License photo updated!', 'success');
    } catch (e) {
        showToast('Failed to upload license photo.', 'error');
    } finally {
        showLoading(false);
        event.target.value = '';
    }
}

// ── Password visibility toggle ────────────────────────────────────────────────
function togglePwVisibility(inputId, btn) {
    const input = document.getElementById(inputId);
    const isHidden = input.type === 'password';
    input.type = isHidden ? 'text' : 'password';
    btn.querySelector('.eye-show').style.display = isHidden ? 'none' : '';
    btn.querySelector('.eye-hide').style.display = isHidden ? '' : 'none';
}

// ── Change Password ───────────────────────────────────────────────────────────
function togglePasswordForm() {
    const form = document.getElementById('passwordForm');
    const visible = form.style.display !== 'none';
    form.style.display = visible ? 'none' : 'block';
    if (visible) {
        document.getElementById('inputCurrentPw').value = '';
        document.getElementById('inputNewPw').value = '';
        document.getElementById('inputConfirmPw').value = '';
    }
}

async function changePassword() {
    const currentPw  = document.getElementById('inputCurrentPw').value.trim();
    const newPw      = document.getElementById('inputNewPw').value.trim();
    const confirmPw  = document.getElementById('inputConfirmPw').value.trim();

    if (!currentPw || !newPw || !confirmPw) {
        showToast('Please fill in all password fields.', 'error');
        return;
    }
    if (newPw.length < 6) {
        showToast('New password must be at least 6 characters.', 'error');
        return;
    }
    if (newPw !== confirmPw) {
        showToast('New passwords do not match.', 'error');
        return;
    }

    showLoading(true);
    try {
        const res = await fetch(`${API}/api/customer/password`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            body: JSON.stringify({ currentPassword: currentPw, newPassword: newPw })
        });
        const text = await res.text();
        if (!res.ok) {
            showToast(text || 'Failed to update password.', 'error');
            return;
        }
        showToast('Password updated successfully!', 'success');
        togglePasswordForm();
    } catch (e) {
        showToast('Failed to update password. Please try again.', 'error');
    } finally {
        showLoading(false);
    }
}

// ── Reviews History ───────────────────────────────────────────────────────────
async function loadMyReviews() {
    const list = document.getElementById('reviewsList');
    const countBadge = document.getElementById('reviewsCount');
    list.innerHTML = '<div style="text-align:center;padding:40px;color:#bbb;font-size:13px;">Loading...</div>';
    try {
        const res = await fetch(`${API}/api/reviews/my-reviews`, {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        if (!res.ok) throw new Error('Failed');
        const reviews = await res.json();
        countBadge.textContent = reviews.length;
        if (reviews.length === 0) {
            list.innerHTML = `
                <div class="reviews-empty">
                    <svg viewBox="0 0 24 24" fill="#ccc"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/></svg>
                    <p>No reviews yet</p>
                    <small>Reviews you leave on vehicles will appear here.</small>
                </div>`;
            return;
        }
        list.innerHTML = reviews.map(r => buildMyReviewCard(r)).join('');
    } catch (e) {
        list.innerHTML = '<div style="text-align:center;padding:40px;color:#ef4444;font-size:13px;">Failed to load reviews.</div>';
    }
}

function buildMyReviewCard(r) {
    const initials = r.vehicleName ? r.vehicleName.charAt(0).toUpperCase() : 'V';
    const stars = Array.from({ length: 5 }, (_, i) =>
        `<svg viewBox="0 0 24 24" fill="${i < r.rating ? '#f59e0b' : '#e5e7eb'}"><path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg>`
    ).join('');
    const date = r.createdAt ? new Date(r.createdAt).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' }) : '';
    return `
        <div class="review-card" id="review-${r.reviewId}">
            <div class="review-avatar">${initials}</div>
            <div class="review-body">
                <div class="review-top">
                    <span class="review-vehicle">${r.vehicleName || 'Vehicle'}</span>
                    <div class="review-stars">${stars}</div>
                    <span class="review-date">${date}</span>
                </div>
                <div class="review-comment">${r.comment || ''}</div>
            </div>
            <button class="btn-delete-review" onclick="confirmDeleteReview(${r.reviewId})" title="Delete review">
                <svg viewBox="0 0 24 24" fill="currentColor"><path d="M6 19a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2V7H6v12zm3-8h2v7H9v-7zm4 0h2v7h-2v-7zM15.5 4l-1-1h-5l-1 1H5v2h14V4h-3.5z"/></svg>
            </button>
        </div>`;
}

function confirmDeleteReview(reviewId) {
    if (!confirm('Delete this review? This cannot be undone.')) return;
    deleteMyReview(reviewId);
}

async function deleteMyReview(reviewId) {
    showLoading(true);
    try {
        const res = await fetch(`${API}/api/reviews/${reviewId}`, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        if (!res.ok) throw new Error('Failed');
        const card = document.getElementById(`review-${reviewId}`);
        if (card) card.remove();
        const remaining = document.querySelectorAll('.review-card').length;
        document.getElementById('reviewsCount').textContent = remaining;
        if (remaining === 0) {
            document.getElementById('reviewsList').innerHTML = `
                <div class="reviews-empty">
                    <svg viewBox="0 0 24 24" fill="#ccc"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/></svg>
                    <p>No reviews yet</p>
                    <small>Reviews you leave on vehicles will appear here.</small>
                </div>`;
        }
        showToast('Review deleted.', 'success');
    } catch (e) {
        showToast('Failed to delete review.', 'error');
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
