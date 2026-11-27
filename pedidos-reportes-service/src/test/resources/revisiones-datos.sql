-- PA4: mismo esquema que crea sistema-pedidos en MySQL, con 3 pedidos (1 revisado y 2 pendientes).
DROP TABLE IF EXISTS pedido_revision;
DROP TABLE IF EXISTS pedido;

CREATE TABLE pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente VARCHAR(120) NOT NULL,
    producto_codigo VARCHAR(30) NOT NULL,
    producto_nombre VARCHAR(120) NOT NULL,
    precio_unitario DECIMAL(12, 2) NOT NULL,
    cantidad INT NOT NULL,
    total DECIMAL(12, 2) NOT NULL,
    fecha TIMESTAMP NOT NULL,
    revisado BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE TABLE pedido_revision (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    observacion VARCHAR(200) NOT NULL,
    fecha_revision TIMESTAMP NOT NULL,
    CONSTRAINT fk_pedido_revision_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id),
    CONSTRAINT uk_pedido_revision_pedido UNIQUE (pedido_id)
);

INSERT INTO pedido (id, cliente, producto_codigo, producto_nombre, precio_unitario, cantidad, total, fecha, revisado)
VALUES (1, 'Ana Torres', 'LAP-001', 'Laptop', 2500.00, 2, 5000.00, CURRENT_TIMESTAMP, TRUE),
       (2, 'Carlos Rojas', 'MON-001', 'Monitor', 850.00, 1, 850.00, CURRENT_TIMESTAMP, FALSE),
       (3, 'Luis Paz', 'TEC-001', 'Teclado', 120.00, 3, 360.00, CURRENT_TIMESTAMP, FALSE);

INSERT INTO pedido_revision (pedido_id, observacion, fecha_revision)
VALUES (1, 'Datos verificados con el cliente', TIMESTAMP '2026-11-26 10:15:00');
