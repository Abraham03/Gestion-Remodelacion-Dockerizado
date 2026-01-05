package com.gestionremodelacion.gestion.export;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gestionremodelacion.gestion.empresa.model.Empresa;

@Service
public class ExporterService {

    private final Map<ExportType, ExportStrategy> strategies;

    // Spring inyecta automáticamente todas las clases que implementan ExportStrategy
    // (ExcelExportStrategy y PdfExportStrategy)
    public ExporterService(List<ExportStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(ExportStrategy::getType, Function.identity()));
    }

    /**
     * Método único para exportar. El "ExportType" decide si es Excel o PDF.
     */
    public ByteArrayOutputStream export(ExportType type, List<? extends Exportable> data, String title, Empresa empresa) {
        ExportStrategy strategy = strategies.get(type);

        if (strategy == null) {
            throw new IllegalArgumentException("Formato de exportación no soportado: " + type);
        }

        return strategy.generateReport(data, title, empresa);
    }
}
