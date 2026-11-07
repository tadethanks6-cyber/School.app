import re
import sys

def main():
    with open('public/index.html', 'r', encoding='utf-8') as f:
        html = f.read()

    # 1. Update Staff initialization block (around lines 4495-4530)
    old_staff_init = '''    // 2. STAFF: Strictly ONLY user/school-registered staff (all dummy template staff 100% purged)
    let userStaff = [];
    try {
      userStaff = JSON.parse(localStorage.getItem('dilijent_user_registered_staff') || '[]');
      if (!Array.isArray(userStaff)) userStaff = [];
    } catch (e) {
      userStaff = [];
    }

    // Exclude any staff in deletedStaffIds
    userStaff = userStaff.filter(us => {
      if (!us) return false;
      const sid = (us.id || '').trim().toLowerCase();
      const sname = (us.name || '').trim().toLowerCase();
      const stripped = normalizeStaffName(us.name);
      return !deletedStaffIds.has(sid) && !deletedStaffIds.has(sname) && (!stripped || !deletedStaffIds.has(stripped));
    });

    // Deduplicate userStaff: at most one registration per person in the SAME work sector
    const seenStaffSectors = new Set();
    const cleanStaffList = [];
    userStaff.forEach(st => {
      const norm = normalizeStaffName(st.name);
      const sector = getStaffSector(st.role, st.dept);
      const key = `${norm}__${sector}`;
      if (!norm) return;
      if (seenStaffSectors.has(key)) return;
      seenStaffSectors.add(key);
      st.isUserCreated = true;
      cleanStaffList.push(st);
    });

    staffList = cleanStaffList;
    localStorage.setItem('dilijent_user_registered_staff', JSON.stringify(staffList));
    localStorage.setItem('dilijent_staff_list', JSON.stringify(staffList));'''

    new_staff_init = '''    // 2. STAFF: Strictly ONLY user/school-registered staff (all dummy template staff 100% purged)
    const DUMMY_TEMPLATE_STAFF_NAMES = [
      'ዶ/ር ተስፋዬ ገብረመድህን', 'ተስፋዬ ገብረመድህን',
      'ወ/ሮ መስከረም በቀለ', 'መስከረም በቀለ',
      'ወ/ሪት ትዕግስት ወርቁ', 'ትዕግስት ወርቁ', 'ትእግስት ወርቁ',
      'አቶ ዳዊት ብርሃኑ', 'ዳዊት ብርሃኑ',
      'አቶ ግርማ አየለ', 'ግርማ አየለ',
      'ወ/ሮ ዓለሚቱ ታደሰ', 'ዓለሚቱ ታደሰ', 'አለሚቱ ታደሰ',
      'አቶ በላይነህ ጥላሁን', 'በላይነህ ጥላሁን',
      'አቶ ሙሉጌታ ታከለ', 'ሙሉጌታ ታከለ',
      'ወ/ሮ ትእግስት አስፋው', 'ትእግስት አስፋው', 'ትዕግስት አስፋው',
      'አቶ ቴዎድሮስ ግርማ', 'ቴዎድሮስ ግርማ',
      'ወ/ሪት ቤተልሔም ወርቁ', 'ቤተልሔም ወርቁ',
      'አቶ ደረጀ በቀለ', 'ደረጀ በቀለ',
      'አቶ ግርማቸው አሰፋ', 'ግርማቸው አሰፋ',
      'ወ/ሮ ሰላማዊት አበራ', 'ሰላማዊት አበራ',
      'አቶ በየነ ኃይሉ', 'በየነ ኃይሉ',
      'ወ/ሪት ትዕግስት ወርቄ', 'ትዕግስት ወርቄ',
      'አቶ ከበደ ለማ', 'ከበደ ለማ',
      'ወ/ሪት ሰላማዊት አበበ', 'ሰላማዊት አበበ',
      'አቶ ዮናስ ብርሃኑ', 'ዮናስ ብርሃኑ',
      'አቶ ሙሉጌታ ከበደ', 'ሙሉጌታ ከበደ',
      'አቶ ዮሐንስ ታደሰ', 'ዮሐንስ ታደሰ',
      'ወ/ሮ ብርቱካን በቀለ', 'ብርቱካን በቀለ',
      'ተማሪ አቤል ታደሰ', 'አቤል ታደሰ'
    ];
    const DUMMY_TEMPLATE_STAFF_CODES = [
      'adm-001', 'adm-002', 'adm-003', 'adm-004', 'adm-005', 'adm-006', 'adm-007',
      't-101', 't-102', 't-103', 't-104', 't-201', 't-202', 't-203', 't-204', 't-205'
    ];
    // Permanently add all dummy template names and codes to deletedStaffIds
    DUMMY_TEMPLATE_STAFF_NAMES.forEach(n => {
      deletedStaffIds.add(n.toLowerCase());
      const norm = normalizeStaffName(n);
      if (norm) deletedStaffIds.add(norm);
    });
    DUMMY_TEMPLATE_STAFF_CODES.forEach(c => deletedStaffIds.add(c));
    localStorage.setItem('dilijent_deleted_staff_ids', JSON.stringify([...deletedStaffIds]));

    // Load from BOTH dilijent_user_registered_staff AND dilijent_staff_list to ensure newly registered staff are never lost
    let storedUserStaff = [];
    try {
      storedUserStaff = JSON.parse(localStorage.getItem('dilijent_user_registered_staff') || '[]');
      if (!Array.isArray(storedUserStaff)) storedUserStaff = [];
    } catch (e) { storedUserStaff = []; }

    let storedStaffList = [];
    try {
      storedStaffList = JSON.parse(localStorage.getItem('dilijent_staff_list') || '[]');
      if (!Array.isArray(storedStaffList)) storedStaffList = [];
    } catch (e) { storedStaffList = []; }

    const combinedCandidateStaff = [...storedUserStaff, ...storedStaffList];
    const dummyStaffNameSet = new Set(DUMMY_TEMPLATE_STAFF_NAMES.map(n => normalizeStaffName(n)));
    const dummyStaffCodeSet = new Set(DUMMY_TEMPLATE_STAFF_CODES);

    const cleanStaffList = [];
    const seenStaffKeys = new Set();

    combinedCandidateStaff.forEach(st => {
      if (!st || !st.name) return;
      const sId = (st.id || '').trim().toLowerCase();
      const sName = (st.name || '').trim().toLowerCase();
      const sNorm = normalizeStaffName(st.name);
      const sPhone = (st.phone || '').trim().toLowerCase();

      // Check if blocked by deletedStaffIds
      if (deletedStaffIds.has(sId) || deletedStaffIds.has(sName) || (sNorm && deletedStaffIds.has(sNorm)) || (sPhone && deletedStaffIds.has(sPhone))) {
        return;
      }
      // Check if this is a dummy template code or name
      if (dummyStaffCodeSet.has(sId) || dummyStaffNameSet.has(sNorm)) {
        return;
      }

      const dedupeKey = `${sNorm || sName}__${getStaffSector(st.role, st.dept)}`;
      if (seenStaffKeys.has(dedupeKey)) return;
      seenStaffKeys.add(dedupeKey);

      st.isUserCreated = true;
      cleanStaffList.push(st);
    });

    // Ensure Sara Niguse (School Secretary) is always present unless explicitly deleted
    const saraNorm = normalizeStaffName('Sara Niguse');
    if (!deletedStaffIds.has(saraNorm) && !deletedStaffIds.has('0960225925') && !deletedStaffIds.has('stf-sec-01')) {
      const hasSara = cleanStaffList.some(st => normalizeStaffName(st.name) === saraNorm || st.phone === '0960225925');
      if (!hasSara) {
        cleanStaffList.push({
          id: 'STF-SEC-01',
          name: 'Sara Niguse',
          role: 'ዋና ጸሐፊ (School Secretary)',
          dept: 'አስተዳደር እና ፋይናንስ (Administration)',
          phone: '0960225925',
          qual: 'ዲፕሎማ / ቢኤ (BA/Diploma)',
          photo: '',
          status: 'ንቁ',
          isUserCreated: true
        });
      }
    }

    staffList = cleanStaffList;
    localStorage.setItem('dilijent_user_registered_staff', JSON.stringify(staffList));
    localStorage.setItem('dilijent_staff_list', JSON.stringify(staffList));'''

    assert old_staff_init in html, "Could not find old_staff_init in public/index.html"
    html = html.replace(old_staff_init, new_staff_init, 1)

    # 2. Update DEFAULT_ACCOUNTS and userAccounts filtering (around lines 6563-6590)
    old_accounts_block = '''    const DEFAULT_ACCOUNTS = [
      { name: 'እኔ (ታደሰ ምስጋናው - ሲስተም አድሚን)', role: '👑 ሲስተም አድሚን', email: 'tadessemisganaw4@gmail.com', phone: '0911999999', pass: 'tade@2026', isAdmin: true, isVicePrincipal: false, roleKey: 'admin' },
      { name: 'Sara Niguse', role: '✍️ ዋና ጸሐፊ / ሬጅስትራር', email: 'sara.niguse@dilijent.edu.et', phone: '0960225925', pass: 'sara@123', isAdmin: false, isVicePrincipal: false, roleKey: 'secretary' },
      { name: 'አቶ ሙሉጌታ ከበደ', role: '👔 ርዕሰ መምህር', email: 'principal@dilijent.edu.et', phone: '0911000000', pass: 'Principal@26', isAdmin: false, isVicePrincipal: false, roleKey: 'principal' },
      { name: 'አቶ ከበደ ለማ', role: '🎓 ምክትል ርዕሰ መምህር', email: 'vp@dilijent.edu.et', phone: '0918000000', pass: 'VicePrin@26', isAdmin: false, isVicePrincipal: true, roleKey: 'vice_principal' },
      { name: 'አቶ ዮሐንስ ታደሰ', role: '💰 ፋይናንስ / ሂሳብ ሹም', email: 'finance@dilijent.edu.et', phone: '0911446688', pass: 'Finance@2026', isAdmin: false, isVicePrincipal: false, roleKey: 'finance' },
      { name: 'ወ/ሮ ብርቱካን በቀለ', role: '🧹 ፅዳት እና ጥበቃ (ድጋፍ ሰጪ)', email: '', phone: '0911557799', pass: 'Support@2026', isAdmin: false, isVicePrincipal: false, roleKey: 'support' },
      { name: 'ተማሪ አቤል ታደሰ', role: '👨‍🎓 ተማሪ (12A)', email: '', phone: '0911002233', pass: 'Student@2026', isAdmin: false, isVicePrincipal: false, roleKey: 'student' }
    ];

    let userAccounts = [];
    try {
      userAccounts = JSON.parse(localStorage.getItem('dilijent_user_accounts')) || [];
    } catch (e) {
      userAccounts = [];
    }
    if (!Array.isArray(userAccounts) || userAccounts.length === 0) {
      userAccounts = [...DEFAULT_ACCOUNTS];
    }
    // Filter out any permanently deleted accounts
    userAccounts = userAccounts.filter(acc => {
      const aname = (acc.name || '').trim().toLowerCase();
      const aphone = (acc.phone || '').trim().toLowerCase();
      const stripped = aname.replace(/^(መምህር|መምህርት|አቶ|ወ\/ሮ|ወ\/ሪት|ዶ\/ር)\s+/g, '').trim().toLowerCase();
      return !deletedStaffIds.has(aname) && !deletedStaffIds.has(aphone) && (!stripped || !deletedStaffIds.has(stripped));
    });'''

    new_accounts_block = '''    const DEFAULT_ACCOUNTS = [
      { name: 'እኔ (ታደሰ ምስጋናው - ሲስተም አድሚን)', role: '👑 ሲስተም አድሚን', email: 'tadessemisganaw4@gmail.com', phone: '0911999999', pass: 'tade@2026', isAdmin: true, isVicePrincipal: false, roleKey: 'admin' },
      { name: 'Sara Niguse', role: '✍️ ዋና ጸሐፊ / ሬጅስትራር', email: 'sara.niguse@dilijent.edu.et', phone: '0960225925', pass: 'sara@123', isAdmin: false, isVicePrincipal: false, roleKey: 'secretary' }
    ];

    let userAccounts = [];
    try {
      userAccounts = JSON.parse(localStorage.getItem('dilijent_user_accounts')) || [];
    } catch (e) {
      userAccounts = [];
    }
    if (!Array.isArray(userAccounts) || userAccounts.length === 0) {
      userAccounts = [...DEFAULT_ACCOUNTS];
    }
    // Filter out any permanently deleted accounts or legacy dummy accounts
    const legacyDummyNamesSet = new Set([
      'ዶ/ር ተስፋዬ ገብረመድህን', 'ወ/ሮ መስከረም በቀለ', 'ወ/ሪት ትዕግስት ወርቁ', 'አቶ ዳዊት ብርሃኑ',
      'አቶ ግርማ አየለ', 'ወ/ሮ ዓለሚቱ ታደሰ', 'አቶ በላይነህ ጥላሁን', 'አቶ ሙሉጌታ ታከለ',
      'ወ/ሮ ትእግስት አስፋው', 'አቶ ቴዎድሮስ ግርማ', 'ወ/ሪት ቤተልሔም ወርቁ', 'አቶ ደረጀ በቀለ',
      'አቶ ግርማቸው አሰፋ', 'ወ/ሮ ሰላማዊት አበራ', 'አቶ በየነ ኃይሉ', 'ወ/ሪት ትዕግስት ወርቄ',
      'አቶ ከበደ ለማ', 'ወ/ሪት ሰላማዊት አበበ', 'አቶ ዮናስ ብርሃኑ', 'አቶ ሙሉጌታ ከበደ',
      'አቶ ዮሐንስ ታደሰ', 'ወ/ሮ ብርቱካን በቀለ', 'ተማሪ አቤል ታደሰ'
    ].map(n => normalizeStaffName(n)));

    userAccounts = userAccounts.filter(acc => {
      if (!acc) return false;
      const aname = (acc.name || '').trim().toLowerCase();
      const aphone = (acc.phone || '').trim().toLowerCase();
      const stripped = aname.replace(/^(መምህር|መምህርት|አቶ|ወ\/ሮ|ወ\/ሪት|ዶ\/ር)\s+/g, '').trim().toLowerCase();
      const norm = normalizeStaffName(acc.name);
      if (deletedStaffIds.has(aname) || deletedStaffIds.has(aphone) || (stripped && deletedStaffIds.has(stripped)) || (norm && deletedStaffIds.has(norm))) {
        return false;
      }
      if (legacyDummyNamesSet.has(norm)) {
        return false;
      }
      return true;
    });'''

    assert old_accounts_block in html, "Could not find old_accounts_block in public/index.html"
    html = html.replace(old_accounts_block, new_accounts_block, 1)

    # 3. Update removeStaff to ensure thorough purging across all arrays and storage keys
    old_remove_staff = '''      if (confirm(`ሰራተኛ/መምህር ${staffName} (${targetId}) ከዳታቤዝ በቋሚነት እንዲሰረዝ ይፈልጋሉ?`)) {
        if (targetId) deletedStaffIds.add(targetId.toLowerCase());
        if (staffName && staffName !== 'ይህን ሰራተኛ') {
          deletedStaffIds.add(staffName.toLowerCase());
          const stripped = staffName.replace(/^(መምህር|መምህርት|አቶ|ወ\/ሮ|ወ\/ሪት|ዶ\/ር)\s+/g, '').trim().toLowerCase();
          if (stripped) deletedStaffIds.add(stripped);
        }
        if (targetStaff && targetStaff.phone) {
          deletedStaffIds.add(targetStaff.phone.trim().toLowerCase());
        }
        localStorage.setItem('dilijent_deleted_staff_ids', JSON.stringify([...deletedStaffIds]));

        // 1. Remove from staffList
        staffList = staffList.filter(st => {
          const sid = (st.id || '').trim().toLowerCase();
          const sname = (st.name || '').trim().toLowerCase();
          const stripped = sname.replace(/^(መምህር|መምህርት|አቶ|ወ\/ሮ|ወ\/ሪት|ዶ\/ር)\s+/g, '').trim().toLowerCase();
          return sid !== targetId.toLowerCase() && sname !== staffName.toLowerCase() && (!stripped || stripped !== staffName.toLowerCase());
        });

        // 2. Remove from permanent user-registered staff backup
        try {
          let userStaff = JSON.parse(localStorage.getItem('dilijent_user_registered_staff') || '[]');
          userStaff = userStaff.filter(us => {
            const usId = (us.id || '').trim().toLowerCase();
            const usName = (us.name || '').trim().toLowerCase();
            return usId !== targetId.toLowerCase() && usName !== staffName.toLowerCase();
          });
          localStorage.setItem('dilijent_user_registered_staff', JSON.stringify(userStaff));
        } catch (e) {}

        // 3. Remove from staffAttendanceListWeb
        staffAttendanceListWeb = staffAttendanceListWeb.filter(sa => {
          const saId = String(sa.id || '').trim().toLowerCase();
          const saName = (sa.name || '').trim().toLowerCase();
          return saId !== targetId.toLowerCase() && saName !== staffName.toLowerCase();
        });
        saveStaffAttendanceWeb();

        // 4. Remove from userAccounts
        userAccounts = userAccounts.filter(acc => {
          const aname = (acc.name || '').trim().toLowerCase();
          const aphone = (acc.phone || '').trim().toLowerCase();
          return aname !== staffName.toLowerCase() && (!targetStaff || !targetStaff.phone || aphone !== targetStaff.phone.trim().toLowerCase());
        });
        localStorage.setItem('dilijent_user_accounts', JSON.stringify(userAccounts));

        saveState();
        if (typeof renderStaff === 'function') renderStaff();
        if (typeof renderStaffAttendanceWeb === 'function') renderStaffAttendanceWeb();
        alert(`🗑️ ሰራተኛ ${staffName} ከዳታቤዝ በቋሚነት ተሰርዟል! ወደፊትም በጭራሽ አይመለስም።`);
      }'''

    new_remove_staff = '''      if (confirm(`ሰራተኛ/መምህር ${staffName} (${targetId}) ከዳታቤዝ በቋሚነት እንዲሰረዝ ይፈልጋሉ?`)) {
        const normTarget = normalizeStaffName(staffName);
        if (targetId) deletedStaffIds.add(targetId.toLowerCase());
        if (staffName && staffName !== 'ይህን ሰራተኛ') {
          deletedStaffIds.add(staffName.toLowerCase());
          const stripped = staffName.replace(/^(መምህር|መምህርት|አቶ|ወ\/ሮ|ወ\/ሪት|ዶ\/ር)\s+/g, '').trim().toLowerCase();
          if (stripped) deletedStaffIds.add(stripped);
          if (normTarget) deletedStaffIds.add(normTarget);
        }
        if (targetStaff && targetStaff.phone) {
          deletedStaffIds.add(targetStaff.phone.trim().toLowerCase());
        }
        localStorage.setItem('dilijent_deleted_staff_ids', JSON.stringify([...deletedStaffIds]));

        // 1. Remove from staffList
        staffList = staffList.filter(st => {
          const sid = (st.id || '').trim().toLowerCase();
          const sname = (st.name || '').trim().toLowerCase();
          const sNorm = normalizeStaffName(st.name);
          return sid !== targetId.toLowerCase() && sname !== staffName.toLowerCase() && (!sNorm || sNorm !== normTarget);
        });
        localStorage.setItem('dilijent_staff_list', JSON.stringify(staffList));

        // 2. Remove from permanent user-registered staff backup
        try {
          let userStaff = JSON.parse(localStorage.getItem('dilijent_user_registered_staff') || '[]');
          userStaff = userStaff.filter(us => {
            const usId = (us.id || '').trim().toLowerCase();
            const usName = (us.name || '').trim().toLowerCase();
            const usNorm = normalizeStaffName(us.name);
            return usId !== targetId.toLowerCase() && usName !== staffName.toLowerCase() && (!usNorm || usNorm !== normTarget);
          });
          localStorage.setItem('dilijent_user_registered_staff', JSON.stringify(userStaff));
        } catch (e) {}

        // 3. Remove from staffAttendanceListWeb
        staffAttendanceListWeb = staffAttendanceListWeb.filter(sa => {
          const saId = String(sa.id || '').trim().toLowerCase();
          const saName = (sa.name || '').trim().toLowerCase();
          const saNorm = normalizeStaffName(sa.name);
          return saId !== targetId.toLowerCase() && saName !== staffName.toLowerCase() && (!saNorm || saNorm !== normTarget);
        });
        saveStaffAttendanceWeb();

        // 4. Remove from userAccounts
        userAccounts = userAccounts.filter(acc => {
          const aname = (acc.name || '').trim().toLowerCase();
          const aphone = (acc.phone || '').trim().toLowerCase();
          const aNorm = normalizeStaffName(acc.name);
          return aname !== staffName.toLowerCase() && (!aNorm || aNorm !== normTarget) && (!targetStaff || !targetStaff.phone || aphone !== targetStaff.phone.trim().toLowerCase());
        });
        localStorage.setItem('dilijent_user_accounts', JSON.stringify(userAccounts));

        saveState();
        if (typeof renderStaff === 'function') renderStaff();
        if (typeof renderStaffAttendanceWeb === 'function') renderStaffAttendanceWeb();
        alert(`🗑️ ሰራተኛ ${staffName} ከዳታቤዝ በቋሚነት ተሰርዟል! ወደፊትም በጭራሽ አይመለስም።`);
      }'''

    assert old_remove_staff in html, "Could not find old_remove_staff in public/index.html"
    html = html.replace(old_remove_staff, new_remove_staff, 1)

    # 4. In saveNewStaff, ensure immediate persistence to both dilijent_staff_list and dilijent_user_registered_staff
    old_save_staff_sync = '''      // 1. Add to main staffList
      staffList.unshift(newStaffObj);

      // 2. Save to permanent user-registered staff backup in localStorage
      let userStaff = [];
      try {
        userStaff = JSON.parse(localStorage.getItem('dilijent_user_registered_staff') || '[]');
      } catch (err) { userStaff = []; }
      userStaff = userStaff.filter(us => (us.id || '').toLowerCase() !== id.toLowerCase() && (us.name || '').trim().toLowerCase() !== name.toLowerCase());
      userStaff.unshift(newStaffObj);
      localStorage.setItem('dilijent_user_registered_staff', JSON.stringify(userStaff));'''

    new_save_staff_sync = '''      // 1. Add to main staffList & dilijent_staff_list
      staffList.unshift(newStaffObj);
      localStorage.setItem('dilijent_staff_list', JSON.stringify(staffList));

      // 2. Save to permanent user-registered staff backup in localStorage
      let userStaff = [];
      try {
        userStaff = JSON.parse(localStorage.getItem('dilijent_user_registered_staff') || '[]');
      } catch (err) { userStaff = []; }
      userStaff = userStaff.filter(us => (us.id || '').toLowerCase() !== id.toLowerCase() && (us.name || '').trim().toLowerCase() !== name.toLowerCase() && normalizeStaffName(us.name) !== normStaffName);
      userStaff.unshift(newStaffObj);
      localStorage.setItem('dilijent_user_registered_staff', JSON.stringify(userStaff));'''

    assert old_save_staff_sync in html, "Could not find old_save_staff_sync in public/index.html"
    html = html.replace(old_save_staff_sync, new_save_staff_sync, 1)

    # 5. Write to public/index.html, docs/index.html, index.html
    for dest in ['public/index.html', 'docs/index.html', 'index.html']:
        with open(dest, 'w', encoding='utf-8') as out:
            out.write(html)
        print(f"Updated {dest} successfully.")

if __name__ == '__main__':
    main()
