package com.calzadosmorales.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.calzadosmorales.entity.*;
import com.calzadosmorales.service.ProductoService;
import com.calzadosmorales.service.CloudinaryService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService service;

    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("productos", service.listarProductos());
        model.addAttribute("categorias", service.listarCategorias());
        model.addAttribute("tallas", service.listarTallas());
        model.addAttribute("colores", service.listarColores());
        model.addAttribute("materiales", service.listarMateriales());
        model.addAttribute("producto", new Producto());
        return "productos";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Producto producto, 
            @RequestParam(name = "archivoFoto1", required = false) MultipartFile archivoFoto1,
            @RequestParam(name = "archivoFoto2", required = false) MultipartFile archivoFoto2,
            @RequestParam(name = "archivoFoto3", required = false) MultipartFile archivoFoto3,
            @RequestParam(name = "eliminarFoto1", defaultValue = "false") boolean eliminarFoto1,
            @RequestParam(name = "eliminarFoto2", defaultValue = "false") boolean eliminarFoto2,
            @RequestParam(name = "eliminarFoto3", defaultValue = "false") boolean eliminarFoto3,
            @RequestParam(name = "tallasId", required = false) List<Integer> tallasId,
            @RequestParam(name = "tallasStock", required = false) List<Integer> tallasStock,
            RedirectAttributes flash) {
        
        if (producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            flash.addFlashAttribute("error", "El precio debe ser mayor a 0.");
            return "redirect:/productos";
        }

        List<ProductoImagen> listaImagenesFinal = new ArrayList<>();
        MultipartFile[] archivos = {archivoFoto1, archivoFoto2, archivoFoto3};
        boolean[] eliminarBanderas = {eliminarFoto1, eliminarFoto2, eliminarFoto3};

        try {
            List<ProductoImagen> imagenesAnteriores = new ArrayList<>();
            Producto actual = null;
            
            if (producto.getId_producto() != null) {
                actual = service.buscarProducto(producto.getId_producto());
                if (actual != null && actual.getImagenes() != null) {
                    List<ProductoImagen> ordenadas = new ArrayList<>(actual.getImagenes());
                    ordenadas.sort((img1, img2) -> Integer.compare(img1.getIdImagen(), img2.getIdImagen()));
                    imagenesAnteriores = ordenadas;
                }
            }

            for (int i = 0; i < 3; i++) {
                MultipartFile archivoNuevo = archivos[i];
                boolean seMarcoEliminar = eliminarBanderas[i];
                ProductoImagen imgPrevia = (imagenesAnteriores.size() > i) ? imagenesAnteriores.get(i) : null;

                if (archivoNuevo != null && !archivoNuevo.isEmpty()) {
                    if (imgPrevia != null) {
                        cloudinaryService.eliminarFoto(imgPrevia.getImagenUrl());
                    }
                    String urlCloudinary = cloudinaryService.subirFoto(archivoNuevo);
                    if (urlCloudinary != null) {
                        listaImagenesFinal.add(new ProductoImagen(urlCloudinary, actual != null ? actual : producto));
                    }
                } else if (seMarcoEliminar) {
                    if (imgPrevia != null) {
                        cloudinaryService.eliminarFoto(imgPrevia.getImagenUrl());
                    }
                } else if (imgPrevia != null) {
                    listaImagenesFinal.add(imgPrevia);
                }
            }

            List<ProductoTalla> listaTallasStock = new ArrayList<>();
            if (tallasId != null && tallasStock != null) {
                for (int i = 0; i < tallasId.size(); i++) {
                    Integer stockVal = tallasStock.get(i);
                    if (stockVal != null && stockVal >= 0) {
                        ProductoTalla prodTalla = new ProductoTalla();
                        prodTalla.setStock(stockVal);
                        
                        // Inicializamos la llave con el ID del producto si existe, sino se mapeará en el Service
                        Integer idProdContexto = (actual != null) ? actual.getId_producto() : producto.getId_producto();
                        ProductoTallaKey key = new ProductoTallaKey(idProdContexto, tallasId.get(i));
                        prodTalla.setId(key);
                        
                        Talla t = new Talla();
                        t.setId_talla(tallasId.get(i));
                        prodTalla.setTalla(t);
                        prodTalla.setProducto(actual != null ? actual : producto);
                        
                        listaTallasStock.add(prodTalla);
                    }
                }
            }

            if (producto.getId_producto() != null && actual != null) {
                actual.getImagenes().clear();
                for (ProductoImagen img : listaImagenesFinal) {
                    img.setProducto(actual);
                    actual.getImagenes().add(img);
                }
                
                actual.setNombre(producto.getNombre());
                actual.setPrecio(producto.getPrecio());
                actual.setDescripcion(producto.getDescripcion());
                actual.setCategoria(producto.getCategoria());
                actual.setColor(producto.getColor());
                actual.setMaterial(producto.getMaterial());
                actual.setTallas(listaTallasStock);
                
                producto = actual;
            } else {
                producto.setImagenes(listaImagenesFinal);
                producto.setTallas(listaTallasStock);
                producto.setEstado(true); 
            }

        } catch (Exception e) {
            System.err.println("Error crítico detectado en el flujo transaccional: " + e.getMessage());
            flash.addFlashAttribute("error", "Hubo un problema al procesar y almacenar la información del producto.");
            return "redirect:/productos";
        }

        service.guardarProducto(producto);
        flash.addFlashAttribute("success", "Producto guardado y sincronizado correctamente.");
        return "redirect:/productos";
    }

    @GetMapping("/cambiarEstado/{id}/{estado}")
    public String cambiarEstado(@PathVariable("id") Integer id, @PathVariable("estado") boolean nuevoEstado, RedirectAttributes flash) {
        Producto p = service.buscarProducto(id);
        if (p != null) {
            p.setEstado(nuevoEstado);
            service.guardarProducto(p);
            flash.addFlashAttribute("info", "Estado del producto actualizado.");
        }
        return "redirect:/productos";
    }
     
    @GetMapping("/consulta-stock")
    public String pantallaConsultaStock(
            @RequestParam(name = "idCat", defaultValue = "0") int idCat,
            @RequestParam(name = "talla", defaultValue = "") String talla,
            Model model) {
        
        model.addAttribute("listaCategorias", service.listarCategorias());
        var resultados = service.consultarStockConFiltros(idCat, talla);
        model.addAttribute("listaResultados", (resultados != null) ? resultados : new java.util.ArrayList<>());
        
        model.addAttribute("idCategoriaSeleccionada", idCat);
        model.addAttribute("tallaFiltro", talla);
        
        return "consultar_stock"; 
    }

    @GetMapping("/estancados")
    @PreAuthorize("hasRole('ROLE_2')") 
    public String verProductosEstancados(Model model) {
        model.addAttribute("listaEstancados", service.obtenerProductosEstancados());
        return "productos_estancados"; 
    }

    @GetMapping("/api-stock/{id}")
    @ResponseBody
    public List<Map<String, Object>> obtenerStockTallasJson(@PathVariable("id") Integer id) {
        List<Map<String, Object>> listaJson = new ArrayList<>();
        Producto p = service.buscarProducto(id);
        if (p != null && p.getTallas() != null) {
            for (ProductoTalla pt : p.getTallas()) {
                Map<String, Object> map = new HashMap<>();
                map.put("idTalla", pt.getTalla().getId_talla());
                map.put("stock", pt.getStock());
                listaJson.add(map);
            }
        }
        return listaJson;
    }
}