ALTER TABLE invitaciones
ADD COLUMN id_empleado BIGINT NULL;


ALTER TABLE invitaciones
ADD CONSTRAINT fk_invitaciones_empleado
FOREIGN KEY (id_empleado) REFERENCES empleados(id);