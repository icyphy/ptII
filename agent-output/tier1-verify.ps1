$ErrorActionPreference = 'Stop'
$base = 'http://localhost:7777/api/v1'

# Create a fresh session with a tiny SDF model that has Ramp + AddSubtract + Recorder.
$session = (Invoke-RestMethod -Method Post -Uri "$base/sessions").id
$moml = @'
<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="Tier1Test" class="ptolemy.actor.TypedCompositeActor">
  <property name="SDF Director" class="ptolemy.domains.sdf.kernel.SDFDirector">
    <property name="iterations" class="ptolemy.data.expr.Parameter" value="5"/>
  </property>
  <entity name="Src"      class="ptolemy.actor.lib.Ramp"/>
  <entity name="Sum"      class="ptolemy.actor.lib.AddSubtract"/>
  <entity name="Recorder" class="ptolemy.actor.lib.Recorder"/>
</entity>
'@
Invoke-RestMethod -Method Post -Uri "$base/sessions/$session/load" `
    -ContentType 'application/json' `
    -Body (@{ moml = $moml } | ConvertTo-Json -Compress) | Out-Null

function Probe($label, $body) {
    Write-Host "==== $label ===="
    $payload = @{
        from = $body.from
        to   = $body.to
    }
    if ($body.parent) { $payload.parent = $body.parent }
    $result = Invoke-RestMethod -Method Post -Uri "$base/sessions/$session/tools/connect" `
        -ContentType 'application/json' `
        -Body ($payload | ConvertTo-Json -Compress)
    $result | ConvertTo-Json -Depth 10
    Write-Host ''
}

Probe 'A. AddSubtract.input does not exist (expect didYouMean=plus/minus, availablePorts)' @{ from = 'Src.output'; to = 'Sum.input' }
Probe 'B. wrong entity prefix (expect SOURCE_ENTITY_NOT_FOUND + suggest Src)' @{ from = 'Ramp.output'; to = 'Sum.plus' }
Probe 'C. direction reversed (expect REVERSED_DIRECTION)' @{ from = 'Recorder.input'; to = 'Src.output' }
Probe 'D. typo in port name (expect didYouMean=output)' @{ from = 'Src.outpt'; to = 'Sum.plus' }
Probe 'E. good connect (expect ok=true)' @{ from = 'Src.output'; to = 'Sum.plus' }
