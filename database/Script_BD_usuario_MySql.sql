-- PA4: base y usuarios de laboratorio en MySQL. Ejecutar una vez como root:
--   mysql -u root -p < database/Script_BD_usuario_MySql.sql
-- Las claves son de laboratorio y coinciden con los valores por defecto de cada application.properties.
-- Fuera del laboratorio se cambian y se pasan con DB_URL, DB_USERNAME y DB_PASSWORD.

CREATE DATABASE IF NOT EXISTS pedidosdb CHARACTER SET utf8mb4;

-- sistema-pedidos-springboot (8081): escribe pedidos y revisiones; Hibernate crea las tablas.
CREATE USER IF NOT EXISTS 'spedidos_app'@'localhost' IDENTIFIED BY 'userapp_2026!';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES ON pedidosdb.* TO 'spedidos_app'@'localhost';

-- pedidos-reportes-service (8083): solo lee.
CREATE USER IF NOT EXISTS 'spedidos_reportes'@'localhost' IDENTIFIED BY 'ReportesDemo2026!';
GRANT SELECT ON pedidosdb.* TO 'spedidos_reportes'@'localhost';

FLUSH PRIVILEGES;
