$ErrorActionPreference = 'Stop'
$base = 'http://localhost:7777/api/v1'

# Build a small SDF model with Ramp, Const, AddSubtract, Scale and Recorder.
$session = (Invoke-RestMethod -Method Post -Uri "$base/sessions").id
$moml = @'
<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="Tier2Test" class="ptolemy.actor.TypedCompositeActor">
  <property name="SDF Director" class="ptolemy.domains.sdf.kernel.SDFDirector">
    <property name="iterations" class="ptolemy.data.expr.Parameter" value="5"/>
  </property>
  <entity name="Src"      class="ptolemy.actor.lib.Ramp"/>
  <entity name="Bias"     class="ptolemy.actor.lib.Const"/>
  <entity name="Sum"      class="ptolemy.actor.lib.AddSubtract"/>
  <entity name="Gain"     class="ptolemy.actor.lib.Scale"/>
  <entity name="Recorder" class="ptolemy.actor.lib.Recorder"/>
</entity>
'@
Invoke-RestMethod -Method Post -Uri "$base/sessions/$session/load" `
    -ContentType 'application/json' `
    -Body (@{ moml = $moml } | ConvertTo-Json -Compress) | Out-Null

function Call($body) {
    Invoke-RestMethod -Method Post `
        -Uri "$base/sessions/$session/tools/connect_many" `
        -ContentType 'application/json' `
        -Body ($body | ConvertTo-Json -Depth 5 -Compress)
}

Write-Host '==== A. ALL invalid: 3 wrong edges in one batch (expect rejected length 3, applied 0) ===='
$a = Call @{
    edges = @(
        @{ from = 'Src.outpt';   to = 'Sum.plus' },
        @{ from = 'Bias.output'; to = 'Sum.input' },
        @{ from = 'Gain.output'; to = 'Recorder.intput' }
    )
}
$a | ConvertTo-Json -Depth 10

Write-Host ''
Write-Host '==== B. MIXED: 1 bad + 3 good — whole batch must be rejected, no state change ===='
$b = Call @{
    edges = @(
        @{ from = 'Src.output';  to = 'Sum.plus' },
        @{ from = 'Bias.output'; to = 'Sum.minus' },
        @{ from = 'Sum.output';  to = 'Gain.input' },
        @{ from = 'Gain.output'; to = 'Recorder.foobar' }
    )
}
$b | ConvertTo-Json -Depth 10

# Confirm rollback: query graph
$graph = Invoke-RestMethod -Uri "$base/sessions/$session/graph"
Write-Host ''
Write-Host '   edges after rejected batch (should be empty):'
$graph.edges | ConvertTo-Json -Depth 4

Write-Host ''
Write-Host '==== C. ALL good 4-edge fan-out batch — atomic apply ===='
$c = Call @{
    edges = @(
        @{ from = 'Src.output';  to = 'Sum.plus' },
        @{ from = 'Src.output';  to = 'Gain.input' },
        @{ from = 'Bias.output'; to = 'Sum.minus' },
        @{ from = 'Gain.output'; to = 'Recorder.input' }
    )
}
$c | ConvertTo-Json -Depth 10

# After C, the graph should have 4 edges, Src.output should fan-out.
$graph = Invoke-RestMethod -Uri "$base/sessions/$session/graph"
Write-Host ''
Write-Host '   edges after good batch:'
$graph.edges | ConvertTo-Json -Depth 4

Write-Host ''
Write-Host '==== D. Validate + run, prove the wired model actually simulates ===='
$validate = Invoke-RestMethod -Method Post -Uri "$base/sessions/$session/tools/validate" -ContentType 'application/json' -Body '{}'
Write-Host "validate.ok=$($validate.ok) issues=$($validate.data.issues.Count)"
$run = Invoke-RestMethod -Method Post -Uri "$base/sessions/$session/run" -ContentType 'application/json' -Body '{}'
Write-Host "run.ok=$($run.ok) message=$($run.message)"
