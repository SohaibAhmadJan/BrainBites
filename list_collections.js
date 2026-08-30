const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const serviceAccount = require('./serviceAccountKey.json');

initializeApp({
    credential: cert(serviceAccount)
});

const db = getFirestore();

async function listCollections() {
  const collections = await db.listCollections();
  console.log(collections.map(c => c.id));
}

listCollections().catch(console.error);
