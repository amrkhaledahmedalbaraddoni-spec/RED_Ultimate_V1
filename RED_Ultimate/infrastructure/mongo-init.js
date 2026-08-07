// RED Sovereign Mongo Init - Ultimate V2
db = db.getSiblingDB('red_sovereign');

try {
  db.createUser({
    user: "red_user",
    pwd: "RED_Mongo_2026_Ultra_Secure_32!",
    roles: [
      { role: "readWrite", db: "red_sovereign" },
      { role: "dbAdmin", db: "red_sovereign" }
    ]
  });
} catch (e) {
  print("User already exists: " + e);
}

db.createCollection("messages");
db.createCollection("stories");
db.createCollection("groups");
db.createCollection("calls");
db.createCollection("users");

db.messages.createIndex({ conversationId: 1, sequenceNumber: 1 });
db.messages.createIndex({ uuid: 1 }, { unique: true });
db.messages.createIndex({ senderId: 1 });
db.messages.createIndex({ createdAt: 1 });
db.stories.createIndex({ expiresAt: 1 }, { expireAfterSeconds: 0 });
db.stories.createIndex({ userId: 1 });
db.groups.createIndex({ groupId: 1 }, { unique: true });
db.users.createIndex({ email: 1 }, { unique: true });

print("🔴 RED MongoDB Initialized - Sovereign Collections Created Ultimate V2");
