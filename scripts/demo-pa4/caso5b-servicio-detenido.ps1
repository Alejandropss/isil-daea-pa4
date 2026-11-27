. "$PSScriptRoot\comun.ps1"
Titulo 'CASO 5B - pedidos-reportes-service detenido'

$operador = Login 'operador'
$token = TokenCsrf $operador

Paso 'Detener pedidos-reportes-service (se finaliza el proceso del puerto 8083)'
$conexion = Get-NetTCPConnection -LocalPort 8083 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
if ($conexion) {
  Stop-Process -Id $conexion.OwningProcess -Force
  Start-Sleep -Seconds 3
  Write-Host "   pedidos-reportes-service detenido (PID $($conexion.OwningProcess))." -ForegroundColor Magenta
}
Get "$Reportes/estadisticas/revisiones"

Paso 'sistema-pedidos maneja la caída sin mostrar trazas técnicas'
Get '/supervision/pedidos' $operador

Paso 'La revisión es local: funciona con el 8083 apagado y la base queda consistente'
$estado = 'SELECT p.id, p.revisado, r.observacion FROM pedido p LEFT JOIN pedido_revision r ON r.pedido_id = p.id ORDER BY p.id'
MySql $estado
Revisar $operador $token 3 'Revisado con reportes apagado'
MySql $estado
