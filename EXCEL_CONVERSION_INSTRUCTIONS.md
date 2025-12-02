# PSGC Excel to Kotlin Conversion Instructions

## Overview
This guide will help you convert your PSGC Excel file into Kotlin code that can be used in the Android app.

## Step 1: Install Python Dependencies

You need Python installed on your computer. Then install the required libraries:

```bash
pip install openpyxl pandas
```

**Note:** If you don't have Python, you can:
- Download from: https://www.python.org/downloads/
- Or use an online Excel to JSON converter and I'll help you convert it differently

## Step 2: Run the Conversion Script

1. **Place your Excel file** in the project root directory (same folder as this script)
2. **Open terminal/command prompt** in the project root
3. **Run the script:**

```bash
python convert-excel-to-kotlin.py <your_excel_file.xlsx>
```

**Example:**
```bash
python convert-excel-to-kotlin.py psgc_data.xlsx
```

## Step 3: Review the Generated File

The script will create a file called `PsgcData_generated.kt` with all the provinces and municipalities.

## Step 4: Replace the Existing File

1. **Backup the current file** (optional but recommended):
   - Copy `app/src/main/java/com/onlineexamination/data/model/PsgcData.kt` to `PsgcData_backup.kt`

2. **Replace the content:**
   - Open `PsgcData_generated.kt`
   - Copy all the content
   - Open `app/src/main/java/com/onlineexamination/data/model/PsgcData.kt`
   - Replace all content with the generated code
   - Save the file

## Step 5: Rebuild Project

1. In Android Studio: **File > Sync Project with Gradle Files**
2. Or run: `./gradlew build`

## Troubleshooting

### If the script can't find columns:
The script tries to auto-detect columns, but if your Excel file has different column names, you may need to:

1. **Check the column names** shown by the script
2. **Update the script** to match your Excel file's column names
3. Or **tell me the column names** and I'll update the script for you

### Common Excel Column Names:
- **Code/PSGC Code**: Usually named "PSGC Code", "Code", "PSGC", "ID"
- **Province**: Usually named "Province", "Province Name", "Prov"
- **Municipality**: Usually named "Municipality", "City", "Municipality/City", "City/Municipality"

### If Python is not installed:
You can:
1. Install Python from https://www.python.org/downloads/
2. Or send me the Excel file structure (column names and a few sample rows) and I'll create a different solution

## What the Script Does

1. Reads your Excel file
2. Identifies columns for codes, provinces, and municipalities
3. Organizes data by province → municipalities
4. Generates Kotlin code in the correct format
5. Saves to `PsgcData_generated.kt`

## After Conversion

Once you've replaced the file:
- ✅ All provinces will be available in the dropdown
- ✅ Municipalities will load based on selected province
- ✅ The dropdown will work with all Philippine provinces

## Need Help?

If you encounter any issues:
1. Share the Excel file column names
2. Share any error messages
3. I'll help you fix the script or provide an alternative solution

