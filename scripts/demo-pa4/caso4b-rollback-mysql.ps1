# Caso 4, parte B: el mismo fallo en MySQL real. Una restricción CHECK temporal (creada con root) hace
# que el INSERT de pedido_revision falle después del UPDATE del pedido. Al final se elimina la restricción.
param([string]$LogSpring)

. "$PSScriptRoot\comun.ps1"
Titulo 'CASO 4B - Rollback real en MySQL'

$falla = 'FORZAR FALLO EN INSERT'
$estado = 'SELECT p.id, p.revisado, (SELECT COUNT(*) FROM pedido_revision r WHERE r.pedido_id = p.id) AS revisiones FROM pedido p WHERE p.id = 2'
$operador = Login 'operador'
$token = TokenCsrf $operador

Paso 'Agregar una restricción temporal que rechaza esa observación (usuario root de laboratorio)'
$sqlCheck = "ALTER TABLE pedido_revision ADD CONSTRAINT ck_forzar_fallo CHECK (observacion <> '$falla')"
Write-Host "   [MySQL root] $sqlCheck" -ForegroundColor DarkGray
& $MySqlExe -h 127.0.0.1 -u root -D pedidosdb -e $sqlCheck

Paso 'MySQL antes'
MySql $estado

Paso 'Revisar el pedido 2: el UPDATE se ejecuta y el INSERT falla'
$inicio = if ($LogSpring -and (Test-Path $LogSpring)) { (Get-Content $LogSpring).Count } else { 0 }
Revisar $operador $token 2 $falla

if ($LogSpring -and (Test-Path $LogSpring)) {
  Paso 'Log de sistema-pedidos: lo que pasó dentro de la transacción'
  Get-Content $LogSpring -Encoding UTF8 | Select-Object -Skip $inicio |
    Select-String -CaseSensitive -Pattern 'Creating new transaction|UPDATE pedido SET revisado|INSERT INTO pedido_revision|Rolling back JPA|Check constraint' |
    Select-Object -First 6 | ForEach-Object {
      $linea = ($_.Line -replace '^.*?\s(DEBUG|INFO|ERROR)\s.*?:\s', '') -replace '\s+', ' '
      if ($linea.Length -gt 118) { $linea = $linea.Substring(0, 118) + '...' }
      Write-Host "   $linea" -ForegroundColor $(if ($linea -match 'Rolling back|Check constraint') { 'Magenta' } else { 'Gray' })
    }
}

Paso 'MySQL después: revisado sigue en 0 y no hay revisión'
MySql $estado

Paso 'Quitar la restricción temporal'
Write-Host '   [MySQL root] ALTER TABLE pedido_revision DROP CHECK ck_forzar_fallo' -ForegroundColor DarkGray
& $MySqlExe -h 127.0.0.1 -u root -D pedidosdb -e 'ALTER TABLE pedido_revision DROP CHECK ck_forzar_fallo'
