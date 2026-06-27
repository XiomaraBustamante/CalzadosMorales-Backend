package com.calzadosmorales.service;

import com.calzadosmorales.entity.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

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
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            
            
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, byteArrayOutputStream);
            
            document.open();

            
            Font fontEmpresa = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Paragraph.ALIGN_CENTER);
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);

           
            Paragraph pEmpresa = new Paragraph("CALZADOS MORALES", fontEmpresa);
            pEmpresa.setAlignment(Element.ALIGN_CENTER);
            document.add(pEmpresa);

            String tipoComp = venta.getTipoComprobante() != null ? venta.getTipoComprobante().toUpperCase() : "COMPROBANTE";
            String nroComp = (venta.getSerie() != null ? venta.getSerie() : "001") + "-" + (venta.getNumero() != null ? venta.getNumero() : "00000000");
            Paragraph pTitulo = new Paragraph(tipoComp + " NRO: " + nroComp, fontTitulo);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(pTitulo);
            
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------", fontNormal));

            
            String nombreCliente = "Cliente General";
            String docCliente = "S/D";
            
            if (venta.getCliente() != null) {
                if (venta.getCliente() instanceof PersonaNatural) {
                    PersonaNatural pn = (PersonaNatural) venta.getCliente();
                    nombreCliente = pn.getNombre() + " " + pn.getApellido();
                    docCliente = pn.getDni();
                } else if (venta.getCliente() instanceof PersonaJuridica) {
                    PersonaJuridica pj = (PersonaJuridica) venta.getCliente();
                    nombreCliente = pj.getRazonSocial();
                    docCliente = pj.getRuc();
                }
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String fechaFormateada = venta.getFecha() != null ? venta.getFecha().format(formatter) : "";

            document.add(new Paragraph("Cliente: " + nombreCliente, fontNormal));
            document.add(new Paragraph("Documento (DNI/RUC): " + docCliente, fontNormal));
            document.add(new Paragraph("Fecha de Emisión: " + fechaFormateada, fontNormal));
            document.add(new Paragraph("Vendedor: " + (venta.getUsuario() != null ? venta.getUsuario().getNombre() : "S/V"), fontNormal));
            document.add(new Paragraph("Método de Pago: " + (venta.getMetodoPago() != null ? venta.getMetodoPago() : "Efectivo"), fontNormal));
            
            document.add(new Paragraph("\n"));

           
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{10f, 55f, 15f, 20f}); 

           
            table.addCell(new PdfPCell(new Phrase("Cant.", fontBold)));
            table.addCell(new PdfPCell(new Phrase("Descripción del Calzado", fontBold)));
            table.addCell(new PdfPCell(new Phrase("P. Unit", fontBold)));
            table.addCell(new PdfPCell(new Phrase("Subtotal", fontBold)));

            
            if (venta.getDetalles() != null) {
                for (DetalleVenta d : venta.getDetalles()) {
                    table.addCell(new PdfPCell(new Phrase(String.valueOf(d.getCantidad()), fontNormal)));
                    
                    String desc = d.getProductoTalla().getProducto().getNombre() 
                            + " (Talla: " + d.getProductoTalla().getTalla().getNombre() + ")";
                    table.addCell(new PdfPCell(new Phrase(desc, fontNormal)));
                    table.addCell(new PdfPCell(new Phrase("S/ " + d.getPrecio(), fontNormal)));
                    table.addCell(new PdfPCell(new Phrase("S/ " + d.getSubtotal(), fontNormal)));
                }
            }
            document.add(table);
            document.add(new Paragraph("\n"));

           
            BigDecimal totalVenta = venta.getTotal() != null ? venta.getTotal() : BigDecimal.ZERO;
            BigDecimal gravada = totalVenta.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP);
            BigDecimal igv = totalVenta.subtract(gravada);

            document.add(new Paragraph("Op. Gravada: S/ " + gravada, fontNormal));
            document.add(new Paragraph("I.G.V. (18%): S/ " + igv, fontNormal));
            
            Paragraph pTotal = new Paragraph("TOTAL GENERAL: S/ " + totalVenta, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            document.add(pTotal);

            document.close();
            return byteArrayOutputStream.toByteArray();

        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO AL EXPORTAR BINARIO PDF DESDE SERVICE OPENPDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}