param(
    [string]$OutputFile = "tools/skill-index/SKILL_INDEX.md",
    [string]$StateFile = "tools/skill-index/.skill-index-state.json",
    [switch]$Incremental
)

$ErrorActionPreference = "Stop"

$sources = @(
    @{ Name = "Cursor Skills"; Path = "$env:USERPROFILE\.cursor\skills"; Pattern = "SKILL.md" },
    @{ Name = "Cursor Skills Cursor"; Path = "$env:USERPROFILE\.cursor\skills-cursor"; Pattern = "SKILL.md" },
    @{ Name = "Claude Skills"; Path = "$env:USERPROFILE\.claude\skills"; Pattern = "SKILL.md" },
    @{ Name = "Repo Skill Library"; Path = "E:\00 - AI\00 - Skills\SKILLS"; Pattern = "SKILL.md" }
)

function Get-SkillMeta {
    param([string]$SkillFile)

    $name = Split-Path (Split-Path $SkillFile -Parent) -Leaf
    $description = ""
    $version = ""

    $lines = Get-Content -LiteralPath $SkillFile -ErrorAction SilentlyContinue
    foreach ($line in $lines) {
        if (-not $description -and $line -match "^description:\s*""(.+)""\s*$") {
            $description = $Matches[1]
        } elseif (-not $description -and $line -match "^description:\s*(.+)\s*$") {
            $description = $Matches[1].Trim('"')
        }
        if (-not $name -and $line -match "^name:\s*(.+)\s*$") {
            $name = $Matches[1].Trim('"')
        }
        if (-not $version -and $line -match "^version:\s*(.+)\s*$") {
            $version = $Matches[1].Trim('"')
        }
    }

    return [PSCustomObject]@{
        Name = $name
        Description = $description
        Version = $version
        Path = $SkillFile
    }
}

function Get-SkillFiles {
    param(
        [array]$SourceDefs
    )

    $items = @()
    foreach ($source in $SourceDefs) {
        if (-not (Test-Path -LiteralPath $source.Path)) {
            continue
        }

        $files = Get-ChildItem -LiteralPath $source.Path -Recurse -File | Where-Object { $_.Name -ieq "SKILL.md" }
        foreach ($file in $files) {
            $items += [PSCustomObject]@{
                Source = $source.Name
                Path = $file.FullName
                Length = $file.Length
                LastWriteTimeUtc = $file.LastWriteTimeUtc.ToString("o")
            }
        }
    }

    return $items | Sort-Object Path -Unique
}

function Build-SkillEntry {
    param(
        [string]$Source,
        [string]$SkillPath,
        [long]$Length,
        [string]$LastWriteTimeUtc
    )

    $meta = Get-SkillMeta -SkillFile $SkillPath
    return [PSCustomObject]@{
        Source = $Source
        Name = $meta.Name
        Version = $meta.Version
        Description = $meta.Description
        Path = $SkillPath
        Length = $Length
        LastWriteTimeUtc = $LastWriteTimeUtc
    }
}

function Load-PreviousState {
    param([string]$Path)

    if (-not (Test-Path -LiteralPath $Path)) {
        return @()
    }

    try {
        $raw = Get-Content -LiteralPath $Path -Raw -Encoding UTF8
        if (-not $raw) {
            return @()
        }
        $state = $raw | ConvertFrom-Json
        if ($null -eq $state.items) {
            return @()
        }
        return @($state.items)
    } catch {
        Write-Warning "State file unreadable, fallback to full rebuild: $Path"
        return @()
    }
}

$cwd = Get-Location
$outPath = Join-Path $cwd $OutputFile
$outDir = Split-Path $outPath -Parent
if (-not (Test-Path -LiteralPath $outDir)) {
    New-Item -ItemType Directory -Path $outDir -Force | Out-Null
}

$statePath = Join-Path $cwd $StateFile
$stateDir = Split-Path $statePath -Parent
if (-not (Test-Path -LiteralPath $stateDir)) {
    New-Item -ItemType Directory -Path $stateDir -Force | Out-Null
}

$currentFiles = Get-SkillFiles -SourceDefs $sources
$previousItems = if ($Incremental) { Load-PreviousState -Path $statePath } else { @() }

$prevByPath = @{}
foreach ($item in $previousItems) {
    if ($null -ne $item.Path -and -not $prevByPath.ContainsKey($item.Path)) {
        $prevByPath[$item.Path] = $item
    }
}

$allItems = @()
$reused = 0
$reparsed = 0

foreach ($file in $currentFiles) {
    $prev = $null
    if ($prevByPath.ContainsKey($file.Path)) {
        $prev = $prevByPath[$file.Path]
    }

    $canReuse = $false
    if ($Incremental -and $null -ne $prev) {
        $prevLength = 0
        if ($null -ne $prev.Length) {
            $prevLength = [long]$prev.Length
        }
        $prevWrite = ""
        if ($null -ne $prev.LastWriteTimeUtc) {
            $prevWrite = [string]$prev.LastWriteTimeUtc
        }
        if ($prevLength -eq [long]$file.Length -and $prevWrite -eq [string]$file.LastWriteTimeUtc) {
            $canReuse = $true
        }
    }

    if ($canReuse) {
        $allItems += [PSCustomObject]@{
            Source = [string]$prev.Source
            Name = [string]$prev.Name
            Version = [string]$prev.Version
            Description = [string]$prev.Description
            Path = [string]$prev.Path
            Length = [long]$prev.Length
            LastWriteTimeUtc = [string]$prev.LastWriteTimeUtc
        }
        $reused += 1
    } else {
        $allItems += Build-SkillEntry -Source $file.Source -SkillPath $file.Path -Length $file.Length -LastWriteTimeUtc $file.LastWriteTimeUtc
        $reparsed += 1
    }
}

$allItems = $allItems | Sort-Object Path -Unique | Sort-Object Source, Name, Path

$statePayload = [PSCustomObject]@{
    generatedAt = (Get-Date).ToString("o")
    incremental = [bool]$Incremental
    workspace = $cwd.Path
    items = @($allItems)
}
$statePayload | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $statePath -Encoding UTF8

$now = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
$lines = New-Object System.Collections.Generic.List[string]
$lines.Add("# Local Skill Index")
$lines.Add("")
$lines.Add("- Generated: $now")
$lines.Add("- Total skills: $($allItems.Count)")
$lines.Add("- Workspace: $cwd")
$lines.Add("- Rebuild mode: $(if ($Incremental) { 'Incremental' } else { 'Full' })")
$lines.Add("- Parsed files: $reparsed")
$lines.Add("- Reused from cache: $reused")
$lines.Add("")
$lines.Add("## Quick Start")
$lines.Add("")
$lines.Add("- Rebuild: ``Ctrl+Shift+P`` -> ``Tasks: Run Task`` -> ``Skills: Rebuild Local Index``")
$lines.Add("- Open index: ``Ctrl+Shift+P`` -> ``Tasks: Run Task`` -> ``Skills: Open Local Index``")
$lines.Add("")

$grouped = $allItems | Group-Object Source
foreach ($group in $grouped) {
    $lines.Add("## $($group.Name)")
    $lines.Add("")
    foreach ($item in $group.Group) {
        $ver = if ($item.Version) { " (v$($item.Version))" } else { "" }
        $desc = if ($item.Description) { " - $($item.Description)" } else { "" }
        $lines.Add("- **$($item.Name)**$ver")
        $lines.Add("  - Path: ``$($item.Path)``")
        if ($desc) {
            $lines.Add("  - Desc:$desc")
        }
    }
    $lines.Add("")
}

$lines | Set-Content -LiteralPath $outPath -Encoding UTF8
Write-Host "Skill index rebuilt: $outPath"
Write-Host "Mode: $(if ($Incremental) { 'Incremental' } else { 'Full' }) | Parsed: $reparsed | Reused: $reused"
