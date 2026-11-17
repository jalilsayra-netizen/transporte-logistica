# Transporte y Logística – Taller Integrador II

Spring Boot (Java 17) + Bootstrap 5. El código está en español y comentado.
**HTML, CSS y JavaScript están en archivos separados** (carpeta `static`).

## 1. Cómo ejecutarlo

1. Abrir esta carpeta en IntelliJ (File → Open → elegir la carpeta con el `pom.xml`) y esperar que Maven descargue las librerías.
2. Abrir `src/main/java/com/transporte/TransporteApplication.java` y apretar el triángulo verde (Run).
3. Entrar a http://localhost:8080

| Usuario | Contraseña | Rol |
|---|---|---|
| admin | admin123 | ADMIN |
| operador | operador123 | OPERADOR |
| conductor | conductor123 | CONDUCTOR (Carlos Gómez) |
| conductor2 | conductor123 | CONDUCTOR (Marta Pérez) |

La base de datos (H2) se crea sola en la carpeta `data/`. Para empezar de cero, parar el sistema y borrar esa carpeta.

## 2. Dónde está cada cosa

```
src/main/java/com/transporte/
├── entity/       Las TABLAS: una clase por entidad (Cliente, Pedido, Envio...) con sus validaciones
├── repository/   Hablan con la base de datos (consultas). Una interfaz por entidad
├── service/      La LÓGICA: reglas de negocio. Lo más importante está en EnvioService
├── controller/   La API REST (las URLs /api/...). Reciben la petición y llaman al service
├── exception/    NegocioException (errores) y ManejadorErrores (los convierte a JSON)
├── config/       SecurityConfig (login y roles), UsuarioDetailsService, DatosIniciales
src/main/resources/
├── application.properties     configuración (puerto, base de datos)
└── static/                    LO QUE VE EL USUARIO
    ├── *.html                 una pantalla por archivo (solo estructura + clases de Bootstrap)
    ├── css/estilos.css        estilos propios
    └── js/                    api.js (funciones comunes) + un .js por pantalla
```

**Camino de una petición:** pantalla (HTML + JS) → `controller` → `service` → `repository` → base de datos.

## 3. Ejemplo: qué pasa cuando se crea un envío

1. En `envios.html` se completa el formulario y se aprieta *Crear envío*.
2. `js/envios.js` junta los datos con `leerFormulario()` y hace `POST /api/envios` (función `api()` de `api.js`).
3. `EnvioController.crear()` recibe el JSON. `@Valid` revisa las validaciones de la entidad `Envio`.
4. `EnvioService.crear()` comprueba las reglas: el pedido existe y no tiene envío, las fechas son coherentes, el vehículo no está en mantenimiento (RN01), la carga entra (RN05), no hay viajes superpuestos (RN03)...
5. Si todo está bien, `EnvioRepository.save()` lo guarda y se anota el primer registro del historial (RN07).
6. Si algo falla, el service lanza `NegocioException`; `ManejadorErrores` la convierte en `{"error": "mensaje"}` y `mostrarError()` lo muestra en rojo en la pantalla.

## 4. Reglas de negocio (todas en `service/EnvioService.java`)

| Código | Regla |
|---|---|
| RN01 | No asignar un vehículo en mantenimiento o inactivo |
| RN02 | No asignar un conductor inactivo, de licencia o con licencia vencida |
| RN03 | Un vehículo o conductor no puede tener dos viajes en el mismo horario |
| RN04 | Un envío ENTREGADO o CANCELADO no puede cambiar de estado |
| RN05 | El peso del pedido no puede superar la capacidad del vehículo |
| RN06 | Para registrar una entrega: fecha, hora, quién recibe y observaciones son obligatorios |
| RN07 | Cada cambio de estado se guarda en el historial y no se puede modificar |

Estados del envío: `PREPARANDO → EN_TRANSITO → ENTREGADO` (o `CANCELADO`). Un pedido genera un solo envío.
Las "bajas" de clientes, vehículos, conductores y usuarios son lógicas: el registro queda INACTIVO.

## 5. Endpoints y permisos

| Recurso | URL | ADMIN | OPERADOR | CONDUCTOR |
|---|---|:-:|:-:|:-:|
| Login | `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/me` | ✔ | ✔ | ✔ |
| Usuarios | `/api/usuarios` | ✔ | | |
| Clientes | `/api/clientes` (`?q=` busca) | ✔ | ✔ | |
| Pedidos | `/api/pedidos` (`?pendientes=true`) | ✔ | ✔ | |
| Vehículos | `/api/vehiculos` (`?q=`, `?estado=`) | ✔ | ✔ | |
| Conductores | `/api/conductores` | ✔ | solo consultar | |
| Tipos de vehículo | `/api/tipos-vehiculo` | ✔ | solo consultar | |
| Envíos | `GET/POST/PUT/DELETE /api/envios` (DELETE cancela) | ✔ | ✔ | solo consultar los suyos |
| Historial | `GET /api/envios/{id}/historial` | ✔ | ✔ | los suyos |
| Cambiar estado | `PUT /api/envios/{id}/estado` | ✔ | ✔ | solo iniciar el traslado de los suyos |
| Asignar | `PUT /api/envios/{id}/vehiculo/{idVehiculo}` y `.../conductor/{idConductor}` | ✔ | ✔ | |
| Registrar entrega | `POST /api/envios/{id}/entrega` | ✔ | | los suyos |
| Entregas | `GET /api/entregas` | ✔ | | las suyas |

Los permisos se controlan en el backend (`SecurityConfig`). Los errores siempre vuelven como JSON `{"error": "..."}`.
Documentación interactiva de la API (solo ADMIN logueado): http://localhost:8080/swagger-ui.html

## 6. Validaciones
- **Servidor** (obligatoria): anotaciones en las entidades (`@NotBlank`, `@Size`, `@Pattern`, `@Min`, `@Email`...) y controles en los services.
- **Navegador**: atributos HTML5 (`required`, `pattern`, `min`, `maxlength`, `type="email"`) con las clases de validación de Bootstrap.

## 7. Prueba rápida (flujo completo)
1. `admin` → Envíos → **Nuevo envío** → elegir un pedido, itinerario, vehículo AB123CD y conductor Gómez → Crear.
2. Salir → entrar como `conductor` → Envíos → **Iniciar traslado**.
3. Entrar como `admin` o `conductor` → Entregas → completar → **Confirmar entrega** → el envío queda ENTREGADO.
4. Seguimiento → elegir el envío → se ve el historial con los 3 estados.
5. Reglas: pedido de 9500 kg con la camioneta AC456DE → "Capacidad de carga superada". El vehículo AD789FG (en mantenimiento) no se puede asignar.

## 8. MySQL (opcional)
`CREATE DATABASE transporte CHARACTER SET utf8mb4;` y ejecutar con el perfil `mysql` (ver `application-mysql.properties`).
