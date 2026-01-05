package com.gestionremodelacion.gestion.export;

import java.io.ByteArrayOutputStream;
import java.util.List;

import com.gestionremodelacion.gestion.empresa.model.Empresa;

public interface ExportStrategy {

    /**
     * Define qué tipo de archivo genera esta estrategia (EXCEL, PDF, CSV)
     */
    ExportType getType();

    /**
     * Genera el archivo binario.
     */
    ByteArrayOutputStream generateReport(List<? extends Exportable> data, String title, Empresa empresa);
}
