 Proyecto e-shop

# Ecoshop-NoCountry
Plataforma e-commerce sustentable con cálculo de huella de carbono por producto y dashboard de impacto ambiental.
Participantes: Tomy (Front-end), Facundo (Front-end), Ezequiel (Front-end), Santiago (UX-UI), Brian (Back-End) y Javiera (Back-end) 

## **1. Herramientas de Organización y Comunicación**

- **Canal oficial:** WhatsApp (grupo activo del equipo).
- **Gestión de tareas:** Se utilizará **Notion** como espacio principal de organización y seguimiento.
- **Repositorio:** Proyecto creado en **GitHub** por Tomy. Todos los miembros tendrán acceso para colaborar.
- **Diseño UX/UI:** Se utilizará **Figma** por Santiago para wireframes, prototipado y diseño visual.

## **2. Flujo de Trabajo y Estándares**

- **Control de versiones:** Se adopta el flujo de ramas `main` / `develop` / `feature/*`.
- **Revisiones obligatorias:** Todo cambio en `develop` requerirá **Pull Request** y revisión de al menos un integrante (Falto definir si una persona en especifico revisa el código o lo hacemos en una daily en si).

## **3. Stack Tecnológico Confirmado**

### **Frontend**

- Framework: **Next.js**
- Librerías: **TailwindCSS**, **shadcn/ui**, **TanStack**, **Zustand**
- Autenticación: **Clerk**
- Diseño: **Figma** (Santiago UX/UI)
- Enfoque: Diseño desktop-first adaptable a mobile

### Backend

- Framework: **Spring Boot 3.5.7** (Java 21)
- Base de datos: **PostgreSQL 14+**
- Autenticación: **Clerk** (JWT)
- ORM: **Spring Data JPA / Hibernate**
- Deploy: **Render** (Producción)
- Manejo de nombres de archivos en Inglés

#### Estado del Backend

- ✅ **Backend completamente funcional y desplegado**
- ✅ **URL de Producción:** `https://ecoshop-backend-mm8u.onrender.com`
- ✅ **Base de datos:** PostgreSQL en Render (Oregon, US West)
- ✅ **Autenticación:** Clerk integrado y funcionando
- ✅ **Todos los endpoints probados y funcionando correctamente**
- ✅ **API REST completa con documentación**

#### Documentación del Backend

Para más detalles sobre el backend, consulta: [`backend/ecoshop/README.md`](backend/ecoshop/README.md)
