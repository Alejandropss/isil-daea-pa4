. "$PSScriptRoot\comun.ps1"
Titulo 'CASO 5C - pedidos-reportes-service restaurado'

Paso 'Con el 8083 encendido otra vez, el resumen incluye la revisión hecha durante la caída'
Get "$Reportes/estadisticas/revisiones"
Get "$Reportes/revisiones?estado=REVISADO"
MySql 'SELECT COUNT(*) AS total, SUM(revisado) AS revisados FROM pedido'
