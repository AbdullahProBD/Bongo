#!/usr/bin/env python3
"""
Clean, GNU-compliant Debian package builder.
Used as a cross-platform fallback when dpkg-deb is not installed (e.g. on macOS or non-Debian CI).
Produces standard .deb (ar) archives with pure GNU tar format and zero Apple PAX metadata.
"""

import os
import sys
import tarfile
import subprocess
import shutil

def clean_filter(tarinfo):
    tarinfo.uid = 0
    tarinfo.gid = 0
    tarinfo.uname = "root"
    tarinfo.gname = "root"
    tarinfo.pax_headers = {}
    basename = os.path.basename(tarinfo.name)
    if basename.startswith("._") or basename in (".DS_Store", "__MACOSX"):
        return None
    return tarinfo

def build_deb(build_root, output_deb):
    os.makedirs(os.path.dirname(os.path.abspath(output_deb)), exist_ok=True)
    tmp_dir = os.path.join(os.path.dirname(output_deb), ".deb_tmp")
    if os.path.exists(tmp_dir):
        shutil.rmtree(tmp_dir)
    os.makedirs(tmp_dir, exist_ok=True)

    debian_dir = os.path.join(build_root, "DEBIAN")
    control_tar = os.path.join(tmp_dir, "control.tar")
    data_tar = os.path.join(tmp_dir, "data.tar")

    # 1. Create control.tar
    with tarfile.open(control_tar, "w", format=tarfile.GNU_FORMAT) as tar:
        for item in sorted(os.listdir(debian_dir)):
            item_path = os.path.join(debian_dir, item)
            tar.add(item_path, arcname=f"./{item}", filter=clean_filter)

    # 2. Create data.tar
    with tarfile.open(data_tar, "w", format=tarfile.GNU_FORMAT) as tar:
        for item in sorted(os.listdir(build_root)):
            if item == "DEBIAN":
                continue
            item_path = os.path.join(build_root, item)
            tar.add(item_path, arcname=f"./{item}", filter=clean_filter)

    # 3. Compress with zstd if available, otherwise gzip
    use_zstd = shutil.which("zstd") is not None
    if use_zstd:
        control_comp = os.path.join(tmp_dir, "control.tar.zst")
        data_comp = os.path.join(tmp_dir, "data.tar.zst")
        subprocess.run(["zstd", "-19", "-f", control_tar, "-o", control_comp], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        subprocess.run(["zstd", "-19", "-f", data_tar, "-o", data_comp], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        control_name = "control.tar.zst"
        data_name = "data.tar.zst"
    else:
        import gzip
        control_comp = os.path.join(tmp_dir, "control.tar.gz")
        data_comp = os.path.join(tmp_dir, "data.tar.gz")
        with open(control_tar, "rb") as f_in, gzip.open(control_comp, "wb", compresslevel=9) as f_out:
            shutil.copyfileobj(f_in, f_out)
        with open(data_tar, "rb") as f_in, gzip.open(data_comp, "wb", compresslevel=9) as f_out:
            shutil.copyfileobj(f_in, f_out)
        control_name = "control.tar.gz"
        data_name = "data.tar.gz"

    debian_binary = os.path.join(tmp_dir, "debian-binary")
    with open(debian_binary, "wb") as f:
        f.write(b"2.0\n")

    # 4. Write standard ar archive
    files = [
        ("debian-binary", debian_binary),
        (control_name, control_comp),
        (data_name, data_comp),
    ]

    with open(output_deb, "wb") as deb:
        deb.write(b"!<arch>\n")
        for name, file_path in files:
            size = os.path.getsize(file_path)
            mtime = int(os.path.getmtime(file_path))
            header = f"{name:<16}{mtime:<12}{0:<6}{0:<6}{100644:<8}{size:<10}\x60\n".encode("ascii")
            deb.write(header)
            with open(file_path, "rb") as f:
                deb.write(f.read())
            if size % 2 == 1:
                deb.write(b"\n")

    shutil.rmtree(tmp_dir)

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: package_deb.py <build_root> <output_deb>", file=sys.stderr)
        sys.exit(1)
    build_deb(sys.argv[1], sys.argv[2])
