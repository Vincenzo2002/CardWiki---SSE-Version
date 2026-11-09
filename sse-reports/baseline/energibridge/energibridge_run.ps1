# EnergiBridge measurement series for CardWiki (run from the repository root).
#   varied (default): CSV inputs from jmeter/data/varied.csv, load 200 users, stress 20 -> 2000 -> 20 users
#   fixed (-Data ""): original scenario (search Haupt, deck 1), load 20 users, stress 5 -> 50 -> 5 users
param(
  [ValidateSet("baseline","improved")][string]$Label = "baseline",
  [ValidateSet("load","stress")][string]$Plan = "load",
  [string]$Data = "jmeter/data/varied.csv",
  [int]$Users = 0,                 # 0 = default for the plan and data mode
  [int]$BaseUsers = 0,             # stress only
  [int]$Runs = 10,
  [int]$Interval = 1000,
  [double]$IdleMaxWatts = 2.5,     # calm gate: mean power of the 30 s cool-down before each measurement
  [int]$CalmTries = 10,
  [double]$MaxBinWatts = 0,        # 0 = record the peak 30 s power only; > 0 also rejects runs above it
  [switch]$Smoke,
  [string]$Eb = "C:\tools\energibridge\energibridge.exe",
  [string]$Jmeter = "C:\Users\Asus\Downloads\apache-jmeter-5.6.3\apache-jmeter-5.6.3\bin\jmeter.bat"
)
$ErrorActionPreference = "Stop"
# Logs use English number formatting regardless of the Windows locale
[Threading.Thread]::CurrentThread.CurrentCulture = [Globalization.CultureInfo]::InvariantCulture
$api ="http://localhost:8080/api/v1"
$admin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $admin) { throw "Run this script from an administrator PowerShell (EnergiBridge needs it for the RAPL driver)" }
if ((Get-Service rapl).Status -ne "Running") { throw "Driver 'rapl' is not running" }
Invoke-RestMethod "$api/decks/1" | Out-Null   # backend alive?

$varied = [bool]$Data
if ($Plan -eq "load") {
  if (-not $Users) { $Users = if ($varied) { 200 } else { 20 } }
  $d = if ($Smoke) { 30 } else { 300 }
  $planProps = @("-Jusers=$Users","-Jramp_up=10","-Jduration=$d")
  $total = $d
} else {
  if (-not $Users) { $Users = if ($varied) { 2000 } else { 50 } }
  if (-not $BaseUsers) { $BaseUsers = if ($varied) { 20 } else { 5 } }
  $main = if ($Smoke) { 20 } else { 180 }
  $base = if ($Smoke) { 10 } else { 60 }
  $planProps = @("-Jusers=$Users","-Jramp_up=10","-Jduration=$main",
                 "-Jbase_users=$BaseUsers","-Jbase_ramp_up=5","-Jbase_duration=$base")
  $total = $main + 2 * $base
}
$window = $total + 10

$root = if ($Label -eq "baseline") { "sse-reports/baseline" } else { "sse-reports/sse-improvements" }
$out  = if ($varied) { "$root/energibridge/$Label/$Plan-varied-u$Users" } else { "$root/energibridge/$Label/$Plan" }
if ($Smoke) { $out += "_smoke" }
if (Test-Path "$out/run01.csv") { throw "$out already contains measurements; move them or choose another label" }
New-Item -ItemType Directory -Force $out | Out-Null
New-Item -ItemType Directory -Force "$out/rejected" | Out-Null

if ($varied) {
  # Same checks as jmeter/run_jmeter.py: every pair must exist in the running database
  $pairs = Import-Csv $Data -Encoding UTF8
  if (-not $pairs -or ($pairs[0].PSObject.Properties.Name -join ",") -ne "search,deck_id") { throw "$Data must have the header search,deck_id" }
  foreach ($p in ($pairs | Sort-Object search, deck_id -Unique)) {
    $page = Invoke-RestMethod "$api/decks?name=$([uri]::EscapeDataString($p.search))&limit=10&offset=0"
    if (-not ($page.content | Where-Object { "$($_.id)" -eq $p.deck_id })) { throw "Deck $($p.deck_id) is not in the first search page for '$($p.search)'; update the CSV" }
    if ("$((Invoke-RestMethod "$api/decks/$($p.deck_id)").id)" -ne $p.deck_id) { throw "Deck $($p.deck_id) cannot be opened" }
  }
  Copy-Item $Data "$out/input.csv"
  $inputCsv = (Resolve-Path "$out/input.csv").Path
  $planProps += @("-Jdata_file=$inputCsv","-Jdata_mode=csv","-Jsample_variables=search,deck_id")
  $dataLine = "input: $Data ($($pairs.Count) pairs, sha256 $((Get-FileHash $inputCsv -Algorithm SHA256).Hash.ToLower())), copied to input.csv"
} else {
  $planProps += @("-Jsearch=Haupt","-Jdeck_id=1")
  $dataLine = "input: fixed search=Haupt deck_id=1"
}

function Get-PowerScheme {
  # powercfg prints a localized line; keep the GUID and use the English name of the built-in plans
  $line = powercfg /getactivescheme | Out-String
  $guid = [regex]::Match($line, "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}").Value
  $names = @{ "381b4222-f694-41f0-9685-ff5bb260df2e" = "Balanced";
              "8c5e7fda-e8bf-4a96-9a85-a6e23a8c635c" = "High performance";
              "a1841308-3541-4fab-bc81-f71556f20b4a" = "Power saver" }
  $name = if ($names.ContainsKey($guid)) { $names[$guid] } else { [regex]::Match($line, "\(([^)]*)\)\s*$").Groups[1].Value }
  "$guid ($name)"
}

@(
  "date: $(Get-Date -Format o)",
  "checkout commit: $(git rev-parse HEAD)",
  "branch: $(git branch --show-current)",
  "cpu: $((Get-CimInstance Win32_Processor).Name)",
  "os: $((Get-CimInstance Win32_OperatingSystem).Caption) $((Get-CimInstance Win32_OperatingSystem).Version)",
  "power scheme: $(Get-PowerScheme)",
  "energibridge: $(& $Eb --version)",
  "energibridge sampling interval: $Interval ms (default 200 ms crashed under load)",
  "calm gate: each measurement starts only after a 30 s cool-down with mean CPU power <= $IdleMaxWatts W (max $CalmTries tries), logged in cooldown.txt",
  "in-run filter: $(if ($MaxBinWatts -gt 0) { "a measurement is repeated (max 3 attempts) if any 30 s window exceeds $MaxBinWatts W; rejected attempts are kept in rejected/" } else { "none; peak 30 s power is logged in quality.txt" })",
  "java: $(cmd /c 'java -version 2>&1' | Select-Object -First 1)",
  "plan: ${Plan}_test.jmx $($planProps -join ' ') think_ms=500",
  $dataLine,
  "runs: $Runs, idle ${window} s at start and end, smoke=$Smoke"
) | Set-Content "$out/environment.txt"

$jm = @("-n","-t","jmeter/${Plan}_test.jmx") + $planProps + @("-Jthink_ms=500")

function Get-CpuSnapshot {
  Get-Process -ErrorAction SilentlyContinue | Group-Object Name | ForEach-Object {
    [pscustomobject]@{ Name = $_.Name; CPU = [double](($_.Group | Measure-Object CPU -Sum).Sum) }
  }
}
function Write-ProcDelta($before, $after, $file) {
  $b = @{}; $before | ForEach-Object { $b[$_.Name] = $_.CPU }
  $after | ForEach-Object { [pscustomobject]@{ Name = $_.Name; CPU_s = [math]::Round($_.CPU - [double]$b[$_.Name], 1) } } |
    Sort-Object CPU_s -Descending | Select-Object -First 8 |
    Format-Table -AutoSize | Out-String | Set-Content $file
}
function Get-PeakBinPower($csv, $bin = 30) {
  $r = Import-Csv $csv
  $prev = [double]$r[0].'CPU_ENERGY (J)'; $t = 0.0; $e = 0.0; $peak = 0.0
  for ($i = 1; $i -lt $r.Count; $i++) {
    $v = [double]$r[$i].'CPU_ENERGY (J)'
    $t += [double]$r[$i].Delta / 1000
    $e += [math]::Max(0, $v - $prev); $prev = $v
    if ($t -ge $bin) { $peak = [math]::Max($peak, $e / $t); $t = 0.0; $e = 0.0 }
  }
  return $peak
}
function Get-MeanPower($csv) {
  $r = Import-Csv $csv
  $e = [double]$r[-1].'CPU_ENERGY (J)' - [double]$r[0].'CPU_ENERGY (J)'
  $t = ([double]$r[-1].Time - [double]$r[0].Time) / 1000
  if ($t -le 0) { return [double]::PositiveInfinity }
  return $e / $t
}

# Calm gate: measure the 30 s cool-down and wait until the system is back to idle
function Wait-Calm($name) {
  $tmp = Join-Path $env:TEMP "eb_cooldown.csv"
  for ($try = 1; $try -le $CalmTries; $try++) {
    & $Eb -i $Interval -o $tmp -- powershell -NoProfile -Command "Start-Sleep 30" | Out-Null
    $w = Get-MeanPower $tmp
    Add-Content "$out/cooldown.txt" ("{0} try {1}: {2:N2} W" -f $name, $try, $w)
    if ($w -le $IdleMaxWatts) { return }
    Write-Warning ("{0}: cool-down at {1:N2} W > {2} W, waiting..." -f $name, $w, $IdleMaxWatts)
  }
  throw "$name`: system not calm after $CalmTries cool-downs; check background activity (cooldown.txt)"
}

function Invoke-Measured($name, [string[]]$Command) {
  $accepted = $false
  for ($try = 1; $try -le 3 -and -not $accepted; $try++) {
    Write-Host "$name (attempt $try)"
    Wait-Calm $name
    Remove-Item "$out/$name.jtl" -ErrorAction SilentlyContinue
    $s1 = Get-CpuSnapshot
    & $Eb -i $Interval --summary -o "$out/$name.csv" -- @Command |
      Tee-Object "$out/$name.summary.txt"
    $s2 = Get-CpuSnapshot
    Write-ProcDelta $s1 $s2 "$out/$name.procs.txt"
    $hasSummary = [bool](Select-String -Path "$out/$name.summary.txt" -Pattern "Energy consumption in joules" -Quiet)
    $peak = if ($hasSummary) { Get-PeakBinPower "$out/$name.csv" } else { 0 }
    if ($hasSummary -and ($MaxBinWatts -le 0 -or $peak -le $MaxBinWatts)) {
      $accepted = $true
      Add-Content "$out/quality.txt" ("{0}: accepted on attempt {1}; peak 30 s power {2:N2} W" -f $name, $try, $peak)
    } elseif ($hasSummary -and $try -eq 3) {
      $accepted = $true
      Add-Content "$out/quality.txt" "$name accepted after 3 attempts; peak 30 s power $([math]::Round($peak,2)) W exceeds $MaxBinWatts W"
    } else {
      Write-Warning "$name rejected (summary=$hasSummary, peak 30 s power=$([math]::Round($peak,2)) W). Retrying..."
      foreach ($ext in "csv","summary.txt","procs.txt","jtl") {
        if (Test-Path "$out/$name.$ext") { Move-Item "$out/$name.$ext" "$out/rejected/$name.attempt$try.$ext" -Force }
      }
    }
  }
  if (-not $accepted) { throw "$name failed 3 times (no summary)" }
}

Write-Host "Warm-up (not measured)..."
& $Jmeter @jm -l "$out/warmup.jtl" | Out-Null

$idle = @("powershell","-NoProfile","-Command","Start-Sleep $window")
Invoke-Measured "idle_start" $idle
for ($i = 1; $i -le $Runs; $i++) {
  $n = "{0:D2}" -f $i
  Invoke-Measured "run$n" (@($Jmeter) + $jm + @("-l","$out/run$n.jtl"))
}
Invoke-Measured "idle_end" $idle
