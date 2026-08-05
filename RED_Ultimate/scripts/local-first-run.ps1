param(
    [Parameter(Mandatory = $true)][string]$ServerIp,
    [switch]$BuildAndroid
)
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$RepoRoot = Split-Path -Parent $Root
$EnvFile = Join-Path $Root ".env"

if ($ServerIp -notmatch '^([0-9]{1,3}\.){3}[0-9]{1,3}$') { throw "ServerIp must be a local IPv4 address" }
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { throw "Docker Desktop is required" }
& docker info *> $null
if ($LASTEXITCODE -ne 0) { throw "Docker Desktop is not running" }
& docker compose version *> $null
if ($LASTEXITCODE -ne 0) { throw "Docker Compose v2 is required" }

$DockerMemoryBytes = [double](& docker info --format '{{.MemTotal}}')
if ($LASTEXITCODE -ne 0) { throw "Unable to read Docker memory limit" }
$DockerMemoryGiB = $DockerMemoryBytes / 1GB
if ($DockerMemoryGiB -lt 5.5) {
    throw ("Docker has only {0:N1} GiB available. RED needs at least 5.5 GiB available after VM overhead (6 GiB configured; 8 GiB recommended), then restart Docker Desktop and retry." -f $DockerMemoryGiB)
}
Write-Host ("Docker memory preflight: {0:N1} GiB PASS" -f $DockerMemoryGiB)

function New-Hex([int]$Bytes) {
    $buffer = New-Object byte[] $Bytes
    $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($buffer) } finally { $rng.Dispose() }
    return (-join ($buffer | ForEach-Object { $_.ToString("x2") }))
}

$OpenSslExe = $null
$OpenSslCommand = Get-Command openssl -ErrorAction SilentlyContinue
if ($OpenSslCommand) { $OpenSslExe = $OpenSslCommand.Source }
if (-not $OpenSslExe) {
    $OpenSslCandidates = @(
        (Join-Path $env:ProgramFiles "Git\usr\bin\openssl.exe"),
        (Join-Path $env:ProgramFiles "Git\mingw64\bin\openssl.exe")
    )
    $OpenSslExe = $OpenSslCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1
}

if (-not (Test-Path $EnvFile)) {
    $text = Get-Content (Join-Path $Root ".env.example") -Raw
    $replacements = @{
        "replace_with_a_long_random_database_password" = (New-Hex 32)
        "replace_with_a_long_random_mongodb_password" = (New-Hex 32)
        "replace_with_a_long_random_minio_password" = (New-Hex 32)
        "replace_with_a_long_random_redis_password" = (New-Hex 32)
        "replace_with_a_long_random_asterisk_password" = (New-Hex 32)
        "replace_with_a_long_random_turn_secret" = (New-Hex 32)
        "replace_with_at_least_32_random_characters" = (New-Hex 48)
        "replace_with_at_least_14_random_characters" = (New-Hex 20)
        "replace_with_the_gateway_password" = (New-Hex 24)
        "192.168.1.50" = $ServerIp
    }
    foreach ($entry in $replacements.GetEnumerator()) { $text = $text.Replace($entry.Key, $entry.Value) }
    [IO.File]::WriteAllText($EnvFile, $text, [Text.UTF8Encoding]::new($false))
    Write-Host "Created private RED_Ultimate/.env"
} else {
    Write-Host "Using existing RED_Ultimate/.env (not overwritten)."
}

$Secrets = Join-Path $Root "secrets"
$PrivateKey = Join-Path $Secrets "red_identity_private_key.pem"
$PublicKey = Join-Path $Secrets "red_identity_public_key.pem"
if (-not (Test-Path $PrivateKey)) {
    New-Item -ItemType Directory -Force $Secrets | Out-Null
    if ($OpenSslExe) {
        Write-Host "Generating identity authority with local OpenSSL: $OpenSslExe"
        & $OpenSslExe genpkey -algorithm EC -pkeyopt ec_paramgen_curve:P-256 -out $PrivateKey
        if ($LASTEXITCODE -ne 0) { throw "Identity private key generation failed" }
        & $OpenSslExe pkey -in $PrivateKey -pubout -out $PublicKey
        if ($LASTEXITCODE -ne 0) { throw "Identity public key generation failed" }
    } else {
        Write-Host "Host OpenSSL not found; generating identity authority inside an ephemeral Alpine container."
        & docker run --rm --volume "${Secrets}:/keys" alpine:3.20 sh -ec "apk add --no-cache openssl >/dev/null; umask 077; openssl genpkey -algorithm EC -pkeyopt ec_paramgen_curve:P-256 -out /keys/red_identity_private_key.pem; openssl pkey -in /keys/red_identity_private_key.pem -pubout -out /keys/red_identity_public_key.pem"
        if ($LASTEXITCODE -ne 0) { throw "Containerized identity key generation failed" }
    }
    if (-not (Test-Path $PrivateKey) -or -not (Test-Path $PublicKey)) { throw "Identity authority files were not created" }
    Write-Host "Created local identity authority keys; back up secrets securely."
} else {
    Write-Host "Using existing identity authority keys (not overwritten)."
}

Push-Location $Root
try {
    & docker compose --env-file $EnvFile config --quiet
    if ($LASTEXITCODE -ne 0) { throw "Docker Compose validation failed" }
    Write-Host "Docker Compose configuration: PASS"
    & docker compose --env-file $EnvFile build
    if ($LASTEXITCODE -ne 0) { throw "Docker Compose build failed" }
    & docker compose --env-file $EnvFile up -d
    if ($LASTEXITCODE -ne 0) { throw "Docker Compose startup failed" }

    Write-Host -NoNewline "Waiting for backend"
    $healthy = $false
    foreach ($attempt in 1..60) {
        try {
            $response = Invoke-WebRequest -Uri "http://127.0.0.1/health" -UseBasicParsing -TimeoutSec 3
            if ($response.StatusCode -eq 200) { $healthy = $true; break }
        } catch { }
        Write-Host -NoNewline "."
        Start-Sleep -Seconds 3
    }
    if (-not $healthy) {
        & docker compose --env-file $EnvFile ps
        & docker compose --env-file $EnvFile logs --tail=120 backend
        throw "Backend did not become healthy"
    }
    Write-Host " PASS"
    $sfu = Invoke-WebRequest -Uri "http://127.0.0.1/sfu-health" -UseBasicParsing -TimeoutSec 5
    if ($sfu.StatusCode -ne 200) { throw "SFU health failed" }
    Write-Host "SFU health: PASS"
} finally { Pop-Location }

if ($BuildAndroid) {
    Push-Location $RepoRoot
    try {
        & docker build --file Dockerfile --build-arg "RED_SERVER_URL=http://$ServerIp" --tag red-local:latest .
        if ($LASTEXITCODE -ne 0) { throw "Verified Android artifact build failed" }
        & docker rm -f red-artifacts 2>$null | Out-Null
        & docker create --name red-artifacts red-local:latest | Out-Null
        $Artifacts = Join-Path $RepoRoot "local-artifacts"
        New-Item -ItemType Directory -Force $Artifacts | Out-Null
        & docker cp "red-artifacts:/app/app.jar" (Join-Path $Artifacts "app.jar")
        & docker cp "red-artifacts:/opt/red-app-debug.apk" (Join-Path $Artifacts "red-app-debug.apk")
        & docker rm red-artifacts | Out-Null
        Write-Host "APK and app.jar saved under local-artifacts."
    } finally { Pop-Location }
}

Write-Host ""
Write-Host "RED local first run is ready: http://$ServerIp/"
Write-Host "Health endpoint: http://$ServerIp/health"
Write-Host "Admin credentials remain only in RED_Ultimate/.env."
