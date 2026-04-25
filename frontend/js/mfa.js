const MFA = (() => {

  const OTP_LENGTH    = 6;
  const RESEND_DELAY  = 60;
  let   countdownTimer = null;
  let   currentMethod  = 'email';

  // ── OTP input wiring ─────────────────────────────────────
  function initOtpInputs() {
    const inputs = Array.from({ length: OTP_LENGTH }, (_, i) => document.getElementById(`otp${i}`));

    inputs.forEach((input, idx) => {
      input.addEventListener('keydown', (e) => {
        if (e.key === 'Backspace' && !input.value && idx > 0) {
          inputs[idx - 1].focus();
        }
      });

      input.addEventListener('input', (e) => {
        const val = e.target.value.replace(/\D/g, '');
        input.value = val.slice(-1);
        if (val && idx < OTP_LENGTH - 1) {
          inputs[idx + 1].focus();
        }
        if (getOtpValue().length === OTP_LENGTH) {
          document.getElementById('verifyBtn').focus();
        }
      });

      input.addEventListener('paste', (e) => {
        e.preventDefault();
        const pasted = (e.clipboardData || window.clipboardData).getData('text').replace(/\D/g, '');
        pasted.split('').slice(0, OTP_LENGTH).forEach((char, i) => {
          if (inputs[i]) inputs[i].value = char;
        });
        const next = Math.min(pasted.length, OTP_LENGTH - 1);
        inputs[next].focus();
      });
    });

    inputs[0]?.focus();
  }

  function getOtpValue() {
    return Array.from({ length: OTP_LENGTH }, (_, i) => {
      const el = document.getElementById(`otp${i}`);
      return el ? el.value : '';
    }).join('');
  }

  function clearOtpInputs() {
    for (let i = 0; i < OTP_LENGTH; i++) {
      const el = document.getElementById(`otp${i}`);
      if (el) el.value = '';
    }
    document.getElementById('otp0')?.focus();
  }

  // ── Countdown timer ──────────────────────────────────────
  function startCountdown() {
    const countdownEl = document.getElementById('countdown');
    const timerTextEl = document.getElementById('timerText');
    const resendBtn   = document.getElementById('resendBtn');
    let   seconds     = RESEND_DELAY;

    resendBtn.disabled = true;
    clearInterval(countdownTimer);

    countdownTimer = setInterval(() => {
      seconds--;
      if (countdownEl) countdownEl.textContent = seconds;
      if (seconds <= 0) {
        clearInterval(countdownTimer);
        if (timerTextEl) timerTextEl.textContent = 'Didn\'t receive it?';
        if (resendBtn)   resendBtn.disabled = false;
      }
    }, 1000);
  }

  // ── Method tabs ──────────────────────────────────────────
  function initMethodTabs() {
    document.querySelectorAll('.method-tab').forEach(tab => {
      tab.addEventListener('click', () => {
        document.querySelectorAll('.method-tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        currentMethod = tab.dataset.method;

        const subtitle = document.getElementById('mfaSubtitle');
        if (currentMethod === 'totp') {
          subtitle.textContent = 'Enter the 6-digit code from your authenticator app.';
        } else {
          subtitle.textContent = 'A 6-digit code has been sent to your registered email address.';
        }

        clearOtpInputs();
        hideAlert();
      });
    });
  }

  // ── Alert helpers ────────────────────────────────────────
  function showAlert(message, type = 'error') {
    const el = document.getElementById('mfaAlert');
    if (!el) return;
    el.className = `alert alert-${type} show`;
    el.textContent = message;
  }

  function hideAlert() {
    const el = document.getElementById('mfaAlert');
    if (el) el.className = 'alert';
  }

  function setVerifyLoading(loading) {
    const btn = document.getElementById('verifyBtn');
    if (!btn) return;
    btn.disabled = loading;
    btn.innerHTML = loading
      ? '<span class="spinner"></span> Verifying…'
      : 'Verify &amp; Continue';
  }

  // ── Verify OTP ───────────────────────────────────────────
  async function verifyOtp(otp) {
    const email         = sessionStorage.getItem('pending_email');
    const rememberDevice = sessionStorage.getItem('remember_device') === 'true';
    const deviceProfile = await DeviceFingerprint.getFullProfile();

    const response = await API.post('/auth/mfa/verify', {
      email,
      otp,
      method:            currentMethod,
      deviceFingerprint: deviceProfile.fingerprint,
    }, false);

    Auth.saveSession(response.token, response.user, rememberDevice);
    sessionStorage.removeItem('pending_email');
    sessionStorage.removeItem('remember_device');

    const role = response.user && response.user.role;
    window.location.href = (role === 'ROLE_ADMIN') ? 'dashboard.html' : 'notes.html';
  }

  // ── Resend OTP ───────────────────────────────────────────
  async function resendOtp() {
    const email = sessionStorage.getItem('pending_email');
    if (!email) {
      window.location.href = 'login.html';
      return;
    }

    try {
      await API.post('/auth/mfa/resend', { email, method: currentMethod }, false);
      showAlert('A new code has been sent.', 'success');
      clearOtpInputs();
      startCountdown();
    } catch (err) {
      showAlert(err.message || 'Failed to resend code.');
    }
  }

  // ── Init ─────────────────────────────────────────────────
  function init() {
    if (!sessionStorage.getItem('pending_email')) {
      window.location.href = 'login.html';
      return;
    }

    initOtpInputs();
    initMethodTabs();
    startCountdown();

    document.getElementById('mfaForm')?.addEventListener('submit', async (e) => {
      e.preventDefault();
      hideAlert();

      const otp = getOtpValue();
      if (otp.length < OTP_LENGTH) {
        showAlert('Please enter the complete 6-digit code.');
        return;
      }

      setVerifyLoading(true);
      try {
        await verifyOtp(otp);
      } catch (err) {
        showAlert(err.message || 'Verification failed. Please try again.');
        clearOtpInputs();
      } finally {
        setVerifyLoading(false);
      }
    });

    document.getElementById('resendBtn')?.addEventListener('click', resendOtp);
  }

  return { init };
})();

document.addEventListener('DOMContentLoaded', MFA.init);
