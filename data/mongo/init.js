// RED — MongoDB bootstrap (data plane).
//
// Runs exactly once, when the MongoDB data volume is empty. It contains NO
// credentials: the application user and password are injected by the runtime
// (platform/compose/docker-compose.yml) from contracts/env.schema.json.
//
// The application connects with authSource=admin, so the user is created in the
// admin database with roles scoped to the RED database only.
//
// Idempotent by design: re-running on an existing deployment must not fail.

const appDbName = process.env.MONGO_APP_DB || process.env.MONGO_INITDB_DATABASE || 'red_sovereign';
const appUser = process.env.MONGO_APP_USER || process.env.MONGO_USER || 'red_user';
const appPassword = process.env.MONGO_APP_PASSWORD || process.env.MONGO_PASSWORD;

const appDb = db.getSiblingDB(appDbName);

if (!appPassword) {
  print('[RED] MONGO_APP_PASSWORD is not set: skipping application user creation.');
} else {
  const admin = db.getSiblingDB('admin');
  const existing = admin.getUser(appUser);
  if (existing) {
    admin.updateUser(appUser, {
      pwd: appPassword,
      roles: [
        { role: 'readWrite', db: appDbName },
        { role: 'dbAdmin', db: appDbName }
      ]
    });
    print(`[RED] MongoDB user '${appUser}' updated.`);
  } else {
    admin.createUser({
      user: appUser,
      pwd: appPassword,
      roles: [
        { role: 'readWrite', db: appDbName },
        { role: 'dbAdmin', db: appDbName }
      ]
    });
    print(`[RED] MongoDB user '${appUser}' created.`);
  }
}

function ensureCollection(name) {
  if (!appDb.getCollectionNames().includes(name)) {
    appDb.createCollection(name);
    print(`[RED] collection '${name}' created.`);
  }
}

['messages', 'stories', 'groups', 'calls', 'users'].forEach(ensureCollection);

// Durable delivery: a message is unique per conversation sequence.
appDb.messages.createIndex({ conversationId: 1, sequenceNumber: 1 }, { unique: true });
appDb.messages.createIndex({ uuid: 1 }, { unique: true });
appDb.messages.createIndex({ senderId: 1 });
appDb.messages.createIndex({ createdAt: 1 });

// Stories disappear 24h after expiresAt (server-side TTL).
appDb.stories.createIndex({ expiresAt: 1 }, { expireAfterSeconds: 0 });
appDb.stories.createIndex({ userId: 1 });

appDb.groups.createIndex({ groupId: 1 }, { unique: true });

// RED identity is username based: no phone, no email (see W0 identity invariants).
appDb.users.createIndex({ username: 1 }, { unique: true });

print('[RED] MongoDB bootstrap complete (indexes + TTL).');
