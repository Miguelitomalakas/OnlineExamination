"""
Extract complete PSGC data from barangay package using FLAT structure
"""
import barangay
from collections import defaultdict

print("Extracting PSGC data from barangay package...")
print("=" * 80)

# Use BARANGAY_FLAT which has type, psgc_id, parent_psgc_id
provinces_dict = {}
municipalities_by_province = defaultdict(list)

print(f"\nProcessing {len(barangay.BARANGAY_FLAT)} entries from BARANGAY_FLAT...")

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
    
    # Check if it's a province (type is 'province' or code ends with 000000)
    if entry_type == 'province' or (len(psgc_id) == 9 and psgc_id.endswith('000000')):
        if psgc_id not in provinces_dict:
            provinces_dict[psgc_id] = {
                'code': psgc_id,
                'name': name
            }
    
    # Check if it's a municipality or city
    elif entry_type in ['municipality', 'city', 'city (icc)', 'city (huc)', 'city (cc)']:
        # Find parent province
        # Parent ID might be the province code
        parent_province_code = None
        
        # Try to find province by parent_id
        if parent_id:
            # Normalize parent_id
            if len(parent_id) < 9:
                parent_id_normalized = parent_id.ljust(9, '0')
            elif len(parent_id) > 9:
                parent_id_normalized = parent_id[:9]
            else:
                parent_id_normalized = parent_id
            
            # Check if parent is a province
            if parent_id_normalized in provinces_dict:
                parent_province_code = parent_id_normalized
            else:
                # Try to find province by code prefix (first 2 digits)
                prefix = psgc_id[:2] if len(psgc_id) >= 2 else ''
                for prov_code in provinces_dict.keys():
                    if prov_code.startswith(prefix):
                        parent_province_code = prov_code
                        break
        
        # If still not found, try to match by code prefix
        if not parent_province_code:
            prefix = psgc_id[:2] if len(psgc_id) >= 2 else ''
            for prov_code in provinces_dict.keys():
                if prov_code.startswith(prefix):
                    parent_province_code = prov_code
                    break
        
        if parent_province_code:
            # Check if not already added
            if not any(m['code'] == psgc_id for m in municipalities_by_province[parent_province_code]):
                municipalities_by_province[parent_province_code].append({
                    'code': psgc_id,
                    'name': name
                })

print(f"\n✅ Found {len(provinces_dict)} provinces")
total_munis = sum(len(m) for m in municipalities_by_province.values())
print(f"✅ Total municipalities: {total_munis}")

# Show sample
if len(provinces_dict) > 0:
    sample_prov = list(provinces_dict.items())[0]
    print(f"\nSample province: {sample_prov[1]['name']} ({sample_prov[0]})")
    print(f"  Municipalities: {len(municipalities_by_province[sample_prov[0]])}")

# Generate Kotlin code
print("\n" + "=" * 80)
print("\nGenerating Kotlin code...")

kotlin_code = """package com.onlineexamination.data.model

data class Municipality(
    val code: String,
    val name: String
)

data class Province(
    val code: String,
    val name: String,
    val municipalities: List<Municipality>
)

object PsgcData {
    val provinces: List<Province> = listOf(
"""

province_list = []
for code, data in sorted(provinces_dict.items(), key=lambda x: x[1]['name']):
    municipalities = sorted(municipalities_by_province.get(code, []), key=lambda x: x['name'])
    
    muni_list = ",\n".join([
        f'                Municipality("{m["code"]}", "{m["name"]}")'
        for m in municipalities
    ])
    
    if muni_list:
        prov_entry = f'        Province(\n            code = "{data["code"]}",\n            name = "{data["name"]}",\n            municipalities = listOf(\n{muni_list}\n            )\n        )'
    else:
        prov_entry = f'        Province(\n            code = "{data["code"]}",\n            name = "{data["name"]}",\n            municipalities = emptyList()\n        )'
    
    province_list.append(prov_entry)

kotlin_code += ",\n".join(province_list)
kotlin_code += "\n    )\n}"

# Save to file
output_file = "PsgcData_from_barangay.kt"
with open(output_file, 'w', encoding='utf-8') as f:
    f.write(kotlin_code)

print(f"✅ Generated: {output_file}")
print(f"📋 File size: {len(kotlin_code):,} characters")
print(f"📋 Provinces: {len(provinces_dict)}")
print(f"📋 Total municipalities: {total_munis}")
print(f"\n📋 Next: Copy content to app/src/main/java/com/onlineexamination/data/model/PsgcData.kt")

