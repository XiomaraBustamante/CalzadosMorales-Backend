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
    
    // 🔥 CORREGIDO: Ahora valida duplicados solo por Nombre y Color (quitamos la Talla única)
    boolean existsByNombreAndColor(String nombre, Color color);
    
    // 🔥 CORREGIDO: Consulta adaptada para sumar el stock desde la tabla intermedia producto_talla
    @Query("SELECT COUNT(p) FROM Producto p JOIN p.tallas t WHERE p.estado = true GROUP BY p.id_producto HAVING SUM(t.stock) <= 3")
    Long contarProductosStockCritico();
    
    // CONSULTA COMPARTIDA (STOCK CON FILTROS)
    @Query(value = "CALL sp_ConsultaStockFiltros(:idCat, :talla)", nativeQuery = true)
    List<Object[]> consultaStockFiltros(@Param("idCat") int idCat, @Param("talla") String talla);

    // CONSULTA VENDEDOR #3 (Productos estancados)
    @Query(value = "CALL sp_VendedorProductosEstancados()", nativeQuery = true)
    List<Object[]> productosEstancados();
}