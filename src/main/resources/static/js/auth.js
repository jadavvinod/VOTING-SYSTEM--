// Redirects to /login if no token is present, or if the role doesn't match what's required.
function requireAuth(requiredRole) {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    if (!token || (requiredRole && role !== requiredRole)) {
        window.location.href = '/login';
        return null;
    }
    return token;
}

// Wrapper around fetch that attaches the JWT automatically and handles 401s.
async function authFetch(url, options = {}) {
    const token = localStorage.getItem('token');
    options.headers = Object.assign({}, options.headers, {
        'Authorization': 'Bearer ' + token,
        'Content-Type': 'application/json'
    });
    const response = await fetch(url, options);
    if (response.status === 401 || response.status === 403) {
        localStorage.clear();
        window.location.href = '/login';
        return null;
    }
    return response;
}

function logout() {
    localStorage.clear();
    window.location.href = '/login';
}
