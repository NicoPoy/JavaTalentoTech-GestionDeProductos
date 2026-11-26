-- Migración de la API simple de artículos a las funciones de tienda.
-- No elimina ni modifica la tabla articulo requerida por el PDF.
-- Ejecutar una vez (se puede volver a ejecutar sin duplicar categorías/productos migrados).

CREATE DATABASE IF NOT EXISTS articulos_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE articulos_db;

CREATE TABLE IF NOT EXISTS categoria (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_categoria_nombre (nombre)
) ENGINE=InnoDB;

INSERT INTO categoria (nombre)
SELECT 'General'
WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE LOWER(nombre) = 'general');

CREATE TABLE IF NOT EXISTS producto (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(1500) NOT NULL DEFAULT '',
    precio DECIMAL(12,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    categoria_id BIGINT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    KEY ix_producto_categoria (categoria_id),
    KEY ix_producto_stock (activo, stock),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id)
        REFERENCES categoria (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_producto_precio CHECK (precio > 0),
    CONSTRAINT ck_producto_stock CHECK (stock >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS usuario (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    email VARCHAR(190) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuario_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pedido (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    estado VARCHAR(20) NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (id),
    KEY ix_pedido_usuario_fecha (usuario_id, fecha_creacion),
    CONSTRAINT fk_pedido_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_pedido_total CHECK (total >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS linea_pedido (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pedido_id BIGINT NOT NULL,
    producto_id BIGINT NULL,
    nombre_producto VARCHAR(255) NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (id),
    KEY ix_linea_pedido (pedido_id),
    KEY ix_linea_producto (producto_id),
    CONSTRAINT fk_linea_pedido FOREIGN KEY (pedido_id)
        REFERENCES pedido (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_linea_producto FOREIGN KEY (producto_id)
        REFERENCES producto (id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT ck_linea_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_linea_precio CHECK (precio_unitario > 0),
    CONSTRAINT ck_linea_subtotal CHECK (subtotal >= 0)
) ENGINE=InnoDB;

-- Copia los artículos actuales al catálogo de tienda. Se asigna stock cero
-- para que el operador cargue la existencia real antes de confirmar pedidos.
INSERT INTO producto (id, nombre, descripcion, precio, stock, categoria_id, activo)
SELECT a.id, a.nombre, '', CAST(a.precio AS DECIMAL(12,2)), 0, c.id, TRUE
FROM articulo a
JOIN categoria c ON LOWER(c.nombre) = 'general'
WHERE NOT EXISTS (SELECT 1 FROM producto p WHERE p.id = a.id);

-- Revisar tablas y productos migrados.
SHOW TABLES;
SELECT * FROM categoria;
SELECT * FROM producto ORDER BY id;
