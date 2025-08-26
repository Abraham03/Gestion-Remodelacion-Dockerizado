package com.gestionremodelacion.gestion.proyecto.service;

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
import com.gestionremodelacion.gestion.mapper.ProyectoMapper;
import com.gestionremodelacion.gestion.proyecto.dto.request.ProyectoRequest;
import com.gestionremodelacion.gestion.proyecto.dto.response.ProyectoExcelDTO;
import com.gestionremodelacion.gestion.proyecto.dto.response.ProyectoPdfDTO;
import com.gestionremodelacion.gestion.proyecto.dto.response.ProyectoResponse;
import com.gestionremodelacion.gestion.proyecto.model.Proyecto;
import com.gestionremodelacion.gestion.proyecto.repository.ProyectoRepository;

@Service
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final ProyectoMapper proyectoMapper;

    public ProyectoService(ProyectoRepository proyectoRepository, ProyectoMapper proyectoMapper) {
        this.proyectoRepository = proyectoRepository;
        this.proyectoMapper = proyectoMapper;
    }

    @Transactional(readOnly = true)
    public Page<ProyectoResponse> getAllProyectos(Pageable pageable, String filter) {
        if (filter != null && !filter.trim().isEmpty()) {
            return proyectoRepository.findByFilterWithDetails(filter, pageable);
        } else {
            return proyectoRepository.findAllWithDetails(pageable);
        }
    }

    @Transactional(readOnly = true)
    public ProyectoResponse getProyectoById(Long id) {
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado con ID: " + id));
        return proyectoMapper.toProyectoResponse(proyecto);
    }

    @Transactional
    public ProyectoResponse createProyecto(ProyectoRequest proyectoRequest) {
        Proyecto proyecto = proyectoMapper.toProyecto(proyectoRequest);
        Proyecto savedProyecto = proyectoRepository.save(proyecto);
        return proyectoMapper.toProyectoResponse(savedProyecto);
    }

    @Transactional
    public ProyectoResponse updateProyecto(Long id, ProyectoRequest proyectoRequest) {
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado con ID: " + id));

        proyectoMapper.updateProyectoFromRequest(proyectoRequest, proyecto);
        Proyecto updatedProyecto = proyectoRepository.save(proyecto);
        return proyectoMapper.toProyectoResponse(updatedProyecto);
    }

    @Transactional
    public ApiResponse<Void> deleteProyecto(Long id) {
        if (!proyectoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado con ID: " + id);
        }
        proyectoRepository.deleteById(id);
        return ApiResponse.success(null);
    }

    private List<Proyecto> findAllProyectosForExport(String filter, String sort) {
        Sort sortObj = Sort.by(Sort.Direction.ASC, "nombreProyecto");
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                String property = sortParts[0];
                Sort.Direction direction = "desc".equalsIgnoreCase(sortParts[1]) ? Sort.Direction.DESC : Sort.Direction.ASC;
                sortObj = Sort.by(direction, property);
            }
        }

        if (filter != null && !filter.trim().isEmpty()) {
            return proyectoRepository.findByFilterForExport(filter, sortObj);
        } else {
            return proyectoRepository.findAll(sortObj);
        }
    }

    @Transactional(readOnly = true)
    public List<ProyectoPdfDTO> findProyectosForPdfExport(String filter, String sort) {
        List<Proyecto> proyectos = findAllProyectosForExport(filter, sort);
        return proyectos.stream()
                .map(ProyectoPdfDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProyectoExcelDTO> findProyectosForExcelExport(String filter, String sort) {
        List<Proyecto> proyectos = findAllProyectosForExport(filter, sort);
        return proyectos.stream()
                .map(ProyectoExcelDTO::new)
                .collect(Collectors.toList());
    }

}
