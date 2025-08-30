# Accounts & Movements Service

Microservicio encargado de la gestión de cuentas bancarias y movimientos (depósitos, retiros), así como la generación de
reportes de estado de cuenta.

## 🛠️ Tecnologías

- Java 11
- Spring Boot
- Spring Data JPA
- H2 Database (dev/test)
- Rest template
- Maven
- Lombok
- JUnit 5

## 🔄 Endpoints REST

### ✅ `AccountController`

Ruta base: `/api/accounts`

| Método | Ruta    | Descripción                         |
|--------|---------|-------------------------------------|
| GET    | `/`     | Listar cuentas                      |
| GET    | `/{id}` | Obtener una cuenta por ID           |
| POST   | `/`     | Crear una cuenta                    |
| PUT    | `/{id}` | Actualizar una cuenta               |
| PATCH  | `/{id}` | Actualización parcial de una cuenta |
| DELETE | `/{id}` | Eliminar una cuenta                 |

---

### 💰 `MovementController`

Ruta base: `/api/movements`

| Método | Ruta                                                                                       | Descripción                         |
|--------|--------------------------------------------------------------------------------------------|-------------------------------------|
| GET    | `/`                                                                                        | Listar movimientos                  |
| GET    | `/{id}`                                                                                    | Obtener movimiento por ID           |
| POST   | `/`                                                                                        | Registrar nuevo movimiento          |
| PUT    | `/{id}`                                                                                    | Actualizar movimiento existente     |
| PATCH  | `/{id}`                                                                                    | Actualización parcial de movimiento |
| DELETE | `/{id}`                                                                                    | Eliminar una movimiento             |
| GET    | `/clients/{clientId}/report?dateTransactionStart=2024-01-01&dateTransactionEnd=2027-12-31` | Generar reporte de estado de cuenta |

---

## 🔒 Validaciones

- No se permite depositar o retirar montos negativos.
- No se permite retirar fondos si el balance no es suficiente.
- Las fechas del reporte deben estar en orden lógico (start ≤ end y start ≤ hoy).

## 📡 Comunicación entre servicios

- Este microservicio se comunica con el servicio `client` vía **RestTemplate** para obtener información del cliente.

## 🧰 Base de Datos (H2)

- Tablas: `accounts`, `movements`

## 🧑‍💻 Autor

- Desarrollado por **Gandhy Cuasapás**