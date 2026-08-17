param(
    [Parameter(Mandatory = $true)]
    [ValidateSet(1, 2)]
    [int]$Workshop
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot

$javaExe = Get-ChildItem -Recurse -Filter "java.exe" "$env:USERPROFILE\.gradle\jdks" -ErrorAction SilentlyContinue |
    Select-Object -First 1 -ExpandProperty FullName

if (-not $javaExe) {
    Write-Host "No provisioned JDK found. Run '.\gradlew.bat compileKotlin' once first, then retry."
    exit 1
}

$classpathOutput = & "$repoRoot\gradlew.bat" -q --console=plain workshopClasspath
$classpath = ($classpathOutput | Select-Object -Last 1).Trim()

$mainClass = "org.example.Workshop${Workshop}Kt"

& $javaExe @("-Dstdout.encoding=UTF-8", "-cp", $classpath, $mainClass)
