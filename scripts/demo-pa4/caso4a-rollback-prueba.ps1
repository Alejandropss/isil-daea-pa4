# Caso 4, parte A: la prueba de integración provoca un fallo real en el INSERT (restricción CHECK en H2)
# después del UPDATE y comprueba que el UPDATE se revierte.
. "$PSScriptRoot\comun.ps1"
Titulo 'CASO 4A - Rollback real en la prueba de integración'

Set-Location "$PSScriptRoot\..\..\sistema-pedidos-springboot"
Paso 'mvn test -Dtest=PedidoRevisionServiceIntegrationTest (con el log de SQL y de transacciones visible)'
$opciones = '-Dlogging.level.org.springframework.jdbc.core=DEBUG -Dlogging.level.org.springframework.orm.jpa.JpaTransactionManager=DEBUG'
mvn -B test "-Dtest=PedidoRevisionServiceIntegrationTest#rollback*" "-DargLine=$opciones" 2>&1 |
  Select-String -CaseSensitive -Pattern 'Creating new transaction with name \[com|UPDATE pedido SET revisado|INSERT INTO pedido_revision|Rolling back JPA|Check constraint|Tests run:|BUILD' |
  ForEach-Object {
    $linea = ($_.Line -replace '^.*?\s(DEBUG|INFO|ERROR)\s.*?:\s', '') -replace '\s+', ' '
    if ($linea.Length -gt 118) { $linea = $linea.Substring(0, 118) + '...' }
    $color = if ($linea -match 'Rolling back|Check constraint') { 'Magenta' } elseif ($linea -match 'Tests run|BUILD') { 'Green' } else { 'Gray' }
    Write-Host "   $linea" -ForegroundColor $color
  }
