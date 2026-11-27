. "$PSScriptRoot\comun.ps1"
Titulo 'CASO 3 - Validaciones e ID inexistente'

$operador = Login 'operador'
$token = TokenCsrf $operador
$estado = 'SELECT p.id, p.revisado, (SELECT COUNT(*) FROM pedido_revision r WHERE r.pedido_id = p.id) AS revisiones FROM pedido p ORDER BY p.id'

Paso 'MySQL antes'
MySql $estado

Paso 'pedidoId inválido: 0 y texto'
Revisar $operador $token 0 'Observación válida'
Revisar $operador $token 'abc' 'Observación válida'

Paso 'Observación vacía y observación de más de 200 caracteres'
Revisar $operador $token 2 '   '
Revisar $operador $token 2 ('x' * 201)

Paso 'ID no registrado'
Revisar $operador $token 999 'Observación válida'

Paso 'MySQL después: ninguna escritura parcial'
MySql $estado
