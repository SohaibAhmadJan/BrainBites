const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');

const serviceAccount = require('./brainbites-24332456-firebase-adminsdk-fbsvc-c254c826d4.json');

initializeApp({
    credential: cert(serviceAccount)
});

const db = getFirestore();

async function verify() {
    console.log('Verifying Firestore Migration...');

    const collections = ['facts', 'quizzes', 'collections', 'categories', 'app_config'];

    for (const collName of collections) {
        const snapshot = await db.collection(collName).get();
        console.log(`Collection "${collName}": ${snapshot.size} documents found.`);

        if (snapshot.size > 0 && collName !== 'app_config') {
            const doc = snapshot.docs[0];
            console.log(`  - Sample ID: ${doc.id}`);
            const data = doc.data();
            if (collName === 'facts') console.log(`  - Sample Fact: ${data.fact.substring(0, 50)}...`);
            if (collName === 'quizzes') console.log(`  - Sample Question: ${data.question}`);
        }
    }

    const configDoc = await db.collection('app_config').doc('global').get();
    if (configDoc.exists) {
        console.log('Collection "app_config": Global config document exists.');
        console.log('  - Maintenance Mode: ' + configDoc.data().maintenanceMode);
    } else {
        // Checking for "app_config/global" which might have been seeded as "app_config/global" in the script
        // or the script used db.collection('app_config').doc('global')
        console.log('Collection "app_config": Global config document MISSING at "global" ID.');
    }

    console.log('Verification Complete.');
    process.exit(0);
}

verify().catch(err => {
    console.error('Verification failed:', err);
    process.exit(1);
});
