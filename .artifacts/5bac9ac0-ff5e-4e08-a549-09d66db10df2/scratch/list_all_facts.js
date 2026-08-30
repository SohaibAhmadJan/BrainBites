const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const serviceAccount = require('../../../serviceAccountKey.json');

initializeApp({
  credential: cert(serviceAccount)
});

const db = getFirestore();

async function list() {
  console.log("Fetching all facts from Firestore...");
  const snapshot = await db.collection('facts').get();
  console.log(`Total facts found: ${snapshot.size}`);

  snapshot.forEach(doc => {
    const data = doc.data();
    console.log(`ID: "${doc.id}", Text: "${data.fact ? data.fact.substring(0, 50) : 'N/A'}..."`);
  });
}

list();
