# Bongo Windows Developer and Local Installation Script
# Installs Python dependencies and runs Bongo in background mode

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  Bongo for Windows - Local Setup" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$WindowsDir = Split-Path -Parent $ScriptDir
$ServiceScript = Join-Path $WindowsDir "service\bongo_service.py"
$SettingsScript = Join-Path $WindowsDir "ui\bongo_settings.py"

# Verify Python
try {
    $pythonVersion = python --version
    Write-Host "Found Python: $pythonVersion" -ForegroundColor Green
} catch {
    Write-Error "Python is not installed or not in PATH."
    exit 1
}

# Run Settings UI or Service
if ($args.Count -gt 0 -and $args[0] -eq "--settings") {
    Write-Host "Launching Bongo Settings..." -ForegroundColor Yellow
    python $SettingsScript
} else {
    Write-Host "Starting Bongo Input Service (Press F12 to toggle Bangla)..." -ForegroundColor Green
    python $ServiceScript
}
