const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const serviceAccount = require('F:/BrainBites/serviceAccountKey.json');

initializeApp({
    credential: cert(serviceAccount)
});

const db = getFirestore();

async function check() {
    console.log('Checking installations collection...');
    const snapshot = await db.collection('installations').get();
    console.log(`Total documents found: ${snapshot.size}`);
    snapshot.forEach(doc => {
        console.log(`ID: ${doc.id}, Data:`, JSON.stringify(doc.data()));
    });
}

check().catch(err => {
    console.error('Error during check:', err);
    process.exit(1);
});
