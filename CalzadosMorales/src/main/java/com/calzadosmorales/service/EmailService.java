package com.calzadosmorales.service;

import com.calzadosmorales.entity.Venta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private PdfService pdfService;

    
    private static final String MAILERSEND_API_KEY = "mlsn.7a5a2b5e87ad2993b04eca118a5f372668dac79e51c2c31aeb85024180dacc3c";
    private static final String MAILERSEND_FROM_EMAIL = "ventas@test-68zxl27ook54j905.mlsender.net"; 
    private static final String MAILERSEND_FROM_NAME = "Calzados Morales";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Async
    public void enviarComprobanteCorreo(Venta venta, String correoDestino) {
        if (correoDestino == null || correoDestino.trim().isEmpty() || correoDestino.contains("morales.com")) {
            System.out.println("LOG: Correo genérico o vacío detectado. Se omite el envío.");
            return;
        }

        try {
            
            byte[] pdfBytes = pdfService.obtenerVentaPDFBytes(venta);
            if (pdfBytes == null || pdfBytes.length == 0) {
                System.err.println("❌ Error: El PDF se generó vacío.");
                return;
            }

         
            String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);
            String nombreArchivo = venta.getTipoComprobante() + "_" + venta.getSerie() + "_" + venta.getNumero() + ".pdf";

           
            String clienteNombre = venta.getCliente() != null ? "Cliente" : "Cliente General";
            String asunto = "👟 Tu Comprobante de Compra - Calzados Morales (" + venta.getSerie() + "-" + venta.getNumero() + ")";
            
            String cuerpoHtml = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto;'>"
                    + "<h2 style='color: #2c3e50;'>¡Gracias por tu compra!</h2>"
                    + "<p>Adjunto encontrarás tu <strong>" + venta.getTipoComprobante() + "</strong> digital en formato PDF.</p>"
                    + "<p>Total pagado: <strong>S/ " + venta.getTotal() + "</strong></p>"
                    + "<hr/><p style='font-size: 12px; color: #7f8c8d;'>Calzados Morales &bull; Lima, Perú</p>"
                    + "</div>";

           
            Map<String, Object> jsonMap = Map.of(
                "from", Map.of("email", MAILERSEND_FROM_EMAIL, "name", MAILERSEND_FROM_NAME),
                "to", List.of(Map.of("email", correoDestino, "name", clienteNombre)),
                "subject", asunto,
                "html", cuerpoHtml,
                "attachments", List.of(
                    Map.of(
                        "content", pdfBase64,
                        "filename", nombreArchivo,
                        "type", "application/pdf",
                        "disposition", "attachment"
                    )
                )
            );

            String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(jsonMap);

            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mailersend.com/v1/email"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + MAILERSEND_API_KEY)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(20))
                    .build();

           
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 202 || response.statusCode() == 200) {
                            System.out.println("✅ MAILERSEND SUCCESS: ¡Comprobante enviado libremente a: " + correoDestino);
                        } else {
                            System.err.println("❌ MAILERSEND ERROR HTTP " + response.statusCode() + " - " + response.body());
                        }
                    });

        } catch (Exception e) {
            System.err.println("❌ ERROR CRÍTICO EN INFRAESTRUCTURA MAILERSEND: " + e.getMessage());
        }
    }
}