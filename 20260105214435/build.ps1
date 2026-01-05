$ErrorActionPreference = "Continue"
$mavenPath = "D:\app\ideaIC-2023.2.6.win\plugins\maven\lib\maven3\bin\mvn.cmd"
$projectDir = "c:\Users\yysyp\CodeBuddy\20260105214435"

Write-Host "Changing to project directory: $projectDir"
Set-Location $projectDir

Write-Host "Running Maven clean compile..."
& $mavenPath clean compile

if ($LASTEXITCODE -eq 0) {
    Write-Host "Build successful! Running package..."
    & $mavenPath package -DskipTests

    if ($LASTEXITCODE -eq 0) {
        Write-Host "Package successful! Starting application..."
        & $mavenPath spring-boot:run
    } else {
        Write-Host "Package failed with exit code: $LASTEXITCODE"
    }
} else {
    Write-Host "Compile failed with exit code: $LASTEXITCODE"
}
