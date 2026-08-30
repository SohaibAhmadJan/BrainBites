const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const serviceAccount = require('./serviceAccountKey.json');

initializeApp({
    credential: cert(serviceAccount)
});

const db = getFirestore();

async function checkCategories() {
  const snapshot = await db.collection('categories').get();
  const categories = [];
  snapshot.forEach(doc => {
    const data = doc.data();
    categories.push({
      id: doc.id,
      name: data.name,
      hasVectorIcon: data.hasOwnProperty('vectorIcon'),
      vectorIcon: data.vectorIcon,
      hasDescription: data.hasOwnProperty('description'),
      description: data.description
    });
  });
  console.log(JSON.stringify(categories, null, 2));
}

checkCategories().catch(console.error);
