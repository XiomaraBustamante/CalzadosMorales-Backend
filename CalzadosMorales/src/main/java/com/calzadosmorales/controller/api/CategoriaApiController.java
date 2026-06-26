package com.calzadosmorales.controller.api;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.calzadosmorales.entity.Categoria;
import com.calzadosmorales.repository.CategoriaRepository;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
public class CategoriaApiController {

    @Autowired
    private CategoriaRepository categoriaRepository;

  
    @GetMapping(value = "/listar", produces = "application/json")
    public ResponseEntity<List<Categoria>> listarCategoriasParaMovil() {
        try {
           
            List<Categoria> categoriasConZapatos = categoriaRepository.findCategoriasConProductosActivos();
            
            if (categoriasConZapatos.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            
            return ResponseEntity.ok(categoriasConZapatos);
            
        } catch (Exception e) {
            System.err.println("Error crítico en API de categorías móvil: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}