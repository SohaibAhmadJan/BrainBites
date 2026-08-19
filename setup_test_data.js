const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');

const serviceAccount = require('./brainbites-24332456-firebase-adminsdk-fbsvc-c254c826d4.json');

initializeApp({
    credential: cert(serviceAccount)
});

const db = getFirestore();

async function setup() {
    console.log('Creating test document facts/test_phase3...');
    await db.collection('facts').doc('test_phase3').set({
        fact: 'This is a test fact for Phase 3 verification.',
        title: 'Test Document',
        category: 'Human Behavior',
        isPublished: true,
        createdAt: Date.now(),
        updatedAt: Date.now()
    });
    console.log('Test document created.');
    process.exit(0);
}

setup().catch(err => {
    console.error(err);
    process.exit(1);
});
