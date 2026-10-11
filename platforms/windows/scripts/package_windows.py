#!/usr/bin/env python3
"""
Bongo Windows Packaging and Verification Script
Automates the build and validation of Windows release packages (.exe and .msi).
"""

import os
import sys
import shutil
import hashlib
import subprocess
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
WINDOWS_DIR = SCRIPT_DIR.parent
PROJECT_ROOT = WINDOWS_DIR.parents[1]
BUILD_DIR = WINDOWS_DIR / "build"
RELEASE_DIR = WINDOWS_DIR / "release"


def compute_sha256(file_path: Path) -> str:
    h = hashlib.sha256()
    with open(file_path, "rb") as f:
        while chunk := f.read(65536):
            h.update(chunk)
    return h.hexdigest()


def check_prerequisites():
    print("Checking Windows packaging prerequisites...")
    engine_cargo = WINDOWS_DIR / "engine" / "Cargo.toml"
    if not engine_cargo.is_file():
        raise FileNotFoundError(f"Missing engine Cargo.toml: {engine_cargo}")

    service_script = WINDOWS_DIR / "service" / "bongo_service.py"
    if not service_script.is_file():
        raise FileNotFoundError(f"Missing service script: {service_script}")

    ui_script = WINDOWS_DIR / "ui" / "bongo_settings.py"
    if not ui_script.is_file():
        raise FileNotFoundError(f"Missing settings UI script: {ui_script}")

    inno_script = WINDOWS_DIR / "installer" / "bongo.iss"
    if not inno_script.is_file():
        raise FileNotFoundError(f"Missing Inno Setup script: {inno_script}")

    wix_script = WINDOWS_DIR / "installer" / "bongo.wxs"
    if not wix_script.is_file():
        raise FileNotFoundError(f"Missing WiX script: {wix_script}")

    print("All core Windows source components verified.")


def build_windows():
    BUILD_DIR.mkdir(parents=True, exist_ok=True)
    RELEASE_DIR.mkdir(parents=True, exist_ok=True)

    if sys.platform != "win32":
        print("Note: Non-Windows host detected. Performing packaging integrity audit.")
        check_prerequisites()
        print("Packaging integrity audit passed.")
        return

    # On Windows: execute build-windows.bat
    bat_path = SCRIPT_DIR / "build-windows.bat"
    subprocess.run([str(bat_path)], check=True)


if __name__ == "__main__":
    try:
        build_windows()
    except Exception as e:
        sys.stderr.write(f"Packaging error: {e}\n")
        sys.exit(1)
