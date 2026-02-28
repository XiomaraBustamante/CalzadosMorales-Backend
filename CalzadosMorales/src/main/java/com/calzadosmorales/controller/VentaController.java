package com.calzadosmorales.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // IMPORTANTE
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.calzadosmorales.entity.Cliente;
import com.calzadosmorales.entity.PersonaJuridica;
import com.calzadosmorales.entity.DetalleVenta;
import com.calzadosmorales.entity.Producto;
import com.calzadosmorales.entity.Usuario;
import com.calzadosmorales.entity.Venta;
import com.calzadosmorales.repository.UsuarioRepository; // IMPORTANTE
import com.calzadosmorales.service.ClienteService;
import com.calzadosmorales.service.ExcelService;
import com.calzadosmorales.service.PdfService;
import com.calzadosmorales.service.ProductoService;
import com.calzadosmorales.service.VentaService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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
    private ExcelService excelService; 

    // PANTALLA PRINCIPAL
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

    // AGREGAR AL CARRITO
    @PostMapping("/agregar")
    public String agregarProducto(
            @RequestParam("id_producto") Integer idProducto,
            @RequestParam("cantidad") Integer cantidad,
            HttpSession session,
            RedirectAttributes flash) {

        if (cantidad == null || cantidad <= 0) {
            flash.addFlashAttribute("error", "La cantidad debe ser mayor a 0.");
            return "redirect:/ventas/nueva";
        }

        Producto producto = productoService.buscarProducto(idProducto);
        if (producto.getStock() < cantidad) {
            flash.addFlashAttribute("error", "Stock insuficiente.");
            return "redirect:/ventas/nueva";
        }

        List<DetalleVenta> carrito = (List<DetalleVenta>) session.getAttribute("carrito");
        if (carrito == null) carrito = new ArrayList<>();

        boolean existe = false;
        for (DetalleVenta det : carrito) {
            if (det.getProducto().getId_producto().equals(idProducto)) {
                det.setCantidad(det.getCantidad() + cantidad);
                det.setSubtotal(producto.getPrecio().multiply(new BigDecimal(det.getCantidad())));
                existe = true;
                break;
            }
        }

        if (!existe) {
            DetalleVenta detalle = new DetalleVenta();
            detalle.setCantidad(cantidad);
            detalle.setProducto(producto);
            detalle.setPrecio(producto.getPrecio());
            detalle.setSubtotal(producto.getPrecio().multiply(new BigDecimal(cantidad)));
            carrito.add(detalle);
        }
        
        session.setAttribute("carrito", carrito);
        flash.addFlashAttribute("success", "Producto Agregado");
        return "redirect:/ventas/nueva";
    }

    //LIMPIAR
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
            @RequestParam(name = "generarPdf", defaultValue = "true") boolean generarPdf, // <-- NUEVO PARÁMETRO
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

            if (clienteReal instanceof PersonaJuridica) {
                venta.setTipoComprobante("Factura");
                venta.setSerie("F001");
            } else {
                venta.setTipoComprobante("Boleta");
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
            
            ventaService.registrarVenta(venta);
            venta.setNumero(String.format("%06d", venta.getId_venta()));
            ventaService.registrarVenta(venta); 
            
            session.removeAttribute("carrito");
            
            // --- LÓGICA DE DECISIÓN ---
            if (generarPdf) {
                return "redirect:/ventas/verPDF/" + venta.getId_venta();
            } else {
                flash.addFlashAttribute("success", "Venta registrada exitosamente (Sin comprobante).");
                // Si es vendedor va a sus ventas, si no a nueva venta
                return "redirect:/ventas/nueva"; 
            }
            
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al procesar la venta: " + e.getMessage());
            return "redirect:/ventas/nueva";
        }
    }
    
    // GENERAR PDF

    @GetMapping("/verPDF/{id}")
    public void verPDF(@PathVariable("id") Integer idVenta, HttpServletResponse response) {
        try {
            Venta venta = ventaService.buscarPorId(idVenta); 
            if (venta != null) {
                pdfService.exportarVentaPDF(response, venta);
            }
        } catch (Exception e) {
            System.err.println("Error al visualizar el PDF: " + e.getMessage());
        }
    }
    

    // NUEVAS RUTAS: CONSULTAS Y REPORTES
 

 // --- PARA EL VENDEDOR ---

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
    // --- PARA EL ADMINISTRADOR ---

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
        
        // Verificamos si se han enviado fechas (el usuario presionó el botón)
        if (inicio != null && !inicio.isEmpty() && fin != null && !fin.isEmpty()) {
            model.addAttribute("listaVentas", ventaService.obtenerReporteFechas(inicio, fin));
            model.addAttribute("totalSumatoria", ventaService.obtenerSumatoriaRango(inicio, fin));
            
            // Mantener las fechas en los inputs para que el usuario vea qué consultó
            model.addAttribute("fechaInicio", inicio);
            model.addAttribute("fechaFin", fin);
            
            // 2. Agregamos una bandera para indicar que se realizó una búsqueda
            model.addAttribute("busquedaRealizada", true);
        } else {
            // 3. Primera vez que entra: lista nula o vacía y bandera en false
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