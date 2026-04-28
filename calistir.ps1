$ErrorActionPreference = "Stop"

$projeKok = Split-Path -Parent $MyInvocation.MyCommand.Path
$jdkYolu = "C:\Users\IMHek\OneDrive\Documents\JDK\java-1.8.0-openjdk-1.8.0.492.b09-1.win.jdk.x86_64"
$kaynakKlasoru = Join-Path $projeKok "src\main\java"
$ciktiKlasoru = Join-Path $projeKok "target\classes"
$logKlasoru = Join-Path $projeKok "kaynaklar"
$standartLog = Join-Path $logKlasoru "son-calisma.log"
$hataLog = Join-Path $logKlasoru "son-calisma-hata.log"
$konsolModu = $args -contains "--konsol"

$env:JAVA_HOME = $jdkYolu
$env:Path = "$jdkYolu\bin;$env:Path"

if (-not (Test-Path $ciktiKlasoru)) {
    New-Item -ItemType Directory -Path $ciktiKlasoru | Out-Null
}

if (-not (Test-Path $logKlasoru)) {
    New-Item -ItemType Directory -Path $logKlasoru | Out-Null
}

$kaynakDosyalari = Get-ChildItem -Path $kaynakKlasoru -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }

& "$jdkYolu\bin\javac.exe" -encoding UTF-8 -d $ciktiKlasoru $kaynakDosyalari

if ($konsolModu) {
    & "$jdkYolu\bin\java.exe" "-Dfile.encoding=UTF-8" -cp $ciktiKlasoru com.futbolanaliz.uygulama.Uygulama --konsol
    Write-Host ""
    Write-Host "Uygulama kapandı. Pencereyi kapatmak için bir tuşa basın..."
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
} else {
    Remove-Item $standartLog, $hataLog -ErrorAction SilentlyContinue
    $javaArgumanlari = "-Dfile.encoding=UTF-8 -cp `"$ciktiKlasoru`" com.futbolanaliz.uygulama.Uygulama"
    Start-Process -FilePath "$jdkYolu\bin\javaw.exe" `
        -ArgumentList $javaArgumanlari `
        -WorkingDirectory $projeKok `
        -RedirectStandardOutput $standartLog `
        -RedirectStandardError $hataLog
}
