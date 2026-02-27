package com.calzadosmorales.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.calzadosmorales.repository.VentaRepository;

@Service
public class DashboardVendedorService {

    @Autowired
    private VentaRepository ventaRepo;

    public Map<String, Object> obtenerDatosDashboardVendedor(int idUsuario) {
        Map<String, Object> datos = new HashMap<>();

        // 1. TARJETAS (KPIs)
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

        // 2. GRÁFICO DE BARRAS (Rendimiento Semanal)
        datos.put("datosBarras", ventaRepo.getRendimientoComparativo(idUsuario));
        
        // 3. GRÁFICO CIRCULAR (Ventas por Género)
        // Usamos la misma clave que ya tienes en el HTML para no romper nada
        List<Object[]> datosGenero = ventaRepo.getVentasPorGenero(idUsuario);
        datos.put("categoriasTop", datosGenero);
 
        // 4. TABLA DE ACTIVIDAD (Últimos 7 clientes)
        datos.put("ultimasVentasVendedor", ventaRepo.getUltimosSieteClientes(idUsuario));

        return datos;
    }
}