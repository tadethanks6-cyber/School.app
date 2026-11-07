import sys

def main():
    with open('public/index.html', 'r', encoding='utf-8') as f:
        text = f.read()

    # 1. Update table header
    old_th = '<th>አቴንዳንስ መዝግብ (ምክትል/አድሚን)</th>'
    new_th = '<th>የአቴንዳንስ ሁኔታ (ተለዋዋጭ ድሮፕ ዳወን)</th>'
    assert old_th in text, 'old_th not found'
    text = text.replace(old_th, new_th)

    # 2. Add selectStyle computation inside tbody.innerHTML map
    old_badge_calc = "const statusBadgeClass = statusVal === 'ተገኝቷል' ? 'badge-success' : (statusVal === 'ዘግይቷል' ? 'badge-warning' : (statusVal === 'ፍቃድ' ? 'badge-info' : 'badge-danger'));"
    new_badge_calc = """const statusBadgeClass = statusVal === 'ተገኝቷል' ? 'badge-success' : (statusVal === 'ዘግይቷል' ? 'badge-warning' : (statusVal === 'ፍቃድ' ? 'badge-info' : 'badge-danger'));
        let selectStyle = 'background:#DCFCE7; color:#166534; border:1.5px solid #86EFAC;';
        if (statusVal === 'ዘግይቷል') {
          selectStyle = 'background:#FEF3C7; color:#92400E; border:1.5px solid #FCD34D;';
        } else if (statusVal === 'ቀርቷል') {
          selectStyle = 'background:#FEE2E2; color:#991B1B; border:1.5px solid #FCA5A5;';
        } else if (statusVal === 'ፍቃድ') {
          selectStyle = 'background:#EDE9FE; color:#5B21B6; border:1.5px solid #C4B5FD;';
        }"""
    assert old_badge_calc in text, 'old_badge_calc not found'
    text = text.replace(old_badge_calc, new_badge_calc)

    # 3. Update the row rendering in renderStaffAttendanceWeb()
    old_buttons_block = """            <td style="text-align:center; white-space:nowrap;">
              <div style="display:inline-flex; gap:3px; align-items:center; flex-wrap:wrap; justify-content:center;">
                <button type="button" class="btn-secondary" style="padding:0.2rem 0.45rem; font-size:0.72rem; color:#166534; background:#DCFCE7; border-color:#86EFAC; font-weight:700;" onclick="quickMarkStaffAttendanceWeb('${item.id}', 'ተገኝቷል')" title="በሰዓቱ ተገኝቷል">✓ ተገኝቷል</button>
                <button type="button" class="btn-secondary" style="padding:0.2rem 0.45rem; font-size:0.72rem; color:#92400E; background:#FEF3C7; border-color:#FCD34D; font-weight:700;" onclick="quickMarkStaffAttendanceWeb('${item.id}', 'ዘግይቷል')" title="ዘግይቶ ገብቷል">⏱ ዘግይቷል</button>
                <button type="button" class="btn-secondary" style="padding:0.2rem 0.45rem; font-size:0.72rem; color:#991B1B; background:#FEE2E2; border-color:#FCA5A5; font-weight:700;" onclick="quickMarkStaffAttendanceWeb('${item.id}', 'ቀርቷል')" title="አልተገኘም / ቀሪ">✕ ቀርቷል</button>
                <button type="button" class="btn-secondary" style="padding:0.2rem 0.45rem; font-size:0.72rem; color:#5B21B6; background:#EDE9FE; border-color:#C4B5FD; font-weight:700;" onclick="quickMarkStaffAttendanceWeb('${item.id}', 'ፍቃድ')" title="በፍቃድ ላይ">📝 ፍቃድ</button>
                <button type="button" class="btn-secondary" style="padding:0.2rem 0.45rem; font-size:0.72rem; color:#1E40AF; background:#EFF6FF; border-color:#BFDBFE; font-weight:700;" onclick="openStaffAttEditModal('${item.id}')" title="ሰዓት እና ማስታወሻ አርም">✏️ ሰዓት</button>
              </div>
            </td>"""

    new_buttons_block = """            <td style="text-align:center; white-space:nowrap; min-width:175px;">
              <div style="display:inline-flex; align-items:center; gap:5px; justify-content:center;">
                <select onchange="handleStaffAttDropdownChange('${item.id}', this.value)"
                        style="padding:5px 9px; border-radius:18px; font-weight:800; font-size:0.78rem; cursor:pointer; outline:none; transition:all 0.15s; box-shadow:0 1px 3px rgba(0,0,0,0.06); ${selectStyle}">
                  <option value="ተገኝቷል" ${statusVal === 'ተገኝቷል' ? 'selected' : ''}>✓ ተገኝቷል</option>
                  <option value="ዘግይቷል" ${statusVal === 'ዘግይቷል' ? 'selected' : ''}>⏱ ዘግይቷል</option>
                  <option value="ቀርቷል" ${statusVal === 'ቀርቷል' ? 'selected' : ''}>✕ ቀርቷል</option>
                  <option value="ፍቃድ" ${statusVal === 'ፍቃድ' ? 'selected' : ''}>📝 ፍቃድ</option>
                  <option value="edit_time">✏️ ሰዓት አርም...</option>
                </select>
                <button type="button" class="btn-secondary" style="padding:3px 7px; border-radius:8px; font-size:0.74rem; border-color:#CBD5E1; color:#1E3A8A; font-weight:700;" onclick="openStaffAttEditModal('${item.id}')" title="ሰዓት እና ማስታወሻ በእጅ አርም">
                  ✏️ ሰዓት
                </button>
              </div>
            </td>"""

    assert old_buttons_block in text, 'old_buttons_block not found'
    text = text.replace(old_buttons_block, new_buttons_block)

    # 4. Add handleStaffAttDropdownChange function right after quickMarkStaffAttendanceWeb
    old_quick_mark = """      saveStaffAttendanceWeb();
      renderStaffAttendanceWeb();
    }"""

    new_quick_mark = """      saveStaffAttendanceWeb();
      renderStaffAttendanceWeb();
    }

    function handleStaffAttDropdownChange(id, val) {
      if (val === 'edit_time') {
        openStaffAttEditModal(id);
        renderStaffAttendanceWeb();
      } else {
        quickMarkStaffAttendanceWeb(id, val);
      }
    }"""

    assert old_quick_mark in text, 'old_quick_mark not found'
    text = text.replace(old_quick_mark, new_quick_mark)

    for p in ['public/index.html', 'docs/index.html', 'index.html']:
        with open(p, 'w', encoding='utf-8') as out:
            out.write(text)
        print(f'Successfully updated {p}')

if __name__ == '__main__':
    main()
