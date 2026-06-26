package com.calzadosmorales.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venta")
public class Venta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id_venta;

	@Column(nullable = false)
	private LocalDateTime fecha;

	@Column(nullable = false)
	private BigDecimal total;

	@Column(length = 20)
	private String estado;

	@Column(name = "tipo_comprobante", length = 20)
	private String tipoComprobante;

	@Column(length = 5)
	private String serie;

	@Column(length = 20)
	private String numero;
	
	@Column(name = "metodo_pago", length = 20)
	private String metodoPago;

	// 🌟 NUEVO: Campo de Auditoría para discriminar canales de venta (WEB / ANDROID)
	@Column(length = 20)
	private String origen;

	// 🌟 NUEVO: Token de control móvil único para asegurar idempotencia (Evita duplicados)
	@Column(name = "codigo_sincronizacion", unique = true, length = 100)
	private String codigoSincronizacion;

	@ManyToOne
	@JoinColumn(name = "id_usuario", nullable = false)
	private Usuario usuario;
    
	@ManyToOne
	@JoinColumn(name = "id_cliente", nullable = false)
	private Cliente cliente;
	
	@OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<DetalleVenta> detalles = new ArrayList<>();

	public Venta() {
		this.fecha = LocalDateTime.now();
		this.detalles = new ArrayList<>();
		this.estado = "REGISTRADA";
		this.tipoComprobante = "Boleta";
		this.serie = "B001";
		this.metodoPago = "Efectivo";
		this.origen = "WEB"; // Fallback por defecto nativo
	}

	public void agregarDetalle(DetalleVenta detalle) {
		detalles.add(detalle);
		detalle.setVenta(this);
	}

	public Integer getId_venta() {
		return id_venta;
	}

	public void setId_venta(Integer id_venta) {
		this.id_venta = id_venta;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getTipoComprobante() {
		return tipoComprobante;
	}

	public void setTipoComprobante(String tipoComprobante) {
		this.tipoComprobante = tipoComprobante;
	}

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}
	
	public String getMetodoPago() {
	    return metodoPago;
	}

	public void setMetodoPago(String metodoPago) {
	    this.metodoPago = metodoPago;
	}

	// 🌟 GETTERS Y SETTERS NUEVOS
	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}

	public String getCodigoSincronizacion() {
		return codigoSincronizacion;
	}

	public void setCodigoSincronizacion(String codigoSincronizacion) {
		this.codigoSincronizacion = codigoSincronizacion;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Cliente getCliente() {
	    return cliente;
	}

	public void setCliente(Cliente cliente) {
	    this.cliente = cliente;
	}

	public List<DetalleVenta> getDetalles() {
		return detalles;
	}

	public void setDetalles(List<DetalleVenta> detalles) {
		this.detalles = detalles;
	}
}