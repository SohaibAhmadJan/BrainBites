const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');

const serviceAccount = require('./brainbites-24332456-firebase-adminsdk-fbsvc-c254c826d4.json');

initializeApp({
    credential: cert(serviceAccount)
});

const db = getFirestore();

const TARGET_UID = process.argv[2];

if (!TARGET_UID) {
    console.error('Usage: node bootstrap_admin.js <USER_UID>');
    process.exit(1);
}

async function bootstrap() {
    console.log(`Granting SUPER_ADMIN role to UID: ${TARGET_UID}...`);

    await db.collection('admins').doc(TARGET_UID).set({
        email: 'your-email@example.com', // User should update this
        displayName: 'Initial Super Admin',
        role: 'SUPER_ADMIN',
        permissions: [], // Full access for SUPER_ADMIN is hardcoded in rules
        isActive: true,
        createdAt: Date.now(),
        updatedAt: Date.now()
    });

    console.log('Success! You now have full administrative control.');
    process.exit(0);
}

bootstrap().catch(err => {
    console.error('Bootstrap failed:', err);
    process.exit(1);
});
