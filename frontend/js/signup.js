(() => {

  // ── Password strength ────────────────────────────────────
  function measureStrength(pwd) {
    let score = 0;
    if (pwd.length >= 8)  score++;
    if (pwd.length >= 12) score++;
    if (/[A-Z]/.test(pwd)) score++;
    if (/[0-9]/.test(pwd)) score++;
    if (/[^A-Za-z0-9]/.test(pwd)) score++;
    return score; // 0–5
  }

  function updateStrengthBar(pwd) {
    const fill = document.getElementById('strengthFill');
    const text = document.getElementById('strengthText');
    if (!fill || !text) return;

    const score = measureStrength(pwd);
    const pct   = (score / 5) * 100;
    const levels = [
      { label: '',         color: 'transparent' },
      { label: 'Very weak',  color: '#f85149' },
      { label: 'Weak',       color: '#f0883e' },
      { label: 'Fair',       color: '#d29922' },
      { label: 'Good',       color: '#3fb950' },
      { label: 'Strong',     color: '#238636' },
    ];
    const level = levels[score] || levels[0];
    fill.style.width      = `${pct}%`;
    fill.style.background = level.color;
    text.textContent      = pwd ? level.label : '';
    text.style.color      = level.color;
  }

  // ── Alert helpers ────────────────────────────────────────
  function showAlert(msg, type = 'error') {
    const el = document.getElementById('signupAlert');
    if (!el) return;
    el.className = `alert alert-${type} show`;
    el.textContent = msg;
  }

  function hideAlert() {
    const el = document.getElementById('signupAlert');
    if (el) el.className = 'alert';
  }

  function setLoading(loading) {
    const btn = document.getElementById('signupBtn');
    if (!btn) return;
    btn.disabled = loading;
    btn.innerHTML = loading ? '<span class="spinner"></span> Creating account…' : 'Create Account';
  }

  // ── Form submit ──────────────────────────────────────────
  async function handleSubmit(e) {
    e.preventDefault();
    hideAlert();

    const name            = document.getElementById('fullName').value.trim();
    const email           = document.getElementById('email').value.trim();
    const password        = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    // Validation
    if (!name)  { showAlert('Full name is required.'); return; }
    if (!email) { showAlert('Email address is required.'); return; }
    if (password.length < 8) { showAlert('Password must be at least 8 characters.'); return; }
    if (password !== confirmPassword) { showAlert('Passwords do not match.'); return; }

    setLoading(true);

    try {
      await API.post('/auth/register', { name, email, password }, false);
      showAlert('Account created! Redirecting to login…', 'success');
      setTimeout(() => { window.location.href = 'login.html'; }, 1800);
    } catch (err) {
      showAlert(err.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  }

  // ── Init ────────────────────────────────────────────────
  document.addEventListener('DOMContentLoaded', () => {
    // Password strength meter
    document.getElementById('password')?.addEventListener('input', e => {
      updateStrengthBar(e.target.value);
    });

    // Show/hide password toggle
    document.getElementById('togglePassword')?.addEventListener('click', () => {
      const input = document.getElementById('password');
      input.type = input.type === 'password' ? 'text' : 'password';
    });

    document.getElementById('signupForm')?.addEventListener('submit', handleSubmit);
  });
})();
