"""
Extract barangay data from barangay package and populate municipalities in PsgcData.kt
"""
import barangay
from collections import defaultdict
import re
import sys

print("Extracting barangay data from barangay package...")
print("=" * 80)

try:
    # Read existing PsgcData.kt to get municipality codes
    print("\nReading existing PsgcData.kt structure...")
    with open('app/src/main/java/com/onlineexamination/data/model/PsgcData.kt', 'r', encoding='utf-8') as f:
        psgc_content = f.read()
    
    # Extract municipality codes from the file
    municipality_pattern = r'Municipality\("(\d+)",\s*"([^"]+)"\)'
    municipalities = re.findall(municipality_pattern, psgc_content)
    
    print(f"Found {len(municipalities)} municipalities in PsgcData.kt")
    
    # Build a mapping of municipality code to barangays
    municipality_barangays = defaultdict(list)
    
    print("\nProcessing barangay data from BARANGAY_FLAT...")
    print(f"Total entries in BARANGAY_FLAT: {len(barangay.BARANGAY_FLAT)}")
    
    processed = 0
    for entry in barangay.BARANGAY_FLAT:
        if not isinstance(entry, dict):
            continue
        
        entry_type = entry.get('type', '').lower()
        psgc_id = str(entry.get('psgc_id', '')).strip()
        parent_id = str(entry.get('parent_psgc_id', '')).strip()
        name = entry.get('name', '').strip()
        
        if not name or not psgc_id:
            continue
        
        # Normalize PSGC ID to 9 digits
        if len(psgc_id) < 9:
            psgc_id = psgc_id.ljust(9, '0')
        elif len(psgc_id) > 9:
            psgc_id = psgc_id[:9]
        
        # Check if it's a barangay
        if entry_type == 'barangay':
            # Normalize parent_id
            if parent_id:
                if len(parent_id) < 9:
                    parent_id_normalized = parent_id.ljust(9, '0')
                elif len(parent_id) > 9:
                    parent_id_normalized = parent_id[:9]
                else:
                    parent_id_normalized = parent_id
                
                # Check if this parent matches any municipality code
                muni_codes = [m[0] for m in municipalities]
                if parent_id_normalized in muni_codes:
                    municipality_barangays[parent_id_normalized].append({
                        'code': psgc_id,
                        'name': name
                    })
                    processed += 1
                    if processed % 1000 == 0:
                        print(f"  Processed {processed} barangays...")
    
    print(f"\n✅ Found barangays for {len(municipality_barangays)} municipalities")
    total_barangays = sum(len(barangays) for barangays in municipality_barangays.values())
    print(f"✅ Total barangays: {total_barangays}")
    
    # Show sample
    if municipality_barangays:
        sample_muni = list(municipality_barangays.items())[0]
        print(f"\nSample municipality: {sample_muni[0]}")
        print(f"  Barangays: {len(sample_muni[1])}")
        if sample_muni[1]:
            print(f"  First barangay: {sample_muni[1][0]['name']}")
    
    # Now generate updated Kotlin code
    print("\n" + "=" * 80)
    print("\nGenerating updated Kotlin code with barangays...")
    
    # Extract all provinces with their municipalities using a more robust regex
    # Match Province blocks
    province_pattern = r'Province\(\s*code\s*=\s*"([^"]+)",\s*name\s*=\s*"([^"]+)",\s*municipalities\s*=\s*listOf\(([^)]+(?:\([^)]*\)[^)]*)*)\)\s*\)'
    
    # Better approach: parse line by line
    lines = psgc_content.split('\n')
    kotlin_code = """package com.onlineexamination.data.model

data class Barangay(
    val code: String,
    val name: String
)

data class Municipality(
    val code: String,
    val name: String,
    val barangays: List<Barangay> = emptyList()
)

data class Province(
    val code: String,
    val name: String,
    val municipalities: List<Municipality>
)

object PsgcData {
    val provinces: List<Province> = listOf(
"""
    
    province_entries = []
    i = 0
    while i < len(lines):
        line = lines[i]
        
        # Look for Province declaration
        if 'Province(' in line and 'code =' in line:
            # Extract province code
            prov_code_match = re.search(r'code\s*=\s*"([^"]+)"', line)
            if prov_code_match:
                prov_code = prov_code_match.group(1)
                
                # Extract province name (might be on same line or next)
                prov_name_match = re.search(r'name\s*=\s*"([^"]+)"', line)
                if not prov_name_match and i + 1 < len(lines):
                    prov_name_match = re.search(r'name\s*=\s*"([^"]+)"', lines[i + 1])
                
                if prov_name_match:
                    prov_name = prov_name_match.group(1)
                    
                    # Find municipalities list
                    muni_entries = []
                    i += 1
                    while i < len(lines) and 'municipalities = listOf(' not in lines[i]:
                        i += 1
                    
                    if i < len(lines):
                        i += 1  # Skip the listOf line
                        # Collect municipality lines
                        while i < len(lines):
                            muni_line = lines[i].strip()
                            if 'Municipality(' in muni_line:
                                muni_match = re.search(r'Municipality\("(\d+)",\s*"([^"]+)"\)', muni_line)
                                if muni_match:
                                    muni_code = muni_match.group(1)
                                    muni_name = muni_match.group(2)
                                    
                                    # Get barangays for this municipality
                                    barangays = municipality_barangays.get(muni_code, [])
                                    if barangays:
                                        # Sort barangays by name
                                        sorted_barangays = sorted(barangays, key=lambda x: x['name'])
                                        barangay_list = ',\n'.join([
                                            f'                        Barangay("{b["code"]}", "{b["name"]}")'
                                            for b in sorted_barangays
                                        ])
                                        muni_entries.append(f'                Municipality(\n                    code = "{muni_code}",\n                    name = "{muni_name}",\n                    barangays = listOf(\n{barangay_list}\n                    )\n                )')
                                    else:
                                        muni_entries.append(f'                Municipality("{muni_code}", "{muni_name}")')
                            elif ')' in muni_line and muni_entries:
                                # End of municipalities list
                                break
                            i += 1
                    
                    # Create province entry
                    if muni_entries:
                        muni_list_str = ',\n'.join(muni_entries)
                        province_entries.append(f'        Province(\n            code = "{prov_code}",\n            name = "{prov_name}",\n            municipalities = listOf(\n{muni_list_str}\n            )\n        )')
        i += 1
    
    # Alternative simpler approach: use regex to find all Municipality entries and replace them
    print("Using regex replacement approach...")
    
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
        else:
            return match.group(0)  # Keep original if no barangays
    
    # Replace all Municipality entries
    updated_content = re.sub(
        r'Municipality\("(\d+)",\s*"([^"]+)"\)',
        replace_municipality,
        psgc_content
    )
    
    # Save to file
    output_file = "app/src/main/java/com/onlineexamination/data/model/PsgcData.kt"
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(updated_content)
    
    print(f"✅ Updated: {output_file}")
    print(f"📋 Municipalities with barangays: {len(municipality_barangays)}")
    print(f"📋 Total barangays: {total_barangays}")
    
except Exception as e:
    print(f"\n❌ Error: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)
