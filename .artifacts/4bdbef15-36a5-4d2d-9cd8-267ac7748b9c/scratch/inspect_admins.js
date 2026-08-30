const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const serviceAccount = require('F:/BrainBites/serviceAccountKey.json');

initializeApp({
    credential: cert(serviceAccount)
});

const db = getFirestore();

async function inspect() {
  const admins = await db.collection('admins').get();
  admins.docs.forEach(d => {
      console.log(`Admin ID: ${d.id}`);
      console.log(JSON.stringify(d.data(), null, 2));
      console.log('---');
  });
}

inspect().catch(console.error);
