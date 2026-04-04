function showAlert(msg, type) {
  const a = document.getElementById('alert');
  a.textContent = msg;
  a.className = 'alert ' + type;
}

function clearAlert() { document.getElementById('alert').className = 'alert'; }

function setLoading(val) {
  document.getElementById('loginBtn').disabled = val;
  document.getElementById('btnText').style.display = val ? 'none' : 'inline';
  document.getElementById('spinner').style.display = val ? 'block' : 'none';
}

async function handleLogin() {
  clearAlert();
  const email    = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value;

  if (!email || !password) {
    showAlert('Please enter your email and password.', 'error'); return;
  }

  setLoading(true);
  try {
    const res = await fetch('http://localhost:8081/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });

    const data = await res.json();

    if (res.ok) {
      localStorage.setItem('token', data.token);
      localStorage.setItem('role', data.role);
      localStorage.setItem('email', data.email);
      localStorage.setItem('firstName', data.firstName);

      showAlert('Login successful! Redirecting...', 'success');

      setTimeout(() => {
        if (data.role === 'admin')       window.location.href = '/admin/dashboard';
        else if (data.role === 'vendor') window.location.href = '/vendor/dashboard';
        else                             window.location.href = '/home';
      }, 1000);
    } else {
      showAlert(data || 'Invalid email or password.', 'error');
    }
  } catch (err) {
    showAlert('Cannot connect to server. Is the backend running?', 'error');
  } finally {
    setLoading(false);
  }
}

document.addEventListener('keydown', e => { if (e.key === 'Enter') handleLogin(); });
