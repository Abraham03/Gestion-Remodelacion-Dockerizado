package com.gestionremodelacion.gestion.export;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.gestionremodelacion.gestion.empresa.model.Empresa;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Component
public class PdfExportStrategy implements ExportStrategy {

    // --- CONSTANTES DE ESTILO (Performance: Se crean una sola vez) ---
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.ITALIC, BaseColor.GRAY);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
    private static final Font CELL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
    private static final BaseColor HEADER_BG_COLOR = new BaseColor(18, 18, 18);

    @Override
    public ExportType getType() {
        return ExportType.PDF;
    }

    @Override
    public ByteArrayOutputStream generateReport(List<? extends Exportable> data, String title, Empresa empresa) {
// Usamos try-with-resources para asegurar que el Stream se cierre (Memory Management)
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            // 1. Agregar Logo (Manejo de errores aislado)
            addCompanyLogo(document, empresa);

            // Espacio
            document.add(new Paragraph("\n\n"));

            // 2. Agregar Encabezado de la Empresa
            addCompanyHeader(document, title, empresa);

            // 3. Generar Tabla de Datos
            addDataTable(document, data);

            document.close();
            return out;

        } catch (Exception e) {
            // Loguear el error y relanzar como RuntimeException para que el Controller lo maneje
            throw new RuntimeException("Error al generar el reporte PDF: " + e.getMessage(), e);
        }
    }

    // --- MÉTODOS PRIVADOS (SOLID: Fragmentación de lógica) ---
    private void addCompanyLogo(Document document, Empresa empresa) {
        if (empresa == null || empresa.getLogoUrl() == null || empresa.getLogoUrl().isEmpty()) {
            return;
        }
        try {
            // Performance: Cargar imágenes por URL es lento. 
            // En un futuro, considera cachear estas imágenes en el servidor.
            Image logo = Image.getInstance(new URL(empresa.getLogoUrl()));

            float logoWidth = document.getPageSize().getWidth() * 0.30f;
            float logoHeight = (logo.getHeight() / logo.getWidth()) * logoWidth;

            logo.scaleAbsolute(logoWidth, logoHeight);

            // Posicionamiento absoluto (Esquina superior izquierda)
            float x = document.leftMargin();
            float y = document.getPageSize().getTop() - logoHeight - 10f;
            logo.setAbsolutePosition(x, y);

            document.add(logo);
        } catch (Exception e) {
            // Fail-safe: Si la imagen falla (404, timeout), el reporte se genera SIN logo.
            System.err.println("Advertencia: No se pudo cargar el logo: " + e.getMessage());
        }
    }

    private void addCompanyHeader(Document document, String title, Empresa empresa) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        headerTable.setSpacingAfter(20f);

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(PdfPCell.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        titleCell.setVerticalAlignment(Element.ALIGN_TOP);

        // Datos dinámicos
        String nombreEmpresa = (empresa != null) ? empresa.getNombreEmpresa() : "N/A";
        String telefono = (empresa != null && empresa.getTelefono() != null)
                ? formatPhoneNumber(empresa.getTelefono())
                : "No disponible";

        // Construcción de parrafos
        addParagraphToCell(titleCell, nombreEmpresa, TITLE_FONT, Element.ALIGN_RIGHT);
        addParagraphToCell(titleCell, "Tel: " + telefono, SUBTITLE_FONT, Element.ALIGN_RIGHT);
        addParagraphToCell(titleCell, title, SUBTITLE_FONT, Element.ALIGN_RIGHT);

        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        addParagraphToCell(titleCell, "Generado el: " + currentDate, SUBTITLE_FONT, Element.ALIGN_RIGHT);

        headerTable.addCell(titleCell);
        document.add(headerTable);
    }

    private void addDataTable(Document document, List<? extends Exportable> data) throws DocumentException {
        if (data == null || data.isEmpty()) {
            document.add(new Paragraph("No hay datos para mostrar."));
            return;
        }

        // Obtener headers del primer elemento
        List<String> headers = data.get(0).getExportHeaders();
        if (headers == null || headers.isEmpty()) {
            return;
        }

        PdfPTable table = new PdfPTable(headers.size());
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        // 3.1 Renderizar Encabezados de Tabla
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setBackgroundColor(HEADER_BG_COLOR);
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);
        }

        // 3.2 Renderizar Datos (Streams implicitos en el foreach)
        for (Exportable item : data) {
            List<List<String>> rowData = item.getExportData();
            if (rowData != null) {
                for (List<String> row : rowData) {
                    for (String cellText : row) {
                        PdfPCell cell = new PdfPCell(new Phrase(cellText, CELL_FONT));
                        cell.setPadding(5);
                        cell.setBorderColor(BaseColor.LIGHT_GRAY);
                        table.addCell(cell);
                    }
                }
            }
        }
        document.add(table);
    }

    // Helper para reducir verbosidad
    private void addParagraphToCell(PdfPCell cell, String text, Font font, int alignment) {
        Paragraph p = new Paragraph(text, font);
        p.setAlignment(alignment);
        cell.addElement(p);
    }

    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return "";
        }
        String digitsOnly = phoneNumber.replaceAll("\\D", "");
        if (digitsOnly.length() != 10) {
            return phoneNumber;
        }
        return String.format("(%s) %s-%s",
                digitsOnly.substring(0, 3),
                digitsOnly.substring(3, 6),
                digitsOnly.substring(6, 10));
    }

}
