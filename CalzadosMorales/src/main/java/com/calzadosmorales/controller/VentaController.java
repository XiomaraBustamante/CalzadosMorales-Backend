package com.calzadosmorales.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional; // 🌟 IMPORTANTE: Añadida la importación para transacciones
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.calzadosmorales.entity.Cliente;
import com.calzadosmorales.entity.PersonaNatural;
import com.calzadosmorales.entity.PersonaJuridica;
import com.calzadosmorales.entity.DetalleVenta;
import com.calzadosmorales.entity.ProductoTalla;
import com.calzadosmorales.entity.ProductoTallaKey;
import com.calzadosmorales.entity.Usuario;
import com.calzadosmorales.entity.Venta;
import com.calzadosmorales.repository.UsuarioRepository;
import com.calzadosmorales.repository.VentaRepository;
import com.calzadosmorales.repository.ProductoTallaRepository;
import com.calzadosmorales.service.ClienteService;
import com.calzadosmorales.service.ExcelService;
import com.calzadosmorales.service.PdfService;
import com.calzadosmorales.service.ProductoService;
import com.calzadosmorales.service.VentaService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private ClienteService clienteService; 
    
    @Autowired
    private PdfService pdfService;

    @Autowired
    private UsuarioRepository usuarioRepo; 
    
    @Autowired
    private VentaRepository ventaRepo;
    
    @Autowired
    private ProductoTallaRepository productoTallaRepo;
    
    @Autowired
    private ExcelService excelService; 


    @GetMapping("/nueva")
    public String nuevaVenta(Model model, HttpSession session) {
        model.addAttribute("productos", productoService.listarProductos());
        model.addAttribute("clientes", clienteService.listarTodos()); 
        
        List<DetalleVenta> carrito = (List<DetalleVenta>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute("carrito", carrito);
        }

        BigDecimal total = carrito.stream()
                .map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("carrito", carrito);
        model.addAttribute("total", total);
        return "nueva_venta"; 
    }


    @PostMapping("/agregar")
    public String agregarProducto(
            @RequestParam("id_producto") Integer idProducto,
            @RequestParam("id_talla") Integer idTalla, 
            @RequestParam("cantidad") Integer cantidad,
            HttpSession session,
            RedirectAttributes flash) {

        if (cantidad == null || cantidad <= 0) {
            flash.addFlashAttribute("error", "La cantidad debe ser mayor a 0.");
            return "redirect:/ventas/nueva";
        }

        ProductoTallaKey key = new ProductoTallaKey(idProducto, idTalla);
        ProductoTalla pt = productoTallaRepo.findById(key).orElse(null);

        if (pt == null) {
            flash.addFlashAttribute("error", "La variante de talla seleccionada no existe.");
            return "redirect:/ventas/nueva";
        }

        if (pt.getStock() < cantidad) {
            flash.addFlashAttribute("error", "Stock insuficiente para la talla seleccionada. Disponible: " + pt.getStock());
            return "redirect:/ventas/nueva";
        }

        List<DetalleVenta> carrito = (List<DetalleVenta>) session.getAttribute("carrito");
        if (carrito == null) carrito = new ArrayList<>();

        boolean existe = false;
        for (DetalleVenta det : carrito) {
            if (det.getProductoTalla().getId().getId_producto().equals(idProducto) && 
                det.getProductoTalla().getId().getId_talla().equals(idTalla)) {
                
                if ((det.getCantidad() + cantidad) > pt.getStock()) {
                    flash.addFlashAttribute("error", "No puede agregar más del stock disponible total.");
                    return "redirect:/ventas/nueva";
                }
                
                det.setCantidad(det.getCantidad() + cantidad);
                det.setSubtotal(pt.getProducto().getPrecio().multiply(new BigDecimal(det.getCantidad())));
                existe = true;
                break;
            }
        }

        if (!existe) {
            DetalleVenta detalle = new DetalleVenta();
            detalle.setCantidad(cantidad);
            detalle.setProductoTalla(pt); 
            detalle.setPrecio(pt.getProducto().getPrecio());
            detalle.setSubtotal(pt.getProducto().getPrecio().multiply(new BigDecimal(cantidad)));
            carrito.add(detalle);
        }
        
        session.setAttribute("carrito", carrito);
        flash.addFlashAttribute("success", "Producto Agregado con Talla");
        return "redirect:/ventas/nueva";
    }


    @GetMapping("/quitar/{index}")
    public String quitarDelCarrito(@PathVariable("index") int index, HttpSession session) {
        List<DetalleVenta> carrito = (List<DetalleVenta>) session.getAttribute("carrito");
        if (carrito != null && index < carrito.size()) carrito.remove(index);
        return "redirect:/ventas/nueva";
    }
    
    @GetMapping("/limpiar")
    public String cancelarVenta(HttpSession session) {
        session.removeAttribute("carrito");
        return "redirect:/ventas/nueva";
    }


    @PostMapping("/guardar")
    public String guardarVenta(
            @RequestParam("id_cliente") Integer idCliente, 
            @RequestParam("metodo_pago") String metodoPago, 
            @RequestParam("tipo_comprobante") String tipoComprobante, 
            @RequestParam(name = "generarPdf", defaultValue = "true") boolean generarPdf, 
            HttpSession session, 
            RedirectAttributes flash,
            Authentication auth) { 

        List<DetalleVenta> carrito = (List<DetalleVenta>) session.getAttribute("carrito");
        if (carrito == null || carrito.isEmpty()) {
            flash.addFlashAttribute("error", "El carrito está vacío.");
            return "redirect:/ventas/nueva";
        }

        try {
            Venta venta = new Venta();
            venta.setFecha(LocalDateTime.now());
            
            Cliente clienteReal = clienteService.buscarPorId(idCliente);
            venta.setCliente(clienteReal);

            venta.setTipoComprobante(tipoComprobante);
            venta.setMetodoPago(metodoPago);
            
            venta.setOrigen("WEB");

            if ("Factura".equalsIgnoreCase(tipoComprobante)) {
                venta.setSerie("F001");
            } else {
                venta.setSerie("B001");
            }

            String username = auth.getName();
            Usuario usuarioLogueado = usuarioRepo.findByUsuario(username);
            venta.setUsuario(usuarioLogueado); 
            
            BigDecimal total = carrito.stream()
                    .map(DetalleVenta::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            venta.setTotal(total);
            
            for (DetalleVenta d : carrito) {
                venta.agregarDetalle(d);
            }
            
            Integer ultimoCorrelativo = ventaRepo.findMaxCorrelativoBySerie(venta.getSerie());
            int siguienteCorrelativo = (ultimoCorrelativo != null) ? ultimoCorrelativo + 1 : 1;
            
            venta.setNumero(String.format("%06d", siguienteCorrelativo));
            
            ventaService.registrarVenta(venta); 
            
            session.removeAttribute("carrito");
            
            if (generarPdf) {
                return "redirect:/ventas/verPDF/" + venta.getId_venta();
            }

            return "redirect:/ventas/nueva";

        } catch (Exception e) {
            e.printStackTrace(); 
            flash.addFlashAttribute("error", "Error al procesar la venta: " + e.getMessage());
            return "redirect:/ventas/nueva";
        }
    }

    @GetMapping("/verPDF/{id}")
    @ResponseBody
    @Transactional(readOnly = true) // 🔥 SOLUCIÓN: Mantiene activa la sesión de persistencia para mapear las listas Lazy en la nube
    public void verPDF(@PathVariable("id") Integer idVenta, HttpServletResponse response) {
        try {
            Venta venta = ventaService.buscarPorId(idVenta); 
            if (venta != null) {
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "inline; filename=Comprobante_" + venta.getNumero() + ".pdf");
                
                pdfService.exportarVentaPDF(response, venta);
            }
        } catch (Exception e) {
            System.err.println("Error al visualizar el PDF: " + e.getMessage());
        }
    }


    @GetMapping("/listar")
    @ResponseBody 
    public ResponseEntity<?> listarVentas() {
        try {
            List<Venta> listaCompleta = ventaRepo.findAll(); 
            List<Map<String, Object>> respuestaLimpia = new ArrayList<>();

            for (Venta v : listaCompleta) {
                Map<String, Object> map = new HashMap<>();
                map.put("id_venta", v.getId_venta());
                
                String clienteNombre = "Cliente General";
                if (v.getCliente() != null) {
                    Cliente c = v.getCliente();
                    if (c instanceof PersonaNatural) {
                        PersonaNatural pn = (PersonaNatural) c;
                        clienteNombre = pn.getNombre() + " " + pn.getApellido();
                    } else if (c instanceof PersonaJuridica) {
                        PersonaJuridica pj = (PersonaJuridica) c;
                        clienteNombre = pj.getRazonSocial();
                    }
                }
                
                map.put("cliente_nombre", clienteNombre);
                map.put("monto_total", v.getTotal());
                map.put("fecha_registro", v.getFecha() != null ? v.getFecha().toString().replace("T", " ") : "");
                map.put("numero_boleta", v.getSerie() != null && v.getNumero() != null ? v.getSerie() + "-" + v.getNumero() : "BOL-000");
                respuestaLimpia.add(map);
            }

            return ResponseEntity.ok(respuestaLimpia);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }

    @GetMapping("/mis-ventas")
    @PreAuthorize("hasRole('ROLE_2')")
    public String verMisVentas(Authentication auth, Model model) {
        String username = auth.getName();
        Usuario usuarioLogueado = usuarioRepo.findByUsuario(username);
        if (usuarioLogueado == null) return "redirect:/login";
        model.addAttribute("listaVentas", ventaService.listarMisVentas(usuarioLogueado.getId_usuario()));
        model.addAttribute("totalHoy", ventaService.totalVendidoHoyVendedor(usuarioLogueado.getId_usuario()));
        return "mis_ventas"; 
    }

    @GetMapping("/recuperar-clientes")
    @PreAuthorize("hasRole('ROLE_2')") 
    public String verClientesPorRecuperar(Authentication auth, Model model) {
        String username = auth.getName();
        Usuario usuarioLogueado = usuarioRepo.findByUsuario(username);
        if (usuarioLogueado == null) return "redirect:/login";
        model.addAttribute("listaClientes", ventaService.clientesPorRecuperar(usuarioLogueado.getId_usuario()));
        return "recuperar_clientes"; 
    }

    @GetMapping("/historial-general")
    @PreAuthorize("hasRole('ROLE_1')") 
    public String historialGeneral(Model model) {
        model.addAttribute("listaHistorial", ventaService.obtenerHistorialGeneralAdmin());
        return "historial_general"; 
    }

    @PreAuthorize("hasAnyAuthority('1', 'ROLE_1')")
    @GetMapping("/reporte-fechas")
    public String reportePorFechas(
             @RequestParam(name = "inicio", required = false) String inicio,
             @RequestParam(name = "fin", required = false) String fin,
             Model model) {
        if (inicio != null && !inicio.isEmpty() && fin != null && !fin.isEmpty()) {
            model.addAttribute("listaVentas", ventaService.obtenerReporteFechas(inicio, fin));
            model.addAttribute("totalSumatoria", ventaService.obtenerSumatoriaRango(inicio, fin));
            model.addAttribute("fechaInicio", inicio);
            model.addAttribute("fechaFin", fin);
            model.addAttribute("busquedaRealizada", true);
        } else {
            model.addAttribute("listaVentas", null); 
            model.addAttribute("totalSumatoria", 0.0);
            model.addAttribute("busquedaRealizada", false);
        }
        return "reporte_fechas"; 
    }
    
    @PreAuthorize("hasAnyAuthority('1', 'ROLE_1')") 
    @GetMapping("/analisis-horario")
    public String verAnalisisHorario(Model model) {
        model.addAttribute("listaAnalisis", ventaService.obtenerAnalisisHorario());
        return "analisis_horario"; 
    }
    
    @GetMapping("/exportar-excel")
    @PreAuthorize("hasRole('ROLE_1')")
    public void exportarExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Reporte_Ventas_CalzadosMorales.xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> listaHistorial = ventaService.obtenerHistorialGeneralAdmin();
        excelService.exportarVentasExcel(response, listaHistorial);
    }
}