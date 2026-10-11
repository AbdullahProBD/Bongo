@echo off
rem ============================================================================
rem Bongo Windows Build Script
rem Builds Rust engine DLL, packages Python binaries, and generates Inno Setup
rem EXE and WiX MSI release installers.
rem ============================================================================

setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
set WINDOWS_ROOT=%SCRIPT_DIR%..
set PROJECT_ROOT=%WINDOWS_ROOT%\..\..
set BUILD_DIR=%WINDOWS_ROOT%\build
set RELEASE_DIR=%WINDOWS_ROOT%\release

if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"
if not exist "%RELEASE_DIR%" mkdir "%RELEASE_DIR%"

echo [1/5] Checking environment and prerequisites...

where python >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Error: python is not installed or not in PATH.
    exit /b 1
)

echo [2/5] Checking Rust engine...
where cargo >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo Building Rust bongo_engine.dll...
    cargo build --manifest-path "%WINDOWS_ROOT%\engine\Cargo.toml" --release
    if exist "%WINDOWS_ROOT%\engine\target\release\bongo_engine.dll" (
        copy /y "%WINDOWS_ROOT%\engine\target\release\bongo_engine.dll" "%BUILD_DIR%\"
        echo Built bongo_engine.dll successfully.
    )
) else (
    echo Note: cargo not found in PATH. Using offline built-in Python phonetic engine.
)

echo [3/5] Compiling Python executables with PyInstaller...
python -m pip install pyinstaller --quiet

python -m PyInstaller --noconfirm --clean --onefile --noconsole ^
    --name bongo ^
    --icon "%WINDOWS_ROOT%\data\bongo.ico" ^
    --distpath "%BUILD_DIR%" ^
    "%WINDOWS_ROOT%\service\bongo_service.py"

python -m PyInstaller --noconfirm --clean --onefile --noconsole ^
    --name bongo-settings ^
    --icon "%WINDOWS_ROOT%\data\bongo.ico" ^
    --distpath "%BUILD_DIR%" ^
    "%WINDOWS_ROOT%\ui\bongo_settings.py"

echo [4/5] Building Inno Setup EXE installer...
set "ISCC_CMD="
where iscc >nul 2>&1 && set "ISCC_CMD=iscc"
if not defined ISCC_CMD if exist "%LOCALAPPDATA%\Programs\Inno Setup 6\ISCC.exe" set "ISCC_CMD=%LOCALAPPDATA%\Programs\Inno Setup 6\ISCC.exe"
if not defined ISCC_CMD if exist "%ProgramFiles(x86)%\Inno Setup 6\ISCC.exe" set "ISCC_CMD=%ProgramFiles(x86)%\Inno Setup 6\ISCC.exe"
if not defined ISCC_CMD if exist "%ProgramFiles%\Inno Setup 6\ISCC.exe" set "ISCC_CMD=%ProgramFiles%\Inno Setup 6\ISCC.exe"

if defined ISCC_CMD (
    "%ISCC_CMD%" "%WINDOWS_ROOT%\installer\bongo.iss"
    echo Built Inno Setup installer in %RELEASE_DIR%
)
if not defined ISCC_CMD echo Note: Inno Setup compiler iscc not found. Skipping EXE installer creation.

echo [5/5] Building WiX MSI installer...
set "WIX_EXE="
where wix >nul 2>&1 && set "WIX_EXE=wix"
if not defined WIX_EXE if exist "%ProgramFiles%\WiX Toolset v7.0\bin\wix.exe" set "WIX_EXE=%ProgramFiles%\WiX Toolset v7.0\bin\wix.exe"
if not defined WIX_EXE if exist "%ProgramFiles%\WiX Toolset v4\bin\wix.exe" set "WIX_EXE=%ProgramFiles%\WiX Toolset v4\bin\wix.exe"

set "CANDLE_EXE="
where candle >nul 2>&1 && set "CANDLE_EXE=candle"

if defined CANDLE_EXE (
    candle -arch x64 -out "%BUILD_DIR%\bongo.wixobj" "%WINDOWS_ROOT%\installer\bongo.wxs"
    light -ext WixUIExtension -out "%RELEASE_DIR%\Bongo-1.0.0-x64.msi" "%BUILD_DIR%\bongo.wixobj"
    echo Built WiX MSI installer in %RELEASE_DIR%
)
if not defined CANDLE_EXE if defined WIX_EXE (
    "%WIX_EXE%" build -arch x64 -out "%RELEASE_DIR%\Bongo-1.0.0-x64.msi" "%WINDOWS_ROOT%\installer\bongo.wxs"
    echo Built WiX MSI installer in %RELEASE_DIR%
)
if not defined CANDLE_EXE if not defined WIX_EXE echo Note: WiX toolset not found. Skipping MSI installer creation.

echo Generating SHA256 checksums...
powershell -Command "if (Test-Path '%RELEASE_DIR%\*.exe') { Get-ChildItem '%RELEASE_DIR%\*.exe' | ForEach-Object { (Get-FileHash $_.FullName -Algorithm SHA256).Hash.ToLower() + '  ' + $_.Name | Set-Content ($_.FullName + '.sha256') } }"
powershell -Command "if (Test-Path '%RELEASE_DIR%\*.msi') { Get-ChildItem '%RELEASE_DIR%\*.msi' | ForEach-Object { (Get-FileHash $_.FullName -Algorithm SHA256).Hash.ToLower() + '  ' + $_.Name | Set-Content ($_.FullName + '.sha256') } }"

echo ============================================================================
echo Windows build finished successfully!
echo Artifacts located in: %RELEASE_DIR%
echo ============================================================================
