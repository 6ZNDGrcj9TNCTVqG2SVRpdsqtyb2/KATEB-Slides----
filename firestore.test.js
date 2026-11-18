const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_101";
const BOB_UID = "bob_uid_202";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validPresentationPayload(uid, overrides = {}) {
  const past = new Date(Date.now() - 5000);
  return {
    userId: uid,
    title: "Q3 Strategy Deck",
    description: "Quarterly business roadmap",
    languageCode: "AR",
    aspectRatioId: "16:9",
    themeId: "royal_ink",
    sourceType: "MANUAL",
    slideCount: 6,
    folderTag: "work",
    locked: false,
    deleted: false,
    payloadJson: "{\"slides\":[]}",
    createdAt: past,
    updatedAt: past,
    ...overrides,
  };
}

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

test("Unauthenticated user cannot read, list, create, or delete presentations", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const ref = unauthDb.collection("users").doc(ALICE_UID).collection("presentations");
  await assertFails(ref.get());
  await assertFails(ref.doc("deck_1").get());
  await assertFails(ref.doc("deck_1").set(validPresentationPayload(ALICE_UID)));
  await assertFails(ref.doc("deck_1").delete());
});

test("Authenticated owner can create, read, list, update, and delete own presentation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const col = aliceDb.collection("users").doc(ALICE_UID).collection("presentations");
  const payload = validPresentationPayload(ALICE_UID);

  await assertSucceeds(col.doc("deck_alpha").set(payload));
  await assertSucceeds(col.doc("deck_alpha").get());
  await assertSucceeds(col.get());

  await assertSucceeds(
    col.doc("deck_alpha").update({
      title: "Updated Q3 Strategy Deck",
      slideCount: 8,
      locked: true,
      folderTag: "board",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertSucceeds(col.doc("deck_alpha").delete());
});

test("Cross-user access to another user's presentations is strictly denied", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("users")
      .doc(BOB_UID)
      .collection("presentations")
      .doc("bob_deck")
      .set(validPresentationPayload(BOB_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobCol = aliceDb.collection("users").doc(BOB_UID).collection("presentations");
  await assertFails(bobCol.get());
  await assertFails(bobCol.doc("bob_deck").get());
  await assertFails(bobCol.doc("bob_deck").update({ title: "Hijacked" }));
  await assertFails(bobCol.doc("bob_deck").delete());
  await assertFails(bobCol.doc("alice_injected").set(validPresentationPayload(ALICE_UID)));
});

test("Ghost fields, @DocumentId 'id', bad languageCode, future timestamps, and createdAt mutation are rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const col = aliceDb.collection("users").doc(ALICE_UID).collection("presentations");

  // Ghost field / @DocumentId id field
  await assertFails(
    col.doc("deck_bad_1").set(validPresentationPayload(ALICE_UID, { id: "deck_bad_1" }))
  );

  // Invalid language code
  await assertFails(
    col.doc("deck_bad_2").set(validPresentationPayload(ALICE_UID, { languageCode: "FR" }))
  );

  // Future timestamp
  await assertFails(
    col.doc("deck_bad_3").set(validPresentationPayload(ALICE_UID, { createdAt: new Date(Date.now() + 60000) }))
  );

  // Invalid userId spoofing
  await assertFails(
    col.doc("deck_bad_4").set(validPresentationPayload(BOB_UID))
  );

  // Valid create, then immutable createdAt mutation attempt
  await assertSucceeds(col.doc("deck_good").set(validPresentationPayload(ALICE_UID)));
  await assertFails(
    col.doc("deck_good").update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    col.doc("deck_good").update({
      userId: BOB_UID,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});
