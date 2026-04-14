function showAlert(msg, type) {
    const a = document.getElementById('alert');
    a.textContent = msg;
    a.className = 'alert ' + type;
}
function clearAlert() { document.getElementById('alert').className = 'alert'; }

function setLoading(val) {
    document.getElementById('loginBtn').disabled = val;
    document.getElementById('btnText').style.display   = val ? 'none'  : 'inline';
    document.getElementById('spinner').style.display   = val ? 'block' : 'none';
}

async function handleLogin() {
    clearAlert();
    const email    = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;

    if (!email || !password) {
        showAlert('Please enter your email and password.', 'error');
        return;
    }

    // Basic email format check
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        showAlert('Please enter a valid email address.', 'error');
        return;
    }

    setLoading(true);
    try {
        const res  = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        const data = await res.json();

        if (res.ok) {
            if (data.requiresOtp) {
                // First-time login — redirect to OTP page
                window.location.href = `/verify-otp?email=${encodeURIComponent(data.email)}`;
            } else {
                // Already verified (or vendor) — JWT returned directly, go straight to dashboard
                localStorage.setItem('token',     data.token);
                localStorage.setItem('role',      data.role);
                localStorage.setItem('email',     data.email);
                localStorage.setItem('firstName', data.firstName);
                showAlert('Login successful! Redirecting...', 'success');
                setTimeout(() => {
                    if (data.role === 'admin')       window.location.href = '/admin/dashboard';
                    else if (data.role === 'vendor') window.location.href = '/vendor/dashboard';
                    else                             window.location.href = '/home';
                }, 1000);
            }
        } else {
            // Map backend messages to user-friendly text
            const raw = typeof data === 'string' ? data : '';
            let msg = 'Incorrect email or password. Please try again.';
            if (raw.includes('No account found'))   msg = 'No account found with this email. Please register first.';
            else if (raw.includes('Invalid password')) msg = 'Incorrect password. Please try again.';
            else if (raw.includes('pending admin'))    msg = 'Your vendor account is awaiting admin approval.';
            else if (raw.includes('rejected'))         msg = 'Your vendor application was not approved.';
            showAlert(msg, 'error');
        }
    } catch (err) {
        showAlert('Something went wrong. Please try again later.', 'error');
    } finally {
        setLoading(false);
    }
}

function togglePwVisibility(inputId, btn) {
    const input = document.getElementById(inputId);
    const isHidden = input.type === 'password';
    input.type = isHidden ? 'text' : 'password';
    btn.querySelector('.eye-show').style.display = isHidden ? 'none' : '';
    btn.querySelector('.eye-hide').style.display = isHidden ? ''     : 'none';
}

document.addEventListener('keydown', e => { if (e.key === 'Enter') handleLogin(); });
