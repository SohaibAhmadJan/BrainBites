const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');
const fs = require('fs');
const path = require('path');

// The JSON file you downloaded
const serviceAccount = require('./brainbites-24332456-firebase-adminsdk-fbsvc-c254c826d4.json');

console.log('Initializing Firebase Admin...');
try {
    initializeApp({
        credential: cert(serviceAccount)
    });
    console.log('Firebase Admin initialized successfully.');
} catch (error) {
    console.error('Failed to initialize Firebase Admin:', error);
    process.exit(1);
}

const db = getFirestore();

async function migrate() {
  console.log('Starting migration to Firestore...');

  // 1. Load Data
  const factsPath = path.join(__dirname, 'app/src/main/assets/facts.json');
  const quizPath = path.join(__dirname, 'app/src/main/assets/quiz_data.json');
  const collPath = path.join(__dirname, 'app/src/main/assets/collections.json');

  if (!fs.existsSync(factsPath) || !fs.existsSync(quizPath) || !fs.existsSync(collPath)) {
      console.error('Missing required JSON files in assets folder.');
      process.exit(1);
  }

  const factsData = JSON.parse(fs.readFileSync(factsPath, 'utf8'));
  const quizData = JSON.parse(fs.readFileSync(quizPath, 'utf8'));
  const collectionsData = JSON.parse(fs.readFileSync(collPath, 'utf8'));

  const quizMap = {};
  quizData.quizzes.forEach(q => {
    quizMap[q.factId] = q;
  });

  // 2. Migrate Facts & Quizzes
  console.log('Migrating Facts and Quizzes...');
  const factsBatch = db.batch();
  factsData.facts.forEach(fact => {
    const factRef = db.collection('facts').doc(fact.id);
    factsBatch.set(factRef, {
      fact: fact.fact,
      title: fact.title || 'Psychology Insight',
      category: fact.category,
      imageUrl: `https://picsum.photos/seed/${fact.id}/1200/800`,
      isPublished: true,
      createdAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp()
    });

    const quiz = quizMap[fact.id];
    if (quiz) {
      const quizRef = db.collection('quizzes').doc(fact.id);
      factsBatch.set(quizRef, {
        factId: fact.id,
        question: quiz.question,
        options: quiz.options,
        correctAnswerIndex: quiz.correctIndex,
        teaserType: quiz.teaserType,
        isActive: true,
        createdAt: FieldValue.serverTimestamp()
      });
    }
  });
  await factsBatch.commit();
  console.log('Facts and Quizzes migration batch committed.');

  // 3. Migrate Collections
  console.log('Migrating Collections...');
  const collBatch = db.batch();
  collectionsData.collections.forEach(coll => {
    const collRef = db.collection('collections').doc(coll.id);
    collBatch.set(collRef, {
      ...coll,
      isPublished: true,
      createdAt: FieldValue.serverTimestamp()
    });
  });
  await collBatch.commit();
  console.log('Collections migration batch committed.');

  // 4. Setup Categories (Based on Android Enum)
  console.log('Migrating Categories...');
  const categories = [
    { id: 'HUMAN_BEHAVIOR', name: 'Human Behavior', icon: '👥', color: '#A8DADC', sortOrder: 1 },
    { id: 'MENTAL_HEALTH', name: 'Mental Health', icon: '🧠', color: '#457B9D', sortOrder: 2 },
    { id: 'BRAIN_SCIENCE', name: 'Brain Science', icon: '🧪', color: '#E9C46A', sortOrder: 3 },
    { id: 'LOVE_ATTRACTION', name: 'Love & Attraction', icon: '💖', color: '#E76F51', sortOrder: 4 },
    { id: 'PERSONALITY', name: 'Personality Traits', icon: '🎭', color: '#F4A261', sortOrder: 5 },
    { id: 'BODY_LANGUAGE', name: 'Body Language', icon: '✋', color: '#2A9D8F', sortOrder: 6 },
    { id: 'SUBCONSCIOUS', name: 'Subconscious Mind', icon: '🌌', color: '#264653', sortOrder: 7 },
    { id: 'SOCIAL_PSYCHOLOGY', name: 'Social Psychology', icon: '🏘️', color: '#8AB17D', sortOrder: 8 },
    { id: 'HABITS_MOTIVATION', name: 'Habits & Motivation', icon: '📈', color: '#B5838D', sortOrder: 9 },
    { id: 'MEMORY_LEARNING', name: 'Memory & Learning', icon: '📚', color: '#6D6875', sortOrder: 10 }
  ];
  const catBatch = db.batch();
  categories.forEach(cat => {
    const catRef = db.collection('categories').doc(cat.id);
    catBatch.set(catRef, { ...cat, isActive: true });
  });
  await catBatch.commit();
  console.log('Categories migration batch committed.');

  console.log('Migration Successful!');
  process.exit(0);
}

migrate().catch(err => {
  console.error('Migration failed:', err);
  process.exit(1);
});
