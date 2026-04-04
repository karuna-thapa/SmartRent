// Shared navbar auth state — runs on every page
(function () {
    const token = localStorage.getItem('token');
    const firstName = localStorage.getItem('firstName');
    if (!token || !firstName) return;

    const navAuth = document.getElementById('navAuth');
    const navUser = document.getElementById('navUser');
    if (navAuth) navAuth.style.display = 'none';
    if (navUser) {
        navUser.style.display = 'flex';
        const initials = document.getElementById('navInitials');
        const nameEl   = document.getElementById('navUserName');
        if (initials) initials.textContent = firstName.charAt(0).toUpperCase();
        if (nameEl)   nameEl.textContent   = firstName;
    }
})();

function toggleUserDropdown() {
    document.getElementById('userDropdown')?.classList.toggle('open');
}

document.addEventListener('click', function (e) {
    const btn      = document.getElementById('userBtn');
    const dropdown = document.getElementById('userDropdown');
    if (btn && dropdown && !btn.contains(e.target)) {
        dropdown.classList.remove('open');
    }
});

function navLogout(e) {
    e.preventDefault();
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('email');
    localStorage.removeItem('firstName');
    window.location.href = '/home';
}
