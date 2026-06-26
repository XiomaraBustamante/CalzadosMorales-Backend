package com.calzadosmorales.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.calzadosmorales.entity.Categoria;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    Categoria findByNombre(String nombre);
 
    List<Categoria> findByEstado(Boolean estado);

    // 🌟 CONSULTA MAESTRA: Trae categorías activas que tienen productos activos vinculados (Sin duplicados)
    @Query("SELECT DISTINCT c FROM Producto p JOIN p.categoria c WHERE c.estado = true AND p.estado = true")
    List<Categoria> findCategoriasConProductosActivos();
}