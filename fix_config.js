const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');

const serviceAccount = require('./brainbites-24332456-firebase-adminsdk-fbsvc-c254c826d4.json');

initializeApp({
    credential: cert(serviceAccount)
});

const db = getFirestore();

async function fix() {
    console.log('Setting up app_config/global...');

    const config = {
        maintenanceMode: false,
        minVersion: '1.0.0',
        latestVersion: '3.4.8.7',
        quizzesEnabled: true,
        achievementsEnabled: true,
        dailyFactId: '1',
        dailyTipTitle: 'The 2-Minute Rule',
        dailyTipMessage: 'If a task takes less than 2 minutes, do it now.',
        homeSectionsOrder: ['HERO', 'CATEGORIES', 'QUICK_ACTIONS', 'MOOD', 'RECENT', 'DISCOVER', 'ACHIEVEMENTS', 'TIP', 'TRENDING'],
        updatedAt: FieldValue.serverTimestamp()
    };

    await db.collection('app_config').doc('global').set(config);
    console.log('App config initialized.');
    process.exit(0);
}

fix().catch(err => {
    console.error('Fix failed:', err);
    process.exit(1);
});
