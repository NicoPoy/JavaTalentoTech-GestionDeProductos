CREATE DATABASE IF NOT EXISTS articulos_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE articulos_db;

CREATE TABLE IF NOT EXISTS articulo (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    precio DOUBLE NOT NULL,
    PRIMARY KEY (id)
);
