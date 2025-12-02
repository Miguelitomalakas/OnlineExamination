"""
Split PsgcData provinces into smaller chunks to avoid "Method too large" error
"""
import re

print("=" * 80)
print("Splitting PsgcData provinces into chunks")
print("=" * 80)

# Read file
print("\n[1/4] Reading file...")
with open('app/src/main/java/com/onlineexamination/data/model/PsgcData.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Extract header
header_match = re.search(r'(package com\.onlineexamination\.data\.model.*?data class Province.*?\n)', content, re.DOTALL)
if not header_match:
    print("❌ Could not find header")
    exit(1)

header = header_match.group(1)

# Find where provinces list starts
prov_start = content.find('private fun loadAllProvinces(): List<Province> = listOf(')
if prov_start == -1:
    print("❌ Could not find loadAllProvinces function")
    exit(1)

# Extract provinces content (from listOf( to the matching closing paren)
provinces_section = content[prov_start:]
# Find the matching closing paren for listOf
paren_count = 0
end_pos = 0
for i, char in enumerate(provinces_section):
    if char == '(':
        paren_count += 1
    elif char == ')':
        paren_count -= 1
        if paren_count == 0:
            end_pos = i + 1
            break

provinces_content = provinces_section[:end_pos]
footer = content[prov_start + len(provinces_section):]

# Extract individual Province blocks using a simpler approach
print("\n[2/4] Extracting provinces...")
# Count provinces by counting "Province(" that are at the start of a line with proper indentation
province_lines = []
in_province = False
current_province = []
indent_level = 0

lines = provinces_content.split('\n')
for i, line in enumerate(lines):
    stripped = line.strip()
    if stripped.startswith('Province(') and 'code =' in line:
        if current_province:
            province_lines.append('\n'.join(current_province))
        current_province = [line]
        in_province = True
    elif in_province:
        current_province.append(line)
        if stripped == ')' and i < len(lines) - 1:
            # Check if next non-empty line is also closing paren or new province
            next_lines = [l.strip() for l in lines[i+1:i+5] if l.strip()]
            if next_lines and (next_lines[0] == ')' or next_lines[0].startswith('Province(')):
                province_lines.append('\n'.join(current_province))
                current_province = []
                in_province = False

if current_province:
    province_lines.append('\n'.join(current_province))

print(f"✅ Found {len(province_lines)} provinces")

# Split into chunks of 20 provinces each
chunk_size = 20
num_chunks = (len(province_lines) + chunk_size - 1) // chunk_size
print(f"📋 Splitting into {num_chunks} chunks of ~{chunk_size} provinces each")

# Generate new code
print("\n[3/4] Generating new code...")
new_content = header
new_content += """object PsgcData {
    val provinces: List<Province> by lazy {
        buildList {
"""

# Add calls to chunk functions
for i in range(num_chunks):
    new_content += f"            addAll(getProvincesChunk{i + 1}())\n"

new_content += """        }
    }
    
"""

# Generate chunk functions
for chunk_num in range(num_chunks):
    start = chunk_num * chunk_size
    end = min(start + chunk_size, len(province_lines))
    chunk_provinces = province_lines[start:end]
    
    new_content += f"    private fun getProvincesChunk{chunk_num + 1}(): List<Province> = listOf(\n"
    new_content += ',\n'.join(chunk_provinces)
    new_content += "\n    )\n\n"

new_content += footer

# Add import
if 'import kotlin.collections.buildList' not in new_content:
    new_content = new_content.replace(
        'package com.onlineexamination.data.model',
        'package com.onlineexamination.data.model\n\nimport kotlin.collections.buildList'
    )

# Save
print("\n[4/4] Saving file...")
with open('app/src/main/java/com/onlineexamination/data/model/PsgcData.kt', 'w', encoding='utf-8') as f:
    f.write(new_content)

print("\n" + "=" * 80)
print("✅ COMPLETE!")
print(f"📋 Split {len(province_lines)} provinces into {num_chunks} chunks")
print("=" * 80)

