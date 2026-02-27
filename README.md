# 👠 Calzados Morales | ERP & BI System
> **"Transformando el retail de calzado con tecnología Spring Boot y análisis de datos en tiempo real."**

---

## 📊 Dashboard de Control Inteligente
| Módulo Administrativo | Módulo de Ventas |
|:---:|:---:|
| ![Admin](https://img.shields.io/badge/Status-Líder_de_Caja-blue?style=flat-square) | ![Vendedor](https://img.shields.io/badge/Status-Ventas_Activas-success?style=flat-square) |
| Gestión global de stock y rendimiento de personal. | KPIs de comisiones, productos estrella y metas. |

---

## 🚀 Key Features (Lo más destacado)

> [!IMPORTANT]
> **Integridad Transaccional:** El sistema garantiza que ninguna venta se registre si no hay stock disponible, utilizando el motor de transacciones de **Spring Data JPA** y la anotación `@Transactional`.

* **⚡ Reportes en 1-Clic:** Exportación profesional a **Excel** (Apache POI) con formato de tabla nativa y generación de comprobantes **PDF** (JasperReports) automáticos.
* **🧠 Business Intelligence:** Análisis de segmentación de mercado por género y rendimiento comparativo mediante procedimientos almacenados optimizados en **MySQL**.
* **🛡️ Seguridad Robusta:** Implementación de **Spring Security** con control de acceso por roles (Admin/Vendedor) y encriptación de credenciales con BCrypt.

---

## 🛠️ Stack Tecnológico



### **Backend**
* **Lenguaje:** Java 17 (LTS)
* **Framework:** Spring Boot 3.2.2
* **Seguridad:** Spring Security (Auth & Authorization)
* **Librerías:** Apache POI, JasperReports, Lombok.

### **Frontend**
* **Motor de Plantillas:** Thymeleaf
* **Diseño:** Bootstrap 5 + Material Design Icons (MDI)
* **Gráficos:** Chart.js (Visualización dinámica de KPIs)

### **Base de Datos**
* **Motor:** MySQL 8.0
* **Lógica:** Procedimientos Almacenados (Stored Procedures) para optimización de reportes y triggers de auditoría.

---

## 📂 Estructura del Proyecto
```text
com.calzadosmorales
 ├── controller    # Controladores de rutas, APIs y manejo de peticiones
 ├── service       # Lógica de negocio, transaccionalidad y puentes a BD
 ├── repository    # Interfaces de persistencia y llamadas a procedimientos nativos
 ├── entity        # Modelos de datos y mapeo objeto-relacional (JPA Entities)
 └── fragmentos    # Componentes UI reutilizables (Navbar, Sidebar, Footer)

##👤 Equipo de Desarrollo
Xiomara Bustamante - Líder de Proyecto & Backend Developer

Andrés Juarez - Fullstack Developer

Jean Pierre Morales - Frontend Developer & UI Designer

Dyabis Quispe - Database Administrator (DBA)

Stefano Flores - QA Tester & Quality Assurance

<p align="center">
<b>Calzados Morales 2026</b>
<i>"Donde la elegancia camina con el código."</i>
</p>
