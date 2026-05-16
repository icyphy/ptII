$ErrorActionPreference = 'Stop'
$base = 'http://localhost:7777/api/v1'

# Load a model that intentionally includes an actor whose port set is
# tricky (Expression).  Then trigger 2 bad connects against it and
# check that ModelContext surfaces classFailures.
$session = (Invoke-RestMethod -Method Post -Uri "$base/sessions").id
$moml = @'
<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="ClsFailTest" class="ptolemy.actor.TypedCompositeActor">
  <property name="SDF Director" class="ptolemy.domains.sdf.kernel.SDFDirector"/>
  <entity name="Src"   class="ptolemy.actor.lib.Ramp"/>
  <entity name="Exp1"  class="ptolemy.actor.lib.Expression">
    <property name="expression" class="ptolemy.data.expr.Parameter" value="1.0"/>
  </entity>
  <entity name="Rec"   class="ptolemy.actor.lib.Recorder"/>
</entity>
'@
Invoke-RestMethod -Method Post -Uri "$base/sessions/$session/load" `
    -ContentType 'application/json' `
    -Body (@{ moml = $moml } | ConvertTo-Json -Compress) | Out-Null

function Probe($payload) {
    Invoke-RestMethod -Method Post `
        -Uri "$base/sessions/$session/tools/connect" `
        -ContentType 'application/json' `
        -Body ($payload | ConvertTo-Json -Compress)
}

Write-Host '==== 1st failing connect on Expression (port "input" does not exist) ===='
$r1 = Probe @{ from = 'Src.output'; to = 'Exp1.input' }
Write-Host "  ok=$($r1.ok) message=$($r1.message)"

Write-Host '==== 2nd failing connect on Expression (port "x") ===='
$r2 = Probe @{ from = 'Src.output'; to = 'Exp1.x' }
Write-Host "  ok=$($r2.ok) message=$($r2.message)"

# Read the model graph + context block via the dryRun mechanism on a
# no-op call (no native /context endpoint).  Easiest: call validate
# which echoes data, then inspect /graph (no classFailures there).
# Simplest: trigger any tool with _dryRun=true on the connect_many
# path so we see modelContext in the response.
Write-Host '==== Inspect ModelContext via dry-run validate ===='
$ctxResult = Invoke-RestMethod -Method Post `
    -Uri "$base/sessions/$session/tools/add_entity" `
    -ContentType 'application/json' `
    -Body (@{ _dryRun = $true; name = 'Probe'; className = 'ptolemy.actor.lib.Const' } | ConvertTo-Json -Compress)
$ctx = $ctxResult.data.modelContext
Write-Host "classFailures present? $($ctx.classFailures -ne $null)"
if ($ctx.classFailures) {
    $ctx.classFailures | ConvertTo-Json -Depth 5
}
