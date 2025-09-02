package com.gestionremodelacion.gestion.horastrabajadas.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionremodelacion.gestion.empleado.model.Empleado;
import com.gestionremodelacion.gestion.empleado.repository.EmpleadoRepository;
import com.gestionremodelacion.gestion.exception.ResourceNotFoundException;
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
            HorasTrabajadasMapper horasTrabajadasMapper, EmpleadoRepository empleadoRepository,
            ProyectoRepository proyectoRepository) {
        this.horasTrabajadasRepository = horasTrabajadasRepository;
        this.horasTrabajadasMapper = horasTrabajadasMapper;
        this.empleadoRepository = empleadoRepository;
        this.proyectoRepository = proyectoRepository;
    }

    @Transactional(readOnly = true)
    public Page<HorasTrabajadasResponse> getAllHorasTrabajadas(Pageable pageable, String filter) {
        return (filter != null && !filter.trim().isEmpty())
                ? horasTrabajadasRepository.findByFilterWithDetails(filter, pageable)
                : horasTrabajadasRepository.findAllWithDetails(pageable);
    }

    @Transactional(readOnly = true)
    public HorasTrabajadasResponse getHorasTrabajadasById(Long id) {
        return horasTrabajadasRepository.findById(id)
                .map(horasTrabajadasMapper::toHorasTrabajadasResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registro de horas trabajadas no encontrado con ID: " + id));
    }

    @Transactional
    public HorasTrabajadasResponse createHorasTrabajadas(HorasTrabajadasRequest horasTrabajadasRequest) {
        Empleado empleado = empleadoRepository.findById(horasTrabajadasRequest.getIdEmpleado())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empleado no encontrado con ID: " + horasTrabajadasRequest.getIdEmpleado()));

        Proyecto proyecto = proyectoRepository.findById(horasTrabajadasRequest.getIdProyecto())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proyecto no encontrado con ID: " + horasTrabajadasRequest.getIdProyecto()));

        HorasTrabajadas horasTrabajadas = horasTrabajadasMapper.toHorasTrabajadas(horasTrabajadasRequest);
        horasTrabajadas.setEmpleado(empleado);
        horasTrabajadas.setProyecto(proyecto);
        horasTrabajadas.setCostoPorHoraActual(empleado.getCostoPorHora());

        HorasTrabajadas savedHorasTrabajadas = horasTrabajadasRepository.save(horasTrabajadas);

        actualizarCostoManoDeObraProyecto(proyecto);

        return horasTrabajadasMapper.toHorasTrabajadasResponse(savedHorasTrabajadas);
    }

    @Transactional
    public HorasTrabajadasResponse updateHorasTrabajadas(Long id, HorasTrabajadasRequest horasTrabajadasRequest) {
        HorasTrabajadas horasTrabajadas = horasTrabajadasRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registro de horas trabajadas no encontrado con ID: " + id));

        Proyecto proyectoOriginal = horasTrabajadas.getProyecto();

        horasTrabajadasMapper.updateHorasTrabajadasFromRequest(horasTrabajadasRequest, horasTrabajadas);

        // Refrescar relaciones si han cambiado
        if (!horasTrabajadas.getEmpleado().getId().equals(horasTrabajadasRequest.getIdEmpleado())) {
            Empleado nuevoEmpleado = empleadoRepository.findById(horasTrabajadasRequest.getIdEmpleado())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Empleado no encontrado con ID: " + horasTrabajadasRequest.getIdEmpleado()));
            horasTrabajadas.setEmpleado(nuevoEmpleado);
            horasTrabajadas.setCostoPorHoraActual(nuevoEmpleado.getCostoPorHora());
        }

        if (!horasTrabajadas.getProyecto().getId().equals(horasTrabajadasRequest.getIdProyecto())) {
            Proyecto nuevoProyecto = proyectoRepository.findById(horasTrabajadasRequest.getIdProyecto())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Proyecto no encontrado con ID: " + horasTrabajadasRequest.getIdProyecto()));
            horasTrabajadas.setProyecto(nuevoProyecto);
        }

        HorasTrabajadas updatedHorasTrabajadas = horasTrabajadasRepository.save(horasTrabajadas);

        // Recalcular costos para ambos proyectos si el proyecto cambió
        actualizarCostoManoDeObraProyecto(proyectoOriginal);
        if (!proyectoOriginal.getId().equals(updatedHorasTrabajadas.getProyecto().getId())) {
            actualizarCostoManoDeObraProyecto(updatedHorasTrabajadas.getProyecto());
        }

        return horasTrabajadasMapper.toHorasTrabajadasResponse(updatedHorasTrabajadas);
    }

    @Transactional
    public void deleteHorasTrabajadas(Long id) {
        HorasTrabajadas horasTrabajadas = horasTrabajadasRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registro de horas trabajadas no encontrado con ID: " + id));

        Proyecto proyectoAfectado = horasTrabajadas.getProyecto();

        horasTrabajadasRepository.delete(horasTrabajadas);

        // Asegurarse de que el proyecto no sea nulo antes de actualizar
        if (proyectoAfectado != null) {
            actualizarCostoManoDeObraProyecto(proyectoAfectado);
        }
    }

    private void actualizarCostoManoDeObraProyecto(Proyecto proyecto) {
        List<HorasTrabajadas> horasDelProyecto = horasTrabajadasRepository.findByProyectoId(proyecto.getId());
        BigDecimal nuevoCostoTotal = horasDelProyecto.stream()
                .map(h -> h.getHoras().multiply(h.getCostoPorHoraActual()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        proyecto.setCostoManoDeObra(nuevoCostoTotal);
        proyectoRepository.save(proyecto);
    }

    @Transactional(readOnly = true)
    public List<HorasTrabajadasExportDTO> findHorasTrabajadasForExport(String filter, String sort) {
        Sort sortObj = Sort.by(Sort.Direction.DESC, "fecha"); // Ordenar por fecha por defecto
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                String property = sortParts[0];
                Sort.Direction direction = "desc".equalsIgnoreCase(sortParts[1]) ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
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
     * Itera sobre todos los registros de horas trabajadas con 'costoPorHoraActual'
     * nulo,
     * asigna el costo por hora actual del empleado y guarda el registro.
     * Nota: Este método solo debe ejecutarse una vez en el despliegue.
     */
    @Transactional
    public void corregirCostosHorasHistoricas() {
        System.out.println("Iniciando corrección de costos por hora en registros históricos...");

        // Usamos una consulta personalizada para obtener solo los registros que
        // necesitan ser corregidos
        List<HorasTrabajadas> registrosSinCosto = horasTrabajadasRepository.findByCostoPorHoraActualIsNull();

        for (HorasTrabajadas registro : registrosSinCosto) {
            // Se asume que la entidad Empleado ya está cargada debido a las anotaciones de
            // relación
            Empleado empleado = registro.getEmpleado();
            if (empleado != null && empleado.getCostoPorHora() != null) {
                registro.setCostoPorHoraActual(empleado.getCostoPorHora());
                horasTrabajadasRepository.save(registro);
            }
        }
        System.out.println(
                "Corrección de costos por hora completada. Registros actualizados: " + registrosSinCosto.size());
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
            proyecto.setCostoManoDeObra(BigDecimal.ZERO);
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

                proyecto.setCostoManoDeObra(proyecto.getCostoManoDeObra().add(costoTotal));
                proyectoRepository.save(proyecto);
            }
        }
        System.out.println("Recálculo de gastos consolidados completado.");
    }

}
