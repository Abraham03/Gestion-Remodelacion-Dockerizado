package com.gestionremodelacion.gestion.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import com.gestionremodelacion.gestion.empresa.model.Empresa;

@Component
public class ExcelExportStrategy implements ExportStrategy {

    @Override
    public ExportType getType() {
        return ExportType.EXCEL;
    }

    @Override
    public ByteArrayOutputStream generateReport(List<? extends Exportable> data, String title, Empresa empresa) {
        // 1. Creamos el stream fuera del try para tener control manual
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 2. SXSSFWorkbook dentro del try para asegurar que se limpie la memoria temporal al terminar
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {

            Sheet sheet = workbook.createSheet(title);

            // Le decimos a SXSSF que rastree el ancho de las columnas aunque los datos estén en disco.
            if (sheet instanceof SXSSFSheet) {
                ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
            }

            // --- ESTILOS (Misma lógica visual) ---
            CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLACK.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN);

            // --- ENCABEZADOS Y DATOS ---
            int rowIdx = 0;

            // Encabezado de Empresa (Opcional, misma lógica de antes)
            if (empresa != null) {
                Row companyRow = sheet.createRow(rowIdx++);
                Cell companyCell = companyRow.createCell(0);
                companyCell.setCellValue(empresa.getNombreEmpresa());
                companyCell.setCellStyle(titleStyle);
                // Unimos celdas si hay datos, si no, asumimos un ancho de 5 columnas por defecto
                int colSpan = (data != null && !data.isEmpty()) ? data.get(0).getExportHeaders().size() - 1 : 5;
                if (colSpan > 0) {
                    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, colSpan));
                }
            }

            // Fila vacía
            rowIdx++;

            if (data != null && !data.isEmpty()) {
                // Headers de la Tabla
                List<String> headers = data.get(0).getExportHeaders();
                Row headerRow = sheet.createRow(rowIdx++);
                for (int col = 0; col < headers.size(); col++) {
                    Cell cell = headerRow.createCell(col);
                    cell.setCellValue(headers.get(col));
                    cell.setCellStyle(headerStyle);
                }

                // Datos
                for (Exportable item : data) {
                    for (List<String> rowData : item.getExportData()) {
                        Row row = sheet.createRow(rowIdx++);
                        for (int col = 0; col < rowData.size(); col++) {
                            Cell cell = row.createCell(col);
                            cell.setCellValue(rowData.get(col));
                            cell.setCellStyle(cellStyle);
                        }
                    }
                }

                // Autoajustar columnas (Cuidado: esto es lento en reportes masivos, úsalo con precaución)
                for (int i = 0; i < headers.size(); i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            workbook.write(out);
            return out;

        } catch (IOException e) {
            throw new RuntimeException("Error al generar el archivo Excel", e);
        }
    }

}
