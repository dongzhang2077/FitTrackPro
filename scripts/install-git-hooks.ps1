$ErrorActionPreference = "Stop"

$repoRoot = (git rev-parse --show-toplevel).Trim()
$sourceHook = Join-Path $repoRoot ".githooks\pre-push"
$targetDir = Join-Path $repoRoot ".git\hooks"
$targetHook = Join-Path $targetDir "pre-push"

if (!(Test-Path $sourceHook)) {
    throw "Missing hook source at $sourceHook"
}

New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
Copy-Item -Path $sourceHook -Destination $targetHook -Force

Write-Host "Installed pre-push hook: $targetHook"
