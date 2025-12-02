# Setup Checklist - Things You Need to Do

## 🔴 CRITICAL: Firebase Configuration (YOU MUST DO THIS)

### 1. Enable Email/Password Authentication
- [ ] Go to Firebase Console: https://console.firebase.google.com/
- [ ] Select your project
- [ ] Go to **Authentication** → **Sign-in method**
- [ ] Click **Email/Password**
- [ ] Enable the toggle for **Email/Password**
- [ ] Click **Save**

**Without this, email verification will NOT work!**

### 2. Verify google-services.json
- [ ] Check if `app/google-services.json` exists
- [ ] If not, download it from Firebase Console → Project Settings → Your apps → Android app
- [ ] Place it in the `app/` folder (same level as `build.gradle.kts`)

### 3. Configure Firestore Security Rules
- [ ] Go to Firebase Console → **Firestore Database** → **Rules**
- [ ] Update rules to allow users to read/write their own data
- [ ] See `FIREBASE_SETUP_GUIDE.md` for the exact rules
- [ ] Click **Publish**

### 4. (Optional) Customize Email Templates
- [ ] Go to Firebase Console → **Authentication** → **Templates**
- [ ] Customize email verification template if desired
- [ ] Customize password reset template if desired

## ✅ What I Fixed in the Code

### 1. Date Picker Calendar
- ✅ Fixed the date picker to show calendar dialog when clicked
- ✅ Made the entire field clickable (not just the icon)
- ✅ Calendar dialog now appears properly

### 2. Email Verification Code
- ✅ Code is already correct - it calls `user.sendEmailVerification().await()`
- ✅ The issue is that Firebase Authentication needs to be configured (see above)

## 📋 Step-by-Step Instructions

### Step 1: Enable Firebase Authentication
1. Open Firebase Console
2. Select your project
3. Click **Authentication** in left sidebar
4. Click **Get Started** (if first time)
5. Click **Sign-in method** tab
6. Click **Email/Password**
7. Enable **Email/Password** toggle
8. Click **Save**

### Step 2: Test Email Verification
1. Build and run your app
2. Create a new account with a real email address
3. Check your email inbox (and spam folder)
4. You should receive an email from Firebase
5. Click the verification link
6. Return to app and sign in

## 🔍 Troubleshooting

### If calendar still doesn't appear:
- Make sure you're clicking on the date field or the calendar icon
- The dialog should appear as an AlertDialog with a DatePicker inside
- If it doesn't work, try clicking the calendar icon on the right side of the field

### If emails are not being sent:
1. **Check Firebase Console** → **Authentication** → **Sign-in method** - Make sure Email/Password is enabled
2. **Check spam folder** - Firebase emails sometimes go to spam
3. **Wait a few minutes** - First email might take a few minutes
4. **Check email address** - Make sure you're using a valid email
5. **Check Firebase quotas** - Free tier has limits but should be enough for testing

### If verification link doesn't work:
- The link should open in a browser first
- Then redirect to your app
- Make sure your app is installed on the device
- Check that `google-services.json` has the correct package name

## 📝 Summary

**What's working now:**
- ✅ Date picker calendar dialog (fixed)
- ✅ Email verification code (already correct)
- ✅ Password visibility toggle
- ✅ Form validation
- ✅ PSGC dropdowns

**What YOU need to do:**
- 🔴 Enable Email/Password authentication in Firebase Console (REQUIRED)
- 🔴 Verify google-services.json is in place (REQUIRED)
- 🔴 Set up Firestore security rules (REQUIRED)
- ⚪ Customize email templates (optional)

## 🚀 Quick Start

1. **Enable Firebase Authentication** (5 minutes)
   - Firebase Console → Authentication → Sign-in method → Email/Password → Enable → Save

2. **Test the app** (2 minutes)
   - Build and run
   - Create account
   - Check email for verification link

3. **Done!** ✅

---

**Need help?** Check `FIREBASE_SETUP_GUIDE.md` for detailed instructions.

