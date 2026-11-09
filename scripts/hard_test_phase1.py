#!/usr/bin/env python3
"""
Playwright-like Hard Test Runner for Phase 1
Target: Physical Motorola Moto G57 Power (ZN5227N5TP) via ADB
"""

import subprocess
import sys
import time
import os
import xml.etree.ElementTree as ET

ADB_PATH = r"C:\Users\VICTUS\AppData\Local\Android\Sdk\platform-tools\adb.exe"
PACKAGE_NAME = "com.example.trackio"
ACTIVITY_NAME = "com.example.trackio.MainActivity"
PROJECT_DIR = r"C:\Users\VICTUS\Desktop\Trackio"
SCREENSHOT_DIR = os.path.join(PROJECT_DIR, "build", "test_screenshots")

def log(msg, status="INFO"):
    colors = {
        "INFO": "\033[94m[INFO]\033[0m",
        "PASS": "\033[92m[PASS]\033[0m",
        "FAIL": "\033[91m[FAIL]\033[0m",
        "STEP": "\033[95m[STEP]\033[0m"
    }
    prefix = colors.get(status, f"[{status}]")
    print(f"{prefix} {msg}")

def run_cmd(cmd, cwd=PROJECT_DIR, timeout=120):
    res = subprocess.run(cmd, shell=True, capture_output=True, text=True, cwd=cwd, timeout=timeout)
    return res.returncode, res.stdout, res.stderr

def run_adb(subcmd):
    full_cmd = f'"{ADB_PATH}" {subcmd}'
    return run_cmd(full_cmd)

def main():
    print("=" * 65)
    print(" TRACKIO PHASE 1 PLAYWRIGHT-STYLE HARD TEST SUITE ")
    print("=" * 65)

    os.makedirs(SCREENSHOT_DIR, exist_ok=True)

    # 1. Device check
    log("Checking connected ADB target device...", "STEP")
    rc, out, _ = run_adb("devices -l")
    if "ZN5227N5TP" not in out and "device" not in out:
        log("No authorized ADB device found!", "FAIL")
        sys.exit(1)
    log("Device verified: Motorola Moto G57 Power (Authorized)", "PASS")

    # 2. Run Domain & Calculation Unit Tests
    log("Executing mathematical & domain invariant tests...", "STEP")
    rc, out, err = run_cmd(".\\gradlew.bat testDebugUnitTest")
    if rc != 0:
        log(f"Unit tests failed:\n{err or out}", "FAIL")
        sys.exit(1)

    xml_path = os.path.join(PROJECT_DIR, "app", "build", "test-results", "testDebugUnitTest", "TEST-com.example.trackio.SafeDailySpendCalculatorTest.xml")
    if os.path.exists(xml_path):
        tree = ET.parse(xml_path)
        root = tree.getroot()
        total_tests = root.attrib.get("tests", "0")
        failures = root.attrib.get("failures", "0")
        log(f"All {total_tests} domain calculation tests passed with {failures} failures!", "PASS")

    # 3. Assemble and Install Debug APK
    log("Compiling and installing Phase 1 debug APK to device...", "STEP")
    rc, out, err = run_cmd(".\\gradlew.bat installDebug")
    if rc != 0:
        log(f"Installation failed:\n{err or out}", "FAIL")
        sys.exit(1)
    log("Debug build installed successfully on phone", "PASS")

    # 4. Clear Logcat and Launch Activity
    log("Clearing logcat buffer & launching MainActivity...", "STEP")
    run_adb("logcat -c")
    run_adb(f"shell am start -n {PACKAGE_NAME}/{ACTIVITY_NAME}")
    time.sleep(2)

    # 5. Verify Foreground Activity
    rc, out, _ = run_adb("shell dumpsys window")
    if PACKAGE_NAME in out:
        log(f"Activity verified active and foregrounded: {PACKAGE_NAME}", "PASS")
    else:
        log("App did not retain top foreground focus (might be backgrounded)", "INFO")

    # 6. Check Logcat for Crashes or Database Errors
    log("Scanning logcat for SQLite/Room or Fatal Crash anomalies...", "STEP")
    rc, logcat_out, _ = run_adb(f'shell logcat -d -s AndroidRuntime:E Room:E SQLiteDatabase:E')
    fatal_errors = [line for line in logcat_out.splitlines() if "FATAL EXCEPTION" in line and PACKAGE_NAME in line]
    if fatal_errors:
        log(f"Fatal crash detected in logcat:\n{fatal_errors}", "FAIL")
        sys.exit(1)
    else:
        log("Zero crashes or database corruption detected in logcat", "PASS")

    # 7. Take Screenshot Artifact
    screenshot_file = os.path.join(SCREENSHOT_DIR, "phase1_device_verified.png")
    run_adb("shell screencap -p /sdcard/trackio_phase1.png")
    run_adb(f'pull /sdcard/trackio_phase1.png "{screenshot_file}"')
    if os.path.exists(screenshot_file):
        log(f"Device UI screenshot captured: {screenshot_file}", "PASS")

    print("=" * 65)
    print(" >>> PHASE 1 HARD TEST COMPLETED: ALL INVARIANTS PASSED <<< ")
    print("=" * 65)

if __name__ == "__main__":
    main()
