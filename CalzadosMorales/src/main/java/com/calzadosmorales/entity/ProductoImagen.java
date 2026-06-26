package com.calzadosmorales.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "producto_imagen")
public class ProductoImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private int idImagen;

    @Column(name = "imagen_url", nullable = false, length = 500)
    private String imagenUrl;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    @JsonBackReference // 🔥 CRÍTICO: Evita que el JSON entre en un bucle infinito
    private Producto producto;

    // --- CONSTRUCTORES ---
    public ProductoImagen() {}

    public ProductoImagen(String imagenUrl, Producto producto) {
        this.imagenUrl = imagenUrl;
        this.producto = producto;
    }

    // --- GETTERS Y SETTERS ---
    public int getIdImagen() { return idImagen; }
    public void setIdImagen(int idImagen) { this.idImagen = idImagen; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
}