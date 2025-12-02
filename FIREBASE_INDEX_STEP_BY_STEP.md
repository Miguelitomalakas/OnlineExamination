# Step-by-Step: Creating Firestore Index

## Method 1: Using the Error Link (Pinakamadali)

1. **I-click ang link na nasa error message** (yung mahabang URL)
   - O kaya pumunta sa: https://console.firebase.google.com/project/online-examination-8372d/firestore/indexes?create_composite=Clpwcm9qZWN0cy9vbmxpbmUtZXhhbWluYXRpb24tODM3MmQvZGF0YWJhc2VzLyhkZWZhdWx0KS9jb2xsZWN0aW9uR3JvdXBzL3Byb2ZpbGVzL2ZpZWxkcy9scm4QAQ

2. **Kung may lalabas na form, punan ang mga sumusunod:**

   **Collection ID:**
   - Type: `profiles`
   - ⚠️ IMPORTANTE: I-check ang checkbox na **"Collection group"** (hindi lang "Collection")

   **Query scope:**
   - Piliin: **"Collection group"** (hindi "Collection")

   **Fields to index:**
   - Click "Add field"
   - **Field path:** `lrn`
   - **Order:** `Ascending` (o `Ascending`)
   - Click "Done" o "Add"

3. **I-click ang "Create" button**

4. **Hintayin ang index build** (mga 2-5 minutes)
   - Makikita mo ang status: "Building" → "Enabled"
   - Kapag "Enabled" na, pwede na mag-signup ulit

---

## Method 2: Manual Creation (Kung walang link)

1. **Pumunta sa Firebase Console:**
   - https://console.firebase.google.com/project/online-examination-8372d/firestore/indexes

2. **I-click ang "Create Index" button** (sa taas)

3. **Sa form na lalabas, punan:**

   **Step 1: Collection ID**
   ```
   profiles
   ```
   - ✅ **I-check ang checkbox:** "Collection group" (IMPORTANTE!)

   **Step 2: Query scope**
   - Piliin: **"Collection group"**

   **Step 3: Fields to index**
   - Click "Add field"
   - **Field path:** `lrn`
   - **Order:** `Ascending`
   - Click "Done"

4. **I-click "Create"**

5. **Hintayin na matapos ang build** (mga 2-5 minutes)

---

## Visual Guide ng Form Fields:

```
┌─────────────────────────────────────────┐
│ Create Index                            │
├─────────────────────────────────────────┤
│                                         │
│ Collection ID:                          │
│ ┌─────────────────────────────────┐    │
│ │ profiles                        │    │
│ └─────────────────────────────────┘    │
│ ☑ Collection group  ← CHECK THIS!      │
│                                         │
│ Query scope:                            │
│ ○ Collection                            │
│ ● Collection group  ← SELECT THIS!      │
│                                         │
│ Fields to index:                         │
│ ┌─────────────────────────────────┐    │
│ │ Field path: lrn                │    │
│ │ Order: Ascending ▼             │    │
│ └─────────────────────────────────┘    │
│                                         │
│ [Cancel]  [Create] ← CLICK THIS        │
└─────────────────────────────────────────┘
```

---

## Important Reminders:

1. ✅ **Collection group** dapat naka-check (hindi lang "Collection")
2. ✅ **Field path** ay `lrn` (lowercase, walang spaces)
3. ✅ **Order** ay `Ascending`
4. ⏳ **Hintayin** na matapos ang build bago mag-try ulit

---

## Paano malaman kung tapos na:

1. Pumunta sa: https://console.firebase.google.com/project/online-examination-8372d/firestore/indexes
2. Hanapin ang index na may:
   - Collection: `profiles`
   - Field: `lrn`
3. Tignan ang **Status** column:
   - "Building" = Hindi pa tapos, hintay pa
   - "Enabled" = ✅ Tapos na! Pwede na mag-signup

---

## Troubleshooting:

**Q: Hindi ko makita ang "Collection group" checkbox?**
- A: Siguraduhin na sa **Firestore Indexes** page ka, hindi sa Rules o Data page.

**Q: Error pa rin after creating index?**
- A: Hintayin na maging "Enabled" ang status. Minsan kailangan ng refresh.

**Q: Walang lalabas na form pag-click ng link?**
- A: Try mo i-manual create gamit ang Method 2.

