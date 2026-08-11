$ErrorActionPreference = "Stop"

$projectDirectory = $PSScriptRoot
$jdkDirectory = "C:\Program Files\Java\jdk-17"
$mavenCommand = "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd"
$packageInput = Join-Path $projectDirectory "target\jpackage-input"
$distributionDirectory = Join-Path $projectDirectory "dist"
$applicationIcon = Join-Path $projectDirectory "src\main\resources\icons\kobo-manager.ico"

if (-not (Test-Path -LiteralPath "$jdkDirectory\bin\jpackage.exe")) {
    throw "No se encontro jpackage en $jdkDirectory. Instala o configura JDK 17."
}

if (-not (Test-Path -LiteralPath $mavenCommand)) {
    throw "No se encontro Maven en $mavenCommand."
}

if (-not (Test-Path -LiteralPath $applicationIcon)) {
    throw "No se encontro el icono de la aplicacion en $applicationIcon."
}

$env:JAVA_HOME = $jdkDirectory
$env:Path = "$jdkDirectory\bin;$env:Path"

Write-Host "1/3 Ejecutando pruebas y creando el JAR..." -ForegroundColor Cyan
& $mavenCommand clean package
if ($LASTEXITCODE -ne 0) {
    throw "La compilacion o las pruebas han fallado."
}

if (Test-Path -LiteralPath $packageInput) {
    Remove-Item -LiteralPath $packageInput -Recurse -Force
}
New-Item -ItemType Directory -Path $packageInput | Out-Null
Copy-Item -LiteralPath "$projectDirectory\target\KoboManager.jar" -Destination $packageInput

if (Test-Path -LiteralPath "$distributionDirectory\KoboManager") {
    Remove-Item -LiteralPath "$distributionDirectory\KoboManager" -Recurse -Force
}
New-Item -ItemType Directory -Path $distributionDirectory -Force | Out-Null

Write-Host "2/3 Creando la aplicacion portable..." -ForegroundColor Cyan
& "$jdkDirectory\bin\jpackage.exe" `
    --type app-image `
    --dest $distributionDirectory `
    --input $packageInput `
    --name KoboManager `
    --main-jar KoboManager.jar `
    --main-class com.arcac.managerkobo.app.Main `
    --icon $applicationIcon `
    --app-version 1.0.0 `
    --vendor "arcac" `
    --description "Exploracion y analisis de datos de lectura de Kobo" `
    --java-options "-Dfile.encoding=UTF-8"

if ($LASTEXITCODE -ne 0) {
    throw "No se pudo crear la aplicacion portable."
}

Write-Host "3/3 Aplicacion creada correctamente:" -ForegroundColor Green
Write-Host "$distributionDirectory\KoboManager\KoboManager.exe"
Write-Host "Abrela, comprueba su funcionamiento y comprime la carpeta KoboManager para compartirla."
