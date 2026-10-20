#!/usr/bin/env bash
#
# Bongo Linux One-Command Installer
# Supports one-line pipe installation via curl (installs prebuilt deb package or builds from source),
# as well as local in-tree compilation and installation.
#

set -euo pipefail

SCRIPT_DIR=""
if [[ -n "${BASH_SOURCE[0]:-}" ]]; then
    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd || true)"
fi

# Detect whether running locally inside cloned repository
IS_LOCAL=false
if [[ -n "${SCRIPT_DIR}" && -f "${SCRIPT_DIR}/../ibus/bongo.xml" ]]; then
    IS_LOCAL=true
fi

# ========================================================
# Remote Mode (e.g. curl -sSL ... | bash)
# ========================================================
if [[ "${IS_LOCAL}" != "true" ]]; then
    echo "=================================================="
    echo "           Bongo Linux Installation               "
    echo "=================================================="

    ARCH="$(uname -m)"
    case "${ARCH}" in
        x86_64|amd64)
            DEB_ARCH="amd64"
            ;;
        aarch64|arm64)
            DEB_ARCH="arm64"
            ;;
        *)
            echo "Error: Unsupported CPU architecture: ${ARCH}" >&2
            exit 1
            ;;
    esac

    # Determine sudo prefix if not root
    SUDO=""
    if [[ $EUID -ne 0 ]]; then
        if command -v sudo >/dev/null 2>&1; then
            SUDO="sudo"
        else
            echo "Error: Root privileges or sudo required to install Bongo." >&2
            exit 1
        fi
    fi

    # On Debian/Ubuntu/Mint/Pop!_OS: install official prebuilt .deb package
    if command -v apt-get >/dev/null 2>&1 || command -v dpkg >/dev/null 2>&1; then
        echo "--> Detected Debian / Ubuntu based system (${DEB_ARCH})"
        TMP_DEB="/tmp/bongo_${DEB_ARCH}.deb"
        DEB_URL="https://github.com/mehedishakeel/Bongo/releases/latest/download/bongo_1.0.1_${DEB_ARCH}.deb"
        FALLBACK_URL="https://github.com/mehedishakeel/Bongo/releases/latest/download/bongo_1.0.0_${DEB_ARCH}.deb"

        echo "--> Downloading ${DEB_URL}..."
        if ! curl -sSL -f -o "${TMP_DEB}" "${DEB_URL}"; then
            echo "--> Trying ${FALLBACK_URL}..."
            curl -sSL -f -o "${TMP_DEB}" "${FALLBACK_URL}" || {
                echo "Error: Failed to download release package from GitHub." >&2
                exit 1
            }
        fi

        echo "--> Installing Bongo and resolving dependencies..."
        if command -v apt-get >/dev/null 2>&1; then
            ${SUDO} apt-get update -qq || true
            ${SUDO} apt-get install -y "${TMP_DEB}"
        else
            ${SUDO} dpkg -i "${TMP_DEB}"
        fi
        rm -f "${TMP_DEB}"

        if command -v ibus >/dev/null 2>&1; then
            echo "--> Refreshing IBus component registry..."
            ibus write-cache 2>/dev/null || true
            ibus restart 2>/dev/null || true
        fi

        echo "=================================================="
        echo "   Bongo has been successfully installed!        "
        echo "=================================================="
        echo ""
        echo "Next Steps:"
        echo "1. Open GNOME / KDE Settings -> Keyboard -> Input Sources."
        echo "2. Add 'Bengali (Bongo)' to your active keyboards."
        echo "3. Switch keyboards anytime with Super + Space or Control + Space."
        echo "4. Launch 'bongo-settings' to customize typing modes and fonts."
        echo ""
        exit 0
    fi

    # On non-Debian distributions: clone source and run installer
    echo "--> Non-Debian distribution detected (${ARCH}). Installing from source..."
    TMP_SRC="$(mktemp -d /tmp/bongo-src-XXXXXX)"
    git clone --depth=1 https://github.com/mehedishakeel/Bongo.git "${TMP_SRC}"
    bash "${TMP_SRC}/platforms/linux/scripts/install.sh"
    rm -rf "${TMP_SRC}"
    exit 0
fi

# ========================================================
# Local In-Tree Build & Install Mode
# ========================================================
LINUX_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
REPO_ROOT="$(cd "${LINUX_ROOT}/../.." && pwd)"

PREFIX="${PREFIX:-/usr}"
DESTDIR="${DESTDIR:-}"

# If running as non-root and PREFIX was not explicitly changed, install to ~/.local
if [[ $EUID -ne 0 && "${PREFIX}" == "/usr" && -z "${DESTDIR}" ]]; then
    PREFIX="${HOME}/.local"
fi

echo "=================================================="
echo "           Bongo Linux Installation               "
echo "=================================================="

# Check for Cargo / Rust (including in user home if running under sudo)
if ! command -v cargo >/dev/null 2>&1; then
    if [[ -n "${SUDO_USER:-}" && -x "/home/${SUDO_USER}/.cargo/bin/cargo" ]]; then
        export PATH="/home/${SUDO_USER}/.cargo/bin:${PATH}"
    elif [[ -x "${HOME}/.cargo/bin/cargo" ]]; then
        export PATH="${HOME}/.cargo/bin:${PATH}"
    fi
fi

if ! command -v cargo >/dev/null 2>&1; then
    echo "Error: cargo (Rust package manager) is required to build Bongo." >&2
    echo "Install Rust via: curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh" >&2
    echo "Or on Ubuntu/Debian: sudo apt install -y cargo rustc" >&2
    exit 1
fi

if ! command -v ibus >/dev/null 2>&1; then
    echo "Warning: IBus was not detected on this system." >&2
    echo "Install dependencies on Ubuntu/Debian via:" >&2
    echo "  sudo apt install -y ibus libibus-1.0-dev python3 python3-gi gir1.2-ibus-1.0 gir1.2-gtk-4.0" >&2
fi

LIB_TARGET="${BONGO_LIB_TARGET:-${LINUX_ROOT}/engine/target/release/libbongo_engine.so}"
if [[ ! -f "${LIB_TARGET}" ]]; then
    echo "--> Building native engine (libbongo_engine.so)..."
    cd "${LINUX_ROOT}/engine"
    cargo build --release
fi

if [[ ! -f "${LIB_TARGET}" ]]; then
    echo "Error: ${LIB_TARGET} not found." >&2
    exit 1
fi

echo "--> Installing components to ${DESTDIR}${PREFIX}..."

# 1. Install library
install -d "${DESTDIR}${PREFIX}/lib/bongo"
install -m 755 "${LIB_TARGET}" "${DESTDIR}${PREFIX}/lib/bongo/libbongo_engine.so"

# 2. Install IBus engine daemon
install -d "${DESTDIR}${PREFIX}/libexec"
install -m 755 "${LINUX_ROOT}/ibus/bongo_ibus_engine.py" "${DESTDIR}${PREFIX}/libexec/bongo-ibus-engine"

# 3. Install IBus component XML
install -d "${DESTDIR}${PREFIX}/share/ibus/component"
if [[ -z "${DESTDIR}" && "${PREFIX}" != "/usr" ]]; then
    sed -e "s|/usr/libexec/bongo-ibus-engine|${PREFIX}/libexec/bongo-ibus-engine|g" \
        -e "s|/usr/bin/bongo-settings|${PREFIX}/bin/bongo-settings|g" \
        -e "s|/usr/share/|${PREFIX}/share/|g" \
        "${LINUX_ROOT}/ibus/bongo.xml" > "${DESTDIR}${PREFIX}/share/ibus/component/bongo.xml"
    chmod 644 "${DESTDIR}${PREFIX}/share/ibus/component/bongo.xml"
else
    install -m 644 "${LINUX_ROOT}/ibus/bongo.xml" "${DESTDIR}${PREFIX}/share/ibus/component/bongo.xml"
fi

# 4. Install GTK Settings & Welcome app
chmod +x "${LINUX_ROOT}/ui/bongo_settings.py"
install -d "${DESTDIR}${PREFIX}/bin"
install -m 755 "${LINUX_ROOT}/ui/bongo_settings.py" "${DESTDIR}${PREFIX}/bin/bongo-settings"

# 5. Install Desktop entry
install -d "${DESTDIR}${PREFIX}/share/applications"
if [[ -z "${DESTDIR}" && "${PREFIX}" != "/usr" ]]; then
    sed "s|^Exec=bongo-settings|Exec=${PREFIX}/bin/bongo-settings|" "${LINUX_ROOT}/data/app.getbongo.Bongo.desktop" > "${DESTDIR}${PREFIX}/share/applications/app.getbongo.Bongo.desktop"
    chmod 644 "${DESTDIR}${PREFIX}/share/applications/app.getbongo.Bongo.desktop"
else
    install -m 644 "${LINUX_ROOT}/data/app.getbongo.Bongo.desktop" "${DESTDIR}${PREFIX}/share/applications/app.getbongo.Bongo.desktop"
fi

# Remove legacy duplicate launcher (installed by older versions) that caused
# Bongo to appear twice in application menus
if [[ -z "${DESTDIR}" ]]; then
    rm -f "${PREFIX}/share/applications/bongo.desktop" 2>/dev/null || true
fi

# 6. Install AppStream metadata
install -d "${DESTDIR}${PREFIX}/share/metainfo"
install -m 644 "${LINUX_ROOT}/data/app.getbongo.Bongo.metainfo.xml" "${DESTDIR}${PREFIX}/share/metainfo/app.getbongo.Bongo.metainfo.xml"

# 7. Install Icons
for size in 16 32 48 64 128 256 512; do
    ICON_SRC="${LINUX_ROOT}/data/icons/hicolor/${size}x${size}/apps/bongo.png"
    if [[ -f "${ICON_SRC}" ]]; then
        install -d "${DESTDIR}${PREFIX}/share/icons/hicolor/${size}x${size}/apps"
        install -m 644 "${ICON_SRC}" "${DESTDIR}${PREFIX}/share/icons/hicolor/${size}x${size}/apps/bongo.png"
    fi
done

# Install pixmaps fallback
install -d "${DESTDIR}${PREFIX}/share/pixmaps"
install -m 644 "${LINUX_ROOT}/data/icons/bongo.png" "${DESTDIR}${PREFIX}/share/pixmaps/bongo.png"

# Update system caches if running on live system
if [[ -z "${DESTDIR}" ]]; then
    echo "--> Updating desktop and icon caches..."
    if command -v update-desktop-database >/dev/null 2>&1; then
        update-desktop-database "${PREFIX}/share/applications" || true
    fi
    if command -v gtk-update-icon-cache >/dev/null 2>&1; then
        gtk-update-icon-cache -f -t "${PREFIX}/share/icons/hicolor" || true
    fi

    # Ensure ~/.local/bin is in PATH for shell sessions
    if [[ "${PREFIX}" == "${HOME}/.local" ]]; then
        if [[ -f "${HOME}/.bashrc" ]] && ! grep -q '\.local/bin' "${HOME}/.bashrc" 2>/dev/null; then
            echo 'export PATH="$HOME/.local/bin:$PATH"' >> "${HOME}/.bashrc"
        fi
        if [[ -f "${HOME}/.profile" ]] && ! grep -q '\.local/bin' "${HOME}/.profile" 2>/dev/null; then
            echo 'export PATH="$HOME/.local/bin:$PATH"' >> "${HOME}/.profile"
        fi
    fi

    # Restart IBus daemon if running
    if command -v ibus >/dev/null 2>&1; then
        echo "--> Refreshing IBus component registry..."
        ibus write-cache || true
        ibus restart || true
    fi
fi

echo "=================================================="
echo "   Bongo has been successfully installed!        "
echo "=================================================="
echo ""
echo "Next Steps:"
echo "1. Open GNOME / KDE Settings -> Keyboard -> Input Sources."
echo "2. Add 'Bengali (Bongo)' to your active keyboards."
echo "3. Switch keyboards anytime with Super + Space or Control + Space."
echo "4. Launch 'bongo-settings' to customize typing modes and fonts."
echo ""
