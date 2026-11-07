import re
import sys

def main():
    with open('public/index.html', 'r', encoding='utf-8') as f:
        html = f.read()

    # =========================================================================
    # 1. ADD EDIT STUDENT MODAL AND EDIT STAFF MODAL HTML
    # =========================================================================
    target_modal_anchor = '  <!-- Modal: Auto Excel/PDF/CSV Converter & Importer -->'
    
    new_modals_html = '''  <!-- Modal: Edit Student -->
  <div id="editStudentModal" class="modal-overlay">
    <div class="modal-box">
      <div class="modal-title">✏️ የተማሪ መረጃ ማሻሻያ (Edit Student)</div>
      <p style="font-size:0.8rem; color:#475569; margin-bottom:1rem;">የተማሪውን መረጃ በማንኛውም ሰዓት አሻሽለው ማዳን ይችላሉ።</p>
      <form onsubmit="saveEditedStudent(event)">
        <input type="hidden" id="editStudentId" value="">
        <!-- Photo Upload Box -->
        <div class="form-group" style="text-align:center;">
          <label style="display:block; margin-bottom:0.4rem; font-weight:600;">የተማሪው ፎቶ</label>
          <div style="display:flex; flex-direction:column; align-items:center;">
            <input type="file" id="editStudentPhotoInput" accept="image/*" style="display:none;" onchange="handleEditStudentPhotoSelected(event)">
            <div id="editStudentPhotoPreview" onclick="document.getElementById('editStudentPhotoInput').click()" style="width:90px; height:90px; border-radius:50%; border:2.5px dashed var(--primary); display:flex; flex-direction:column; align-items:center; justify-content:center; cursor:pointer; background-size:cover; background-position:center; background-color:#EFF6FF; overflow:hidden; transition:0.2s; box-shadow:0 2px 8px rgba(0,0,0,0.08);">
              <span style="font-size:1.8rem;">📸</span>
              <span style="font-size:0.65rem; color:var(--primary); font-weight:700;">ፎቶ ቀይር</span>
            </div>
            <div style="margin-top:0.4rem; display:flex; gap:0.4rem;">
              <button type="button" class="btn-secondary" style="padding:0.25rem 0.6rem; font-size:0.72rem;" onclick="document.getElementById('editStudentPhotoInput').click()">📁 ፎቶ ምረጥ</button>
              <button type="button" class="btn-secondary" style="padding:0.25rem 0.6rem; font-size:0.72rem; color:var(--danger);" onclick="clearEditStudentPhoto()">🗑️ አስወግድ</button>
            </div>
          </div>
        </div>

        <div class="form-group">
          <label>የተማሪው ሙሉ ስም *</label>
          <input type="text" id="editStudentName" class="form-control" required placeholder="ሙሉ ስም">
        </div>
        <div style="display:grid; grid-template-columns:1fr 1fr; gap:0.5rem;">
          <div class="form-group">
            <label>ጾታ *</label>
            <select id="editStudentGender" class="form-control">
              <option value="ወንድ">ወንድ (Male)</option>
              <option value="ሴት">ሴት (Female)</option>
            </select>
          </div>
          <div class="form-group">
            <label>ክፍል *</label>
            <select id="editStudentGrade" class="form-control">
              <option value="12A">12A (Natural Science)</option>
              <option value="12B">12B (Social Science)</option>
              <option value="11A">11A (Natural Science)</option>
              <option value="11B">11B (Social Science)</option>
              <option value="10A">10A</option>
              <option value="9A">9A</option>
              <option value="8ኛ ክፍል">8ኛ ክፍል</option>
              <option value="7ኛ ክፍል">7ኛ ክፍል</option>
              <option value="6ኛ ክፍል">6ኛ ክፍል</option>
              <option value="5ኛ ክፍል">5ኛ ክፍል</option>
              <option value="4ኛ ክፍል">4ኛ ክፍል</option>
              <option value="3ኛ ክፍል">3ኛ ክፍል</option>
              <option value="2ኛ ክፍል">2ኛ ክፍል</option>
              <option value="1ኛ ክፍል">1ኛ ክፍል</option>
            </select>
          </div>
        </div>
        <div style="display:grid; grid-template-columns:1fr 1fr; gap:0.5rem;">
          <div class="form-group">
            <label>የወላጅ/አሳዳጊ ስልክ ቁጥር *</label>
            <input type="tel" id="editStudentPhone" class="form-control" required placeholder="09XXXXXXXX">
          </div>
          <div class="form-group">
            <label>የተማሪው ሁኔታ (Status) *</label>
            <select id="editStudentStatus" class="form-control">
              <option value="ንቁ">ንቁ (Active)</option>
              <option value="ያቋረጠ">ያቋረጠ (Dropped)</option>
              <option value="የተዛወረ">የተዛወረ (Transferred)</option>
            </select>
          </div>
        </div>
        <div class="modal-actions">
          <button type="button" class="btn-secondary" onclick="closeEditStudentModal()">ሰርዝ (Cancel)</button>
          <button type="submit" class="btn-primary" style="background:#2563EB;">💾 አሻሽልና መዝግብ (Update Student)</button>
        </div>
      </form>
    </div>
  </div>

  <!-- Modal: Edit Staff/Teacher -->
  <div id="editStaffModal" class="modal-overlay">
    <div class="modal-box">
      <div class="modal-title">✏️ የመምህር / ሰራተኛ መረጃ ማሻሻያ (Edit Staff)</div>
      <p style="font-size:0.8rem; color:#475569; margin-bottom:1rem;">የሰራተኛውን ወይም የመምህሩን መረጃ በማንኛውም ሰዓት አሻሽለው ማዳን ይችላሉ።</p>
      <form onsubmit="saveEditedStaff(event)">
        <input type="hidden" id="editStaffId" value="">
        <!-- Photo Upload Box -->
        <div class="form-group" style="text-align:center;">
          <label style="display:block; margin-bottom:0.4rem; font-weight:600;">የሰራተኛው / መምህሩ ፎቶ</label>
          <div style="display:flex; flex-direction:column; align-items:center;">
            <input type="file" id="editStaffPhotoInput" accept="image/*" style="display:none;" onchange="handleEditStaffPhotoSelected(event)">
            <div id="editStaffPhotoPreview" onclick="document.getElementById('editStaffPhotoInput').click()" style="width:90px; height:90px; border-radius:50%; border:2.5px dashed var(--primary); display:flex; flex-direction:column; align-items:center; justify-content:center; cursor:pointer; background-size:cover; background-position:center; background-color:#EFF6FF; overflow:hidden; transition:0.2s; box-shadow:0 2px 8px rgba(0,0,0,0.08);">
              <span style="font-size:1.8rem;">📸</span>
              <span style="font-size:0.65rem; color:var(--primary); font-weight:700;">ፎቶ ቀይር</span>
            </div>
            <div style="margin-top:0.4rem; display:flex; gap:0.4rem;">
              <button type="button" class="btn-secondary" style="padding:0.25rem 0.6rem; font-size:0.72rem;" onclick="document.getElementById('editStaffPhotoInput').click()">📁 ፎቶ ምረጥ</button>
              <button type="button" class="btn-secondary" style="padding:0.25rem 0.6rem; font-size:0.72rem; color:var(--danger);" onclick="clearEditStaffPhoto()">🗑️ አስወግድ</button>
            </div>
          </div>
        </div>

        <div class="form-group">
          <label>ሙሉ ስም *</label>
          <input type="text" id="editStaffName" class="form-control" required placeholder="ሙሉ ስም">
        </div>

        <div style="display:grid; grid-template-columns:1fr 1fr; gap:0.5rem;">
          <div class="form-group">
            <label>የስራ መደብ (Role) *</label>
            <select id="editStaffRole" class="form-control">
              <option value="መምህር">መምህር (Teacher)</option>
              <option value="የክፍል ኃላፊ">የክፍል ኃላፊ (Homeroom)</option>
              <option value="የት/ክፍል ኃላፊ">የት/ክፍል ኃላፊ (Dept. Head)</option>
              <option value="ርዕሰ መምህር">ርዕሰ መምህር (Principal)</option>
              <option value="ምክትል ርዕሰ መምህር">ምክትል ርዕሰ መምህር</option>
              <option value="ዋና ጸሐፊ">ዋና ጸሐፊ (Secretary)</option>
              <option value="ፋይናንስ/ሂሳብ ሹም">ፋይናንስ / ሂሳብ ሹም</option>
              <option value="የጥበቃ ኃላፊ">የጥበቃ ኃላፊ</option>
              <option value="የፅዳት ሰራተኛ">የፅዳት ሰራተኛ</option>
            </select>
          </div>
          <div class="form-group">
            <label>የትምህርት/ስራ ዘርፍ *</label>
            <input type="text" id="editStaffDept" class="form-control" required placeholder="ዘርፍ">
          </div>
        </div>

        <div style="display:grid; grid-template-columns:1fr 1fr; gap:0.5rem;">
          <div class="form-group">
            <label>ስልክ ቁጥር *</label>
            <input type="tel" id="editStaffPhone" class="form-control" required placeholder="09XXXXXXXX">
          </div>
          <div class="form-group">
            <label>የትምህርት ደረጃ</label>
            <select id="editStaffQual" class="form-control">
              <option value="ባችለር ዲግሪ">ባችለር ዲግሪ (BA/BSc)</option>
              <option value="ማስተርስ">ማስተርስ (MA/MSc)</option>
              <option value="ዲፕሎማ">ዲፕሎማ (Diploma)</option>
              <option value="ሰርተፊኬት">ሰርተፊኬት</option>
              <option value="ሌላ">ሌላ</option>
            </select>
          </div>
        </div>

        <div class="form-group">
          <label>የሰራተኛው ሁኔታ (Status) *</label>
          <select id="editStaffStatus" class="form-control">
            <option value="ንቁ">ንቁ (Active)</option>
            <option value="ፈቃድ ላይ">ፈቃድ ላይ (On Leave)</option>
            <option value="የተዛወረ">የተዛወረ (Transferred)</option>
          </select>
        </div>

        <div class="modal-actions">
          <button type="button" class="btn-secondary" onclick="closeEditStaffModal()">ሰርዝ (Cancel)</button>
          <button type="submit" class="btn-primary" style="background:#2563EB;">💾 አሻሽልና መዝግብ (Update Staff)</button>
        </div>
      </form>
    </div>
  </div>

'''
    assert target_modal_anchor in html, "Could not find target_modal_anchor"
    html = html.replace(target_modal_anchor, new_modals_html + target_modal_anchor, 1)

    # =========================================================================
    # 2. UPDATE renderStudents() TO INCLUDE ✏️ አሻሽል BUTTON
    # =========================================================================
    old_student_action = '''          <td style="text-align:center;">
            <button class="btn-secondary" style="padding:0.25rem 0.5rem; font-size:0.75rem; color:var(--danger);" onclick="removeStudent('${s.id}')">🗑️ ሰርዝ</button>
          </td>'''

    new_student_action = '''          <td style="text-align:center; white-space:nowrap;">
            <button class="btn-secondary" style="padding:0.25rem 0.55rem; font-size:0.75rem; color:#2563EB; font-weight:700; border-color:#93C5FD; margin-right:4px;" onclick="openEditStudentModal('${s.id}')">✏️ አሻሽል</button>
            <button class="btn-secondary" style="padding:0.25rem 0.55rem; font-size:0.75rem; color:var(--danger); font-weight:700; border-color:#FCA5A5;" onclick="removeStudent('${s.id}')">🗑️ ሰርዝ</button>
          </td>'''

    assert old_student_action in html, "Could not find old_student_action"
    html = html.replace(old_student_action, new_student_action, 1)

    # =========================================================================
    # 3. UPDATE renderStaff() TO INCLUDE ✏️ አሻሽል BUTTON
    # =========================================================================
    old_staff_action = '''          <td style="text-align:center;">
            <button class="btn-secondary" style="padding:0.25rem 0.5rem; font-size:0.75rem; color:var(--danger);" onclick="removeStaff('${st.id}')">🗑️ ሰርዝ</button>
          </td>'''

    new_staff_action = '''          <td style="text-align:center; white-space:nowrap;">
            <button class="btn-secondary" style="padding:0.25rem 0.55rem; font-size:0.75rem; color:#2563EB; font-weight:700; border-color:#93C5FD; margin-right:4px;" onclick="openEditStaffModal('${st.id}')">✏️ አሻሽል</button>
            <button class="btn-secondary" style="padding:0.25rem 0.55rem; font-size:0.75rem; color:var(--danger); font-weight:700; border-color:#FCA5A5;" onclick="removeStaff('${st.id}')">🗑️ ሰርዝ</button>
          </td>'''

    assert old_staff_action in html, "Could not find old_staff_action"
    html = html.replace(old_staff_action, new_staff_action, 1)

    # =========================================================================
    # 4. UPDATE renderStaffAttendanceWeb() TO INCLUDE QUICK ACTION BUTTONS
    # =========================================================================
    old_att_action = '''            <td style="text-align:center;">
              <button class="btn-secondary" style="padding:0.25rem 0.5rem; font-size:0.75rem;" onclick="openStaffAttEditModal('${item.id}')">✏️ አስተካክል</button>
            </td>'''

    new_att_action = '''            <td style="text-align:center; white-space:nowrap;">
              <div style="display:inline-flex; gap:3px; align-items:center; flex-wrap:wrap; justify-content:center;">
                <button type="button" class="btn-secondary" style="padding:0.2rem 0.45rem; font-size:0.72rem; color:#166534; background:#DCFCE7; border-color:#86EFAC; font-weight:700;" onclick="quickMarkStaffAttendanceWeb('${item.id}', 'ተገኝቷል')" title="በሰዓቱ ተገኝቷል">✓ ተገኝቷል</button>
                <button type="button" class="btn-secondary" style="padding:0.2rem 0.45rem; font-size:0.72rem; color:#92400E; background:#FEF3C7; border-color:#FCD34D; font-weight:700;" onclick="quickMarkStaffAttendanceWeb('${item.id}', 'ዘግይቷል')" title="ዘግይቶ ገብቷል">⏱ ዘግይቷል</button>
                <button type="button" class="btn-secondary" style="padding:0.2rem 0.45rem; font-size:0.72rem; color:#991B1B; background:#FEE2E2; border-color:#FCA5A5; font-weight:700;" onclick="quickMarkStaffAttendanceWeb('${item.id}', 'ቀርቷል')" title="አልተገኘም / ቀሪ">✕ ቀርቷል</button>
                <button type="button" class="btn-secondary" style="padding:0.2rem 0.45rem; font-size:0.72rem; color:#5B21B6; background:#EDE9FE; border-color:#C4B5FD; font-weight:700;" onclick="quickMarkStaffAttendanceWeb('${item.id}', 'ፍቃድ')" title="በፍቃድ ላይ">📝 ፍቃድ</button>
                <button type="button" class="btn-secondary" style="padding:0.2rem 0.45rem; font-size:0.72rem; color:#1E40AF; background:#EFF6FF; border-color:#BFDBFE; font-weight:700;" onclick="openStaffAttEditModal('${item.id}')" title="ሰዓት እና ማስታወሻ አርም">✏️ ሰዓት</button>
              </div>
            </td>'''

    assert old_att_action in html, "Could not find old_att_action"
    html = html.replace(old_att_action, new_att_action, 1)

    # =========================================================================
    # 5. RESTRICT PASSWORD MANAGER & SETTINGS VISIBILITY TO ADMIN ONLY
    # =========================================================================
    old_ui_settings_block = '''      // KEEP SETTINGS & PASSWORD MANAGER BUTTONS ALWAYS VISIBLE & FULLY FUNCTIONAL
      const btnSettingsHeader = document.getElementById('btnSchoolSettingsHeader');
      const btnPassHeader = document.getElementById('btnAdminPassManager');
      const btnSettingsBanner = document.getElementById('btnBannerSchoolSettings');
      const btnPassBanner = document.getElementById('btnBannerPassManager');
      const headerLogoGroup = document.getElementById('headerLogoGroup') || document.querySelector('.logo-group');

      if (btnSettingsHeader) btnSettingsHeader.style.display = 'inline-flex';
      if (btnPassHeader) btnPassHeader.style.display = 'inline-flex';
      if (btnSettingsBanner) btnSettingsBanner.style.display = 'inline-block';
      if (btnPassBanner) btnPassBanner.style.display = 'inline-block';

      if (headerLogoGroup) {
        headerLogoGroup.style.cursor = 'pointer';
        headerLogoGroup.title = 'የትምህርት ቤት አርማ እና ቅንብር ለመቀየር ይጫኑ';
      }'''

    new_ui_settings_block = '''      // PASSWORD MANAGER & SETTINGS: STRICTLY RESTRICTED TO SYSTEM ADMIN ONLY
      const btnSettingsHeader = document.getElementById('btnSchoolSettingsHeader');
      const btnPassHeader = document.getElementById('btnAdminPassManager');
      const btnSettingsBanner = document.getElementById('btnBannerSchoolSettings');
      const btnPassBanner = document.getElementById('btnBannerPassManager');
      const btnSchemeBanner = document.getElementById('btnBannerScheme');
      const headerLogoGroup = document.getElementById('headerLogoGroup') || document.querySelector('.logo-group');

      if (btnSettingsHeader) btnSettingsHeader.style.display = isAdmin ? 'inline-flex' : 'none';
      if (btnPassHeader) btnPassHeader.style.display = isAdmin ? 'inline-flex' : 'none';
      if (btnSettingsBanner) btnSettingsBanner.style.display = isAdmin ? 'inline-block' : 'none';
      if (btnPassBanner) btnPassBanner.style.display = isAdmin ? 'inline-block' : 'none';
      if (btnSchemeBanner) btnSchemeBanner.style.display = isAdmin ? 'inline-block' : 'none';

      if (headerLogoGroup) {
        if (isAdmin) {
          headerLogoGroup.style.cursor = 'pointer';
          headerLogoGroup.title = 'የትምህርት ቤት አርማ እና ቅንብር ለመቀየር ይጫኑ';
        } else {
          headerLogoGroup.style.cursor = 'default';
          headerLogoGroup.title = 'ዲሊጀንት ትምህርት ቤት';
        }
      }'''

    assert old_ui_settings_block in html, "Could not find old_ui_settings_block"
    html = html.replace(old_ui_settings_block, new_ui_settings_block, 1)

    # =========================================================================
    # 6. RESTRICT openSchoolSettingsModal & openPasswordManagerModal TO ADMIN ONLY
    # =========================================================================
    old_open_settings = '''    function openSchoolSettingsModal() {
      try {'''

    new_open_settings = '''    function openSchoolSettingsModal() {
      if (!isCurrentUserAdminWeb()) {
        alert('⛔ የተከለከለ ፈቃድ!\\n\\nየትምህርት ቤት ቅንብር እና አርማ (Logo) የመቀየር ፈቃድ የተሰጠው ለሲስተም አድሚን (System Admin) ብቻ ነው።');
        return;
      }
      try {'''

    assert old_open_settings in html, "Could not find old_open_settings"
    html = html.replace(old_open_settings, new_open_settings, 1)

    old_open_pass_manager = '''    function openPasswordManagerModal() {
      try {'''

    new_open_pass_manager = '''    function openPasswordManagerModal() {
      if (!isCurrentUserAdminWeb()) {
        alert('⛔ የተከለከለ ፈቃድ!\\n\\nየተጠቃሚዎችን የይለፍ ቃል መቆጣጠር እና መቀየር የሚቻለው በሲስተም አድሚን (System Admin) ገጽ ብቻ ነው።');
        return;
      }
      try {'''

    assert old_open_pass_manager in html, "Could not find old_open_pass_manager"
    html = html.replace(old_open_pass_manager, new_open_pass_manager, 1)

    # =========================================================================
    # 7. ADD JAVASCRIPT FUNCTIONS: openEditStudentModal, saveEditedStudent,
    #                              openEditStaffModal, saveEditedStaff,
    #                              quickMarkStaffAttendanceWeb
    # =========================================================================
    js_functions_code = '''
    // ==========================================
    // EDIT STUDENT & EDIT STAFF HANDLERS
    // ==========================================
    let editingStudentId = null;
    let selectedEditStudentPhotoBase64 = '';

    function openEditStudentModal(id) {
      const student = students.find(s => String(s.id).toLowerCase() === String(id).toLowerCase());
      if (!student) {
        alert('⚠️ ተማሪው በዳታቤዝ ውስጥ አልተገኘም!');
        return;
      }
      editingStudentId = student.id;
      selectedEditStudentPhotoBase64 = student.photo || '';

      const idEl = document.getElementById('editStudentId');
      const nameEl = document.getElementById('editStudentName');
      const genderEl = document.getElementById('editStudentGender');
      const gradeEl = document.getElementById('editStudentGrade');
      const phoneEl = document.getElementById('editStudentPhone');
      const statusEl = document.getElementById('editStudentStatus');
      const previewEl = document.getElementById('editStudentPhotoPreview');

      if (idEl) idEl.value = student.id;
      if (nameEl) nameEl.value = student.name || '';
      if (genderEl) genderEl.value = student.gender || 'ወንድ';
      if (gradeEl) {
        let found = false;
        for (let opt of gradeEl.options) {
          if (opt.value === student.grade) {
            opt.selected = true;
            found = true;
            break;
          }
        }
        if (!found && student.grade) {
          const newOpt = document.createElement('option');
          newOpt.value = student.grade;
          newOpt.innerText = student.grade;
          newOpt.selected = true;
          gradeEl.appendChild(newOpt);
        }
      }
      if (phoneEl) phoneEl.value = student.phone || '';
      if (statusEl) statusEl.value = student.status || 'ንቁ';

      if (previewEl) {
        if (student.photo) {
          previewEl.style.backgroundImage = `url('${student.photo}')`;
          previewEl.innerHTML = '';
        } else {
          previewEl.style.backgroundImage = 'none';
          previewEl.innerHTML = '<span style="font-size:1.8rem;">📸</span><span style="font-size:0.65rem; color:var(--primary); font-weight:700;">ፎቶ ቀይር</span>';
        }
      }

      const modal = document.getElementById('editStudentModal');
      if (modal) modal.style.display = 'flex';
    }

    function closeEditStudentModal() {
      const modal = document.getElementById('editStudentModal');
      if (modal) modal.style.display = 'none';
      editingStudentId = null;
      selectedEditStudentPhotoBase64 = '';
    }

    function handleEditStudentPhotoSelected(e) {
      const file = e.target.files[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = function(evt) {
        selectedEditStudentPhotoBase64 = evt.target.result;
        const preview = document.getElementById('editStudentPhotoPreview');
        if (preview) {
          preview.style.backgroundImage = `url('${selectedEditStudentPhotoBase64}')`;
          preview.innerHTML = '';
        }
      };
      reader.readAsDataURL(file);
    }

    function clearEditStudentPhoto() {
      selectedEditStudentPhotoBase64 = '';
      const preview = document.getElementById('editStudentPhotoPreview');
      const input = document.getElementById('editStudentPhotoInput');
      if (input) input.value = '';
      if (preview) {
        preview.style.backgroundImage = 'none';
        preview.innerHTML = '<span style="font-size:1.8rem;">📸</span><span style="font-size:0.65rem; color:var(--primary); font-weight:700;">ፎቶ ምረጥ</span>';
      }
    }

    function saveEditedStudent(e) {
      if (e) e.preventDefault();
      if (!editingStudentId) return;

      const student = students.find(s => String(s.id).toLowerCase() === String(editingStudentId).toLowerCase());
      if (!student) {
        alert('⚠️ ተማሪው አልተገኘም!');
        return;
      }

      const newName = (document.getElementById('editStudentName')?.value || '').trim();
      const newGender = document.getElementById('editStudentGender')?.value || 'ወንድ';
      const newGrade = document.getElementById('editStudentGrade')?.value || student.grade;
      const newPhone = (document.getElementById('editStudentPhone')?.value || '').trim();
      const newStatus = document.getElementById('editStudentStatus')?.value || 'ንቁ';

      if (!newName) {
        alert('እባክዎ የተማሪውን ሙሉ ስም ያስገቡ!');
        return;
      }

      student.name = newName;
      student.gender = newGender;
      student.grade = newGrade;
      student.phone = newPhone;
      student.status = newStatus;
      if (selectedEditStudentPhotoBase64) {
        student.photo = selectedEditStudentPhotoBase64;
      }

      localStorage.setItem('dilijent_students', JSON.stringify(students));
      saveState();
      closeEditStudentModal();
      renderStudents();
      renderHomeroomRoster();
      renderGrades();
      renderDashboard();
      alert(`✅ የተማሪ ${newName} (${student.id}) መረጃ በተሳካ ሁኔታ ተሻሽሏል!`);
    }

    let editingStaffId = null;
    let selectedEditStaffPhotoBase64 = '';

    function openEditStaffModal(id) {
      const staff = staffList.find(st => String(st.id).toLowerCase() === String(id).toLowerCase());
      if (!staff) {
        alert('⚠️ ሰራተኛው በዳታቤዝ ውስጥ አልተገኘም!');
        return;
      }
      editingStaffId = staff.id;
      selectedEditStaffPhotoBase64 = staff.photo || '';

      const idEl = document.getElementById('editStaffId');
      const nameEl = document.getElementById('editStaffName');
      const roleEl = document.getElementById('editStaffRole');
      const deptEl = document.getElementById('editStaffDept');
      const phoneEl = document.getElementById('editStaffPhone');
      const qualEl = document.getElementById('editStaffQual');
      const statusEl = document.getElementById('editStaffStatus');
      const previewEl = document.getElementById('editStaffPhotoPreview');

      if (idEl) idEl.value = staff.id;
      if (nameEl) nameEl.value = staff.name || '';
      if (roleEl) roleEl.value = staff.role || 'መምህር';
      if (deptEl) deptEl.value = staff.dept || '';
      if (phoneEl) phoneEl.value = staff.phone || '';
      if (qualEl) qualEl.value = staff.qual || 'ባችለር ዲግሪ';
      if (statusEl) statusEl.value = staff.status || 'ንቁ';

      if (previewEl) {
        if (staff.photo) {
          previewEl.style.backgroundImage = `url('${staff.photo}')`;
          previewEl.innerHTML = '';
        } else {
          previewEl.style.backgroundImage = 'none';
          previewEl.innerHTML = '<span style="font-size:1.8rem;">📸</span><span style="font-size:0.65rem; color:var(--primary); font-weight:700;">ፎቶ ቀይር</span>';
        }
      }

      const modal = document.getElementById('editStaffModal');
      if (modal) modal.style.display = 'flex';
    }

    function closeEditStaffModal() {
      const modal = document.getElementById('editStaffModal');
      if (modal) modal.style.display = 'none';
      editingStaffId = null;
      selectedEditStaffPhotoBase64 = '';
    }

    function handleEditStaffPhotoSelected(e) {
      const file = e.target.files[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = function(evt) {
        selectedEditStaffPhotoBase64 = evt.target.result;
        const preview = document.getElementById('editStaffPhotoPreview');
        if (preview) {
          preview.style.backgroundImage = `url('${selectedEditStaffPhotoBase64}')`;
          preview.innerHTML = '';
        }
      };
      reader.readAsDataURL(file);
    }

    function clearEditStaffPhoto() {
      selectedEditStaffPhotoBase64 = '';
      const preview = document.getElementById('editStaffPhotoPreview');
      const input = document.getElementById('editStaffPhotoInput');
      if (input) input.value = '';
      if (preview) {
        preview.style.backgroundImage = 'none';
        preview.innerHTML = '<span style="font-size:1.8rem;">📸</span><span style="font-size:0.65rem; color:var(--primary); font-weight:700;">ፎቶ ምረጥ</span>';
      }
    }

    function saveEditedStaff(e) {
      if (e) e.preventDefault();
      if (!editingStaffId) return;

      const staff = staffList.find(st => String(st.id).toLowerCase() === String(editingStaffId).toLowerCase());
      if (!staff) {
        alert('⚠️ ሰራተኛው አልተገኘም!');
        return;
      }

      const oldName = staff.name;
      const newName = (document.getElementById('editStaffName')?.value || '').trim();
      const newRole = (document.getElementById('editStaffRole')?.value || '').trim();
      const newDept = (document.getElementById('editStaffDept')?.value || '').trim();
      const newPhone = (document.getElementById('editStaffPhone')?.value || '').trim();
      const newQual = (document.getElementById('editStaffQual')?.value || '').trim();
      const newStatus = document.getElementById('editStaffStatus')?.value || 'ንቁ';

      if (!newName) {
        alert('እባክዎ ሙሉ ስም ያስገቡ!');
        return;
      }

      staff.name = newName;
      staff.role = newRole;
      staff.dept = newDept;
      staff.phone = newPhone;
      staff.qual = newQual;
      staff.status = newStatus;
      if (selectedEditStaffPhotoBase64) {
        staff.photo = selectedEditStaffPhotoBase64;
      }

      localStorage.setItem('dilijent_staff_list', JSON.stringify(staffList));

      try {
        let uStaff = JSON.parse(localStorage.getItem('dilijent_user_registered_staff') || '[]');
        const idx = uStaff.findIndex(us => String(us.id).toLowerCase() === String(editingStaffId).toLowerCase() || us.name === oldName);
        if (idx >= 0) {
          uStaff[idx] = { ...uStaff[idx], ...staff };
        } else {
          uStaff.push(staff);
        }
        localStorage.setItem('dilijent_user_registered_staff', JSON.stringify(uStaff));
      } catch (err) {}

      const attMatch = staffAttendanceListWeb.find(sa => String(sa.id).toLowerCase() === String(editingStaffId).toLowerCase() || sa.name === oldName);
      if (attMatch) {
        attMatch.name = newName;
        attMatch.role = newRole;
        attMatch.dept = newDept;
        attMatch.isTeacher = newRole.toLowerCase().includes('መምህር') || newDept.toLowerCase().includes('መምህር');
        saveStaffAttendanceWeb();
      }

      const accMatch = userAccounts.find(acc => acc.name === oldName || (staff.phone && acc.phone === staff.phone));
      if (accMatch) {
        accMatch.name = newName;
        accMatch.role = newRole;
        accMatch.phone = newPhone;
        localStorage.setItem('dilijent_user_accounts', JSON.stringify(userAccounts));
      }

      saveState();
      closeEditStaffModal();
      renderStaff();
      renderStaffAttendanceWeb();
      renderDashboard();
      alert(`✅ የሰራተኛ ${newName} (${staff.id}) መረጃ በተሳካ ሁኔታ ተሻሽሏል!`);
    }

    function quickMarkStaffAttendanceWeb(id, status) {
      if (!canAccessStaffAttendanceWeb()) {
        alert('🔒 የተከለከለ ፈቃድ! የሰራተኞች አቴንዳንስ መመዝገብ የሚችሉት ምክትል ርዕሰ መምህር ወይም ሲስተም አድሚን ብቻ ናቸው።');
        return;
      }
      const item = staffAttendanceListWeb.find(s => String(s.id).toLowerCase() === String(id).toLowerCase());
      if (!item) return;

      const now = new Date();
      const hours = now.getHours();
      const minutes = String(now.getMinutes()).padStart(2, '0');
      const isMorning = currentStaffWebShift === 'morning';
      const period = hours < 12 ? 'ጧት' : 'ከሰዓት';
      const displayHours = String(hours % 12 || 12).padStart(2, '0');
      const currentTimeStr = `${displayHours}:${minutes} ${period}`;

      if (isMorning) {
        item.morningStatus = status;
        item.morningTime = (status === 'ቀርቷል' || status === 'ፍቃድ') ? '-' : currentTimeStr;
        item.morningRemarks = (status === 'ተገኝቷል') ? 'በሰዓቱ ገብተዋል' : ((status === 'ዘግይቷል') ? 'ዘግይተው ገብተዋል' : ((status === 'ፍቃድ') ? 'በፈቃድ ላይ ናቸው' : 'ያልተገኙ (ቀሪ)'));
      } else {
        item.afternoonStatus = status;
        item.afternoonTime = (status === 'ቀርቷል' || status === 'ፍቃድ') ? '-' : currentTimeStr;
        item.afternoonRemarks = (status === 'ተገኝቷል') ? 'በሰዓቱ ወጥተዋል' : ((status === 'ዘግይቷል') ? 'ቀደም ብለው ወጥተዋል' : ((status === 'ፍቃድ') ? 'በፈቃድ ወጥተዋል' : 'ሳይፈርሙ ወጥተዋል'));
      }

      saveStaffAttendanceWeb();
      renderStaffAttendanceWeb();
    }
'''

    # Place the JS functions right before function removeStudent
    target_js_anchor = '    function removeStudent(idOrIdx) {'
    assert target_js_anchor in html, "Could not find target_js_anchor"
    html = html.replace(target_js_anchor, js_functions_code + '\n' + target_js_anchor, 1)

    # Write out to public/index.html, docs/index.html, index.html
    for dest in ['public/index.html', 'docs/index.html', 'index.html']:
        with open(dest, 'w', encoding='utf-8') as out:
            out.write(html)
        print(f"Updated {dest} successfully.")

if __name__ == '__main__':
    main()
