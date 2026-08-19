const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');

const serviceAccount = require('./brainbites-24332456-firebase-adminsdk-fbsvc-c254c826d4.json');

initializeApp({
    credential: cert(serviceAccount)
});

const db = getFirestore();

/**
 * atomicUpdateFact
 * Demonstrates the REQUIRED backend logic:
 * 1. Modify the content (Fact)
 * 2. Create the audit record
 * Both performed in a single ATOMIC transaction.
 */
async function atomicUpdateFact(adminUid, factId, newData, reason) {
    console.log(`Admin ${adminUid} updating fact ${factId}...`);

    try {
        await db.runTransaction(async (transaction) => {
            const factRef = db.collection('facts').doc(factId);
            const factSnapshot = await transaction.get(factRef);

            if (!factSnapshot.exists) {
                throw new Error('Fact does not exist!');
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

        console.log('Fact updated and audit log created successfully.');
    } catch (e) {
        console.error('Transaction failed:', e);
    }
}

// atomicUpdateFact('YOUR_ADMIN_UID', '1', { fact: 'Validated fact content.' }, 'Routine update');
