package com.calzadosmorales.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.calzadosmorales.entity.*;
import com.calzadosmorales.repository.*;
import com.calzadosmorales.service.EmailService; // 🌟 NUEVO IMPORT

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/ventas")
public class VentaApiController {

    @Autowired private VentaRepository ventaRepository;
    @Autowired private DetalleVentaRepository detalleVentaRepository;
    @Autowired private ProductoTallaRepository productoTallaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EmailService emailService; // 🌟 NUEVA INYECCIÓN

    @PostMapping("/guardar")
    @Transactional(rollbackFor = Exception.class) // 🌟 ATOMICIDAD: Garantiza rollback completo si algo falla en la iteración de stock
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> guardarVenta(@RequestBody Map<String, Object> requestBody) {
        try {
            // 🌟 VALIDACIÓN DE IDEMPOTENCIA: Evita duplicaciones por reintentos de red desde Android
            String codigoSincronizacion = (String) requestBody.get("codigo_sincronizacion");
            if (codigoSincronizacion != null && !codigoSincronizacion.trim().isEmpty()) {
                if (ventaRepository.existsByCodigoSincronizacion(codigoSincronizacion)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Map.of("error", "Esta venta ya fue procesada previamente en la nube."));
                }
            }

            Map<String, Object> clienteJson = (Map<String, Object>) requestBody.get("cliente");
            List<Map<String, Object>> detallesJson = (List<Map<String, Object>>) requestBody.get("detalles");

            if (clienteJson == null || detallesJson == null || detallesJson.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Datos de venta o cliente incompletos"));
            }

            String documento = clienteJson.get("num_documento") != null ? clienteJson.get("num_documento").toString() : null;
            if (documento == null || documento.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El documento del cliente es requerido"));
            }

            Optional<Cliente> clienteExistente = clienteRepository.findByNumDocumento(documento);
            Cliente clienteParaVenta;

            if (clienteExistente.isPresent()) {
                clienteParaVenta = clienteExistente.get();
            } else {
                if (documento.length() == 8) { // Persona Natural
                    PersonaNatural natural = new PersonaNatural();
                    natural.setNombre(clienteJson.get("nombre").toString());
                    natural.setDni(documento); 
                    
                    String apellidoJson = clienteJson.get("apellido") != null ? clienteJson.get("apellido").toString() : "S/A";
                    natural.setApellido(apellidoJson);
                    
                    Integer generoId = 1; 
                    if (clienteJson.get("genero") != null) {
                        try {
                            generoId = Integer.parseInt(clienteJson.get("genero").toString());
                        } catch (NumberFormatException e) {
                            if ("M".equalsIgnoreCase(clienteJson.get("genero").toString())) generoId = 1;
                            else if ("F".equalsIgnoreCase(clienteJson.get("genero").toString())) generoId = 2;
                        }
                    }
                    natural.setGenero(generoId);
                    
                    natural.setDireccion(clienteJson.get("direccion") != null ? clienteJson.get("direccion").toString() : "Dirección Campo");
                    natural.setTelefono(clienteJson.get("telefono") != null ? clienteJson.get("telefono").toString() : "999999999");
                    natural.setEmail(clienteJson.get("email") != null ? clienteJson.get("email").toString() : "natural" + documento + "@morales.com");
                    natural.setEstado(true);
                    
                    clienteParaVenta = clienteRepository.save(natural);
                } else { // Persona Jurídica
                    PersonaJuridica juridica = new PersonaJuridica();
                    juridica.setRazonSocial(clienteJson.get("nombre").toString()); 
                    juridica.setRuc(documento);
                    
                    String repreLegalJson = clienteJson.get("repre_legal") != null ? clienteJson.get("repre_legal").toString() : "S/R";
                    juridica.setRepreLegal(repreLegalJson);
                    
                    juridica.setDireccion(clienteJson.get("direccion") != null ? clienteJson.get("direccion").toString() : "Dirección Empresa");
                    juridica.setTelefono(clienteJson.get("telefono") != null ? clienteJson.get("telefono").toString() : "999999999");
                    juridica.setEmail(clienteJson.get("email") != null ? clienteJson.get("email").toString() : "juridica" + documento + "@morales.com");
                    juridica.setEstado(true);
                    
                    clienteParaVenta = clienteRepository.save(juridica);
                }
            }

            // 1. Crear la Cabecera de la Venta
            Venta nuevaVenta = new Venta();
            nuevaVenta.setFecha(LocalDateTime.now());
            nuevaVenta.setEstado("REGISTRADA");
            
            String tipoComp = requestBody.get("tipo_comprobante") != null ? requestBody.get("tipo_comprobante").toString() : "Boleta";
            String metPago = requestBody.get("metodo_pago") != null ? requestBody.get("metodo_pago").toString() : "Efectivo";
            
            String serieMovil = "Factura".equalsIgnoreCase(tipoComp) ? "MF01" : "MB01";
            
            Integer ultimoCorrelativo = ventaRepository.findMaxCorrelativoBySerie(serieMovil);
            int siguienteCorrelativo = (ultimoCorrelativo != null) ? ultimoCorrelativo + 1 : 1;
            String numeroComprobantePuro = String.format("%06d", siguienteCorrelativo);
            
            nuevaVenta.setNumero(numeroComprobantePuro); 
            nuevaVenta.setSerie(serieMovil);
            nuevaVenta.setCliente(clienteParaVenta); 

            nuevaVenta.setOrigen("ANDROID"); 
            nuevaVenta.setCodigoSincronizacion(codigoSincronizacion);
            nuevaVenta.setTipoComprobante(tipoComp);
            nuevaVenta.setMetodoPago(metPago);

            Integer idUsuarioRequest = 2; // Fallback seguro (Vendedor)
            if (requestBody.get("id_usuario") != null) {
                try {
                    idUsuarioRequest = Integer.parseInt(requestBody.get("id_usuario").toString());
                } catch (NumberFormatException ignored) {}
            }
            
            Usuario usuario = usuarioRepository.findById(idUsuarioRequest).orElse(null);
            if (usuario == null) {
                List<Usuario> usuarios = usuarioRepository.findAll();
                if (!usuarios.isEmpty()) {
                    usuario = usuarios.get(0);
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "No existen usuarios registrados en el sistema de caja."));
                }
            }
            nuevaVenta.setUsuario(usuario);

            BigDecimal totalVenta = BigDecimal.ZERO;
            for (Map<String, Object> det : detallesJson) {
                int cantidad = (Integer) det.get("cantidad");
                BigDecimal precio = new BigDecimal(det.get("precio").toString());
                totalVenta = totalVenta.add(precio.multiply(new BigDecimal(cantidad)));
            }
            nuevaVenta.setTotal(totalVenta);

            // Guardamos la cabecera en la base de datos central
            Venta ventaGuardada = ventaRepository.save(nuevaVenta);

            // 2. Detalles de Venta y Ajuste de Stock
            for (Map<String, Object> det : detallesJson) {
                Integer idProducto = (Integer) det.get("id_producto");
                Integer idTalla = (Integer) det.get("id_talla");
                int cantidad = (Integer) det.get("cantidad");
                BigDecimal precio = new BigDecimal(det.get("precio").toString());

                ProductoTallaKey llaveCompuesta = new ProductoTallaKey(idProducto, idTalla);
                Optional<ProductoTalla> optTalla = productoTallaRepository.findById(llaveCompuesta);

                if (optTalla.isPresent()) {
                    ProductoTalla prodTalla = optTalla.get();

                    if (prodTalla.getStock() < cantidad) {
                        throw new RuntimeException("Stock insuficiente para el producto ID: " + idProducto);
                    }

                    DetalleVenta detalle = new DetalleVenta();
                    detalle.setVenta(ventaGuardada);
                    detalle.setProductoTalla(prodTalla);
                    detalle.setCantidad(cantidad);
                    detalle.setPrecio(precio);
                    detalle.setSubtotal(precio.multiply(new BigDecimal(cantidad)));
                    
                    detalleVentaRepository.save(detalle);

                    prodTalla.setStock(Math.max(prodTalla.getStock() - cantidad, 0));
                    productoTallaRepository.save(prodTalla);
                }
            }

            // 🌟 CONTROL OMNICANAL ASÍNCRONO: Dispara el hilo secundario de email en background
            // Extrae el correo real del cliente ingresado en la transacción de Android
            String emailCliente = clienteParaVenta.getEmail();
            emailService.enviarComprobanteCorreo(ventaGuardada, emailCliente);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "¡Venta guardada, stock actualizado y comprobante enviado con éxito!",
                "numero_boleta", serieMovil + "-" + ventaGuardada.getNumero()
            ));

        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error transaccional en la API: " + e.getMessage()));
        }
    }

    @GetMapping("/listar")
    public ResponseEntity<?> listarVentas() {
        try {
            List<Venta> listaCompleta = ventaRepository.findAll();
            List<Map<String, Object>> respuestaLimpia = new ArrayList<>();

            for (Venta v : listaCompleta) {
                Map<String, Object> map = new HashMap<>();
                map.put("id_venta", v.getId_venta());
                
                String clienteNombre = "Cliente General";
                if (v.getCliente() != null) {
                    clienteNombre = "Cliente Código: " + v.getCliente().getId_cliente();
                }
                
                map.put("cliente_nombre", clienteNombre);
                map.put("monto_total", v.getTotal());
                map.put("fecha_registro", v.getFecha().toString().replace("T", " "));
                map.put("numero_boleta", v.getSerie() != null && v.getNumero() != null ? v.getSerie() + "-" + v.getNumero() : "BOL-000");
                respuestaLimpia.add(map);
            }

            return ResponseEntity.ok(respuestaLimpia);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error: " + e.getMessage()));
        }
    }
}