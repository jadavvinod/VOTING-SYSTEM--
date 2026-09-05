function requireAuth(requiredRole) {

    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');

    console.log('AUTH CHECK');
    console.log('Path:', window.location.pathname);
    console.log('Required role:', requiredRole);
    console.log('Stored role:', role);
    console.log('Token exists:', !!token);

    if (!token) {
        console.error('AUTH FAILED: No token');
        window.location.replace('/login');
        return false;
    }

    if (requiredRole && role !== requiredRole) {
        console.error(
            'AUTH FAILED: Role mismatch. Required:',
            requiredRole,
            'Actual:',
            role
        );

        /*
         * Do NOT clear the token here.
         * This allows us to diagnose frontend role problems.
         */
        return false;
    }

    console.log('AUTH SUCCESS');

    return token;
}


async function authFetch(url, options = {}) {

    const token = localStorage.getItem('token');

    if (!token) {
        console.error('AUTH FETCH FAILED: No token');
        window.location.replace('/login');
        return null;
    }

    options.headers = Object.assign(
        {},
        options.headers || {},
        {
            'Authorization': 'Bearer ' + token,
            'Content-Type': 'application/json'
        }
    );

    try {

        const response =
            await fetch(url, options);

        console.log(
            'AUTH FETCH:',
            url,
            'STATUS:',
            response.status
        );

        if (response.status === 401 ||
            response.status === 403) {

            console.error(
                'SERVER AUTH FAILED:',
                response.status
            );

            localStorage.removeItem('token');
            localStorage.removeItem('role');
            localStorage.removeItem('fullName');

            window.location.replace('/login');

            return null;
        }

        return response;

    } catch (error) {

        console.error(
            'AUTH FETCH ERROR:',
            error
        );

        throw error;
    }
}


function logout() {

    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('fullName');

    window.location.replace('/login');
}