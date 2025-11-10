UPDATE horas_trabajadas ht
JOIN empleados e ON ht.id_empleado = e.id
JOIN proyectos p ON ht.id_proyecto = p.id
SET
    ht.nombre_empleado = e.nombre_completo,
    ht.nombre_proyecto = p.nombre_proyecto,
    ht.unidad = IF(e.modelo_pago = 'POR_DIA', 'dias', 'horas'),
    ht.cantidad = IF(e.modelo_pago = 'POR_DIA', ht.horas / 8, ht.horas)
WHERE
    ht.nombre_empleado IS NULL; 

