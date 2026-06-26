package com.calzadosmorales.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_venta")
public class DetalleVenta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id_detalle;

	@ManyToOne
	@JoinColumn(name = "id_venta", nullable = false)
	private Venta venta;

	
	@ManyToOne
	@JoinColumns({
		@JoinColumn(name = "id_producto", referencedColumnName = "id_producto", nullable = false),
		@JoinColumn(name = "id_talla", referencedColumnName = "id_talla", nullable = false)
	})
	private ProductoTalla productoTalla;

	@Column(nullable = false)
	private Integer cantidad;

	@Column(name = "precio", nullable = false)
	private BigDecimal precio;

	@Column(nullable = false)
	private BigDecimal subtotal;

	public DetalleVenta() {
	}

	public Integer getId_detalle() {
		return id_detalle;
	}

	public void setId_detalle(Integer id_detalle) {
		this.id_detalle = id_detalle;
	}

	public Venta getVenta() {
		return venta;
	}

	public void setVenta(Venta venta) {
		this.venta = venta;
	}


	public ProductoTalla getProductoTalla() {
		return productoTalla;
	}

	public void setProductoTalla(ProductoTalla productoTalla) {
		this.productoTalla = productoTalla;
	}

	public Integer getCantidad() {
		return cantidad;
	}

	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}

	public BigDecimal getPrecio() {
		return precio;
	}

	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}
}