const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');

const serviceAccount = require('./brainbites-24332456-firebase-adminsdk-fbsvc-c254c826d4.json');

initializeApp({
    credential: cert(serviceAccount)
});

const db = getFirestore();
const ADMIN_UID = '3QO7YdOInpMeM9BOf91Uq69fO0M2';

async function verify() {
    console.log(`Verifying Admin status for UID: ${ADMIN_UID}...`);

    const doc = await db.collection('admins').doc(ADMIN_UID).get();

    if (!doc.exists) {
        console.error('FAIL: Admin document does not exist.');
        process.exit(1);
    }

    const data = doc.data();
    console.log('PASS: Admin document exists.');
    console.log(`Role: ${data.role}`);
    console.log(`Is Active: ${data.isActive}`);

    if (data.role === 'SUPER_ADMIN' && data.isActive === true) {
        console.log('PASS: SUPER_ADMIN recognized and active.');
    } else {
        console.error('FAIL: Role or Active status mismatch.');
        process.exit(1);
    }

    process.exit(0);
}

verify().catch(err => {
    console.error(err);
    process.exit(1);
});
