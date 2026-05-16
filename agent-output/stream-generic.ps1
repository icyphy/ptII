param([string]$Sid,[string]$Out)
$url = "http://localhost:7777/api/v1/sessions/$Sid/agent/chat/stream"
$body = Get-Content C:\projects\ptII\agent-output\pipe-test-req.json -Raw
try {
  $req = [System.Net.HttpWebRequest]::Create($url)
  $req.Method='POST'; $req.ContentType='application/json'
  $req.Timeout=1800000; $req.ReadWriteTimeout=1800000
  $bytes=[Text.Encoding]::UTF8.GetBytes($body); $req.ContentLength=$bytes.Length
  $rs=$req.GetRequestStream(); $rs.Write($bytes,0,$bytes.Length); $rs.Close()
  $resp=$req.GetResponse(); $sr=New-Object IO.StreamReader($resp.GetResponseStream())
  while ($null -ne ($line=$sr.ReadLine())) { $w=New-Object IO.StreamWriter($Out,$true,[Text.Encoding]::UTF8); $w.WriteLine($line); $w.Close() }
  $sr.Close(); $resp.Close()
  "DONE" | Out-File "$Out.done"
} catch { "ERR: $($_.Exception.Message)" | Out-File "$Out.done" }
