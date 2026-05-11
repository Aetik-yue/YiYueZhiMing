param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^\d+\.\d+\.\d+$')]
    [string]$Version,

    [Parameter(Mandatory = $true)]
    [string]$Notes
)

$ErrorActionPreference = "Stop"

function Join-Chars([int[]]$Codes) {
    return -join ($Codes | ForEach-Object { [char]$_ })
}

$appName = Join-Chars @(0x4ee5, 0x8d8a, 0x4e4b, 0x540d)
$changeLog = Join-Chars @(0x66f4, 0x65b0, 0x65e5, 0x5fd7)
$releaseDateLabel = Join-Chars @(0x53d1, 0x5e03, 0x65e5, 0x671f, 0xff1a)
$updatesTitle = Join-Chars @(0x66f4, 0x65b0, 0x5185, 0x5bb9)

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$gradle = Join-Path $env:USERPROFILE ".gradle\wrapper\dists\gradle-8.12-bin\cetblhg4pflnnks72fxwobvgv\gradle-8.12\bin\gradle.bat"
$jdk = Join-Path $env:USERPROFILE ".jdks\temurin-24"
$apkSource = Join-Path $repoRoot "app\build\outputs\apk\debug\app-debug.apk"
$appGradle = Join-Path $repoRoot "app\build.gradle.kts"
$versionDir = Join-Path $repoRoot "version\$Version"
$apkTarget = Join-Path $versionDir "$appName.apk"
$releaseUploadTarget = Join-Path $versionDir "YiYueZhiMing-$Version.apk"
$notesTarget = Join-Path $versionDir "$changeLog.md"
$ghCommand = Get-Command gh -ErrorAction SilentlyContinue
$ghExe = if ($null -ne $ghCommand) { $ghCommand.Source } else { $null }
if ($null -eq $ghExe) {
    $ghPath = "C:\Program Files\GitHub CLI\gh.exe"
    if (Test-Path -LiteralPath $ghPath) {
        $ghExe = $ghPath
    }
}

if (-not (Test-Path -LiteralPath $gradle)) {
    throw "Gradle was not found: $gradle"
}

if (-not (Test-Path -LiteralPath $jdk)) {
    throw "JDK was not found: $jdk"
}

if (Test-Path -LiteralPath $versionDir) {
    throw "Version directory already exists: $versionDir"
}

if ($null -eq $ghExe) {
    throw "GitHub CLI was not found. Install gh first."
}

$env:JAVA_HOME = $jdk

Push-Location $repoRoot
try {
    $parts = $Version.Split(".")
    $versionCode = ([int]$parts[0] * 100) + ([int]$parts[1] * 10) + [int]$parts[2]
    $gradleText = Get-Content -LiteralPath $appGradle -Raw
    $gradleText = [regex]::Replace($gradleText, 'versionCode\s*=\s*\d+', "versionCode = $versionCode")
    $gradleText = [regex]::Replace($gradleText, 'versionName\s*=\s*"[^"]+"', "versionName = `"$Version`"")
    Set-Content -LiteralPath $appGradle -Value $gradleText -Encoding UTF8

    & $gradle ":app:assembleDebug"
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed"
    }

    New-Item -ItemType Directory -Force -Path $versionDir | Out-Null
    Copy-Item -LiteralPath $apkSource -Destination $apkTarget -Force

    $date = Get-Date -Format "yyyy-MM-dd"
    $notesContent = @(
        "# $appName $Version $changeLog",
        "",
        "$releaseDateLabel$date",
        "",
        "## $updatesTitle",
        "",
        $Notes,
        "",
        "## APK",
        "",
        "- ``$appName.apk``"
    ) -join [Environment]::NewLine
    Set-Content -LiteralPath $notesTarget -Value $notesContent -Encoding UTF8

    git status --short | Out-Host
    git add README.md LICENSE .gitignore scripts/release-apk.ps1 build.gradle.kts settings.gradle.kts gradle.properties app version
    git commit -m "Release $Version"
    git tag "v$Version"
    git push origin main --tags
    & $ghExe release create "v$Version" --title "$appName $Version" --notes-file $notesTarget
    Copy-Item -LiteralPath $apkTarget -Destination $releaseUploadTarget -Force
    try {
        & $ghExe release upload "v$Version" "$releaseUploadTarget#$appName.apk" --clobber
    }
    finally {
        if (Test-Path -LiteralPath $releaseUploadTarget) {
            Remove-Item -LiteralPath $releaseUploadTarget -Force
        }
    }
}
finally {
    Pop-Location
}
