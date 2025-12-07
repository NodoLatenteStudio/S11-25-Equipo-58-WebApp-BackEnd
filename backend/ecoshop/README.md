# EcoShop Backend

E-commerce sostenible con métricas de impacto ambiental - Backend API

## 📋 Descripción

EcoShop es una plataforma de comercio electrónico enfocada en productos sostenibles que integra métricas de impacto ambiental para cada producto y transacción. El backend proporciona una API REST completa con autenticación mediante Clerk y gestión de productos, marcas, certificaciones, usuarios y pedidos.

## 🛠️ Tecnologías

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Security** (OAuth2 Resource Server con JWT)
- **Spring Data JPA** (Hibernate)
- **PostgreSQL 14+**
- **Maven 3.8+**
- **Clerk** (Autenticación y gestión de usuarios)

## 📦 Requisitos

- Java 21
- Maven 3.8+
- PostgreSQL 14+
- Docker (opcional, para PostgreSQL)
- Cuenta de Clerk (para autenticación)

## ⚙️ Configuración Inicial

### Opción 1: Usando Docker Compose (Recomendado)

1. Iniciar PostgreSQL con Docker Compose:

```bash
docker-compose up -d
```

Esto creará una instancia de PostgreSQL en el puerto **5433** con:
- Base de datos: `ecoshop`
- Usuario: `postgres`
- Contraseña: `postgres`

**Nota:** El puerto está configurado en 5433 (no 5432) para evitar conflictos con instalaciones locales de PostgreSQL.

### Opción 2: Instalación Local de PostgreSQL

1. Instalar PostgreSQL localmente
2. Crear la base de datos:

```sql
CREATE DATABASE ecoshop;
```

### Variables de Entorno

Copiar el archivo `.env.example` a `.env` y configurar las variables:

```bash
# En Windows (PowerShell)
Copy-Item .env.example .env
```

Editar el archivo `.env` con tus credenciales:

```env
# Base de datos
DB_URL=jdbc:postgresql://localhost:5433/ecoshop
DB_USERNAME=postgres
DB_PASSWORD=tu_contraseña

# Clerk (Autenticación)
CLERK_SECRET_KEY=sk_test_xxxxx
CLERK_PUBLISHABLE_KEY=pk_test_xxxxx
CLERK_WEBHOOK_SECRET=whsec_xxxxx
CLERK_ISSUER=https://your-instance.clerk.accounts.dev
CLERK_API_URL=https://api.clerk.com

# Opcional
SPRING_PROFILES_ACTIVE=dev
JPA_DDL_AUTO=update
JPA_SHOW_SQL=false
LOG_LEVEL=INFO
```

**Importante:** Obtén las claves de Clerk desde: https://dashboard.clerk.com → Tu aplicación → API Keys

### Configuración de Spring Profiles

El proyecto usa perfiles de Spring. Por defecto usa el perfil `dev`:

- **dev**: Configuración para desarrollo local
- **prod**: Configuración para producción (requiere configuración adicional)

Para cambiar el perfil, establecer la variable de entorno:

```bash
SPRING_PROFILES_ACTIVE=dev
```

## 🚀 Ejecutar la Aplicación

### Desarrollo

#### Opción 1: Desde el IDE (Recomendado)

1. Abrir el proyecto en tu IDE (IntelliJ IDEA, Eclipse, VS Code, etc.)
2. Asegurar que Java 21 esté configurado como SDK del proyecto
3. Configurar las variables de entorno en la configuración de ejecución
4. Ejecutar la clase `EcoShopApplication` como aplicación Java
5. La aplicación estará disponible en: `http://localhost:8080`

#### Opción 2: Desde la Terminal

```bash
mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

### Compilar

```bash
mvn clean install
```

## 🔐 Autenticación

### Clerk Integrado

La aplicación utiliza **Clerk** para autenticación y gestión de usuarios. Clerk ya está completamente integrado y funcionando.

**Configuración:**
- Autenticación JWT mediante Spring Security OAuth2 Resource Server
- Validación automática de tokens usando JWKS de Clerk
- Sincronización de usuarios mediante webhooks
- El `usuarioId` se obtiene automáticamente del token JWT para endpoints protegidos

**Flujo de Autenticación:**
1. Usuario se autentica en el frontend con Clerk
2. Frontend obtiene token JWT de Clerk
3. Frontend envía requests con header: `Authorization: Bearer <token>`
4. Backend valida el token automáticamente con Clerk
5. Backend extrae `clerkId` del token y busca el usuario en BD
6. Usuario autenticado disponible en `SecurityContext`

**Variables de Entorno Requeridas:**
- `CLERK_ISSUER`: URL de tu instancia de Clerk
- `CLERK_SECRET_KEY`: Secret key de Clerk (para webhooks, opcional)
- `CLERK_WEBHOOK_SECRET`: Secret para verificar webhooks (opcional)

## 📊 Estructura del Proyecto

```
src/main/java/com/ecoshop/
├── config/                    # Configuraciones
│   ├── ClerkProperties.java  # Propiedades de Clerk
│   ├── SecurityConfig.java   # Configuración de seguridad (OAuth2 Resource Server)
│   └── WebConfig.java        # Configuración web
├── controller/               # Controladores REST (organizados por dominio)
│   ├── carrito/
│   │   └── CarritoController.java
│   ├── categoria/
│   │   └── CategoriaController.java
│   ├── certificacion/
│   │   └── CertificacionController.java
│   ├── checkout/
│   │   └── CheckoutController.java
│   ├── clerk/
│   │   ├── ClerkConfigController.java
│   │   ├── ClerkTestController.java    # Solo desarrollo
│   │   └── ClerkWebhookController.java
│   ├── contenido/
│   │   └── ContenidoEducativoController.java
│   ├── eco/
│   │   ├── CanjeController.java
│   │   ├── EcoPuntosController.java
│   │   └── RecompensaController.java
│   ├── health/
│   │   └── HealthController.java
│   ├── impacto/
│   │   └── ImpactoAmbientalController.java
│   ├── inventario/
│   │   └── InventarioController.java
│   ├── marca/
│   │   └── MarcaController.java
│   ├── pago/
│   │   └── PagoController.java
│   ├── pedido/
│   │   └── PedidoController.java
│   ├── pedidoitem/
│   │   └── PedidoItemController.java
│   ├── producto/
│   │   └── ProductoController.java
│   └── usuario/
│       └── UsuarioController.java
├── domain/                   # Entidades JPA
│   ├── Canje.java
│   ├── Carrito.java
│   ├── CarritoItem.java
│   ├── Categoria.java
│   ├── Certificacion.java
│   ├── ContenidoEducativo.java
│   ├── Marca.java
│   ├── Pedido.java
│   ├── PedidoItem.java
│   ├── Producto.java
│   ├── Recompensa.java
│   ├── StockHistorial.java
│   └── Usuario.java
├── dto/                      # Data Transfer Objects (organizados por dominio)
│   ├── Canje/
│   ├── Carrito/
│   ├── Categoria/
│   ├── Certificacion/
│   ├── Checkout/
│   ├── clerk/
│   ├── ContenidoEducativo/
│   ├── EcoPuntos/
│   ├── ImpactoAmbiental/
│   ├── Inventario/
│   ├── Marca/
│   ├── Pago/
│   ├── Pedido/
│   ├── PedidoItem/
│   ├── Producto/
│   ├── Recompensa/
│   └── Usuario/
├── enums/                    # Enumeraciones
│   └── EcoBadge.java
├── exception/                # Manejo de excepciones
│   ├── BadRequestException.java
│   ├── ForbiddenException.java
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── mapper/                   # Mappers entre DTOs y Entidades
│   ├── CanjeMapper.java
│   ├── CarritoMapper.java
│   ├── CategoriaMapper.java
│   ├── CertificacionMapper.java
│   ├── ContenidoEducativoMapper.java
│   ├── MarcaMapper.java
│   ├── PedidoItemMapper.java
│   ├── PedidoMapper.java
│   ├── ProductoMapper.java
│   ├── RecompensaMapper.java
│   └── UsuarioMapper.java
├── repository/               # Repositorios JPA (organizados por dominio)
│   ├── carrito/
│   ├── categoria/
│   ├── certificacion/
│   ├── contenido/
│   ├── eco/
│   ├── marca/
│   ├── pedido/
│   ├── pedidoitem/
│   ├── producto/
│   ├── stockhistorial/
│   └── usuario/
├── security/
│   └── clerk/
│       └── ClerkJwtAuthenticationConverter.java  # Convertidor JWT → Authentication
└── service/                  # Lógica de negocio (organizados por dominio)
    ├── carrito/
    ├── categoria/
    ├── certificacion/
    ├── checkout/
    ├── contenido/
    ├── eco/
    ├── impacto/
    ├── inventario/
    ├── marca/
    ├── pago/
    ├── pedido/
    ├── pedidoitem/
    ├── producto/
    └── usuario/
```

## 🗄️ Base de Datos

### Tablas Principales

- `usuarios`: Usuarios del sistema (vinculados con Clerk mediante `clerk_id`)
- `marcas`: Marcas de productos
- `productos`: Productos con métricas ambientales
- `categorias`: Categorías de productos
- `certificaciones`: Certificaciones ambientales
- `producto_certificaciones`: Tabla intermedia (relación many-to-many)
- `pedidos`: Pedidos de clientes
- `pedido_items`: Items de cada pedido
- `carritos`: Carritos de compras de usuarios autenticados
- `carrito_items`: Items de cada carrito
- `recompensas`: Catálogo de recompensas del Eco-Wallet
- `canjes`: Historial de canjes de recompensas
- `contenido_educativo`: Contenido educativo sobre sostenibilidad
- `stock_historial`: Historial de cambios de stock

### Configuración

- Las tablas se crean automáticamente mediante JPA con `ddl-auto: update`
- En producción, usar `ddl-auto: validate` o migraciones con Flyway/Liquibase
- PostgreSQL 14+ recomendado

### Relaciones

- **Usuario ↔ Marca**: One-to-Many (un usuario puede tener múltiples marcas)
- **Usuario ↔ Pedido**: One-to-Many (un usuario puede tener múltiples pedidos)
- **Usuario ↔ Carrito**: One-to-One (un usuario tiene un carrito)
- **Usuario ↔ Canje**: One-to-Many (un usuario puede tener múltiples canjes)
- **Marca ↔ Producto**: One-to-Many (una marca puede tener múltiples productos)
- **Categoria ↔ Producto**: Many-to-One (una categoría puede tener múltiples productos)
- **Producto ↔ Certificacion**: Many-to-Many (tabla intermedia `producto_certificaciones`)
- **Pedido ↔ PedidoItem**: One-to-Many (un pedido tiene múltiples items)
- **Producto ↔ PedidoItem**: Many-to-One (un producto puede estar en múltiples items)
- **Carrito ↔ CarritoItem**: One-to-Many (un carrito tiene múltiples items)
- **Producto ↔ CarritoItem**: Many-to-One (un producto puede estar en múltiples items del carrito)
- **Recompensa ↔ Canje**: One-to-Many (una recompensa puede tener múltiples canjes)
- **Producto ↔ StockHistorial**: One-to-Many (un producto tiene múltiples registros de historial)

## 📡 Endpoints Disponibles

### Health Check

- [x] `GET /api/v1/health` - Verificar estado de la API (público)

### Productos

- [x] `GET /api/v1/productos` - Obtener todos los productos con paginación y filtros (público)
  - Query params: `categoria`, `ecoBadge`, `marca`, `material`, `origen`, `precioMin`, `precioMax`, `certificaciones`, `ordenar`, `pagina` (default: 1), `tamano` (default: 6)
- [x] `GET /api/v1/productos/{id}` - Obtener producto por ID (público)
- [x] `GET /api/v1/productos/marca/{marcaId}` - Obtener productos por marca (público)
- [x] `GET /api/v1/productos/buscar?q={query}` - Buscar productos por nombre (público, paginado)
- [x] `GET /api/v1/productos/{id}/metricas-ambientales` - Obtener métricas ambientales detalladas (público)
- [x] `GET /api/v1/productos/{id}/trazabilidad` - Obtener trazabilidad completa del producto (público)
- [x] `GET /api/v1/productos/{id}/viaje` - Obtener información del viaje del producto (público)
- [x] `GET /api/v1/productos/{id}/sugerencias` - Obtener sugerencias de productos más sostenibles (público)
- [x] `GET /api/v1/productos/comparar?ids={id1,id2,id3}` - Comparar múltiples productos (público)
- [x] `GET /api/v1/productos/filtros/disponibles` - Obtener opciones de filtros disponibles (público)
- [x] `POST /api/v1/productos` - Crear nuevo producto (requiere autenticación)
- [x] `PUT /api/v1/productos/{id}` - Actualizar producto (requiere autenticación)
- [x] `DELETE /api/v1/productos/{id}` - Eliminar producto (requiere autenticación)

### Certificaciones

- [x] `GET /api/v1/certificaciones` - Obtener todas las certificaciones (público)
- [x] `GET /api/v1/certificaciones/{id}` - Obtener certificación por ID (público)
- [x] `GET /api/v1/certificaciones/sello/{nombreSello}` - Obtener certificación por nombre de sello (público)
- [x] `POST /api/v1/certificaciones` - Crear nueva certificación (requiere autenticación)
- [x] `PUT /api/v1/certificaciones/{id}` - Actualizar certificación (requiere autenticación)
- [x] `DELETE /api/v1/certificaciones/{id}` - Eliminar certificación (requiere autenticación)

### Categorías

- [x] `GET /api/v1/categorias` - Obtener todas las categorías (público)
- [x] `GET /api/v1/categorias/{id}` - Obtener categoría por ID (público)
- [x] `POST /api/v1/categorias` - Crear nueva categoría (requiere autenticación)
- [x] `PUT /api/v1/categorias/{id}` - Actualizar categoría (requiere autenticación)
- [x] `DELETE /api/v1/categorias/{id}` - Eliminar categoría (requiere autenticación)

### Usuarios

- [x] `GET /api/v1/usuarios` - Obtener todos los usuarios (requiere autenticación)
- [x] `GET /api/v1/usuarios/{id}` - Obtener usuario por ID (requiere autenticación)
- [x] `GET /api/v1/usuarios/me` - Obtener usuario autenticado actual (requiere autenticación)
- [x] `GET /api/v1/usuarios/me/pedidos` - Obtener pedidos del usuario autenticado (requiere autenticación)
- [x] `GET /api/v1/usuarios/{id}/dashboard-impacto` - Obtener dashboard de impacto ambiental (requiere autenticación)
- [x] `GET /api/v1/usuarios/{id}/metricas-ambientales` - Obtener métricas ambientales agregadas (requiere autenticación)
- [x] `GET /api/v1/usuarios/{id}/historial-impacto` - Obtener historial de impacto ambiental (requiere autenticación)
- [x] `GET /api/v1/usuarios/{id}/tendencias-impacto` - Obtener tendencias de impacto ambiental (requiere autenticación)
- [x] `GET /api/v1/usuarios/{id}/objetivos` - Obtener objetivos de sostenibilidad (requiere autenticación)
- [x] `GET /api/v1/usuarios/{id}/objetivos/progreso` - Obtener progreso de objetivos (requiere autenticación)
- [x] `GET /api/v1/usuarios/{id}/eco-puntos` - Obtener estado de eco-puntos (requiere autenticación)
- [x] `GET /api/v1/usuarios/{id}/eco-puntos/historial` - Obtener historial de eco-puntos (requiere autenticación)
- [x] `POST /api/v1/usuarios` - Crear nuevo usuario (requiere autenticación)
- [x] `PUT /api/v1/usuarios/{id}` - Actualizar usuario (requiere autenticación)
- [x] `PUT /api/v1/usuarios/me` - Actualizar usuario autenticado actual (requiere autenticación)
- [x] `PUT /api/v1/usuarios/{id}/objetivos` - Actualizar objetivos de sostenibilidad (requiere autenticación)
- [x] `DELETE /api/v1/usuarios/{id}` - Eliminar usuario (requiere autenticación)

**Nota:** Los usuarios normalmente se crean mediante webhooks de Clerk cuando un usuario se registra en el frontend. Estos endpoints gestionan los datos del usuario en la base de datos local.

### Marcas

- [x] `GET /api/v1/marcas` - Obtener todas las marcas (requiere autenticación)
- [x] `GET /api/v1/marcas/{id}` - Obtener marca por ID (requiere autenticación)
- [x] `GET /api/v1/marcas/buscar?q={query}` - Buscar marcas por nombre (público, paginado)
- [x] `GET /api/v1/marcas/{id}/dashboard` - Obtener dashboard de métricas de marca (requiere autenticación)
- [x] `GET /api/v1/marcas/{id}/metricas-ventas` - Obtener métricas de ventas (requiere autenticación)
- [x] `GET /api/v1/marcas/{id}/metricas-ambientales` - Obtener métricas ambientales (requiere autenticación)
- [x] `GET /api/v1/marcas/{id}/productos/estadisticas` - Obtener estadísticas de productos (requiere autenticación)
- [x] `POST /api/v1/marcas` - Crear nueva marca (requiere autenticación, `usuarioId` se obtiene automáticamente del JWT)
- [x] `PUT /api/v1/marcas/{id}` - Actualizar marca (requiere autenticación, `usuarioId` se obtiene automáticamente del JWT)
- [x] `DELETE /api/v1/marcas/{id}` - Eliminar marca (requiere autenticación)

### Pedidos

- [x] `GET /api/v1/pedidos` - Obtener todos los pedidos (requiere autenticación)
- [x] `GET /api/v1/pedidos/{id}` - Obtener pedido por ID (requiere autenticación)
- [x] `GET /api/v1/pedidos/usuario/{usuarioId}` - Obtener pedidos de un usuario (requiere autenticación)
- [x] `GET /api/v1/pedidos/{id}/impacto-ambiental` - Obtener impacto ambiental del pedido (requiere autenticación)
- [x] `POST /api/v1/pedidos` - Crear nuevo pedido (requiere autenticación, `usuarioId` se obtiene automáticamente del JWT)
- [x] `PUT /api/v1/pedidos/{id}` - Actualizar pedido completo (requiere autenticación, `usuarioId` se obtiene automáticamente del JWT)
- [x] `PATCH /api/v1/pedidos/{id}/estado?estado={estado}` - Actualizar solo el estado del pedido (requiere autenticación)
- [x] `DELETE /api/v1/pedidos/{id}` - Eliminar pedido (requiere autenticación)

### Items de Pedido

- [x] `GET /api/v1/pedido-items/pedido/{pedidoId}` - Obtener todos los items de un pedido (requiere autenticación)
- [x] `POST /api/v1/pedido-items` - Agregar item a un pedido (requiere autenticación)
- [x] `PUT /api/v1/pedido-items/{itemId}?cantidad={cantidad}` - Actualizar cantidad de un item (requiere autenticación)
- [x] `DELETE /api/v1/pedido-items/{itemId}` - Eliminar item de un pedido (requiere autenticación)

### Carrito de Compras

- [x] `GET /api/v1/carrito` - Obtener carrito del usuario autenticado (requiere autenticación)
- [x] `GET /api/v1/carrito/impacto-ambiental` - Calcular impacto ambiental del carrito (requiere autenticación)
- [x] `POST /api/v1/carrito/items` - Agregar item al carrito (requiere autenticación)
- [x] `PUT /api/v1/carrito/items/{itemId}?cantidad={cantidad}` - Actualizar cantidad de item (requiere autenticación)
- [x] `DELETE /api/v1/carrito/items/{itemId}` - Eliminar item del carrito (requiere autenticación)
- [x] `DELETE /api/v1/carrito` - Vaciar carrito (requiere autenticación)

### Checkout

- [x] `POST /api/v1/checkout/calcular-impacto` - Calcular impacto ambiental completo antes de finalizar compra (requiere autenticación)

### Impacto Ambiental

- [x] `GET /api/v1/impacto-ambiental/producto/{productoId}` - Calcular impacto ambiental de un producto (público)
- [x] `GET /api/v1/impacto-ambiental/pedido/{pedidoId}` - Calcular impacto ambiental de un pedido (requiere autenticación)
- [x] `GET /api/v1/impacto-ambiental/comparar?productoIds={id1,id2,id3}` - Comparar impacto de múltiples productos (público)
- [x] `POST /api/v1/impacto-ambiental/calcular?productoId={id}` - Calcular impacto ambiental (público)

### Eco-Puntos

- [x] `POST /api/v1/eco-puntos/calcular?pedidoId={id}` - Calcular eco-puntos que se ganarían con un pedido (requiere autenticación)
- [x] `GET /api/v1/usuarios/{id}/eco-puntos` - Obtener estado actual de eco-puntos (requiere autenticación)
- [x] `GET /api/v1/usuarios/{id}/eco-puntos/historial` - Obtener historial de eco-puntos (requiere autenticación)

### Recompensas (Eco-Wallet)

- [x] `GET /api/v1/recompensas` - Obtener todas las recompensas (público)
- [x] `GET /api/v1/recompensas/{id}` - Obtener recompensa por ID (público)
- [x] `GET /api/v1/recompensas/activas` - Obtener recompensas activas (público)
- [x] `GET /api/v1/recompensas/tipo/{tipo}` - Obtener recompensas por tipo (público)
- [x] `GET /api/v1/recompensas/disponibles?puntos={puntos}` - Obtener recompensas disponibles por puntos (público)
- [x] `POST /api/v1/recompensas` - Crear nueva recompensa (requiere autenticación)
- [x] `PUT /api/v1/recompensas/{id}` - Actualizar recompensa (requiere autenticación)
- [x] `DELETE /api/v1/recompensas/{id}` - Eliminar recompensa (requiere autenticación)

### Canjes (Eco-Wallet)

- [x] `GET /api/v1/canjes/{id}` - Obtener canje por ID (requiere autenticación)
- [x] `GET /api/v1/canjes/usuario/{usuarioId}` - Obtener canjes de un usuario (requiere autenticación)
- [x] `GET /api/v1/canjes/usuario/{usuarioId}/estado/{estado}` - Obtener canjes por estado (requiere autenticación)
- [x] `GET /api/v1/canjes/usuario/{usuarioId}/fecha?fechaInicio={date}&fechaFin={date}` - Obtener canjes por rango de fechas (requiere autenticación)
- [x] `POST /api/v1/canjes` - Realizar canje de recompensa (requiere autenticación)
- [x] `PATCH /api/v1/canjes/{id}/estado?estado={estado}` - Actualizar estado de canje (requiere autenticación, solo admin)

### Contenido Educativo

- [x] `GET /api/v1/contenido-educativo` - Obtener todos los contenidos (público)
- [x] `GET /api/v1/contenido-educativo/{id}` - Obtener contenido por ID (público)
- [x] `GET /api/v1/contenido-educativo/activos` - Obtener contenidos activos (público)
- [x] `GET /api/v1/contenido-educativo/categoria/{categoria}` - Obtener contenidos por categoría (público)
- [x] `GET /api/v1/contenido-educativo/buscar?titulo={titulo}` - Buscar contenidos por título (público)
- [x] `POST /api/v1/contenido-educativo` - Crear nuevo contenido (requiere autenticación)
- [x] `PUT /api/v1/contenido-educativo/{id}` - Actualizar contenido (requiere autenticación)
- [x] `DELETE /api/v1/contenido-educativo/{id}` - Eliminar contenido (requiere autenticación)

### Inventario Avanzado

- [x] `GET /api/v1/inventario/marca/{marcaId}/stock-bajo?umbralStock={umbral}` - Obtener productos con stock bajo (requiere autenticación, solo propietario o admin)
- [x] `GET /api/v1/inventario/producto/{productoId}/historial` - Obtener historial completo de stock (requiere autenticación)
- [x] `GET /api/v1/inventario/producto/{productoId}/historial/fecha?fechaInicio={date}&fechaFin={date}` - Obtener historial por rango de fechas (requiere autenticación)
- [x] `GET /api/v1/inventario/producto/{productoId}/prediccion-demanda` - Obtener predicción de demanda (requiere autenticación)
- [x] `PUT /api/v1/inventario/producto/{productoId}/stock` - Actualizar stock con registro automático (requiere autenticación, solo propietario o admin)

### Pagos

- [x] `POST /api/v1/pagos/crear` - Crear pago genérico (requiere autenticación)
- [x] `GET /api/v1/pagos/verificar/{pedidoId}` - Verificar estado de pago (requiere autenticación)

#### Stripe

- [x] `POST /api/v1/pagos/stripe/crear-intento` - Crear intento de pago con Stripe (requiere autenticación)
- [x] `POST /api/v1/pagos/stripe/confirmar` - Confirmar pago de Stripe (requiere autenticación)
- [x] `POST /api/v1/pagos/stripe/webhook` - Webhook de Stripe (público, verifica firma)

#### PayPal

- [x] `POST /api/v1/pagos/paypal/crear-orden` - Crear orden de PayPal (requiere autenticación)
- [x] `POST /api/v1/pagos/paypal/capturar` - Capturar pago de PayPal (requiere autenticación)

#### MercadoPago

- [x] `POST /api/v1/pagos/mercadopago/crear-preferencia` - Crear preferencia de pago (requiere autenticación)
- [x] `POST /api/v1/pagos/mercadopago/webhook` - Webhook de MercadoPago (público)

### Clerk - Configuración y Testing

- [x] `GET /api/v1/config/clerk` - Verificar configuración de Clerk (público)
- [x] `GET /api/v1/test/clerk/check-config` - Verificar conectividad con API de Clerk (público, solo desarrollo)
- [x] `GET /api/v1/test/clerk/users?limit={limit}` - Listar usuarios de Clerk (público, solo desarrollo) ⚠️ **Solo para desarrollo**
- [x] `POST /api/v1/test/clerk/create-user` - Crear usuario de prueba en Clerk y BD local (público, solo desarrollo) ⚠️ **Solo para desarrollo**

**Nota:** Los endpoints de test (`/api/v1/test/clerk/**`) son únicamente para desarrollo/testing. En producción, los usuarios se crean normalmente a través del frontend con Clerk, y luego Clerk envía un webhook para sincronizarlos. Estos endpoints NO son necesarios si ya tienes frontend funcionando.

### Clerk - Webhooks

- [x] `POST /api/v1/webhooks/clerk` - Recibir webhooks de Clerk (público, pero debe verificar firma) ✅ **NECESARIO**

**Nota:** Este endpoint es **obligatorio** para la integración con Clerk. Clerk lo llama automáticamente cuando se crean, actualizan o eliminan usuarios. Maneja los siguientes eventos:
- `user.created`: Crea un usuario en la BD local
- `user.updated`: Actualiza un usuario en la BD local
- `user.deleted`: Elimina un usuario de la BD local

**✅ IMPORTANTE:** La verificación de firma del webhook está implementada usando HMAC SHA256.

## 📝 Ejemplos de Uso

### Autenticación

Todos los endpoints protegidos requieren un token JWT de Clerk en el header:

```
Authorization: Bearer <tu_token_jwt>
```

Para obtener un token JWT:
1. Usuario se autentica en el frontend con Clerk
2. Frontend obtiene el token de la sesión de Clerk
3. Frontend envía el token en cada request al backend

### Crear Certificación

```json
POST /api/v1/certificaciones
Content-Type: application/json
Authorization: Bearer <token_jwt>

{
  "nombreSello": "Fair Trade",
  "descripcion": "Certificación que garantiza condiciones de comercio justo",
  "entidadEmisora": "Fair Trade International"
}
```

**Respuesta:**
```json
{
  "certificacionId": 1,
  "nombreSello": "Fair Trade",
  "descripcion": "Certificación que garantiza condiciones de comercio justo",
  "entidadEmisora": "Fair Trade International"
}
```

### Crear Producto

```json
POST /api/v1/productos
Content-Type: application/json
Authorization: Bearer <token_jwt>

{
  "marcaId": 1,
  "nombre": "Botella reutilizable EcoLife 500ml",
  "descripcion": "Botella de acero inoxidable con capacidad de 500ml",
  "precio": 14990.00,
  "stock": 50,
  "sku": "BOT-ECO-500",
  "materiales": "Acero inoxidable",
  "origen": "Chile",
  "huellaCarbonoTotal": 0.8,
  "porcentajeReciclable": 100,
  "ecoBadge": "bajo_impacto",
  "imagenUrl": "https://example.com/botella.jpg",
  "certificaciones": [1, 2]
}
```

**Nota sobre certificaciones:** En POST/PUT, envía un array de IDs de certificaciones (números enteros). En GET, las certificaciones se devuelven como nombres de sellos (strings) para facilitar el uso en el frontend.

**Respuesta:**
```json
{
  "productoId": 1,
  "marcaId": 1,
  "nombreMarca": "EcoLife",
  "nombre": "Botella reutilizable EcoLife 500ml",
  "descripcion": "Botella de acero inoxidable con capacidad de 500ml",
  "precio": 14990.00,
  "stock": 50,
  "sku": "BOT-ECO-500",
  "materiales": "Acero inoxidable",
  "origen": "Chile",
  "huellaCarbonoTotal": 0.8,
  "porcentajeReciclable": 100,
  "ecoBadge": "bajo_impacto",
  "imagenUrl": "https://example.com/botella.jpg",
  "activo": true,
  "fechaCreacion": "2024-12-03T20:00:00",
  "certificaciones": ["Fair Trade", "Carbon Neutral"]
}
```

### Crear Marca

```json
POST /api/v1/marcas
Content-Type: application/json
Authorization: Bearer <token_jwt>

{
  "nombreOficial": "EcoLife",
  "descripcionSostenible": "Marca comprometida con la sostenibilidad",
  "sitioWeb": "https://ecolife.com",
  "logoUrl": "https://example.com/logo.png"
}
```

**Nota:** El `usuarioId` se obtiene automáticamente del token JWT, no es necesario incluirlo en el body.

### Crear Pedido

```json
POST /api/v1/pedidos
Content-Type: application/json
Authorization: Bearer <token_jwt>

{
  "estado": "pendiente_pago",
  "direccionEnvio": "Av. Principal 123, Santiago, Chile",
  "metodoPago": "tarjeta_credito"
}
```

**Nota:** El `usuarioId` se obtiene automáticamente del token JWT. Después de crear el pedido, agrega items usando el endpoint de pedido-items.

### Actualizar Producto (Actualización Parcial)

```json
PUT /api/v1/productos/1
Content-Type: application/json
Authorization: Bearer <token_jwt>

{
  "nombre": "Botella Reutilizable EcoLife - Actualizada",
  "precio": 15990.00,
  "stock": 45
}
```

**Nota:** Solo los campos enviados se actualizan. Los campos no enviados mantienen su valor anterior (no se ponen en null).

## 🔒 Seguridad

### Configuración Actual

- **CSRF**: Deshabilitado (no necesario para APIs REST con JWT)
- **CORS**: Habilitado para todos los orígenes (`*`) - **Cambiar en producción**
- **Sesiones**: Stateless (cada request es independiente)
- **Autenticación**: JWT mediante Spring Security OAuth2 Resource Server
- **Validación de Tokens**: Automática mediante JWKS de Clerk
- **Autorización**: Validación granular de permisos (usuarios solo acceden a sus datos, marcas solo a sus productos, admins acceso completo)
- **Verificación de Webhooks**: Implementada con HMAC SHA256 para Clerk y Stripe

### Endpoints Públicos

Los siguientes endpoints no requieren autenticación:

#### Lectura Pública
- `GET /api/v1/health`
- `GET /api/v1/productos/**` (todos los endpoints de lectura)
- `GET /api/v1/certificaciones/**` (todos los endpoints de lectura)
- `GET /api/v1/categorias/**` (todos los endpoints de lectura)
- `GET /api/v1/marcas/buscar` (búsqueda pública)
- `GET /api/v1/impacto-ambiental/**` (cálculos públicos)
- `GET /api/v1/recompensas/**` (catálogo público)
- `GET /api/v1/contenido-educativo/**` (todos los endpoints de lectura)

#### Webhooks y Configuración
- `POST /api/v1/webhooks/clerk` (público pero verifica firma HMAC SHA256)
- `POST /api/v1/pagos/stripe/webhook` (público pero verifica firma)
- `POST /api/v1/pagos/mercadopago/webhook` (público)
- `GET /api/v1/config/clerk`
- `GET /api/v1/test/clerk/**` (solo desarrollo)

### Endpoints Protegidos

Todos los siguientes endpoints requieren un token JWT válido de Clerk:

#### Gestión de Datos (CRUD)
- `POST /api/v1/productos` y modificaciones (`PUT`, `DELETE`)
- `POST /api/v1/certificaciones` y modificaciones
- `POST /api/v1/categorias` y modificaciones
- Todos los endpoints de `/api/v1/usuarios/**` (excepto lectura pública si aplica)
- Todos los endpoints de `/api/v1/marcas/**` (excepto búsqueda)
- Todos los endpoints de `/api/v1/pedidos/**`
- Todos los endpoints de `/api/v1/pedido-items/**`
- Todos los endpoints de `/api/v1/carrito/**`
- Todos los endpoints de `/api/v1/checkout/**`
- Todos los endpoints de `/api/v1/eco-puntos/**`
- Todos los endpoints de `/api/v1/canjes/**`
- `POST /api/v1/recompensas` y modificaciones
- `POST /api/v1/contenido-educativo` y modificaciones
- Todos los endpoints de `/api/v1/inventario/**`
- Todos los endpoints de `/api/v1/pagos/**` (excepto webhooks)

#### Autorización Granular

- **Usuarios**: Solo pueden acceder a sus propios datos (validado por `usuarioId` del JWT)
- **Marcas**: Solo pueden gestionar sus propios productos y ver sus métricas
- **Administradores**: Acceso completo a todos los recursos
- **Canjes**: Usuarios solo pueden ver sus propios canjes, admins pueden ver todos
- **Inventario**: Solo propietarios de marca o admins pueden gestionar stock

### Importante para Producción

1. **CORS**: Configurar dominios específicos en `SecurityConfig.java`:
   ```java
   configuration.setAllowedOrigins(Arrays.asList("https://tu-dominio.com"));
   ```

2. **Verificación de Webhooks**: ✅ Implementada con HMAC SHA256 para Clerk y Stripe

3. **Variables de Entorno**: Todas las claves secretas deben estar en variables de entorno, nunca en código

4. **HTTPS**: Configurar HTTPS en producción (obligatorio para webhooks)

5. **Rate Limiting**: Considerar implementar rate limiting para prevenir abuso

6. **Validación de Input**: Todos los DTOs usan Bean Validation (`@Valid`)

7. **SQL Injection**: Prevenido mediante uso de JPA/Hibernate (prepared statements)

8. **XSS**: Prevenido mediante serialización JSON segura

## 🎯 Características Principales

### Módulo de Productos

- CRUD completo de productos
- Relación many-to-many con certificaciones
- Relación many-to-one con categorías
- Métricas ambientales (huella de carbono, porcentaje reciclable, eco-badge)
- Cálculo automático de eco-badge basado en múltiples factores
- Filtros avanzados (categoría, eco-badge, marca, material, origen, precio, certificaciones)
- Búsqueda por nombre con paginación
- Comparación de productos (hasta 4 productos)
- Sugerencias de productos más sostenibles
- Trazabilidad completa del producto (origen, transporte, empaque, entrega)
- Métricas ambientales detalladas (agua, CO₂ ahorrado, equivalencias)
- Actualización parcial (solo campos enviados se actualizan)
- Validaciones de negocio (precio, nombre, SKU único)
- Manejo de fechas de creación automático
- Paginación (6 productos por página, página inicial: 1)

### Módulo de Certificaciones

- CRUD completo de certificaciones
- Búsqueda por nombre de sello (case-insensitive)
- Relación many-to-many con productos
- Validación de unicidad

### Módulo de Categorías

- CRUD completo de categorías
- Relación one-to-many con productos
- Validación de nombre único

### Módulo de Usuarios

- CRUD completo de usuarios
- Integración con Clerk mediante `clerkId`
- Sincronización automática mediante webhooks
- Dashboard de impacto ambiental personalizado
- Métricas ambientales agregadas (CO₂ ahorrado, agua ahorrada, compras sostenibles)
- Historial de impacto ambiental con fechas formateadas (dd/MM/yyyy)
- Tendencias de impacto ambiental (agregación por día, semana, mes)
- Sistema de eco-puntos con niveles y metas
- Objetivos de sostenibilidad personalizables
- Endpoints `/me` para usuario autenticado
- Validación de relaciones antes de eliminar (marcas, pedidos)
- Autorización granular (usuarios solo acceden a sus datos)

### Módulo de Marcas

- CRUD completo de marcas
- Asociación automática con usuario autenticado
- `usuarioId` se obtiene automáticamente del JWT
- Dashboard de métricas de marca
- Métricas de ventas (total vendido, productos más vendidos)
- Métricas ambientales agregadas
- Estadísticas de productos
- Búsqueda por nombre con paginación
- Autorización granular (marcas solo gestionan sus productos)

### Módulo de Pedidos

- CRUD completo de pedidos
- Gestión de estados (pendiente_pago, procesando, enviado, entregado, cancelado)
- Asociación automática con usuario autenticado
- Cálculo automático de huella de carbono total
- Cálculo automático de CO₂ ahorrado vs productos convencionales
- Cálculo automático de agua ahorrada
- Cálculo automático de eco-puntos ganados
- Impacto ambiental detallado del pedido
- Actualización parcial del estado mediante PATCH
- Integración con pasarelas de pago (Stripe, PayPal, MercadoPago)

### Módulo de Items de Pedido

- CRUD completo de items de pedido
- Relación con productos y pedidos
- Cálculo de subtotales automático
- Actualización de cantidad mediante query parameter
- Actualización automática de stock al crear/modificar/eliminar items
- Restauración de stock al cancelar pedido

### Módulo de Carrito de Compras

- Carrito persistente para usuarios autenticados
- CRUD completo de items del carrito
- Cálculo automático de impacto ambiental del carrito
- Actualización de cantidad de items
- Vaciar carrito completo
- Persistencia en base de datos (no se pierde al cerrar sesión)

### Módulo de Checkout

- Cálculo de impacto ambiental completo antes de finalizar compra
- Cálculo de eco-puntos a ganar
- Equivalencias en términos comprensibles (árboles, duchas, etc.)

### Módulo de Impacto Ambiental

- Cálculo detallado de huella de carbono por producto
- Cálculo detallado de huella de carbono por pedido
- Comparación de impacto entre múltiples productos
- Métricas ambientales detalladas (agua, CO₂, equivalencias)
- Cálculo automático de emisiones por etapa (manufactura, materiales, transporte, empaque, entrega)

### Módulo de Eco-Puntos

- Sistema de puntos basado en compras sostenibles
- Cálculo de puntos antes de finalizar compra
- Historial de puntos ganados
- Niveles de eco-puntos
- Metas personalizables

### Módulo de Eco-Wallet (Recompensas y Canjes)

- Catálogo de recompensas canjeables
- Gestión de stock de recompensas
- Filtrado por tipo y puntos disponibles
- Historial de canjes
- Códigos únicos de canje
- Estados de canje (pendiente, completado, cancelado, expirado)
- Autorización granular (usuarios solo ven sus canjes)

### Módulo de Contenido Educativo

- CRUD completo de contenido educativo
- Categorización de contenido
- Búsqueda por título
- Filtrado por categoría
- Contenidos activos/inactivos

### Módulo de Inventario Avanzado

- Alertas de stock bajo por marca
- Historial completo de cambios de stock
- Historial filtrado por rango de fechas
- Predicción de demanda basada en ventas recientes
- Actualización de stock con registro automático en historial
- Tipos de movimiento (entrada, salida, ajuste)
- Motivos de cambio de stock
- Autorización granular (solo propietarios de marca o admins)

### Módulo de Pagos

- Integración con Stripe (tarjetas de crédito/débito)
- Integración con PayPal
- Integración con MercadoPago
- Creación de intents/órdenes/preferencias de pago
- Confirmación de pagos
- Webhooks con verificación de firma
- Verificación de estado de pago
- Actualización automática de estado del pedido

## 🔍 Validaciones

### ProductoRequestDTO

- `marcaId`: Obligatorio (debe existir)
- `categoriaId`: Opcional (debe existir si se proporciona)
- `nombre`: Obligatorio, máximo 200 caracteres
- `precio`: Obligatorio, mayor a 0
- `stock`: Obligatorio, mínimo 0
- `sku`: Opcional, único si se proporciona
- `certificaciones`: Lista de IDs de certificaciones (deben existir)
- `huellaCarbonoTotal`: Opcional, debe ser >= 0
- `porcentajeReciclable`: Opcional, rango 0-100
- `ecoBadge`: Opcional, valores: muy_bajo_impacto, bajo_impacto, medio_impacto, alto_impacto

### CertificacionRequestDTO

- `nombreSello`: Obligatorio, único
- `descripcion`: Opcional
- `entidadEmisora`: Opcional

### CategoriaRequestDTO

- `nombre`: Obligatorio, único
- `descripcion`: Opcional

### UsuarioRequestDTO

- `email`: Obligatorio, formato válido, único
- `rol`: Obligatorio (cliente, marca, admin)
- `clerkId`: Opcional (se sincroniza desde Clerk)
- `nombre`: Opcional
- `apellido`: Opcional

### MarcaRequestDTO

- `nombreOficial`: Obligatorio, máximo 150 caracteres
- `usuarioId`: Opcional (se obtiene automáticamente del JWT)
- `descripcionSostenible`: Opcional
- `sitioWeb`: Opcional, formato URL válido
- `logoUrl`: Opcional, formato URL válido

### PedidoRequestDTO

- `estado`: Opcional, valores válidos: pendiente_pago, procesando, enviado, entregado, cancelado
- `direccionEnvio`: Obligatorio al crear, opcional al actualizar
- `metodoPago`: Opcional (stripe, paypal, mercadopago)
- `usuarioId`: Opcional (se obtiene automáticamente del JWT)

### PedidoItemRequestDTO

- `pedidoId`: Obligatorio (debe existir)
- `productoId`: Obligatorio (debe existir)
- `cantidad`: Obligatorio, mínimo 1
- `precioUnitario`: Opcional (se obtiene del producto si no se proporciona)

### CarritoItemRequestDTO

- `productoId`: Obligatorio (debe existir)
- `cantidad`: Obligatorio, mínimo 1

### RecompensaRequestDTO

- `nombre`: Obligatorio
- `descripcion`: Opcional
- `puntosRequeridos`: Obligatorio, mínimo 1
- `tipo`: Obligatorio (descuento, envio_gratis, producto, etc.)
- `valor`: Opcional (depende del tipo)
- `stockDisponible`: Opcional, mínimo 0
- `activo`: Opcional, default true

### CanjeRequestDTO

- `recompensaId`: Obligatorio (debe existir y estar activa)

### ContenidoEducativoRequestDTO

- `titulo`: Obligatorio
- `descripcion`: Opcional
- `contenido`: Obligatorio
- `categoria`: Opcional
- `activo`: Opcional, default true

### ActualizarStockRequestDTO

- `nuevoStock`: Obligatorio, mínimo 0
- `tipoMovimiento`: Obligatorio (entrada, salida, ajuste)
- `motivo`: Opcional

## ⚠️ Manejo de Errores

El sistema incluye un `GlobalExceptionHandler` que maneja:

- **400 Bad Request**: Validaciones de negocio (ej: recurso no encontrado, datos inválidos)
- **404 Not Found**: Recurso no encontrado (ej: producto o usuario inexistente)
- **400 Validation Failed**: Errores de validación de campos (Bean Validation)
- **401 Unauthorized**: Token JWT inválido o usuario no encontrado en BD
- **500 Internal Server Error**: Errores inesperados del servidor

Ejemplo de respuesta de error:
```json
{
  "timestamp": "2024-12-03T20:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Producto no encontrado con id: 999"
}
```

## 📌 Notas Importantes

### Autenticación con Clerk

- El `usuarioId` se obtiene automáticamente del token JWT para los endpoints de **Marcas** y **Pedidos**, no es necesario incluirlo en el body
- Si un token JWT es válido pero el usuario no existe en la BD local, se lanza una excepción 401
- Los usuarios se sincronizan automáticamente mediante webhooks cuando se registran en Clerk

### Diferencias entre Endpoints

- **Endpoints de Test (`/api/v1/test/clerk/**`)**: Solo para desarrollo. Permiten crear usuarios directamente en Clerk. NO necesarios en producción si ya tienes frontend.
- **Endpoints de Usuarios (`/api/v1/usuarios/**`)**: Para producción. Gestionan usuarios en la BD local. Requieren autenticación.
- **Webhooks (`/api/v1/webhooks/clerk`)**: Obligatorio. Clerk lo llama automáticamente para sincronizar usuarios. NO es para uso directo del cliente.

### Actualización Parcial

- En `PUT /api/v1/productos/{id}`, solo los campos enviados se actualizan
- Los campos no enviados mantienen su valor anterior (no se ponen en null)
- Esto permite actualizaciones parciales sin perder información

### Certificaciones en Productos

- **POST/PUT**: Envía un array de IDs de certificaciones: `"certificaciones": [1, 2, 3]`
- **GET**: Recibe un array de nombres de sellos: `"certificaciones": ["Fair Trade", "Carbon Neutral"]`
- Si envías `certificaciones: null` en PUT, se preservan las certificaciones existentes
- Si envías `certificaciones: []`, se eliminan todas las certificaciones

## 📚 Ejemplos de Endpoints

### Health Check

**GET /api/v1/health**
```http
GET /api/v1/health
```

**Respuesta:**
```
EcoShop API OK
```

### Productos

**GET /api/v1/productos** (con filtros y paginación)
```http
GET /api/v1/productos?categoria=1&ecoBadge=bajo_impacto&precioMin=10000&precioMax=50000&pagina=1&tamano=6&ordenar=precio_asc
```

**Respuesta:**
```json
{
  "productos": [...],
  "paginaActual": 1,
  "totalPaginas": 5,
  "totalElementos": 30,
  "tamanoPagina": 6
}
```

**GET /api/v1/productos/{id}/metricas-ambientales**
```http
GET /api/v1/productos/1/metricas-ambientales
```

**Respuesta:**
```json
{
  "productoId": 1,
  "huellaCarbonoTotal": 0.8,
  "aguaConsumida": 50.0,
  "co2Ahorrado": 0.3,
  "aguaAhorrada": 20.0,
  "equivalenciaArboles": 0.03,
  "equivalenciaDuchas": 0.2
}
```

**GET /api/v1/productos/{id}/trazabilidad**
```http
GET /api/v1/productos/1/trazabilidad
```

**Respuesta:**
```json
{
  "productoId": 1,
  "origenPais": "Chile",
  "distanciaTransporte": 500.0,
  "etapas": [
    {
      "etapa": "Origen",
      "emisionesCO2": 0.2,
      "descripcion": "Producción en Chile"
    },
    {
      "etapa": "Empaque",
      "emisionesCO2": 0.1,
      "descripcion": "Empaque sostenible"
    }
  ],
  "huellaCarbonoTotal": 0.8
}
```

**GET /api/v1/productos/comparar?ids=1,2,3**
```http
GET /api/v1/productos/comparar?ids=1,2,3
```

**Respuesta:**
```json
{
  "productos": [
    {
      "productoId": 1,
      "nombre": "Producto A",
      "huellaCarbono": 0.8,
      "precio": 15000.0
    }
  ],
  "productoRecomendado": {
    "productoId": 1,
    "razon": "Mejor balance precio-impacto"
  }
}
```

**POST /api/v1/productos**
```http
POST /api/v1/productos
Content-Type: application/json
Authorization: Bearer <token_jwt>

{
  "marcaId": 1,
  "categoriaId": 2,
  "nombre": "Botella reutilizable EcoLife 500ml",
  "descripcion": "Botella de acero inoxidable",
  "precio": 14990.00,
  "stock": 50,
  "sku": "BOT-ECO-500",
  "materiales": "Acero inoxidable",
  "origen": "Chile",
  "huellaCarbonoTotal": 0.8,
  "porcentajeReciclable": 100,
  "certificaciones": [1, 2]
}
```

### Usuarios

**GET /api/v1/usuarios/me**
```http
GET /api/v1/usuarios/me
Authorization: Bearer <token_jwt>
```

**GET /api/v1/usuarios/{id}/dashboard-impacto**
```http
GET /api/v1/usuarios/1/dashboard-impacto
Authorization: Bearer <token_jwt>
```

**Respuesta:**
```json
{
  "usuarioId": 1,
  "totalCO2Ahorrado": 15.5,
  "totalAguaAhorrada": 500.0,
  "comprasSostenibles": 12,
  "ecoPuntosTotales": 450,
  "nivelEcoPuntos": "Bronce",
  "objetivoCO2": 20.0,
  "progresoCO2": 77.5,
  "objetivoEcoPuntos": 500,
  "progresoEcoPuntos": 90.0
}
```

**GET /api/v1/usuarios/{id}/historial-impacto**
```http
GET /api/v1/usuarios/1/historial-impacto
Authorization: Bearer <token_jwt>
```

**Respuesta:**
```json
{
  "usuarioId": 1,
  "compras": [
    {
      "pedidoId": 1,
      "fechaPedido": "03/12/2024",
      "total": 29980.0,
      "co2Ahorrado": 1.2,
      "aguaAhorrada": 50.0,
      "ecoPuntosGanados": 30
    }
  ],
  "totalAcumulado": {
    "co2Ahorrado": 15.5,
    "aguaAhorrada": 500.0
  }
}
```

**PUT /api/v1/usuarios/{id}/objetivos**
```http
PUT /api/v1/usuarios/1/objetivos
Content-Type: application/json
Authorization: Bearer <token_jwt>

{
  "metaCO2": 25.0,
  "metaEcoPuntosPersonalizada": 600
}
```

### Carrito

**GET /api/v1/carrito**
```http
GET /api/v1/carrito
Authorization: Bearer <token_jwt>
```

**Respuesta:**
```json
{
  "carritoId": 1,
  "usuarioId": 1,
  "items": [
    {
      "itemId": 1,
      "productoId": 1,
      "nombreProducto": "Botella EcoLife",
      "cantidad": 2,
      "precioUnitario": 14990.0,
      "subtotal": 29980.0
    }
  ],
  "total": 29980.0
}
```

**POST /api/v1/carrito/items**
```http
POST /api/v1/carrito/items
Content-Type: application/json
Authorization: Bearer <token_jwt>

{
  "productoId": 1,
  "cantidad": 2
}
```

**GET /api/v1/carrito/impacto-ambiental**
```http
GET /api/v1/carrito/impacto-ambiental
Authorization: Bearer <token_jwt>
```

**Respuesta:**
```json
{
  "huellaCarbono": 1.6,
  "co2Ahorrado": 0.6,
  "co2AhorradoEquivalente": "Equivalente a 0.06 árboles plantados",
  "aguaAhorrada": 100.0,
  "aguaAhorradaEquivalente": "Equivalente a 1 ducha de 5 minutos"
}
```

### Checkout

**POST /api/v1/checkout/calcular-impacto**
```http
POST /api/v1/checkout/calcular-impacto
Authorization: Bearer <token_jwt>
```

**Respuesta:**
```json
{
  "huellaCarbono": 2.8,
  "co2Ahorrado": 1.1,
  "co2AhorradoEquivalente": "Equivalente a 0.1 árboles plantados",
  "ecoPuntosAGanar": 72,
  "aguaAhorrada": 150.0,
  "aguaAhorradaEquivalente": "Equivalente a 1.5 duchas de 5 minutos"
}
```

### Eco-Puntos

**POST /api/v1/eco-puntos/calcular?pedidoId=1**
```http
POST /api/v1/eco-puntos/calcular?pedidoId=1
Authorization: Bearer <token_jwt>
```

**Respuesta:**
```json
{
  "pedidoId": 1,
  "puntosAGanar": 72,
  "desglose": {
    "porMonto": 50,
    "porBajoImpacto": 15,
    "porMaterialReciclado": 5,
    "porCertificaciones": 2
  }
}
```

**GET /api/v1/usuarios/{id}/eco-puntos**
```http
GET /api/v1/usuarios/1/eco-puntos
Authorization: Bearer <token_jwt>
```

**Respuesta:**
```json
{
  "usuarioId": 1,
  "ecoPuntos": 450,
  "nivelEcoPuntos": "Bronce",
  "metaEcoPuntos": 500,
  "progresoPorcentaje": 90.0
}
```

### Recompensas

**GET /api/v1/recompensas/disponibles?puntos=150**
```http
GET /api/v1/recompensas/disponibles?puntos=150
```

**Respuesta:**
```json
[
  {
    "recompensaId": 1,
    "nombre": "Descuento 10%",
    "descripcion": "Descuento del 10% en tu próxima compra",
    "puntosRequeridos": 100,
    "tipo": "descuento",
    "valor": 10.0,
    "stockDisponible": 50,
    "activo": true
  }
]
```

### Canjes

**POST /api/v1/canjes**
```http
POST /api/v1/canjes
Content-Type: application/json
Authorization: Bearer <token_jwt>

{
  "recompensaId": 1
}
```

**Respuesta:**
```json
{
  "canjeId": 1,
  "usuarioId": 1,
  "recompensaId": 1,
  "nombreRecompensa": "Descuento 10%",
  "puntosGastados": 100,
  "codigoCanje": "CANJE-2024-001",
  "estado": "pendiente",
  "fechaCanje": "2024-12-03T20:00:00"
}
```

### Pagos

**POST /api/v1/pagos/stripe/crear-intento**
```http
POST /api/v1/pagos/stripe/crear-intento
Content-Type: application/json
Authorization: Bearer <token_jwt>

{
  "pedidoId": 1,
  "monto": 29980.0,
  "moneda": "CLP"
}
```

**Respuesta:**
```json
{
  "pedidoId": 1,
  "metodoPago": "stripe",
  "estadoPago": "procesando",
  "monto": 29980.0,
  "moneda": "CLP",
  "idTransaccionPago": "pi_xxxxx",
  "clientSecret": "pi_xxxxx_secret_xxxxx",
  "fechaCreacion": "2024-12-03T20:00:00"
}
```

**GET /api/v1/pagos/verificar/{pedidoId}**
```http
GET /api/v1/pagos/verificar/1
Authorization: Bearer <token_jwt>
```

### Inventario

**GET /api/v1/inventario/marca/{marcaId}/stock-bajo?umbralStock=10**
```http
GET /api/v1/inventario/marca/1/stock-bajo?umbralStock=10
Authorization: Bearer <token_jwt>
```

**Respuesta:**
```json
{
  "marcaId": 1,
  "umbralStock": 10,
  "productosStockBajo": [
    {
      "productoId": 1,
      "nombre": "Botella EcoLife",
      "stockActual": 5,
      "stockMinimo": 10,
      "diferencia": -5
    }
  ],
  "totalProductosBajoStock": 3
}
```

**GET /api/v1/inventario/producto/{productoId}/prediccion-demanda**
```http
GET /api/v1/inventario/producto/1/prediccion-demanda
Authorization: Bearer <token_jwt>
```

**Respuesta:**
```json
{
  "productoId": 1,
  "demandaPromedioMensual": 50,
  "tendencia": "creciente",
  "recomendacionReposicion": "Reponer 100 unidades",
  "stockActual": 45,
  "diasHastaAgotarse": 27
}
```

### Impacto Ambiental

**GET /api/v1/impacto-ambiental/producto/{productoId}**
```http
GET /api/v1/impacto-ambiental/producto/1
```

**Respuesta:**
```json
{
  "productoId": 1,
  "huellaCarbonoTotal": 0.8,
  "desglose": {
    "emisionesManufactura": 0.3,
    "emisionesMateriales": 0.2,
    "emisionesTransporte": 0.2,
    "emisionesEmpaque": 0.1,
    "emisionesEntrega": 0.0
  },
  "co2Ahorrado": 0.3,
  "aguaConsumida": 50.0,
  "aguaAhorrada": 20.0
}
```

**GET /api/v1/impacto-ambiental/pedido/{pedidoId}**
```http
GET /api/v1/impacto-ambiental/pedido/1
Authorization: Bearer <token_jwt>
```

**Respuesta:**
```json
{
  "pedidoId": 1,
  "huellaCarbonoTotal": 2.8,
  "co2Ahorrado": 1.1,
  "aguaAhorrada": 150.0,
  "productos": [
    {
      "productoId": 1,
      "cantidad": 2,
      "huellaCarbono": 1.6
    }
  ],
  "emisionesTransporte": 0.5,
  "emisionesEntrega": 0.7
}
```

### Contenido Educativo

**GET /api/v1/contenido-educativo/categoria/{categoria}**
```http
GET /api/v1/contenido-educativo/categoria/Consumo%20Responsable
```

**GET /api/v1/contenido-educativo/buscar?titulo=consumo**
```http
GET /api/v1/contenido-educativo/buscar?titulo=consumo
```

### Marcas

**GET /api/v1/marcas/{id}/dashboard**
```http
GET /api/v1/marcas/1/dashboard
Authorization: Bearer <token_jwt>
```

**Respuesta:**
```json
{
  "marcaId": 1,
  "totalVentas": 1500000.0,
  "totalPedidos": 50,
  "productosMasVendidos": [
    {
      "productoId": 1,
      "nombre": "Botella EcoLife",
      "unidadesVendidas": 100,
      "ingresos": 1499000.0
    }
  ],
  "metricasAmbientales": {
    "totalCO2Ahorrado": 120.5,
    "totalAguaAhorrada": 5000.0
  }
}
```
