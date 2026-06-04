@echo off
powershell -NoProfile -Command "try { $ports = @(8080,5173); foreach($p in $ports){ $conns = Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue; if($conns){ $pids = $conns | Select-Object -ExpandProperty OwningProcess -Unique; foreach($pid in $pids){ Write-Output \"Stopping PID $pid on port $p\"; Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue } } else { Write-Output \"Port $p -> not listening\" } } } catch { Write-Output \"Error while stopping ports: $_\"; exit 1 }"

echo Backend and client processes stopped (if they were running).
exit /b 0
