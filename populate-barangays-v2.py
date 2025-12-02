"""
Extract barangay data from barangay package and populate municipalities
"""
import barangay
from collections import defaultdict
import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

print("=" * 80)
print("Extracting barangay data from barangay package...")
print("=" * 80)
sys.stdout.flush()

# Read existing PsgcData.kt
print("\n[1/4] Reading PsgcData.kt...")
sys.stdout.flush()
with open('app/src/main/java/com/onlineexamination/data/model/PsgcData.kt', 'r', encoding='utf-8') as f:
    psgc_content = f.read()

# Extract municipality codes
municipality_pattern = r'Municipality\("(\d+)",\s*"([^"]+)"\)'
municipalities = re.findall(municipality_pattern, psgc_content)
print(f"✅ Found {len(municipalities)} municipalities")
sys.stdout.flush()

# Build mapping of municipality code to barangays
print("\n[2/4] Processing barangay data...")
sys.stdout.flush()
municipality_barangays = defaultdict(list)

barangay_count = 0
for entry in barangay.BARANGAY_FLAT:
    if not isinstance(entry, dict):
        continue
    
    entry_type = entry.get('type', '').lower()
    if entry_type != 'barangay':
        continue
    
    psgc_id = str(entry.get('psgc_id', '')).strip()
    parent_id = str(entry.get('parent_psgc_id', '')).strip()
    name = entry.get('name', '').strip()
    
    if not name or not psgc_id or not parent_id:
        continue
    
    # Normalize IDs to 9 digits
    if len(psgc_id) < 9:
        psgc_id = psgc_id.ljust(9, '0')
    elif len(psgc_id) > 9:
        psgc_id = psgc_id[:9]
    
    if len(parent_id) < 9:
        parent_id = parent_id.ljust(9, '0')
    elif len(parent_id) > 9:
        parent_id = parent_id[:9]
    
    # Check if parent matches any municipality
    muni_codes = [m[0] for m in municipalities]
    if parent_id in muni_codes:
        municipality_barangays[parent_id].append({
            'code': psgc_id,
            'name': name
        })
        barangay_count += 1
        if barangay_count % 5000 == 0:
            print(f"  Processed {barangay_count} barangays...")
            sys.stdout.flush()

print(f"✅ Found {barangay_count} barangays for {len(municipality_barangays)} municipalities")
sys.stdout.flush()

# Show sample
if municipality_barangays:
    sample = list(municipality_barangays.items())[0]
    print(f"\nSample: Municipality {sample[0]} has {len(sample[1])} barangays")
    if sample[1]:
        print(f"  First: {sample[1][0]['name']}")
sys.stdout.flush()

# Replace Municipality entries with barangays
print("\n[3/4] Updating PsgcData.kt...")
sys.stdout.flush()

def replace_municipality(match):
    muni_code = match.group(1)
    muni_name = match.group(2)
    barangays = municipality_barangays.get(muni_code, [])
    
    if barangays:
        # Sort by name
        sorted_barangays = sorted(barangays, key=lambda x: x['name'])
        barangay_list = ',\n'.join([
            f'                        Barangay("{b["code"]}", "{b["name"]}")'
            for b in sorted_barangays
        ])
        return f'Municipality(\n                    code = "{muni_code}",\n                    name = "{muni_name}",\n                    barangays = listOf(\n{barangay_list}\n                    )\n                )'
    else:
        return match.group(0)

# Replace all Municipality entries
updated_content = re.sub(
    r'Municipality\("(\d+)",\s*"([^"]+)"\)',
    replace_municipality,
    psgc_content
)

# Save file
print("\n[4/4] Saving updated file...")
sys.stdout.flush()
with open('app/src/main/java/com/onlineexamination/data/model/PsgcData.kt', 'w', encoding='utf-8') as f:
    f.write(updated_content)

print("\n" + "=" * 80)
print("✅ COMPLETE!")
print(f"📋 Municipalities with barangays: {len(municipality_barangays)}")
print(f"📋 Total barangays added: {barangay_count}")
print("=" * 80)
sys.stdout.flush()

