. "$PSScriptRoot\comun.ps1"
Titulo 'CASO 1 - Revisión correcta'

Paso 'Registrar pedidos con el flujo existente (reservan stock en inventario-service)'
$operador = Login 'operador'
$token = TokenCsrf $operador
RegistrarPedido $operador $token 'Ana Torres' 'LAP-001' 2
RegistrarPedido $operador $token 'Carlos Rojas' 'MON-001' 1
RegistrarPedido $operador $token 'Luis Paz' 'TEC-001' 3

Paso 'MySQL antes de revisar'
MySql 'SELECT id, cliente, total, revisado FROM pedido ORDER BY id'
MySql 'SELECT id, pedido_id, observacion, fecha_revision FROM pedido_revision'

Paso 'Revisar el pedido 1 con una observación válida'
Revisar $operador $token 1 'Datos verificados con el cliente'

Paso 'MySQL después: revisado = 1 y una sola fila en pedido_revision'
MySql 'SELECT id, cliente, total, revisado FROM pedido ORDER BY id'
MySql 'SELECT id, pedido_id, observacion, fecha_revision FROM pedido_revision'
