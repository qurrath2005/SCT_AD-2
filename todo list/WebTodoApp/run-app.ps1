# PowerShell script to run the Smart To-Do List app

Write-Host "Starting Smart To-Do List App..." -ForegroundColor Green

# Get the current directory
$currentDir = Get-Location
$appPath = Join-Path -Path $currentDir -ChildPath "index.html"

# Check if Python is available
$pythonAvailable = $false
try {
    $pythonVersion = python --version
    $pythonAvailable = $true
    Write-Host "Python detected: $pythonVersion" -ForegroundColor Cyan
} catch {
    Write-Host "Python not found. Will use direct file opening instead." -ForegroundColor Yellow
}

# Try to run with Python HTTP server if available
if ($pythonAvailable) {
    Write-Host "Starting Python HTTP server on port 8000..." -ForegroundColor Cyan
    Write-Host "Access the app at: http://localhost:8000" -ForegroundColor Green
    Write-Host "Press Ctrl+C to stop the server when done." -ForegroundColor Yellow
    
    # Start Python HTTP server
    python -m http.server 8000
} else {
    # Open the file directly in the default browser
    Write-Host "Opening app directly in browser..." -ForegroundColor Cyan
    Start-Process $appPath
    
    Write-Host "App opened in your default browser." -ForegroundColor Green
    Write-Host "Press Enter to exit..." -ForegroundColor Yellow
    Read-Host
}
