# profile-system

Prueba técnica: dos microservicios en Quarkus + MongoDB.

- **`ms-profile-crud`** (escritura) — crea, reemplaza, actualiza parcialmente y borra
  perfiles, y emite un JWT al crear. También expone endpoints internos para que
  `ms-profile-query` le consulte perfiles.
- **`ms-profile-query`** (lectura) — consulta un perfil (por id sacado del token, o por
  email), pidiéndoselo internamente a `ms-profile-crud` con Basic Auth (un par
  `clientId` / `secret`).

Los dos hablan **HTTPS obligatorio** (TLS 1.2+). El JWT lo firma `ms-profile-crud` con
su llave privada y lo verifican los dos servicios con la pública.

## Antes de arrancar (una sola vez)

```bash
./generate-keys.sh
```

Genera el par de llaves JWT (privada/pública) y el certificado TLS de cada
microservicio, y los coloca donde cada proyecto los necesita. En Windows, corre esto
desde **Git Bash** — PowerShell no trae `openssl`.

Estos archivos (`*.pem`) están en `.gitignore` a propósito: no se versionan, cada quien
los genera en su máquina.

Lo mismo con el secreto de la Basic Auth entre servicios: copia el ejemplo en cada
microservicio y pon el **mismo** valor en los dos.

```bash
cp ms-profile-crud/.env.example ms-profile-crud/.env
cp ms-profile-query/.env.example ms-profile-query/.env
# edita ambos .env y define INTERNAL_CLIENT_SECRET con el mismo valor
```

Los `.env` están en `.gitignore`. Quarkus los lee solo (dev y test); en Kubernetes el
valor llega desde un Secret.

## Cómo correrlo local

1. `docker compose up -d` — levanta Mongo en `localhost:27017`.
2. En una terminal: `cd ms-profile-crud && ./mvnw quarkus:dev`
3. En otra terminal: `cd ms-profile-query && ./mvnw quarkus:dev`
4. En tu cliente REST (Postman / Thunder Client / curl), **permite certificados
   autofirmados** — los dos servicios usan TLS con certificado propio, no firmado por
   una CA real. Con `curl` es la bandera `-k`.

| Servicio          | URL base                  | Puerto dev (HTTP interno) |
|-------------------|---------------------------|---------------------------|
| `ms-profile-crud` | `https://localhost:8444`  | 8081                      |
| `ms-profile-query`| `https://localhost:8445`  | 8082                      |

## Endpoints y cómo usarlos

El flujo típico: creas un perfil en `ms-profile-crud`, te guardas el `token` que
devuelve, y con ese `Bearer <token>` llamas al resto.

---

### `ms-profile-crud` — `https://localhost:8444`

#### `POST /create-profile` — crear un perfil (público)

Crea el perfil y devuelve el perfil + un JWT (válido 2 h) que identifica a su dueño.

```http
POST https://localhost:8444/create-profile
Content-Type: application/json

{
  "name": "Jeison",
  "lastName": "Reyes",
  "cellphone": "8091234567",
  "email": "jeison@example.com",
  "address": "San Francisco de Macoris"
}
```

Respuesta `201 Created`:

```json
{
  "profile": {
    "id": "66f0a1b2c3d4e5f6a7b8c9d0",
    "name": "Jeison",
    "lastName": "Reyes",
    "cellphone": "8091234567",
    "email": "jeison@example.com",
    "address": "San Francisco de Macoris"
  },
  "token": "eyJhbGciOiJSUzI1Ni ... "
}
```

- `400` si falta un campo o el email no tiene formato válido.
- `409` si ya existe un perfil con ese email (índice único en MongoDB).

#### `PUT /update-profile/{id}` — reemplazo total (requiere token del dueño)

Hay que mandar **los cinco campos**; PUT reemplaza el perfil entero.

```http
PUT https://localhost:8444/update-profile/66f0a1b2c3d4e5f6a7b8c9d0
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Jeison",
  "lastName": "Reyes Actualizado",
  "cellphone": "8091234567",
  "email": "jeison@example.com",
  "address": "Nueva direccion 123"
}
```

Respuesta `200 OK` con el perfil actualizado.

- `401` sin token o con token inválido/vencido.
- `403` si el token no pertenece a ese `{id}`.
- `404` si el perfil no existe.

#### `PATCH /update-profile/{id}` — actualización parcial (requiere token del dueño)

Manda solo los campos que quieras cambiar.

```http
PATCH https://localhost:8444/update-profile/66f0a1b2c3d4e5f6a7b8c9d0
Authorization: Bearer <token>
Content-Type: application/json

{
  "address": "Solo cambia la direccion"
}
```

Respuesta `200 OK` con el perfil completo ya actualizado. Mismos errores que `PUT`.

#### `DELETE /delete-profile/{id}` — borrar (requiere token del dueño)

```http
DELETE https://localhost:8444/delete-profile/66f0a1b2c3d4e5f6a7b8c9d0
Authorization: Bearer <token>
```

Respuesta `204 No Content`. Mismos errores que `PUT`.

#### `POST /refresh-token` — renovar el token (requiere token válido)

Sesión deslizante: mientras el token actual siga vigente, devuelve uno nuevo con el
reloj reiniciado. Si ya venció, responde `401` (recuperar acceso después de vencido
requeriría un login con contraseña, fuera del alcance de la prueba).

```http
POST https://localhost:8444/refresh-token
Authorization: Bearer <token>
```

Respuesta `200 OK`:

```json
{ "token": "eyJhbGciOiJSUzI1Ni... (nuevo, distinto al anterior)" }
```

#### `GET /internal/profile/{id}` y `GET /internal/profile/search?email=` — internos

Solo los llama `ms-profile-query`. Protegidos con **Basic Auth** (el par
`clientId` / `secret`), no con JWT. Devuelven un `ProfileResponse` o `404`.

```http
GET https://localhost:8444/internal/profile/search?email=jeison@example.com
Authorization: Basic <base64(clientId:secret)>
```

El `clientId` es `ms-profile-query` y el `secret` es el valor de
`INTERNAL_CLIENT_SECRET`. En la práctica esto lo arma `ms-profile-query` solo
(`BasicAuthClientFilter`); rara vez lo llamas a mano.

---

### `ms-profile-query` — `https://localhost:8445`

Ambos endpoints requieren `Authorization: Bearer <token>` (rol `profile-owner`) y solo
devuelven **tu propio** perfil.

#### `GET /get-profile` — mi perfil

El id sale del `sub` del JWT, no de la URL.

```http
GET https://localhost:8445/get-profile
Authorization: Bearer <token>
```

Respuesta `200 OK`:

```json
{
  "id": "66f0a1b2c3d4e5f6a7b8c9d0",
  "name": "Jeison",
  "lastName": "Reyes",
  "cellphone": "8091234567",
  "email": "jeison@example.com",
  "address": "San Francisco de Macoris"
}
```

- `401` sin token / token inválido.
- `404` si el perfil ya no existe en `ms-profile-crud`.
- `503` si `ms-profile-crud` no responde (timeout / circuit breaker abierto).

#### `GET /search-profile?email=` — buscar por email

```http
GET https://localhost:8445/search-profile?email=jeison@example.com
Authorization: Bearer <token>
```

Si el email existe pero **no es el tuyo**, responde `404` igual que si no existiera —
nunca `403`, para no revelar qué correos están registrados.

#### Ejemplo rápido con `curl`

```bash
# 1. Crear perfil y quedarte con el token
TOKEN=$(curl -sk -X POST https://localhost:8444/create-profile \
  -H "Content-Type: application/json" \
  -d '{"name":"Jeison","lastName":"Reyes","cellphone":"8091234567","email":"jeison@example.com","address":"SFM"}' \
  | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

# 2. Consultar tu perfil desde ms-profile-query
curl -sk https://localhost:8445/get-profile -H "Authorization: Bearer $TOKEN"
```

## Tests

`./mvnw test` en cada proyecto por separado. Ninguno de los dos necesita que el otro
esté corriendo:

- `ms-profile-crud` levanta contra Mongo (asegúrate de tener `docker compose up -d`).
- `ms-profile-query` mockea el cliente REST hacia `ms-profile-crud` y simula el JWT.

Los dos necesitan su `.env` con `INTERNAL_CLIENT_SECRET` (ver "Antes de arrancar").

## Kubernetes

Manifiestos en `k8s/`. Necesitas un clúster corriendo (Docker Desktop con Kubernetes
activado, o Minikube) — confírmalo con `kubectl get nodes`.

1. Construye la imagen de cada microservicio:
   ```powershell
   cd ms-profile-crud
   ./mvnw package "-Dquarkus.container-image.build=true" "-DskipTests"
   cd ..\ms-profile-query
   ./mvnw package "-Dquarkus.container-image.build=true" "-DskipTests"
   ```
   (en PowerShell, las comillas alrededor de cada `-D` son necesarias)
2. Crea los Secrets:
```bash
   cd k8s
   export INTERNAL_CLIENT_SECRET=$(openssl rand -base64 24)
   export MONGO_PASSWORD=$(openssl rand -base64 24)
   ./create-secrets.sh
```
3. Aplica los manifiestos — **el de Mongo primero**, los dos microservicios lo
   necesitan corriendo dentro del clúster para arrancar:
   ```powershell
   kubectl apply -f mongo.yaml
   kubectl apply -f ms-profile-crud.yaml
   kubectl apply -f ms-profile-query.yaml
   ```
4. Verifica que los tres queden `Running`:
   ```powershell
   kubectl get pods
   ```
5. Los Services son `ClusterIP` (no visibles desde fuera del clúster por diseño). Para
   probarlos desde tu máquina, abre un túnel a cada uno en su propia terminal:
   ```bash
   kubectl port-forward svc/ms-profile-crud 8444:8444
   kubectl port-forward svc/ms-profile-query 8445:8445
   ```
   Con los túneles abiertos, las mismas URLs de arriba funcionan igual.

### Actualizar el código dentro de Kubernetes

Un pod corre exactamente lo que quedó grabado en la imagen: cambiar el código no le
hace nada. Para que un cambio llegue, hay que reconstruir la imagen **del microservicio
que cambiaste** y reiniciar su Deployment, en ese orden:

```powershell
cd ms-profile-crud
./mvnw package "-Dquarkus.container-image.build=true" "-DskipTests"
kubectl rollout restart deployment/ms-profile-crud
```

Si el cambio tocó los dos microservicios, hay que reconstruir y reiniciar los dos.
`kubectl rollout restart` sin reconstruir antes solo reinicia el mismo contenedor de
siempre, no mete nada nuevo.
