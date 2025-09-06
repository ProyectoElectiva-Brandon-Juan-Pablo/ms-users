ms-users — Servicio de Usuarios (Spring Boot)

Servicio REST para administrar usuarios (registro, consulta, actualización y eliminación) y, opcionalmente, autenticación con JWT. Pensado para ejecutarse en VS Code sobre Windows con Java 17 y Maven Wrapper.

Ajusta los nombres de paquetes, puertos y rutas si tu proyecto usa otros valores.

🚀 Inicio rápido (VS Code en Windows)

Clona y abre la carpeta en VS Code.

(Opcional) Crea .env con tus variables (ver Variables de entorno).

Ejecuta con Maven Wrapper:

.\mvnw spring-boot:run


Por defecto en http://localhost:8081 (ajusta server.port si usas otro).

Probar en el navegador o con curl:

curl http://localhost:8081/actuator/health

🧰 Stack

Java 17+

Spring Boot (Web, Validation, Data JPA, Security —opcional—)

Base de datos: Postgres/MySQL/H2 (elige y configura)

Maven Wrapper (mvnw, mvnw.cmd)

Docker & Docker Compose (opcional, si usas compose.yaml)

Lombok (opcional)

📁 Estructura del proyecto (referencia)
ms-users/
├─ src/
│  ├─ main/
│  │  ├─ java/com/tiendapc/user_service/   # controllers, services, repositories, dto, config, etc.
│  │  └─ resources/
│  │     ├─ application.properties (o .yml)
│  │     └─ schema.sql / data.sql (opcional)
│  └─ test/java/com/tiendapc/user_service/
├─ pom.xml
├─ compose.yaml            # si usas contenedores
├─ .gitattributes          # normaliza finales de línea
├─ .gitignore
└─ README.md


Según tu captura: existen pom.xml, compose.yaml, mvnw.cmd, application.properties y tests bajo com.tiendapc.user_service.

⚙️ Configuración
application.properties (ejemplo)
server.port=8081

# Elige tu motor de BD y ajusta el driver & URL
spring.datasource.url=jdbc:postgresql://localhost:5432/ms_users
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# JWT (si usas seguridad)
app.jwt.secret=CAMBIA_ESTE_SECRET
app.jwt.expiration=3600000

Variables de entorno (sugeridas)

Crea un archivo .env (si usas Docker Compose o configuración por entorno):

SERVER_PORT=8081
DB_URL=jdbc:postgresql://db:5432/ms_users
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=CAMBIA_ESTE_SECRET
JWT_EXPIRATION=3600000


En application.properties puedes referenciarlas así:

server.port=${SERVER_PORT:8081}
spring.datasource.url=${DB_URL:jdbc:h2:mem:ms_users}
spring.datasource.username=${DB_USERNAME:sa}
spring.datasource.password=${DB_PASSWORD:}
app.jwt.secret=${JWT_SECRET:dev-secret}
app.jwt.expiration=${JWT_EXPIRATION:3600000}

🧪 Comandos útiles
# Ejecutar
.\mvnw spring-boot:run

# Pruebas
.\mvnw test

# Empaquetar JAR
.\mvnw clean package
java -jar target\ms-users-*.jar

# Formateo/validación (si tienes plugins configurados)
.\mvnw spotless:apply
.\mvnw verify

🧩 Endpoints (ejemplo)

Ajusta los paths si tu controlador usa otro prefijo (por ejemplo /api/users o /users).

Usuarios

POST /api/v1/users — Crear usuario

GET /api/v1/users — Listar usuarios (paginado opcional)

GET /api/v1/users/{id} — Obtener por id

PUT /api/v1/users/{id} — Actualizar

DELETE /api/v1/users/{id} — Eliminar

Autenticación (opcional)

POST /api/v1/auth/register — Registro

POST /api/v1/auth/login — Login → devuelve JWT

GET /api/v1/auth/me — Perfil autenticado (requiere Authorization: Bearer <token>)

Ejemplos curl
# Crear usuario
curl -X POST http://localhost:8081/api/v1/users ^
  -H "Content-Type: application/json" ^
  -d "{ \"firstName\":\"Juan\", \"lastName\":\"López\", \"email\":\"juan@demo.com\", \"password\":\"Secreta123\" }"

# Login (si está implementado)
curl -X POST http://localhost:8081/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{ \"email\":\"juan@demo.com\", \"password\":\"Secreta123\" }"

🗃️ Modelo de datos (sugerido)
class User {
  Long id;
  String firstName;
  String lastName;
  String email;         // único
  String passwordHash;  // NO guardar en texto plano
  String role;          // USER / ADMIN
  Boolean enabled;
  Instant createdAt;
  Instant updatedAt;
}

🐳 Docker / Docker Compose (opcional)

Si tienes compose.yaml, puedes levantar DB y el servicio:

# Levantar todo
docker compose up -d

# Ver logs
docker compose logs -f ms-users

# Detener
docker compose down


Asegúrate de que los servicios en compose.yaml usen las mismas variables de entorno que definiste.

🔧 Problemas comunes

Aviso CRLF/LF en Git (Windows): es normal. Añade .gitattributes y normaliza:

* text=auto
*.java        text eol=lf
*.yml         text eol=lf
*.yaml        text eol=lf
*.xml         text eol=lf
*.properties  text eol=lf
*.md          text eol=lf
*.bat         text eol=crlf
*.cmd         text eol=crlf

git add .gitattributes
git add --renormalize .
git commit -m "Normalize line endings"


Puerto ocupado: cambia server.port o libera el puerto.

Conexión a BD: revisa DB_URL, credenciales y que el contenedor/servicio esté arriba.

OneDrive/paths largos: evita rutas con espacios/carpetas sincronizadas si da problemas.
