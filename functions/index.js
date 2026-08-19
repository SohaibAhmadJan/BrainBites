/**
 * BrainBites Admin API
 * Trusted Backend Mutations with Atomic Auditing.
 */

const { initializeApp } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const { onCall, HttpsError } = require('firebase-functions/v2/https');
const { verifyAdmin } = require('./utils/auth');

initializeApp();
const db = getFirestore();

/**
 * updateFactAtomic
 * Atomic Fact Mutation + Audit Log
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

            // 1. Write Fact
            transaction.set(factRef, {
                ...data,
                updatedAt: Date.now()
            }, { merge: true });

            // 2. Write Audit Log
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
        throw new HttpsError('internal', 'Cloud transaction failed.');
    }
});

/**
 * deleteFactAtomic
 * Atomic Fact Deletion + Audit Log
 */
exports.deleteFactAtomic = onCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'manage.content');
    const { id, reason } = request.data;

    if (!id) throw new HttpsError('invalid-argument', 'Missing fact ID.');

    try {
        await db.runTransaction(async (transaction) => {
            const factRef = db.collection('facts').doc(id);
            const factSnapshot = await transaction.get(factRef);

            if (!factSnapshot.exists) throw new Error('Fact not found.');

            const beforeData = factSnapshot.data();

            // 1. Delete Fact
            transaction.delete(factRef);

            // 2. Write Audit Log
            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: admin.uid,
                action: 'DELETE_FACT',
                targetType: 'FACT',
                targetId: id,
                before: beforeData,
                after: null,
                reason: reason || 'Manual expunge',
                createdAt: Date.now()
            });
        });
        return { status: "success", factId: id };
    } catch (e) {
        throw new HttpsError('internal', e.message);
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
        throw new HttpsError('internal', e.message);
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
            if (!snapshot.exists) throw new Error('Category not found.');

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
        throw new HttpsError('internal', e.message);
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
        throw new HttpsError('internal', e.message);
    }
});

/**
 * updateAdminAtomic
 */
exports.updateAdminAtomic = onCall(async (request) => {
    const caller = await verifyAdmin(request, db, 'manage.admins');
    const { uid, data, reason } = request.data;

    if (!uid || !data) throw new HttpsError('invalid-argument', 'Invalid payload.');

    // A. SELF-ESCALATION PROTECTION
    if (caller.uid === uid && (data.role || data.permissions)) {
        throw new HttpsError('permission-denied', 'Self-modification of protocol roles or clearance levels is prohibited.');
    }

    try {
        return await db.runTransaction(async (transaction) => {
            const adminRef = db.collection('admins').doc(uid);
            const snapshot = await transaction.get(adminRef);
            const beforeData = snapshot.exists ? snapshot.data() : null;

            // Security: Only SUPER_ADMIN can modify other SUPER_ADMINs
            if (beforeData && beforeData.role === 'SUPER_ADMIN' && caller.role !== 'SUPER_ADMIN') {
                throw new Error('Insufficient clearance to modify a SUPER_ADMIN.');
            }

            // B. LAST SUPER_ADMIN PROTECTION (Deactivation or Role Change)
            if (beforeData && beforeData.role === 'SUPER_ADMIN' && beforeData.isActive) {
                const isLosingSuperStatus = data.isActive === false || (data.role && data.role !== 'SUPER_ADMIN');

                if (isLosingSuperStatus) {
                    const activeSuperAdmins = await db.collection('admins')
                        .where('role', '==', 'SUPER_ADMIN')
                        .where('isActive', '==', true)
                        .get();

                    if (activeSuperAdmins.size <= 1) {
                        throw new Error('Critical Protocol Failure: Cannot deactivate the final active SUPER_ADMIN node.');
                    }
                }
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
        throw new HttpsError('internal', e.message);
    }
});

/**
 * deleteAdminAtomic
 */
exports.deleteAdminAtomic = onCall(async (request) => {
    const caller = await verifyAdmin(request, db, 'manage.admins');
    const { uid, reason } = request.data;

    if (!uid) throw new HttpsError('invalid-argument', 'Missing UID.');

    // A. SELF-DELETION PROTECTION
    if (caller.uid === uid) {
        throw new HttpsError('permission-denied', 'Self-expungement from registry is prohibited via this interface.');
    }

    try {
        return await db.runTransaction(async (transaction) => {
            const ref = db.collection('admins').doc(uid);
            const snapshot = await transaction.get(ref);
            if (!snapshot.exists) throw new Error('Admin not found.');

            const targetData = snapshot.data();

            if (targetData.role === 'SUPER_ADMIN' && caller.role !== 'SUPER_ADMIN') {
                throw new Error('Insufficient clearance.');
            }

            // B. LAST SUPER_ADMIN PROTECTION
            if (targetData.role === 'SUPER_ADMIN' && targetData.isActive) {
                const activeSuperAdmins = await db.collection('admins')
                    .where('role', '==', 'SUPER_ADMIN')
                    .where('isActive', '==', true)
                    .get();

                if (activeSuperAdmins.size <= 1) {
                    throw new Error('Critical Protocol Failure: Cannot remove the final active SUPER_ADMIN node.');
                }
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
        throw new HttpsError('internal', e.message);
    }
});

/**
 * sendGlobalNotificationAtomic
 */
exports.sendGlobalNotificationAtomic = onCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'manage.content');
    const { data, reason } = request.data;

    try {
        const notifRef = db.collection('notifications').doc();
        const notification = {
            ...data,
            id: notifRef.id,
            timestamp: Date.now()
        };

        await db.runTransaction(async (transaction) => {
            transaction.set(notifRef, notification);

            const auditRef = db.collection('audit_logs').doc();
            transaction.set(auditRef, {
                adminUid: admin.uid,
                action: 'SEND_NOTIFICATION',
                targetType: 'NOTIFICATION',
                targetId: notifRef.id,
                before: null,
                after: notification,
                reason: reason || 'Broadcast dispatch',
                createdAt: Date.now()
            });
        });
        return { status: "success", notificationId: notifRef.id };
    } catch (e) {
        throw new HttpsError('internal', e.message);
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
            if (!snapshot.exists) throw new Error('Notification not found.');

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
        throw new HttpsError('internal', e.message);
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
        throw new HttpsError('internal', e.message);
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
        throw new HttpsError('internal', e.message);
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
            if (!snapshot.exists) throw new Error('Quiz not found.');

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
        throw new HttpsError('internal', e.message);
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
        throw new HttpsError('internal', e.message);
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
        throw new HttpsError('internal', e.message);
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
        throw new HttpsError('internal', e.message);
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
        throw new HttpsError('internal', e.message);
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
            if (!snapshot.exists) throw new Error('Quote not found.');

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
        throw new HttpsError('internal', e.message);
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
        throw new HttpsError('internal', e.message);
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

            // 1. Grant Achievement
            transaction.set(earnedRef, achievementData);

            // 2. Update Count
            transaction.update(userRef, {
                'stats.achievementsCount': (userSnapshot.data().stats?.achievementsCount || 0) + 1,
                'account.updatedAt': Date.now()
            });

            // 3. Audit
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
        throw new HttpsError('internal', e.message);
    }
});
