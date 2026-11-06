import re

with open('public/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

# Define the helper functions to insert:
staff_att_functions = '''    // Safe LocalStorage JSON helper to guarantee zero crashes on corrupted browser data
    function safeGetJSON(key, defaultVal) {
      try {
        const raw = localStorage.getItem(key);
        if (!raw || raw === 'undefined' || raw === 'null') return defaultVal;
        const parsed = JSON.parse(raw);
        return (parsed !== null && parsed !== undefined) ? parsed : defaultVal;
      } catch (e) {
        console.warn('safeGetJSON fallback for', key, e);
        return defaultVal;
      }
    }

    function shareWebsiteLink() {
      const url = window.location.href;
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(url).then(() => {
          alert('🔗 የትምህርት ቤቱ ድረ-ገጽ ሊንክ ተቀድቷል (Copied)! በማንኛውም መተግበሪያ ሊያጋሩት ይችላሉ።');
        }).catch(() => {
          prompt('የድረ-ገጹን ሊንክ ይቅዱ (Copy Link):', url);
        });
      } else {
        prompt('የድረ-ገጹን ሊንክ ይቅዱ (Copy Link):', url);
      }
    }

    function setAttendanceSubMode(mode) {
      currentAttendanceSubMode = mode;
      const btnStudents = document.getElementById('btnAttendanceSubStudents');
      const btnStaff = document.getElementById('btnAttendanceSubStaff');
      const studentContainer = document.getElementById('studentAttendanceContainer');
      const staffContainer = document.getElementById('staffAttendanceContainer');
      const restrictedBanner = document.getElementById('staffAttendanceRestrictedBanner');
      const authorizedContent = document.getElementById('staffAttendanceAuthorizedContent');

      if (mode === 'students') {
        if (btnStudents) {
          btnStudents.className = 'btn-primary';
          btnStudents.style.background = 'var(--primary)';
          btnStudents.style.color = 'white';
        }
        if (btnStaff) {
          btnStaff.className = 'btn-secondary';
          btnStaff.style.background = '#F1F5F9';
          btnStaff.style.color = '#334155';
        }
        if (studentContainer) studentContainer.style.display = 'block';
        if (staffContainer) staffContainer.style.display = 'none';
      } else {
        if (btnStudents) {
          btnStudents.className = 'btn-secondary';
          btnStudents.style.background = '#F1F5F9';
          btnStudents.style.color = '#334155';
        }
        if (btnStaff) {
          btnStaff.className = 'btn-primary';
          btnStaff.style.background = 'var(--primary)';
          btnStaff.style.color = 'white';
        }
        if (studentContainer) studentContainer.style.display = 'none';
        if (staffContainer) staffContainer.style.display = 'block';

        const hasAccess = canAccessStaffAttendanceWeb();
        if (restrictedBanner) restrictedBanner.style.display = hasAccess ? 'none' : 'block';
        if (authorizedContent) authorizedContent.style.display = hasAccess ? 'block' : 'none';

        renderStaffAttendanceWeb();
      }
    }

    function setStaffAttendanceWebShift(shift) {
      currentStaffWebShift = shift;
      const btnM = document.getElementById('btnWebShiftMorning');
      const btnA = document.getElementById('btnWebShiftAfternoon');
      const shiftNotice = document.getElementById('staffShiftRuleNotice');

      if (shift === 'morning') {
        if (btnM) { btnM.className = 'btn-primary'; btnM.style.background = 'var(--primary)'; btnM.style.color = 'white'; }
        if (btnA) { btnA.className = 'btn-secondary'; btnA.style.background = '#F1F5F9'; btnA.style.color = '#334155'; }
        if (shiftNotice) shiftNotice.innerHTML = '🌅 የጧት መግቢያ ክፍለ-ጊዜ • መደበኛ የገቢ ሰዓት ከ01:30 - 01:51 ጧት';
      } else {
        if (btnM) { btnM.className = 'btn-secondary'; btnM.style.background = '#F1F5F9'; btnM.style.color = '#334155'; }
        if (btnA) { btnA.className = 'btn-primary'; btnA.style.background = 'var(--primary)'; btnA.style.color = 'white'; }
        if (shiftNotice) shiftNotice.innerHTML = '🌇 የከሰዓት መውጫ ክፍለ-ጊዜ • መደበኛ የወጪ ሰዓት ከ09:15 - 09:40 ከሰዓት';
      }
      renderStaffAttendanceWeb();
    }

    function filterStaffAttCategory(category, btn) {
      currentStaffAttCategory = category;
      document.querySelectorAll('.staff-dept-filter').forEach(b => {
        b.style.background = '#F1F5F9';
        b.style.color = '#334155';
      });
      if (btn) {
        btn.style.background = 'var(--primary)';
        btn.style.color = 'white';
      }
      renderStaffAttendanceWeb();
    }

    function renderStaffAttendanceWeb() {
      const tbody = document.getElementById('staffAttendanceTableBody');
      if (!tbody) return;

      const isMorning = currentStaffWebShift === 'morning';
      const q = (document.getElementById('staffAttSearchInput')?.value || '').trim().toLowerCase();

      let totalCount = 0;
      let presentCount = 0;
      let lateCount = 0;
      let absentCount = 0;
      let leaveCount = 0;

      const list = Array.isArray(staffAttendanceListWeb) ? staffAttendanceListWeb : [];

      const filtered = list.filter(item => {
        if (!item) return false;
        if (currentStaffAttCategory === 'teachers' && !item.isTeacher) return false;
        if (currentStaffAttCategory === 'admin_sec' && item.isTeacher) return false;
        if (q) {
          const matchName = (item.name || '').toLowerCase().includes(q);
          const matchRole = (item.role || '').toLowerCase().includes(q);
          const matchDept = (item.dept || '').toLowerCase().includes(q);
          if (!matchName && !matchRole && !matchDept) return false;
        }
        return true;
      });

      list.forEach(item => {
        if (!item) return;
        totalCount++;
        const st = isMorning ? (item.morningStatus || 'ተገኝቷል') : (item.afternoonStatus || 'ተገኝቷል');
        if (st === 'ተገኝቷል' || st === 'Present') presentCount++;
        else if (st === 'ዘግይቷል' || st === 'Late') lateCount++;
        else if (st === 'ቀርቷል' || st === 'Absent') absentCount++;
        else if (st === 'ፍቃድ' || st === 'Leave') leaveCount++;
      });

      const statTotal = document.getElementById('staffAttStatTotal');
      const statPresent = document.getElementById('staffAttStatPresent');
      const statLate = document.getElementById('staffAttStatLate');
      const statAbsent = document.getElementById('staffAttStatAbsent');
      const statLeave = document.getElementById('staffAttStatLeave');

      if (statTotal) statTotal.innerText = totalCount;
      if (statPresent) statPresent.innerText = presentCount;
      if (statLate) statLate.innerText = lateCount;
      if (statAbsent) statAbsent.innerText = absentCount;
      if (statLeave) statLeave.innerText = leaveCount;

      if (filtered.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:1.5rem; color:#64748B;">ምንም የሰራተኛ መረጃ አልተገኘም</td></tr>`;
        return;
      }

      tbody.innerHTML = filtered.map((item) => {
        const timeVal = isMorning ? (item.morningTime || '01:30 ጧት') : (item.afternoonTime || '09:20 ከሰዓት');
        const statusVal = isMorning ? (item.morningStatus || 'ተገኝቷል') : (item.afternoonStatus || 'ተገኝቷል');
        const remarksVal = isMorning ? (item.morningRemarks || 'በሰዓቱ ገብተዋል') : (item.afternoonRemarks || 'በሰዓቱ ተገኝተዋል');

        const mStatus = item.morningStatus || 'ተገኝቷል';
        const aStatus = item.afternoonStatus || 'ተገኝቷል';

        const statusBadgeClass = statusVal === 'ተገኝቷል' ? 'badge-success' : (statusVal === 'ዘግይቷል' ? 'badge-warning' : (statusVal === 'ፍቃድ' ? 'badge-info' : 'badge-danger'));

        return `
          <tr>
            <td>
              <strong>${item.name}</strong>
              <br><small style="color:#64748B;">${item.id || ''}</small>
            </td>
            <td>
              <span class="badge badge-primary">${item.role || 'ሰራተኛ'}</span>
              <br><small style="color:#64748B;">${item.dept || ''}</small>
            </td>
            <td>
              <div style="font-size:0.75rem; display:flex; flex-direction:column; gap:2px;">
                <span>🌅 ጧት፡ <strong>${item.morningTime || '-'}</strong> (${mStatus})</span>
                <span>🌇 ከሰዓት፡ <strong>${item.afternoonTime || '-'}</strong> (${aStatus})</span>
              </div>
            </td>
            <td><strong style="color:var(--primary); font-family:monospace;">${timeVal}</strong></td>
            <td><span class="badge ${statusBadgeClass}">${statusVal}</span></td>
            <td><small style="color:#475569;">${remarksVal}</small></td>
            <td style="text-align:center;">
              <button class="btn-secondary" style="padding:0.25rem 0.5rem; font-size:0.75rem;" onclick="openStaffAttEditModal('${item.id}')">✏️ አስተካክል</button>
            </td>
          </tr>
        `;
      }).join('');
    }

    function saveStaffAttendanceWeb() {
      try {
        localStorage.setItem('dilijent_staff_attendance', JSON.stringify(staffAttendanceListWeb));
      } catch (err) {
        console.error('Error saving staff attendance:', err);
      }
      if (typeof renderStaffAttendanceWeb === 'function') {
        try { renderStaffAttendanceWeb(); } catch (e) { console.error(e); }
      }
    }
'''

# Replace `function saveStaffAttendanceWeb() { ... }` block with `staff_att_functions`
old_save_block = re.search(r'function saveStaffAttendanceWeb\(\)[\s\S]*?saveStaffAttendanceWeb\(\);', html)
if old_save_block:
    html = html[:old_save_block.start()] + staff_att_functions + html[old_save_block.end():]
    print("Replaced saveStaffAttendanceWeb block with full definitions.")
else:
    print("Could not find old saveStaffAttendanceWeb block!")

# Replace unprotected JSON.parse(localStorage.getItem('dilijent_current_user'))
html = html.replace(
    "let currentWebUser = JSON.parse(localStorage.getItem('dilijent_current_user')) || null;",
    "let currentWebUser = safeGetJSON('dilijent_current_user', null);"
)

# Also remove minlength="6" from login password inputs so shorter PINs (like 1234) don't trigger silent HTML5 validation blocks
html = html.replace('id="inputFullLoginPass" class="form-control" required placeholder="የይለፍ ቃልዎን እዚህ ይተይቡ" value="" minlength="6"', 'id="inputFullLoginPass" class="form-control" required placeholder="የይለፍ ቃልዎን እዚህ ይተይቡ" value=""')
html = html.replace('id="inputWebLoginPass" class="form-control" required placeholder="የይለፍ ቃልዎን እዚህ ይተይቡ" value="" minlength="6"', 'id="inputWebLoginPass" class="form-control" required placeholder="የይለፍ ቃልዎን እዚህ ይተይቡ" value=""')

# Make sure enterGuestModeWeb is robust
if 'function enterGuestModeWeb' not in html:
    guest_func = '''
    function enterGuestModeWeb() {
      isGuestMode = true;
      currentWebUser = null;
      updateCurrentUserUI();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
'''
    html = html.replace('function openLoginModal()', guest_func + '\n    function openLoginModal()')

with open('public/index.html', 'w', encoding='utf-8') as f:
    f.write(html)

print("public/index.html successfully updated with safe functions!")
