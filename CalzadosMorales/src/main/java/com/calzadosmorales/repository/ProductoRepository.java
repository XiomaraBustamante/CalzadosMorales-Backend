package com.calzadosmorales.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.calzadosmorales.entity.Color;
import com.calzadosmorales.entity.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    
   
    boolean existsByNombreAndColor(String nombre, Color color);
    
   
    @Query("SELECT COUNT(p) FROM Producto p JOIN p.tallas t WHERE p.estado = true GROUP BY p.id_producto HAVING SUM(t.stock) <= 3")
    Long contarProductosStockCritico();
    
    
    @Query(value = "CALL sp_ConsultaStockFiltros(:idCat, :talla)", nativeQuery = true)
    List<Object[]> consultaStockFiltros(@Param("idCat") int idCat, @Param("talla") String talla);

    @Query(value = "CALL sp_VendedorProductosEstancados()", nativeQuery = true)
    List<Object[]> productosEstancados();
}