const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');

const serviceAccount = require('./brainbites-24332456-firebase-adminsdk-fbsvc-c254c826d4.json');

initializeApp({
    credential: cert(serviceAccount)
});

const db = getFirestore();

async function check() {
    const snapshot = await db.collection('achievements').get();
    console.log(`Achievements Count: ${snapshot.size}`);
    process.exit(0);
}

check().catch(err => {
    console.error(err);
    process.exit(1);
});
