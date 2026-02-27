# 👠 Calzados Morales - ERP System v1.0
> **Solución integral para la gestión retail de calzado, optimizando el ciclo de inventario y ventas.**

[![Java Version](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.2-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Security](https://img.shields.io/badge/Spring_Security-Auth-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)](https://spring.io/projects/spring-security)

---

## 📸 Vista Previa del Sistema
*¡Visualiza el rendimiento de tu negocio en tiempo real!*

| Dashboard Vendedor | Dashboard Administrador |
|:---:|:---:|
| Análisis de segmentación y KPIs personales | Control global de ingresos y stock crítico |



---

## 🛠️ Arquitectura Técnica (Stack Tecnológico)

El sistema sigue el patrón de diseño **MVC (Modelo-Vista-Controlador)** reforzado con una capa de servicios para desacoplar la lógica de negocio.



* **Core:** Java 17 + Spring Boot 3.
* **Seguridad:** Spring Security con persistencia en BD (BCrypt para contraseñas).
* **Persistencia:** Spring Data JPA + MySQL Stored Procedures (para lógica compleja de reportes).
* **Frontend:** Thymeleaf + Bootstrap 5 (Plantilla Premium StarAdmin).
* **Reportes:** * **PDF:** JasperReports (Boletas y Facturas dinámicas).
    * **Excel:** Apache POI (Exportación de historial con formato de tabla nativa).

---

## 🌟 Funcionalidades Estrella

### 📊 Inteligencia de Negocios (BI)
- **Segmentación por Género:** Gráfico dinámico que analiza el perfil del cliente mediante la integración de tablas de Persona Natural.
- **KPIs en Tiempo Real:** Cálculo automático de comisiones (5%), ticket promedio y productos "estrella".

### 🛒 Gestión de Ventas Avanzada
- **Transaccionalidad Segura:** Uso de `@Transactional` para garantizar que el descuento de stock y el registro de venta ocurran simultáneamente o se cancelen en caso de error.
- **Comprobantes Pro:** Generación de PDF con códigos únicos y diseño corporativo.

### 🛡️ Seguridad y Roles
| Rol | Acceso a Almacén | Ventas | Reportes Globales | Seguridad |
|:---:|:---:|:---:|:---:|:---:|
| **Admin** | ✅ Total | ❌ (Solo consulta) | ✅ Historial Gral. | ✅ Gestión Usuarios |
| **Vendedor**| 👁️ Solo lectura | ✅ Nueva Venta | 👁️ Solo propias | ❌ Denegado |

---

## 🔧 Configuración del Entorno

### 1. Requisitos
* JDK 17 o superior.
* MySQL Server 8.0.
* NetBeans, IntelliJ o VS Code.

### 2. Base de Datos
Importa el script SQL incluido en la carpeta `/db`. Este script contiene:
* Estructura de tablas normalizada.
* Procedimientos almacenados para optimización de consultas.
* Sembrado de datos (Seeders) para pruebas inmediatas.

### 3. Propiedades de Conexión
Edita el archivo `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/BDCALZADOS
spring.datasource.username=root
spring.datasource.password=tus_credenciales

👥 Equipo de Desarrollo
Xiomara Bustamante - Líder de Proyecto & Backend Developer

Andrés [Apellido] - Fullstack Developer

Jean Pierre [Apellido] - Database Administrator

© 2026 Calzados Morales | Caminando con elegancia y tecnología.
