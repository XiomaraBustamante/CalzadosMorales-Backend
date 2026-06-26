package com.calzadosmorales.entity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty; // 🌟 NUEVO IMPORT

@Entity
@Table(name = "producto_talla")
public class ProductoTalla {

    @EmbeddedId
    private ProductoTallaKey id = new ProductoTallaKey();

    @ManyToOne
    @MapsId("id_producto")
    @JoinColumn(name = "id_producto")
    @JsonBackReference(value = "producto-talla")
    private Producto producto;

    @ManyToOne
    @MapsId("id_talla")
    @JoinColumn(name = "id_talla")
    private Talla talla;

    @Column(nullable = false)
    private Integer stock;

    public ProductoTalla() {}

    // 🌟 TRUCO CORPORATIVO: Generamos la propiedad virtual para hacer match perfecto con Android
    @JsonProperty("id_producto_talla")
    public int getIdProductoTallaVirtual() {
        // Usamos el id de la talla como identificador plano para el constructor de Kotlin
        return this.talla != null ? this.talla.getId_talla() : 0;
    }

    // --- GETTERS Y SETTERS ORIGINALES ---
    public ProductoTallaKey getId() { return id; }
    public void setId(ProductoTallaKey id) { this.id = id; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public Talla getTalla() { return talla; }
    public void setTalla(Talla talla) { this.talla = talla; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}