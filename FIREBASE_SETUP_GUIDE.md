# Firebase Setup Guide for Email Verification

This guide will help you configure Firebase Authentication to enable email verification and password reset functionality.

## ✅ Step 1: Enable Email/Password Authentication in Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project (or create a new one if you haven't)
3. Navigate to **Authentication** in the left sidebar
4. Click on **Get Started** (if it's your first time)
5. Click on the **Sign-in method** tab
6. Click on **Email/Password**
7. Enable the first toggle (**Email/Password**)
8. **DO NOT** enable "Email link (passwordless sign-in)" unless you want that feature
9. Click **Save**

## ✅ Step 2: Configure Email Templates (Optional but Recommended)

1. In Firebase Console, go to **Authentication** → **Templates** tab
2. You'll see two templates:
   - **Email address verification**
   - **Password reset**

3. Click on **Email address verification**:
   - You can customize the email subject and body
   - Make sure the email contains: `{{ .LINK }}` - this is the verification link
   - Default template should work, but you can customize it

4. Click on **Password reset**:
   - Customize if needed
   - Make sure it contains: `{{ .LINK }}` - this is the reset link

5. Click **Save** for each template

## ✅ Step 3: Authorized Domains

1. In Firebase Console, go to **Authentication** → **Settings** tab
2. Scroll down to **Authorized domains**
3. Make sure these domains are listed:
   - `your-project-id.firebaseapp.com` (automatically added)
   - `your-project-id.web.app` (automatically added)
   - `localhost` (for development)
   - Your custom domain (if you have one)

## ✅ Step 4: Firestore Security Rules (Important!)

1. Go to **Firestore Database** in Firebase Console
2. Click on the **Rules** tab
3. Update the rules to allow users to read/write their own data:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read/write their own user document
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // Users can read/write their own profile documents
      match /profiles/{profileId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
    
    // Allow querying profiles for LRN validation (read-only)
    match /{path=**}/profiles/{profileId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && resource.data.lrn != null 
        && request.resource.data.lrn == resource.data.lrn;
    }
  }
}
```

4. Click **Publish**

## ✅ Step 5: Verify google-services.json is in Place

1. Make sure `google-services.json` is in the `app/` folder
2. The file should be downloaded from Firebase Console → Project Settings → Your apps → Android app
3. If you haven't downloaded it yet:
   - Go to Firebase Console → Project Settings (gear icon)
   - Scroll down to "Your apps"
   - Click on your Android app (or add one if it doesn't exist)
   - Download `google-services.json`
   - Place it in `app/` folder (same level as `build.gradle.kts`)

## ✅ Step 6: Test Email Verification

1. Build and run your app
2. Create a new account
3. Check the email you used for signup
4. You should receive a verification email from Firebase
5. Click the verification link in the email
6. Return to the app and try to sign in

## 🔍 Troubleshooting

### Emails not being sent?

1. **Check spam folder** - Firebase emails sometimes go to spam
2. **Check Firebase Console** → **Authentication** → **Users** - see if the user was created
3. **Check email format** - Make sure you're using a valid email format
4. **Check Firebase quotas** - Free tier has limits, but should be enough for testing
5. **Check email provider** - Some email providers (like corporate emails) might block Firebase emails

### Verification email link not working?

1. Make sure the app is using the correct package name in `google-services.json`
2. Check if the link opens in a browser - it should redirect to your app
3. For Android, you might need to add an intent filter in `AndroidManifest.xml` (usually handled automatically)

### "User not found" or "Invalid email" errors?

1. Make sure Email/Password authentication is enabled in Firebase Console
2. Check that the email format is correct (e.g., `user@example.com`)
3. Verify that `google-services.json` is properly configured

## 📱 Additional Android Setup (if needed)

If email verification links don't open your app automatically, you may need to add this to `AndroidManifest.xml`:

```xml
<activity
    android:name=".MainActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
    
    <!-- Add this for email verification links -->
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="https"
            android:host="your-project-id.firebaseapp.com" />
    </intent-filter>
</activity>
```

Replace `your-project-id` with your actual Firebase project ID.

## ✅ Checklist

- [ ] Email/Password authentication enabled in Firebase Console
- [ ] Email templates configured (optional)
- [ ] `google-services.json` file in `app/` folder
- [ ] Firestore security rules updated
- [ ] Test account created
- [ ] Verification email received
- [ ] Email verification link works

## 🎉 You're Done!

Once you complete these steps, email verification should work. Users will receive verification emails when they sign up, and they need to verify their email before they can sign in.

---

**Note**: The first time you send emails, Firebase might take a few minutes to process. Subsequent emails should be sent immediately.

