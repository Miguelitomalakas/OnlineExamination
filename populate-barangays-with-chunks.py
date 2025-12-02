"""
Extract barangay data and populate municipalities, splitting into chunks to avoid method size limit
"""
import barangay
from collections import defaultdict
import re

print("=" * 80)
print("Extracting barangay data (with chunking to avoid method size limit)...")
print("=" * 80)

# First, we need the original PsgcData.kt structure
# Let me read from a backup or regenerate from the original structure
print("\n[1/5] Reading original PsgcData structure...")

# We'll need to get the original file structure first
# For now, let's check if we can find it or regenerate it
# Actually, let's just regenerate everything from scratch with chunking

print("\n[2/5] Processing barangay data from package...")
municipality_barangays = defaultdict(list)

# We need the municipality codes - let's extract them from the barangay package
# by finding municipalities first
municipalities_dict = {}
provinces_dict = {}

for entry in barangay.BARANGAY_FLAT:
    if not isinstance(entry, dict):
        continue
    
    entry_type = entry.get('type', '').lower()
    psgc_id = str(entry.get('psgc_id', '')).strip()
    parent_id = str(entry.get('parent_psgc_id', '')).strip()
    name = entry.get('name', '').strip()
    
    if not name or not psgc_id:
        continue
    
    # Normalize to 9 digits
    if len(psgc_id) < 9:
        psgc_id = psgc_id.ljust(9, '0')
    else:
        psgc_id = psgc_id[:9]
    
    if parent_id:
        if len(parent_id) < 9:
            parent_id = parent_id.ljust(9, '0')
        else:
            parent_id = parent_id[:9]
    
    if entry_type == 'province' or (len(psgc_id) == 9 and psgc_id.endswith('000000')):
        provinces_dict[psgc_id] = name
    elif entry_type in ['municipality', 'city', 'city (icc)', 'city (huc)', 'city (cc)']:
        municipalities_dict[psgc_id] = {'name': name, 'parent': parent_id}
    elif entry_type == 'barangay':
        if parent_id in municipalities_dict:
            municipality_barangays[parent_id].append({
                'code': psgc_id,
                'name': name
            })

print(f"Found {len(provinces_dict)} provinces")
print(f"Found {len(municipalities_dict)} municipalities")
print(f"Found {sum(len(b) for b in municipality_barangays.values())} barangays")

# Group municipalities by province
municipalities_by_province = defaultdict(list)
for muni_code, muni_data in municipalities_dict.items():
    prov_code = muni_data['parent']
    if prov_code in provinces_dict:
        municipalities_by_province[prov_code].append({
            'code': muni_code,
            'name': muni_data['name'],
            'barangays': municipality_barangays.get(muni_code, [])
        })

print("\n[3/5] Generating Kotlin code with chunking...")

# Generate header
kotlin_code = """package com.onlineexamination.data.model

import kotlin.collections.buildList

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
    val provinces: List<Province> by lazy {
        buildList {
"""

# Split provinces into chunks of 1 (one province per chunk to avoid method size limit)
chunk_size = 1
province_list = sorted(provinces_dict.items(), key=lambda x: x[1])
num_chunks = (len(province_list) + chunk_size - 1) // chunk_size

for chunk_num in range(num_chunks):
    kotlin_code += f"            addAll(getProvincesChunk{chunk_num + 1}())\n"

kotlin_code += """        }
    }
    
"""

# Generate chunk functions
for chunk_num in range(num_chunks):
    start = chunk_num * chunk_size
    end = min(start + chunk_size, len(province_list))
    chunk_provinces = province_list[start:end]
    
    kotlin_code += f"    private fun getProvincesChunk{chunk_num + 1}(): List<Province> = listOf(\n"
    
    province_entries = []
    for prov_code, prov_name in chunk_provinces:
        municipalities = sorted(municipalities_by_province.get(prov_code, []), key=lambda x: x['name'])
        
        muni_entries = []
        for muni in municipalities:
            if muni['barangays']:
                sorted_barangays = sorted(muni['barangays'], key=lambda x: x['name'])
                barangay_list = ',\n'.join([
                    f'                        Barangay("{b["code"]}", "{b["name"]}")'
                    for b in sorted_barangays
                ])
                muni_entries.append(f'                Municipality(\n                    code = "{muni["code"]}",\n                    name = "{muni["name"]}",\n                    barangays = listOf(\n{barangay_list}\n                    )\n                )')
            else:
                muni_entries.append(f'                Municipality("{muni["code"]}", "{muni["name"]}")')
        
        muni_list_str = ',\n'.join(muni_entries)
        province_entries.append(f'        Province(\n            code = "{prov_code}",\n            name = "{prov_name}",\n            municipalities = listOf(\n{muni_list_str}\n            )\n        )')
    
    kotlin_code += ',\n'.join(province_entries)
    kotlin_code += "\n    )\n\n"

kotlin_code += "}\n"

# Save
print("\n[4/5] Saving file...")
with open('app/src/main/java/com/onlineexamination/data/model/PsgcData.kt', 'w', encoding='utf-8') as f:
    f.write(kotlin_code)

print("\n[5/5] Complete!")
print(f"Generated file with {len(province_list)} provinces")
print(f"Split into {num_chunks} chunks")
print(f"Total municipalities: {len(municipalities_dict)}")
print(f"Total barangays: {sum(len(b) for b in municipality_barangays.values())}")
print("=" * 80)

