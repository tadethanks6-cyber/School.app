const fs = require('fs');

const html = fs.readFileSync('public/index.html', 'utf-8');

// Extract script content
const scriptMatch = html.match(/<script(?![^>]*src=)>([\s\S]*?)<\/script>/g);
if (!scriptMatch) {
  console.error("No script blocks found!");
  process.exit(1);
}

// Mock browser environment
const localStorageData = {};
global.localStorage = {
  getItem: (k) => localStorageData[k] || null,
  setItem: (k, v) => { localStorageData[k] = String(v); },
  removeItem: (k) => { delete localStorageData[k]; },
  clear: () => { Object.keys(localStorageData).forEach(k => delete localStorageData[k]); }
};

global.document = {
  getElementById: (id) => ({
    value: '',
    innerText: '',
    innerHTML: '',
    style: {},
    classList: { add: () => {}, remove: () => {} },
    focus: () => {},
    options: [{ text: 'መምህር' }],
    selectedIndex: 0
  }),
  querySelectorAll: () => [],
  addEventListener: () => {}
};

global.window = {
  scrollTo: () => {},
  addEventListener: () => {}
};
global.alert = (msg) => { console.log("[TEST ALERT]", msg.split('\n')[0]); };
global.confirm = () => true;

// Evaluate script
try {
  // Join the inline scripts and execute
  const allScript = scriptMatch.map(s => s.replace(/<\/?script[^>]*>/g, '')).join('\n');
  eval(allScript);
  console.log("Scripts evaluated successfully with 0 syntax errors!");

  // Verify staffList contains ONLY clean/user staff
  console.log("Initial staff count:", staffList.length);
  const dummyNames = [
    'አቶ ሙሉጌታ ታከለ', 'ወ/ሮ ትእግስት አስፋው', 'ዶ/ር ተስፋዬ ገብረመድህን',
    'አቶ ግርማቸው አሰፋ', 'ወ/ሮ ሰላማዊት አበራ', 'አቶ ሙሉጌታ ከበደ'
  ];
  const foundDummy = staffList.filter(s => dummyNames.includes(s.name));
  console.log("Found dummy staff in staffList:", foundDummy.length);
  if (foundDummy.length > 0) {
    console.error("FAIL: Dummy staff still present in staffList!", foundDummy);
    process.exit(1);
  }

  // Verify DEFAULT_ACCOUNTS
  console.log("DEFAULT_ACCOUNTS count:", DEFAULT_ACCOUNTS.length);
  if (DEFAULT_ACCOUNTS.length !== 2) {
    console.error("FAIL: DEFAULT_ACCOUNTS should have exactly 2 accounts (Admin & Sara Niguse), found:", DEFAULT_ACCOUNTS.length);
    process.exit(1);
  }

  console.log("ALL TESTS PASSED WITH 0 ERRORS!");
} catch (e) {
  console.error("Test evaluation error:", e);
  process.exit(1);
}
