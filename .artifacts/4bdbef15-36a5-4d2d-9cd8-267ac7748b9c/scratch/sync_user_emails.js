const { initializeApp, cert } = require('firebase-admin/app');
const { getAuth } = require('firebase-admin/auth');
const { getFirestore } = require('firebase-admin/firestore');
const serviceAccount = require('F:/BrainBites/serviceAccountKey.json');

initializeApp({
    credential: cert(serviceAccount)
});

const auth = getAuth();
const db = getFirestore();

async function sync() {
  const listUsersResult = await auth.listUsers();
  console.log(`Checking ${listUsersResult.users.length} users...`);

  for (const userRecord of listUsersResult.users) {
    if (!userRecord.email) {
        console.log(` - User ${userRecord.uid} has no email in Auth, skipping.`);
        continue;
    }

    const userDoc = await db.collection('users').doc(userRecord.uid).get();
    if (userDoc.exists) {
        const data = userDoc.data();
        if (!data.profile?.email) {
            console.log(` - Updating missing email for ${userRecord.uid}: ${userRecord.email}`);
            await db.collection('users').doc(userRecord.uid).update({
                'profile.email': userRecord.email,
                'profile.displayName': data.profile?.displayName || userRecord.displayName || 'Knowledge Seeker',
                'updatedAt': Date.now()
            });
        } else {
            console.log(` - User ${userRecord.uid} already has email: ${data.profile.email}`);
        }
    } else {
        console.log(` - User ${userRecord.uid} doc not found in Firestore.`);
    }
  }
  console.log('Sync complete.');
}

sync().catch(console.error);
