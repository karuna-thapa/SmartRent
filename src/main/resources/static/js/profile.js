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

    // ── Auto-open My Bookings + show success popup if redirected from booking page
    if (sessionStorage.getItem('bookingSuccess')) {
        sessionStorage.removeItem('bookingSuccess');
        const bookingsTab = document.querySelector('.tab-btn[onclick*="myBookings"]');
        if (bookingsTab) switchTab('myBookings', bookingsTab);
        const popup = document.getElementById('bookingSuccessPopup');
        if (popup) popup.style.display = 'flex';
    }

    // ── Handle eSewa payment callback result (?payment=success|failed)
    const urlParams   = new URLSearchParams(window.location.search);
    const payResult   = urlParams.get('payment');
    if (payResult) {
        const bookingsTab = document.querySelector('.tab-btn[onclick*="myBookings"]');
        if (bookingsTab) switchTab('myBookings', bookingsTab);
        if (payResult === 'success') {
            const popup = document.getElementById('paySuccessPopup');
            if (popup) popup.style.display = 'flex';
        } else {
            const popup = document.getElementById('payFailedPopup');
            if (popup) popup.style.display = 'flex';
        }
        // Clean URL
        window.history.replaceState({}, '', '/profile');
    }
});

// ── Tab switching ─────────────────────────────────────────────────────────────
function switchTab(id, btn) {
    document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    document.getElementById(id).classList.add('active');
    btn.classList.add('active');
    if (id === 'reviews')    loadMyReviews();
    if (id === 'myBookings') loadMyBookings();
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

    // Sync navbar profile image
    const navInitials = document.getElementById('navInitials');
    if (navInitials) {
        navInitials.textContent = '';
        if (d.profileImage) {
            localStorage.setItem('profileImage', d.profileImage);
            const navImg = document.createElement('img');
            navImg.src = d.profileImage;
            navImg.alt = 'Profile';
            navInitials.appendChild(navImg);
        } else {
            localStorage.removeItem('profileImage');
            navInitials.textContent = initial;
        }
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

// ── My Bookings ───────────────────────────────────────────────────────────────
async function loadMyBookings() {
    const list = document.getElementById('bookingsList');
    const countBadge = document.getElementById('bookingsCount');
    list.innerHTML = '<div style="text-align:center;padding:40px;color:#bbb;font-size:13px;">Loading...</div>';
    try {
        const res = await fetch(`${API}/api/customer/bookings`, {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        if (!res.ok) throw new Error('Failed');
        const bookings = await res.json();
        countBadge.textContent = bookings.length;
        if (bookings.length === 0) {
            list.innerHTML = `
                <div class="bookings-empty">
                    <svg viewBox="0 0 24 24" fill="#ccc" width="48" height="48"><path d="M17 12h-5v5h5v-5zM16 1v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19a2 2 0 0 0 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2h-1V1h-2zm3 18H5V8h14v11z"/></svg>
                    <p>No bookings yet</p>
                    <small>Your bookings will appear here after you book a vehicle.</small>
                </div>`;
            return;
        }
        list.innerHTML = bookings.map(b => buildBookingCard(b)).join('');
    } catch (e) {
        list.innerHTML = '<div style="text-align:center;padding:40px;color:#ef4444;font-size:13px;">Failed to load bookings.</div>';
    }
}

function buildBookingCard(b) {
    const statusColor = { CONFIRMED: '#22c55e', PENDING: '#f59e0b', CANCELLED: '#ef4444' };
    const statusLabel = { CONFIRMED: 'Approved', PENDING: 'Pending', CANCELLED: 'Cancelled' };
    const payColor    = b.paymentStatus === 'PAID' ? '#22c55e' : '#ef4444';
    const payLabel    = b.paymentStatus === 'PAID' ? 'Paid' : 'Unpaid';
    const fmtDate = d => d ? new Date(d).toLocaleDateString('en-US', { day: '2-digit', month: 'short', year: 'numeric' }) : '—';
    const fmtAmt  = v => v ? 'NPR ' + Number(v).toLocaleString('en-NP', { minimumFractionDigits: 2 }) : '—';
    const imgHtml = b.vehicleImageUrl
        ? `<img src="${b.vehicleImageUrl}" alt="${b.vehicleName}" class="booking-card-img"/>`
        : `<div class="booking-card-img-placeholder"><svg viewBox="0 0 24 24" fill="#c7d2fe" width="40" height="40"><path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.85 7h10.29l1.04 3H5.81l1.04-3zM19 17H5v-5h14v5z"/></svg></div>`;
    const canCancel   = b.bookingStatus !== 'CANCELLED';
    const isApproved  = b.bookingStatus === 'CONFIRMED';
    const canPay      = isApproved && b.paymentStatus !== 'PAID';
    const showPayLocked = !isApproved && b.bookingStatus !== 'CANCELLED' && b.paymentStatus !== 'PAID';

    // Deadline Calculation: 24 hours before startDate
    let deadlineMsg = '';
    if (b.paymentStatus !== 'PAID' && b.bookingStatus !== 'CANCELLED') {
        const start = new Date(b.startDate);
        const deadline = new Date(start);
        deadline.setDate(start.getDate() - 1); // 24 hours before
        deadlineMsg = `<div class="booking-deadline">Deadline: Pay before ${fmtDate(deadline)}</div>`;
    }

    let payBtn = '';
    if (canPay) {
        payBtn = `<button class="btn-pay-booking" id="pay-btn-${b.bookingId}" onclick="confirmPayBooking(${b.bookingId})">Pay Now</button>`;
    } else if (showPayLocked) {
        payBtn = `<button class="btn-pay-booking" style="opacity:0.5;cursor:not-allowed;background:#94a3b8;border-color:#94a3b8;" onclick="showPayLockedMsg()" disabled>Pay Now</button>`;
    }

    return `
        <div class="booking-card" id="booking-${b.bookingId}">
            <div class="booking-card-left">
                ${imgHtml}
                <div class="booking-vehicle-label">${b.vehicleName || 'Vehicle'}</div>
            </div>
            <div class="booking-card-mid">
                <div class="booking-ref-row">
                    <span class="booking-status-badge" style="background:${statusColor[b.bookingStatus] || '#94a3b8'}20;color:${statusColor[b.bookingStatus] || '#94a3b8'};border:1px solid ${statusColor[b.bookingStatus] || '#94a3b8'}40">
                        ${statusLabel[b.bookingStatus] || b.bookingStatus}
                    </span>
                </div>
                <div class="booking-meta-row"><span class="booking-meta-label">Start Date</span><span>${fmtDate(b.startDate)}</span></div>
                <div class="booking-meta-row"><span class="booking-meta-label">End Date</span><span>${fmtDate(b.endDate)}</span></div>
                <div class="booking-meta-row"><span class="booking-meta-label">Total Amount</span><strong>${fmtAmt(b.totalPrice)}</strong></div>
            </div>
            <div class="booking-card-right">
                <div class="booking-meta-row">
                    <span class="booking-meta-label">Payment</span>
                    <span style="color:${payColor};font-weight:600;">${payLabel}</span>
                </div>
                ${b.pickupLocation  ? `<div class="booking-meta-row"><span class="booking-meta-label">From</span><span>${b.pickupLocation}</span></div>`  : ''}
                ${b.dropoffLocation ? `<div class="booking-meta-row"><span class="booking-meta-label">To</span><span>${b.dropoffLocation}</span></div>` : ''}
                <div class="booking-card-actions">
                    ${payBtn}
                    ${canCancel ? `<button class="btn-cancel-booking" onclick="confirmCancelBooking(${b.bookingId})">Cancel</button>` : ''}
                </div>
                ${deadlineMsg}
            </div>
        </div>`;
}

function showPayLockedMsg() {
    const popup = document.getElementById('payLockedPopup');
    if (popup) popup.style.display = 'flex';
}

// Opens eSewa confirmation modal
function confirmPayBooking(id) {
    document.getElementById('esewaBookingId').value = id;
    document.getElementById('esewaConfirmModal').style.display = 'flex';
}

// Called when user clicks "Pay via eSewa" inside the modal
async function proceedEsewaPayment() {
    const id  = document.getElementById('esewaBookingId').value;
    const btn = document.getElementById('esewaConfirmBtn');
    btn.disabled    = true;
    btn.textContent = 'Loading...';

    try {
        const res = await fetch(`${API}/api/payment/esewa/initiate/${id}`, {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        if (!res.ok) {
            const msg = await res.text();
            showToast(msg || 'Could not initiate payment.', 'error');
            return;
        }
        const data = await res.json();

        // Build a hidden form and submit to eSewa
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = data.esewaUrl;
        form.style.display = 'none';
        Object.entries(data.fields).forEach(([key, val]) => {
            const input = document.createElement('input');
            input.type  = 'hidden';
            input.name  = key;
            input.value = val;
            form.appendChild(input);
        });
        document.body.appendChild(form);
        form.submit();
    } catch (e) {
        showToast('Something went wrong. Please try again.', 'error');
        btn.disabled    = false;
        btn.textContent = 'Pay via eSewa';
    }
}

function confirmCancelBooking(id) {
    if (!confirm('Cancel this booking? This cannot be undone.')) return;
    cancelBooking(id);
}

async function cancelBooking(id) {
    showLoading(true);
    try {
        const res = await fetch(`${API}/api/customer/bookings/${id}/cancel`, {
            method: 'PUT',
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        if (!res.ok) throw new Error('Failed');
        showToast('Booking cancelled.', 'success');
        loadMyBookings();
    } catch (e) {
        showToast('Failed to cancel booking.', 'error');
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
    localStorage.removeItem('profileImage');
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
