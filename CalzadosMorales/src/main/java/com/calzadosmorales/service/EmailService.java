package com.calzadosmorales.service;

import com.calzadosmorales.entity.Venta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

@Service
public class EmailService {

    @Autowired
    private PdfService pdfService;

    // 🌟 PEGA AQUÍ TU API KEY DE RESEND
    private static final String RESEND_API_KEY = "re_aUJkAEJf_Ky1KxTnoYKY3nEfVHZN26FcE";

    @Async
    public void enviarComprobanteCorreo(Venta venta, String correoDestino) {
        // En el plan gratuito de Resend, solo puedes enviar correos a tu propio correo de registro
        // Para tu presentación, asegúrate de registrar en Android tu mismo correo con el que creaste Resend
        if (correoDestino == null || correoDestino.trim().isEmpty()) {
            System.out.println("LOG: Correo vacío. Se cancela el envío.");
            return;
        }

        try {
            // 1. Conseguimos el PDF en bytes desde tu Jasper
            byte[] pdfBytes = pdfService.obtenerVentaPDFBytes(venta);
            if (pdfBytes == null || pdfBytes.length == 0) {
                System.err.println("❌ Error: El PDF se generó vacío.");
                return;
            }

            // 2. Convertimos el PDF a Base64 (Requisito de las APIs HTTP para adjuntar archivos)
            String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);
            String nombreArchivo = venta.getTipoComprobante() + "_" + venta.getSerie() + "_" + venta.getNumero() + ".pdf";

            // 3. Estructuramos el JSON exacto que pide Resend para enviar correos con adjuntos
            String jsonPayload = "{"
                + "\"from\": \"Calzados Morales <onboarding@resend.dev>\"," // Remitente por defecto gratuito
                + "\"to\": [\"" + correoDestino + "\"],"
                + "\"subject\": \"👟 Tu Comprobante de Compra - Calzados Morales (" + venta.getSerie() + "-" + venta.getNumero() + ")\","
                + "\"html\": \"<h3>¡Gracias por tu compra en Calzados Morales!</h3><p>Adjunto encontrarás tu comprobante digital en formato PDF.</p>\","
                + "\"attachments\": ["
                + "  {"
                + "    \"content\": \"" + pdfBase64 + "\","
                + "    \"filename\": \"" + nombreArchivo + "\""
                + "  }"
                + "]"
                + "}";

            // 4. Construimos la petición HTTP POST hacia los servidores de Resend (Puerto 443 - HTTPS Seguro)
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + RESEND_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            // 5. Enviamos de forma asíncrona en el backend
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            System.out.println("✅ RESEND API SUCCESS: ¡Correo enviado con éxito en Railway!");
                        } else {
                            System.err.println("❌ RESEND API ERROR: Código de respuesta " + response.statusCode() + " - " + response.body());
                        }
                    });

        } catch (Exception e) {
            System.err.println("❌ ERROR EN EL SERVICIO HTTP DE RESEND: " + e.getMessage());
        }
    }
}