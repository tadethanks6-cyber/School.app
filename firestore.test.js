const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read students", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("students").get());
});

test("Authenticated user: cannot read another user's student", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("students").doc("bob_student").set({
      studentIdNumber: "ET-001",
      fullName: "Bob Student",
      gender: "ወንድ",
      age: 15,
      grade: "10ኛ",
      section: "A",
      parentName: "Parent Bob",
      parentPhone: "0911223344",
      userId: BOB_UID,
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("students").doc("bob_student").get());
});

test("Authenticated user: can create and read their own student", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("students").doc("alice_student").set({
      studentIdNumber: "ET-002",
      fullName: "Alice Student",
      gender: "ሴት",
      age: 16,
      grade: "10ኛ",
      section: "B",
      parentName: "Parent Alice",
      parentPhone: "0911998877",
      userId: ALICE_UID,
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("students").where("userId", "==", ALICE_UID).get()
  );
});
