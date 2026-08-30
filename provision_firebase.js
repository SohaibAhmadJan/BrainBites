const { initializeApp, cert } = require('firebase-admin/app');
const { getAuth } = require('firebase-admin/auth');
const { getFirestore } = require('firebase-admin/firestore');
const { getSecurityRules } = require('firebase-admin/security-rules');

const serviceAccount = require('./serviceAccountKey.json');

const app = initializeApp({
    credential: cert(serviceAccount)
});

const auth = getAuth(app);
const db = getFirestore(app);

const TARGET_EMAIL = 'sohaibahmedjan7@gmail.com';
const TEMP_PASSWORD = 'BrainBitesAdmin2026!';

async function provision() {
    console.log(`🚀 Provisioning ${TARGET_EMAIL}...`);

    let userRecord;
    try {
        userRecord = await auth.getUserByEmail(TARGET_EMAIL);
        console.log(`✔ User exists (UID: ${userRecord.uid})`);
    } catch (e) {
        userRecord = await auth.createUser({
            email: TARGET_EMAIL,
            password: TEMP_PASSWORD,
            displayName: 'System Super Admin'
        });
        console.log(`✔ Created User (UID: ${userRecord.uid})`);
    }

    await db.collection('admins').doc(userRecord.uid).set({
        email: TARGET_EMAIL,
        displayName: 'Initial Super Admin',
        role: 'SUPER_ADMIN',
        permissions: ['manage.admins', 'manage.content', 'manage.config', 'users.edit'],
        isActive: true,
        createdAt: Date.now(),
        updatedAt: Date.now()
    }, { merge: true });
    console.log(`✔ Firestore registry updated.`);

    const rulesContent = `
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function isAdmin() {
      return request.auth != null &&
             exists(/databases/$(database)/documents/admins/$(request.auth.uid)) &&
             get(/databases/$(database)/documents/admins/$(request.auth.uid)).data.isActive == true;
    }
    match /{document=**} { allow read, write: if isAdmin(); }
    match /facts/{id} { allow read: if true; }
    match /categories/{id} { allow read: if true; }
    match /collections/{id} { allow read: if true; }
    match /achievements/{id} { allow read: if true; }
    match /quizzes/{id} { allow read: if true; }
    match /notifications/{id} { allow read: if true; }
    match /users/{uid}/{document=**} {
       allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
    `.trim();

    try {
        const securityRules = getSecurityRules(app);
        const source = {
            files: [{
                name: 'firestore.rules',
                content: rulesContent
            }]
        };
        const ruleset = await securityRules.createRuleset(source);
        await securityRules.releaseRuleset(ruleset.name, 'cloud.firestore');
        console.log(`✔ Security Rules deployed.`);
    } catch (err) {
        console.error(`✖ Rules Deployment Failed: ${err.message}`);
    }

    console.log(`\n✨ Done! Login with:`);
    console.log(`📧 Email: ${TARGET_EMAIL}`);
    console.log(`🔑 Pass: ${TEMP_PASSWORD}`);
    process.exit(0);
}

provision().catch(err => {
    console.error(`🔥 ERROR: ${err.message}`);
    process.exit(1);
});
