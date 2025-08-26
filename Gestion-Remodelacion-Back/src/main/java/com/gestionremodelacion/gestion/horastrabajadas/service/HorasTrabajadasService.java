package com.gestionremodelacion.gestion.horastrabajadas.service;

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
import com.gestionremodelacion.gestion.horastrabajadas.dto.request.HorasTrabajadasRequest;
import com.gestionremodelacion.gestion.horastrabajadas.dto.response.HorasTrabajadasExportDTO;
import com.gestionremodelacion.gestion.horastrabajadas.dto.response.HorasTrabajadasResponse;
import com.gestionremodelacion.gestion.horastrabajadas.model.HorasTrabajadas;
import com.gestionremodelacion.gestion.horastrabajadas.repository.HorasTrabajadasRepository;
import com.gestionremodelacion.gestion.mapper.HorasTrabajadasMapper;

@Service
public class HorasTrabajadasService {

    private final HorasTrabajadasRepository horasTrabajadasRepository;
    private final HorasTrabajadasMapper horasTrabajadasMapper;

    public HorasTrabajadasService(HorasTrabajadasRepository horasTrabajadasRepository,
            HorasTrabajadasMapper horasTrabajadasMapper) {
        this.horasTrabajadasRepository = horasTrabajadasRepository;
        this.horasTrabajadasMapper = horasTrabajadasMapper;
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
        HorasTrabajadas horasTrabajadas = horasTrabajadasMapper.toHorasTrabajadas(horasTrabajadasRequest);
        HorasTrabajadas savedHorasTrabajadas = horasTrabajadasRepository.save(horasTrabajadas);
        return horasTrabajadasMapper.toHorasTrabajadasResponse(savedHorasTrabajadas);
    }

    @Transactional
    public HorasTrabajadasResponse updateHorasTrabajadas(Long id, HorasTrabajadasRequest horasTrabajadasRequest) {
        HorasTrabajadas horasTrabajadas = horasTrabajadasRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de horas trabajadas no encontrado con ID: " + id));

        horasTrabajadasMapper.updateHorasTrabajadasFromRequest(horasTrabajadasRequest, horasTrabajadas); // <-- Usa el mapper
        HorasTrabajadas updatedHorasTrabajadas = horasTrabajadasRepository.save(horasTrabajadas);
        return horasTrabajadasMapper.toHorasTrabajadasResponse(updatedHorasTrabajadas);
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

}
