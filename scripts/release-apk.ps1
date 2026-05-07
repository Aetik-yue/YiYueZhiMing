param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^\d+\.\d+\.\d+$')]
    [string]$Version,

    [Parameter(Mandatory = $true)]
    [string]$Notes
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$gradle = Join-Path $env:USERPROFILE ".gradle\wrapper\dists\gradle-8.12-bin\cetblhg4pflnnks72fxwobvgv\gradle-8.12\bin\gradle.bat"
$jdk = Join-Path $env:USERPROFILE ".jdks\temurin-24"
$apkSource = Join-Path $repoRoot "app\build\outputs\apk\debug\app-debug.apk"
$versionDir = Join-Path $repoRoot "version\$Version"
$apkTarget = Join-Path $versionDir "以越之名.apk"
$notesTarget = Join-Path $versionDir "更新日志.md"
$ghCommand = Get-Command gh -ErrorAction SilentlyContinue
$ghExe = if ($null -ne $ghCommand) { $ghCommand.Source } else { $null }
if ($null -eq $ghExe) {
    $ghPath = "C:\Program Files\GitHub CLI\gh.exe"
    if (Test-Path -LiteralPath $ghPath) {
        $ghExe = $ghPath
    }
}

if (-not (Test-Path -LiteralPath $gradle)) {
    throw "未找到 Gradle：$gradle"
}

if (-not (Test-Path -LiteralPath $jdk)) {
    throw "未找到 JDK：$jdk"
}

if (Test-Path -LiteralPath $versionDir) {
    throw "版本目录已存在：$versionDir"
}

if ($null -eq $ghExe) {
    throw "未找到 GitHub CLI，请先安装 gh。"
}

$env:JAVA_HOME = $jdk

Push-Location $repoRoot
try {
    & $gradle ":app:assembleDebug"
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle 构建失败"
    }

    New-Item -ItemType Directory -Force -Path $versionDir | Out-Null
    Copy-Item -LiteralPath $apkSource -Destination $apkTarget -Force

    $date = Get-Date -Format "yyyy-MM-dd"
    @"
# 以越之名 $Version 更新日志

发布日期：$date

## 更新内容

$Notes

## APK

- ``以越之名.apk``
"@ | Set-Content -LiteralPath $notesTarget -Encoding UTF8

    git status --short | Out-Host
    git add README.md LICENSE .gitignore scripts/release-apk.ps1 build.gradle.kts settings.gradle.kts gradle.properties app version
    git commit -m "Release $Version"
    git tag "v$Version"
    git push origin main --tags
    & $ghExe release create "v$Version" $apkTarget --title "以越之名 $Version" --notes-file $notesTarget
}
finally {
    Pop-Location
}
