package com.gestionremodelacion.gestion.horastrabajadas.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.gestionremodelacion.gestion.dto.response.ApiResponse;
import com.gestionremodelacion.gestion.empleado.model.Empleado;
import com.gestionremodelacion.gestion.empleado.repository.EmpleadoRepository;
import com.gestionremodelacion.gestion.horastrabajadas.dto.request.HorasTrabajadasRequest;
import com.gestionremodelacion.gestion.horastrabajadas.dto.response.HorasTrabajadasExportDTO;
import com.gestionremodelacion.gestion.horastrabajadas.dto.response.HorasTrabajadasResponse;
import com.gestionremodelacion.gestion.horastrabajadas.model.HorasTrabajadas;
import com.gestionremodelacion.gestion.horastrabajadas.repository.HorasTrabajadasRepository;
import com.gestionremodelacion.gestion.mapper.HorasTrabajadasMapper;
import com.gestionremodelacion.gestion.proyecto.model.Proyecto;
import com.gestionremodelacion.gestion.proyecto.repository.ProyectoRepository;

@Service
public class HorasTrabajadasService {

    private final HorasTrabajadasRepository horasTrabajadasRepository;
    private final HorasTrabajadasMapper horasTrabajadasMapper;
    private final EmpleadoRepository empleadoRepository;
    private final ProyectoRepository proyectoRepository;

    public HorasTrabajadasService(HorasTrabajadasRepository horasTrabajadasRepository,
            HorasTrabajadasMapper horasTrabajadasMapper, EmpleadoRepository empleadoRepository, ProyectoRepository proyectoRepository) {
        this.horasTrabajadasRepository = horasTrabajadasRepository;
        this.horasTrabajadasMapper = horasTrabajadasMapper;
        this.empleadoRepository = empleadoRepository;
        this.proyectoRepository = proyectoRepository;
    }

    @Transactional(readOnly = true)
    public Page<HorasTrabajadasResponse> getAllHorasTrabajadas(Pageable pageable, String filter) {
        if (filter != null && !filter.trim().isEmpty()) {
            return horasTrabajadasRepository.findByFilterWithDetails(filter, pageable);
        } else {
            return horasTrabajadasRepository.findAllWithDetails(pageable);
        }
    }

    @Transactional(readOnly = true)
    public HorasTrabajadasResponse getHorasTrabajadasById(Long id) {
        HorasTrabajadas horasTrabajadas = horasTrabajadasRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de horas trabajadas no encontrado con ID: " + id));
        return horasTrabajadasMapper.toHorasTrabajadasResponse(horasTrabajadas);
    }

    @Transactional
    public HorasTrabajadasResponse createHorasTrabajadas(HorasTrabajadasRequest horasTrabajadasRequest) {
        // 1. Obtener el empleado y su costo actual
        Empleado empleado = empleadoRepository.findById(horasTrabajadasRequest.getIdEmpleado())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado"));
        // 2. Crear la entidad a partir del DTO
        HorasTrabajadas horasTrabajadas = horasTrabajadasMapper.toHorasTrabajadas(horasTrabajadasRequest);
        // 3. Establecer el costo por hora actual del empleado
        horasTrabajadas.setCostoPorHoraActual(empleado.getCostoPorHora());
        // 4. Calcular el monto total de las horas trabajadas
        BigDecimal montoTotal = horasTrabajadas.getHoras().multiply(horasTrabajadas.getCostoPorHoraActual());
        // 5. Guardar la entidad de HorasTrabajadas
        HorasTrabajadas savedHorasTrabajadas = horasTrabajadasRepository.save(horasTrabajadas);
        // 6. Obtener el proyecto asociado para actualizarlo
        Proyecto proyecto = proyectoRepository.findById(horasTrabajadas.getProyecto().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado"));
        // 7. Actualizar el costo consolidado del proyecto
        BigDecimal otrosGastosActuales = proyecto.getOtrosGastosDirectosConsolidado();
        BigDecimal nuevosGastos = otrosGastosActuales.add(montoTotal);
        proyecto.setOtrosGastosDirectosConsolidado(nuevosGastos);
        // 8. Guardar la actualización del proyecto
        proyectoRepository.save(proyecto);    

        return horasTrabajadasMapper.toHorasTrabajadasResponse(savedHorasTrabajadas);
    }

    @Transactional
    public HorasTrabajadasResponse updateHorasTrabajadas(Long id, HorasTrabajadasRequest horasTrabajadasRequest) {
        // 1. Obtener el registro de horas original antes de la actualización
        HorasTrabajadas horasTrabajadasOriginal = horasTrabajadasRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de horas trabajadas no encontrado con ID: " + id));

        // 2. Obtener los valores antiguos para el cálculo
        BigDecimal horasAntiguas = horasTrabajadasOriginal.getHoras();
        BigDecimal costoAntiguo = horasTrabajadasOriginal.getCostoPorHoraActual();
        BigDecimal costoTotalAntiguo = horasAntiguas.multiply(costoAntiguo);

        // 3. Aplicar las actualizaciones del request al objeto original
        horasTrabajadasMapper.updateHorasTrabajadasFromRequest(horasTrabajadasRequest, horasTrabajadasOriginal);

        // 4. Actualizar el costo por hora actual del registro si es necesario
        if (horasTrabajadasRequest.getCostoPorHora() != null) {
            horasTrabajadasOriginal.setCostoPorHoraActual(horasTrabajadasRequest.getCostoPorHora());
        } else {
            // Si el costo no viene en el request, asumimos que se mantiene el del empleado
            Empleado empleado = empleadoRepository.findById(horasTrabajadasOriginal.getEmpleado().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado"));
            horasTrabajadasOriginal.setCostoPorHoraActual(empleado.getCostoPorHora());
        }

        // 5. Calcular el nuevo costo total
        BigDecimal horasNuevas = horasTrabajadasOriginal.getHoras();
        BigDecimal costoNuevo = horasTrabajadasOriginal.getCostoPorHoraActual();
        BigDecimal costoTotalNuevo = horasNuevas.multiply(costoNuevo);

        // 6. Calcular la diferencia para ajustar el proyecto
        BigDecimal diferenciaCosto = costoTotalNuevo.subtract(costoTotalAntiguo);

        // 7. Guardar el registro de HorasTrabajadas actualizado
        horasTrabajadasRepository.save(horasTrabajadasOriginal);

        // 8. Ajustar el costo consolidado del proyecto
        Proyecto proyecto = proyectoRepository.findById(horasTrabajadasOriginal.getProyecto().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado"));
        
        BigDecimal nuevosGastos = proyecto.getOtrosGastosDirectosConsolidado().add(diferenciaCosto);
        proyecto.setOtrosGastosDirectosConsolidado(nuevosGastos);
        proyectoRepository.save(proyecto);
        return horasTrabajadasMapper.toHorasTrabajadasResponse(horasTrabajadasOriginal);
    }

    @Transactional
    public ApiResponse<Void> deleteHorasTrabajadas(Long id) {
        if (!horasTrabajadasRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de horas trabajadas no encontrado con ID: " + id);
        }
        horasTrabajadasRepository.deleteById(id);
        return new ApiResponse<>(HttpStatus.OK.value(), "Registro de horas trabajadas eliminado exitosamente.", null);
    }

    @Transactional(readOnly = true)
    public List<HorasTrabajadasExportDTO> findHorasTrabajadasForExport(String filter, String sort) {
        Sort sortObj = Sort.by(Sort.Direction.DESC, "fecha"); // Ordenar por fecha por defecto
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                String property = sortParts[0];
                Sort.Direction direction = "desc".equalsIgnoreCase(sortParts[1]) ? Sort.Direction.DESC : Sort.Direction.ASC;
                sortObj = Sort.by(direction, property);
            }
        }

        List<HorasTrabajadas> horas;
        if (filter != null && !filter.trim().isEmpty()) {
            horas = horasTrabajadasRepository.findByFilterForExport(filter, sortObj);
        } else {
            horas = horasTrabajadasRepository.findAll(sortObj);
        }

        return horas.stream()
                .map(HorasTrabajadasExportDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * ✅ MÉTODO DE MIGRACIÓN 1: Corrige los registros históricos.
     * Itera sobre todos los registros de horas trabajadas con 'costoPorHoraActual' nulo,
     * asigna el costo por hora actual del empleado y guarda el registro.
     * Nota: Este método solo debe ejecutarse una vez en el despliegue.
     */
    @Transactional
    public void corregirCostosHorasHistoricas() {
        System.out.println("Iniciando corrección de costos por hora en registros históricos...");

        // Usamos una consulta personalizada para obtener solo los registros que necesitan ser corregidos
        List<HorasTrabajadas> registrosSinCosto = horasTrabajadasRepository.findByCostoPorHoraActualIsNull();

        for (HorasTrabajadas registro : registrosSinCosto) {
            // Se asume que la entidad Empleado ya está cargada debido a las anotaciones de relación
            Empleado empleado = registro.getEmpleado();
            if (empleado != null && empleado.getCostoPorHora() != null) {
                registro.setCostoPorHoraActual(empleado.getCostoPorHora());
                horasTrabajadasRepository.save(registro);
            }
        }
        System.out.println("Corrección de costos por hora completada. Registros actualizados: " + registrosSinCosto.size());
    }    
    
    /**
     * ✅ MÉTODO DE MIGRACIÓN 2: Recalcula los gastos consolidados de los proyectos.
     * Este método se ejecuta DESPUÉS de corregir los costos de horas.
     * Recalcula el campo 'otrosGastosDirectosConsolidado' en cada proyecto
     * sumando todos los costos de horas trabajadas asociados.
     * Nota: Este método solo debe ejecutarse una vez en el despliegue.
     */
    @Transactional
    public void recalcularGastosConsolidadosProyectos() {
        System.out.println("Iniciando recálculo de gastos consolidados en proyectos...");

        // 1. Obtener todos los proyectos
        List<Proyecto> todosLosProyectos = proyectoRepository.findAll();

        // 2. Reiniciar el campo de gastos en cada proyecto para evitar duplicados
        for (Proyecto proyecto : todosLosProyectos) {
            proyecto.setOtrosGastosDirectosConsolidado(BigDecimal.ZERO);
            proyectoRepository.save(proyecto);
        }

        // 3. Obtener todos los registros de horas trabajadas (ya corregidos)
        List<HorasTrabajadas> todasLasHoras = horasTrabajadasRepository.findAll();

        // 4. Sumar los costos y actualizar cada proyecto
        for (HorasTrabajadas registro : todasLasHoras) {
            if (registro.getProyecto() != null && registro.getCostoPorHoraActual() != null) {
                Proyecto proyecto = registro.getProyecto();
                BigDecimal costoTotal = registro.getHoras().multiply(registro.getCostoPorHoraActual());
                proyecto.setOtrosGastosDirectosConsolidado(proyecto.getOtrosGastosDirectosConsolidado().add(costoTotal));
                proyectoRepository.save(proyecto);
            }
        }
        System.out.println("Recálculo de gastos consolidados completado.");
    }    



}
