const API = (() => {
  const BASE_URL = 'http://localhost:8081/api';

  function getToken() {
    return sessionStorage.getItem('jwt_token') || localStorage.getItem('jwt_token');
  }

  function buildHeaders(includeAuth) {
    const headers = { 'Content-Type': 'application/json' };
    if (includeAuth) {
      const token = getToken();
      if (token) headers['Authorization'] = `Bearer ${token}`;
    }
    return headers;
  }

  async function request(method, endpoint, body = null, auth = true) {
    const options = {
      method,
      headers: buildHeaders(auth),
    };
    if (body) options.body = JSON.stringify(body);

    const response = await fetch(`${BASE_URL}${endpoint}`, options);

    const data = await response.json().catch(() => ({}));

    if (response.status === 401) {
      if (data.message === 'SESSION_IP_CHANGED') {
        showIpChangedBanner();
        Auth.clearSession();
        setTimeout(() => { window.location.href = 'login.html'; }, 3000);
        return;
      }
      Auth.clearSession();
      window.location.href = 'login.html';
      return;
    }

    if (response.status === 403) {
      window.location.href = 'unauthorized.html';
      return;
    }

    if (!response.ok) {
      throw { status: response.status, message: data.message || 'An unexpected error occurred.' };
    }

    // Unwrap ApiResponse wrapper — return .data so callers get the actual payload
    return (data && 'success' in data && 'data' in data) ? data.data : data;
  }

  function showIpChangedBanner() {
    const banner = document.createElement('div');
    banner.style.cssText = [
      'position:fixed', 'top:0', 'left:0', 'right:0', 'z-index:99999',
      'background:#f85149', 'color:#fff', 'text-align:center',
      'padding:18px 24px', 'font-size:1rem', 'font-weight:600',
      'box-shadow:0 4px 20px rgba(248,81,73,0.4)',
    ].join(';');
    banner.innerHTML = '&#9888; Security Alert: Your IP address has changed — logging you out for your protection…';
    document.body.prepend(banner);
  }

  return {
    get:    (endpoint, auth = true)        => request('GET',    endpoint, null, auth),
    post:   (endpoint, body, auth = true)  => request('POST',   endpoint, body, auth),
    put:    (endpoint, body, auth = true)  => request('PUT',    endpoint, body, auth),
    delete: (endpoint, auth = true)        => request('DELETE', endpoint, null, auth),
    getToken,
  };
})();
