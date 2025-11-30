# 🏟️ Sistema de Gestión de Instalaciones Deportivas - UHU

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JSP](https://img.shields.io/badge/JSP-Jakarta-red?style=for-the-badge)
![MySQL](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)
![Stripe](https://img.shields.io/badge/Stripe-635BFF?style=for-the-badge&logo=stripe&logoColor=white)

> **Proyecto académico** para la asignatura de *Desarrollo de Aplicaciones Web*.  
> **Universidad de Huelva** (Curso 2025-2026).

Este proyecto implementa una plataforma web completa para la gestión, reserva y administración de espacios deportivos universitarios. Está construido sobre una arquitectura **MVC nativa con Java EE (Servlets/JSP)**, destacando por su gestión de transacciones robusta y una integración a bajo nivel con la pasarela de pagos Stripe.

---
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/CodesInfinity/instalaciones-uhu) 
---

## 🚀 Funcionalidades Principales

### 👥 Gestión de Usuarios y Roles (RBAC)
- **Roles Jerárquicos:** Administrador (0), Estudiante (1) y Profesor (2).
- **Ciclo de Vida:** Registro, Login seguro (sesiones HTTP), edición de perfil y baja.
- **Flujo de Aprobación:** Sistema para solicitar ascenso a rol de Profesor, requiriendo validación administrativa.

### 🎾 Inventario de Instalaciones
- **CRUD Completo:** Alta, baja y modificación de espacios deportivos.
- **Gestión Multimedia Avanzada:** Subida de imágenes con persistencia dual (carpeta de despliegue + carpeta fuente) para mantener los archivos en entornos de desarrollo.
- **Catálogo Público:** Filtrado y visualización de detalles técnicos.

### 📅 Sistema de Reservas Inteligente
- **Algoritmo de Disponibilidad:** Generación dinámica de *slots* de 90 minutos y exclusión de fines de semana.
- **Motor de Precios Dinámico:** Cálculo automático de tarifas basado en:
  - Tipo de usuario (Gratis para Profesores).
  - Posesión de Tarjeta Universitaria (TUO).
  - Características de la pista (Suplemento por luz artificial).
- **Integridad de Datos:** Prevención de condiciones de carrera (*Race Conditions*) para evitar dobles reservas.

### 💳 Pasarela de Pagos (Stripe)
- **Integración Nativa:** Uso de `HttpsURLConnection` para comunicar con la API REST de Stripe (sin SDKs externos).
- **Seguridad:** Tokenización de tarjetas en el cliente mediante `Stripe.js`.

---

## 🛠️ Stack Tecnológico

| Capa | Tecnología | Descripción |
| :--- | :--- | :--- |
| **Backend** | Java Servlets | Controladores con patrón Front Controller. |
| **Modelo** | JPA / JTA | Persistencia y gestión de transacciones. |
| **Base de Datos** | MySQL | Motor de base de datos relacional. |
| **Frontend** | JSP / JSTL | Vistas dinámicas con *Layout Pattern*. |
| **Scripting** | Vanilla JS (ES6) | Validaciones fetch y lógica de UI. |
| **Pagos** | Stripe API | Procesamiento de transacciones bancarias. |

---

## ⚙️ Instalación y Configuración

### 1. Requisitos Previos
- **JDK 11** o superior.
- **Servidor de Aplicaciones:** GlassFish, Payara o Tomcat.
- **MySQL Server**.

### 2. Base de Datos
Ejecuta el script SQL ubicado en `/web/sql/database.sql` para generar el esquema de tablas iniciales.

Configura la conexión en `src/conf/persistence.xml`:
```xml
<property name="javax.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/instalaciones_uhu"/>
<property name="javax.persistence.jdbc.user" value="root"/>
<property name="javax.persistence.jdbc.password" value="tu_password"/>
```

3. Configuración de Stripe (Esencial para Pagos) ⚠️
Para habilitar la pasarela de pagos en entorno local, sigue estos pasos:

Navega a la carpeta: src/java/app/controladores/conf/data.

Abre el archivo y copia el HASH de seguridad.

Ve al archivo: src/java/app/controladores/controladorReserva.java.
Localiza la constante STRIPE_SECRET_KEY y pega el hash después del prefijo sk_.

// Ejemplo
private static final String STRIPE_SECRET_KEY = "sk_PEGAR_TU_HASH_AQUI";

---

📂 Estructura del Proyecto
```
instalaciones-uhu/
├── src/java/app/
│   ├── controladores/    # Servlets (Lógica de negocio y API Stripe)
│   ├── modelos/          # Entidades JPA (Usuario, Reserva, Espacio)
│   └── servicios/        # Lógica auxiliar
├── web/
│   ├── img/              # Recursos gráficos
│   ├── scripts/          # JS (Validaciones, Logout, UI)
│   ├── styles/           # CSS modular
│   └── WEB-INF/
│       └── vistas/       # JSPs protegidos (Auth, Admin, Reservas)
└── build/                # Directorio de despliegue
```

✒️ Autor
Agustín Rodríguez Aguilar

💻 Repositorio GitHub ```https://github.com/CodesInfinity/instalaciones-uhu/```

🌐 Web Personal ![Personal](https://agustinrodriguez.netlify.app/)


<div align="center"> <sub>Desarrollado como práctica universitaria - Escuela Técnica Superior de Ingeniería (ETSI)</sub> </div>
