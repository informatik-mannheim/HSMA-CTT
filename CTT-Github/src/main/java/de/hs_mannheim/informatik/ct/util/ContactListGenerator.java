package de.hs_mannheim.informatik.ct.util;

import de.hs_mannheim.informatik.ct.model.Contact;
import de.hs_mannheim.informatik.ct.persistence.services.DateTimeService;
import lombok.val;
import lombok.var;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class ContactListGenerator {
    private final DateTimeService dateTimeService;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm", Locale.GERMANY);

    private final String sheetName = "Kontaktliste";
    private final int columnWidth = 30 * 256;
    private final Function<String, String> headerText = email ->
            String.format("Mögliche Kontakte von %s an der Hochschule Mannheim", email);
    private final Function<LocalDateTime, String> timestampText = (timestamp) ->
            String.format("Stand: %s", dateTimeFormatter.format(timestamp));
    private final List<String> tableHeadings;
    private final List<Function<Contact<?>, String>> tableCellValues;
    private final String footerNotice = "Erstellt mit CTT, dem Corona Tracking Tool der Hochschule Mannheim";

    private final FontStyler headerFontStyler = font -> font.setFontHeightInPoints((short) 16);
    private final FontStyler timeStampStyler = font -> font.setItalic(true);
    private final FontStyler tableHeadingsStyler = font -> font.setBold(true);
    private final FontStyler footerStyler = font -> font.setItalic(true);
    private final FontStyler contactStyler = font -> {
        val redColorCode = (short) 10;
        font.setBold(true);
        font.setColor(redColorCode);
    };

    public ContactListGenerator(DateTimeService dateTimeService, List<String> tableHeadings, List<Function<Contact<?>, String>> tableCellValues) {
        this.dateTimeService = dateTimeService;
        this.tableHeadings = tableHeadings;
        this.tableCellValues = tableCellValues;
    }

    public HSSFWorkbook generateWorkbook(Iterable<Contact<?>> contacts, String targetEmail) {
        val workbook = new HSSFWorkbook();
        val sheet = workbook.createSheet(sheetName);

        writeText(appendRow(sheet), headerText.apply(targetEmail), styleWithFont(workbook, headerFontStyler));
        writeText(appendRow(sheet), timestampText.apply(dateTimeService.getNow()), styleWithFont(workbook, timeStampStyler));
        appendRow(sheet);
        writeCells(appendRow(sheet), tableHeadings, styleWithFont(workbook, tableHeadingsStyler));
        writeContacts(contacts, targetEmail, workbook, sheet);
        appendRow(sheet);
        writeText(appendRow(sheet), footerNotice, styleWithFont(workbook, footerStyler));

        for (int columnIndex = 0; columnIndex < tableHeadings.size(); columnIndex++) {
            sheet.setColumnWidth(columnIndex, this.columnWidth);
        }

        return workbook;
    }

    private void writeContacts(Iterable<Contact<?>> contacts, String targetEmail, HSSFWorkbook workbook, HSSFSheet sheet) {
        val defaultStyle = workbook.createCellStyle();
        val contactStyle = styleWithFont(workbook, contactStyler);
        for (val contact : contacts) {
            var rowStyle = defaultStyle;
            if (targetEmail.equals(contact.getTarget().getEmail())) {
                rowStyle = contactStyle;
            }

            writeCells(
                    appendRow(sheet),
                    applyTextGenerators(contact, tableCellValues),
                    defaultStyle
            );
        }
    }

    private static void writeText(HSSFRow row, String headerText, CellStyle style) {
        val cell = row.createCell(0);
        cell.setCellValue(headerText);
        cell.setCellStyle(style);
    }

    private static void writeCells(HSSFRow row, Iterable<String> cellTexts, CellStyle style) {
        var cellIndex = 0;
        for (val cellText : cellTexts) {
            val cell = row.createCell(cellIndex++);
            cell.setCellValue(cellText);
            cell.setCellStyle(style);
        }
    }

    private static <T> Iterable<String> applyTextGenerators(T data, Iterable<Function<T, String>> generators) {
        val texts = new ArrayList<String>();
        for (val generator : generators) {
            texts.add(generator.apply(data));
        }

        return texts;
    }

    private static HSSFRow appendRow(HSSFSheet sheet) {
        return sheet.createRow(sheet.getLastRowNum() + 1);
    }

    private static HSSFCellStyle styleWithFont(HSSFWorkbook workbook, FontStyler fontStyler) {
        val font = workbook.createFont();
        val style = workbook.createCellStyle();

        fontStyler.style(font);
        style.setFont(font);

        return style;
    }

    private interface FontStyler {
        void style(Font font);
    }
}
