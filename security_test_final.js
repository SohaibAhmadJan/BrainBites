const {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} = require('@firebase/rules-unit-testing');
const fs = require('fs');

async function runTests() {
  const rules = fs.readFileSync('firestore.rules', 'utf8');
  const testEnv = await initializeTestEnvironment({
    projectId: 'brainbites-backend-test',
    firestore: { rules, host: 'localhost', port: 8080 },
  });

  console.log('--- STARTING FINAL SECURITY VALIDATION ---');

  const alice = testEnv.authenticatedContext('alice');
  const bob = testEnv.authenticatedContext('bob');
  const unauth = testEnv.unauthenticatedContext();

  // Setup data
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection('users').doc('alice').set({
      account: { uid: 'alice', status: 'ACTIVE' },
      stats: { streakCount: 5, factsReadCount: 10 }
    });
    await db.collection('users').doc('disabled_user').set({
      account: { uid: 'disabled_user', status: 'DISABLED' }
    });
    await db.collection('admins').doc('super_admin').set({
      role: 'SUPER_ADMIN', isActive: true, permissions: []
    });
  });

  const superAdmin = testEnv.authenticatedContext('super_admin');

  try {
    // Test 1: Unauthenticated
    console.log('Test 1: Unauthenticated read fact...');
    await assertSucceeds(unauth.firestore().collection('facts').doc('1').get());
    console.log('Test 1.1: Unauthenticated write fact...');
    await assertFails(unauth.firestore().collection('facts').doc('1').set({ fact: 'bad' }));

    // Test 2: Cross-user access
    console.log('Test 2: Bob accessing Alice\'s subcollection...');
    await assertFails(bob.firestore().collection('users').doc('alice').collection('favorites').doc('1').get());

    // Test 3: Disabled user
    console.log('Test 3: Disabled user attempting write...');
    const disabledCtx = testEnv.authenticatedContext('disabled_user');
    await assertFails(disabledCtx.firestore().collection('users').doc('disabled_user').collection('favorites').doc('1').set({ addedAt: 123 }));

    // Test 4: Account integrity
    console.log('Test 4: Alice changing status to DISABLED...');
    await assertFails(alice.firestore().collection('users').doc('alice').update({ 'account.status': 'DISABLED' }));

    // Test 5: Stat throttling
    console.log('Test 5: Alice jumping streak +50...');
    await assertFails(alice.firestore().collection('users').doc('alice').update({ 'stats.streakCount': 55 }));
    console.log('Test 5.1: Alice incrementing streak +1...');
    await assertSucceeds(alice.firestore().collection('users').doc('alice').update({ 'stats.streakCount': 6 }));

    // Test 6: API-only content write
    console.log('Test 6: SUPER_ADMIN writing directly to Facts via Client SDK...');
    await assertFails(superAdmin.firestore().collection('facts').doc('1').update({ fact: 'New fact' }));

    // Test 7: Analytics Integrity
    console.log('Test 7: Alice forging analytics UID...');
    await assertFails(alice.firestore().collection('analytics_events').doc().set({ name: 'view', uid: 'bob', timestamp: Date.now() }));

    console.log('--- ALL TEST SCENARIOS PASSED ---');
  } finally {
    await testEnv.cleanup();
  }
}

runTests().catch(err => {
    console.error('Test Suite Failed:', err);
    process.exit(1);
});
