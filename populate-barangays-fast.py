"""
Fast version: Extract barangay data and populate municipalities
"""
import barangay
from collections import defaultdict
import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

print("=" * 80)
print("Extracting barangay data (optimized version)...")
print("=" * 80)

# Read existing PsgcData.kt
print("\n[1/4] Reading PsgcData.kt...")
with open('app/src/main/java/com/onlineexamination/data/model/PsgcData.kt', 'r', encoding='utf-8') as f:
    psgc_content = f.read()

# Extract municipality codes and create a SET for fast lookup
municipality_pattern = r'Municipality\("(\d+)",\s*"([^"]+)"\)'
municipalities = re.findall(municipality_pattern, psgc_content)
muni_code_set = {m[0] for m in municipalities}  # Use set for O(1) lookup
print(f"✅ Found {len(municipalities)} municipalities")
print(f"✅ Created lookup set with {len(muni_code_set)} codes")

# Build mapping
print("\n[2/4] Processing {:,} barangay entries...".format(len(barangay.BARANGAY_FLAT)))
municipality_barangays = defaultdict(list)

barangay_count = 0
for i, entry in enumerate(barangay.BARANGAY_FLAT):
    if not isinstance(entry, dict):
        continue
    
    if entry.get('type', '').lower() != 'barangay':
        continue
    
    psgc_id = str(entry.get('psgc_id', '')).strip()
    parent_id = str(entry.get('parent_psgc_id', '')).strip()
    name = entry.get('name', '').strip()
    
    if not name or not psgc_id or not parent_id:
        continue
    
    # Normalize to 9 digits
    if len(psgc_id) < 9:
        psgc_id = psgc_id.ljust(9, '0')
    else:
        psgc_id = psgc_id[:9]
    
    if len(parent_id) < 9:
        parent_id = parent_id.ljust(9, '0')
    else:
        parent_id = parent_id[:9]
    
    # Fast lookup using set
    if parent_id in muni_code_set:
        municipality_barangays[parent_id].append({
            'code': psgc_id,
            'name': name
        })
        barangay_count += 1
    
    if (i + 1) % 10000 == 0:
        print(f"  Processed {i+1:,}/{len(barangay.BARANGAY_FLAT):,} entries, found {barangay_count:,} barangays...")

print(f"\n✅ Found {barangay_count:,} barangays for {len(municipality_barangays)} municipalities")

# Replace Municipality entries
print("\n[3/4] Updating PsgcData.kt...")

def replace_municipality(match):
    muni_code = match.group(1)
    muni_name = match.group(2)
    barangays = municipality_barangays.get(muni_code, [])
    
    if barangays:
        sorted_barangays = sorted(barangays, key=lambda x: x['name'])
        barangay_list = ',\n'.join([
            f'                        Barangay("{b["code"]}", "{b["name"]}")'
            for b in sorted_barangays
        ])
        return f'Municipality(\n                    code = "{muni_code}",\n                    name = "{muni_name}",\n                    barangays = listOf(\n{barangay_list}\n                    )\n                )'
    return match.group(0)

updated_content = re.sub(
    r'Municipality\("(\d+)",\s*"([^"]+)"\)',
    replace_municipality,
    psgc_content
)

# Save
print("\n[4/4] Saving file...")
with open('app/src/main/java/com/onlineexamination/data/model/PsgcData.kt', 'w', encoding='utf-8') as f:
    f.write(updated_content)

print("\n" + "=" * 80)
print("✅ COMPLETE!")
print(f"📋 Municipalities with barangays: {len(municipality_barangays)}")
print(f"📋 Total barangays: {barangay_count:,}")
print("=" * 80)

