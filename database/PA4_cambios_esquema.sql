-- PA4: cambios de esquema. Hibernate los aplica solo al arrancar sistema-pedidos-springboot
-- (ddl-auto=update). Este script es el equivalente manual, por si se prefiere crearlos a mano.
USE pedidosdb;

-- 1) Nuevo atributo: todo pedido nace pendiente de revisión.
ALTER TABLE pedido ADD COLUMN revisado BOOLEAN NOT NULL DEFAULT FALSE;

-- 2) Evidencia de la revisión.
CREATE TABLE pedido_revision (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    observacion VARCHAR(200) NOT NULL,
    fecha_revision DATETIME(6) NOT NULL,
    -- Integridad referencial: no puede existir una revisión de un pedido que no existe.
    CONSTRAINT fk_pedido_revision_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id),
    -- Una sola revisión confirmada por pedido.
    CONSTRAINT uk_pedido_revision_pedido UNIQUE (pedido_id)
);
