function switchRole(role, el) {
  document.querySelectorAll('.role-tab').forEach(t => t.classList.remove('active'));
  el.classList.add('active');
  document.getElementById('customerForm').classList.toggle('active', role === 'customer');
  document.getElementById('vendorForm').classList.toggle('active', role === 'vendor');
  clearAlert();
}

function showAlert(msg, type) {
  const a = document.getElementById('alert');
  a.textContent = msg;
  a.className = 'alert ' + type;
}

function clearAlert() { document.getElementById('alert').className = 'alert'; }

function setLoading(val) {
  const btn = document.getElementById('registerBtn');
  if (!btn) return;
  btn.disabled = val;
  document.getElementById('btnText').style.display = val ? 'none' : 'inline';
  document.getElementById('spinner').style.display = val ? 'block' : 'none';
}

function v(id) { return document.getElementById(id).value.trim(); }

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
  if (password.length < 6) { showAlert('Password must be at least 6 characters.', 'error'); return; }
  if (password !== confirmPw) { showAlert('Passwords do not match!', 'error'); return; }

  setLoading(true);
  try {
    const res = await fetch('http://localhost:8081/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ firstName, lastName, email, password, phoneNumber: phone, address, dob, licenseNo })
    });
    const data = await res.text();
    if (res.ok || res.status === 201) {
      showAlert('Account created successfully! Redirecting to login...', 'success');
      setTimeout(() => { window.location.href = 'login.html'; }, 1500);
    } else {
      showAlert(data || 'Registration failed. Please try again.', 'error');
    }
  } catch (err) {
    showAlert('Cannot connect to server. Is the backend running?', 'error');
  } finally {
    setLoading(false);
  }
}

function submitVendor() {
  showAlert('Vendor registration coming soon!', 'error');
}
