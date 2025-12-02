# Firestore Index Setup Instructions

## Problem
When signing up a student, you're getting this error:
```
FAILED_PRECONDITION: The query requires a COLLECTION_GROUP_ASC index for collection profiles and field lrn.
```

This happens because Firestore needs an index for collection group queries that check LRN uniqueness across all user profiles.

## Solution: Create the Index

You have **two options** to create the required index:

### Option 1: Use the Direct Link (Easiest)
1. Click on the link provided in the error message, OR
2. Go to: https://console.firebase.google.com/project/online-examination-8372d/firestore/indexes
3. Click "Create Index" or use the link from the error message
4. The index configuration should be:
   - **Collection ID**: `profiles` (Collection Group)
   - **Fields to index**:
     - Field: `lrn`
     - Order: Ascending
5. Click "Create"
6. Wait for the index to build (usually takes a few minutes)

### Option 2: Use Firebase CLI (If you have it set up)
1. Make sure you have `firestore.indexes.json` in your project root (we'll create this)
2. Run: `firebase deploy --only firestore:indexes`
3. Wait for the index to build

## Index Configuration Details

The index needed is:
- **Collection Group**: `profiles`
- **Field**: `lrn`
- **Query Scope**: Collection Group (searches across all `profiles` subcollections)
- **Order**: Ascending

## After Creating the Index

1. The index will take a few minutes to build
2. You can check the status in Firebase Console → Firestore → Indexes
3. Once it shows "Enabled", try signing up again
4. The error should be resolved

## Why This Index is Needed

The app checks if an LRN (Learner Reference Number) already exists across all student profiles using a collection group query:

```kotlin
firestore.collectionGroup("profiles")
    .whereEqualTo("lrn", lrn)
    .limit(1)
    .get()
```

This query searches across all `profiles` subcollections under any `users/{userId}/profiles/` path, which requires a collection group index.

