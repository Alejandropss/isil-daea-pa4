. "$PSScriptRoot\comun.ps1"
Titulo 'CASO 5A - pedidos-reportes-service disponible'

Paso 'Recurso 1: pedidos por estado de revisión (Spring JDBC con parámetro)'
Get "$Reportes/revisiones?estado=REVISADO"
Get "$Reportes/revisiones?estado=PENDIENTE"

Paso 'Recurso 2: resumen estadístico calculado en el Service'
Get "$Reportes/estadisticas/revisiones"

Paso 'Parámetro inválido'
Get "$Reportes/revisiones?estado=ANULADO"

Paso 'La pantalla de supervisión consume ambos recursos con el cliente HTTP'
$operador = Login 'operador'
Get '/supervision/pedidos' $operador
