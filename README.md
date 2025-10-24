# 🏀 Aplicación Web de Reservas de Espacios Deportivos

Este proyecto consiste en el desarrollo de una **aplicación web** para la **gestión de reservas de espacios deportivos** en la Universidad de Huelva. Permite a los usuarios registrados consultar la disponibilidad de las instalaciones, realizar reservas en tramos de 1 hora y 30 minutos, y gestionar su historial de reservas.

---

## 🎯 Objetivos del proyecto

- Facilitar la reserva de pistas, campos y otros espacios deportivos.
- Permitir la visualización de horarios disponibles en tiempo real.
- Garantizar la seguridad de los datos de usuario mediante autenticación y contraseñas encriptadas.
- Asegurar que las reservas respeten los horarios establecidos (08:30 a 20:30).
- Ofrecer una interfaz intuitiva y accesible, adaptada a distintos dispositivos.

---

## ⚙️ Tecnologías utilizadas

**Frontend:**
- HTML5  
- CSS3 (con soporte de Bootstrap 5)  
- JavaScript (AJAX y validaciones)  

**Backend:**
- Java (Servlets y JSP)  
- Patrón MVC  
- Jakarta EE / GlassFish 7.0.14  
- JPA (EclipseLink o Hibernate)

**Base de datos:**
- Apache Derby

---

## 🧩 Funcionalidades principales

- **Registro e inicio de sesión de usuarios** (con encriptación de contraseñas).  
- **Gestión de espacios deportivos** (alta, baja, modificación, consulta).  
- **Visualización dinámica de horarios disponibles** mediante AJAX.  
- **Creación de reservas** en bloques de 1h30, evitando solapamientos.  
- **Validación de horarios** (solo entre 08:30 y 20:30).  
- **Roles de usuario** (estudiante / administrador).  
- **Gestión segura de sesiones** y control de acceso a zonas restringidas.

---

## 🧠 Arquitectura y diseño

El proyecto sigue el **patrón de diseño MVC (Modelo-Vista-Controlador)**:
- **Modelo (Model):** Clases Java que representan las entidades del sistema (Usuario, EspacioDeportivo, Reserva).  
- **Vista (View):** Páginas JSP con soporte de Bootstrap y JavaScript.  
- **Controlador (Controller):** Servlets encargados de gestionar las peticiones y coordinar la lógica de negocio.

Además, se utiliza **JPA** para la persistencia de datos y **validaciones** tanto en el lado del cliente como en el servidor.

---

## 🕒 Reglas de negocio

- Los horarios válidos de reserva son **de 08:30 a 20:30**.  
- Cada reserva tiene una **duración fija de 1 hora y 30 minutos**.  
- No se permiten **solapamientos** entre reservas del mismo espacio.  
- Solo los **usuarios autenticados** pueden crear o cancelar reservas.  

---

## 🔐 Seguridad

- Contraseñas encriptadas con **hash SHA-256** o **BCrypt**.  
- Validación de datos en servidor y cliente.  
- Gestión de sesiones y control de acceso por roles.  
- Comunicación segura mediante protocolo HTTPS (configurable en GlassFish).

---

## 🧪 Pruebas

- **Pruebas funcionales:** creación, consulta y cancelación de reservas.  
- **Pruebas de seguridad:** validación de autenticación y manejo de sesiones.  
- **Pruebas de usabilidad:** diseño responsive y flujo intuitivo para el usuario.

---

## 🚀 Despliegue

1. Clonar el repositorio:
   ```bash
   git clone [(https://github.com/CodesInfinity/instalaciones-uhu.git]
