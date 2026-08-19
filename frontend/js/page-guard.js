/**
 * Secure Vault — Synchronous Page Guard
 * 
 * Runs immediately (non-deferred) in <head> before any HTML is painted.
 * For protected pages: hides <html> instantly if no valid session exists,
 * then redirects. For public pages with an active session: redirects before
 * login/register content ever renders.
 * 
 * This eliminates the "shaking" / flash-of-unstyled-content (FOUC) caused
 * by deferred auth.js running after the body has already been painted.
 */
(function () {
    var path = window.location.pathname;

    var isPublicPage = path.endsWith('index.html') ||
        path.endsWith('/') ||
        path.endsWith('login.html') ||
        path.endsWith('register.html') ||
        path.endsWith('forgot-password.html') ||
        path.endsWith('reset-password.html');

    var isAdminPage = path.indexOf('admin-') !== -1;
    var isProtectedPage = !isPublicPage;

    // Read session from localStorage synchronously (no network, no delay)
    var token = localStorage.getItem('sv_token');
    var userJson = localStorage.getItem('sv_user');
    var user = null;
    try { user = userJson ? JSON.parse(userJson) : null; } catch (e) { }
    var authenticated = !!token;

    if (isProtectedPage) {
        if (!authenticated) {
            // Hide instantly then redirect — zero flash
            document.documentElement.style.visibility = 'hidden';
            window.location.replace('login.html');
        } else if (isAdminPage && user && user.role !== 'ADMIN') {
            document.documentElement.style.visibility = 'hidden';
            window.location.replace('dashboard.html');
        }
        // Auth OK — page renders normally, no need to hide
    } else if (isPublicPage && authenticated &&
        (path.endsWith('login.html') || path.endsWith('register.html'))) {
        // Already logged in — skip login/register, redirect before paint
        document.documentElement.style.visibility = 'hidden';
        var dest = (user && user.role === 'ADMIN') ? 'admin-dashboard.html' : 'dashboard.html';
        window.location.replace(dest);
    }
})();
