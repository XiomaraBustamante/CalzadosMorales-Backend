package com.calzadosmorales.controller.api;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.calzadosmorales.entity.Producto;
import com.calzadosmorales.service.ProductoService; 

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*") 
public class ProductoApiController {

    @Autowired
    private ProductoService productoService; 

   
    @GetMapping(value = "/listar", produces = "application/json")
    public ResponseEntity<List<Producto>> listarProductosParaMovil() {
        try {
            List<Producto> todosLosProductos = productoService.listarProductos(); 
            
            if (todosLosProductos.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            
            List<Producto> listaFiltrada = todosLosProductos.stream()
                    .filter(prod -> prod.getEstado() != null && prod.getEstado())
                    .collect(Collectors.toList());
            
            if (listaFiltrada.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            
           
            listaFiltrada.forEach(prod -> {
                if (prod.getImagenes() != null) {
                    prod.getImagenes().forEach(img -> img.setProducto(null));
                }
                if (prod.getTallas() != null) {
                    prod.getTallas().forEach(t -> t.setProducto(null));
                }
            });
            
            return ResponseEntity.ok(listaFiltrada);
            
        } catch (Exception e) {
            System.err.println("Error crítico en API de catálogo móvil: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping(value = "/api-detalle/{id}", produces = "application/json")
    public ResponseEntity<?> obtenerProductoCompletoParaMovil(@PathVariable("id") Integer id) {
        try {
            Producto prod = productoService.buscarProducto(id); 
            
            if (prod != null) {
                
                java.util.Map<String, Object> jsonMap = new java.util.HashMap<>();
                jsonMap.put("id_producto", prod.getId_producto());
                jsonMap.put("nombre", prod.getNombre());
                jsonMap.put("descripcion", prod.getDescripcion());
               
                jsonMap.put("precio", prod.getPrecio() != null ? prod.getPrecio().doubleValue() : 0.0);
                jsonMap.put("estado", prod.getEstado());
                jsonMap.put("categoria", prod.getCategoria());
                jsonMap.put("color", prod.getColor());
                jsonMap.put("material", prod.getMaterial());
                
             
                java.util.List<java.util.Map<String, Object>> listaImgs = new java.util.ArrayList<>();
                if (prod.getImagenes() != null) {
                    prod.getImagenes().forEach(img -> {
                        java.util.Map<String, Object> jsonImg = new java.util.HashMap<>();
                        jsonImg.put("id_imagen", img.getIdImagen());
                        jsonImg.put("imagenUrl", img.getImagenUrl());
                        listaImgs.add(jsonImg);
                    });
                }
                jsonMap.put("imagenes", listaImgs);
                
                
                java.util.List<java.util.Map<String, Object>> listaTallas = new java.util.ArrayList<>();
                if (prod.getTallas() != null) {
                    prod.getTallas().forEach(t -> {
                        java.util.Map<String, Object> jsonTalla = new java.util.HashMap<>();
                       
                        jsonTalla.put("id_producto_talla", t.getTalla() != null ? t.getTalla().getId_talla() : 0);
                        
                       
                        java.util.Map<String, Object> subTallaMap = new java.util.HashMap<>();
                        if (t.getTalla() != null) {
                            subTallaMap.put("id_talla", t.getTalla().getId_talla());
                            subTallaMap.put("nombre", t.getTalla().getNombre());
                        }
                        jsonTalla.put("talla", subTallaMap);
                        jsonTalla.put("stock", t.getStock());
                        
                        listaTallas.add(jsonTalla);
                    });
                }
                jsonMap.put("tallas", listaTallas);
                
                return ResponseEntity.ok(jsonMap);
            }
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El calzado seleccionado no existe.");
            
        } catch (Exception e) {
            System.err.println("Error crítico en API de detalle móvil: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    }
