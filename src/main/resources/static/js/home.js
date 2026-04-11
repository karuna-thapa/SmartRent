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

        grid.innerHTML = brands.map(b => {
            const logoHtml = b.brandLogoUrl
                ? `<img src="${b.brandLogoUrl}" alt="${b.brandName}" class="brand-logo-img"
                        onerror="this.style.display='none';this.nextElementSibling.style.display='flex'"/>
                   <div class="brand-logo-circle" style="display:none">${b.brandName.charAt(0)}</div>`
                : `<div class="brand-logo-circle">${b.brandName.charAt(0)}</div>`;
            return `
                <div class="brand-card" onclick="filterByBrand(${b.brandId})">
                    ${logoHtml}
                    <p class="brand-name">${b.brandName}</p>
                    <p class="brand-count">${b.vehicleCount} Vehicle${b.vehicleCount !== 1 ? 's' : ''}</p>
                </div>
            `;
        }).join('');
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
    const price = v.rentalPrice
        ? `Rs. ${Number(v.rentalPrice).toLocaleString()}`
        : 'N/A';

    const badge = v.categoryName || 'Vehicle';
    const seats = v.seatsCapacity ? `${v.seatsCapacity} Seats` : '';
    const brand  = v.brandName || '';

    const imgHtml = v.imageUrl
        ? `<img src="${v.imageUrl}" alt="${v.vehicleName}" style="width:100%;height:140px;object-fit:cover;display:block;"
                onerror="this.outerHTML='<div class=vehicle-img-svg-fallback></div>'">`
        : `<svg viewBox="0 0 200 120" fill="none">
                <rect width="200" height="120" rx="12" fill="#f0f4ff"/>
                <rect x="15" y="55" width="170" height="50" rx="6" fill="#dde4ff"/>
                <path d="M25 55 L50 25 L150 25 L175 55" fill="#c5cfff"/>
                <circle cx="50" cy="105" r="14" fill="#4a6cf7" opacity="0.7"/>
                <circle cx="150" cy="105" r="14" fill="#4a6cf7" opacity="0.7"/>
                <rect x="60" y="27" width="80" height="28" rx="3" fill="#a0b0ff" opacity="0.5"/>
           </svg>`;

    return `
        <div class="vehicle-card">
            <div class="vehicle-img">
                ${imgHtml}
            </div>
            <div class="vehicle-info">
                <div class="vehicle-header">
                    <h3 class="vehicle-name">${v.vehicleName}</h3>
                    <span class="vehicle-badge">${badge}</span>
                </div>
                <div class="vehicle-specs">
                    ${seats ? `<span>${seats}</span>` : ''}
                    ${brand  ? `<span>${brand}</span>`  : ''}
                </div>
                <div class="vehicle-price">
                    <span class="price">${price}</span>
                    <span class="price-per">/day</span>
                </div>
                <button class="btn-book" onclick="bookVehicle(${v.vehicleId})">Book Now</button>
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
