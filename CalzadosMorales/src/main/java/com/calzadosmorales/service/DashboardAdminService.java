package com.calzadosmorales.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.calzadosmorales.repository.ProductoRepository;
import com.calzadosmorales.repository.VentaRepository;

@Service
public class DashboardAdminService {

    @Autowired 
    private VentaRepository ventaRepo;

    public Map<String, Object> cargarPanelAdministrativo() {
        Map<String, Object> datos = new HashMap<>();
        

        
        Double ingresos = ventaRepo.getAdminCajaHoy();
        datos.put("ingresosDia", ingresos != null ? ingresos : 0.0);
        
        Integer stockCritico = ventaRepo.getAdminStockCritico();
        datos.put("productosEnAlerta", stockCritico != null ? stockCritico : 0);
        
        Integer clientesNuevos = ventaRepo.getAdminClientesNuevosMes();
        datos.put("clientesNuevos", clientesNuevos != null ? clientesNuevos : 0);
        
        Integer totalVentas = ventaRepo.getAdminCantidadVentasHoy();
        datos.put("cantidadVentas", totalVentas != null ? totalVentas : 0);
        
        Double promedio = ventaRepo.getAdminTicketPromedio();
        datos.put("ticketPromedio", promedio != null ? promedio : 0.0);

  

        datos.put("ventasSemanales", ventaRepo.getAdminVentasSemanales());
        datos.put("stockCategorias", ventaRepo.getAdminStockPorCategoria());


        datos.put("topVendedores", ventaRepo.getAdminTopCincoVendedores());

        return datos;
    }
}