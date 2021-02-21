package de.hs_mannheim.informatik.ct.controller;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuchDTO;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

@Component
public class Utilities {
	@Value("${server.port}")
	private String port;

	@Value("${hostname}")
	private String host;

	Workbook excelErzeugen(Collection<VeranstaltungsBesuchDTO> kontakte, String email) {
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("Kontaktliste");

		Font font = sheet.getWorkbook().createFont();
		font.setFontHeightInPoints((short) 16);
		CellStyle csbi = wb.createCellStyle();
		csbi.setFont(font);

		font = sheet.getWorkbook().createFont();
		font.setBold(true);
		CellStyle csbo = wb.createCellStyle();
		csbo.setFont(font);

		font = sheet.getWorkbook().createFont();
		font.setItalic(true);
		CellStyle csit = wb.createCellStyle();
		csit.setFont(font);

		font = sheet.getWorkbook().createFont();
		font.setColor((short)10);  // rot
		font.setBold(true);
		CellStyle csrb = wb.createCellStyle();
		csrb.setFont(font);

		int rows = 0;
		Row row = sheet.createRow(rows++);
		Cell cell = row.createCell(0);
		cell.setCellValue("Mögliche Kontakte von " + email + " an der Hochschule Mannheim");
		cell.setCellStyle(csbi);

		row = sheet.createRow(rows++);
		cell = row.createCell(0);
		cell.setCellValue("(Stand: " + DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm").format(LocalDateTime.now()) + ")");
		cell.setCellStyle(csit);

		rows++;
		row = sheet.createRow(rows++);
		int col = 0;
		for (String titel : new String[] {"EMail-Adresse", "Raum/Veranstaltung", "Anmeldezeit", "Zeitlicher Abstand"}) {
			cell = row.createCell(col);
			cell.setCellValue(titel);
			cell.setCellStyle(csbo);
			sheet.setColumnWidth(col++, 24 * 256);
		};

		for (VeranstaltungsBesuchDTO kontakt : kontakte) {
			Row lrow = sheet.createRow(rows++);

			lrow.createCell(0).setCellValue(kontakt.getBesucherEmail());
			lrow.createCell(1).setCellValue(kontakt.getVeranstaltungsName());
			lrow.createCell(2).setCellValue(new SimpleDateFormat("dd.MM.yyyy, HH:mm ").format(kontakt.getTimestamp()));
			lrow.createCell(3).setCellValue(kontakt.getDiffInMin() + " min");

			if (email.equals(kontakt.getBesucherEmail())) {
				IntStream.range(0, 4).forEach(i -> lrow.getCell(i).setCellStyle(csrb));
			}
		}

		row = sheet.createRow(rows + 1);
		cell = row.createCell(0);
		cell.setCellValue("Erstellt mit CTT, dem Corona Tracking Tool der Hochschule Mannheim");
		cell.setCellStyle(csit);

		return wb;
	}

	public Date uhrzeitAufDatumSetzen(Date datum, String zeit) {
		if (zeit !=  null && zeit.length() == 5) {
			Calendar cal = new GregorianCalendar();
			cal.setTime(datum);
			cal.set(Calendar.HOUR, Integer.parseInt(zeit.substring(0, 2)));
			cal.set(Calendar.MINUTE, Integer.parseInt(zeit.substring(3, 5)));
			
			datum = cal.getTime();
		}

		return datum;
	}

	/**
	 * Converts a relative local path to an absolute URI
	 * @param localPath Local path of the resource
	 * @param request The request is used to differentiate between http/https. Should be moved to setting!
	 * @return An absolute URI to the given resource
	 */
	public UriComponents getUriToLocalPath(String localPath, HttpServletRequest request) {
		 return UriComponentsBuilder.newInstance()
				.scheme(request.getScheme()) // TODO: Optimally http/https should be configured somewhere
				.host(host)
				.port(port)
				.path(localPath)
				.build();
	}
}
