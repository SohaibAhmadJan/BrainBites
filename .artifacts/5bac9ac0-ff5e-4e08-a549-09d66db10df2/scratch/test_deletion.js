const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const serviceAccount = require('../../../serviceAccountKey.json');

initializeApp({
  credential: cert(serviceAccount)
});

const db = getFirestore();

async function test() {
  const targetText = "Phase 3 Transaction Test SUCCESS.";
  console.log(`Searching for fact with text: "${targetText}"`);

  const snapshot = await db.collection('facts').where('fact', '==', targetText).get();

  if (snapshot.empty) {
    console.log('No fact found with that exact text.');
    // Try partial search
    const all = await db.collection('facts').get();
    let found = false;
    all.forEach(d => {
        if (d.data().fact && d.data().fact.includes("Phase 3")) {
            console.log(`FOUND NEAR MATCH: ID: "${d.id}", Text: "${d.data().fact}"`);
            found = true;
        }
    });
    if (!found) console.log('No near matches found either.');
    return;
  }

  snapshot.forEach(doc => {
    console.log(`Found matching fact! Real ID: "${doc.id}"`);
    console.log('Attempting deletion of this ID...');
    doc.ref.delete().then(() => console.log('Deletion successful!'))
                   .catch(e => console.error('Deletion failed:', e));
  });
}

test();
