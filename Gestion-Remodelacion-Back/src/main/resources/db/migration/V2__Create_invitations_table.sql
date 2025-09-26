-- V2: Añade el sistema de invitaciones y el campo de email requerido en la tabla de usuarios

-- Paso 1: Crear la tabla de invitaciones
CREATE TABLE invitaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    empresa_id BIGINT NOT NULL,
    fecha_expiracion DATETIME NOT NULL,
    utilizada BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_invitacion_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE
);

-- Paso 2: Modificar la tabla de usuarios para añadir el email
ALTER TABLE users
ADD COLUMN email VARCHAR(255);

-- Paso 3: Hacer la columna email no nula y única
-- Si tienes usuarios existentes, primero debes rellenar sus correos
UPDATE users SET email = CONCAT(username, '@example.com') WHERE email IS NULL;
ALTER TABLE users MODIFY COLUMN email VARCHAR(255) NOT NULL;
ALTER TABLE users ADD CONSTRAINT uk_user_email UNIQUE (email);