"""Simple test to check barangay package"""
import sys
try:
    import barangay
    print("✅ Barangay package imported successfully")
    
    if hasattr(barangay, 'BARANGAY_FLAT'):
        print(f"✅ BARANGAY_FLAT exists with {len(barangay.BARANGAY_FLAT)} entries")
        if len(barangay.BARANGAY_FLAT) > 0:
            first = barangay.BARANGAY_FLAT[0]
            print(f"✅ First entry type: {type(first)}")
            if isinstance(first, dict):
                print(f"✅ First entry keys: {list(first.keys())}")
                print(f"✅ First entry: {first}")
    else:
        print("❌ BARANGAY_FLAT not found")
        print(f"Available attributes: {[attr for attr in dir(barangay) if not attr.startswith('_')]}")
except Exception as e:
    print(f"❌ Error: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)

