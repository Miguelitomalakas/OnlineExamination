"""
Direct fix: Replace the provinces declaration with lazy initialization
"""
print("Reading file...")
with open('app/src/main/java/com/onlineexamination/data/model/PsgcData.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Add import if not present
if 'import kotlin.collections.buildList' not in content:
    content = content.replace(
        'package com.onlineexamination.data.model',
        'package com.onlineexamination.data.model\n\nimport kotlin.collections.buildList'
    )

# Replace the provinces declaration
old_declaration = 'object PsgcData {\n    val provinces: List<Province> = listOf('
new_declaration = '''object PsgcData {
    val provinces: List<Province> by lazy {
        buildList {
            // All provinces will be loaded here
            addAll(loadAllProvinces())
        }
    }
    
    private fun loadAllProvinces(): List<Province> = listOf('''

if old_declaration in content:
    content = content.replace(old_declaration, new_declaration)
    
    # Also need to close the lazy block properly
    # Find the closing of the listOf
    # The structure is: listOf(...) then ) then }
    # We need: listOf(...) then ) then } (for lazy)
    
    # Find the last closing parens before the final }
    # Count backwards from the end
    lines = content.split('\n')
    # Find the last line with just ')' before the final '}'
    for i in range(len(lines) - 1, -1, -1):
        if lines[i].strip() == ')' and i > len(lines) - 5:
            # This is likely the closing of listOf
            # Insert closing for lazy block
            lines.insert(i + 1, '        }')
            break
    
    content = '\n'.join(lines)
    
    print("✅ Fixed provinces declaration")
    print("Saving file...")
    with open('app/src/main/java/com/onlineexamination/data/model/PsgcData.kt', 'w', encoding='utf-8') as f:
        f.write(content)
    print("✅ Done!")
else:
    print("❌ Could not find provinces declaration")
    print("Looking for: val provinces: List<Province> = listOf(")
    if 'val provinces' in content:
        idx = content.find('val provinces')
        print(f"Found 'val provinces' at position {idx}")
        print("Context:", content[idx:idx+200])

