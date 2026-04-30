$ErrorActionPreference = "Stop"

$jdkYolu = $env:JAVA_HOME
if ([string]::IsNullOrWhiteSpace($jdkYolu)) {
    $jdkYolu = "C:\Users\IMHek\OneDrive\Documents\JDK\java-1.8.0-openjdk-1.8.0.492.b09-1.win.jdk.x86_64"
}

$javac = Join-Path $jdkYolu "bin\javac.exe"
$java = Join-Path $jdkYolu "bin\java.exe"

if (!(Test-Path $javac)) {
    throw "javac bulunamadi: $javac"
}

if (!(Test-Path "target\classes")) {
    New-Item -ItemType Directory -Path "target\classes" | Out-Null
}

$kaynakDosyalari = Get-ChildItem -Path "src\main\java" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
& $javac -encoding UTF-8 -d "target\classes" $kaynakDosyalari
& $java -cp "target\classes" com.futbolanaliz.uygulama.BakimKontrolUygulamasi
