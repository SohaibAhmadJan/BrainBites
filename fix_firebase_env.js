const { initializeApp, cert } = require('firebase-admin/app');
const { getAuth } = require('firebase-admin/auth');
const { getFirestore } = require('firebase-admin/firestore');
const serviceAccount = require('./serviceAccountKey.json');

initializeApp({
  credential: cert(serviceAccount)
});

const auth = getAuth();
const db = getFirestore();

async function fixEnv() {
  const email = 'sohaibahmedjan7@gmail.com';
  console.log(`Starting fix for ${email}...`);

  try {
    // 1. Check/Create User in Auth
    let user;
    try {
      user = await auth.getUserByEmail(email);
      console.log('User already exists in Auth.');
    } catch (e) {
      if (e.code === 'auth/user-not-found') {
        user = await auth.createUser({
          email: email,
          password: 'BrainBites2026!',
          displayName: 'Sohaib Ahmed'
        });
        console.log('User created successfully in Auth.');
      } else {
        throw e;
      }
    }

    // 2. Ensure user is in 'admins' collection
    const adminRef = db.collection('admins').doc(user.uid);
    await adminRef.set({
      displayName: 'Sohaib Ahmed',
      email: email,
      role: 'SUPER_ADMIN',
      isActive: true,
      permissions: ['all'],
      createdAt: Date.now(),
      updatedAt: Date.now()
    }, { merge: true });
    console.log('Admin document anchored in Firestore.');

    console.log('ENVIRONMENT FIX COMPLETE.');
    process.exit(0);
  } catch (err) {
    console.error('CRITICAL FAILURE:', err);
    process.exit(1);
  }
}

fixEnv();
