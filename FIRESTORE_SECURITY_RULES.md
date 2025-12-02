# Firestore Security Rules Setup Guide

## Problem
Kapag nag-signup, may "permission denied" error dahil hindi pa naka-configure ang Firestore security rules.

## Solution
I-update ang Firestore security rules sa Firebase Console.

## Steps to Fix:

### 1. Buksan ang Firebase Console
- Pumunta sa https://console.firebase.google.com/
- Piliin ang iyong project

### 2. Pumunta sa Firestore Database
- Sa left sidebar, click **Firestore Database**
- Click sa tab na **Rules** (sa taas)

### 3. I-copy at i-paste ang sumusunod na rules:

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
    
    // LRN index collection (used for LRN uniqueness check - no index needed)
    match /lrn_index/{lrn} {
      // Allow read for authenticated users (needed for LRN check during signup)
      allow read: if request.auth != null;
      // Allow create for authenticated users (when creating student account)
      allow create: if request.auth != null;
      // Note: Update and delete should be restricted to admins only
    }
  }
}
```

### 4. I-click ang **Publish** button
- Matapos i-paste ang rules, i-click ang **Publish** button sa taas
- Hintayin na ma-save ang rules (mga ilang segundo)

### 5. Testing
- Subukan ulit ang signup
- Dapat gumana na ngayon

## Important Notes:

⚠️ **Security Warning**: Ang rules na ito ay para sa development/testing lang. Para sa production:
- Dagdagan ng mas strict na validation
- I-validate ang data structure
- I-limit ang fields na pwedeng i-update
- I-consider ang role-based access control

## Alternative: Temporary Open Rules (Development Only)

Kung gusto mong mabilis na ma-test, pwede mong gamitin ang temporary open rules (HUWAG GAMITIN SA PRODUCTION):

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

⚠️ **WARNING**: Ang rules na ito ay nagbibigay ng access sa lahat ng authenticated users. Gamitin lang para sa testing!

## Troubleshooting:

1. **"Permission denied" pa rin?**
   - Siguraduhin na na-publish mo na ang rules
   - Check kung authenticated na ang user (dapat authenticated na after Firebase Auth)
   - I-refresh ang app

2. **"Missing or insufficient permissions"?**
   - Check kung tama ang structure ng rules
   - Siguraduhin na `rules_version = '2';` ang nasa unang line

3. **Collection group query error?**
   - Siguraduhin na may index para sa collection group query
   - Firebase Console ay magpapakita ng link para sa missing index

## Next Steps:

Pagkatapos ma-fix ang security rules:
1. Subukan ang signup para sa lahat ng roles (Student, Teacher, Admin)
2. I-verify na na-save ang data sa Firestore
3. I-check ang Firestore Database para makita ang saved data

