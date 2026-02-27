package com.calzadosmorales.service;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ExcelService {

	public void exportarVentasExcel(HttpServletResponse response, List<Object[]> ventas) throws IOException {
	    XSSFWorkbook workbook = new XSSFWorkbook();
	    XSSFSheet sheet = workbook.createSheet("Historial de Ventas");

	    // 1. CREAR CABECERA SIMPLE (La Tabla le dará el diseño luego)
	    String[] headers = {"ID", "Fecha / Hora", "Vendedor", "Cliente", "Comprobante", "Total", "Estado"};
	    Row headerRow = sheet.createRow(0);
	    for (int i = 0; i < headers.length; i++) {
	        headerRow.createCell(i).setCellValue(headers[i]);
	    }

	    // 2. LLENAR DATOS
	    int rowCount = 1;
	    for (Object[] v : ventas) {
	        Row dataRow = sheet.createRow(rowCount++);
	        dataRow.createCell(0).setCellValue(v[0] != null ? v[0].toString() : "");
	        dataRow.createCell(1).setCellValue(v[1] != null ? v[1].toString() : "");
	        dataRow.createCell(2).setCellValue(v[2] != null ? v[2].toString() : "");
	        dataRow.createCell(3).setCellValue(v[3] != null ? v[3].toString() : "");
	        dataRow.createCell(4).setCellValue(v[4] != null ? v[4].toString() : "");
	        
	        // El Total lo ponemos como número para que la tabla lo reconozca
	        if (v[5] != null) {
	            try {
	                dataRow.createCell(5).setCellValue(Double.parseDouble(v[5].toString()));
	            } catch (Exception e) {
	                dataRow.createCell(5).setCellValue(v[5].toString());
	            }
	        }
	        dataRow.createCell(6).setCellValue(v[6] != null ? v[6].toString() : "");
	    }

	    // 3. CONVERTIR A TABLA OFICIAL (Aquí está la magia sin errores)
	    int lastRow = sheet.getLastRowNum();
	    if (lastRow > 0) { // Solo si hay datos
	        AreaReference reference = workbook.getCreationHelper().createAreaReference(
	                new CellReference(0, 0), 
	                new CellReference(lastRow, headers.length - 1)
	        );

	        XSSFTable table = sheet.createTable(reference);
	        table.setName("VentasMorales");
	        table.getCTTable().addNewAutoFilter();

	        // Estilo predefinido que NO falla: TableStyleMedium2 (Limpio y profesional)
	        table.getCTTable().addNewTableStyleInfo();
	        table.getCTTable().getTableStyleInfo().setName("TableStyleMedium2");
	        table.getCTTable().getTableStyleInfo().setShowRowStripes(true);
	    }

	    // 4. AUTO-AJUSTE DE COLUMNAS
	    for (int i = 0; i < headers.length; i++) {
	        sheet.autoSizeColumn(i);
	    }

	    // 5. ENVIAR AL NAVEGADOR
	    ServletOutputStream outputStream = response.getOutputStream();
	    workbook.write(outputStream);
	    workbook.close();
	    outputStream.close();
	}
}