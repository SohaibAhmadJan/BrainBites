const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const serviceAccount = require('F:/BrainBites/serviceAccountKey.json');

initializeApp({
    credential: cert(serviceAccount)
});

const db = getFirestore();

async function count() {
  const users = await db.collection('users').get();
  const admins = await db.collection('admins').get();
  const analytics = await db.collection('analytics_events').get();

  console.log(`Users: ${users.size}`);
  users.docs.forEach(d => {
      const data = d.data();
      console.log(` - User ${d.id}: Status=${data.account?.status}, Created=${data.account?.createdAt}`);
  });

  console.log(`Admins: ${admins.size}`);
  admins.docs.forEach(d => console.log(` - Admin ${d.id}`));

  console.log(`Analytics Events: ${analytics.size}`);
  const installs = analytics.docs.filter(d => d.data().name === 'app_install');
  console.log(` - App Install Events: ${installs.length}`);
}

count().catch(console.error);
