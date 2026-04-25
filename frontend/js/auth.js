const Auth = (() => {

  const TOKEN_KEY  = 'jwt_token';
  const USER_KEY   = 'zt_user';
  const EXPIRY_KEY = 'jwt_expiry';

  // ── Token management ─────────────────────────────────────
  function saveSession(token, user, rememberDevice = false) {
    const storage = rememberDevice ? localStorage : sessionStorage;
    storage.setItem(TOKEN_KEY, token);
    storage.setItem(USER_KEY, JSON.stringify(user));

    const payload = parseJwt(token);
    if (payload && payload.exp) {
      storage.setItem(EXPIRY_KEY, payload.exp * 1000);
    }
  }

  function clearSession() {
    [TOKEN_KEY, USER_KEY, EXPIRY_KEY].forEach(k => {
      localStorage.removeItem(k);
      sessionStorage.removeItem(k);
    });
  }

  function getUser() {
    const raw = sessionStorage.getItem(USER_KEY) || localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }

  function isAuthenticated() {
    const token  = API.getToken();
    const expiry = sessionStorage.getItem(EXPIRY_KEY) || localStorage.getItem(EXPIRY_KEY);
    if (!token) return false;
    if (expiry && Date.now() > parseInt(expiry, 10)) {
      clearSession();
      return false;
    }
    return true;
  }

  function parseJwt(token) {
    try {
      const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      return JSON.parse(atob(base64));
    } catch {
      return null;
    }
  }

  // ── Guard ────────────────────────────────────────────────
  function requireAuth() {
    if (!isAuthenticated()) {
      window.location.href = 'login.html';
      return;
    }
  }

  function redirectIfAuthenticated() {
    if (isAuthenticated()) {
      window.location.href = 'dashboard.html';
    }
  }

  // ── Login flow ───────────────────────────────────────────
  function showAlert(id, message, type = 'error') {
    const el = document.getElementById(id);
    if (!el) return;
    el.className = `alert alert-${type} show`;
    el.textContent = message;
  }

  function hideAlert(id) {
    const el = document.getElementById(id);
    if (el) el.className = 'alert';
  }

  function setLoading(btnId, loading) {
    const btn = document.getElementById(btnId);
    if (!btn) return;
    if (loading) {
      btn.disabled = true;
      btn.innerHTML = '<span class="spinner"></span> Verifying…';
    } else {
      btn.disabled = false;
      btn.innerHTML = 'Sign In';
    }
  }

  // ── Init login page ──────────────────────────────────────
  async function initLoginPage() {
    redirectIfAuthenticated();

    const form         = document.getElementById('loginForm');
    const togglePwd    = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('password');

    if (!form) return;

    togglePwd?.addEventListener('click', () => {
      const isPassword = passwordInput.type === 'password';
      passwordInput.type = isPassword ? 'text' : 'password';
    });

    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      hideAlert('loginAlert');

      const email          = document.getElementById('email').value.trim();
      const password       = document.getElementById('password').value;
      const rememberDevice = document.getElementById('rememberDevice').checked;

      if (!email || !password) {
        showAlert('loginAlert', 'Please enter your email and password.');
        return;
      }

      setLoading('loginBtn', true);

      try {
        const deviceProfile = await DeviceFingerprint.getFullProfile();

        const response = await API.post('/auth/login', {
          email,
          password,
          deviceFingerprint: deviceProfile.fingerprint,
          deviceLabel:       deviceProfile.label,
          deviceOs:          deviceProfile.os,
          deviceBrowser:     deviceProfile.browser,
        }, false);

        if (response.mfaRequired) {
          sessionStorage.setItem('pending_email', email);
          sessionStorage.setItem('remember_device', rememberDevice);
          window.location.href = 'mfa-verify.html';
          return;
        }

        saveSession(response.token, response.user, rememberDevice);
        const role = response.user && response.user.role;
        window.location.href = (role === 'ROLE_ADMIN') ? 'dashboard.html' : 'notes.html';

      } catch (err) {
        const msg = err.message || '';
        if (msg === 'ACCOUNT_LOCKED' || msg.toLowerCase().includes('locked')) {
          showAlert('loginAlert', 'Your account has been locked after too many failed attempts. Please contact your administrator to unlock it.');
        } else {
          showAlert('loginAlert', msg || 'Login failed. Please try again.');
        }
      } finally {
        setLoading('loginBtn', false);
      }
    });
  }

  // ── Logout ───────────────────────────────────────────────
  async function logout() {
    try {
      await API.post('/auth/logout', {});
    } catch {
      // proceed with local cleanup regardless
    } finally {
      clearSession();
      window.location.href = 'login.html';
    }
  }

  return {
    initLoginPage,
    requireAuth,
    redirectIfAuthenticated,
    isAuthenticated,
    getUser,
    saveSession,
    clearSession,
    parseJwt,
    logout,
  };
})();

// Auto-init based on current page
document.addEventListener('DOMContentLoaded', () => {
  const page = window.location.pathname;
  if (page.includes('login.html'))    Auth.initLoginPage();
  if (page.includes('dashboard.html')) Auth.requireAuth();

  document.getElementById('logoutBtn')?.addEventListener('click', Auth.logout);
});
