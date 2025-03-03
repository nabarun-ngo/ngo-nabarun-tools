package ngo.nabarun.tools.helper;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class RolePermissionExtractor {
    private Map<String, List<String>> rolePermissionMap;
    private Set<String> allPermissions;
    private Map<String, String> permissionDescriptions;
    
    public RolePermissionExtractor(File file, String sheetName) {
        rolePermissionMap = new HashMap<>();
        allPermissions = new HashSet<>();
        permissionDescriptions = new HashMap<>();
        loadExcelData(file, sheetName);
    }
    
    private void loadExcelData(File file, String sheetName) {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                System.out.println("Sheet not found: " + sheetName);
                return;
            }
            
            Iterator<Row> rowIterator = sheet.iterator();
            Row headerRow = rowIterator.next(); // First row contains headers
            
            List<String> roles = new ArrayList<>();
            List<Integer> roleColumns = new ArrayList<>();
            
            for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String columnName = cell.getStringCellValue().trim();
                    if (columnName.startsWith("ROLE-")) {
                        String roleName = columnName.replace("ROLE-", "");
                        roles.add(roleName);
                        roleColumns.add(i);
                        rolePermissionMap.put(roleName, new ArrayList<>());
                    }
                }
            }
            
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Cell permissionCell = row.getCell(0);
                Cell descriptionCell = row.getCell(1);
                
                if (permissionCell != null) {
                    String permission = permissionCell.getStringCellValue().trim();
                    if (!permission.isEmpty()) {
                        allPermissions.add(permission);
                        String description = (descriptionCell != null) ? descriptionCell.getStringCellValue().trim() : "";
                        permissionDescriptions.put(permission, description);
                    }
                    
                    for (int i = 0; i < roleColumns.size(); i++) {
                        Cell cell = row.getCell(roleColumns.get(i));
                        if (cell != null && "Y".equalsIgnoreCase(cell.getStringCellValue().trim())) {
                            rolePermissionMap.get(roles.get(i)).add(permission);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public Map<String, List<String>> getRolePermissionMap() {
        return rolePermissionMap;
    }
    
    public Set<String> getAllPermissions() {
        return allPermissions;
    }
    
    public Map<String, String> getPermissionDescriptions() {
        return permissionDescriptions;
    }
    
}
