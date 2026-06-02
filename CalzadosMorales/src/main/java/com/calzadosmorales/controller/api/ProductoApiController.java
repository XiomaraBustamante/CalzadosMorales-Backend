package com.calzadosmorales.controller.api;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.calzadosmorales.entity.Producto;
import com.calzadosmorales.service.ProductoService; 

@RestController
@RequestMapping("/api/productos")
public class ProductoApiController {

    @Autowired
    private ProductoService productoService; 

    // Este endpoint llamará la app móvil con para jalar el catálogo
    //@GetMapping("/listar")
    @GetMapping(value = "/listar", produces = "application/json")
    public ResponseEntity<List<Producto>> listarProductosParaMovil() {
        try {
            List<Producto> lista = productoService.listarProductos(); 
            if (lista.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}