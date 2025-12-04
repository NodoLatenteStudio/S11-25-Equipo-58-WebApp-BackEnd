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
├── controller/               # Controladores REST
│   ├── CertificacionController.java
│   ├── ClerkConfigController.java
│   ├── ClerkTestController.java    # Solo desarrollo
│   ├── ClerkWebhookController.java
│   ├── HealthController.java
│   ├── MarcaController.java
│   ├── PedidoController.java
│   ├── PedidoItemController.java
│   ├── ProductoController.java
│   └── UsuarioController.java
├── domain/                   # Entidades JPA
│   ├── Certificacion.java
│   ├── Marca.java
│   ├── Pedido.java
│   ├── PedidoItem.java
│   ├── Producto.java
│   └── Usuario.java
├── dto/                      # Data Transfer Objects
│   ├── CertificacionRequestDTO.java
│   ├── CertificacionResponseDTO.java
│   ├── ImpactoAmbientalResponse.java
│   ├── clerk/
│   │   └── ClerkWebhookDTO.java
│   ├── Marca/
│   │   ├── MarcaRequestDTO.java
│   │   └── MarcaResponseDTO.java
│   ├── Pedido/
│   │   ├── PedidoRequestDTO.java
│   │   └── PedidoResponseDTO.java
│   ├── PedidoItem/
│   │   ├── PedidoItemRequestDTO.java
│   │   └── PedidoItemResponseDTO.java
│   ├── Producto/
│   │   ├── ProductoRequestDTO.java
│   │   └── ProductoResponseDTO.java
│   └── Usuario/
│       ├── UsuarioRequestDTO.java
│       └── UsuarioResponseDTO.java
├── exception/                # Manejo de excepciones
│   ├── BadRequestException.java
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── mapper/                   # Mappers entre DTOs y Entidades
│   ├── CertificacionMapper.java
│   ├── MarcaMapper.java
│   ├── PedidoItemMapper.java
│   ├── PedidoMapper.java
│   ├── ProductoMapper.java
│   └── UsuarioMapper.java
├── repository/               # Repositorios JPA
│   ├── CertificacionRepository.java
│   ├── MarcaRepository.java
│   ├── PedidoItemRepository.java
│   ├── PedidoRepository.java
│   ├── ProductoRepository.java
│   └── UsuarioRepository.java
├── security/
│   └── clerk/
│       └── ClerkJwtAuthenticationConverter.java  # Convertidor JWT → Authentication
└── service/                  # Lógica de negocio
    ├── CertificacionService.java
    ├── MarcaService.java
    ├── PedidoItemService.java
    ├── PedidoService.java
    ├── ProductoService.java
    ├── UsuarioService.java
    └── impl/
        ├── CertificacionServiceImpl.java
        ├── MarcaServiceImpl.java
        ├── PedidoItemServiceImpl.java
        ├── PedidoServiceImpl.java
        ├── ProductoServiceImpl.java
        └── UsuarioServiceImpl.java
```

## 🗄️ Base de Datos

### Tablas Principales

- `usuarios`: Usuarios del sistema (vinculados con Clerk mediante `clerk_id`)
- `marcas`: Marcas de productos
- `productos`: Productos con métricas ambientales
- `certificaciones`: Certificaciones ambientales
- `producto_certificaciones`: Tabla intermedia (relación many-to-many)
- `pedidos`: Pedidos de clientes
- `pedido_items`: Items de cada pedido

### Configuración

- Las tablas se crean automáticamente mediante JPA con `ddl-auto: update`
- En producción, usar `ddl-auto: validate` o migraciones con Flyway/Liquibase
- PostgreSQL 14+ recomendado

### Relaciones

- **Usuario ↔ Marca**: One-to-Many (un usuario puede tener múltiples marcas)
- **Usuario ↔ Pedido**: One-to-Many (un usuario puede tener múltiples pedidos)
- **Marca ↔ Producto**: One-to-Many (una marca puede tener múltiples productos)
- **Producto ↔ Certificacion**: Many-to-Many (tabla intermedia `producto_certificaciones`)
- **Pedido ↔ PedidoItem**: One-to-Many (un pedido tiene múltiples items)
- **Producto ↔ PedidoItem**: Many-to-One (un producto puede estar en múltiples items)

## 📡 Endpoints Disponibles

### Health Check

- [x] `GET /api/v1/health` - Verificar estado de la API (público)

### Productos

- [x] `GET /api/v1/productos` - Obtener todos los productos (público)
- [x] `GET /api/v1/productos/{id}` - Obtener producto por ID (público)
- [x] `GET /api/v1/productos/marca/{marcaId}` - Obtener productos por marca (público)
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

### Usuarios

- [x] `GET /api/v1/usuarios` - Obtener todos los usuarios (requiere autenticación)
- [x] `GET /api/v1/usuarios/{id}` - Obtener usuario por ID (requiere autenticación)
- [x] `POST /api/v1/usuarios` - Crear nuevo usuario (requiere autenticación)
- [x] `PUT /api/v1/usuarios/{id}` - Actualizar usuario (requiere autenticación)
- [x] `DELETE /api/v1/usuarios/{id}` - Eliminar usuario (requiere autenticación)

**Nota:** Los usuarios normalmente se crean mediante webhooks de Clerk cuando un usuario se registra en el frontend. Estos endpoints gestionan los datos del usuario en la base de datos local.

### Marcas

- [x] `GET /api/v1/marcas` - Obtener todas las marcas (requiere autenticación)
- [x] `GET /api/v1/marcas/{id}` - Obtener marca por ID (requiere autenticación)
- [x] `POST /api/v1/marcas` - Crear nueva marca (requiere autenticación, `usuarioId` se obtiene automáticamente del JWT)
- [x] `PUT /api/v1/marcas/{id}` - Actualizar marca (requiere autenticación, `usuarioId` se obtiene automáticamente del JWT)
- [x] `DELETE /api/v1/marcas/{id}` - Eliminar marca (requiere autenticación)

### Pedidos

- [x] `GET /api/v1/pedidos` - Obtener todos los pedidos (requiere autenticación)
- [x] `GET /api/v1/pedidos/{id}` - Obtener pedido por ID (requiere autenticación)
- [x] `GET /api/v1/pedidos/usuario/{usuarioId}` - Obtener pedidos de un usuario (requiere autenticación)
- [x] `POST /api/v1/pedidos` - Crear nuevo pedido (requiere autenticación, `usuarioId` se obtiene automáticamente del JWT)
- [x] `PUT /api/v1/pedidos/{id}` - Actualizar pedido completo (requiere autenticación, `usuarioId` se obtiene automáticamente del JWT)
- [x] `PATCH /api/v1/pedidos/{id}/estado?estado={estado}` - Actualizar solo el estado del pedido (requiere autenticación)
- [x] `DELETE /api/v1/pedidos/{id}` - Eliminar pedido (requiere autenticación)

### Items de Pedido

- [x] `GET /api/v1/pedido-items/pedido/{pedidoId}` - Obtener todos los items de un pedido (requiere autenticación)
- [x] `POST /api/v1/pedido-items` - Agregar item a un pedido (requiere autenticación)
- [x] `PUT /api/v1/pedido-items/{itemId}?cantidad={cantidad}` - Actualizar cantidad de un item (requiere autenticación)
- [x] `DELETE /api/v1/pedido-items/{itemId}` - Eliminar item de un pedido (requiere autenticación)

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

**⚠️ IMPORTANTE:** La verificación de firma del webhook está pendiente de implementar (TODO crítico para producción).

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

### Endpoints Públicos

Los siguientes endpoints no requieren autenticación:

- `GET /api/v1/health`
- `GET /api/v1/productos/**` (solo lectura)
- `GET /api/v1/certificaciones/**` (solo lectura)
- `POST /api/v1/webhooks/clerk` (público pero debe verificar firma)
- `GET /api/v1/config/clerk`
- `GET /api/v1/test/clerk/**` (solo desarrollo)

### Endpoints Protegidos

Todos los demás endpoints requieren un token JWT válido de Clerk:

- `POST /api/v1/productos` y modificaciones
- `POST /api/v1/certificaciones` y modificaciones
- Todos los endpoints de `/api/v1/usuarios/**`
- Todos los endpoints de `/api/v1/marcas/**`
- Todos los endpoints de `/api/v1/pedidos/**`
- Todos los endpoints de `/api/v1/pedido-items/**`

### Importante para Producción

1. **CORS**: Configurar dominios específicos en `SecurityConfig.java`:
   ```java
   configuration.setAllowedOrigins(Arrays.asList("https://tu-dominio.com"));
   ```

2. **Verificación de Webhooks**: Implementar verificación de firma en `ClerkWebhookController` (actualmente marcado como TODO crítico)

3. **Variables de Entorno**: Todas las claves de Clerk deben estar en variables de entorno, nunca en código

4. **HTTPS**: Configurar HTTPS en producción

## 🎯 Características Principales

### Módulo de Productos

- CRUD completo de productos
- Relación many-to-many con certificaciones
- Métricas ambientales (huella de carbono, porcentaje reciclable, eco-badge)
- Actualización parcial (solo campos enviados se actualizan)
- Validaciones de negocio (precio, nombre, SKU único)
- Manejo de fechas de creación automático

### Módulo de Certificaciones

- CRUD completo de certificaciones
- Búsqueda por nombre de sello (case-insensitive)
- Relación many-to-many con productos
- Validación de unicidad

### Módulo de Usuarios

- CRUD completo de usuarios
- Integración con Clerk mediante `clerkId`
- Sincronización automática mediante webhooks
- Validación de relaciones antes de eliminar (marcas, pedidos)

### Módulo de Marcas

- CRUD completo de marcas
- Asociación automática con usuario autenticado
- `usuarioId` se obtiene automáticamente del JWT

### Módulo de Pedidos

- CRUD completo de pedidos
- Gestión de estados (pendiente_pago, procesando, enviado, entregado, cancelado)
- Asociación automática con usuario autenticado
- Cálculo de totales automático
- Actualización parcial del estado mediante PATCH

### Módulo de Items de Pedido

- CRUD completo de items de pedido
- Relación con productos y pedidos
- Cálculo de subtotales automático
- Actualización de cantidad mediante query parameter

## 🔍 Validaciones

### ProductoRequestDTO

- `marcaId`: Obligatorio (debe existir)
- `nombre`: Obligatorio, máximo 200 caracteres
- `precio`: Obligatorio, mayor a 0
- `stock`: Obligatorio, mínimo 0
- `sku`: Opcional, único si se proporciona
- `certificaciones`: Lista de IDs de certificaciones (deben existir)

### CertificacionRequestDTO

- `nombreSello`: Obligatorio

### UsuarioRequestDTO

- `email`: Obligatorio, formato válido, único
- `rol`: Obligatorio (cliente, marca, admin)
- `clerkId`: Opcional (se sincroniza desde Clerk)

### MarcaRequestDTO

- `nombreOficial`: Obligatorio, máximo 150 caracteres
- `usuarioId`: Opcional (se obtiene automáticamente del JWT)

### PedidoRequestDTO

- `estado`: Opcional, valores válidos: pendiente_pago, procesando, enviado, entregado, cancelado
- `direccionEnvio`: Obligatorio al crear, opcional al actualizar
- `usuarioId`: Opcional (se obtiene automáticamente del JWT)

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

## 🚧 Pendientes para Producción

Ver archivo `PENDIENTES-PROYECTO.md` para la lista completa de funcionalidades pendientes según los requerimientos del proyecto.

**Críticos:**
- ⚠️ Implementar verificación de firma de webhooks de Clerk
- ⚠️ Configurar CORS con dominios específicos (no usar `*`)
- ⚠️ Configurar HTTPS

## 📚 Documentación Adicional

- `PENDIENTES-PROYECTO.md`: Lista completa de funcionalidades pendientes según requerimientos
- `.env.example`: Ejemplo de variables de entorno
- Código fuente: Comentarios detallados en español en todas las clases

## 🤝 Contribución

Este proyecto forma parte del trabajo del Equipo 58. Para contribuir:

1. Revisar los requerimientos en `PENDIENTES-PROYECTO.md`
2. Crear una rama para la funcionalidad
3. Implementar siguiendo las convenciones del proyecto
4. Actualizar documentación si es necesario
5. Hacer pull request

---

**Última actualización:** 2024-12-03
**Versión:** 0.0.1-SNAPSHOT
