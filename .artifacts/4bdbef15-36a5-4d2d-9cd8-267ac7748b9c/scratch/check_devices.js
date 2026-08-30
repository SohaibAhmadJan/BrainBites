const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const serviceAccount = require('F:/BrainBites/serviceAccountKey.json');

initializeApp({
    credential: cert(serviceAccount)
});

const db = getFirestore();

async function check() {
  const devices = await db.collectionGroup('devices').get();
  console.log(`Total Device Records: ${devices.size}`);
  devices.docs.forEach(d => {
      const data = d.data();
      console.log(`Device ${d.id}: lastSeenAt=${data.lastSeenAt}, updatedAt=${data.updatedAt}`);
  });

  const analytics = await db.collection('analytics_events').where('name', '==', 'app_install').get();
  console.log(`Total app_install events: ${analytics.size}`);
  analytics.docs.forEach(d => {
      const data = d.data();
      console.log(`Event ${d.id}: device_id=${data.params?.device_id}, uid=${data.uid}`);
  });
}

check().catch(console.error);
