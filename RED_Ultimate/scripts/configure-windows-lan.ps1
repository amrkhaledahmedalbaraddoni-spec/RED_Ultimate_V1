param(
    [ValidateRange(1024, 65535)][int]$HttpPort = 8088,
    [switch]$TrustCurrentNetwork,
    [switch]$EnableMediaPorts
)
$ErrorActionPreference = 'Stop'
$principal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
if (-not $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    $arguments = @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', "`"$PSCommandPath`"", '-HttpPort', $HttpPort)
    if ($TrustCurrentNetwork) { $arguments += '-TrustCurrentNetwork' }
    if ($EnableMediaPorts) { $arguments += '-EnableMediaPorts' }
    Start-Process powershell.exe -Verb RunAs -ArgumentList $arguments
    Write-Host 'Requested administrator permission in a new PowerShell window.'
    exit 0
}

$profiles = @(Get-NetConnectionProfile | Where-Object { $_.IPv4Connectivity -ne 'Disconnected' })
$public = @($profiles | Where-Object NetworkCategory -eq 'Public')
if ($public.Count -gt 0 -and -not $TrustCurrentNetwork) {
    throw "The active network is Public. Rerun with -TrustCurrentNetwork only if this is your trusted private LAN/hotspot."
}
if ($TrustCurrentNetwork) {
    $public | ForEach-Object { Set-NetConnectionProfile -InterfaceIndex $_.InterfaceIndex -NetworkCategory Private }
}

function Ensure-Rule([string]$Name, [string]$Protocol, [string]$Ports) {
    Get-NetFirewallRule -DisplayName $Name -ErrorAction SilentlyContinue | Remove-NetFirewallRule
    New-NetFirewallRule -DisplayName $Name -Direction Inbound -Protocol $Protocol -LocalPort $Ports -Action Allow -Profile Private | Out-Null
    Write-Host "FIREWALL_RULE_READY $Name $Protocol/$Ports Private"
}
Ensure-Rule 'RED Local HTTPS-Alpha' 'TCP' "$HttpPort"
if ($EnableMediaPorts) {
    Ensure-Rule 'RED TURN' 'UDP' '3478'
    Ensure-Rule 'RED SFU RTP' 'UDP' '40000-40100'
    Ensure-Rule 'RED TURN Relay' 'UDP' '45000-45050'
}
Write-Host 'RED_WINDOWS_LAN_READY'
