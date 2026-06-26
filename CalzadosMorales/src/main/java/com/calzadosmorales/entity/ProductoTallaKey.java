package com.calzadosmorales.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProductoTallaKey implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id_producto;
    private Integer id_talla;

    public ProductoTallaKey() {}

    public ProductoTallaKey(Integer id_producto, Integer id_talla) {
        this.id_producto = id_producto;
        this.id_talla = id_talla;
    }

    public Integer getId_producto() { return id_producto; }
    public void setId_producto(Integer id_producto) { this.id_producto = id_producto; }

    public Integer getId_talla() { return id_talla; }
    public void setId_talla(Integer id_talla) { this.id_talla = id_talla; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == o || getClass() != o.getClass()) return false;
        ProductoTallaKey that = (ProductoTallaKey) o;
        return Objects.equals(id_producto, that.id_producto) && Objects.equals(id_talla, that.id_talla);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_producto, id_talla);
    }
}