const API = 'http://localhost:8081';

// ===== INIT =====
document.addEventListener('DOMContentLoaded', () => {
    setDefaultDates();
    loadBrands();
    loadFeaturedVehicles();
    loadRecentReviews();
    initReturnToggle();
});

// ===== RETURN TO DIFFERENT LOCATION TOGGLE =====
function initReturnToggle() {
    const toggle = document.getElementById('returnDiff');
    const field  = document.getElementById('dropoffLocationField');
    if (!toggle || !field) return;

    toggle.addEventListener('change', () => {
        if (toggle.checked) {
            field.classList.add('visible');
        } else {
            field.classList.remove('visible');
            const input = document.getElementById('dropoffLocation');
            if (input) input.value = '';
        }
    });
}

// ===== DATE DEFAULTS =====
function setDefaultDates() {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(today.getDate() + 1);
    const fmt = d => d.toISOString().split('T')[0];
    const pd = document.getElementById('pickupDate');
    const dd = document.getElementById('dropoffDate');
    if (pd) pd.value = fmt(today);
    if (dd) dd.value = fmt(tomorrow);
}

// ===== LOAD BRANDS =====
async function loadBrands() {
    const grid = document.getElementById('brandsGrid');
    if (!grid) return;

    try {
        const res = await fetch(`${API}/api/brands`);
        const brands = await res.json();

        if (!brands.length) return; // keep static placeholders

        grid.innerHTML = brands.map(b => `
                <div class="brand-card" onclick="filterByBrand(${b.brandId})">
                    <div class="brand-logo-circle">${b.brandName.charAt(0)}</div>
                    <p class="brand-name">${b.brandName}</p>
                    <p class="brand-count">${b.vehicleCount} Vehicle${b.vehicleCount !== 1 ? 's' : ''}</p>
                </div>
            `).join('');
    } catch (e) {
        // Network error — keep static placeholders
    }
}

// ===== LOAD FEATURED VEHICLES =====
async function loadFeaturedVehicles() {
    const grid = document.getElementById('vehiclesGrid');
    if (!grid) return;

    try {
        const res = await fetch(`${API}/api/vehicles/featured`);
        const vehicles = await res.json();

        if (!vehicles.length) return; // keep static placeholders

        grid.innerHTML = vehicles.map(v => buildVehicleCard(v)).join('');
    } catch (e) {
        // keep static placeholders
    }
}

function buildVehicleCard(v) {
    const imgSrc = v.imageUrl || '';
    const ratingHtml = v.averageRating
        ? `<svg width="14" height="14" viewBox="0 0 20 20" fill="#f5a623"><path d="M10 1l2.5 5 5.5.8-4 3.9.9 5.5L10 13.5l-4.9 2.6.9-5.5L2 6.8l5.5-.8z"/></svg><span class="card-rating-val">${Number(v.averageRating).toFixed(1)}</span>`
        : `<span class="card-no-reviews">No reviews</span>`;

    return `
        <div class="vehicle-card">
            <div class="card-image">
                ${imgSrc ? `<img src="${imgSrc}" alt="${v.vehicleName}" onerror="this.style.display='none'">` : ''}
            </div>
            <div class="card-body">
                <div>
                    <h3 class="card-name">${v.vehicleName}</h3>
                    <span class="card-category-label">${v.categoryName || 'Vehicle'}</span>
                </div>
                <div class="card-meta-item">
                    <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                    <span>${v.seatsCapacity || '—'} Seats</span>
                </div>
                <div class="card-rating-price-row">
                    <div class="card-rating">${ratingHtml}</div>
                    <div class="price-block">
                        <span class="price-amount">NRS ${Number(v.rentalPrice).toLocaleString()}</span>
                        <span class="price-unit">/day</span>
                    </div>
                </div>
                <button class="btn-primary-card" onclick="bookVehicle(${v.vehicleId})">Book Now</button>
            </div>
        </div>
    `;
}

// ===== LOAD REVIEWS =====
async function loadRecentReviews() {
    const grid = document.getElementById('reviewsGrid');
    if (!grid) return;

    try {
        const res = await fetch(`${API}/api/reviews/recent`);
        const reviews = await res.json();

        if (!reviews.length) return; // keep static placeholders

        grid.innerHTML = reviews.map(r => buildReviewCard(r)).join('');
    } catch (e) {
        // keep static placeholders
    }
}

function buildReviewCard(r) {
    const stars = buildStars(r.rating);
    const initial = r.customerName ? r.customerName.charAt(0).toUpperCase() : '?';
    const colors = ['#4a6cf7', '#f5a623', '#22c55e', '#a855f7', '#ef4444', '#06b6d4'];
    const color = colors[(r.reviewId || 0) % colors.length];

    return `
        <div class="review-card">
            <div class="review-stars">${stars}</div>
            <p class="review-text">"${r.comment || 'Great experience!'}"</p>
            <div class="reviewer">
                <div class="reviewer-avatar" style="background:${color}">${initial}</div>
                <div>
                    <p class="reviewer-name">${r.customerName || 'Customer'}</p>
                    <p class="reviewer-role">${r.vehicleName ? `Rented: ${r.vehicleName}` : 'Verified Customer'}</p>
                </div>
            </div>
        </div>
    `;
}

function buildStars(rating) {
    const filled = `<svg viewBox="0 0 20 20" fill="#f5a623"><path d="M10 1l2.5 5 5.5.8-4 3.9.9 5.5L10 13.5l-4.9 2.6.9-5.5L2 6.8l5.5-.8z"/></svg>`;
    const empty  = `<svg viewBox="0 0 20 20" fill="#ddd"><path d="M10 1l2.5 5 5.5.8-4 3.9.9 5.5L10 13.5l-4.9 2.6.9-5.5L2 6.8l5.5-.8z"/></svg>`;
    let html = '';
    for (let i = 1; i <= 5; i++) html += i <= rating ? filled : empty;
    return html;
}

// ===== BOOKING FORM =====
function clearField(id) {
    const el = document.getElementById(id);
    if (el) el.value = '';
}

function searchVehicles() {
    const pickup      = document.getElementById('pickupLocation')?.value?.trim();
    const pickupDate  = document.getElementById('pickupDate')?.value;
    const dropoffDate = document.getElementById('dropoffDate')?.value;

    if (!pickup) {
        showBookingError('Please enter a pick-up location.');
        document.getElementById('pickupLocation')?.focus();
        return;
    }

    if (!pickupDate || !dropoffDate) {
        showBookingError('Please select pick-up and drop-off dates.');
        return;
    }

    if (new Date(dropoffDate) <= new Date(pickupDate)) {
        showBookingError('Drop-off date must be after pick-up date.');
        return;
    }

    window.location.href = `/vehicle-rentals`;
}

function showBookingError(msg) {
    let el = document.getElementById('bookingError');
    if (!el) {
        el = document.createElement('p');
        el.id = 'bookingError';
        el.style.cssText = 'color:#ef4444;font-size:12px;margin-top:8px;';
        document.querySelector('.booking-bottom')?.after(el);
    }
    el.textContent = msg;
    setTimeout(() => el && (el.textContent = ''), 4000);
}

// ===== NAVIGATION HELPERS =====
function filterByBrand(brandId) {
    window.location.href = `/vehicles.html?brandId=${brandId}`;
}

function bookVehicle(vehicleId) {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = `/login?redirect=vehicles&vehicleId=${vehicleId}`;
        return;
    }
    window.location.href = `/booking.html?vehicleId=${vehicleId}`;
}

// ===== HAMBURGER MENU =====
const hamburger = document.getElementById('hamburger');
if (hamburger) {
    hamburger.addEventListener('click', () => {
        document.querySelector('.nav-links')?.classList.toggle('mobile-open');
        document.querySelector('.nav-auth')?.classList.toggle('mobile-open');
    });
}
