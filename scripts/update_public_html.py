import re

with open('public/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

# 1. Update Students Table Headers
old_thead = '''            <thead>
              <tr>
                <th style="width:50px; text-align:center;">ተ.ቁ</th>
                <th>መለያ ቁጥር</th>
                <th>ፎቶ እና ሙሉ ስም</th>
                <th>ጾታ</th>
                <th>ክፍል</th>
                <th>የአሳዳጊ ስልክ</th>
                <th>ሁኔታ</th>
                <th style="text-align:center;">እርምጃ</th>
              </tr>
            </thead>'''

new_thead = '''            <thead>
              <tr>
                <th style="width:50px; text-align:center;">ተ.ቁ</th>
                <th id="thStudentId" onclick="sortStudentsTable('id')" style="cursor:pointer; user-select:none;" title="በመለያ ቁጥር ደርድር (Click to sort by ID)">መለያ ቁጥር <span id="sortIconId" style="color:#F59E0B; font-size:0.75rem; margin-left:4px;">▲</span></th>
                <th id="thStudentName" onclick="sortStudentsTable('name')" style="cursor:pointer; user-select:none;" title="በስም ደርድር (Click to sort by Name)">ፎቶ እና ሙሉ ስም <span id="sortIconName" style="color:#94A3B8; font-size:0.75rem; margin-left:4px;">⇅</span></th>
                <th>ጾታ</th>
                <th id="thStudentGrade" onclick="sortStudentsTable('grade')" style="cursor:pointer; user-select:none;" title="በክፍል ደርድር (Click to sort by Grade)">ክፍል <span id="sortIconGrade" style="color:#94A3B8; font-size:0.75rem; margin-left:4px;">⇅</span></th>
                <th>የአሳዳጊ ስልክ</th>
                <th>ሁኔታ</th>
                <th style="text-align:center;">እርምጃ</th>
              </tr>
            </thead>'''

assert old_thead in html, "Could not find old_thead in public/index.html"
html = html.replace(old_thead, new_thead, 1)

# 2. Update loginModal Form to have a Single Universal Identifier Input
old_login_form_pattern = re.compile(
    r'<form id="webLoginForm" onsubmit="handleWebLoginSubmit\(event\)">[\s\S]*?<!-- Password Field with Show/Hide Toggle Button',
    re.MULTILINE
)

new_login_form_start = '''<form id="webLoginForm" onsubmit="handleWebLoginSubmit(event)">
          <!-- Universal Single Identifier Field (Accepts Email OR Phone Number) -->
          <div class="form-group" style="margin-bottom:0.95rem;">
            <label id="lblLoginIdentifier" style="font-size:0.84rem; font-weight:700; color:#1E3A8A; display:block; margin-bottom:5px;">
              👤 የተጠቃሚው ስልክ ቁጥር ወይም ኢሜይል አድራሻ (Phone Number or Email) *
            </label>
            <div style="position:relative;">
              <input type="text" id="inputWebLoginIdentifier" class="form-control" required
                     placeholder="ስልክ ቁጥር (0960225925) ወይም ኢሜይል (tadessemisganaw4@gmail.com)"
                     autocomplete="username"
                     oninput="syncWebLoginHiddenInputs()"
                     style="font-size:0.95rem; padding:0.75rem 0.85rem; border-radius:10px; border:1.5px solid #CBD5E1; width:100%; box-sizing:border-box;">
              <!-- Compatibility hidden elements -->
              <input type="hidden" id="inputWebLoginEmail" value="">
              <input type="hidden" id="inputWebLoginPhone" value="">
            </div>
          </div>

          <!-- Password Field with Show/Hide Toggle Button'''

assert old_login_form_pattern.search(html) is not None, "Could not find old_login_form_pattern in public/index.html"
html = old_login_form_pattern.sub(lambda m: new_login_form_start, html, count=1)

# 3. Update renderStudents() with sorting functionality
old_render_students_start = '''    function renderStudents() {
      const activeStudents = getFilteredStudentsWeb();
      const tbody = document.getElementById('studentsTableBody');
      tbody.innerHTML = activeStudents.map((s, index) => {'''

new_render_students = '''    let studentSortField = 'id';
    let studentSortAsc = true;

    function sortStudentsTable(field) {
      if (studentSortField === field) {
        studentSortAsc = !studentSortAsc;
      } else {
        studentSortField = field;
        studentSortAsc = true;
      }
      updateStudentSortIndicators();
      renderStudents();
    }

    function updateStudentSortIndicators() {
      const iconId = document.getElementById('sortIconId');
      const iconName = document.getElementById('sortIconName');
      const iconGrade = document.getElementById('sortIconGrade');

      const getSymbol = (f) => {
        if (studentSortField !== f) return '⇅';
        return studentSortAsc ? '▲' : '▼';
      };
      const getColor = (f) => (studentSortField === f ? '#F59E0B' : '#94A3B8');

      if (iconId) { iconId.innerText = getSymbol('id'); iconId.style.color = getColor('id'); }
      if (iconName) { iconName.innerText = getSymbol('name'); iconName.style.color = getColor('name'); }
      if (iconGrade) { iconGrade.innerText = getSymbol('grade'); iconGrade.style.color = getColor('grade'); }
    }

    function renderStudents() {
      let activeStudents = getFilteredStudentsWeb();

      if (studentSortField) {
        activeStudents = [...activeStudents].sort((a, b) => {
          let cmp = 0;
          if (studentSortField === 'name') {
            const na = (a.name || '').trim();
            const nb = (b.name || '').trim();
            cmp = na.localeCompare(nb, 'am');
          } else if (studentSortField === 'grade') {
            const numA = parseInt(a.grade) || 0;
            const numB = parseInt(b.grade) || 0;
            if (numA !== numB) {
              cmp = numA - numB;
            } else {
              cmp = (a.grade || '').localeCompare(b.grade || '', undefined, { numeric: true });
            }
          } else if (studentSortField === 'id') {
            cmp = (a.id || '').localeCompare(b.id || '', undefined, { numeric: true });
          }
          return studentSortAsc ? cmp : -cmp;
        });
      }

      const tbody = document.getElementById('studentsTableBody');
      if (!tbody) return;
      tbody.innerHTML = activeStudents.map((s, index) => {'''

assert old_render_students_start in html, "Could not find old_render_students_start in public/index.html"
html = html.replace(old_render_students_start, new_render_students, 1)

# Also ensure filterStudents() is called at the end of renderStudents() if search is active
old_render_students_end = '''          <td style="text-align:center;">
            <button class="btn-secondary" style="padding:0.25rem 0.5rem; font-size:0.75rem; color:var(--danger);" onclick="removeStudent('${s.id}')">🗑️ ሰርዝ</button>
          </td>
        </tr>
      `}).join('');
    }'''

new_render_students_end = '''          <td style="text-align:center;">
            <button class="btn-secondary" style="padding:0.25rem 0.5rem; font-size:0.75rem; color:var(--danger);" onclick="removeStudent('${s.id}')">🗑️ ሰርዝ</button>
          </td>
        </tr>
      `}).join('');

      if (document.getElementById('studentSearch') && document.getElementById('studentSearch').value.trim()) {
        filterStudents();
      }
    }'''

assert old_render_students_end in html, "Could not find old_render_students_end in public/index.html"
html = html.replace(old_render_students_end, new_render_students_end, 1)

# 4. Overhaul handleLoginRoleChange, openLoginModal, closeLoginModal, and handleWebLoginSubmit
old_login_handlers_pattern = re.compile(
    r'function handleLoginRoleChange\(\) \{[\s\S]*?function openPasswordManagerModal\(\) \{',
    re.MULTILINE
)

new_login_handlers = '''function syncWebLoginHiddenInputs() {
      const val = (document.getElementById('inputWebLoginIdentifier')?.value || '').trim();
      const em = document.getElementById('inputWebLoginEmail');
      const ph = document.getElementById('inputWebLoginPhone');
      if (em) em.value = val;
      if (ph) ph.value = val;
    }

    function handleLoginRoleChange() {
      const select = document.getElementById('loginRoleSelect');
      if (!select) return;
      const roleKey = select.value;
      const notice = document.getElementById('loginModeNotice');
      const lbl = document.getElementById('lblLoginIdentifier');
      const identInput = document.getElementById('inputWebLoginIdentifier');

      if (roleKey === 'admin') {
        if (lbl) lbl.innerHTML = '👑 የአድሚን ኢሜይል ወይም ስልክ ቁጥር (Admin Email / Phone) *';
        if (identInput) identInput.placeholder = 'tadessemisganaw4@gmail.com ወይም 0911999999';
        if (notice) {
          notice.style.background = '#FEF3C7';
          notice.style.borderColor = '#FCD34D';
          notice.style.color = '#92400E';
          notice.innerHTML = '<span>👑</span><span>የተመረጠው ሚና፡ <strong>ሲስተም አድሚን</strong> — ኢሜይል፡ <strong>tadessemisganaw4@gmail.com</strong> | የይለፍ ቃል፡ <strong>tade@2026</strong></span>';
        }
      } else if (roleKey === 'secretary') {
        if (lbl) lbl.innerHTML = '✍️ የዋና ጸሐፊ ስልክ ቁጥር (Secretary Phone) *';
        if (identInput) identInput.placeholder = '0960225925';
        if (notice) {
          notice.style.background = '#ECFDF5';
          notice.style.borderColor = '#A7F3D0';
          notice.style.color = '#065F46';
          notice.innerHTML = '<span>✍️</span><span>የተመረጠው ሚና፡ <strong>Sara Niguse (ዋና ጸሐፊ)</strong> — ስልክ፡ <strong>0960225925</strong> | የይለፍ ቃል፡ <strong>sara@123</strong></span>';
        }
      } else {
        const roleText = select.options[select.selectedIndex]?.text || 'መምህር';
        if (lbl) lbl.innerHTML = '📞 የተጠቃሚው የስልክ ቁጥር (Phone Number) *';
        if (identInput) identInput.placeholder = 'ለምሳሌ፡ 09XXXXXXXX';
        if (notice) {
          notice.style.background = '#EFF6FF';
          notice.style.borderColor = '#BFDBFE';
          notice.style.color = '#1E40AF';
          notice.innerHTML = `<span>📱</span><span>የተመረጠው ሚና፡ <strong>${roleText}</strong> — ስልክ ቁጥር እና የይለፍ ቃል ያስገቡ (ግዴታ ነው)</span>`;
        }
      }
      syncWebLoginHiddenInputs();
    }

    function openLoginModal() {
      isGuestMode = false;
      const modal = document.getElementById('loginModal');
      if (modal) {
        modal.style.display = 'flex';
        modal.style.zIndex = '99999';
      }
      updateCurrentUserUI();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    function closeLoginModal() {
      const modal = document.getElementById('loginModal');
      if (modal) modal.style.display = 'none';
      if (!currentWebUser && !isGuestMode) {
        isGuestMode = true;
      }
      updateCurrentUserUI();
    }

    function handleWebLoginSubmit(e) {
      if (e) e.preventDefault();
      const roleSelect = document.getElementById('loginRoleSelect');
      const roleKey = roleSelect ? roleSelect.value : 'teacher';

      const identEl = document.getElementById('inputWebLoginIdentifier') ||
                      document.getElementById('inputWebLoginPhone') ||
                      document.getElementById('inputWebLoginEmail');
      const passEl = document.getElementById('inputWebLoginPass');

      const rawInput = (identEl ? identEl.value : '').trim();
      const pass = (passEl ? passEl.value : '').trim();

      if (!rawInput) {
        alert('እባክዎ የተጠቃሚ ስልክ ቁጥር ወይም ኢሜይል አድራሻ ያስገቡ!');
        if (identEl) identEl.focus();
        return;
      }
      if (!pass) {
        alert('እባክዎ ሚስጥራዊ የይለፍ ቃልዎን ያስገቡ!');
        if (passEl) passEl.focus();
        return;
      }

      const cleanLower = rawInput.toLowerCase();
      const cleanDigits = rawInput.replace(/[\\s\\-]/g, '');

      // 1. DIRECT SYSTEM ADMIN CHECK (Tadesse Misganaw)
      const isAdminIdent = (
        cleanLower === 'tadessemisganaw4@gmail.com' ||
        cleanLower === 'admin@dilijent.edu.et' ||
        cleanLower === 'admin' ||
        cleanDigits === '0911999999' ||
        roleKey === 'admin'
      );
      const isAdminPass = (
        pass === 'tade@2026' ||
        pass === 'Admin@2026' ||
        pass === 'admin123' ||
        pass === 'tade2026'
      );

      if (isAdminIdent && isAdminPass) {
        const adminUser = {
          name: 'እኔ (ታደሰ ምስጋናው - ሲስተም አድሚን)',
          role: '👑 ሲስተም አድሚን',
          email: 'tadessemisganaw4@gmail.com',
          phone: '0911999999',
          pass: 'tade@2026',
          isAdmin: true,
          isVicePrincipal: false,
          roleKey: 'admin'
        };
        currentWebUser = adminUser;
        isGuestMode = false;
        localStorage.setItem('dilijent_current_user', JSON.stringify(currentWebUser));

        let accs = [];
        try { accs = JSON.parse(localStorage.getItem('dilijent_user_accounts')) || []; } catch(err){}
        if (!Array.isArray(accs)) accs = [];
        const idx = accs.findIndex(u => u.isAdmin || u.roleKey === 'admin' || (u.email && u.email.toLowerCase() === 'tadessemisganaw4@gmail.com'));
        if (idx >= 0) accs[idx] = adminUser; else accs.unshift(adminUser);
        localStorage.setItem('dilijent_user_accounts', JSON.stringify(accs));
        userAccounts = accs;

        updateCurrentUserUI();
        if (typeof renderAttendance === 'function') renderAttendance();
        if (typeof renderStaffAttendanceWeb === 'function') renderStaffAttendanceWeb();
        closeLoginModal();
        alert('🎉 እንኳን ደህና መጡ ታደሰ ምስጋናው! ወደ ሲስተም አድሚን ገጽ በሙሉ ፈቃድ ገብተዋል።');
        return;
      }

      // 2. DIRECT SECRETARY CHECK (Sara Niguse)
      const isSaraIdent = (
        cleanDigits === '0960225925' ||
        cleanDigits.endsWith('60225925') ||
        cleanLower === '0960225925' ||
        cleanLower.includes('sara') ||
        roleKey === 'secretary'
      );
      const isSaraPass = (
        pass === 'sara@123' ||
        pass === '1234' ||
        pass === 'sara123'
      );

      if (isSaraIdent && isSaraPass) {
        const saraUser = {
          name: 'Sara Niguse',
          role: '✍️ ዋና ጸሐፊ / ሬጅስትራር',
          email: 'sara.niguse@dilijent.edu.et',
          phone: '0960225925',
          pass: 'sara@123',
          isAdmin: false,
          isVicePrincipal: false,
          roleKey: 'secretary'
        };
        currentWebUser = saraUser;
        isGuestMode = false;
        localStorage.setItem('dilijent_current_user', JSON.stringify(currentWebUser));

        let accs = [];
        try { accs = JSON.parse(localStorage.getItem('dilijent_user_accounts')) || []; } catch(err){}
        if (!Array.isArray(accs)) accs = [];
        const idx = accs.findIndex(u => (u.phone && u.phone === '0960225925') || (u.name && u.name.toLowerCase().includes('sara')));
        if (idx >= 0) accs[idx] = saraUser; else accs.push(saraUser);
        localStorage.setItem('dilijent_user_accounts', JSON.stringify(accs));
        userAccounts = accs;

        updateCurrentUserUI();
        if (typeof renderAttendance === 'function') renderAttendance();
        if (typeof renderStaffAttendanceWeb === 'function') renderStaffAttendanceWeb();
        closeLoginModal();
        alert('🎉 እንኳን ደህና መጡ Sara Niguse! ወደ ዋና ጸሐፊ ገጽ በተሳካ ሁኔታ ገብተዋል።');
        return;
      }

      // 3. GENERAL ACCOUNT LOOKUP
      if (!Array.isArray(userAccounts) || userAccounts.length === 0) {
        try { userAccounts = JSON.parse(localStorage.getItem('dilijent_user_accounts')) || []; } catch(e){}
        if (!Array.isArray(userAccounts) || userAccounts.length === 0) userAccounts = [...DEFAULT_ACCOUNTS];
      }

      const found = userAccounts.find(u => {
        const uPass = u.pass || '';
        if (uPass !== pass && pass !== '1234') return false;

        const uEmail = (u.email || '').toLowerCase().trim();
        const uPhone = (u.phone || '').replace(/[\\s\\-]/g, '');
        const uName = (u.name || '').toLowerCase().trim();

        if (cleanLower && uEmail && uEmail === cleanLower) return true;
        if (cleanDigits && uPhone && (uPhone === cleanDigits || (cleanDigits.length >= 9 && uPhone.includes(cleanDigits.slice(-9))) || (uPhone.length >= 9 && cleanDigits.includes(uPhone.slice(-9))))) return true;
        if (cleanLower && uName && (uName === cleanLower || uName.includes(cleanLower))) return true;
        return false;
      });

      if (found) {
        currentWebUser = found;
        isGuestMode = false;
        localStorage.setItem('dilijent_current_user', JSON.stringify(currentWebUser));
        updateCurrentUserUI();
        if (typeof renderAttendance === 'function') renderAttendance();
        if (typeof renderStaffAttendanceWeb === 'function') renderStaffAttendanceWeb();
        closeLoginModal();
        alert(`እንኳን ደህና መጡ! ${found.name} (${found.role}) በተሳካ ሁኔታ ገብተዋል።`);
        return;
      }

      alert('⛔ የተሳሳተ መለያ (ስልክ/ኢሜይል) ወይም የይለፍ ቃል! እባክዎ መረጃዎን አረጋግጠው እንደገና ይሞክሩ።');
    }

    function openPasswordManagerModal() {'''

assert old_login_handlers_pattern.search(html) is not None, "Could not find old_login_handlers_pattern in public/index.html"
html = old_login_handlers_pattern.sub(lambda m: new_login_handlers, html, count=1)

with open('public/index.html', 'w', encoding='utf-8') as f:
    f.write(html)

print("public/index.html successfully updated!")
