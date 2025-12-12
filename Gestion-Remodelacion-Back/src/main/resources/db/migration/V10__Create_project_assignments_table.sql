CREATE TABLE proyecto_asignaciones (
    id_proyecto BIGINT NOT NULL,
    id_empleado BIGINT NOT NULL,
    
    PRIMARY KEY (id_proyecto, id_empleado),
    
    CONSTRAINT fk_asignacion_proyecto 
        FOREIGN KEY (id_proyecto) 
        REFERENCES proyectos (id) 
        ON DELETE CASCADE, 
        
    CONSTRAINT fk_asignacion_empleado 
        FOREIGN KEY (id_empleado) 
        REFERENCES empleados (id) 
        ON DELETE CASCADE 
);