package com.calzadosmorales.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "productos")
public class Producto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id_producto;

	@Column(nullable = false, length = 150)
	private String nombre;

	@Column(length = 500)
	private String descripcion;

	@Column(nullable = false)
	private BigDecimal precio;

	@Column(nullable = false)
	private Boolean estado;

	@ManyToOne
	@JoinColumn(name = "id_categoria", nullable = false)
	private Categoria categoria;

	@ManyToOne
	@JoinColumn(name = "id_color")
	private Color color;

	@ManyToOne
	@JoinColumn(name = "id_material")
	private Material material;

	// Se añade 'orphanRemoval = true' para purgar automáticamente fotos antiguas/duplicadas de la BD
	@OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JsonManagedReference(value = "producto-imagen")
	private List<ProductoImagen> imagenes;

	// 🌟 CORREGIDO: Se cambia de CascadeType.ALL a MERGE y REFRESH para evitar el error "detached entity passed to persist"
	@OneToMany(mappedBy = "producto", cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
	@JsonManagedReference(value = "producto-talla")
	private List<ProductoTalla> tallas;

	public Producto() {
	}

	public Integer getId_producto() {
		return id_producto;
	}

	public void setId_producto(Integer id_producto) {
		this.id_producto = id_producto;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public BigDecimal getPrecio() {
		return precio;
	}

	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}

	public Boolean getEstado() {
		return estado;
	}

	public boolean isEstado() {
		return estado != null && estado;
	}

	public void setEstado(Boolean estado) {
		this.estado = estado;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public void setCategoria(Categoria categoria) {
		this.categoria = categoria;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Material getMaterial() {
		return material;
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	public List<ProductoImagen> getImagenes() {
		return imagenes;
	}

	public void setImagenes(List<ProductoImagen> imagenes) {
		this.imagenes = imagenes;
	}

	public List<ProductoTalla> getTallas() {
		return tallas;
	}

	public void setTallas(List<ProductoTalla> tallas) {
		this.tallas = tallas;
	}
}