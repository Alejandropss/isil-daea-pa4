. "$PSScriptRoot\comun.ps1"
Titulo 'CASO 2 - Revisión duplicada'

$operador = Login 'operador'
$token = TokenCsrf $operador

Paso 'MySQL antes del segundo intento'
MySql 'SELECT id, pedido_id, observacion, fecha_revision FROM pedido_revision'

Paso 'Revisar otra vez el pedido 1'
Revisar $operador $token 1 'Segunda revisión del mismo pedido'

Paso 'MySQL después: sigue una sola revisión, sin cambios'
MySql 'SELECT id, pedido_id, observacion, fecha_revision FROM pedido_revision'
MySql 'SELECT id, revisado FROM pedido WHERE id = 1'
