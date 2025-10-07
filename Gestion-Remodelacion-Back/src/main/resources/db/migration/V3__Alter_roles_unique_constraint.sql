-- Paso 1: Eliminar la restricción de unicidad antigua que solo aplicaba a la columna 'name'.
-- El nombre de la restricción 'UKofx66keruapi6vyqpv6f2or37' lo obtuvimos del mensaje de error.
ALTER TABLE roles DROP CONSTRAINT UKofx66keruapi6vyqpv6f2or37;

-- Paso 2: Crear una nueva restricción de unicidad que aplique a la combinación
-- de las columnas 'name' y 'id_empresa'.
ALTER TABLE roles ADD CONSTRAINT UK_role_name_per_empresa UNIQUE (name, id_empresa);