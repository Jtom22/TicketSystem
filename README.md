#TicketSystem

API REST para la venta de entradas a eventos (conciertos, en principio), construida con Spring Boot. Gestiona el catálogo de eventos, sus categorías de entrada, los asientos individuales, el proceso de compra con reserva temporal, y la emisión final de los tickets.

Proyecto personal de aprendizaje — backend funcional de punta a punta; el frontend está en desarrollo.

---

## Índice

- [Qué hace el sistema](#qué-hace-el-sistema)
- [Stack técnico](#stack-técnico)
- [Arquitectura](#arquitectura)
- [Modelo de datos](#modelo-de-datos)
- [El flujo de compra, explicado](#el-flujo-de-compra-explicado)
- [Seguridad](#seguridad)
- [Cómo levantarlo en local](#cómo-levantarlo-en-local)
- [Endpoints principales](#endpoints-principales)
- [Documentación interactiva (Swagger)](#documentación-interactiva-swagger)
- [Estado del proyecto](#estado-del-proyecto)

---

## Qué hace el sistema

- Cualquiera puede ver el **catálogo de eventos** y sus categorías de entrada, sin necesidad de cuenta.
- Un usuario registrado puede **reservar asientos concretos** de una categoría (por ejemplo, "VIP" o "General"), dentro de un pedido.
- Esa reserva es **temporal**: mientras dura, nadie más puede coger esos asientos. Si el usuario no completa la compra a tiempo, el sistema libera los asientos automáticamente.
- Al confirmarse el pedido, se **emite el ticket** definitivo para cada asiento comprado.
- Un administrador gestiona el catálogo (crear eventos, categorías, asientos) y los usuarios del sistema.

## Stack técnico

| Categoría | Tecnología |
-----------------------------------------------------------
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.1 |
| Persistencia | Spring Data JPA (Hibernate) + MySQL |
| Seguridad | Spring Security + JWT (JJWT) |
| Mapeo DTO ↔ Entidad | MapStruct |
| Reducción de boilerplate | Lombok |
| Documentación de API | springdoc-openapi (Swagger UI) |
| Build | Maven |


## Arquitectura

Arquitectura en capas:

```
controllers/    → capa HTTP: recibe la petición, delega, devuelve respuesta
services/       → lógica de negocio y transacciones (interfaz + implementación)
repositories/   → acceso a datos (Spring Data JPA)
entities/       → modelo de datos, mapeado a las tablas
dto/            → datos de entrada/salida de la API, separados de las entidades
mappers/        → traducción Entidad ↔ DTO (MapStruct)
security/       → JWT: generación, validación, filtro de autenticación
config/         → configuración transversal (seguridad, auditoría JPA, Swagger, tareas programadas)
scheduling/      → tareas en segundo plano (liberar reservas expiradas)
exception/       → manejo centralizado de errores de la API
```

## Modelo de datos

```
Event  →  TicketCategory  →  Seat  →  IssuedTicket  ←  Order  ←  User
                               │                          ↑         │
                               └──────────────────────────┘       Role
                                  (reserva temporal, nullable)
```

**Relaciones (claves foráneas reales del esquema):**

| Relación | Cardinalidad | Notas |
|---|---|---|
| `TicketCategory.event_id` → `Event` | muchos a uno | una categoría pertenece a un evento |
| `Seat.category_id` → `TicketCategory` | muchos a uno | un asiento pertenece a una categoría |
| `Seat.reserved_by_order_id` → `Order` | muchos a uno| vínculo *temporal*: solo existe mientras el asiento está `RESERVADO_TEMPORAL`; `null` en cualquier otro estado |
| `IssuedTicket.seat_id` → `Seat` | uno a uno| vínculo *permanente*, se crea al confirmar la compra |
| `IssuedTicket.order_id` → `Order` | muchos a uno | un pedido puede emitir varios tickets |
| `Order.user_id` → `User` | muchos a uno | un usuario puede tener varios pedidos |
| `users_roles` (tabla intermedia) | muchos a muchos | un usuario puede tener varios roles |

La FK `Seat.reserved_by_order_id` es la que sostiene la reserva temporal explicada abajo — conviene tenerla identificada aparte porque, a diferencia del resto, **no es una relación permanente del dominio**, sino el estado de un proceso en curso.

## El flujo de compra, explicado

Esta es la parte más importante del sistema — el motivo por el que un asiento no se puede vender dos veces:

1. **`DISPONIBLE`** — el asiento está libre.
2. Un usuario crea una `Order` incluyendo ese `seatId`. El sistema comprueba que sigue `DISPONIBLE` y lo pasa a **`RESERVADO_TEMPORAL`**, vinculándolo a esa `Order` concreta. Este paso usa un bloqueo optimista (`@Version`): si dos personas intentan reservar el mismo asiento en el mismo instante, solo una gana — la otra recibe un error claro (409) en vez de una condición de carrera silenciosa.
3. El propio servidor calcula la fecha de expiración de la `Order` (10 minutos desde su creación) — el cliente no puede elegirla ni manipularla. Mientras la reserva esté activa, nadie más puede coger ese asiento.
4. Si el usuario completa la compra dentro de ese plazo, se emite un `IssuedTicket` y el asiento pasa a **OCUPADO** — definitivo. Al emitir el ticket, el sistema revalida la fecha de expiración en ese mismo instante (no solo el `status`), para no dejar colarse una compra fuera de plazo en la ventana de hasta 60s antes de que el job de limpieza haya pasado por ahí.
5. Si el usuario NO completa la compra a tiempo, una **tarea programada** (`@Scheduled`, corre cada minuto) detecta que la reserva expiró, libera el asiento de vuelta a `DISPONIBLE`, y marca la `Order` como `Expired`.

## Seguridad

- Autenticación **stateless** con JWT — sin sesiones en el servidor.
- Contraseñas con hash `BCrypt`, nunca en texto plano.
- Los roles se comprueban **en cada petición**, consultando la base de datos (no se confía en lo que diga un token viejo) — si un admin cambia el rol de alguien, el efecto es inmediato en la siguiente petición.
- Endpoints protegidos por rol a nivel de ruta (`SecurityConfig`): el catálogo es de lectura pública, la gestión es solo `ADMIN`, comprar requiere estar autenticado.

## Cómo levantarlo en local

### Requisitos
- Java 21
- Maven (o usa el wrapper incluido, `./mvnw`)
- MySQL corriendo en local

### Pasos

1. Crea la base de datos en MySQL:
   ```sql
   CREATE DATABASE db_ticketsystem;
   ```

2. Copia `src/main/resources/application.properties` con:
   
   spring.datasource.url=jdbc:mysql://localhost:3306/db_ticketsystem
   spring.datasource.username=tu_usuario
   spring.datasource.password=tu_password

   spring.jpa.hibernate.ddl-auto=update

   jwt.secret=UNA_CADENA_LARGA_Y_ALEATORIA_DE_AL_MENOS_64_CARACTERES
   jwt.expiration-ms=86400000


3. Inserta los roles base (todavía no hay migraciones automáticas):
   ```sql
   INSERT INTO roles (name) VALUES ('USER'), ('ADMIN');
   ```

4. Arranca la aplicación:
   ```bash
   ./mvnw spring-boot:run
   ```

5. La API queda disponible en `http://localhost:8080`.

## Endpoints principales

| Método | Ruta | Quién puede |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Público |
| `POST` | `/api/v1/auth/login` | Público |
| `GET` | `/api/v1/events` | Público |
| `POST` | `/api/v1/events` | `ADMIN` |
| `GET` | `/api/v1/ticketCategory/events/{eventId}/ticket-categories` | Público |
| `POST` | `/api/v1/seats/ticket-categories/{categoryId}/seats` | `ADMIN` |
| `POST` | `/api/v1/orders` | Autenticado |
| `POST` | `/api/v1/issued-tickets` | Autenticado |
| `GET` / `PATCH` / `DELETE` | `/api/v1/users/**` | `ADMIN` |



> **Nota sobre `POST /api/v1/orders`:** el body solo lleva `userId` y `seatIds`. El `totalAmount` (sumando el precio real de la categoría de cada asiento), el `status` inicial (`Pending`) y la fecha de expiración de la reserva los calcula el servidor — no se envían en la petición. Así nadie puede manipular cuánto paga ni cuánto dura su reserva.

## Documentación interactiva (Swagger)

Con la aplicación arrancada:
```
http://localhost:8080/swagger-ui.html
```
Desde ahí puedes ver todos los endpoints, sus esquemas de entrada/salida, y probarlos directamente — incluidos los protegidos, pegando un JWT en el botón **Authorize**.

## Estado del proyecto

- ✅ Backend: catálogo, reserva de asientos con bloqueo optimista, autenticación JWT, roles, manejo de errores centralizado, liberación automática de reservas expiradas.
- 🚧 Frontend: en desarrollo (React + Vite).
- 🚧 Pendiente: migraciones de Flyway, Docker Compose para levantar la BD, tests automatizados.
