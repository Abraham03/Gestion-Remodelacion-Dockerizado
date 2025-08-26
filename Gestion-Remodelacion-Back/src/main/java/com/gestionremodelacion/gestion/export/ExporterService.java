package com.gestionremodelacion.gestion.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

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

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.ITALIC, BaseColor.GRAY);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
    private static final Font CELL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

    public ByteArrayOutputStream exportToExcel(List<? extends Exportable> data, String sheetName) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet(sheetName);

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
            // Color Chinese Black (#121212)
            headerStyle.setFillForegroundColor(new org.apache.poi.xssf.usermodel.XSSFColor(new byte[]{(byte) 18, (byte) 18, (byte) 18}, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());

            int rowIdx = 0;

            try (InputStream is = getClass().getResourceAsStream("/images/logo_Excel.png")) {
                if (is != null) {
                    byte[] bytes = IOUtils.toByteArray(is);
                    int pictureIdx = workbook.addPicture(bytes, XSSFWorkbook.PICTURE_TYPE_PNG);
                    CreationHelper helper = workbook.getCreationHelper();
                    XSSFDrawing drawing = sheet.createDrawingPatriarch();
                    ClientAnchor anchor = helper.createClientAnchor();

                    // Mantenemos el posicionamiento original del logo
                    anchor.setCol1(0);
                    anchor.setRow1(0);
                    anchor.setCol2(2);
                    anchor.setRow2(3);

                    drawing.createPicture(anchor, pictureIdx);

                    sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 1));
                    rowIdx = 3;
                } else {
                    System.err.println("Error: No se pudo encontrar el logo en la ruta: /images/logo_Excel.png");
                }
            } catch (IOException e) {
                System.err.println("Error al cargar el logo para Excel: " + e.getMessage());
            }

            if (data != null && !data.isEmpty()) {
                List<String> headers = data.get(0).getExportHeaders();
                if (headers != null && !headers.isEmpty()) {
                    int startColumn = 2;

                    // Agregar nombre de la empresa
                    Row companyRow = sheet.createRow(1);
                    Cell companyCell = companyRow.createCell(startColumn);
                    companyCell.setCellValue("GBS RENOVATIONS LLC");
                    companyCell.setCellStyle(titleStyle);
                    sheet.addMergedRegion(new CellRangeAddress(1, 1, startColumn, headers.size() - 1));

                    // Agregar número de teléfono
                    Row phoneRow = sheet.createRow(2);
                    Cell phoneCell = phoneRow.createCell(startColumn);
                    phoneCell.setCellValue("Teléfono: 8645931407");
                    sheet.addMergedRegion(new CellRangeAddress(2, 2, startColumn, headers.size() - 1));

                    // Agregar el título del reporte
                    Row titleRow = sheet.createRow(3);
                    Cell titleCell = titleRow.createCell(startColumn);
                    titleCell.setCellValue(sheetName);
                    titleCell.setCellStyle(titleStyle);
                    sheet.addMergedRegion(new CellRangeAddress(titleRow.getRowNum(), titleRow.getRowNum(), startColumn, headers.size() - 1));

                    // Agregar fecha de generación
                    Row dateRow = sheet.createRow(4);
                    Cell dateCell = dateRow.createCell(startColumn);
                    dateCell.setCellValue("Generado el: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
                    sheet.addMergedRegion(new CellRangeAddress(dateRow.getRowNum(), dateRow.getRowNum(), startColumn, headers.size() - 1));

                    rowIdx = 5;
                }
            }

            Row headerRow = sheet.createRow(rowIdx++);
            for (int col = 0; col < data.get(0).getExportHeaders().size(); col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(data.get(0).getExportHeaders().get(col));
                cell.setCellStyle(headerStyle);
            }
            sheet.createFreezePane(0, rowIdx - 1);

            for (Exportable item : data) {
                List<List<String>> itemData = item.getExportData();
                if (itemData != null && !itemData.isEmpty()) {
                    for (List<String> rowItems : itemData) {
                        Row row = sheet.createRow(rowIdx++);
                        for (int col = 0; col < rowItems.size(); col++) {
                            Cell cell = row.createCell(col);
                            cell.setCellValue(rowItems.get(col));
                            cell.setCellStyle(cellStyle);
                        }
                    }
                }
            }

            List<String> headers = data.get(0).getExportHeaders();
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out;
        }
    }

    /**
     * Exporta los datos de una lista de objetos que implementan
     * {@link Exportable} a un documento PDF en memoria. El documento incluye un
     * encabezado con un logo, título, fecha y una tabla con los datos
     * proporcionados.
     *
     * @param data Una lista de objetos de cualquier tipo que implementen la
     * interfaz Exportable.
     * @param title El título principal que se mostrará en el documento PDF.
     * @return Un {@link ByteArrayOutputStream} que contiene el archivo PDF en
     * formato binario.
     * @throws DocumentException Si ocurre un error relacionado con la
     * estructura del documento PDF.
     * @throws IOException Si ocurre un error de entrada/salida al leer el logo
     * o escribir el PDF.
     */
    public ByteArrayOutputStream exportToPdf(List<? extends Exportable> data, String title) throws DocumentException, IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            // --- Inicio del bloque de código para agregar el logo ---
            try {
                // Ruta de la imagen del logo que quieres usar
                URI logoUri = getClass().getResource("/images/logo_Excel.png").toURI();
                Path logoPath = Paths.get(logoUri);
                Image logo = Image.getInstance(Files.readAllBytes(logoPath));

                // Escala y ajusta el tamaño del logo (por ejemplo, al 30% del ancho de la página)
                float logoWidth = document.getPageSize().getWidth() * 0.30f;
                float logoHeight = (logo.getHeight() / logo.getWidth()) * logoWidth;
                logo.scaleAbsolute(logoWidth, logoHeight);

                // Posiciona el logo en la página (coordenadas x, y)
                float x = document.leftMargin();
                float y = document.getPageSize().getTop() - logoHeight - 10f;
                logo.setAbsolutePosition(x, y);

                // Agrega el logo directamente al documento, fuera de la tabla
                document.add(logo);
            } catch (IOException | URISyntaxException | DocumentException e) {
                // En caso de error al cargar el logo, se ignora y se continúa
            }
            // --- Fin del bloque del logo ---

            document.add(new Paragraph("\n\n")); // Ajusta los saltos de línea según sea necesario.

            // Sección 1: Encabezado del documento
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);
            headerTable.setSpacingAfter(20f);

            PdfPCell titleCell = new PdfPCell();
            titleCell.setBorder(PdfPCell.NO_BORDER);
            titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            titleCell.setVerticalAlignment(Element.ALIGN_TOP);

            // **Modificación:** Agregar nombre de la empresa y teléfono
            Paragraph companyName = new Paragraph("GBS RENOVATIONS LLC", TITLE_FONT);
            companyName.setAlignment(Element.ALIGN_RIGHT);
            titleCell.addElement(companyName);

            Paragraph companyPhone = new Paragraph("Teléfono: 8645931407", SUBTITLE_FONT);
            companyPhone.setAlignment(Element.ALIGN_RIGHT);
            titleCell.addElement(companyPhone);

            // Agregar el título del reporte
            Paragraph titleParagraph = new Paragraph(title, SUBTITLE_FONT);
            titleParagraph.setAlignment(Element.ALIGN_RIGHT);
            titleCell.addElement(titleParagraph);

            // Agregar la fecha de generación
            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            Paragraph dateParagraph = new Paragraph("Generado el: " + currentDate, SUBTITLE_FONT);
            dateParagraph.setAlignment(Element.ALIGN_RIGHT);
            titleCell.addElement(dateParagraph);

            headerTable.addCell(titleCell);
            document.add(headerTable);

            // Sección 2: Tabla de datos
            if (data != null && !data.isEmpty()) {
                List<String> headers = data.get(0).getExportHeaders();
                if (headers != null && !headers.isEmpty()) {
                    PdfPTable table = new PdfPTable(headers.size());
                    table.setWidthPercentage(100);
                    table.setSpacingBefore(10f);
                    table.setSpacingAfter(10f);

                    // **Modificación:** Usar el color del manual de marca para el encabezado
                    BaseColor headerColor = new BaseColor(18, 18, 18); // Chinese Black (#121212)
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
                        List<List<String>> rows = item.getExportData();
                        for (List<String> rowItems : rows) {
                            for (String cellText : rowItems) {
                                PdfPCell cell = new PdfPCell(new Phrase(cellText, CELL_FONT));
                                cell.setPadding(5);
                                cell.setBorderColor(BaseColor.LIGHT_GRAY);
                                table.addCell(cell);
                            }
                        }
                    }
                    document.add(table);
                } else {
                    document.add(new Paragraph("No hay encabezados para la tabla de datos."));
                }
            } else {
                document.add(new Paragraph("No hay datos para mostrar."));
            }

            document.close();
            return out;
        }
    }
}
