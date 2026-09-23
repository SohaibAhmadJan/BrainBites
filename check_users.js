const { initializeApp, cert, getApps } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const serviceAccount = require('./serviceAccountKey.json');

if (!getApps().length) {
    initializeApp({
        credential: cert(serviceAccount)
    });
}
const db = getFirestore();

async function check() {
  const usersRef = db.collection('users');
  const snapshot = await usersRef.get();
  snapshot.forEach(doc => {
    console.log(doc.id, '=>', JSON.stringify(doc.data().account, null, 2));
    console.log(doc.id, '=> profile =', JSON.stringify(doc.data().profile, null, 2));
  });
}
check();