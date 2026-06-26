package com.calzadosmorales.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.calzadosmorales.entity.Venta;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

  

    @Query(value = "CALL sp_VentasMesVendedor(:id)", nativeQuery = true)
    Double getVentasMes(@Param("id") int idVendedor);

    @Query(value = "CALL sp_ComisionMesVendedor(:id)", nativeQuery = true)
    Double getComisionMes(@Param("id") int idVendedor);

    @Query(value = "CALL sp_ParesVendidosVendedor(:id)", nativeQuery = true)
    Integer getParesVendidos(@Param("id") int idVendedor);

    @Query(value = "CALL sp_ProductoEstrellaVendedor(:id)", nativeQuery = true)
    String getProductoEstrella(@Param("id") int idVendedor);

    @Query(value = "CALL sp_MiMejorCliente(:idVendedor)", nativeQuery = true)
    String getMejorCliente(@Param("idVendedor") int idVendedor);

    @Query(value = "CALL sp_RendimientoVendedorComparativo(:id)", nativeQuery = true)
    List<Object[]> getRendimientoComparativo(@Param("id") int idVendedor);

    @Query(value = "CALL sp_VentasPorGeneroVendedor(:id)", nativeQuery = true)
    List<Object[]> getVentasPorGenero(@Param("id") int idVendedor);

    @Query(value = "CALL sp_MisUltimosSieteClientes(:id)", nativeQuery = true)
    List<Object[]> getUltimosSieteClientes(@Param("id") int idVendedor);

  

    @Query(value = "CALL sp_AdminCajaHoy()", nativeQuery = true)
    Double getAdminCajaHoy();

    @Query(value = "CALL sp_AdminStockCritico()", nativeQuery = true)
    Integer getAdminStockCritico();

    @Query(value = "CALL sp_AdminClientesNuevosMes()", nativeQuery = true)
    Integer getAdminClientesNuevosMes();

    @Query(value = "CALL sp_AdminCantidadVentasHoy()", nativeQuery = true)
    Integer getAdminCantidadVentasHoy();

    @Query(value = "CALL sp_AdminTicketPromedio()", nativeQuery = true)
    Double getAdminTicketPromedio();

    @Query(value = "CALL sp_AdminVentasSemanales()", nativeQuery = true)
    List<Object[]> getAdminVentasSemanales();

    @Query(value = "CALL sp_AdminStockPorCategoria()", nativeQuery = true)
    List<Object[]> getAdminStockPorCategoria();

    @Query(value = "CALL sp_AdminTopCincoVendedores()", nativeQuery = true)
    List<Object[]> getAdminTopCincoVendedores();
    


    @Query(value = "CALL sp_ListarMisVentas(:id)", nativeQuery = true)
    List<Object[]> listarMisVentas(@Param("id") int idUsuario);

    @Query(value = "CALL sp_TotalVendidoHoyVendedor(:id)", nativeQuery = true)
    Double totalVendidoHoyVendedor(@Param("id") int idUsuario);

    @Query(value = "CALL sp_VendedorClientesPorRecuperar(:id)", nativeQuery = true)
    List<Object[]> clientesPorRecuperar(@Param("id") int idUsuario);

    @Query(value = "CALL sp_AdminHistorialGeneralVentas()", nativeQuery = true)
    List<Object[]> adminHistorialGeneral();

    @Query(value = "CALL sp_AdminReporteVentasPorFechas(:ini, :fin)", nativeQuery = true)
    List<Object[]> adminReporteFechas(@Param("ini") String inicio, @Param("fin") String fin);

    @Query(value = "CALL sp_AdminSumatoriaVentasRango(:ini, :fin)", nativeQuery = true)
    Double adminSumatoriaRango(@Param("ini") String inicio, @Param("fin") String fin);

    @Query(value = "CALL sp_AdminAnalisisHorarioVentas()", nativeQuery = true)
    List<Object[]> adminAnalisisHorario();



   
    boolean existsByCodigoSincronizacion(String codigoSincronizacion);


    @Query(value = "SELECT MAX(CAST(numero AS UNSIGNED)) FROM venta WHERE serie = :serie", nativeQuery = true)
    Integer findMaxCorrelativoBySerie(@Param("serie") String serie);
}