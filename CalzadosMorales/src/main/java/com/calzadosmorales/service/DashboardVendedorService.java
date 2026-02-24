package com.calzadosmorales.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.calzadosmorales.repository.DetalleVentaRepository;
import com.calzadosmorales.repository.VentaRepository;


@Service
public class DashboardVendedorService {

    @Autowired
    private VentaRepository ventaRepo;

 
    public Map<String, Object> obtenerDatosDashboardVendedor(int idUsuario) {
        Map<String, Object> datos = new HashMap<>();

        // TARJETAS
        
        Double ventasMes = ventaRepo.getVentasMes(idUsuario);
        datos.put("ventasMes", ventasMes != null ? ventasMes : 0.0);
        
        Double comision = ventaRepo.getComisionMes(idUsuario);
        datos.put("comision", comision != null ? comision : 0.0);
        
        Integer pares = ventaRepo.getParesVendidos(idUsuario);
        datos.put("paresVendidos", pares != null ? pares : 0);
        
        String producto = ventaRepo.getProductoEstrella(idUsuario);
        datos.put("productoStar", producto != null ? producto : "Sin ventas");
        
        String mejorCliente = ventaRepo.getMejorCliente(idUsuario);
        datos.put("mejorCliente", (mejorCliente != null && !mejorCliente.isEmpty()) 
         ? mejorCliente : "Sin registros");


        // GrAfico de Barras
        datos.put("datosBarras", ventaRepo.getRendimientoComparativo(idUsuario));
        
        // GrAfico Circular
        datos.put("categoriasTop", ventaRepo.getVentasPorGenero(idUsuario));
 
        // TABLA DE ACTIVIDAD 

        // Lista de los últimos 7 clientes atendidos
        datos.put("ultimasVentasVendedor", ventaRepo.getUltimosSieteClientes(idUsuario));

        return datos;
    }
}