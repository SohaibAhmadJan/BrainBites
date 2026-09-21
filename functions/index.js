/**
 * BrainBites Admin API
 * Trusted Backend Mutations with Atomic Auditing.
 */

const { initializeApp } = require('firebase-admin/app');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');
const { getAuth } = require('firebase-admin/auth');
const { getMessaging } = require('firebase-admin/messaging');
const { onCall, HttpsError } = require('firebase-functions/v2/https');
const { verifyAdmin } = require('./utils/auth');
const nodemailer = require('nodemailer');

// Set up Nodemailer transporter using Gmail SMTP
const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: process.env.SMTP_EMAIL || 'ahmedjansohaib8@gmail.com',
        pass: process.env.SMTP_PASSWORD || 'your_app_password_here'
    }
});

initializeApp();
const db = getFirestore();

const CORS_CONFIG = {
    cors: [
        "https://brainbites-24332456.web.app",
        "https://brainbites-24332456.firebaseapp.com"
    ]
};

function secureOnCall(handler) {
    return secureOnCall(CORS_CONFIG, async (request) => {
        try {
            return await handler(request);
        } catch (e) {
            if (e instanceof HttpsError) {
                if (e.code === 'internal') {
                    const correlationId = Math.random().toString(36).substring(2, 10);
                    console.error(`[Error Ref: ${correlationId}] Internal HttpsError:`, e);
                    throw new HttpsError('internal', `An internal server error occurred. Reference ID: ${correlationId}`);
                }
                throw e;
            }
            const correlationId = Math.random().toString(36).substring(2, 10);
            console.error(`[Error Ref: ${correlationId}] Unhandled failure:`, e);
            throw new HttpsError('internal', `An internal server error occurred. Reference ID: ${correlationId}`);
        }
    });
}


/**
 * requestEmailVerification
 * Generates a 6-digit OTP, saves it to Firestore, and emails it to the user.
 */
exports.requestEmailVerification = onCall(async (request) => {
    const { email } = request.data;
    if (!email) {
        throw new HttpsError('invalid-argument', 'Email is required.');
    }

    try {
        // 1. Check if user already exists
        try {
            await getAuth().getUserByEmail(email);
            throw new HttpsError('already-exists', 'Account already exists. Please log in.');
        } catch (error) {
            if (error.code !== 'auth/user-not-found') {
                throw error;
            }
            // User does not exist, which is what we want for sign up.
        }

        // 2. Generate 6-digit OTP
        const otp = Math.floor(100000 + Math.random() * 900000).toString();
        const expiresAt = Date.now() + 15 * 60 * 1000; // 15 minutes

        // 3. Save OTP to Firestore
        await db.collection('otp_codes').doc(email.toLowerCase()).set({
            otp: otp,
            expiresAt: expiresAt,
            attempts: 0
        });

        // 4. Send Email via NodeMailer
        const mailOptions = {
            from: `"BrainBites" <${process.env.SMTP_EMAIL || 'ahmedjansohaib8@gmail.com'}>`,
            to: email,
            subject: 'Your BrainBites Verification Code',
            text: `Welcome to BrainBites!\n\nYour 6-digit verification code is: ${otp}\n\nThis code will expire in 15 minutes.\n\nIf you did not request this, please ignore this email.`,
            html: `
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;">
                    <h2 style="color: #2D6A4F; text-align: center;">Welcome to BrainBites!</h2>
                    <p style="font-size: 16px; color: #333;">Please use the following 6-digit verification code to complete your sign-up:</p>
                    <div style="background-color: #f5f5f5; padding: 15px; text-align: center; border-radius: 8px; margin: 20px 0;">
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #1a1a1a;">${otp}</span>
                    </div>
                    <p style="font-size: 14px; color: #666;">This code will expire in 15 minutes.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;" />
                    <p style="font-size: 12px; color: #999; text-align: center;">If you did not request this verification, please ignore this email.</p>
                </div>
            `
        };

        await transporter.sendMail(mailOptions);

        return { status: "success", message: "OTP sent successfully." };
    } catch (e) {
        console.error("requestEmailVerification failure:", e);
        if (e instanceof HttpsError) throw e;
        throw new HttpsError('internal', `Failed to process request: ${e.message}`);
    }
});

/**
 * verifyOtpAndSignUp
 * Validates the OTP, creates the Auth user, and issues a Custom Token.
 */
exports.verifyOtpAndSignUp = onCall(async (request) => {
    const { email, password, name, otp } = request.data;

    if (!email || !password || !name || !otp) {
        throw new HttpsError('invalid-argument', 'Missing required fields.');
    }

    const normalizedEmail = email.toLowerCase();

    try {
        // 1. Fetch OTP record
        const otpRef = db.collection('otp_codes').doc(normalizedEmail);
        const otpDoc = await otpRef.get();

        if (!otpDoc.exists) {
            throw new HttpsError('not-found', 'No pending verification found. Please request a new code.');
        }

        const otpData = otpDoc.data();

        // 2. Validate Expiration & Attempts
        if (Date.now() > otpData.expiresAt) {
            await otpRef.delete();
            throw new HttpsError('deadline-exceeded', 'Verification code has expired. Please request a new one.');
        }

        if (otpData.attempts >= 5) {
            await otpRef.delete();
            throw new HttpsError('resource-exhausted', 'Too many failed attempts. Please request a new code.');
        }

        // 3. Verify Code
        if (otpData.otp !== otp) {
            await otpRef.update({ attempts: FieldValue.increment(1) });
            throw new HttpsError('invalid-argument', 'Incorrect verification code.');
        }

        // 4. Code is correct! Create the user in Firebase Auth
        let userRecord;
        try {
            userRecord = await getAuth().createUser({
                email: normalizedEmail,
                password: password,
                displayName: name,
                emailVerified: true // Automatically verified since they proved they own the email
            });
        } catch (createError) {
             throw new HttpsError('internal', `Failed to create user account: ${createError.message}`);
        }

        // 5. Clean up OTP document
        await otpRef.delete();

        // 6. Generate Custom Token to sign the user in on the client side
        const customToken = await getAuth().createCustomToken(userRecord.uid);

        return {
            status: "success",
            customToken: customToken,
            uid: userRecord.uid
        };

    } catch (e) {
        console.error("verifyOtpAndSignUp failure:", e);
        if (e instanceof HttpsError) throw e;
        throw new HttpsError('internal', `Verification failed: ${e.message}`);
    }
});

/**
 * updateFactAtomic
 */
exports.updateFactAtomic = secureOnCall(async (request) => {
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
exports.deleteFactAtomic = secureOnCall(async (request) => {
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
exports.updateCategoryAtomic = secureOnCall(async (request) => {
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
exports.deleteCategoryAtomic = secureOnCall(async (request) => {
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
exports.updateReportStatusAtomic = secureOnCall(async (request) => {
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
exports.updateAppConfigAtomic = secureOnCall(async (request) => {
    const admin = await verifyAdmin(request, db, 'manage.config');
    const { data, reason } = request.data;

    if (!data) {
        throw new HttpsError('invalid-argument', 'Protocol Failure: Request payload (data) is null or missing.');
    }

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
        console.error("updateAppConfigAtomic ERROR:", e);
        // Return descriptive error to help debug structural issues
        throw new HttpsError('internal', `Config Sync Protocol Failure: ${e.message || 'Unknown Server Error'}`);
    }
});

/**
 * updateAdminAtomic
 */
exports.updateAdminAtomic = secureOnCall(async (request) => {
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
exports.deleteAdminAtomic = secureOnCall(async (request) => {
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
exports.sendGlobalNotificationAtomic = secureOnCall(async (request) => {
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
            timestamp: Date.now(),
            scheduledAt: data.scheduledAt || null
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

        // Step 1: Send FCM Signal
        let fcmResponse;
        try {
            fcmResponse = await getMessaging().send(message);
        } catch (fcmErr) {
            console.error(`[Notification Service] FCM Protocol FAILURE:`, fcmErr);
            throw new Error(`Push Signal Failure: ${fcmErr.message}`);
        }

        // Step 2: Persist to Registry and Audit
        try {
            await db.runTransaction(async (transaction) => {
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
            });
        } catch (dbErr) {
            console.error(`[Notification Service] Database Persistence FAILURE:`, dbErr);
            // We don't throw here if FCM already went out, but we return a warning
            return {
                status: "partial_success",
                notificationId,
                warning: "Push sent, but database registry failed."
            };
        }

        return { status: "success", notificationId, fcmMessageId: fcmResponse };
    } catch (e) {
        console.error("[Notification Service] CRITICAL FATAL ERROR:", e);
        throw new HttpsError('internal', e.message || 'Unknown Server Error during dispatch protocol.');
    }
});

/**
 * updateQuizAtomic
 */
exports.updateQuizAtomic = secureOnCall(async (request) => {
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
exports.deleteQuizAtomic = secureOnCall(async (request) => {
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
exports.updateCollectionAtomic = secureOnCall(async (request) => {
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
exports.deleteCollectionAtomic = secureOnCall(async (request) => {
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
exports.updateAchievementAtomic = secureOnCall(async (request) => {
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
exports.deleteAchievementAtomic = secureOnCall(async (request) => {
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
exports.updateQuoteAtomic = secureOnCall(async (request) => {
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
exports.deleteQuoteAtomic = secureOnCall(async (request) => {
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
exports.bulkImportFactsAtomic = secureOnCall(async (request) => {
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
exports.resetUserStatsAtomic = secureOnCall(async (request) => {
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
exports.awardAchievementAtomic = secureOnCall(async (request) => {
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
exports.deleteNotificationAtomic = secureOnCall(async (request) => {
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
exports.updateUserStatusAtomic = secureOnCall(async (request) => {
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
