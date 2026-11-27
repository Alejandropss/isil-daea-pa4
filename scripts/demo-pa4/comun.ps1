# Funciones compartidas por los casos de la PA4.
# Requisitos: MySQL (3306), WildFly (8082), sistema-pedidos (8081) y pedidos-reportes-service (8083).
# MYSQL_EXE: ruta de mysql.exe si no está en el PATH.
# Las claves de laboratorio se leen de variables de entorno o de application.properties; no se muestran.

[Console]::OutputEncoding = [Text.Encoding]::UTF8
$OutputEncoding = [Text.Encoding]::UTF8

$Base = 'http://localhost:8081/sistema-pedidos'
$Reportes = 'http://localhost:8083/pedidos-reportes-service/api/v1/pedidos'
$Propiedades = Get-Content "$PSScriptRoot\..\..\sistema-pedidos-springboot\src\main\resources\application.properties" -Raw -Encoding UTF8
$MySqlExe = if ($env:MYSQL_EXE) { $env:MYSQL_EXE } else { 'mysql' }

try {
  $maximo = $Host.UI.RawUI.MaxPhysicalWindowSize
  $ancho = [Math]::Min(125, $maximo.Width)
  $Host.UI.RawUI.BufferSize = New-Object Management.Automation.Host.Size($ancho, 3000)
  $Host.UI.RawUI.WindowSize = New-Object Management.Automation.Host.Size($ancho, [Math]::Min(58, $maximo.Height - 1))
} catch { }

function ClaveLab([string]$variable, [string]$propiedad) {
  $valor = [Environment]::GetEnvironmentVariable($variable)
  if ($valor) { return $valor }
  return [regex]::Match($Propiedades, [regex]::Escape($propiedad) + '=\$\{[A-Z_]+:(.+?)\}').Groups[1].Value
}

function Titulo([string]$texto) {
  $Host.UI.RawUI.WindowTitle = $texto
  Write-Host ('=' * 100) -ForegroundColor Cyan
  Write-Host "  $texto" -ForegroundColor Cyan
  Write-Host ('=' * 100) -ForegroundColor Cyan
}

function Paso([string]$texto) {
  Write-Host ''
  Write-Host ">> $texto" -ForegroundColor Yellow
}

function Color([int]$codigo) {
  if ($codigo -ge 200 -and $codigo -lt 400) { return 'Green' }
  if ($codigo -ge 500) { return 'Magenta' }
  return 'Red'
}

# Inicia sesión con el formulario real de Spring Security y devuelve el archivo de cookies.
function Login([string]$usuario) {
  $clave = if ($usuario -eq 'operador') { ClaveLab 'PEDIDOS_OPERADOR_PASSWORD' 'demo.security.operador-password' }
           else { ClaveLab 'PEDIDOS_CONSULTA_PASSWORD' 'demo.security.consulta-password' }
  $cookies = [IO.Path]::GetTempFileName()
  $pagina = (& curl.exe -s -c $cookies -b $cookies "$Base/login") -join ''
  $token = [regex]::Match($pagina, 'name="_csrf"[^>]*value="([^"]+)"').Groups[1].Value
  $codigo = & curl.exe -s -o NUL -w '%{http_code}' -c $cookies -b $cookies -X POST "$Base/login" `
    --data-urlencode "username=$usuario" --data-urlencode "password=$clave" --data-urlencode "_csrf=$token"
  Write-Host "   Sesión iniciada como $usuario (POST /login -> HTTP $codigo)" -ForegroundColor DarkGray
  return $cookies
}

# Token CSRF de la sesión, tomado de la pantalla de supervisión.
function TokenCsrf([string]$cookies) {
  $pagina = (& curl.exe -s -c $cookies -b $cookies "$Base/supervision/pedidos") -join ''
  return [regex]::Match($pagina, 'name="_csrf"[^>]*value="([^"]+)"').Groups[1].Value
}

function MensajePantalla([string]$html) {
  $m = [regex]::Match($html, 'class="(?:alerta (?:error|aviso|exito)|error)">\s*(.*?)\s*</div>', 'Singleline')
  if ($m.Success) { return ($m.Groups[1].Value -replace '\s+', ' ').Trim() }
  return $null
}

function MostrarJson([string]$texto) {
  try { $datos = $texto | ConvertFrom-Json } catch { return }
  $lista = @($datos)
  if ($lista.Count -eq 0) { Write-Host '      [] (lista vacía)' -ForegroundColor Gray; return }
  foreach ($o in $lista) {
    $props = $o.PSObject.Properties | Where-Object { $_.Name -notin @('instance', 'type', 'productoNombre') }
    $pares = $props | ForEach-Object { if ($null -eq $_.Value) { "$($_.Name): null" } else { "$($_.Name): $($_.Value)" } }
    Write-Host ('      { ' + ($pares -join ', ') + ' }') -ForegroundColor Gray
  }
}

# GET con la sesión indicada (o sin sesión). Muestra URL, código, JSON o el mensaje de la pantalla.
function Get([string]$ruta, [string]$cookies) {
  $url = if ($ruta -like 'http*') { $ruta } else { "$Base$ruta" }
  Write-Host "   GET $($url.Replace('http://localhost:', ':'))" -ForegroundColor White
  $salida = [IO.Path]::GetTempFileName()
  $argumentos = @('-s', '-g', '-o', $salida, '-w', '%{http_code} %{redirect_url}', '--max-time', '10')
  if ($cookies) { $argumentos += @('-b', $cookies) }
  $r = & curl.exe @argumentos $url
  $codigo, $destino = "$r".Split(' ', 2)
  if ([int]$codigo -eq 0) { Write-Host '   <- Sin respuesta: conexión rechazada (servicio detenido)' -ForegroundColor Magenta; return }
  $extra = if ($destino) { " -> redirige a $($destino.Replace('http://localhost:8081', ''))" } else { '' }
  Write-Host "   <- HTTP $codigo$extra" -ForegroundColor (Color ([int]$codigo))
  $texto = [IO.File]::ReadAllText($salida, [Text.Encoding]::UTF8)
  if ($texto.TrimStart().StartsWith('[') -or $texto.TrimStart().StartsWith('{')) { MostrarJson $texto; return }
  $mensaje = MensajePantalla $texto
  if ($mensaje) { Write-Host "      Mensaje en la pantalla: $mensaje" -ForegroundColor Gray }
  elseif ($texto -match 'Supervisión de pedidos') { Write-Host '      Se muestra supervision-pedidos.jsp' -ForegroundColor Gray }
}

# POST /pedidos (flujo existente de registro, que reserva stock en inventario-service).
function RegistrarPedido([string]$cookies, [string]$token, [string]$cliente, [string]$codigo, [int]$cantidad) {
  Write-Host "   POST /pedidos: cliente=$cliente, productoCodigo=$codigo, cantidad=$cantidad" -ForegroundColor White
  $r = & curl.exe -s -o NUL -w '%{http_code} %{redirect_url}' --max-time 10 -b $cookies -X POST "$Base/pedidos" `
    --data-urlencode "cliente=$cliente" --data-urlencode "productoCodigo=$codigo" --data-urlencode "cantidad=$cantidad" `
    --data-urlencode "_csrf=$token"
  $http, $destino = "$r".Split(' ', 2)
  Write-Host "   <- HTTP $http -> $($destino.Replace('http://localhost:8081', ''))" -ForegroundColor (Color ([int]$http))
}

# POST /supervision/pedidos/{id}/revisar con observación y token CSRF.
function Revisar([string]$cookies, [string]$token, [string]$id, [string]$observacion) {
  $texto = if ($observacion.Length -gt 40) { $observacion.Substring(0, 40) + "... ($($observacion.Length) caracteres)" } else { $observacion }
  Write-Host "   POST /supervision/pedidos/$id/revisar  observacion='$texto'" -ForegroundColor White
  $salida = [IO.Path]::GetTempFileName()
  $r = & curl.exe -s -g -o $salida -w '%{http_code} %{redirect_url}' --max-time 10 -b $cookies -X POST `
    "$Base/supervision/pedidos/$id/revisar" --data-urlencode "observacion=$observacion" --data-urlencode "_csrf=$token"
  $http, $destino = "$r".Split(' ', 2)
  if ([int]$http -eq 302) {
    Write-Host "   <- HTTP 302 -> $($destino.Replace('http://localhost:8081', ''))  (Post/Redirect/Get)" -ForegroundColor Green
    return
  }
  Write-Host "   <- HTTP $http" -ForegroundColor (Color ([int]$http))
  $mensaje = MensajePantalla ([IO.File]::ReadAllText($salida, [Text.Encoding]::UTF8))
  if ($mensaje) { Write-Host "      Mensaje en la pantalla: $mensaje" -ForegroundColor Gray }
}

# Consulta directa a MySQL para comparar con lo que responde la aplicación.
function MySql([string]$sql) {
  Write-Host "   [MySQL] $sql" -ForegroundColor DarkGray
  $env:MYSQL_PWD = ClaveLab 'DB_PASSWORD' 'spring.datasource.password'
  $filas = & $MySqlExe -h 127.0.0.1 -u spedidos_app -D pedidosdb --default-character-set=utf8mb4 -t -e $sql
  Remove-Item Env:\MYSQL_PWD
  if ($filas) { $filas | ForEach-Object { Write-Host "      $_" -ForegroundColor Green } } else { Write-Host '      (sin filas)' -ForegroundColor Green }
}
