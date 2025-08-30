# Client Service

Este microservicio gestiona los datos de clientes (clients) dentro de un sistema financiero.

## Tecnologías utilizadas

- Java 21
- Spring Boot 3.5.4
- Spring Data JPA
- H2 Database (en memoria)
- Spring Kafka
- Spring Security
- MapStruct
- Lombok
- JUnit 5 + Jacoco

## Endpoints

### `GET /api/clients`

Lista paginada de clientes.

### `GET /api/clients/{id}`

Obtiene la información de un cliente por ID.

### `POST /api/clients`

Crea un nuevo cliente.

### `PUT /api/clients/{id}`

Actualiza la información de un cliente.

### `PATCH /api/clients/{id}`

Actualiza la información parcial de un cliente.

### `DELETE /api/clients/{id}`

Elimina un cliente.

Recupera una lista de clientes según los IDs proporcionados.

## Estructura de DTOs

### `ClientRequest`

```json
{
  "name": "Juan Pérez",
  "gender": "M",
  "age": 30,
  "dni": "1234567890",
  "address": "Calle Falsa 123",
  "phone": "0999999999",
  "password": "secreto",
  "active": true
}
```

### `ClientResponse`

```json
{
  "id": 1,
  "name": "Juan Pérez",
  "gender": "M",
  "age": 30,
  "dni": "1234567890",
  "address": "Calle Falsa 123",
  "phone": "0999999999",
  "active": true,
  "createdAt": "2025-08-01T12:00:00",
  "updatedAt": "2025-08-02T10:00:00"
}
```

## Base de datos

Los scripts `schema.sql` y `data.sql` están configurados en `application.yml` para inicializar la base de datos H2 en
memoria.

La consola H2 está disponible en: `http://localhost:8080/h2-console`

- JDBC URL: `jdbc:h2:mem:clients`
- Usuario: `sa`
- Contraseña: *(vacía)*

## Cobertura de pruebas

Jacoco está configurado para verificar una cobertura mínima del 85% en líneas de código de clases clave, excluyendo
DTOs, modelos, validaciones, configuraciones y excepciones.

## Docker Compose

Para correr kafka localmente ejecutar comando:

- docker-compose up -d

---

Desarrollado por **Gandhy**