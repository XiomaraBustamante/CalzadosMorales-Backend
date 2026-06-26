package com.calzadosmorales.service;

import com.calzadosmorales.entity.*;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PdfService {

    public void exportarVentaPDF(HttpServletResponse response, Venta venta) {
        try {
            byte[] pdfBytes = obtenerVentaPDFBytes(venta);
            if (pdfBytes != null && pdfBytes.length > 0) {
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "inline; filename=comprobante_" + venta.getNumero() + ".pdf");
                response.setContentLength(pdfBytes.length);
                response.getOutputStream().write(pdfBytes);
                response.getOutputStream().flush();
            }
        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO AL RENDERIZAR STREAM HTTP: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    public byte[] obtenerVentaPDFBytes(Venta venta) {
        try {
            
            File file = ResourceUtils.getFile("classpath:reports/comprobante.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());

            
            Map<String, Object> parameters = new HashMap<>();
            
            String nombreCliente = "";
            String docCliente = ""; 
            
            if (venta.getCliente() instanceof PersonaNatural) {
                PersonaNatural pn = (PersonaNatural) venta.getCliente();
                nombreCliente = pn.getNombre() + " " + pn.getApellido();
                docCliente = pn.getDni(); 
            } else if (venta.getCliente() instanceof PersonaJuridica) {
                PersonaJuridica pj = (PersonaJuridica) venta.getCliente();
                nombreCliente = pj.getRazonSocial();
                docCliente = pj.getRuc(); 
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String fechaFormateada = venta.getFecha() != null ? venta.getFecha().format(formatter) : "";

            parameters.put("p_cliente", nombreCliente);
            parameters.put("p_documento", docCliente);
            parameters.put("p_vendedor", venta.getUsuario() != null ? venta.getUsuario().getNombre() : "S/V"); 
            parameters.put("p_fecha", fechaFormateada);
            parameters.put("p_titulo", venta.getTipoComprobante() != null ? venta.getTipoComprobante() : "COMPROBANTE");
            
            parameters.put("p_serie", venta.getSerie() != null ? venta.getSerie() : "001");
            parameters.put("p_numero", venta.getNumero() != null ? venta.getNumero() : "00000000");

        
            BigDecimal totalVenta = venta.getTotal() != null ? venta.getTotal() : BigDecimal.ZERO;
            BigDecimal gravada = totalVenta.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP);
            BigDecimal igv = totalVenta.subtract(gravada);

            parameters.put("op_gravada", gravada);
            parameters.put("igv", igv);
            parameters.put("total", totalVenta);

           
            var detalleDS = venta.getDetalles().stream().map(d -> {
                Map<String, Object> map = new HashMap<>();
                map.put("cantidad", d.getCantidad()); 
                
                String descripcionPremium = d.getProductoTalla().getProducto().getNombre() 
                        + " (Talla: " + d.getProductoTalla().getTalla().getNombre() + ")";
                
                map.put("descripcion", descripcionPremium);
                map.put("precio", d.getPrecio());
                map.put("subtotal", d.getSubtotal());
                return map;
            }).collect(Collectors.toList());

            parameters.put("ItemDataSource", new JRBeanCollectionDataSource(detalleDS));

         
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JRBeanCollectionDataSource(detalleDS));
            
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, byteArrayOutputStream);
            
            return byteArrayOutputStream.toByteArray();

        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO AL EXPORTAR BINARIO PDF DESDE SERVICE: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}