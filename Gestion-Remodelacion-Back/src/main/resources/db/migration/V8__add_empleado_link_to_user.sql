ALTER TABLE users
ADD COLUMN id_empleado BIGINT NULL,
ADD CONSTRAINT fk_user_empleado FOREIGN KEY (id_empleado) REFERENCES empleados(id),
ADD CONSTRAINT uk_user_empleado UNIQUE (id_empleado);