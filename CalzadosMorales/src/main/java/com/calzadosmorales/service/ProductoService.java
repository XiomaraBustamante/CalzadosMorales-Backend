package com.calzadosmorales.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.calzadosmorales.entity.*;
import com.calzadosmorales.repository.*;

@Service
public class ProductoService {

    @Autowired private ProductoRepository productoRepo;
    @Autowired private CategoriaRepository categoriaRepo;
    @Autowired private TallaRepository tallaRepo;
    @Autowired private ColorRepository colorRepo;
    @Autowired private MaterialRepository materialRepo;
    @Autowired private ProductoTallaRepository productoTallaRepo; // 🌟 NUEVO: Inyectado para persistir curvas de stock de forma directa

    public List<Producto> listarProductos() {
        return productoRepo.findAll();
    }

    @Transactional
    public void guardarProducto(Producto p) {
        // 1. Guardamos primero el producto base para asegurar la existencia del ID de MySQL
        List<ProductoTalla> tallasTemporales = p.getTallas();
        p.setTallas(new ArrayList<>()); // Desacoplamos temporalmente de la cascada
        
        if (p.getImagenes() != null) {
            p.getImagenes().forEach(img -> img.setProducto(p));
        }
        
        // Guardado/Actualización de la entidad maestra
        Producto productoPersistido = productoRepo.save(p);

        // 2. Persistencia manual y controlada de la curva de tallas/stocks
        if (tallasTemporales != null) {
            for (ProductoTalla pt : tallasTemporales) {
                pt.setProducto(productoPersistido);
                // Aseguramos que la llave compuesta tome el ID generado en el paso anterior
                pt.getId().setId_producto(productoPersistido.getId_producto());
                productoTallaRepo.save(pt); // Sincronización forzada directa en la BD
                productoPersistido.getTallas().add(pt);
            }
        }
    }

    public Producto buscarProducto(Integer id) {
        return productoRepo.findById(id).orElse(null);
    }

    public boolean existeProductoIgual(String nombre, Color color) {
        return productoRepo.existsByNombreAndColor(nombre, color);
    }

    public List<Categoria> listarCategorias() {
        return categoriaRepo.findByEstado(true);
    }

    public List<Talla> listarTallas() {
        return tallaRepo.findByEstado(true);
    }

    public List<Color> listarColores() {
        return colorRepo.findByEstado(true);
    }

    public List<Material> listarMateriales() {
        return materialRepo.findByEstado(true);
    }
    
    public List<Producto> consultarStockConFiltros(int idCat, String talla) {
        List<Object[]> resultados = productoRepo.consultaStockFiltros(idCat, talla);
        List<Producto> listaCorregida = new ArrayList<>();
        
        try {
            for (Object[] fila : resultados) {
                Producto p = new Producto();

                if (fila[0] != null) p.setId_producto(((Number) fila[0]).intValue());
                if (fila[1] != null) p.setNombre(fila[1].toString());

                if (fila[5] != null) {
                    p.setPrecio(new BigDecimal(fila[5].toString()));
                } else {
                    p.setPrecio(BigDecimal.ZERO);
                }
                
                p.setTallas(new ArrayList<>());
                if (fila[6] != null) {
                    ProductoTalla pt = new ProductoTalla();
                    pt.setStock(((Number) fila[6]).intValue());
                    
                    Talla tObj = new Talla();
                    if (fila[3] != null) tObj.setNombre(fila[3].toString());
                    pt.setTalla(tObj);
                    
                    p.getTallas().add(pt);
                }
                
                p.setCategoria(new Categoria()); 
                if (fila[2] != null) p.getCategoria().setNombre(fila[2].toString());

                p.setColor(new Color());
                if (fila[4] != null) p.getColor().setNombre(fila[4].toString());

                listaCorregida.add(p);
            }
        } catch (Exception e) {
            System.err.println("Error procesando fila de producto en Service: " + e.getMessage());
            e.printStackTrace();
        }
        
        return listaCorregida;
    }

    public List<Object[]> obtenerProductosEstancados() {
        return productoRepo.productosEstancados();
    }
    

    
}