const { initializeApp, cert } = require('firebase-admin/app');
const { getAuth } = require('firebase-admin/auth');
const serviceAccount = require('F:/BrainBites/serviceAccountKey.json');

initializeApp({
    credential: cert(serviceAccount)
});

const auth = getAuth();

async function listAuthUsers() {
  const listUsersResult = await auth.listUsers(10);
  listUsersResult.users.forEach((userRecord) => {
    console.log('User ID:', userRecord.uid);
    console.log('Email:', userRecord.email);
    console.log('DisplayName:', userRecord.displayName);
    console.log('Providers:', userRecord.providerData.map(p => p.providerId));
    console.log('---');
  });
}

listAuthUsers().catch(console.error);
