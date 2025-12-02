"""
Fix PsgcData.kt by using lazy initialization to avoid "Method too large" error
"""
import re

print("=" * 80)
print("Fixing PsgcData.kt - Using lazy initialization")
print("=" * 80)

# Read the current file
print("\n[1/3] Reading PsgcData.kt...")
with open('app/src/main/java/com/onlineexamination/data/model/PsgcData.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Extract the header
header_match = re.search(r'(package com\.onlineexamination\.data\.model\s+.*?data class Province.*?\n)', content, re.DOTALL)
if not header_match:
    print("❌ Could not find header in file")
    exit(1)

header = header_match.group(1)

# Extract all Province blocks
print("\n[2/3] Extracting provinces...")
province_pattern = r'Province\(\s*code\s*=\s*"([^"]+)",\s*name\s*=\s*"([^"]+)",\s*municipalities\s*=\s*listOf\((.*?)\)\s*\)'
province_matches = re.findall(province_pattern, content, re.DOTALL)

print(f"✅ Found {len(province_matches)} provinces")

# Generate new code with lazy initialization
print("\n[3/3] Generating fixed code...")

new_content = header + """object PsgcData {
    val provinces: List<Province> by lazy {
        buildList {
            addAll(getProvincesPart1())
            addAll(getProvincesPart2())
            addAll(getProvincesPart3())
            addAll(getProvincesPart4())
        }
    }
    
    private fun getProvincesPart1(): List<Province> = listOf(
"""

# Split provinces into 4 parts
part_size = (len(province_matches) + 3) // 4
parts = [
    province_matches[i:i + part_size]
    for i in range(0, len(province_matches), part_size)
]

# Generate each part
for part_num, part in enumerate(parts, 1):
    if part_num > 1:
        new_content += f"\n    private fun getProvincesPart{part_num}(): List<Province> = listOf(\n"
    
    province_entries = []
    for prov_code, prov_name, muni_block in part:
        # Extract municipalities from block
        muni_matches = re.findall(
            r'Municipality\(\s*(?:code\s*=\s*"(\d+)"\s*,\s*name\s*=\s*"([^"]+)"\s*(?:,\s*barangays\s*=\s*listOf\((.*?)\))?|"(\d+)"\s*,\s*"([^"]+)")\s*\)',
            muni_block,
            re.DOTALL
        )
        
        muni_entries = []
        for match in muni_matches:
            if match[0]:  # New format with code = and name =
                muni_code = match[0]
                muni_name = match[1]
                barangays_block = match[2] if len(match) > 2 else ""
            else:  # Old format
                muni_code = match[3]
                muni_name = match[4]
                barangays_block = ""
            
            if barangays_block.strip():
                # Has barangays
                barangay_matches = re.findall(r'Barangay\("([^"]+)",\s*"([^"]+)"\)', barangays_block)
                if barangay_matches:
                    barangay_list = ',\n'.join([
                        f'                        Barangay("{b[0]}", "{b[1]}")'
                        for b in barangay_matches
                    ])
                    muni_entries.append(f'                Municipality(\n                    code = "{muni_code}",\n                    name = "{muni_name}",\n                    barangays = listOf(\n{barangay_list}\n                    )\n                )')
                else:
                    muni_entries.append(f'                Municipality("{muni_code}", "{muni_name}")')
            else:
                muni_entries.append(f'                Municipality("{muni_code}", "{muni_name}")')
        
        muni_list_str = ',\n'.join(muni_entries)
        province_entries.append(f'        Province(\n            code = "{prov_code}",\n            name = "{prov_name}",\n            municipalities = listOf(\n{muni_list_str}\n            )\n        )')
    
    new_content += ',\n'.join(province_entries)
    if part_num < len(parts):
        new_content += "\n    )\n"
    else:
        new_content += "\n    )\n}\n"

# Add import for buildList
if 'import kotlin.collections.buildList' not in new_content:
    new_content = new_content.replace(
        'package com.onlineexamination.data.model',
        'package com.onlineexamination.data.model\n\nimport kotlin.collections.buildList'
    )

# Save the fixed file
print("\n[4/4] Saving fixed file...")
with open('app/src/main/java/com/onlineexamination/data/model/PsgcData.kt', 'w', encoding='utf-8') as f:
    f.write(new_content)

print("\n" + "=" * 80)
print("✅ COMPLETE!")
print(f"📋 Split into {len(parts)} parts to avoid method size limit")
print("=" * 80)

