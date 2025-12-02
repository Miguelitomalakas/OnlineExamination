"""
Fix PsgcData.kt by splitting provinces into smaller helper functions
"""
import re

print("=" * 80)
print("Fixing PsgcData.kt - Splitting into helper functions")
print("=" * 80)

# Read the current file
print("\n[1/3] Reading PsgcData.kt...")
with open('app/src/main/java/com/onlineexamination/data/model/PsgcData.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Find where provinces list starts and ends
print("\n[2/3] Parsing file structure...")
start_idx = None
end_idx = None

for i, line in enumerate(lines):
    if 'val provinces: List<Province> = listOf(' in line:
        start_idx = i
    if start_idx is not None and line.strip() == ')' and i > start_idx + 10:
        # Check if this is the closing of provinces list
        # Count opening and closing parens
        paren_count = 0
        for j in range(start_idx, i + 1):
            paren_count += lines[j].count('(') - lines[j].count(')')
        if paren_count == 0:
            end_idx = i
            break

if start_idx is None or end_idx is None:
    print("❌ Could not find provinces list boundaries")
    exit(1)

print(f"✅ Found provinces list at lines {start_idx+1} to {end_idx+1}")

# Extract header and footer
header = ''.join(lines[:start_idx])
footer = ''.join(lines[end_idx+1:])

# Extract provinces content
provinces_content = ''.join(lines[start_idx:end_idx+1])

# Split provinces into chunks (each chunk ~25 provinces to stay under limit)
chunk_size = 25
province_blocks = re.findall(
    r'Province\(\s*code\s*=\s*"([^"]+)",\s*name\s*=\s*"([^"]+)",\s*municipalities\s*=\s*listOf\((.*?)\)\s*\)',
    provinces_content,
    re.DOTALL
)

print(f"✅ Found {len(province_blocks)} provinces")
print(f"📋 Splitting into chunks of ~{chunk_size} provinces")

# Generate new code
print("\n[3/3] Generating fixed code...")

new_content = header
new_content += """object PsgcData {
    val provinces: List<Province> by lazy {
        buildList {
"""

# Split into chunks and generate helper functions
num_chunks = (len(province_blocks) + chunk_size - 1) // chunk_size
for chunk_num in range(num_chunks):
    start = chunk_num * chunk_size
    end = min(start + chunk_size, len(province_blocks))
    chunk = province_blocks[start:end]
    
    # Generate helper function
    new_content += f"            addAll(getProvincesChunk{chunk_num + 1}())\n"
    
    # Generate the chunk function (we'll add this after)
    chunk_functions = []
    chunk_func = f"    private fun getProvincesChunk{chunk_num + 1}(): List<Province> = listOf(\n"
    
    province_entries = []
    for prov_code, prov_name, muni_block in chunk:
        # Parse municipalities - handle both formats
        # Try new format first (with code = and name =)
        muni_new_format = re.findall(
            r'Municipality\(\s*code\s*=\s*"(\d+)",\s*name\s*=\s*"([^"]+)",\s*barangays\s*=\s*listOf\((.*?)\)\s*\)',
            muni_block,
            re.DOTALL
        )
        
        muni_old_format = re.findall(
            r'Municipality\("(\d+)",\s*"([^"]+)"\)',
            muni_block
        )
        
        muni_entries = []
        if muni_new_format:
            # Has barangays
            for muni_code, muni_name, barangays_block in muni_new_format:
                barangay_matches = re.findall(r'Barangay\("([^"]+)",\s*"([^"]+)"\)', barangays_block)
                if barangay_matches:
                    barangay_list = ',\n'.join([
                        f'                        Barangay("{b[0]}", "{b[1]}")'
                        for b in barangay_matches
                    ])
                    muni_entries.append(f'                Municipality(\n                    code = "{muni_code}",\n                    name = "{muni_name}",\n                    barangays = listOf(\n{barangay_list}\n                    )\n                )')
        elif muni_old_format:
            # No barangays
            for muni_code, muni_name in muni_old_format:
                muni_entries.append(f'                Municipality("{muni_code}", "{muni_name}")')
        
        muni_list_str = ',\n'.join(muni_entries)
        province_entries.append(f'        Province(\n            code = "{prov_code}",\n            name = "{prov_name}",\n            municipalities = listOf(\n{muni_list_str}\n            )\n        )')
    
    chunk_func += ',\n'.join(province_entries)
    chunk_func += "\n    )\n\n"
    chunk_functions.append(chunk_func)

new_content += """        }
    }
    
"""

# Add all chunk functions
for func in chunk_functions:
    new_content += func

new_content += footer

# Add import for buildList if not present
if 'import kotlin.collections.buildList' not in new_content:
    new_content = new_content.replace(
        'package com.onlineexamination.data.model',
        'package com.onlineexamination.data.model\n\nimport kotlin.collections.buildList'
    )

# Save
print("\n[4/4] Saving fixed file...")
with open('app/src/main/java/com/onlineexamination/data/model/PsgcData.kt', 'w', encoding='utf-8') as f:
    f.write(new_content)

print("\n" + "=" * 80)
print("✅ COMPLETE!")
print(f"📋 Split into {num_chunks} chunks to avoid method size limit")
print("=" * 80)

