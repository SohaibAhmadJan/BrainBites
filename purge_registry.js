/**
 * BrainBites Maintenance: Registry Purge Protocol
 * Performs a deep cleanup of orphaned Firebase Auth accounts that lack a corresponding
 * administrative role in Firestore. This provides a "Full Wipe" experience on the Free Plan.
 */

const { initializeApp, cert } = require('firebase-admin/app');
const { getAuth } = require('firebase-admin/auth');
const { getFirestore } = require('firebase-admin/firestore');
const readline = require('readline');

// 1. Initialize System Identity
const serviceAccount = require('./serviceAccountKey.json');

initializeApp({
  credential: cert(serviceAccount)
});

const auth = getAuth();
const db = getFirestore();

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

async function runPurge() {
  console.log('\n--- BRAINBITES SECURITY PROTOCOL: REGISTRY PURGE ---\n');

  try {
    // 2. Fetch Authoritative Registry (Firestore)
    console.log('Synchronizing Administrative Registry...');
    const adminSnap = await db.collection('admins').get();
    const adminUids = new Set(adminSnap.docs.map(doc => doc.id));

    // Also fetch regular users to avoid accidental deletion of app customers
    console.log('Synchronizing User Registry...');
    const userSnap = await db.collection('users').get();
    const userUids = new Set(userSnap.docs.map(doc => doc.id));

    // 3. Scan Authentication Identity Stream
    console.log('Scanning Authentication Stream...');
    const listUsersResult = await auth.listUsers(1000);
    const authUsers = listUsersResult.users;

    const orphans = authUsers.filter(user => !adminUids.has(user.uid) && !userUids.has(user.uid));

    if (orphans.length === 0) {
      console.log('\nSUCCESS: Authentication stream is fully synchronized. No orphans detected.');
      process.exit(0);
    }

    console.log(`\nDETECTED: ${orphans.length} Orphaned Identifiers (Auth accounts with no Firestore record).`);
    orphans.forEach(u => console.log(` - ${u.email} (${u.uid})`));

    // 4. Confirmation Handshake
    rl.question('\nCAUTION: These accounts will be permanently expunged from Firebase Auth. Proceed? (y/n): ', async (answer) => {
      if (answer.toLowerCase() === 'y') {
        console.log('\nInitiating Purge Sequence...');

        for (const user of orphans) {
          try {
            await auth.deleteUser(user.uid);
            console.log(`[CLEANSED] ${user.email}`);
          } catch (e) {
            console.error(`[FAILURE] Failed to expunge ${user.email}: ${e.message}`);
          }
        }

        console.log('\nPURGE COMPLETE: Administrative environment is now clean.');
      } else {
        console.log('\nAborted: No changes were made.');
      }
      rl.close();
      process.exit(0);
    });

  } catch (error) {
    console.error('\nCRITICAL PROTOCOL FAILURE:', error);
    process.exit(1);
  }
}

runPurge();
