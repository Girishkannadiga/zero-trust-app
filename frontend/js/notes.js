const Notes = (() => {

  let editingId = null;

  // ── Clock ────────────────────────────────────────────────
  function startClock() {
    const el = document.getElementById('liveClock');
    if (!el) return;
    const tick = () => { el.textContent = new Date().toLocaleTimeString('en-US', { hour12: false }); };
    tick();
    setInterval(tick, 1000);
  }

  // ── Populate user info ────────────────────────────────────
  function populateUser() {
    const user = Auth.getUser();
    if (!user) return;
    const initials = (user.name || user.email || 'U')
      .split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);
    const avatar = document.getElementById('userAvatar');
    const name   = document.getElementById('userName');
    if (avatar) avatar.textContent = initials;
    if (name)   name.textContent   = user.name || user.email;
  }

  // ── Security banner (device / IP check) ──────────────────
  async function checkSecurity() {
    try {
      const fp   = await DeviceFingerprint.getFullProfile();
      const data = await API.get(`/dashboard/security-status?fingerprint=${encodeURIComponent(fp.fingerprint)}`);
      const banner = document.getElementById('securityBanner');
      if (!banner) return;

      const warnings = [];
      if (!data.mfaEnabled)    warnings.push('MFA is disabled on your account');
      if (!data.deviceTrusted) warnings.push('This device is not trusted');
      if (data.ipSuspicious)   warnings.push('Current IP is flagged as suspicious');

      if (warnings.length > 0) {
        banner.className = 'security-banner warn';
        banner.innerHTML = `&#9888; Security notice: ${warnings.join(' · ')}`;
        banner.style.display = 'flex';
      } else {
        banner.className = 'security-banner secure';
        banner.innerHTML = `&#10003; Secure session &mdash; trusted device &amp; IP detected`;
        banner.style.display = 'flex';
      }
    } catch { /* ignore */ }
  }

  // ── Load notes ────────────────────────────────────────────
  async function loadNotes() {
    const grid  = document.getElementById('notesGrid');
    const count = document.getElementById('notesCount');
    if (!grid) return;

    grid.innerHTML = `<div class="notes-empty"><div class="notes-empty-icon">&#8987;</div><div class="notes-empty-text">Loading…</div></div>`;

    try {
      const notes = await API.get('/notes');

      if (count) count.textContent = notes.length
        ? `${notes.length} note${notes.length !== 1 ? 's' : ''}`
        : 'No notes yet';

      if (!notes || notes.length === 0) {
        grid.innerHTML = `
          <div class="notes-empty">
            <div class="notes-empty-icon">&#128196;</div>
            <div class="notes-empty-text">No notes yet</div>
            <div class="notes-empty-sub">Click "Add Note" to create your first confidential note.</div>
          </div>`;
        return;
      }

      grid.innerHTML = notes.map(n => `
        <div class="note-card" data-id="${n.id}">
          <div class="note-card-title">${esc(n.title)}</div>
          <div class="note-card-content">${esc(n.content || '')}</div>
          <div class="note-card-footer">
            <span class="note-date">&#128336; ${formatDate(n.updatedAt)}</span>
            <div class="note-actions">
              <button class="note-btn" onclick="Notes.openEdit(${n.id})">&#9998; Edit</button>
              <button class="note-btn delete" onclick="Notes.deleteNote(${n.id})">&#128465; Delete</button>
            </div>
          </div>
        </div>`).join('');

    } catch {
      grid.innerHTML = `<div class="notes-empty"><div class="notes-empty-text">Unable to load notes.</div></div>`;
    }
  }

  // ── Open modal (add) ──────────────────────────────────────
  function openAdd() {
    editingId = null;
    document.getElementById('modalTitle').textContent  = '&#43; New Note';
    document.getElementById('noteTitle').value         = '';
    document.getElementById('noteContent').value       = '';
    document.getElementById('modalSave').textContent   = 'Save Note';
    hideModalAlert();
    openModal();
    document.getElementById('noteTitle').focus();
  }

  // ── Open modal (edit) ─────────────────────────────────────
  async function openEdit(id) {
    editingId = id;
    const card  = document.querySelector(`.note-card[data-id="${id}"]`);
    const title = card?.querySelector('.note-card-title')?.textContent || '';
    const content = card?.querySelector('.note-card-content')?.textContent || '';

    document.getElementById('modalTitle').innerHTML   = '&#9998; Edit Note';
    document.getElementById('noteTitle').value        = title;
    document.getElementById('noteContent').value      = content;
    document.getElementById('modalSave').textContent  = 'Update Note';
    hideModalAlert();
    openModal();
    document.getElementById('noteTitle').focus();
  }

  // ── Save (create or update) ───────────────────────────────
  async function saveNote() {
    const title   = document.getElementById('noteTitle').value.trim();
    const content = document.getElementById('noteContent').value.trim();

    if (!title) {
      showModalAlert('Title is required.');
      return;
    }

    const btn = document.getElementById('modalSave');
    btn.disabled = true;
    btn.textContent = 'Saving…';

    try {
      if (editingId) {
        await API.put(`/notes/${editingId}`, { title, content });
      } else {
        await API.post('/notes', { title, content });
      }
      closeModal();
      await loadNotes();
    } catch (err) {
      showModalAlert(err.message || 'Failed to save note.');
    } finally {
      btn.disabled = false;
      btn.textContent = editingId ? 'Update Note' : 'Save Note';
    }
  }

  // ── Delete ────────────────────────────────────────────────
  async function deleteNote(id) {
    if (!confirm('Permanently delete this note?')) return;
    try {
      await API.delete(`/notes/${id}`);
      await loadNotes();
    } catch (err) {
      alert(err.message || 'Failed to delete note.');
    }
  }

  // ── Modal helpers ─────────────────────────────────────────
  function openModal()  { document.getElementById('noteModal').classList.add('open'); }
  function closeModal() { document.getElementById('noteModal').classList.remove('open'); }

  function showModalAlert(msg) {
    const el = document.getElementById('modalAlert');
    if (el) { el.className = 'alert alert-error show'; el.textContent = msg; }
  }

  function hideModalAlert() {
    const el = document.getElementById('modalAlert');
    if (el) el.className = 'alert';
  }

  // ── Helpers ───────────────────────────────────────────────
  function formatDate(raw) {
    if (!raw) return '—';
    return new Date(raw).toLocaleString('en-US', {
      month: 'short', day: 'numeric', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  }

  function esc(str) {
    return String(str || '')
      .replace(/&/g, '&amp;').replace(/</g, '&lt;')
      .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
  }

  // ── Init ─────────────────────────────────────────────────
  async function init() {
    Auth.requireAuth();

    // Guard: only ROLE_USER on this page (admins go to dashboard)
    const user = Auth.getUser();
    if (user && user.role === 'ROLE_ADMIN') {
      window.location.href = 'dashboard.html';
      return;
    }

    startClock();
    populateUser();

    // Wire up modal
    document.getElementById('addNoteBtn')?.addEventListener('click', openAdd);
    document.getElementById('modalClose')?.addEventListener('click', closeModal);
    document.getElementById('modalCancel')?.addEventListener('click', closeModal);
    document.getElementById('modalSave')?.addEventListener('click', saveNote);
    document.getElementById('logoutBtn')?.addEventListener('click', Auth.logout);

    // Close modal on overlay click
    document.getElementById('noteModal')?.addEventListener('click', e => {
      if (e.target === e.currentTarget) closeModal();
    });

    // Enter to save (Ctrl+Enter in textarea)
    document.getElementById('noteContent')?.addEventListener('keydown', e => {
      if (e.ctrlKey && e.key === 'Enter') saveNote();
    });

    // Load in parallel
    await Promise.allSettled([loadNotes(), checkSecurity()]);
  }

  return { init, openEdit, deleteNote };
})();

document.addEventListener('DOMContentLoaded', Notes.init);
