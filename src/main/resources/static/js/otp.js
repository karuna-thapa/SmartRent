const params = new URLSearchParams(window.location.search);
const email  = params.get('email') || '';

document.title = 'SmartRent — Login Verification';

function showAlert(msg, kind) {
    const a = document.getElementById('alert');
    a.textContent = msg;
    a.className = 'alert ' + kind;
}
function clearAlert() { document.getElementById('alert').className = 'alert'; }

const boxes = Array.from({ length: 6 }, (_, i) => document.getElementById('b' + i));

boxes.forEach((box, i) => {
    box.addEventListener('input', () => {
        box.value = box.value.replace(/\D/g, '').slice(-1);
        box.classList.toggle('filled', box.value !== '');
        if (box.value && i < 5) boxes[i + 1].focus();
        if (getOtp().length === 6) verifyOtp();
    });

    box.addEventListener('keydown', e => {
        if (e.key === 'Backspace' && !box.value && i > 0) {
            boxes[i - 1].value = '';
            boxes[i - 1].classList.remove('filled');
            boxes[i - 1].focus();
        }
        if (e.key === 'Enter') verifyOtp();
    });

    box.addEventListener('paste', e => {
        e.preventDefault();
        const text = e.clipboardData.getData('text').replace(/\D/g, '');
        text.split('').slice(0, 6).forEach((ch, j) => {
            if (boxes[j]) { boxes[j].value = ch; boxes[j].classList.add('filled'); }
        });
        boxes[Math.min(text.length, 5)].focus();
        if (text.length >= 6) verifyOtp();
    });
});

function getOtp() { return boxes.map(b => b.value).join(''); }

function clearBoxes() {
    boxes.forEach(b => { b.value = ''; b.classList.remove('filled'); });
    boxes[0].focus();
}

function setLoading(val) {
    document.getElementById('verifyBtn').disabled = val;
    document.getElementById('verifyBtnText').style.display = val ? 'none'  : 'inline';
    document.getElementById('verifySpinner').style.display = val ? 'block' : 'none';
}

async function verifyOtp() {
    clearAlert();
    const otp = getOtp();
    if (otp.length !== 6) {
        showAlert('Please enter all 6 digits of the verification code.', 'error');
        return;
    }

    setLoading(true);
    try {
        const res  = await fetch('/api/auth/login/verify-otp', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, otp })
        });
        const data = await res.json();

        if (res.ok) {
            localStorage.setItem('token',     data.token);
            localStorage.setItem('role',      data.role);
            localStorage.setItem('email',     data.email);
            localStorage.setItem('firstName', data.firstName);
            document.getElementById('successPopup').classList.add('show');
            setTimeout(() => {
                if (data.role === 'admin')       window.location.href = '/admin/dashboard';
                else if (data.role === 'vendor') window.location.href = '/vendor/dashboard';
                else                             window.location.href = '/home';
            }, 1500);
        } else {
            showAlert('The code is incorrect or has expired. Please try again.', 'error');
            clearBoxes();
        }
    } catch (err) {
        showAlert('Something went wrong. Please try again.', 'error');
    } finally {
        setLoading(false);
    }
}

let countdownTimer = null;

function startCountdown(seconds) {
    const link    = document.getElementById('resendLink');
    const display = document.getElementById('countdown');
    link.classList.add('disabled');
    let remaining = seconds;
    display.textContent =  (s);
    countdownTimer = setInterval(() => {
        remaining--;
        display.textContent =  (s);
        if (remaining <= 0) {
            clearInterval(countdownTimer);
            display.textContent = '';
            link.classList.remove('disabled');
        }
    }, 1000);
}

async function resendOtp() {
    clearAlert();
    try {
        const res  = await fetch('/api/auth/resend-login-otp', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email })
        });
        const text = await res.text();
        if (res.ok) {
            showAlert('A new code has been sent to ' + email + '. Check your inbox.', 'success');
            clearBoxes();
            startCountdown(30);
        } else {
            showAlert(text || 'Failed to resend. Please try again.', 'error');
        }
    } catch (_) {
        showAlert('Could not connect to server.', 'error');
    }
}

window.addEventListener('DOMContentLoaded', () => {
    boxes[0].focus();
    startCountdown(30);
});
