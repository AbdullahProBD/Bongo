#!/usr/bin/env python3
"""
Lightweight Debian package (.deb) builder for cross-platform environments (macOS, Linux).
Packages a staged directory into a standard Debian archive without requiring dpkg-deb.
"""

import os
import sys
import time
import tarfile
import subprocess
import shutil
from pathlib import Path

def tar_reset(tarinfo):
    """Normalize file metadata to root:root and deterministic mode."""
    tarinfo.uid = 0
    tarinfo.gid = 0
    tarinfo.uname = "root"
    tarinfo.gname = "root"
    tarinfo.pax_headers = {}
    # Ensure standard directory permissions
    if tarinfo.isdir():
        tarinfo.mode = 0o755
    return tarinfo

def compress_zstd_or_gz(input_path, output_ext):
    """Compress a tar file using zstd (if available) or gzip."""
    if output_ext == "zst" and shutil.which("zstd"):
        cmd = ["zstd", "-q", "-19", "-f", str(input_path), "-o", f"{input_path}.zst"]
        subprocess.check_call(cmd)
        return f"{input_path}.zst", "zst"
    else:
        # Fall back to gzip
        cmd = ["gzip", "-9", "-f", str(input_path)]
        subprocess.check_call(cmd)
        return f"{input_path}.gz", "gz"

def write_ar_entry(out, name, data, mtime=None, mode=0o100644):
    """Write an entry in BSD/Debian ar format."""
    if mtime is None:
        mtime = int(time.time())
    header = (
        f"{name:<16}"
        f"{mtime:<12}"
        f"{'0':<6}"
        f"{'0':<6}"
        f"{oct(mode)[2:]:>8}"
        f"{len(data):<10}"
        "`\n"
    ).encode("ascii")
    if len(header) != 60:
        raise ValueError(f"Header length invalid: {len(header)}")
    out.write(header)
    out.write(data)
    if len(data) % 2 != 0:
        out.write(b"\n")

def package_deb(root_dir, deb_output_path):
    root_path = Path(root_dir).resolve()
    deb_path = Path(deb_output_path).resolve()
    deb_path.parent.mkdir(parents=True, exist_ok=True)
    temp_dir = root_path.parent / f"_deb_tmp_{int(time.time())}"
    temp_dir.mkdir(parents=True, exist_ok=True)

    try:
        # 1. debian-binary
        debian_binary = b"2.0\n"

        # 2. control.tar
        control_dir = root_path / "DEBIAN"
        control_tar = temp_dir / "control.tar"
        with tarfile.open(control_tar, "w", format=tarfile.GNU_FORMAT) as tar:
            tar.add(control_dir, arcname=".", filter=tar_reset, recursive=False)
            for item in sorted(control_dir.iterdir()):
                tar.add(item, arcname=f"./{item.name}", filter=tar_reset)

        control_comp, control_ext = compress_zstd_or_gz(control_tar, "zst")
        with open(control_comp, "rb") as f:
            control_data = f.read()

        # 3. data.tar
        data_tar = temp_dir / "data.tar"
        with tarfile.open(data_tar, "w", format=tarfile.GNU_FORMAT) as tar:
            tar.add(root_path, arcname=".", filter=tar_reset, recursive=False)
            for item in sorted(root_path.iterdir()):
                if item.name == "DEBIAN":
                    continue
                tar.add(item, arcname=f"./{item.name}", filter=tar_reset)

        data_comp, data_ext = compress_zstd_or_gz(data_tar, "zst")
        with open(data_comp, "rb") as f:
            data_data = f.read()

        # 4. Assemble .deb
        now = int(time.time())
        with open(deb_path, "wb") as deb_file:
            deb_file.write(b"!<arch>\n")
            write_ar_entry(deb_file, "debian-binary", debian_binary, mtime=now)
            write_ar_entry(deb_file, f"control.tar.{control_ext}", control_data, mtime=now)
            write_ar_entry(deb_file, f"data.tar.{data_ext}", data_data, mtime=now)

        print(f"Successfully created: {deb_path} ({deb_path.stat().st_size} bytes)")
    finally:
        shutil.rmtree(temp_dir, ignore_errors=True)

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: package_deb.py <staged_build_root> <output_deb_file>")
        sys.exit(1)
    package_deb(sys.argv[1], sys.argv[2])
