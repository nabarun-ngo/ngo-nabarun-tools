package ngo.nabarun.tools.misc_tools.converter.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ngo.nabarun.tools.misc_tools.converter.models.ApiEndpoint;

@Component
public class OpenAPIToExcelConverter {

	public void convertToExcelV3(File input,File output) {
		try {
			  // Load the OpenAPI JSON file
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(input); // Replace with your file path

            List<ApiEndpoint> endpoints = new ArrayList<>();
            JsonNode pathsNode = rootNode.path("paths");

            // Pattern to extract all scopes from the description
            Pattern scopePattern = Pattern.compile("SCOPE_([a-zA-Z:_]+)");

            // Iterate over paths
            Iterator<String> paths = pathsNode.fieldNames();
            while (paths.hasNext()) {
                String path = paths.next();
                JsonNode pathNode = pathsNode.path(path);

                // Iterate over HTTP methods
                Iterator<String> methods = pathNode.fieldNames();
                while (methods.hasNext()) {
                    String method = methods.next();
                    JsonNode methodNode = pathNode.path(method);

                    // Extract fields
                    String summary = methodNode.path("summary").asText(null);
                    String description = methodNode.path("description").asText(null);

                    // Extract tags
                    List<String> tags = new ArrayList<>();
                    JsonNode tagsNode = methodNode.path("tags");
                    if (tagsNode.isArray()) {
                        for (JsonNode tagNode : tagsNode) {
                            tags.add(tagNode.asText());
                        }
                    }

                    // Extract all scopes from the description
                    List<String> authorityScopes = new ArrayList<>();
                    if (description != null) {
                        Matcher matcher = scopePattern.matcher(description);
                        while (matcher.find()) {
                            authorityScopes.add(matcher.group(1));
                        }
                    }

                    // Add to the list
                    endpoints.add(new ApiEndpoint(path, method, summary, description, tags, authorityScopes));
                }
            }

            // Print the extracted data
            endpoints.forEach(System.out::println);
            writeToExcel(endpoints,output);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	
	 private static void writeToExcel(List<ApiEndpoint> endpoints, File output) throws IOException {
	        Workbook workbook = new XSSFWorkbook();
	        Sheet sheet = workbook.createSheet("API_Endpoints");

	        // Create header row
	        Row headerRow = sheet.createRow(0);
	        String[] headers = {"Tags", "Path", "Method", "Summary",  "Authority Scopes","Description" };
	        for (int i = 0; i < headers.length; i++) {
	            Cell cell = headerRow.createCell(i);
	            cell.setCellValue(headers[i]);
	            cell.setCellStyle(getHeaderCellStyle(workbook));
	        }

	        // Populate rows with data
	        int rowIndex = 1;
	        for (ApiEndpoint endpoint : endpoints) {
	            Row row = sheet.createRow(rowIndex++);
	            row.createCell(0).setCellValue(String.join(", ", endpoint.getTags()));
	            row.createCell(1).setCellValue(endpoint.getPath());
	            row.createCell(2).setCellValue(endpoint.getMethod());
	            row.createCell(3).setCellValue(endpoint.getSummary());
	            row.createCell(4).setCellValue(String.join(", ", endpoint.getAuthorityScopes()));
	            row.createCell(5).setCellValue(endpoint.getDescription());
	        }

	        // Auto-size columns
	        for (int i = 0; i < headers.length; i++) {
	            sheet.autoSizeColumn(i);
	        }

	        // Write to a file
	        try (FileOutputStream fileOut = new FileOutputStream(output)) {
	            workbook.write(fileOut);
	        }
	        workbook.close();
	        System.out.println("Excel file '"+output.getName()+"' created successfully.");
	    }

	    private static CellStyle getHeaderCellStyle(Workbook workbook) {
	        CellStyle style = workbook.createCellStyle();
	        Font font = workbook.createFont();
	        font.setBold(true);
	        style.setFont(font);
	        return style;
	    }

}
