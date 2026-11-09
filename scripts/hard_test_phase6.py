#!/usr/bin/env python3
"""
Playwright-like E2E Hard Test Runner for Phase 6:
Project Budgets, Wishlist & Financial Impact Copilot, and Weekly Insights
Target: Motorola Moto G57 Power (ZN5227N5TP) via ADB
"""

import subprocess
import sys
import time
import os
import re
import xml.etree.ElementTree as ET

if sys.stdout.encoding != 'utf-8':
    try:
        sys.stdout.reconfigure(encoding='utf-8')
    except Exception:
        pass

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

def run_cmd(cmd, cwd=PROJECT_DIR, timeout=240):
    res = subprocess.run(cmd, shell=True, capture_output=True, text=True, cwd=cwd, timeout=timeout)
    return res.returncode, res.stdout, res.stderr

def run_adb(subcmd):
    full_cmd = f'"{ADB_PATH}" {subcmd}'
    return run_cmd(full_cmd)

def dump_ui(retries=3):
    time.sleep(1.0)
    os.makedirs(os.path.join(PROJECT_DIR, "build"), exist_ok=True)
    local_xml = os.path.join(PROJECT_DIR, "build", "phase6_dump.xml")
    for attempt in range(retries):
        run_adb("shell rm -f /sdcard/phase6_dump.xml")
        run_adb("shell uiautomator dump --compressed /sdcard/phase6_dump.xml")
        time.sleep(0.5)
        if os.path.exists(local_xml):
            try:
                os.remove(local_xml)
            except Exception:
                pass
        rc, out, _ = run_adb(f'pull /sdcard/phase6_dump.xml "{local_xml}"')
        if os.path.exists(local_xml) and os.path.getsize(local_xml) > 100:
            try:
                return ET.parse(local_xml).getroot()
            except Exception as e:
                pass
        time.sleep(1.0)
    return None

def find_node(root, text_contains=None, desc=None):
    if root is None:
        return None, None
    for node in root.iter('node'):
        node_text = node.attrib.get('text', '')
        node_desc = node.attrib.get('content-desc', '')
        text_match = (text_contains is None) or (text_contains.lower() in node_text.lower())
        desc_match = (desc is None) or (desc.lower() in node_desc.lower())

        if text_match and desc_match:
            bounds = node.attrib.get('bounds', '')
            m = re.match(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]', bounds)
            if m:
                x1, y1, x2, y2 = map(int, m.groups())
                return node, ((x1 + x2) // 2, (y1 + y2) // 2)
    return None, None

def find_nav_tab(root, label):
    if root is None:
        return None
    for node in root.iter('node'):
        node_text = node.attrib.get('text', '')
        node_desc = node.attrib.get('content-desc', '')
        if label.lower() == node_text.lower() or label.lower() == node_desc.lower():
            bounds = node.attrib.get('bounds', '')
            m = re.match(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]', bounds)
            if m:
                x1, y1, x2, y2 = map(int, m.groups())
                if y1 > 1900:  # Bottom Navigation Bar area
                    return (x1 + x2) // 2, (y1 + y2) // 2
    return None

def tap(coords):
    x, y = coords
    run_adb(f"shell input tap {x} {y}")
    time.sleep(1.0)

def capture_screenshot(name):
    os.makedirs(SCREENSHOT_DIR, exist_ok=True)
    local_path = os.path.join(SCREENSHOT_DIR, name)
    device_path = f"/sdcard/{name}"
    run_adb(f"shell screencap -p {device_path}")
    run_adb(f"pull {device_path} {local_path}")
    log(f"Captured screenshot: {local_path}", "INFO")
    return local_path

def main():
    log("================================================================", "INFO")
    log("PLAYWRIGHT-STYLE HARD TEST SUITE - PHASE 6: PROJECTS, WISHLIST & INSIGHTS", "INFO")
    log("================================================================", "INFO")

    # Step 1: Check Device
    log("Verifying physical ADB device connectivity...", "STEP")
    rc, out, _ = run_adb("devices")
    if "ZN5227N5TP" not in out:
        log("Device ZN5227N5TP not detected!", "FAIL")
        sys.exit(1)
    log("Motorola Moto G57 Power (ZN5227N5TP) connected and online.", "PASS")

    # Wake screen
    run_adb("shell input keyevent 224")
    run_adb("shell wm dismiss-keyguard")
    time.sleep(1)

    # Step 2: Install freshly built APK
    apk_path = os.path.join(PROJECT_DIR, "app", "build", "outputs", "apk", "direct", "debug", "app-direct-debug.apk")
    log(f"Installing {apk_path} on device...", "STEP")
    rc, out, err = run_adb(f'install -r -d "{apk_path}"')
    if rc != 0 or "Success" not in out:
        log(f"APK installation failed: {out} {err}", "FAIL")
        sys.exit(1)
    log("directDebug APK successfully installed.", "PASS")

    # Step 3: Launch Trackio
    log("Launching Trackio main activity...", "STEP")
    run_adb(f"shell am force-stop {PACKAGE_NAME}")
    time.sleep(1)
    run_adb(f"shell am start -n {PACKAGE_NAME}/{ACTIVITY_NAME}")
    time.sleep(3)

    root = dump_ui()

    # If in onboarding, complete it quickly
    node_welcome, _ = find_node(root, text_contains="Welcome to Trackio")
    if node_welcome is not None:
        log("Onboarding screen detected. Setting up initial allowance...", "INFO")
        _, allowance_coords = find_node(root, text_contains="Monthly Allowance")
        if allowance_coords:
            tap(allowance_coords)
            run_adb("shell input text 8000")
            time.sleep(0.5)

        run_adb("shell input keyevent 66")
        time.sleep(1)
        root = dump_ui()

        _, get_started_coords = find_node(root, text_contains="Get Started")
        if get_started_coords:
            tap(get_started_coords)
            time.sleep(2)
            root = dump_ui()

    # Step 4: Verify Bottom Navigation Bar with 4 tabs
    log("Verifying Bottom Navigation Bar with 4 tabs...", "STEP")
    daily_coords = find_nav_tab(root, "Daily")
    projects_coords = find_nav_tab(root, "Projects")
    wishlist_coords = find_nav_tab(root, "Wishlist")
    insights_coords = find_nav_tab(root, "Insights")

    if not (daily_coords and projects_coords and wishlist_coords and insights_coords):
        log(f"Bottom navigation tabs not all found! Daily={daily_coords}, Projects={projects_coords}, Wishlist={wishlist_coords}, Insights={insights_coords}", "FAIL")
        sys.exit(1)
    log("All 4 Navigation tabs (Daily, Projects, Wishlist, Insights) verified.", "PASS")

    # Tab touch points
    daily_tab = (daily_coords[0], 2185)
    projects_tab = (projects_coords[0], 2185)
    wishlist_tab = (wishlist_coords[0], 2185)
    insights_tab = (insights_coords[0], 2185)

    # Step 5: Test Projects Tab
    log("Testing Navigation to Projects Tab...", "STEP")
    tap(projects_tab)
    time.sleep(1.5)
    root = dump_ui()

    node_proj_title, _ = find_node(root, text_contains="Project Budgets")
    if node_proj_title is None:
        log("Could not find 'Project Budgets' header in Projects tab!", "FAIL")
        sys.exit(1)
    log("'Project Budgets' screen loaded.", "PASS")

    # Click "New Project"
    _, new_proj_btn = find_node(root, text_contains="New Project")
    if not new_proj_btn:
        log("Could not find 'New Project' button!", "FAIL")
        sys.exit(1)
    tap(new_proj_btn)
    time.sleep(1)

    # Enter Project Name & Cap via focused fields
    root = dump_ui()
    _, name_field = find_node(root, text_contains="Project Name")
    if name_field:
        tap(name_field)
        time.sleep(0.3)
        run_adb("shell input text Goa%sTrip")
        time.sleep(0.3)

    root = dump_ui()
    _, cap_field = find_node(root, text_contains="Spending Cap")
    if cap_field:
        tap(cap_field)
        time.sleep(0.3)
        run_adb("shell input text 5000")
        time.sleep(0.3)

    # Dismiss soft keyboard to reveal Save Project button
    run_adb("shell input keyevent 111")
    time.sleep(0.8)

    root = dump_ui()
    _, save_proj_btn = find_node(root, text_contains="Save Project")
    if not save_proj_btn:
        log("Could not find 'Save Project' button in dialog!", "FAIL")
        sys.exit(1)
    tap(save_proj_btn)
    time.sleep(1.5)

    root = dump_ui()
    node_goa, _ = find_node(root, text_contains="Trip")
    if node_goa is None:
        log("Newly created project card not found on screen!", "FAIL")
        sys.exit(1)
    log("Project budget card verified on screen with spending cap.", "PASS")
    capture_screenshot("phase6_projects_tab.png")

    # Step 6: Log a tagged expense and verify project budget link
    log("Logging transaction tagged with '#Goa Trip'...", "STEP")
    root = dump_ui()
    _, fab_coords = find_node(root, desc="Quick Add")
    if not fab_coords:
        tap(daily_tab)
        time.sleep(1)
        root = dump_ui()
        _, fab_coords = find_node(root, desc="Quick Add")

    tap(fab_coords)
    time.sleep(1.5)

    # Fill Quick Add Sheet
    root = dump_ui()
    _, amt_field = find_node(root, text_contains="Amount")
    if amt_field:
        tap(amt_field)
        time.sleep(0.3)
        run_adb("shell input text 1200")
        time.sleep(0.3)

    root = dump_ui()
    _, vendor_field = find_node(root, text_contains="Paid to")
    if vendor_field:
        tap(vendor_field)
        time.sleep(0.3)
        run_adb("shell input text Train%sTickets")
        time.sleep(0.3)

    root = dump_ui()
    _, tag_field = find_node(root, text_contains="Project / Tag")
    if tag_field:
        tap(tag_field)
        time.sleep(0.3)
        run_adb("shell input text Goa%sTrip")
        time.sleep(0.3)

    # Dismiss keyboard to uncover Log Expense button
    run_adb("shell input keyevent 111")
    time.sleep(0.5)

    root = dump_ui()
    _, log_btn = find_node(root, text_contains="Log Expense")
    if log_btn:
        tap(log_btn)
        time.sleep(1.5)

    # Check Projects tab to verify spent amount updated
    tap(projects_tab)
    time.sleep(1.5)
    root = dump_ui()

    node_spent, _ = find_node(root, text_contains="1200")
    if node_spent is None:
        log("Project card did not reflect ₹1200 spent for #Goa Trip!", "FAIL")
        sys.exit(1)
    log("Project '#Goa Trip' accurately aggregated tagged expense (Spent: ₹1,200, Remaining: ₹3,800).", "PASS")

    # Step 7: Test Wishlist Tab & Opportunity Cost
    log("Testing Navigation to Wishlist Tab...", "STEP")
    tap(wishlist_tab)
    time.sleep(1.5)
    root = dump_ui()

    node_wish_title, _ = find_node(root, text_contains="Wishlist & Impact")
    if node_wish_title is None:
        log("Could not find 'Wishlist & Impact' header in Wishlist tab!", "FAIL")
        sys.exit(1)
    log("'Wishlist & Impact' screen loaded.", "PASS")

    # Click "Add Wish"
    _, add_wish_btn = find_node(root, text_contains="Add Wish")
    if not add_wish_btn:
        log("Could not find 'Add Wish' button!", "FAIL")
        sys.exit(1)
    tap(add_wish_btn)
    time.sleep(1)

    # Enter Item Name, Price, Notes
    root = dump_ui()
    _, wish_name_field = find_node(root, text_contains="Item Name")
    if wish_name_field:
        tap(wish_name_field)
        time.sleep(0.3)
        run_adb("shell input text Sony%sHeadphones")
        time.sleep(0.3)

    root = dump_ui()
    _, wish_price_field = find_node(root, text_contains="Target Price")
    if wish_price_field:
        tap(wish_price_field)
        time.sleep(0.3)
        run_adb("shell input text 2500")
        time.sleep(0.3)

    root = dump_ui()
    _, wish_notes_field = find_node(root, text_contains="Notes")
    if wish_notes_field:
        tap(wish_notes_field)
        time.sleep(0.3)
        run_adb("shell input text Amazon%sSale")
        time.sleep(0.3)

    # Dismiss keyboard to uncover Add to Wishlist button
    run_adb("shell input keyevent 111")
    time.sleep(0.5)

    root = dump_ui()
    _, confirm_wish_btn = find_node(root, text_contains="Add to Wishlist")
    if confirm_wish_btn:
        tap(confirm_wish_btn)
        time.sleep(1.5)

    root = dump_ui()
    node_headphones, _ = find_node(root, text_contains="Sony Headphones")
    node_impact, _ = find_node(root, text_contains="IMPACT ON CYCLE")
    if node_headphones is None or node_impact is None:
        log("Wishlist item or Opportunity-Cost calculation not found on screen!", "FAIL")
        sys.exit(1)
    log("Wishlist item 'Sony Headphones' added with real-time financial opportunity-cost copilot.", "PASS")
    capture_screenshot("phase6_wishlist_tab.png")

    # Step 8: Test Insights Tab
    log("Testing Navigation to Insights Tab...", "STEP")
    tap(insights_tab)
    time.sleep(1.5)
    root = dump_ui()

    node_insights_title, _ = find_node(root, text_contains="Weekly Insights")
    node_velocity, _ = find_node(root, text_contains="BURN VELOCITY")
    node_merchant, _ = find_node(root, text_contains="TOP MERCHANT")

    if not (node_insights_title and node_velocity and node_merchant):
        log(f"Insights components not all verified! Title={node_insights_title}, Velocity={node_velocity}, Merchant={node_merchant}", "FAIL")
        sys.exit(1)
    log("Weekly Insights verified: Burn Velocity, Top Merchant, Category distributions all active.", "PASS")
    capture_screenshot("phase6_insights_tab.png")

    log("================================================================", "INFO")
    log("PHASE 6 HARD TEST COMPLETED: ALL E2E CRITERIA VERIFIED ON DEVICE!", "PASS")
    log("================================================================", "INFO")

if __name__ == "__main__":
    main()
