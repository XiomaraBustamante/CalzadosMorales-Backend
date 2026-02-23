package com.calzadosmorales.service;

import com.calzadosmorales.entity.*;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter; // IMPORTANTE
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PdfService {

    public void exportarVentaPDF(HttpServletResponse response, Venta venta) {
        try {
            // 1. CARGAR DISEÑO
            File file = ResourceUtils.getFile("classpath:reports/comprobante.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());

            // 2. PREPARAR PARÁMETROS
            Map<String, Object> parameters = new HashMap<>();
            
            String nombreCliente = "";
            String docCliente = ""; // Aquí solo mandaremos el número
            
            if (venta.getCliente() instanceof PersonaNatural) {
                PersonaNatural pn = (PersonaNatural) venta.getCliente();
                nombreCliente = pn.getNombre() + " " + pn.getApellido();
                docCliente = pn.getDni(); // Solo el número
            } else if (venta.getCliente() instanceof PersonaJuridica) {
                PersonaJuridica pj = (PersonaJuridica) venta.getCliente();
                nombreCliente = pj.getRazonSocial();
                docCliente = pj.getRuc(); // Solo el número
            }

            // Formatear Fecha (Bonita: 23/02/2026 14:07)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String fechaFormateada = venta.getFecha() != null ? venta.getFecha().format(formatter) : "";

            // Datos de Cabecera
            parameters.put("p_cliente", nombreCliente);
            parameters.put("p_documento", docCliente);
            parameters.put("p_vendedor", venta.getUsuario() != null ? venta.getUsuario().getNombre() : "S/V"); 
            parameters.put("p_fecha", fechaFormateada);
            parameters.put("p_titulo", venta.getTipoComprobante() != null ? venta.getTipoComprobante() : "COMPROBANTE");
            
            parameters.put("p_serie", venta.getSerie() != null ? venta.getSerie() : "001");
            parameters.put("p_numero", venta.getNumero() != null ? venta.getNumero() : "00000000");

            // 3. CÁLCULO DE TOTALES
            BigDecimal totalVenta = venta.getTotal() != null ? venta.getTotal() : BigDecimal.ZERO;
            BigDecimal gravada = totalVenta.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP);
            BigDecimal igv = totalVenta.subtract(gravada);

            parameters.put("op_gravada", gravada);
            parameters.put("igv", igv);
            parameters.put("total", totalVenta);

            // 4. DATOS DE LA TABLA (DETALLE)
            var detalleDS = venta.getDetalles().stream().map(d -> {
                Map<String, Object> map = new HashMap<>();
                map.put("cantidad", d.getCantidad()); 
                map.put("descripcion", d.getProducto().getNombre());
                map.put("precio", d.getPrecio());
                map.put("subtotal", d.getSubtotal());
                return map;
            }).collect(Collectors.toList());

            parameters.put("ItemDataSource", new JRBeanCollectionDataSource(detalleDS));

            // 5. GENERAR PDF
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
            
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=boleta_" + venta.getNumero() + ".pdf");
            
            JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());

        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO AL GENERAR PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}