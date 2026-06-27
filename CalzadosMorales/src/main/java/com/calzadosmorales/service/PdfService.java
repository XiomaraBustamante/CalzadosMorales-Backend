package com.calzadosmorales.service;

import com.calzadosmorales.entity.*;
import jakarta.servlet.http.HttpServletResponse;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    
    private static final Color COLOR_PRIMARIO       = new Color(30, 58, 95);    
    private static final Color COLOR_ACENTO         = new Color(212, 160, 23); 
    private static final Color COLOR_HEADER_TABLA  = new Color(236, 240, 245); 
    private static final Color COLOR_FILA_PAR      = new Color(250, 251, 253);
    private static final Color COLOR_BORDE_RECUADRO= new Color(30, 58, 95);    
    private static final Color COLOR_TEXTO_SUAVE   = new Color(100, 110, 125); 

 
    private static final Font FONT_EMPRESA_NOMBRE  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, COLOR_PRIMARIO);
    private static final Font FONT_EMPRESA_DATOS   = FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_TEXTO_SUAVE);
    private static final Font FONT_COMP_TIPO       = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_PRIMARIO);
    private static final Font FONT_COMP_NUMERO     = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, COLOR_ACENTO);
    private static final Font FONT_LABEL           = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, COLOR_TEXTO_SUAVE);
    private static final Font FONT_VALOR           = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
    private static final Font FONT_TABLA_HEADER    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_PRIMARIO);
    private static final Font FONT_TABLA_BODY      = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
    private static final Font FONT_TOTAL_LABEL     = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_TEXTO_SUAVE);
    private static final Font FONT_TOTAL_VALOR     = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
    private static final Font FONT_GRAND_TOTAL_LBL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_PRIMARIO);
    private static final Font FONT_GRAND_TOTAL_VAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARIO);
    private static final Font FONT_PIE             = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, COLOR_TEXTO_SUAVE);

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
            
            System.setProperty("java.awt.headless", "true");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 40, 40, 40, 50);
            PdfWriter.getInstance(document, byteArrayOutputStream);
            document.open();

            
            String tipoComp = venta.getTipoComprobante() != null ? venta.getTipoComprobante().toUpperCase() : "COMPROBANTE";
            String serie   = venta.getSerie() != null ? venta.getSerie() : "B001";
            String numero  = venta.getNumero() != null ? venta.getNumero() : "000001";
            String nroComp = serie + "-" + numero;

            PdfPTable tablaEncabezado = new PdfPTable(2);
            tablaEncabezado.setWidthPercentage(100);
            tablaEncabezado.setWidths(new float[]{60f, 40f});
            tablaEncabezado.setSpacingAfter(4f);

            
            PdfPCell celdaEmpresa = new PdfPCell();
            celdaEmpresa.setBorder(Rectangle.NO_BORDER);
            celdaEmpresa.setPaddingBottom(6f);

            Paragraph pNombreEmpresa = new Paragraph("CALZADOS MORALES", FONT_EMPRESA_NOMBRE);
            pNombreEmpresa.setSpacingAfter(3f);
            celdaEmpresa.addElement(pNombreEmpresa);
            celdaEmpresa.addElement(new Paragraph("RUC: 20550371082", FONT_EMPRESA_DATOS));
            celdaEmpresa.addElement(new Paragraph("Jirón Junín 847, Interior 170, Centro de Lima – Lima", FONT_EMPRESA_DATOS));
            celdaEmpresa.addElement(new Paragraph("Tel: 943 291 489  |  contacto@calzadosmorales.com", FONT_EMPRESA_DATOS));
            tablaEncabezado.addCell(celdaEmpresa);

           
            PdfPCell celdaComp = new PdfPCell();
            celdaComp.setBorderColor(COLOR_BORDE_RECUADRO);
            celdaComp.setBorderWidth(1.5f);
            celdaComp.setPadding(10f);
            celdaComp.setHorizontalAlignment(Element.ALIGN_CENTER);

            Paragraph pTipoComp = new Paragraph(tipoComp, FONT_COMP_TIPO);
            pTipoComp.setAlignment(Element.ALIGN_CENTER);
            celdaComp.addElement(pTipoComp);

          
            PdfPTable lineaInterna = new PdfPTable(1);
            lineaInterna.setWidthPercentage(80);
            PdfPCell lineaCell = new PdfPCell();
            lineaCell.setBorderWidthBottom(1f);
            lineaCell.setBorderColorBottom(COLOR_ACENTO);
            lineaCell.setBorderWidthTop(0); lineaCell.setBorderWidthLeft(0); lineaCell.setBorderWidthRight(0);
            lineaCell.setFixedHeight(4f);
            lineaInterna.addCell(lineaCell);
            celdaComp.addElement(lineaInterna);

            Paragraph pNroComp = new Paragraph(nroComp, FONT_COMP_NUMERO);
            pNroComp.setAlignment(Element.ALIGN_CENTER);
            pNroComp.setSpacingBefore(4f);
            celdaComp.addElement(pNroComp);

            tablaEncabezado.addCell(celdaComp);
            document.add(tablaEncabezado);

            document.add(crearLineaDivisoria(COLOR_PRIMARIO, 1.5f));
            document.add(new Paragraph(" "));

            
            String nombreCliente = "Cliente General";
            String docCliente    = "S/D";
            String labelDoc      = "DNI";

            if (venta.getCliente() != null) {
                if (venta.getCliente() instanceof PersonaNatural) {
                    PersonaNatural pn = (PersonaNatural) venta.getCliente();
                    nombreCliente = pn.getNombre() + " " + pn.getApellido();
                    docCliente    = pn.getDni();
                    labelDoc      = "DNI";
                } else if (venta.getCliente() instanceof PersonaJuridica) {
                    PersonaJuridica pj = (PersonaJuridica) venta.getCliente();
                    nombreCliente = pj.getRazonSocial();
                    docCliente    = pj.getRuc();
                    labelDoc      = "RUC";
                }
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String fechaFormateada = venta.getFecha() != null ? venta.getFecha().format(formatter) : "—";
            String vendedor        = venta.getUsuario() != null ? venta.getUsuario().getNombre() : "S/V";
            String metodoPago      = venta.getMetodoPago() != null ? venta.getMetodoPago() : "Efectivo";

            PdfPTable tablaCliente = new PdfPTable(4);
            tablaCliente.setWidthPercentage(100);
            tablaCliente.setWidths(new float[]{18f, 40f, 18f, 24f});
            tablaCliente.setSpacingAfter(6f);

            agregarParDatos(tablaCliente, "CLIENTE",        nombreCliente);
            agregarParDatos(tablaCliente, "FECHA EMISIÓN",  fechaFormateada);
            agregarParDatos(tablaCliente, labelDoc,         docCliente);
            agregarParDatos(tablaCliente, "VENDEDOR",       vendedor);
            agregarParDatos(tablaCliente, "MÉTODO PAGO",    metodoPago);
            agregarParDatos(tablaCliente, "", ""); 

            document.add(tablaCliente);

            document.add(crearLineaDivisoria(COLOR_ACENTO, 0.8f));
            document.add(new Paragraph(" "));

            
            PdfPTable tablaItems = new PdfPTable(4);
            tablaItems.setWidthPercentage(100);
            tablaItems.setWidths(new float[]{8f, 52f, 18f, 22f});
            tablaItems.setSpacingAfter(8f);
            tablaItems.setHeaderRows(1);

            tablaItems.addCell(crearCeldaHeader("CANT.", Element.ALIGN_CENTER));
            tablaItems.addCell(crearCeldaHeader("DESCRIPCIÓN DEL MODELO", Element.ALIGN_LEFT));
            tablaItems.addCell(crearCeldaHeader("P. UNIT.", Element.ALIGN_RIGHT));
            tablaItems.addCell(crearCeldaHeader("SUBTOTAL", Element.ALIGN_RIGHT));

            if (venta.getDetalles() != null) {
                int filaIdx = 0;
                for (DetalleVenta d : venta.getDetalles()) {
                    Color bgFila = (filaIdx % 2 == 0) ? Color.WHITE : COLOR_FILA_PAR;

                    PdfPCell cCant = crearCeldaBody(String.valueOf(d.getCantidad()), Element.ALIGN_CENTER, bgFila);
                    
                    String desc = d.getProductoTalla().getProducto().getNombre()
                            + " — Talla: " + d.getProductoTalla().getTalla().getNombre();
                    PdfPCell cDesc    = crearCeldaBody(desc, Element.ALIGN_LEFT, bgFila);
                    
                  
                    PdfPCell cPrecio  = crearCeldaBody("S/ " + d.getPrecio().setScale(2, RoundingMode.HALF_UP).toString(), Element.ALIGN_RIGHT, bgFila);
                    PdfPCell cSubtot  = crearCeldaBody("S/ " + d.getSubtotal().setScale(2, RoundingMode.HALF_UP).toString(), Element.ALIGN_RIGHT, bgFila);

                    tablaItems.addCell(cCant);
                    tablaItems.addCell(cDesc);
                    tablaItems.addCell(cPrecio);
                    tablaItems.addCell(cSubtot);
                    filaIdx++;
                }
            }
            document.add(tablaItems);

            document.add(crearLineaDivisoria(COLOR_PRIMARIO, 0.5f));

          
            BigDecimal totalVenta = venta.getTotal() != null ? venta.getTotal() : BigDecimal.ZERO;
            BigDecimal gravada    = totalVenta.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP);
            BigDecimal igv        = totalVenta.subtract(gravada).setScale(2, RoundingMode.HALF_UP);

            PdfPTable tablaTotales = new PdfPTable(2);
            tablaTotales.setWidthPercentage(45);
            tablaTotales.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tablaTotales.setWidths(new float[]{55f, 45f});
            tablaTotales.setSpacingBefore(6f);
            tablaTotales.setSpacingAfter(16f);

            agregarFilaTotalNormal(tablaTotales, "Op. Gravada",  "S/ " + gravada.toPlainString());
            agregarFilaTotalNormal(tablaTotales, "I.G.V. (18%)", "S/ " + igv.toPlainString());

            PdfPCell sepL = new PdfPCell(); sepL.setBorderWidthTop(1f); sepL.setBorderColorTop(COLOR_PRIMARIO);
            sepL.setBorderWidthBottom(0); sepL.setBorderWidthLeft(0); sepL.setBorderWidthRight(0); sepL.setFixedHeight(5f);
            PdfPCell sepR = new PdfPCell(); sepR.setBorderWidthTop(1f); sepR.setBorderColorTop(COLOR_PRIMARIO);
            sepR.setBorderWidthBottom(0); sepR.setBorderWidthLeft(0); sepR.setBorderWidthRight(0); sepR.setFixedHeight(5f);
            tablaTotales.addCell(sepL);
            tablaTotales.addCell(sepR);

            PdfPCell cGtLabel = new PdfPCell(new Phrase("TOTAL GENERAL", FONT_GRAND_TOTAL_LBL));
            cGtLabel.setBorder(Rectangle.NO_BORDER);
            cGtLabel.setHorizontalAlignment(Element.ALIGN_LEFT);

            PdfPCell cGtValor = new PdfPCell(new Phrase("S/ " + totalVenta.setScale(2, RoundingMode.HALF_UP).toString(), FONT_GRAND_TOTAL_VAL));
            cGtValor.setBorder(Rectangle.NO_BORDER);
            cGtValor.setHorizontalAlignment(Element.ALIGN_RIGHT);

            tablaTotales.addCell(cGtLabel);
            tablaTotales.addCell(cGtValor);
            document.add(tablaTotales);

           
            document.add(crearLineaDivisoria(COLOR_ACENTO, 0.6f));

            Paragraph piePag = new Paragraph("Gracias por confiar en Calzados Morales. ¡Camina con estilo!", FONT_PIE);
            piePag.setAlignment(Element.ALIGN_CENTER);
            piePag.setSpacingBefore(8f);
            document.add(piePag);

            document.close();
            return byteArrayOutputStream.toByteArray();

        } catch (Exception e) {
            System.err.println("ERROR AL GENERAR COMPROBANTE EMITIDO: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

   
    private PdfPTable crearLineaDivisoria(Color color, float grosor) throws DocumentException {
        PdfPTable linea = new PdfPTable(1);
        linea.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBorderWidthBottom(grosor);
        cell.setBorderColorBottom(color);
        cell.setBorderWidthTop(0); cell.setBorderWidthLeft(0); cell.setBorderWidthRight(0);
        cell.setFixedHeight(1f);
        linea.addCell(cell);
        return linea;
    }

    private void agregarParDatos(PdfPTable tabla, String label, String valor) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, FONT_LABEL));
        cLabel.setBorder(Rectangle.NO_BORDER);
        cLabel.setPaddingBottom(4f);

        PdfPCell cValor = new PdfPCell(new Phrase(valor, FONT_VALOR));
        cValor.setBorder(Rectangle.NO_BORDER);
        cValor.setPaddingBottom(4f);

        tabla.addCell(cLabel);
        tabla.addCell(cValor);
    }

    private PdfPCell crearCeldaHeader(String texto, int alineacion) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_TABLA_HEADER));
        cell.setBackgroundColor(COLOR_HEADER_TABLA);
        cell.setHorizontalAlignment(alineacion);
        cell.setPadding(6f);
        cell.setBorderColor(new Color(200, 210, 220));
        cell.setBorderWidth(0.5f);
        return cell;
    }

    private PdfPCell crearCeldaBody(String texto, int alineacion, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_TABLA_BODY));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(alineacion);
        cell.setPadding(5f);
        cell.setBorderColor(new Color(220, 225, 232));
        cell.setBorderWidth(0.3f);
        return cell;
    }

    private void agregarFilaTotalNormal(PdfPTable tabla, String label, String valor) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, FONT_TOTAL_LABEL));
        cLabel.setBorder(Rectangle.NO_BORDER);
        cLabel.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell cValor = new PdfPCell(new Phrase(valor, FONT_TOTAL_VALOR));
        cValor.setBorder(Rectangle.NO_BORDER);
        cValor.setHorizontalAlignment(Element.ALIGN_RIGHT);

        tabla.addCell(cLabel);
        tabla.addCell(cValor);
    }
}