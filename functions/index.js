/**
 * BrainBites Admin API
 * Trusted Backend Mutations with Atomic Auditing.
 */

const { initializeApp } = require('firebase-admin/app');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');
const { getMessaging } = require('firebase-admin/messaging');
const { onCall, HttpsError } = require('firebase-functions/v2/https');
const { verifyAdmin } = require('./utils/auth');

initializeApp();
const db = getFirestore();

/**
 * updateFactAtomic
 */
exports.updateFactAtomic = onCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'manage.content');
    const { id, data, reason } = request.data;

    if (!id || !data) {
        throw new HttpsError('invalid-argument', 'Missing payload parameters (id, data).');
    }

    try {
        await db.runTransaction(async (transaction) => {
            const factRef = db.collection('facts').doc(id);
            const factSnapshot = await transaction.get(factRef);

            let beforeData = null;
            if (factSnapshot.exists) {
                beforeData = factSnapshot.data();
            }

            transaction.set(factRef, {
                ...data,
                updatedAt: Date.now()
            }, { merge: true });

            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: admin.uid,
                action: factSnapshot.exists ? 'UPDATE_FACT' : 'CREATE_FACT',
                targetType: 'FACT',
                targetId: id,
                before: beforeData,
                after: data,
                reason: reason || 'Fact synchronization',
                createdAt: Date.now()
            });
        });

        return { status: "success", factId: id };
    } catch (e) {
        console.error("updateFactAtomic failure:", e);
        throw new HttpsError('internal', `Sync Protocol Failure: ${e.message}`);
    }
});

/**
 * deleteFactAtomic
 * Optimized: Now handles "Deep Deletion" (cleans up quizzes and collection references).
 * Fixed: Queries moved outside transaction to prevent protocol rollback.
 */
exports.deleteFactAtomic = onCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'manage.content');
    const { id, reason } = request.data;

    if (!id) throw new HttpsError('invalid-argument', 'Missing fact ID.');

    try {
        // 1. Pre-query collections outside the transaction (Transaction limits)
        const collectionsQuery = db.collection('collections').where('factIds', 'array-contains', id);
        const collectionsSnapshot = await collectionsQuery.get();
        const collectionRefs = collectionsSnapshot.docs.map(doc => doc.ref);

        await db.runTransaction(async (transaction) => {
            const factRef = db.collection('facts').doc(id);
            const quizRef = db.collection('quizzes').doc(id);
            const factSnapshot = await transaction.get(factRef);

            if (!factSnapshot.exists) {
                return { status: "success", factId: id, warning: "Node already expunged." };
            }

            const beforeData = factSnapshot.data();

            // 1. Delete the core fact
            transaction.delete(factRef);

            // 2. Cleanup associated quiz
            transaction.delete(quizRef);

            // 3. Scrub from all curated collections
            collectionRefs.forEach(ref => {
                transaction.update(ref, {
                    factIds: FieldValue.arrayRemove(id)
                });
            });

            // 4. Audit Log
            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: admin.uid,
                action: 'DELETE_FACT_DEEP',
                targetType: 'FACT',
                targetId: id,
                before: beforeData,
                after: null,
                reason: reason || 'Manual deep expunge',
                createdAt: Date.now()
            });
        });
        return { status: "success", factId: id };
    } catch (e) {
        console.error("deleteFactAtomic failure:", e);
        throw new HttpsError('internal', `Expunge Protocol Failure: ${e.message}`);
    }
});

/**
 * updateCategoryAtomic
 */
exports.updateCategoryAtomic = onCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'manage.content');
    const { id, data, reason } = request.data;

    if (!id || !data) throw new HttpsError('invalid-argument', 'Invalid payload.');

    try {
        await db.runTransaction(async (transaction) => {
            const catRef = db.collection('categories').doc(id);
            const snapshot = await transaction.get(catRef);
            const beforeData = snapshot.exists ? snapshot.data() : null;

            transaction.set(catRef, { ...data }, { merge: true });

            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: admin.uid,
                action: snapshot.exists ? 'UPDATE_CATEGORY' : 'CREATE_CATEGORY',
                targetType: 'CATEGORY',
                targetId: id,
                before: beforeData,
                after: data,
                reason: reason || 'Category sync',
                createdAt: Date.now()
            });
        });
        return { status: "success", categoryId: id };
    } catch (e) {
        console.error("updateCategoryAtomic failure:", e);
        throw new HttpsError('internal', `Category Sync Failure: ${e.message}`);
    }
});

/**
 * deleteCategoryAtomic
 */
exports.deleteCategoryAtomic = onCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'manage.content');
    const { id, reason } = request.data;

    if (!id) throw new HttpsError('invalid-argument', 'Missing ID.');

    try {
        await db.runTransaction(async (transaction) => {
            const ref = db.collection('categories').doc(id);
            const snapshot = await transaction.get(ref);
            if (!snapshot.exists) return; // Safe delete

            transaction.delete(ref);

            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: admin.uid,
                action: 'DELETE_CATEGORY',
                targetType: 'CATEGORY',
                targetId: id,
                before: snapshot.data(),
                after: null,
                reason: reason || 'Domain dissolution',
                createdAt: Date.now()
            });
        });
        return { status: "success" };
    } catch (e) {
        console.error("deleteCategoryAtomic failure:", e);
        throw new HttpsError('internal', `Category Deletion Failure: ${e.message}`);
    }
});

/**
 * updateReportStatusAtomic
 */
exports.updateReportStatusAtomic = onCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'users.edit');
    const { id, status, reason } = request.data;

    if (!id || !status) throw new HttpsError('invalid-argument', 'Invalid payload.');

    try {
        await db.runTransaction(async (transaction) => {
            const ref = db.collection('user_reports').doc(id);
            const snapshot = await transaction.get(ref);
            if (!snapshot.exists) throw new Error('Report not found.');

            const beforeData = snapshot.data();

            transaction.update(ref, {
                status: status,
                updatedAt: Date.now()
            });

            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: admin.uid,
                action: 'UPDATE_REPORT_STATUS',
                targetType: 'REPORT',
                targetId: id,
                before: { status: beforeData.status },
                after: { status: status },
                reason: reason || 'Administrative triage',
                createdAt: Date.now()
            });
        });
        return { status: "success" };
    } catch (e) {
        console.error("updateReportStatusAtomic failure:", e);
        throw new HttpsError('internal', `Report Status Update Failure: ${e.message}`);
    }
});

/**
 * updateAppConfigAtomic
 */
exports.updateAppConfigAtomic = onCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'manage.config');
    const { data, reason } = request.data;

    try {
        await db.runTransaction(async (transaction) => {
            const configRef = db.collection('app_settings').doc('global_config');
            const snapshot = await transaction.get(configRef);
            const beforeData = snapshot.data() || {};

            transaction.set(configRef, { ...data, updatedAt: Date.now() }, { merge: true });

            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: admin.uid,
                action: 'UPDATE_CONFIG',
                targetType: 'CONFIG',
                targetId: 'global_config',
                before: beforeData,
                after: data,
                reason: reason || 'System configuration change',
                createdAt: Date.now()
            });
        });
        return { status: "success" };
    } catch (e) {
        console.error("updateAppConfigAtomic failure:", e);
        throw new HttpsError('internal', `Config Sync Failure: ${e.message}`);
    }
});

/**
 * updateAdminAtomic
 */
exports.updateAdminAtomic = onCall(async (request) => {
    const caller = await verifyAdmin(request, db, 'manage.admins');
    const { uid, data, reason } = request.data;

    if (!uid || !data) throw new HttpsError('invalid-argument', 'Invalid payload.');

    if (caller.uid === uid && (data.role || data.permissions)) {
        throw new HttpsError('permission-denied', 'Self-modification of protocol roles is prohibited.');
    }

    try {
        return await db.runTransaction(async (transaction) => {
            const adminRef = db.collection('admins').doc(uid);
            const snapshot = await transaction.get(adminRef);
            const beforeData = snapshot.exists ? snapshot.data() : null;

            if (beforeData && beforeData.role === 'SUPER_ADMIN' && caller.role !== 'SUPER_ADMIN') {
                throw new Error('Insufficient clearance to modify a SUPER_ADMIN.');
            }

            transaction.set(adminRef, { ...data, updatedAt: Date.now() }, { merge: true });

            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: caller.uid,
                action: snapshot.exists ? 'UPDATE_ADMIN' : 'CREATE_ADMIN',
                targetType: 'ADMIN',
                targetId: uid,
                before: beforeData,
                after: data,
                reason: reason || 'Admin registry update',
                createdAt: Date.now()
            });

            return { status: "success", adminUid: uid };
        });
    } catch (e) {
        console.error("updateAdminAtomic failure:", e);
        throw new HttpsError('internal', `Admin Sync Failure: ${e.message}`);
    }
});

/**
 * deleteAdminAtomic
 */
exports.deleteAdminAtomic = onCall(async (request) => {
    const caller = await verifyAdmin(request, db, 'manage.admins');
    const { uid, reason } = request.data;

    if (!uid) throw new HttpsError('invalid-argument', 'Missing UID.');

    if (caller.uid === uid) {
        throw new HttpsError('permission-denied', 'Self-expungement prohibited.');
    }

    try {
        return await db.runTransaction(async (transaction) => {
            const ref = db.collection('admins').doc(uid);
            const snapshot = await transaction.get(ref);
            if (!snapshot.exists) return; // Safe delete

            const targetData = snapshot.data();
            if (targetData.role === 'SUPER_ADMIN' && caller.role !== 'SUPER_ADMIN') {
                throw new Error('Insufficient clearance.');
            }

            transaction.delete(ref);

            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: caller.uid,
                action: 'DELETE_ADMIN',
                targetType: 'ADMIN',
                targetId: uid,
                before: snapshot.data(),
                after: null,
                reason: reason || 'Registry removal',
                createdAt: Date.now()
            });

            return { status: "success" };
        });
    } catch (e) {
        console.error("deleteAdminAtomic failure:", e);
        throw new HttpsError('internal', `Admin Deletion Failure: ${e.message}`);
    }
});

/**
 * sendGlobalNotificationAtomic
 * Dispatches a push notification to all devices subscribed to 'global_broadcasts'
 * and archives the message in the notification registry.
 */
exports.sendGlobalNotificationAtomic = onCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'manage.content');
    const { data, reason } = request.data;

    if (!data || !data.title || !data.message) {
        throw new HttpsError('invalid-argument', 'Message payload must include title and body.');
    }

    try {
        const notifRef = db.collection('notifications').doc();
        const notificationId = notifRef.id;

        const notificationRecord = {
            ...data,
            id: notificationId,
            isGlobal: true,
            timestamp: Date.now()
        };

        // 1. Dispatch FCM Push Notification (OS System Tray)
        const message = {
            topic: 'global_broadcasts',
            notification: {
                title: data.title,
                body: data.message
            },
            data: {
                type: data.type || 'GENERAL',
                imageUrl: data.imageUrl || '',
                deepLinkFactId: data.deepLinkFactId || '',
                notificationId: notificationId
            },
            android: {
                priority: 'high',
                notification: {
                    sound: 'default',
                    channelId: 'brain_bites_notifications'
                }
            }
        };

        // Execute push and database write
        const [fcmResponse] = await Promise.all([
            getMessaging().send(message),
            db.runTransaction(async (transaction) => {
                transaction.set(notifRef, notificationRecord);

                const auditRef = db.collection('audit_logs').doc();
                transaction.set(auditRef, {
                    adminUid: admin.uid,
                    action: 'SEND_NOTIFICATION_GLOBAL',
                    targetType: 'NOTIFICATION',
                    targetId: notificationId,
                    before: null,
                    after: notificationRecord,
                    reason: reason || 'Broadcast dispatch',
                    createdAt: Date.now()
                });
            })
        ]);

        console.log(`Successfully dispatched broadcast: ${fcmResponse}`);
        return { status: "success", notificationId, fcmMessageId: fcmResponse };
    } catch (e) {
        console.error("sendGlobalNotificationAtomic failure:", e);
        throw new HttpsError('internal', `Broadcast Protocol Failure: ${e.message}`);
    }
});

/**
 * updateQuizAtomic
 */
exports.updateQuizAtomic = onCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'manage.content');
    const { id, data, reason } = request.data;

    if (!id || !data) throw new HttpsError('invalid-argument', 'Invalid payload.');

    try {
        await db.runTransaction(async (transaction) => {
            const ref = db.collection('quizzes').doc(id);
            const snapshot = await transaction.get(ref);
            const beforeData = snapshot.exists ? snapshot.data() : null;

            transaction.set(ref, {
                ...data,
                updatedAt: Date.now(),
                createdAt: beforeData ? beforeData.createdAt : Date.now()
            }, { merge: true });

            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: admin.uid,
                action: snapshot.exists ? 'UPDATE_QUIZ' : 'CREATE_QUIZ',
                targetType: 'QUIZ',
                targetId: id,
                before: beforeData,
                after: data,
                reason: reason || 'Quiz content sync',
                createdAt: Date.now()
            });
        });
        return { status: "success", quizId: id };
    } catch (e) {
        console.error("updateQuizAtomic failure:", e);
        throw new HttpsError('internal', `Quiz Sync Failure: ${e.message}`);
    }
});

/**
 * deleteQuizAtomic
 */
exports.deleteQuizAtomic = onCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'manage.content');
    const { id, reason } = request.data;

    if (!id) throw new HttpsError('invalid-argument', 'Missing ID.');

    try {
        await db.runTransaction(async (transaction) => {
            const ref = db.collection('quizzes').doc(id);
            const snapshot = await transaction.get(ref);
            if (!snapshot.exists) return; // Safe delete

            transaction.delete(ref);

            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: admin.uid,
                action: 'DELETE_QUIZ',
                targetType: 'QUIZ',
                targetId: id,
                before: snapshot.data(),
                after: null,
                reason: reason || 'Manual quiz removal',
                createdAt: Date.now()
            });
        });
        return { status: "success" };
    } catch (e) {
        console.error("deleteQuizAtomic failure:", e);
        throw new HttpsError('internal', `Quiz Deletion Failure: ${e.message}`);
    }
});

/**
 * updateCollectionAtomic
 */
exports.updateCollectionAtomic = onCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'manage.content');
    const { id, data, reason } = request.data;

    if (!id || !data) throw new HttpsError('invalid-argument', 'Invalid payload.');

    try {
        await db.runTransaction(async (transaction) => {
            const ref = db.collection('collections').doc(id);
            const snapshot = await transaction.get(ref);
            const beforeData = snapshot.exists ? snapshot.data() : null;

            transaction.set(ref, {
                ...data,
                createdAt: beforeData ? beforeData.createdAt : Date.now()
            }, { merge: true });

            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: admin.uid,
                action: snapshot.exists ? 'UPDATE_COLLECTION' : 'CREATE_COLLECTION',
                targetType: 'COLLECTION',
                targetId: id,
                before: beforeData,
                after: data,
                reason: reason || 'Collection sequence update',
                createdAt: Date.now()
            });
        });
        return { status: "success", collectionId: id };
    } catch (e) {
        console.error("updateCollectionAtomic failure:", e);
        throw new HttpsError('internal', `Collection Sync Failure: ${e.message}`);
    }
});

/**
 * deleteCollectionAtomic
 */
exports.deleteCollectionAtomic = onCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'manage.content');
    const { id, reason } = request.data;

    if (!id) throw new HttpsError('invalid-argument', 'Missing ID.');

    try {
        await db.runTransaction(async (transaction) => {
            const ref = db.collection('collections').doc(id);
            const snapshot = await transaction.get(ref);
            if (!snapshot.exists) return; // Safe delete

            transaction.delete(ref);

            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: admin.uid,
                action: 'DELETE_COLLECTION',
                targetType: 'COLLECTION',
                targetId: id,
                before: snapshot.data(),
                after: null,
                reason: reason || 'Manual collection removal',
                createdAt: Date.now()
            });
        });
        return { status: "success" };
    } catch (e) {
        console.error("deleteCollectionAtomic failure:", e);
        throw new HttpsError('internal', `Collection Deletion Failure: ${e.message}`);
    }
});

/**
 * updateAchievementAtomic
 */
exports.updateAchievementAtomic = onCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'manage.content');
    const { id, data, reason } = request.data;

    if (!id || !data) throw new HttpsError('invalid-argument', 'Invalid payload.');

    try {
        await db.runTransaction(async (transaction) => {
            const ref = db.collection('achievements').doc(id);
            const snapshot = await transaction.get(ref);
            const beforeData = snapshot.exists ? snapshot.data() : null;

            transaction.set(ref, {
                ...data,
                createdAt: beforeData ? beforeData.createdAt : Date.now()
            }, { merge: true });

            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: admin.uid,
                action: snapshot.exists ? 'UPDATE_ACHIEVEMENT' : 'CREATE_ACHIEVEMENT',
                targetType: 'ACHIEVEMENT',
                targetId: id,
                before: beforeData,
                after: data,
                reason: reason || 'Achievement definition sync',
                createdAt: Date.now()
            });
        });
        return { status: "success", achievementId: id };
    } catch (e) {
        console.error("updateAchievementAtomic failure:", e);
        throw new HttpsError('internal', `Achievement Sync Failure: ${e.message}`);
    }
});

/**
 * deleteAchievementAtomic
 */
exports.deleteAchievementAtomic = onCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'manage.content');
    const { id, reason } = request.data;

    if (!id) throw new HttpsError('invalid-argument', 'Missing ID.');

    try {
        await db.runTransaction(async (transaction) => {
            const ref = db.collection('achievements').doc(id);
            const snapshot = await transaction.get(ref);
            if (!snapshot.exists) return; // Safe delete

            transaction.delete(ref);

            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: admin.uid,
                action: 'DELETE_ACHIEVEMENT',
                targetType: 'ACHIEVEMENT',
                targetId: id,
                before: snapshot.data(),
                after: null,
                reason: reason || 'Manual achievement removal',
                createdAt: Date.now()
            });
        });
        return { status: "success" };
    } catch (e) {
        console.error("deleteAchievementAtomic failure:", e);
        throw new HttpsError('internal', `Achievement Deletion Failure: ${e.message}`);
    }
});

/**
 * updateQuoteAtomic
 */
exports.updateQuoteAtomic = onCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'manage.content');
    const { id, data, reason } = request.data;

    if (!id || !data) throw new HttpsError('invalid-argument', 'Invalid payload.');

    try {
        await db.runTransaction(async (transaction) => {
            const ref = db.collection('quotes').doc(id);
            const snapshot = await transaction.get(ref);
            const beforeData = snapshot.exists ? snapshot.data() : null;

            transaction.set(ref, {
                ...data,
                createdAt: beforeData ? beforeData.createdAt : Date.now()
            }, { merge: true });

            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: admin.uid,
                action: snapshot.exists ? 'UPDATE_QUOTE' : 'CREATE_QUOTE',
                targetType: 'QUOTE',
                targetId: id,
                before: beforeData,
                after: data,
                reason: reason || 'Wisdom nexus synchronization',
                createdAt: Date.now()
            });
        });
        return { status: "success", quoteId: id };
    } catch (e) {
        console.error("updateQuoteAtomic failure:", e);
        throw new HttpsError('internal', `Quote Sync Failure: ${e.message}`);
    }
});

/**
 * deleteQuoteAtomic
 */
exports.deleteQuoteAtomic = onCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'manage.content');
    const { id, reason } = request.data;

    if (!id) throw new HttpsError('invalid-argument', 'Missing ID.');

    try {
        await db.runTransaction(async (transaction) => {
            const ref = db.collection('quotes').doc(id);
            const snapshot = await transaction.get(ref);
            if (!snapshot.exists) return; // Safe delete

            transaction.delete(ref);

            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: admin.uid,
                action: 'DELETE_QUOTE',
                targetType: 'QUOTE',
                targetId: id,
                before: snapshot.data(),
                after: null,
                reason: reason || 'Manual wisdom removal',
                createdAt: Date.now()
            });
        });
        return { status: "success" };
    } catch (e) {
        console.error("deleteQuoteAtomic failure:", e);
        throw new HttpsError('internal', `Quote Deletion Failure: ${e.message}`);
    }
});

/**
 * bulkImportFactsAtomic
 */
exports.bulkImportFactsAtomic = onCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'manage.content');
    const { items, reason } = request.data;

    if (!items || !Array.isArray(items)) throw new HttpsError('invalid-argument', 'Items array required.');
    if (items.length > 100) throw new HttpsError('invalid-argument', 'Batch size limited to 100 nodes for transaction safety.');

    try {
        await db.runTransaction(async (transaction) => {
            for (const item of items) {
                const factRef = db.collection('facts').doc(item.id);
                transaction.set(factRef, {
                    ...item,
                    updatedAt: Date.now(),
                    createdAt: item.createdAt || Date.now()
                }, { merge: true });
            }

            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: admin.uid,
                action: 'BULK_IMPORT_FACTS',
                targetType: 'FACT',
                targetId: 'MULTIPLE',
                before: null,
                after: { count: items.length },
                reason: reason || `Bulk ingestion of ${items.length} nodes`,
                createdAt: Date.now()
            });
        });
        return { status: "success", count: items.length };
    } catch (e) {
        console.error("bulkImportFactsAtomic failure:", e);
        throw new HttpsError('internal', `Bulk Import Failure: ${e.message}`);
    }
});

/**
 * resetUserStatsAtomic
 */
exports.resetUserStatsAtomic = onCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'users.edit');
    const { uid, fields, reason } = request.data;

    if (!uid || !fields || !Array.isArray(fields)) {
        throw new HttpsError('invalid-argument', 'Invalid payload.');
    }

    try {
        await db.runTransaction(async (transaction) => {
            const userRef = db.collection('users').doc(uid);
            const snapshot = await transaction.get(userRef);
            if (!snapshot.exists) throw new Error('User not found.');

            const beforeData = snapshot.data().stats || {};
            const updates = {};
            fields.forEach(field => {
                updates[`stats.${field}`] = 0;
            });
            updates['account.updatedAt'] = Date.now();

            transaction.update(userRef, updates);

            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: admin.uid,
                action: 'RESET_USER_STATS',
                targetType: 'USER',
                targetId: uid,
                before: beforeData,
                after: fields.reduce((acc, f) => ({ ...acc, [f]: 0 }), {}),
                reason: reason || 'Administrative stat reset',
                createdAt: Date.now()
            });
        });
        return { status: "success" };
    } catch (e) {
        console.error("resetUserStatsAtomic failure:", e);
        throw new HttpsError('internal', `Stats Reset Failure: ${e.message}`);
    }
});

/**
 * awardAchievementAtomic
 */
exports.awardAchievementAtomic = onCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'users.edit');
    const { uid, achievementId, reason } = request.data;

    if (!uid || !achievementId) throw new HttpsError('invalid-argument', 'Invalid payload.');

    try {
        await db.runTransaction(async (transaction) => {
            const userRef = db.collection('users').doc(uid);
            const achRef = db.collection('achievements').doc(achievementId);
            const userSnapshot = await transaction.get(userRef);
            const achSnapshot = await transaction.get(achRef);

            if (!userSnapshot.exists) throw new Error('User not found.');
            if (!achSnapshot.exists) throw new Error('Achievement definition not found.');

            const earnedRef = userRef.collection('achievements').doc(achievementId);
            const earnedSnapshot = await transaction.get(earnedRef);
            if (earnedSnapshot.exists) throw new Error('User already possesses this milestone.');

            const achievementData = {
                id: achievementId,
                title: achSnapshot.data().title,
                earnedAt: Date.now(),
                isManual: true
            };

            transaction.set(earnedRef, achievementData);

            transaction.update(userRef, {
                'stats.achievementsCount': (userSnapshot.data().stats?.achievementsCount || 0) + 1,
                'account.updatedAt': Date.now()
            });

            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: admin.uid,
                action: 'AWARD_ACHIEVEMENT',
                targetType: 'USER',
                targetId: uid,
                before: null,
                after: achievementData,
                reason: reason || 'Manual milestone award',
                createdAt: Date.now()
            });
        });
        return { status: "success" };
    } catch (e) {
        console.error("awardAchievementAtomic failure:", e);
        throw new HttpsError('internal', `Achievement Award Failure: ${e.message}`);
    }
});

/**
 * deleteNotificationAtomic
 */
exports.deleteNotificationAtomic = onCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'manage.content');
    const { id, reason } = request.data;

    if (!id) throw new HttpsError('invalid-argument', 'Missing ID.');

    try {
        await db.runTransaction(async (transaction) => {
            const ref = db.collection('notifications').doc(id);
            const snapshot = await transaction.get(ref);
            if (!snapshot.exists) return; // Safe delete

            transaction.delete(ref);

            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: admin.uid,
                action: 'DELETE_NOTIFICATION',
                targetType: 'NOTIFICATION',
                targetId: id,
                before: snapshot.data(),
                after: null,
                reason: reason || 'Manual broadcast removal',
                createdAt: Date.now()
            });
        });
        return { status: "success" };
    } catch (e) {
        console.error("deleteNotificationAtomic failure:", e);
        throw new HttpsError('internal', `Broadcast Deletion Failure: ${e.message}`);
    }
});

/**
 * updateUserStatusAtomic
 */
exports.updateUserStatusAtomic = onCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'users.edit');
    const { uid, status, reason } = request.data;

    if (!uid || !status) throw new HttpsError('invalid-argument', 'Invalid payload.');

    try {
        await db.runTransaction(async (transaction) => {
            const userRef = db.collection('users').doc(uid);
            const snapshot = await transaction.get(userRef);
            if (!snapshot.exists) throw new Error('User not found.');

            const beforeData = snapshot.data();

            transaction.update(userRef, {
                'account.status': status,
                'account.updatedAt': Date.now()
            });

            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: admin.uid,
                action: 'UPDATE_USER_STATUS',
                targetType: 'USER',
                targetId: uid,
                before: { status: beforeData.account?.status },
                after: { status: status },
                reason: reason || 'Administrative status change',
                createdAt: Date.now()
            });
        });
        return { status: "success" };
    } catch (e) {
        console.error("updateUserStatusAtomic failure:", e);
        throw new HttpsError('internal', `User Status update failure: ${e.message}`);
    }
});

/**
 * ping
 * Health check for the Trusted API.
 */
exports.ping = onCall(async (request) => {
    return {
        status: "online",
        timestamp: Date.now(),
        version: "v2.1.0-atomic"
    };
});
