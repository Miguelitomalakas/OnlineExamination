# Updated Firestore Security Rules

## I-copy ang rules na ito at i-paste sa Firebase Console:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Users collection
    match /users/{userId} {
      // Allow read if authenticated
      allow read: if request.auth != null;
      
      // Allow create if user is creating their own document (important for signup)
      allow create: if request.auth != null && request.auth.uid == userId;
      
      // Allow update if user owns the document
      allow update: if request.auth != null && request.auth.uid == userId;
      
      // Allow delete if user owns the document (optional)
      allow delete: if request.auth != null && request.auth.uid == userId;
      
      // Profiles subcollection
      match /profiles/{profileId} {
        // Allow read if authenticated
        allow read: if request.auth != null;
        
        // Allow create if user owns the parent document (important for signup)
        allow create: if request.auth != null && request.auth.uid == userId;
        
        // Allow update if user owns the parent document
        allow update: if request.auth != null && request.auth.uid == userId;
        
        // Allow delete if user owns the parent document (optional)
        allow delete: if request.auth != null && request.auth.uid == userId;
      }
    }
    
    // Collection group query for profiles (used for LRN uniqueness check)
    // This allows reading from any profiles subcollection for authenticated users
    match /{path=**}/profiles/{profileId} {
      // Allow read for authenticated users (needed for LRN check during signup)
      allow read: if request.auth != null;
      
      // Note: Write operations are handled by the specific path rules above
    }
  }
}
```

## Key Changes from Your Current Rules:

1. **Separated `read` and `write` into `create`, `update`, `delete`** - Mas specific at mas secure
2. **Explicit `create` permission** - Important para sa signup kasi kailangan mag-create ng bagong document
3. **Simplified collection group query** - Inalis ang complex write condition na maaaring nagdudulot ng issue

## Steps:

1. Buksan ang Firebase Console → Firestore Database → Rules
2. I-replace ang buong rules ng nasa taas
3. I-click ang **Publish**
4. Subukan ulit ang signup

## Kung ayaw pa rin:

Subukan ang temporary open rules para sa testing (HUWAG GAMITIN SA PRODUCTION):

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

