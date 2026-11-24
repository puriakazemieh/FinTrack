[CmdletBinding()]
param(
    [Parameter(Position = 0, ValueFromRemainingArguments = $true)]
    [string[]] $GitNexusArguments = @('status')
)

$ErrorActionPreference = 'Stop'
$gitExecutable = (Get-Command git.exe -ErrorAction Stop).Source
$gitExecutableDirectory = Split-Path -Parent $gitExecutable
$gitInstallDirectory = Split-Path -Parent $gitExecutableDirectory
$gitDirectoryCandidates = @(
    $gitInstallDirectory,
    $gitExecutableDirectory,
    (Split-Path -Parent $gitInstallDirectory)
) | Select-Object -Unique

$opensslDirectory = $null
foreach ($candidateRoot in $gitDirectoryCandidates) {
    foreach ($relativeDirectory in @('ucrt64/bin', 'mingw64/bin', 'bin')) {
        $candidateDirectory = Join-Path $candidateRoot $relativeDirectory
        if ((Test-Path -LiteralPath (Join-Path $candidateDirectory 'libcrypto-3-x64.dll')) -and
            (Test-Path -LiteralPath (Join-Path $candidateDirectory 'libssl-3-x64.dll'))) {
            $opensslDirectory = $candidateDirectory
            break
        }
    }
    if ($null -ne $opensslDirectory) { break }
}

$originalPath = $env:PATH
try {
    if ($null -ne $opensslDirectory) {
        # Limit DLL resolution changes to this invocation and its child process.
        $env:PATH = $opensslDirectory + [IO.Path]::PathSeparator + $originalPath
    }
    $gitNexusExecutable = (Get-Command gitnexus.cmd -ErrorAction Stop).Source
    & $gitNexusExecutable @GitNexusArguments
    $gitNexusExitCode = $LASTEXITCODE
}
finally {
    $env:PATH = $originalPath
}
exit $gitNexusExitCode
