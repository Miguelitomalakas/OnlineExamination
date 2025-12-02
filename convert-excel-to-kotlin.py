"""
Script to convert PSGC Excel file to Kotlin data structure
This script reads an Excel file and generates Kotlin code for PsgcData.kt

Requirements:
    pip install openpyxl pandas

Usage:
    python convert-excel-to-kotlin.py <path_to_excel_file>
    
Example:
    python convert-excel-to-kotlin.py psgc_data.xlsx
"""

import sys
import pandas as pd
import json
from pathlib import Path

def read_excel_file(file_path):
    """Read Excel file and return dataframes"""
    try:
        # Try reading different sheet names
        excel_file = pd.ExcelFile(file_path)
        print(f"Available sheets: {excel_file.sheet_names}")
        
        # Common sheet names for PSGC data
        possible_sheets = ['Provinces', 'Municipalities', 'Cities', 'Data', 'Sheet1']
        
        # Try to find the right sheet
        df = None
        for sheet in excel_file.sheet_names:
            if any(name.lower() in sheet.lower() for name in ['province', 'municipality', 'city', 'data']):
                df = pd.read_excel(file_path, sheet_name=sheet)
                print(f"Using sheet: {sheet}")
                break
        
        if df is None:
            # Use first sheet
            df = pd.read_excel(file_path, sheet_name=0)
            print(f"Using first sheet: {excel_file.sheet_names[0]}")
        
        return df
    except Exception as e:
        print(f"Error reading Excel file: {e}")
        return None

def identify_columns(df):
    """Identify which columns contain province, municipality, and code data"""
    print("\nColumn names in Excel:")
    for i, col in enumerate(df.columns):
        print(f"  {i}: {col}")
    
    # Common column name patterns
    code_patterns = ['code', 'psgc', 'id']
    province_patterns = ['province', 'prov']
    municipality_patterns = ['municipality', 'city', 'muni', 'municipality/city']
    
    code_col = None
    province_col = None
    municipality_col = None
    
    for col in df.columns:
        col_lower = str(col).lower()
        if not code_col and any(pattern in col_lower for pattern in code_patterns):
            code_col = col
        if not province_col and any(pattern in col_lower for pattern in province_patterns):
            province_col = col
        if not municipality_col and any(pattern in col_lower for pattern in municipality_patterns):
            municipality_col = col
    
    print(f"\nIdentified columns:")
    print(f"  Code: {code_col}")
    print(f"  Province: {province_col}")
    print(f"  Municipality: {municipality_col}")
    
    return code_col, province_col, municipality_col

def process_data(df, code_col, province_col, municipality_col):
    """Process Excel data and organize by province -> municipalities"""
    provinces_dict = {}
    
    for _, row in df.iterrows():
        try:
            province_name = str(row[province_col]).strip() if province_col else None
            municipality_name = str(row[municipality_col]).strip() if municipality_col else None
            code = str(row[code_col]).strip() if code_col else None
            
            if not province_name or province_name == 'nan' or not municipality_name or municipality_name == 'nan':
                continue
            
            # Determine if this is a province code or municipality code
            # Province codes typically end with 000000, municipality codes don't
            is_province_code = code and code.endswith('000000') and len(code) == 9
            
            if is_province_code:
                # This is a province
                if code not in provinces_dict:
                    provinces_dict[code] = {
                        'name': province_name,
                        'code': code,
                        'municipalities': []
                    }
            else:
                # This is a municipality - find its province
                # Try to match by province name or code prefix
                found = False
                for prov_code, prov_data in provinces_dict.items():
                    if province_name == prov_data['name']:
                        # Check if municipality already exists
                        if not any(m['name'] == municipality_name for m in prov_data['municipalities']):
                            prov_data['municipalities'].append({
                                'code': code if code and code != 'nan' else f"{prov_code}-{len(prov_data['municipalities'])}",
                                'name': municipality_name
                            })
                        found = True
                        break
                
                if not found:
                    # Create a new province entry if not found
                    prov_code = code[:2] + '0000000' if code and len(code) >= 2 else f"UNKNOWN-{len(provinces_dict)}"
                    provinces_dict[prov_code] = {
                        'name': province_name,
                        'code': prov_code,
                        'municipalities': [{
                            'code': code if code and code != 'nan' else f"{prov_code}-0",
                            'name': municipality_name
                        }]
                    }
        except Exception as e:
            print(f"Error processing row: {e}")
            continue
    
    return provinces_dict

def generate_kotlin_code(provinces_dict):
    """Generate Kotlin code for PsgcData.kt"""
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
        municipalities_str = ",\n".join([
            f'                Municipality("{m["code"]}", "{m["name"]}")'
            for m in data['municipalities']
        ])
        
        province_code = f'        Province(\n            code = "{data["code"]}",\n            name = "{data["name"]}",\n            municipalities = listOf(\n{municipalities_str}\n            )\n        )'
        province_list.append(province_code)
    
    kotlin_code += ",\n".join(province_list)
    kotlin_code += "\n    )\n}"
    
    return kotlin_code

def main():
    if len(sys.argv) < 2:
        print("Usage: python convert-excel-to-kotlin.py <path_to_excel_file>")
        print("\nExample:")
        print("  python convert-excel-to-kotlin.py psgc_data.xlsx")
        sys.exit(1)
    
    excel_path = Path(sys.argv[1])
    
    if not excel_path.exists():
        print(f"Error: File not found: {excel_path}")
        sys.exit(1)
    
    print(f"Reading Excel file: {excel_path}")
    df = read_excel_file(excel_path)
    
    if df is None:
        print("Failed to read Excel file")
        sys.exit(1)
    
    print(f"\nTotal rows: {len(df)}")
    print(f"First few rows:")
    print(df.head())
    
    # Ask user to identify columns if auto-detection fails
    code_col, province_col, municipality_col = identify_columns(df)
    
    if not code_col or not province_col or not municipality_col:
        print("\n⚠️  Could not auto-detect all columns.")
        print("Please check the column names above and update the script if needed.")
        print("\nYou can also manually specify column indices:")
        print("  - Code column index:")
        print("  - Province column index:")
        print("  - Municipality column index:")
        return
    
    print("\nProcessing data...")
    provinces_dict = process_data(df, code_col, province_col, municipality_col)
    
    print(f"\n✅ Processed {len(provinces_dict)} provinces")
    total_municipalities = sum(len(p['municipalities']) for p in provinces_dict.values())
    print(f"✅ Total municipalities: {total_municipalities}")
    
    # Generate Kotlin code
    kotlin_code = generate_kotlin_code(provinces_dict)
    
    # Save to file
    output_file = Path("PsgcData_generated.kt")
    output_file.write_text(kotlin_code, encoding='utf-8')
    
    print(f"\n✅ Kotlin code generated: {output_file}")
    print(f"\n📋 Next steps:")
    print(f"   1. Review the generated file: {output_file}")
    print(f"   2. Copy the content to: app/src/main/java/com/onlineexamination/data/model/PsgcData.kt")
    print(f"   3. Rebuild the Android project")

if __name__ == "__main__":
    main()

