const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');

const serviceAccount = require('./brainbites-24332456-firebase-adminsdk-fbsvc-c254c826d4.json');

initializeApp({
    credential: cert(serviceAccount)
});

const db = getFirestore();

async function migrate() {
    console.log('Migrating Achievements...');
    const list = [
        { id: "first_step", title: "First Step", description: "Read your very first psychology fact.", maxProgress: 1, iconName: "DirectionsRun", requirementType: "READ_COUNT" },
        { id: "scholar", title: "The Scholar", description: "Read 10 unique psychology facts.", maxProgress: 10, iconName: "MenuBook", requirementType: "READ_COUNT" },
        { id: "curator", title: "The Curator", description: "Save 5 facts to your favorites.", maxProgress: 5, iconName: "Favorite", requirementType: "FAVORITE_COUNT" },
        { id: "explorer", title: "The Explorer", description: "Discover facts from 5 different categories.", maxProgress: 5, iconName: "Explore", requirementType: "CATEGORY_COUNT" },
        { id: "thinker", title: "The Thinker", description: "Read 50 unique facts.", maxProgress: 50, iconName: "Psychology", requirementType: "READ_COUNT" },
        { id: "socialite", title: "Socialite", description: "Share 3 facts with friends.", maxProgress: 3, iconName: "Share", requirementType: "SHARE_COUNT" },
        { id: "master", title: "Master of Mind", description: "Read 100 total facts.", maxProgress: 100, iconName: "AutoAwesome", requirementType: "READ_COUNT" }
    ];

    const batch = db.batch();
    list.forEach(item => {
        const docRef = db.collection('achievements').doc(item.id);
        batch.set(docRef, {
            ...item,
            isActive: true,
            createdAt: FieldValue.serverTimestamp()
        });
    });

    await batch.commit();
    console.log('Achievements Migrated.');
    process.exit(0);
}

migrate().catch(err => {
    console.error(err);
    process.exit(1);
});
