# Star Wars Challenge API

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=GabiC15_starwars-java-challenge&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=GabiC15_starwars-java-challenge)

Backend Java para el challenge técnico: expone People, Films, Starships y Vehicles de [SWAPI](https://www.swapi.tech/documentation) de forma paginada y filtrable, detrás de autenticación propia por JWT.

**Live demo:** https://starwars-java-challenge.onrender.com/swagger-ui.html
(el free tier de Render duerme la instancia sin tráfico; el primer request puede tardar ~60s en levantar)

## Stack

- Java 21 / Spring Boot 4.1
- Spring Security 7
- Spring Data JPA + PostgreSQL + Flyway
- RestClient para consumir SWAPI
- springdoc-openapi (Swagger UI)
- JUnit 5, Mockito, AssertJ, WireMock

## Decisiones de diseño

Arquitectura en capas: `controller/`, `service/`, `repository/`, `client/`, `dto/`, `model/`. La dependencia va siempre hacia abajo, un controller no tiene lógica de negocio y un service no sabe de HTTP.

Motor genérico para los 4 recursos (`SwapiResourceClient<P>` + `AbstractResourceService`): en vez de repetir "filtrar por id / nombre / listar" cuatro veces. Cada recurso solo aporta su record de propiedades y un controller/service finitos.

La paginación de SWAPI no es uniforme y no está documentada así: `people`/`starships`/`vehicles` paginan del lado del servidor, pero `films` siempre devuelve las 6 películas completas. La búsqueda por nombre/título tampoco pagina nunca, en ningún recurso. `SwapiResourceDefinition` distingue esto y arma la paginación en memoria cuando hace falta. Filtrar por id, dicho sea de paso, devuelve una "página" de un solo elemento en vez de cambiar la forma de la respuesta.

Caché con Caffeine sobre las respuestas de SWAPI, y circuit breaker con Resilience4j encima: si más de la mitad de las últimas 10 llamadas reales fallaron, el circuito abre 30s y las siguientes requests cortan directo con 503 en vez de esperar el timeout. Un 404 de SWAPI no cuenta como falla, solo los errores de red o parseo. Testeado en `CircuitBreakerIntegrationTest` forzando fallas contra WireMock.

Postgres real en dev/prod, H2 en tests. Así `mvn test` no depende de tener Postgres levantado, tampoco en CI.

## Cómo correrlo

### Con Docker (recomendado)

```bash
docker compose up --build
```

Levanta Postgres y la app juntos, sin configuración previa. API en `http://localhost:8080`, Swagger UI en `http://localhost:8080/swagger-ui.html`. Si ya tenés algo en el puerto 5432, cambiá el mapeo del servicio `postgres` en `docker-compose.yml`.

### Local, sin Docker

Necesitás Postgres corriendo (`docker compose up postgres` alcanza) y Java 21.

```bash
./mvnw spring-boot:run
```

Sin nada exportado, usa los defaults de dev de `application.yml`. Para pisar un default:

```bash
export JWT_SECRET=$(openssl rand -base64 48)
./mvnw spring-boot:run
```

### Tests

```bash
./mvnw test
```

No requiere Postgres ni internet: usa H2 en memoria y WireMock para simular SWAPI.

## Variables de entorno

| Variable | Default (dev) | Descripción |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/starwars_challenge` | URL JDBC de Postgres |
| `DB_USERNAME` | `starwars` | Usuario de la base |
| `DB_PASSWORD` | `starwars` | Password de la base |
| `JWT_SECRET` | secreto de desarrollo incluido | Cambiar en cualquier despliegue real |
| `SWAPI_BASE_URL` | `https://www.swapi.tech/api` | Base URL de SWAPI |
| `PORT` | `8080` | Puerto HTTP |

## Autenticación

Todos los endpoints de datos (`/api/v1/people`, `/films`, `/starships`, `/vehicles`) requieren JWT en `Authorization: Bearer <token>`.

```
POST /api/v1/auth/register   { "email", "password", "fullName" }  -> 201 + token
POST /api/v1/auth/login      { "email", "password" }              -> 200 + token
```

Token dura 1 hora por defecto (`app.jwt.expiration`). Desde Swagger UI: botón **Authorize**, pegar el token sin el prefijo `Bearer`.

## Endpoints de datos

Mismo contrato en los cuatro:

```
GET /api/v1/people?id=&name=&page=&size=
GET /api/v1/films?id=&name=&page=&size=
GET /api/v1/starships?id=&name=&page=&size=
GET /api/v1/vehicles?id=&name=&page=&size=
```

Sin parámetros te devuelve todo paginado (`page` arranca en 1, `size` default 10). Con `name` busca parcial, case-insensitive (en `films` es el título). Si se envía `id`, ese gana y listo: trae el elemento exacto, ignora el resto, 404 si no existe.

```json
{
  "content": [ { "id": "1", "name": "Luke Skywalker", "...": "..." } ],
  "page": 1,
  "size": 10,
  "totalElements": 82,
  "totalPages": 9
}
```

Documentación interactiva con la app corriendo: `/swagger-ui.html`.

## Health check

`GET /actuator/health` es público y devuelve `{"status":"UP"}` si la app y la conexión a Postgres están bien. Solo expone `health`, nada más del actuator queda público.

## Estructura del proyecto

```
src/main/java/com/conexa/starwars/
  controller/  los 5 @RestController: reciben el request, delegan al service
  service/     AuthService + PersonService/FilmService/StarshipService/VehicleService
  repository/  UserRepository
  client/      SwapiResourceClient, su config, y los DTOs internos que
               reflejan el JSON de SWAPI
  model/       User, Role
  dto/         requests/responses de la API propia
  security/    JWT, filtro, UserDetailsService
  config/      SecurityFilterChain, OpenAPI
  common/      PageResponse, manejo global de errores
```

## Testing

Unitarios: `PageResponseTest`, `JwtServiceTest`, `AuthServiceTest`, `PersonServiceTest`.

Integración: `AuthIntegrationTest`, `PeopleIntegrationTest`, `FilmIntegrationTest`, `CircuitBreakerIntegrationTest`.

Cobertura con JaCoCo: `./mvnw verify` genera el reporte en `target/site/jacoco/index.html`. Análisis estático en SonarCloud, corre en cada push a `main` vía `.github/workflows/ci.yml`.
