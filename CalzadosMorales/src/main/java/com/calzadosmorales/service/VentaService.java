package com.calzadosmorales.service;

import com.calzadosmorales.entity.Venta;
import com.calzadosmorales.entity.DetalleVenta;
import com.calzadosmorales.entity.ProductoTalla;
import com.calzadosmorales.repository.VentaRepository;
import com.calzadosmorales.repository.ProductoTallaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoTallaRepository productoTallaRepository;

    // Registrar la venta y descontar las unidades de la tabla producto_talla
    @Transactional
    public Venta registrarVenta(Venta venta) {
        for (DetalleVenta detalle : venta.getDetalles()) {
            ProductoTalla pt = detalle.getProductoTalla();
            
            if (pt == null) {
                throw new RuntimeException("⚠️ Alerta: La variante de la talla es nula.");
            }
            
            if (pt.getStock() < detalle.getCantidad()) {
                throw new RuntimeException("⚠️ Alerta: Stock insuficiente para el calzado seleccionado.");
            }

            // Descuento en caliente en Railway
            pt.setStock(pt.getStock() - detalle.getCantidad());
            productoTallaRepository.save(pt);
            
            detalle.setVenta(venta);
        }
        return ventaRepository.save(venta);
    }

    public Venta buscarPorId(Integer id) {
        return ventaRepository.findById(id).orElse(null);
    }

    public List<Object[]> listarMisVentas(Integer idUsuario) {
        return ventaRepository.listarMisVentas(idUsuario);
    }

    public Double totalVendidoHoyVendedor(Integer idUsuario) {
        Double total = ventaRepository.totalVendidoHoyVendedor(idUsuario);
        return total != null ? total : 0.0;
    }

    public List<Object[]> clientesPorRecuperar(Integer idUsuario) {
        return ventaRepository.clientesPorRecuperar(idUsuario);
    }

    public List<Object[]> obtenerHistorialGeneralAdmin() {
        return ventaRepository.adminHistorialGeneral();
    }

    public List<Object[]> obtenerReporteFechas(String inicio, String fin) {
        return ventaRepository.adminReporteFechas(inicio, fin);
    }

    public Double obtenerSumatoriaRango(String inicio, String fin) {
        Double sumatoria = ventaRepository.adminSumatoriaRango(inicio, fin);
        return sumatoria != null ? sumatoria : 0.0;
    }

    public List<Object[]> obtenerAnalisisHorario() {
        return ventaRepository.adminAnalisisHorario();
    }
}