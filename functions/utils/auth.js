const { HttpsError } = require('firebase-functions/v2/https');

/**
 * verifyAdmin
 * Authenticates the caller and verifies RBAC permissions.
 *
 * @param {import('firebase-functions/v2/https').CallableRequest} request
 * @param {import('firebase-admin/firestore').Firestore} db
 * @param {string} [requiredPermission]
 * @returns {Promise<Object>} The admin document data
 */
async function verifyAdmin(request, db, requiredPermission = null) {
    if (!request.auth) {
        throw new HttpsError('unauthenticated', 'Identity handshake required.');
    }

    const adminUid = request.auth.uid;
    const adminDoc = await db.collection('admins').doc(adminUid).get();

    if (!adminDoc.exists) {
        throw new HttpsError('permission-denied', 'Identity not registered in Administrative Registry.');
    }

    const adminData = adminDoc.data();

    if (!adminData.isActive) {
        throw new HttpsError('permission-denied', 'Administrative identity has been deactivated.');
    }

    // Role-based check
    if (adminData.role === 'SUPER_ADMIN') {
        return { ...adminData, uid: adminUid };
    }

    if (requiredPermission && (!adminData.permissions || !adminData.permissions.includes(requiredPermission))) {
        throw new HttpsError('permission-denied', `Insufficient clearance. Required: ${requiredPermission}`);
    }

    return { ...adminData, uid: adminUid };
}

module.exports = { verifyAdmin };
