package com.gestionremodelacion.gestion.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

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

@Service
public class ExporterService {

    // --- ESTILOS (sin cambios) ---
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Font.BOLD,
            BaseColor.DARK_GRAY);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.ITALIC,
            BaseColor.GRAY);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
    private static final Font CELL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

    /**
     * 
     * Exporta datos a Excel, obteniendo el nombre de la empresa dinámicamente.
     * Se ha eliminado la inserción del logo.
     *
     * @param data      La lista de datos a exportar.
     * @param sheetName El nombre de la hoja de Excel.
     * @param empresa   La empresa del usuario que genera el reporte.
     * @return ByteArrayOutputStream con el archivo Excel.
     */
    public ByteArrayOutputStream exportToExcel(List<? extends Exportable> data, String sheetName, Empresa empresa)
            throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet(sheetName);

            // --- ESTILOS DE CELDA (sin cambios) ---
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
            headerStyle.setFillForegroundColor(
                    new org.apache.poi.xssf.usermodel.XSSFColor(new byte[] { (byte) 18, (byte) 18, (byte) 18 }, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());

            // --- INICIO DE ENCABEZADO DINÁMICO ---
            int rowIdx = 0;
            if (data != null && !data.isEmpty()) {
                List<String> headers = data.get(0).getExportHeaders();
                if (headers != null && !headers.isEmpty()) {
                    // Fila 1: Nombre de la Empresa (dinámico)
                    Row companyRow = sheet.createRow(rowIdx++);
                    Cell companyCell = companyRow.createCell(0);
                    companyCell.setCellValue(
                            empresa != null ? empresa.getNombreEmpresa() : "Nombre de Empresa no disponible");
                    companyCell.setCellStyle(titleStyle);
                    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.size() - 1));

                    // Fila 2: Teléfono (marcador de posición)
                    Row phoneRow = sheet.createRow(rowIdx++);
                    Cell phoneCell = phoneRow.createCell(0);

                    // Obtener el teléfono de la empresa (dinámico)
                    String telefono = (empresa != null && empresa.getTelefono() != null)
                            ? formatPhoneNumber(empresa.getTelefono()) // Usa el método para formatear
                            : "No disponible";
                    phoneCell.setCellValue("Tel:" + telefono);
                    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, headers.size() - 1));

                    // Fila 3: Título del Reporte
                    Row titleRow = sheet.createRow(rowIdx++);
                    Cell titleCell = titleRow.createCell(0);
                    titleCell.setCellValue(sheetName);
                    titleCell.setCellStyle(titleStyle);
                    sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, headers.size() - 1));

                    // Fila 4: Fecha de Generación
                    Row dateRow = sheet.createRow(rowIdx++);
                    Cell dateCell = dateRow.createCell(0);
                    dateCell.setCellValue("Generado el: "
                            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
                    sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, headers.size() - 1));

                    rowIdx++; // Deja una fila en blanco
                }
            }

            // --- TABLA DE DATOS (lógica sin cambios) ---
            Row headerRow = sheet.createRow(rowIdx++);
            for (int col = 0; col < data.get(0).getExportHeaders().size(); col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(data.get(0).getExportHeaders().get(col));
                cell.setCellStyle(headerStyle);
            }

            for (Exportable item : data) {
                for (List<String> rowItems : item.getExportData()) {
                    Row row = sheet.createRow(rowIdx++);
                    for (int col = 0; col < rowItems.size(); col++) {
                        Cell cell = row.createCell(col);
                        cell.setCellValue(rowItems.get(col));
                        cell.setCellStyle(cellStyle);
                    }
                }
            }

            for (int i = 0; i < data.get(0).getExportHeaders().size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out;
        }
    }

    /**
     * 
     * Exporta datos a PDF, obteniendo el logo y nombre de la empresa dinámicamente.
     *
     * @param data    La lista de datos a exportar.
     * @param title   El título del reporte.
     * @param empresa La empresa del usuario que genera el reporte.
     * @return ByteArrayOutputStream con el archivo PDF.
     */
    public ByteArrayOutputStream exportToPdf(List<? extends Exportable> data, String title, Empresa empresa)
            throws DocumentException, IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            // --- LOGO DINÁMICO DESDE URL ---
            if (empresa != null && empresa.getLogoUrl() != null && !empresa.getLogoUrl().isEmpty()) {
                try {
                    Image logo = Image.getInstance(new URL(empresa.getLogoUrl()));
                    float logoWidth = document.getPageSize().getWidth() * 0.30f;
                    float logoHeight = (logo.getHeight() / logo.getWidth()) * logoWidth;
                    logo.scaleAbsolute(logoWidth, logoHeight);
                    float x = document.leftMargin();
                    float y = document.getPageSize().getTop() - logoHeight - 10f;
                    logo.setAbsolutePosition(x, y);
                    document.add(logo);
                } catch (Exception e) {
                    System.err.println("Error al cargar el logo desde la URL para el PDF: " + e.getMessage());
                    // Si falla, el reporte se genera sin logo, no se detiene el proceso.
                }
            }

            document.add(new Paragraph("\n\n"));

            // --- ENCABEZADO DINÁMICO ---
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);
            headerTable.setSpacingAfter(20f);

            PdfPCell titleCell = new PdfPCell();
            titleCell.setBorder(PdfPCell.NO_BORDER);
            titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            titleCell.setVerticalAlignment(Element.ALIGN_TOP);

            // Nombre de la empresa (dinámico)
            Paragraph companyName = new Paragraph(empresa != null ? empresa.getNombreEmpresa() : "N/A", TITLE_FONT);
            companyName.setAlignment(Element.ALIGN_RIGHT);
            titleCell.addElement(companyName);

            // Teléfono (marcador de posición)
            String telefono = (empresa != null && empresa.getTelefono() != null)
                    ? formatPhoneNumber(empresa.getTelefono()) // Usa el método para formatear
                    : "No disponible";
            Paragraph companyPhone = new Paragraph("Tel: " + telefono, SUBTITLE_FONT);
            companyPhone.setAlignment(Element.ALIGN_RIGHT);
            titleCell.addElement(companyPhone);

            // Título y fecha
            Paragraph titleParagraph = new Paragraph(title, SUBTITLE_FONT);
            titleParagraph.setAlignment(Element.ALIGN_RIGHT);
            titleCell.addElement(titleParagraph);
            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            Paragraph dateParagraph = new Paragraph("Generado el: " + currentDate, SUBTITLE_FONT);
            dateParagraph.setAlignment(Element.ALIGN_RIGHT);
            titleCell.addElement(dateParagraph);

            headerTable.addCell(titleCell);
            document.add(headerTable);

            // --- TABLA DE DATOS (lógica sin cambios) ---
            if (data != null && !data.isEmpty()) {
                List<String> headers = data.get(0).getExportHeaders();
                if (headers != null && !headers.isEmpty()) {
                    PdfPTable table = new PdfPTable(headers.size());
                    table.setWidthPercentage(100);
                    table.setSpacingBefore(10f);
                    table.setSpacingAfter(10f);
                    BaseColor headerColor = new BaseColor(18, 18, 18);

                    for (String header : headers) {
                        PdfPCell cell = new PdfPCell();
                        cell.setBackgroundColor(headerColor);
                        cell.setPadding(8);
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPhrase(new Phrase(header, HEADER_FONT));
                        cell.setBorderColor(BaseColor.WHITE);
                        table.addCell(cell);
                    }

                    for (Exportable item : data) {
                        for (List<String> rowItems : item.getExportData()) {
                            for (String cellText : rowItems) {
                                PdfPCell cell = new PdfPCell(new Phrase(cellText, CELL_FONT));
                                cell.setPadding(5);
                                cell.setBorderColor(BaseColor.LIGHT_GRAY);
                                table.addCell(cell);
                            }
                        }
                    }
                    document.add(table);
                }
            } else {
                document.add(new Paragraph("No hay datos para mostrar."));
            }

            document.close();
            return out;
        }
    }

    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.replaceAll("\\D", "").length() != 10) {
            return phoneNumber; // Devuelve el original si no tiene 10 dígitos
        }
        String digitsOnly = phoneNumber.replaceAll("\\D", "");
        return String.format("(%s) %s-%s",
                digitsOnly.substring(0, 3),
                digitsOnly.substring(3, 6),
                digitsOnly.substring(6, 10));
    }
}