const dashboard = (() => {

  let currentSection = 'overview';

  // ── Clock ─────────────────────────────────────────────────
  function startClock() {
    const el = document.getElementById('liveClock');
    if (!el) return;
    const tick = () => { el.textContent = new Date().toLocaleTimeString('en-US', { hour12: false }); };
    tick();
    setInterval(tick, 1000);
  }

  // ── Section switching ─────────────────────────────────────
  const titleMap = {
    overview:  ['Dashboard',       'Security overview'],
    devices:   ['Devices',         'Trusted device management'],
    ipLogs:    ['IP Tracking',     'Access location history'],
    loginLogs: ['Login Logs',      'Recent authentication events'],
    mfa:       ['MFA Settings',    'Multi-factor authentication'],
    profile:   ['Profile',         'Your account details'],
    sessions:  ['Active Sessions', 'Current active sessions'],
    admin:     ['User Management', 'Admin — all registered users'],
  };

  const sectionLoaders = {
    overview:  () => Promise.allSettled([loadSecurityStatus(), loadStats(), loadRiskScore()]),
    devices:   loadDevices,
    ipLogs:    loadIpLogs,
    loginLogs: loadLoginLogs,
    mfa:       loadMfaSection,
    profile:   loadProfile,
    sessions:  loadActiveSessions,
    admin:     loadAdminUsers,
  };

  function switchSection(name) {
    if (currentSection === name) return;
    currentSection = name;
    document.querySelectorAll('.page-body section[data-section]').forEach(s => {
      s.style.display = s.dataset.section === name ? '' : 'none';
    });
    const [title, subtitle] = titleMap[name] || ['Dashboard', ''];
    setText('pageTitle', title);
    setText('pageSubtitle', subtitle);
    document.querySelectorAll('.nav-item[data-section]').forEach(item => {
      item.classList.toggle('active', item.dataset.section === name);
    });
    const loader = sectionLoaders[name];
    if (loader) loader();
  }

  function initSidebarNav() {
    document.querySelectorAll('.nav-item[data-section]').forEach(item => {
      item.addEventListener('click', e => {
        e.preventDefault();
        switchSection(item.dataset.section);
      });
    });
  }

  // ── Populate user info ────────────────────────────────────
  function populateUserInfo() {
    const user = Auth.getUser();
    if (!user) return;
    const initials = (user.name || user.email || 'U')
      .split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);
    setText('sidebarAvatar',   initials);
    setText('sidebarUserName', user.name || user.email);
    setText('sidebarUserRole', user.role || 'User');
  }

  // ── Overview: security status ──────────────────────────────
  async function loadSecurityStatus() {
    try {
      const fp = await DeviceFingerprint.getFullProfile();
      const data = await API.get(`/dashboard/security-status?fingerprint=${encodeURIComponent(fp.fingerprint)}`);
      setText('mfaStatus',    data.mfaEnabled  ? 'Enabled' : 'Disabled');
      setText('deviceStatus', data.deviceLabel || fp.label || '—');
      setText('ipStatus',     normalizeIp(data.currentIp));
      setText('lastLogin',    data.lastLogin    ? formatDate(data.lastLogin) : '—');

      const token = API.getToken();
      if (token) {
        setText('tokenPreview', `${token.slice(0, 12)}…`);
        const payload = Auth.parseJwt(token);
        if (payload?.exp) setText('sessionExpiry', formatDate(new Date(payload.exp * 1000)));
      }

      setBadge('mfaBadge',          data.mfaEnabled,    'ON',        'OFF',         'success', 'danger');
      setBadge('deviceBadgeStatus', data.deviceTrusted, 'Trusted',  'Untrusted',   'success', 'warn');
      setBadge('ipBadge',           !data.ipSuspicious, 'Known',    'Suspicious',  'info',    'warn');
      setOverallStatus(data);
    } catch { /* keep dashes */ }
  }

  // ── Overview: stats ────────────────────────────────────────
  async function loadStats() {
    try {
      const stats = await API.get('/dashboard/stats');
      setText('statSessions', stats.activeSessions  ?? '—');
      setText('statDevices',  stats.trustedDevices  ?? '—');
      setText('statIPs',      stats.uniqueIps        ?? '—');
      setText('statThreats',  stats.blockedAttempts  ?? '—');
      setText('deviceBadge',  String(stats.untrustedDevices ?? 0));
    } catch { /* keep dashes */ }
  }

  // ── Overview: risk score ───────────────────────────────────
  async function loadRiskScore() {
    try {
      const fp = await DeviceFingerprint.getFullProfile();
      const data = await API.get(`/dashboard/risk-score?fingerprint=${encodeURIComponent(fp.fingerprint)}`);
      const score = data.score ?? 0;
      const level = data.level || 'LOW';
      setText('riskScoreValue', score);
      const colors  = { LOW: '#238636', MEDIUM: '#d29922', HIGH: '#f85149' };
      const classes = { LOW: 'badge-success', MEDIUM: 'badge-warn', HIGH: 'badge-danger' };
      const circle = document.getElementById('riskScoreCircle');
      if (circle) circle.style.borderColor = colors[level] || '#2f81f7';
      const scoreVal = document.getElementById('riskScoreValue');
      if (scoreVal) scoreVal.style.color = colors[level] || 'var(--text-primary)';
      const badge = document.getElementById('riskLevelBadge');
      if (badge) { badge.textContent = level; badge.className = `badge ${classes[level] || 'badge-info'}`; }
      const factorsEl = document.getElementById('riskFactors');
      if (factorsEl && data.factors) {
        factorsEl.innerHTML = data.factors.map(f =>
          `<div style="font-size:0.82rem;color:var(--text-secondary);margin-bottom:5px;">
             <span style="color:${colors[level] || '#8b949e'}">&#9888;</span> ${esc(f)}
           </div>`).join('');
      }
      const recsEl = document.getElementById('riskRecommendations');
      if (recsEl && data.recommendations && data.recommendations.length > 0) {
        recsEl.innerHTML =
          `<div style="font-size:0.78rem;font-weight:600;color:var(--text-muted);margin-bottom:6px;text-transform:uppercase;letter-spacing:.05em;">Recommendations</div>` +
          data.recommendations.map(r =>
            `<div style="font-size:0.82rem;color:var(--text-muted);margin-bottom:4px;">&#8594; ${esc(r)}</div>`).join('');
      }
    } catch { /* keep defaults */ }
  }

  // ── Login logs ─────────────────────────────────────────────
  async function loadLoginLogs() {
    const tbody = document.getElementById('loginLogsBody');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="6" class="text-muted text-center" style="padding:24px;">Loading…</td></tr>';
    try {
      const logs = await API.get('/dashboard/login-logs?limit=50');
      if (!logs || !logs.length) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-muted text-center" style="padding:24px;">No login records found.</td></tr>';
        return;
      }
      tbody.innerHTML = logs.map(log => `
        <tr>
          <td class="font-mono">${formatDate(log.timestamp)}</td>
          <td class="font-mono">${esc(normalizeIp(log.ipAddress))}</td>
          <td>${esc(log.deviceLabel || '—')}</td>
          <td>${esc(log.location   || '—')}</td>
          <td><span class="badge ${log.success ? 'badge-success' : 'badge-danger'}">${log.success ? 'Success' : 'Failed'}</span></td>
          <td><span class="badge ${log.mfaUsed ? 'badge-success' : 'badge-warn'}">${log.mfaUsed ? 'Yes' : 'No'}</span></td>
        </tr>`).join('');
    } catch {
      tbody.innerHTML = '<tr><td colspan="6" class="text-muted text-center" style="padding:24px;">Unable to load logs.</td></tr>';
    }
  }

  async function exportLoginLogs() {
    try {
      const logs = await API.get('/dashboard/login-logs?limit=1000');
      if (!logs || !logs.length) { alert('No login logs to export.'); return; }

      const rows = [['Timestamp', 'IP Address', 'Device', 'Location', 'Status', 'MFA Used']];
      logs.forEach(log => {
        rows.push([
          csvEsc(formatDate(log.timestamp)),
          csvEsc(normalizeIp(log.ipAddress)),
          csvEsc(log.deviceLabel || ''),
          csvEsc(log.location   || ''),
          log.success ? 'Success' : 'Failed',
          log.mfaUsed ? 'Yes' : 'No',
        ]);
      });

      const csv = rows.map(r => r.join(',')).join('\n');
      const blob = new Blob([csv], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = 'login-logs.csv'; a.click();
      URL.revokeObjectURL(url);
    } catch (err) { alert(err.message || 'Failed to export logs.'); }
  }

  function csvEsc(val) {
    const s = String(val || '');
    return (s.includes(',') || s.includes('"') || s.includes('\n'))
      ? `"${s.replace(/"/g, '""')}"` : s;
  }

  // ── Devices ────────────────────────────────────────────────
  async function loadDevices() {
    const tbody = document.getElementById('devicesBody');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="8" class="text-muted text-center" style="padding:24px;">Loading…</td></tr>';
    try {
      const devices = await API.get('/devices');
      if (!devices || !devices.length) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-muted text-center" style="padding:24px;">No devices found.</td></tr>';
        return;
      }
      tbody.innerHTML = devices.map(d => `
        <tr>
          <td>${esc(d.label || '—')}</td>
          <td>${esc(d.os || '—')}</td>
          <td>${esc(d.browser || '—')}</td>
          <td class="font-mono" style="font-size:0.78rem;">${esc((d.fingerprint || '').slice(0,12))}…</td>
          <td>${formatDate(d.firstSeen)}</td>
          <td>${formatDate(d.lastSeen)}</td>
          <td><span class="badge ${d.trusted ? 'badge-success' : 'badge-warn'}">${d.trusted ? 'Trusted' : 'Untrusted'}</span></td>
          <td>
            ${d.trusted
              ? `<button class="btn btn-ghost" style="padding:4px 10px;font-size:0.78rem;" onclick="dashboard.revokeDeviceTrust(${d.id})">Revoke</button>`
              : `<button class="btn btn-ghost" style="padding:4px 10px;font-size:0.78rem;" onclick="dashboard.trustDevice(${d.id})">Trust</button>`
            }
          </td>
        </tr>`).join('');
    } catch {
      tbody.innerHTML = '<tr><td colspan="8" class="text-muted text-center" style="padding:24px;">Unable to load devices.</td></tr>';
    }
  }

  async function trustDevice(id) {
    try { await API.put(`/devices/${id}/trust`, {}); loadDevices(); }
    catch (err) { alert(err.message || 'Failed to trust device.'); }
  }

  async function revokeDeviceTrust(id) {
    try { await API.put(`/devices/${id}/revoke`, {}); loadDevices(); }
    catch (err) { alert(err.message || 'Failed to revoke device trust.'); }
  }

  // ── IP Tracking ───────────────────────────────────────────
  async function loadIpLogs() {
    const tbody = document.getElementById('ipLogsBody');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="5" class="text-muted text-center" style="padding:24px;">Loading…</td></tr>';
    try {
      const logs = await API.get('/dashboard/login-logs?limit=100');
      if (!logs || !logs.length) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-muted text-center" style="padding:24px;">No IP records found.</td></tr>';
        return;
      }
      tbody.innerHTML = logs.map(log => `
        <tr>
          <td class="font-mono">${esc(normalizeIp(log.ipAddress))}</td>
          <td class="font-mono">${formatDate(log.timestamp)}</td>
          <td>${esc(log.deviceLabel || '—')}</td>
          <td>${esc(log.location   || '—')}</td>
          <td><span class="badge ${log.success ? 'badge-success' : 'badge-danger'}">${log.success ? 'Allowed' : 'Blocked'}</span></td>
        </tr>`).join('');
    } catch {
      tbody.innerHTML = '<tr><td colspan="5" class="text-muted text-center" style="padding:24px;">Unable to load IP records.</td></tr>';
    }
  }

  // ── MFA section ────────────────────────────────────────────
  async function loadMfaSection() {
    const user = Auth.getUser();
    if (!user) return;
    const enabled = user.mfaEnabled;
    const badge = document.getElementById('mfaStatusBadge');
    if (badge) { badge.textContent = enabled ? 'Enabled' : 'Disabled'; badge.className = `badge ${enabled ? 'badge-success' : 'badge-danger'}`; }
    setText('mfaCurrentStatus', enabled ? '✓ MFA is currently enabled on your account.' : '✗ MFA is currently disabled on your account.');
  }

  // ── Profile section ────────────────────────────────────────
  function loadProfile() {
    const user = Auth.getUser();
    if (!user) return;
    setText('profileName',  user.name  || '—');
    setText('profileEmail', user.email || '—');
    setText('profileRole',  user.role  || '—');
    setText('profileMfa',   user.mfaEnabled ? 'Enabled' : 'Disabled');
    const nameInput = document.getElementById('editNameInput');
    if (nameInput) nameInput.value = user.name || '';
  }

  async function saveProfileName() {
    const nameInput = document.getElementById('editNameInput');
    const name = nameInput?.value.trim();
    if (!name) { alert('Name cannot be empty.'); return; }
    const btn = document.getElementById('saveNameBtn');
    if (btn) btn.disabled = true;
    try {
      const updated = await API.put('/dashboard/profile', { name });
      const user = Auth.getUser();
      if (user) {
        user.name = updated.name;
        const storage = localStorage.getItem('zt_user') ? localStorage : sessionStorage;
        storage.setItem('zt_user', JSON.stringify(user));
      }
      populateUserInfo();
      loadProfile();
      showProfileMsg('profileMsg', 'Name updated successfully!', 'success');
    } catch (err) {
      showProfileMsg('profileMsg', err.message || 'Failed to update name.', 'error');
    } finally {
      if (btn) btn.disabled = false;
    }
  }

  function showProfileMsg(id, msg, type) {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = msg;
    el.style.color = type === 'success' ? '#238636' : '#f85149';
    el.style.display = '';
    setTimeout(() => { el.style.display = 'none'; }, 4000);
  }

  // ── Sessions ───────────────────────────────────────────────
  async function loadActiveSessions() {
    const tbody = document.getElementById('sessionsBody');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="5" class="text-muted text-center" style="padding:24px;">Loading…</td></tr>';
    try {
      const sessions = await API.get('/dashboard/sessions');
      if (!sessions || !sessions.length) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-muted text-center" style="padding:24px;">No active sessions.</td></tr>';
        return;
      }
      tbody.innerHTML = sessions.map(s => `
        <tr>
          <td class="font-mono">${esc(s.sessionId.slice(0, 12))}…</td>
          <td>${esc(s.deviceLabel || '—')}</td>
          <td class="font-mono">${esc(normalizeIp(s.ipAddress))}</td>
          <td>${formatDate(s.startedAt)}</td>
          <td>
            ${s.current
              ? '<span class="badge badge-success">Current</span>'
              : `<button class="btn btn-ghost" style="padding:4px 10px;font-size:0.78rem;" onclick="dashboard.revokeSession('${s.sessionId}')">Revoke</button>`
            }
          </td>
        </tr>`).join('');
    } catch {
      tbody.innerHTML = '<tr><td colspan="5" class="text-muted text-center" style="padding:24px;">Unable to load sessions.</td></tr>';
    }
  }

  async function revokeSession(sessionId) {
    if (!confirm('Revoke this session?')) return;
    try { await API.delete(`/dashboard/sessions/${sessionId}`); loadActiveSessions(); }
    catch (err) { alert(err.message || 'Failed to revoke session.'); }
  }

  async function revokeAllSessions() {
    if (!confirm('Revoke all other active sessions?')) return;
    try { await API.post('/dashboard/sessions/revoke-all', {}); loadActiveSessions(); }
    catch (err) { alert(err.message || 'Failed to revoke sessions.'); }
  }

  // ── Helpers ───────────────────────────────────────────────
  function setText(id, value) { const el = document.getElementById(id); if (el) el.textContent = value; }

  function setBadge(id, condition, trueText, falseText, trueClass = 'success', falseClass = 'danger') {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = condition ? trueText : falseText;
    el.className   = `badge badge-${condition ? trueClass : falseClass}`;
  }

  function setOverallStatus(data) {
    const el = document.getElementById('overallStatus');
    if (!el) return;
    const secure = data.mfaEnabled && data.deviceTrusted && !data.ipSuspicious;
    el.textContent = secure ? 'SECURE' : 'AT RISK';
    el.className   = `badge ${secure ? 'badge-success' : 'badge-danger'}`;
  }

  function formatDate(raw) {
    if (!raw) return '—';
    return new Date(raw).toLocaleString('en-US', { year:'numeric', month:'short', day:'numeric', hour:'2-digit', minute:'2-digit' });
  }

  function esc(str) {
    if (!str) return '';
    return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
  }

  function normalizeIp(ip) {
    if (!ip) return '—';
    if (ip === '0:0:0:0:0:0:0:1' || ip === '::1') return '127.0.0.1';
    return ip;
  }

  // ── Admin: user management ─────────────────────────────────
  async function loadAdminUsers() {
    const tbody = document.getElementById('adminUsersBody');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="11" class="text-muted text-center" style="padding:24px;">Loading…</td></tr>';
    try {
      const users = await API.get('/admin/users');
      if (!users || !users.length) {
        tbody.innerHTML = '<tr><td colspan="11" class="text-muted text-center" style="padding:24px;">No users found.</td></tr>';
        return;
      }
      tbody.innerHTML = users.map(u => `
        <tr>
          <td><strong>${esc(u.name)}</strong></td>
          <td class="font-mono" style="font-size:0.82rem;">${esc(u.email)}</td>
          <td><span class="badge ${u.role === 'ROLE_ADMIN' ? 'badge-danger' : 'badge-info'}">${u.role === 'ROLE_ADMIN' ? 'Admin' : 'User'}</span></td>
          <td class="font-mono" style="font-size:0.78rem;">${esc(normalizeIp(u.lastLoginIp))}</td>
          <td style="font-size:0.78rem;">${u.lastLoginTime ? formatDate(u.lastLoginTime) : '—'}</td>
          <td style="font-size:0.82rem;">${esc(u.lastDevice || '—')}</td>
          <td><span class="badge ${u.deviceTrusted ? 'badge-success' : 'badge-warn'}">${u.deviceTrusted ? 'Trusted' : 'Untrusted'}</span></td>
          <td><span class="badge ${u.mfaEnabled ? 'badge-success' : 'badge-warn'}">${u.mfaEnabled ? 'On' : 'Off'}</span></td>
          <td><span class="badge ${u.accountLocked ? 'badge-danger' : 'badge-success'}">${u.accountLocked ? 'Locked' : 'Active'}</span></td>
          <td style="text-align:center;">${u.activeSessions}</td>
          <td style="display:flex;gap:4px;flex-wrap:wrap;min-width:180px;">
            ${u.mfaEnabled
              ? `<button class="btn btn-ghost" style="padding:3px 7px;font-size:0.73rem;" onclick="dashboard.adminDisableMfa(${u.id})">Disable MFA</button>`
              : `<button class="btn btn-ghost" style="padding:3px 7px;font-size:0.73rem;" onclick="dashboard.adminEnableMfa(${u.id})">Enable MFA</button>`
            }
            ${u.accountLocked
              ? `<button class="btn btn-ghost" style="padding:3px 7px;font-size:0.73rem;" onclick="dashboard.adminUnlockUser(${u.id})">Unlock</button>`
              : `<button class="btn btn-ghost" style="padding:3px 7px;font-size:0.73rem;" onclick="dashboard.adminLockUser(${u.id})">Lock</button>`
            }
            <button class="btn btn-danger" style="padding:3px 7px;font-size:0.73rem;" onclick="dashboard.adminDeleteUser(${u.id},'${esc(u.email)}')">Delete</button>
          </td>
        </tr>`).join('');
    } catch {
      tbody.innerHTML = '<tr><td colspan="11" class="text-muted text-center" style="padding:24px;">Unable to load users. Admin access required.</td></tr>';
    }
  }

  async function adminEnableMfa(id) {
    if (!confirm('Enable MFA for this user?')) return;
    try { await API.put(`/admin/users/${id}/enable-mfa`, {}); loadAdminUsers(); }
    catch (err) { alert(err.message || 'Failed.'); }
  }

  async function adminDisableMfa(id) {
    if (!confirm('Disable MFA for this user?')) return;
    try { await API.put(`/admin/users/${id}/disable-mfa`, {}); loadAdminUsers(); }
    catch (err) { alert(err.message || 'Failed.'); }
  }

  async function adminLockUser(id) {
    if (!confirm('Lock this user account? They will not be able to log in.')) return;
    try { await API.put(`/admin/users/${id}/lock`, {}); loadAdminUsers(); }
    catch (err) { alert(err.message || 'Failed.'); }
  }

  async function adminUnlockUser(id) {
    if (!confirm('Unlock this user account?')) return;
    try { await API.put(`/admin/users/${id}/unlock`, {}); loadAdminUsers(); }
    catch (err) { alert(err.message || 'Failed.'); }
  }

  async function adminDeleteUser(id, email) {
    if (!confirm(`Permanently delete user "${email}"?`)) return;
    try { await API.delete(`/admin/users/${id}`); loadAdminUsers(); }
    catch (err) { alert(err.message || 'Failed.'); }
  }

  function applyRoleVisibility() {
    const user = Auth.getUser();
    if (user && user.role === 'ROLE_ADMIN') {
      document.querySelectorAll('.admin-only').forEach(el => el.style.display = '');
    }
  }

  function startPolling() {
    setInterval(() => {
      if (currentSection === 'overview') Promise.allSettled([loadStats(), loadRiskScore()]);
      else if (currentSection === 'sessions') loadActiveSessions();
      else if (currentSection === 'loginLogs') loadLoginLogs();
    }, 30000);
  }

  // ── Init ─────────────────────────────────────────────────
  async function init() {
    Auth.requireAuth();
    const user = Auth.getUser();
    if (user && user.role !== 'ROLE_ADMIN') { window.location.href = 'notes.html'; return; }

    startClock();
    populateUserInfo();
    applyRoleVisibility();
    initSidebarNav();

    document.getElementById('revokeAllBtn')?.addEventListener('click', revokeAllSessions);
    document.getElementById('exportLogsBtn')?.addEventListener('click', exportLoginLogs);
    document.getElementById('saveNameBtn')?.addEventListener('click', saveProfileName);

    await Promise.allSettled([loadSecurityStatus(), loadStats(), loadRiskScore()]);
    startPolling();
  }

  return {
    init, loadLoginLogs, loadDevices, loadIpLogs, loadActiveSessions, loadAdminUsers,
    revokeSession, trustDevice, revokeDeviceTrust,
    adminEnableMfa, adminDisableMfa, adminLockUser, adminUnlockUser, adminDeleteUser,
    exportLoginLogs,
  };
})();

document.addEventListener('DOMContentLoaded', dashboard.init);
