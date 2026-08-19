# BrainBites Admin Panel - Production Documentation

## System Architecture
The BrainBites Admin Panel is a high-security administrative interface built with React, TypeScript, and Vite. It follows a **Zero-Trust Administrative Architecture**, where the browser has zero direct-write permissions to global collections.

### 1. Trusted Backend (API)
All administrative mutations (Facts, Quizzes, Users, Admins) are routed through **Firebase Cloud Functions**. 
- **Atomic Operations**: Mutations are bundled with audit logging in single Firestore transactions.
- **RBAC Enforcement**: The backend verifies administrative identities (`admins/{uid}`) and specific permissions before every operation.

### 2. Identity & Roles
The system uses the following RBAC hierarchy:
- **SUPER_ADMIN**: Full authoritative control over the registry and content.
- **ADMIN**: Content management and user oversight.
- **CONTENT_MANAGER**: Curated insight management (Facts, Quizzes).
- **ANALYST**: Read-only visibility into telemetry and audit streams.

---

## Operational Procedures

### Local Execution
To run the Admin Panel in development mode:
```bash
cd F:/webBasedAdminPanel
npm run dev
```

### Production Build
To generate the production bundle:
```bash
cd F:/webBasedAdminPanel
npm run build
```

### Deployment
To deploy the entire ecosystem (Functions, Rules, Indexes, and Hosting):
```bash
# From F:/BrainBites root
firebase login
firebase use brainbites-24332456
firebase deploy --only functions,firestore:rules,firestore:indexes,hosting
```

---

## Security Safeguards
1. **Self-Escalation**: Administrators cannot modify their own roles or permissions via the API.
2. **Last Admin Guard**: The system will block any attempt to deactivate or delete the final active `SUPER_ADMIN` to prevent permanent registry lockout.
3. **Audit Immutability**: Audit logs are created by the server and cannot be modified or removed via the Admin Panel UI.

---

## Emergency Recovery
If the primary `SUPER_ADMIN` account is lost:
1. Access the **Firebase Console** (Database tab).
2. Manually locate a trusted user's document in the `admins/` collection.
3. Set `role: "SUPER_ADMIN"` and `isActive: true`.
4. Re-log into the Admin Panel to restore registry control.
