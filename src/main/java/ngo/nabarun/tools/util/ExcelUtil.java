package ngo.nabarun.tools.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import io.micrometer.common.util.StringUtils;

public class ExcelUtil {
	protected static DataFormatter fmt = new DataFormatter();

	/**
	 * Takes Excel File as an input and converts map of sheets
	 * 
	 * @param fileName
	 * @return map of SheetName and Sheet Data as a 2D List
	 * @throws Exception
	 */
	public static Map<String, List<List<String>>> readExcelWorkBook(File source) throws Exception {
		String fileExt = FilenameUtils.getExtension(source.getName()).toUpperCase();
		if (!(fileExt.equalsIgnoreCase("xls") || fileExt.equalsIgnoreCase("xlsx"))) {
			throw new Exception("Only supported format is .xlsx or .xls");
		}
		Map<String, List<List<String>>> excelData = new HashMap<>();
		FileInputStream inputStream = new FileInputStream(source);
		System.err.println(source);
		try (Workbook workbook = fileExt.equalsIgnoreCase("xlsx") ? new XSSFWorkbook(inputStream)
				: new HSSFWorkbook(inputStream)) {
			for (int p = 0; p < workbook.getNumberOfSheets(); p++) {
				try {
					Sheet sheet = workbook.getSheetAt(p);
					excelData.put(workbook.getSheetName(p), getSheetData(sheet));
				} catch (Exception err) {
					err.printStackTrace();
				}
			}
		}
		inputStream.close();
		return excelData;

	}

	/**
	 * Can be used to get data from any Excel Sheet in form of List
	 * 
	 * @param sheetName
	 * @return sheetData in form of List<List<String>>
	 */
	private static List<List<String>> getSheetData(Sheet sheet) {
        List<List<String>> data = new ArrayList<>();
        for (Row row : sheet) {
            List<String> rowData = new ArrayList<>();
            for (Cell cell : row) {
                String value = getCellValue(cell);
                if (!StringUtils.isEmpty(value)) {
                    rowData.add(value.trim());
                }
            }
            data.add(rowData);
        }
        return data;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return fmt.formatCellValue(cell);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return getCachedFormulaValue(cell);
            case BLANK:
                return "";
            default:
                return "Unsupported Cell Type";
        }
    }

    private static String getCachedFormulaValue(Cell cell) {
        switch (cell.getCachedFormulaResultType()) {
            case STRING:
                return cell.getRichStringCellValue().getString();
            case NUMERIC:
                return fmt.formatCellValue(cell);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "Formula Not Supported: " + cell.getCellFormula();
        }
    }
	public static void createExcelWorkBook(File source, Map<String, List<List<String>>> data, boolean hasHeader) throws Exception {
		createExcelWorkBook(source,data,hasHeader,null,null);
	}
	public static void createExcelWorkBook(File source, Map<String, List<List<String>>> data, boolean hasHeader,
			IndexedColors hFontColor, IndexedColors hBgColor) throws Exception {
		String fileExt = FilenameUtils.getExtension(source.getName()).toUpperCase();
		if (!(fileExt.equalsIgnoreCase("xls") || fileExt.equalsIgnoreCase("xlsx"))) {
			throw new Exception("Only supported format is .xlsx or .xls");
		}
		Workbook workbook;
		if (source.exists()) {
			FileInputStream inputStream = new FileInputStream(source);
			workbook = fileExt.equalsIgnoreCase("xlsx") ? new XSSFWorkbook(inputStream)
					: new HSSFWorkbook(inputStream);
		} else {
			workbook = fileExt.equalsIgnoreCase("xlsx") ? new XSSFWorkbook() : new HSSFWorkbook();
		}

		for (String sheetName : data.keySet()) {
			// Create a Sheet
			Sheet sheet = workbook.getSheet(sheetName) == null ? workbook.createSheet(sheetName)
					: workbook.getSheet(sheetName);
			List<List<String>> sheetData = data.get(sheetName);

			if (sheetData.size() > 0) {
				int rowStartIndex = 0;
				/*
				 * if header is there
				 */
				if (hasHeader) {
					rowStartIndex = 1;
					Row headerRow = sheet.createRow(0);
					List<String> headerRowData = sheetData.get(0);
					for (int colIndex = 0; colIndex < headerRowData.size(); colIndex++) {
						Cell cell = headerRow.createCell(colIndex);
						cell.setCellValue(headerRowData.get(colIndex));
						cell.setCellStyle(hFontColor == null || hBgColor == null
								? defaultCellStyle(workbook, IndexedColors.BLACK, IndexedColors.YELLOW)
								: defaultCellStyle(workbook, hFontColor, hBgColor));
					}
				}
				for (int rowIndex = rowStartIndex; rowIndex < sheetData.size(); rowIndex++) {
					Row row = sheet.createRow(rowIndex);
					List<String> rowData = sheetData.get(rowIndex);
					for (int colIndex = 0; colIndex < rowData.size(); colIndex++) {
						row.createCell(colIndex).setCellValue(rowData.get(colIndex));
					}
				}
				for (int colIndex = 0; colIndex < sheetData.get(0).size(); colIndex++) {
					sheet.autoSizeColumn(colIndex);
				}
			}

		}
		if (!source.getParentFile().exists()) {
			source.getParentFile().mkdirs();
		}
		FileOutputStream fileOut = new FileOutputStream(source.getAbsolutePath());
		workbook.write(fileOut);
		fileOut.close();

		workbook.close();
	}

	private static CellStyle defaultCellStyle(Workbook workbook, IndexedColors fontColor, IndexedColors bgColor) {
		// Create a Font for styling header cells
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 12);
		headerFont.setColor(fontColor.getIndex());
		// Create a CellStyle with the font
		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFont(headerFont);
		headerCellStyle.setFillForegroundColor(bgColor.getIndex());
		headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerCellStyle.setBorderLeft(BorderStyle.THIN);
		headerCellStyle.setBorderRight(BorderStyle.THIN);
		headerCellStyle.setBorderTop(BorderStyle.THIN);
		headerCellStyle.setBorderBottom(BorderStyle.THIN);
		return headerCellStyle;
	}

}