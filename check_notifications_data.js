const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const serviceAccount = require('./serviceAccountKey.json');

initializeApp({
    credential: cert(serviceAccount)
});

const db = getFirestore();

async function checkNotifications() {
  const snapshot = await db.collection('notifications').orderBy('timestamp', 'desc').limit(5).get();
  if (snapshot.empty) {
    console.log('No notifications found.');
    return;
  }
  snapshot.forEach(doc => {
    console.log('ID:', doc.id);
    console.log('Data:', JSON.stringify(doc.data(), null, 2));
    console.log('---');
  });
}

checkNotifications().catch(console.error);
