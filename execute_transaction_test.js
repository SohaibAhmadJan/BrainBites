const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');

const serviceAccount = require('./brainbites-24332456-firebase-adminsdk-fbsvc-c254c826d4.json');

initializeApp({
    credential: cert(serviceAccount)
});

const db = getFirestore();
const ADMIN_UID = '3QO7YdOInpMeM9BOf91Uq69fO0M2';

async function atomicUpdateFact(adminUid, factId, newData, reason) {
    console.log(`Step 5: Admin ${adminUid} updating test document ${factId}...`);

    try {
        await db.runTransaction(async (transaction) => {
            const factRef = db.collection('facts').doc(factId);
            const factSnapshot = await transaction.get(factRef);

            if (!factSnapshot.exists) {
                throw new Error('Fact document missing! Run setup_test_data.js first.');
            }

            const beforeData = factSnapshot.data();

            // 1. Update Fact
            transaction.update(factRef, {
                ...newData,
                updatedAt: Date.now()
            });

            // 2. Create Audit Log
            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: adminUid,
                action: 'UPDATE_FACT',
                targetType: 'FACT',
                targetId: factId,
                before: beforeData,
                after: { ...beforeData, ...newData },
                reason: reason,
                createdAt: Date.now()
            });
        });

        console.log('PASS: Transaction committed atomically.');
    } catch (e) {
        console.error('FAIL: Transaction failed:', e);
        process.exit(1);
    }
}

async function run() {
    await atomicUpdateFact(ADMIN_UID, 'test_phase3', { fact: 'Phase 3 Transaction Test SUCCESS.' }, 'Administrative verification');

    console.log('Verifying results...');
    const fact = await db.collection('facts').doc('test_phase3').get();
    const audits = await db.collection('audit_logs').where('targetId', '==', 'test_phase3').get();

    if (fact.data().fact === 'Phase 3 Transaction Test SUCCESS.' && audits.size > 0) {
        console.log('PASS: Content and Audit Log verify correctly.');
        const audit = audits.docs[0].data();
        console.log(`Audit Record ID: ${audits.docs[0].id}`);
        console.log(`Action: ${audit.action}`);
        console.log(`Reason: ${audit.reason}`);
    } else {
        console.error('FAIL: Verification failed.');
        process.exit(1);
    }
    process.exit(0);
}

run();
