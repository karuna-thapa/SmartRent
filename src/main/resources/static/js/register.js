// ── Set DOB max to 18 years ago on page load ──────────────────────────────────
(function setDobMax() {
  const dob = document.getElementById('dob');
  if (!dob) return;
  const today = new Date();
  today.setFullYear(today.getFullYear() - 18);
  dob.max = today.toISOString().split('T')[0];
})();

// ── Clear all customer & vendor fields ────────────────────────────────────────
function clearCustomerForm() {
  ['firstName','lastName','email','phone','dob','address','licenseNo','password','confirmPassword']
    .forEach(id => { const el = document.getElementById(id); if (el) el.value = ''; });
}

function clearVendorForm() {
  ['vendorName','companyName','vendorEmail','vendorPhone','registrationNo','vendorPassword','vendorConfirmPassword']
    .forEach(id => { const el = document.getElementById(id); if (el) el.value = ''; });
  const brand = document.getElementById('brandName');
  if (brand) brand.selectedIndex = 0;
}

function switchRole(role, el) {
  document.querySelectorAll('.role-tab').forEach(t => t.classList.remove('active'));
  el.classList.add('active');
  document.getElementById('customerForm').classList.toggle('active', role === 'customer');
  document.getElementById('vendorForm').classList.toggle('active', role === 'vendor');
  clearAlert();
  if (role === 'vendor') loadBrands();
}

function showAlert(msg, type) {
  const a = document.getElementById('alert');
  a.textContent = msg;
  a.className = 'alert ' + type;
}

function clearAlert() { document.getElementById('alert').className = 'alert'; }

function setLoading(btnId, textId, spinnerId, val) {
  const btn = document.getElementById(btnId);
  if (!btn) return;
  btn.disabled = val;
  document.getElementById(textId).style.display = val ? 'none' : 'inline';
  document.getElementById(spinnerId).style.display = val ? 'block' : 'none';
}

function v(id) { return document.getElementById(id).value.trim(); }

// ── Brand fallback list (always available even if DB is empty) ────────────────
const FALLBACK_BRANDS = [
  // Cars
  'Toyota', 'Honda', 'Hyundai', 'Suzuki', 'Kia',
  'Nissan', 'Mitsubishi', 'Ford', 'BMW', 'Mercedes-Benz',
  'Volkswagen', 'Tata', 'Mahindra', 'Skoda', 'MG',
  // Bikes
  'Yamaha', 'Bajaj', 'Hero', 'TVS', 'Royal Enfield',
  'KTM', 'Kawasaki', 'Suzuki Moto', 'Pulsar', 'Apache',
  // Scooters / EV
  'Mercury', 'Ather', 'Revolt', 'Ola Electric', 'Ninebot'
];

// ── Load brands into the vendor dropdown ──────────────────────────────────────
async function loadBrands() {
  const select = document.getElementById('brandName');
  if (!select || select.options.length > 1) return; // already loaded

  // Step 1 — populate immediately from fallback so dropdown is never empty
  const seen = new Set();
  FALLBACK_BRANDS.forEach(name => {
    seen.add(name.toLowerCase());
    const opt = document.createElement('option');
    opt.value = name;
    opt.textContent = name;
    select.appendChild(opt);
  });

  // Step 2 — merge in any extra brands stored in the DB
  try {
    const res = await fetch('/api/brands');
    if (!res.ok) return;
    const brands = await res.json();
    brands.forEach(b => {
      if (!seen.has(b.brandName.toLowerCase())) {
        const opt = document.createElement('option');
        opt.value = b.brandName;
        opt.textContent = b.brandName;
        select.appendChild(opt);
      }
    });
    // Re-sort options alphabetically (keep placeholder first)
    const opts = Array.from(select.options).slice(1).sort((a, b) => a.text.localeCompare(b.text));
    while (select.options.length > 1) select.remove(1);
    opts.forEach(o => select.appendChild(o));
  } catch (_) { /* API unavailable — fallback list is sufficient */ }
}

// ── Customer registration ─────────────────────────────────────────────────────
async function submitCustomer() {
  clearAlert();
  const firstName = v('firstName'), lastName = v('lastName'),
        email = v('email'), phone = v('phone'), dob = v('dob'),
        address = v('address'), licenseNo = v('licenseNo'),
        password = document.getElementById('password').value,
        confirmPw = document.getElementById('confirmPassword').value;

  if (!firstName || !lastName || !email || !phone || !dob || !licenseNo || !password || !confirmPw) {
    showAlert('Please fill in all required fields.', 'error'); return;
  }

  // Email format validation
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    showAlert('Please enter a valid email address (e.g. name@example.com).', 'error'); return;
  }

  // 18+ age check
  const birthDate = new Date(dob);
  const minDate = new Date();
  minDate.setFullYear(minDate.getFullYear() - 18);
  if (birthDate > minDate) {
    showAlert('You must be at least 18 years old to register.', 'error'); return;
  }

  if (password.length < 6) { showAlert('Password must be at least 6 characters.', 'error'); return; }
  if (password !== confirmPw) { showAlert('Passwords do not match!', 'error'); return; }

  setLoading('registerBtn', 'btnText', 'spinner', true);
  try {
    const res = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ firstName, lastName, email, password, phoneNumber: phone, address, dob, licenseNo })
    });
    const data = await res.text();
    if (res.ok || res.status === 201) {
      clearCustomerForm();
      showAlert('Account created successfully! Redirecting to login…', 'success');
      setTimeout(() => {
        window.location.href = `/login?email=${encodeURIComponent(email)}`;
      }, 1500);
    } else {
      showAlert(data || 'Registration failed. Please try again.', 'error');
    }
  } catch (err) {
    showAlert('Cannot connect to server. Is the backend running?', 'error');
  } finally {
    setLoading('registerBtn', 'btnText', 'spinner', false);
  }
}

// ── Vendor registration ───────────────────────────────────────────────────────
async function submitVendor() {
  clearAlert();
  const vendorName    = v('vendorName');
  const companyName   = v('companyName');
  const email         = v('vendorEmail');
  const phoneNumber   = v('vendorPhone');
  const registrationNo = v('registrationNo');
  const brandName     = v('brandName');
  const password      = document.getElementById('vendorPassword').value;
  const confirmPw     = document.getElementById('vendorConfirmPassword').value;

  if (!vendorName || !companyName || !email || !phoneNumber || !registrationNo || !password || !confirmPw) {
    showAlert('Please fill in all required fields.', 'error'); return;
  }
  if (password.length < 6) { showAlert('Password must be at least 6 characters.', 'error'); return; }
  if (password !== confirmPw) { showAlert('Passwords do not match!', 'error'); return; }

  setLoading('vendorRegisterBtn', 'vendorBtnText', 'vendorSpinner', true);
  try {
    const res = await fetch('/api/auth/register-vendor', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ vendorName, companyName, email, password, phoneNumber, registrationNo, brandName })
    });
    const data = await res.text();
    if (res.ok || res.status === 201) {
      clearVendorForm();
      showAlert('Registration submitted! Awaiting admin approval.', 'success');
      setTimeout(() => { window.location.href = '/login'; }, 2200);
    } else {
      showAlert(data || 'Registration failed. Please try again.', 'error');
    }
  } catch (err) {
    showAlert('Cannot connect to server. Is the backend running?', 'error');
  } finally {
    setLoading('vendorRegisterBtn', 'vendorBtnText', 'vendorSpinner', false);
  }
}
