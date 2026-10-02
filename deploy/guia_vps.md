# Guía paso a paso: VPS seguro para TFM (junior)

> **Cómo leer esta guía:** cubre **solo el TFM** (público, obligatorio).
> Decisión fijada: Nginx en Docker (único con `ports: 80/443`) + Certbot en host modo `webroot`.
> Plantillas en esta carpeta: `edge/compose.yml` (puerta Nginx sola), `edge/nginx.conf` (base) + `edge/conf.d/tfm.conf` (único server hoy), `tfm/compose.yml` (app+MariaDB, sin nginx), `.env.tfm.example`, `application-prod.properties.example`, `ssh-config.example`, `scripts/*`, `fail2ban/*`.
> El `.env` real vive solo en el VPS (`/opt/apps/tfm/.env`, `chmod 600`). Nunca en Git.

> **Objetivo:** TFM en `https://tfm.tudominio.es` con MariaDB privada y backups.
> 
> Escrita para nivel junior: cada fase dice QUÉ es, CÓMO se hace (comando copiable) y CÓMO comprobar que salió bien antes de seguir.

---

# 0. Arquitectura que vamos a construir

**TFM (público):**

```text
INTERNET
   |
HTTPS 443 (solo edge/nginx abre 80/443)
   |
   v
 edge/nginx (Docker aparte, recepcionista que cifra y reparte por conf.d/)
   |
   v
tfm.tudominio.es -> Spring Boot TFM :8080 (expose, en tfm/compose.yml)
   |
   v
MariaDB :3306 (expose, volumen mariadb_data, solo la ve Spring)
```

> **Internet solo entra a edge/nginx.** Ni Spring ni MariaDB tienen `ports:`.
> **Nginx va separado del TFM a propósito** (motivo, sin crear nada más): el día que
> despliegues otro proyecto añadirás su compose + 1 fichero en `edge/conf.d/` y recargarás
> Nginx, sin editar ni reiniciar el stack del TFM.

---

# 1. Qué vamos a instalar

Antes de empezar, conviene saber para qué sirve cada pieza.

| Herramienta          | Para qué sirve                                                |
| -------------------- | ------------------------------------------------------------- |
| Ubuntu               | Sistema operativo del VPS                                     |
| SSH                  | Permite administrar el servidor remotamente                   |
| UFW                  | Firewall sencillo de Ubuntu                                   |
| Fail2ban             | Bloquea determinados intentos repetidos de acceso             |
| Docker               | Ejecuta aplicaciones aisladas en contenedores                 |
| Docker Compose       | Permite gestionar varios contenedores juntos                  |
| MariaDB              | Base de datos del TFM                                         |
| Nginx                | Recibe las peticiones web y las dirige al contenedor correcto |
| Certbot              | Obtiene/renueva certificados HTTPS de Let's Encrypt           |
| Git                  | Descarga y actualiza el código                                |
| GitHub Actions       | Automatiza tests/build/deploy                                 |

No necesitamos instalar todas estas cosas al principio.

Las iremos instalando cuando realmente las necesitemos.

---

# 2. Conceptos importantes antes de empezar

## 2.1 IP pública

El VPS tendrá una IP pública, por ejemplo:

```text
203.0.113.50
```

Es la dirección que identifica al servidor en Internet.

**¿Es fija? Sí.** En OVH (y casi todos los VPS) la IP es estática: no cambia con `reboot`.
Solo cambiaría si destruyes el VPS y creas otro. No tienes que mirarla en el panel cada vez.
Guárdala como alias SSH (ver `ssh-config.example` en esta carpeta):

```sshconfig
Host vps-tfm
  HostName 203.0.113.50
  User deploy
  IdentityFile ~/.ssh/id_ed25519
```

Uso: `ssh vps-tfm`. Cuando el DNS apunte (`$TFM_DOMAIN -> IP`), dejarás de usar la IP directamente.

---

## 2.2 Puerto

Un servidor puede ofrecer distintos servicios mediante puertos.

Por ejemplo:

```text
22   -> SSH
80   -> HTTP
443  -> HTTPS
3306 -> MariaDB
8080 -> Spring Boot
```

No significa que todos deban estar abiertos.

De hecho, queremos que solo estén accesibles desde Internet los que realmente necesitamos.

---

## 2.3 `localhost`

Cuando una aplicación utiliza:

```text
localhost
```

está hablando de "este mismo equipo/contenedor".

En Docker aprenderemos que normalmente no debemos utilizar `localhost` para que un contenedor hable con otro.

Por ejemplo (en tu proyecto será `market-analysis-mysql`, aquí genérico):

```text
jdbc:mariadb://mariadb:3306/market_analysis
```

Aquí `mariadb` será el nombre del servicio Docker (en tu caso: `market-analysis-mysql`).

---

## 2.4 Root

`root` es el usuario administrador de Linux.

Tiene prácticamente todos los permisos.

No queremos trabajar normalmente como root porque un error puede afectar a todo el servidor.

Por eso crearemos:

```text
deploy
```

y utilizaremos `sudo` cuando necesitemos privilegios administrativos.

---

# 2.5 Contratar: qué pedir

Proveedor: el de tu guía es OVH, vale cualquiera similar.

Sistema: Ubuntu 24.04 LTS (el de tu guía).

Plan pequeño vale para empezar: 1-2 vCPU, 2-4GB RAM (tu AlphaSeeker + MariaDB + nginx caben).

Al contratar (OVH Ubuntu): no hay contraseña de root que elegir, root viene deshabilitado.
Te crean el usuario `ubuntu`; su contraseña temporal llega en un enlace seguro del mail de entrega.
Opcionalmente puedes subir tu clave pública SSH en el pedido para entrar sin password desde el inicio.

Al terminar te dan: IP pública, ejemplo 203.0.113.50. Es la dirección de tu casa en Internet.

---

# 3. FASE 1 — Primer acceso al VPS (OVH: usuario `ubuntu`, no `root`)

## Objetivo

Conectarnos al VPS por SSH.

> En OVHcloud Ubuntu el usuario `root` viene **deshabilitado de serie**.
> No entres como `root`. El mail de entrega te dice el usuario (en Ubuntu: `ubuntu`)
> y te manda la contraseña temporal en un enlace seguro. Úsala aquí.
> No ejecutes `sudo passwd root`: no queremos activar root.

Desde nuestro ordenador:

```bash
ssh ubuntu@IP_DEL_VPS
```

Por ejemplo:

```bash
ssh ubuntu@203.0.113.50
```

La primera vez puede aparecer algo parecido a:

```text
The authenticity of host ... can't be established.
Are you sure you want to continue connecting?
```

Es normal en el primer acceso.

Si la IP es realmente la de tu VPS:

```text
yes
```

Después introduce la contraseña temporal de `ubuntu` del enlace seguro de OVH.
Es posible que te obligue a cambiarla en el primer login: hazlo.

---

## Comprobar dónde estamos y obtener root temporal

Ejecuta:

```bash
whoami
```

Debería aparecer:

```text
ubuntu
```

Para tareas de administración usaremos `sudo`. Para seguir esta guía obtén root temporal:

```bash
sudo -i
whoami
```

Ahora debería aparecer:

```text
root
```

Desde aquí sigue FASE 2-4 como `root` (obtenido vía `sudo -i`, no vía `ssh root@...`).

Ahora:

```bash
hostname
```

Muestra el nombre del servidor.

Y:

```bash
uname -a
```

Muestra información del kernel y del sistema.

---

# 4. FASE 2 — Actualizar Ubuntu

## ¿Por qué?

Un VPS recién creado puede tener paquetes desactualizados.

Primero actualizamos la lista de paquetes:

```bash
apt update
```

### ¿Qué hace?

`apt` es el gestor de paquetes de Ubuntu.

`update` descarga información sobre las versiones disponibles.

Todavía no está actualizando los programas.

Después:

```bash
apt full-upgrade -y
```

### ¿Qué hace?

Instala las actualizaciones disponibles.

`-y` significa que respondemos automáticamente "sí" a las preguntas normales de confirmación.

---

## Reiniciar si es necesario

Si se actualiza el kernel o componentes importantes:

```bash
reboot
```

Esto reinicia el VPS.

La conexión SSH se cerrará.

Esperamos unos segundos y volvemos a conectar:

```bash
ssh ubuntu@IP_DEL_VPS
sudo -i
```

---

# 5. FASE 3 — Instalar herramientas básicas

Instalamos algunas herramientas que utilizaremos durante la administración:

```bash
apt install -y \
    curl \
    wget \
    git \
    nano \
    htop \
    ca-certificates \
    gnupg \
    ufw \
    fail2ban
```

## ¿Qué es cada una?

### curl

Permite hacer peticiones HTTP desde terminal.

Por ejemplo:

```bash
curl https://example.com
```

Lo utilizaremos para comprobar que servicios web responden.

### wget

Sirve para descargar archivos desde Internet.

### git

Lo utilizaremos para obtener el código del TFM.

### nano

Editor de texto sencillo.

Por ejemplo:

```bash
nano archivo.txt
```

### htop

Permite ver:

- CPU
- RAM
- procesos

Es muy útil para entender qué está consumiendo recursos.

### ca-certificates

Certificados necesarios para conexiones HTTPS.

### gnupg

Herramientas utilizadas para verificar paquetes/repositorios.

### ufw

Nuestro firewall.

### fail2ban

Ayuda a bloquear determinados intentos repetidos de acceso.

---

# 6. FASE 4 — Crear nuestro usuario `deploy`

No queremos administrar todo como root ni con el `ubuntu` inicial de OVH.
Ya estamos como `root` temporal vía `sudo -i` desde `ubuntu`; creamos `deploy`:

```bash
adduser deploy
```

Ubuntu preguntará por una contraseña y algunos datos.

Los datos personales adicionales pueden dejarse vacíos pulsando Enter.

Después:

```bash
usermod -aG sudo deploy
```

## ¿Qué hace?

Añade `deploy` al grupo `sudo`.

Esto significa que podrá ejecutar comandos administrativos utilizando:

```bash
sudo
```

Por ejemplo:

```bash
sudo apt update
```

---

# 7. FASE 5 — Configurar SSH con una clave

## ¿Qué es SSH?

SSH permite administrar el VPS desde nuestro ordenador.

En vez de conectarnos físicamente al servidor, hacemos:

```text
Nuestro PC
    |
    | SSH
    v
 VPS OVH
```

Queremos utilizar una **clave SSH** en lugar de una contraseña.

Una clave SSH tiene dos partes:

```text
clave privada -> permanece en nuestro PC
clave pública -> se copia al VPS
```

La clave privada **no debe enviarse a nadie**.

---

## 7.1 Comprobar si ya tenemos una clave

En nuestro ordenador Linux:

```bash
ls ~/.ssh/
```

Si ya tienes algo como:

```text
id_ed25519
id_ed25519.pub
```

puedes utilizarla.

Si no:

```bash
ssh-keygen -t ed25519
```

Pulsa Enter para aceptar la ubicación por defecto.

Recomendación: utiliza una passphrase para proteger la clave privada.

---

## 7.2 Copiar la clave al VPS

Desde nuestro PC:

```bash
ssh-copy-id deploy@IP_DEL_VPS
```

Después prueba:

```bash
ssh deploy@IP_DEL_VPS
```

Comprobamos:

```bash
whoami
```

Ahora debería aparecer:

```text
deploy
```

---

# 8. FASE 6 — Desactivar acceso SSH de root y contraseñas

**No hagas esto hasta comprobar que puedes entrar como `deploy`.**
En OVH `root` ya viene deshabilitado: aquí verificamos que siga así y quitamos login por contraseña.

Editamos:

```bash
sudo nano /etc/ssh/sshd_config
```

Busca estas opciones.

Queremos:

```text
PermitRootLogin no
PasswordAuthentication no
PubkeyAuthentication yes
```

Guardamos.

Antes de reiniciar SSH:

```bash
sudo sshd -t
```

Si no aparece ningún error, podemos reiniciar:

```bash
sudo systemctl restart ssh
```

## ¿Qué hace `systemctl`?

Es la herramienta para administrar servicios de Linux.

Por ejemplo:

```bash
sudo systemctl status ssh
```

muestra el estado de SSH.

---

## MUY IMPORTANTE

No cierres tu sesión SSH actual (`ubuntu`/`deploy`) todavía.

Abre otra terminal y prueba:

```bash
ssh deploy@IP_DEL_VPS
```

Solo cuando `ssh deploy@...` funcione **por clave** (sin pedir password),
y `sudo -i` funcione desde `deploy`, sigue al deshabilitado de `ubuntu`.
Nunca actives root con `sudo passwd root`: mantenlo deshabilitado como viene de OVH.

---

## Deshabilitar `ubuntu` (solo cuando `deploy` ya va por clave)

Momento elegido: justo aquí, tras FASE 6 verificada y antes de UFW.
Motivo: `PasswordAuthentication no` ya bloquea su password, pero su **clave**
inicial de OVH (si la pusiste al contratar) seguiría entrando. La anulamos:

```bash
# Desde deploy, con una sesión ubuntu aún abierta por si acaso:
sudo passwd -l ubuntu
sudo rm -f /home/ubuntu/.ssh/authorized_keys
# Caducar el login (conserva /home/ubuntu por si hay que rescatar algo):
sudo usermod --expiredate 1 ubuntu
```

Verificar (debe fallar):

```bash
ssh ubuntu@IP_DEL_VPS
# esperado: Permission denied (publickey) o Account expired
sudo passwd -S ubuntu # -> L = locked
```

A partir de aquí administra solo con `ssh deploy@IP` (+ `ssh-config.example` alias `vps-tfm`).
Si algún día pierdes la clave de `deploy`, rescatas por **consola web KVM de OVH**
(no por SSH): entras como root en consola y reactivas:

```bash
usermod --expiredate "" ubuntu
passwd -u ubuntu
# o crea nueva clave para deploy en /home/deploy/.ssh/authorized_keys
```

---

# 9. FASE 7 — Configurar el firewall UFW

## ¿Qué es un firewall?

Imagina que el VPS es una casa.

La IP es la dirección de la casa.

Los puertos son puertas.

El firewall decide qué puertas pueden utilizarse.

Queremos:

```text
22  -> SSH
80  -> HTTP
443 -> HTTPS
```

Y no queremos abrir (junior: si `nmap` los muestra, algo pusiste mal con `ports:`):

```text
3306 -> MariaDB (solo expose)
8080 -> Spring Boot (solo expose)
```

---

## 9.1 Denegar conexiones entrantes por defecto

```bash
sudo ufw default deny incoming
```

Significa:

> Si no hemos permitido explícitamente un puerto, se rechaza la conexión entrante.

---

## 9.2 Permitir conexiones salientes

```bash
sudo ufw default allow outgoing
```

Esto permite que el servidor pueda salir a Internet.

Por ejemplo:

```text
Spring Boot -> API de Finnhub
Ubuntu -> actualizaciones
```

---

## 9.3 Permitir SSH

```bash
sudo ufw allow 22/tcp
```

**Muy importante:** hacemos esto antes de activar el firewall para no quedarnos fuera.

---

## 9.4 Permitir HTTP

```bash
sudo ufw allow 80/tcp
```

Lo necesitaremos para HTTP y para determinadas comprobaciones de Let's Encrypt.

---

## 9.5 Permitir HTTPS

```bash
sudo ufw allow 443/tcp
```

Este será el puerto principal de nuestra web.

---

## 9.6 Activar firewall

```bash
sudo ufw enable
```

Comprobar:

```bash
sudo ufw status verbose
```

Deberías tener aproximadamente:

```text
22/tcp
80/tcp
443/tcp
```

---

## 9.7 Segunda muralla: firewall Edge de OVH (panel, 5 min, junior)

UFW vive **dentro** del VPS. OVH te da otra muralla **delante** del VPS (Network → IP →
firewall de tu IP pública): filtra antes de que el tráfico llegue, y se configura desde
el panel aunque el VPS esté caído. No sustituye a UFW ni a fail2ban (trabajan en paralelo):
si un día rompes UFW con una regla mala, el Edge te sigue cubriendo.

QUÉ significan las columnas del panel (junior):

* **Prioridad:** orden de lectura (el 0 manda primero; lo específico arriba, lo general abajo).
* **Modo:** Autorizar o Denegar.
* **IP origen:** quién llama. `Todos` (`0.0.0.0/0`) = todo Internet. Lo dejamos abierto porque
  tu autenticación es la llave SSH, no la IP (si pones solo tu casa y tu IP cambia, te encierras fuera).
* **Puerto origen:** el puerto del *cliente* (efímero, aleatorio) → siempre `1024-65535`.
  Trampa clásica: poner 22/80/443 aquí lo rompe todo (esos van en destino).
* **Puerto destino:** el *tuyo*: 22, 80, 443.
* **Estado TCP:** flags de conexión → `Ninguno` (no filtrar por flags).

Filas a crear (en este orden; sin protocolo TODOS se parte el deny en TCP+UDP):

| Prioridad | Modo      | Protocolo | IP origen | Puerto origen | Puerto destino | Estado TCP |
| --------- | --------- | --------- | --------- | ------------- | -------------- | ---------- |
| 0         | Autorizar | TCP       | Todos     | `1024-65535`  | 22             | Ninguno    |
| 1         | Autorizar | TCP       | Todos     | `1024-65535`  | 80             | Ninguno    |
| 2         | Autorizar | TCP       | Todos     | `1024-65535`  | 443            | Ninguno    |
| 10        | Denegar   | TCP       | Todos     | `1024-65535`  | `1-65535`      | Ninguno    |
| 11        | Denegar   | UDP       | Todos     | `1024-65535`  | `1-65535`      | Ninguno    |

* El resto de protocolos (AH/ESP/GRE/ICMP) quedan denegados: el **ping dejará de responder**
  (normal y buscado, no es avería; `nmap` y `curl` siguen igual).

CÓMO comprobar (sin cerrar tu SSH actual; el panel propaga en 1-2 min):

```bash
# otra terminal, desde tu PC:
ssh deploy@TU_IP
nmap TU_IP
# esperado: open solo 22,80,443
curl -I https://tfm.tudominio.es
# esperado: 200
```

Si SSH falla tras activar: desactívalo en el panel (el panel no depende del VPS) y revisa
que la fila 0 autoriza el 22 con origen `1024-65535` (no 22 en origen).

---

# 10. FASE 8 — Fail2ban

## ¿Para qué sirve?

Imagina que alguien intenta entrar repetidamente por SSH:

```text
usuario incorrecto
usuario incorrecto
usuario incorrecto
usuario incorrecto
...
```

Fail2ban analiza determinados logs y puede bloquear temporalmente IPs que presentan patrones de abuso.

No sustituye al firewall ni a las claves SSH.

Es una capa adicional.

> **Dónde va:** Fail2ban va instalado en el **host** (`apt install fail2ban`), no en Docker.
> Lee logs del host y banea vía `iptables`. Por eso el contenedor Nginx expone
> sus logs al host con el volumen `./logs:/var/log/nginx` (ver `edge/compose.yml`).

---

## Activarlo (sshd, por defecto)

```bash
sudo systemctl enable --now fail2ban
```

Comprobar:

```bash
sudo systemctl status fail2ban
```

Comprobar la protección SSH:

```bash
sudo fail2ban-client status sshd
```

---

## Jail nginx-401: aparcada (no ejecutar aquí)

> La jail `nginx-401` (401/403 vía Nginx Docker) **no se instala en FASE 8**.
> Requiere `/opt/apps/edge/logs/access.log`, que solo existe cuando edge/nginx está
> `up` con el volumen `./logs:/var/log/nginx` (ver `edge/compose.yml`).
> Si la instalas ahora fallará con `logpath not found`.
> Ir a **FASE 15a (tras Nginx `up` + primer certificado)** para instalarla.
> Aquí solo se deja `sshd`, que ya está verificado con `fail2ban-client status sshd`.

---

# 11. FASE 9 — Instalar Docker

## ¿Qué es Docker?

Docker nos permite ejecutar aplicaciones dentro de contenedores.

En nuestro caso podremos tener:

```text
Contenedor Spring Boot
Contenedor MariaDB
Contenedor OmniRoute
Contenedor Nginx
```

Cada uno tiene su propia configuración y entorno.

No significa que sean máquinas virtuales completas.

Son procesos aislados que comparten el kernel del servidor.

---

## Instalar Docker

Utiliza el procedimiento oficial de Docker para Ubuntu.

Después comprueba:

```bash
docker --version
```

Y:

```bash
docker compose version
```

Queremos tener disponibles:

```text
docker
docker compose
```

---

## Añadir `deploy` al grupo Docker

```bash
sudo usermod -aG docker deploy
```

Esto permite utilizar Docker sin escribir `sudo` delante de cada comando.

Cierra la sesión:

```bash
exit
```

Y vuelve a entrar:

```bash
ssh deploy@IP_DEL_VPS
```

Prueba:

```bash
docker ps
```

Si funciona, Docker está preparado.

> **Nota de seguridad:** pertenecer al grupo `docker` equivale en la práctica a tener privilegios muy elevados sobre el servidor. Por eso no debemos añadir usuarios que no sean de confianza.

---

# 12. FASE 10 — Probar Docker antes de desplegar nada

Ejecutamos un contenedor muy sencillo:

```bash
docker run --rm hello-world
```

## ¿Qué hace?

Docker:

1. Busca la imagen `hello-world`.
2. La descarga si no está disponible.
3. Crea un contenedor.
4. Lo ejecuta.
5. Muestra un mensaje.
6. `--rm` elimina el contenedor cuando termina.

Si aparece el mensaje de bienvenida de Docker, funciona.

---

# 13. FASE 11 — Crear estructura del servidor

Vamos a mantener nuestros servicios en:

```text
/opt/apps
```

Crear (junior: un stack por carpeta; edge es la puerta, tfm la app):

```bash
sudo mkdir -p /opt/apps/edge/conf.d
sudo mkdir -p /opt/apps/edge/logs
sudo mkdir -p /opt/apps/tfm
sudo mkdir -p /opt/apps/tfm/scripts
sudo mkdir -p /opt/backups
```

Dar la propiedad a `deploy`:

```bash
sudo chown -R deploy:deploy /opt/apps
sudo chown -R deploy:deploy /opt/backups
```

## ¿Por qué `/opt`?

Es una ubicación habitual para software/aplicaciones adicionales que instalamos nosotros.

No es obligatorio, pero mantiene una estructura limpia.

---

# 14. FASE 12 — Dominio y DNS (Parte I: solo `tfm`)

Necesitas UN dominio para el TFM. Ejemplo junior:

```text
tudominio.es → subdominio tfm.tudominio.es
```

Crea UN registro `A`:

```text
tfm.tudominio.es -> 141.94.250.163
```

## CÓMO comprobar (desde tu PC, DNS propagado tarda minutos-horas)

```bash
dig +short tfm.tudominio.es
# esperado: 141.94.250.163. Si sale vacío, espera al TTL y reintenta.
```

## ¿Qué significa?

Cuando alguien visita:

```text
https://$TFM_DOMAIN
```

el DNS le indica:

```text
La IP de ese dominio es IP_DEL_VPS
```

Entonces la petición llega a nuestro servidor.

---

# 15. FASE 13 — Docker Compose (Parte I: edge + TFM separados)

## QUÉ es (1 frase junior)

Compose es la receta que dice qué contenedores levantar juntos. Aquí hay **2 recetas
separadas a propósito**: `edge` (puerta Nginx sola) y `tfm` (app+MariaDB).
Comparten la red `front` (se crea una vez). OmniRoute va aparte.

## CÓMO se hace (copiar plantillas al VPS)

QUÉ copiar (2 recetas + 2 configs + scripts):

* Tu PC: `edge/compose.yml` → VPS: `/opt/apps/edge/compose.yml`
* Tu PC: `edge/nginx.conf` → VPS: `/opt/apps/edge/nginx.conf`
* Tu PC: `edge/conf.d/tfm.conf` → VPS: `/opt/apps/edge/conf.d/tfm.conf`
* Tu PC: `edge/conf.d/tfm-http-only.conf` → VPS: `/opt/apps/edge/conf.d/tfm-http-only.conf`
* Tu PC: `tfm/compose.yml` → VPS: `/opt/apps/tfm/compose.yml`
* Tu PC: `tfm/deploy-tfm.sh` → VPS: `/opt/apps/tfm/deploy-tfm.sh`
* Tu PC: `tfm/backup-mariadb.sh` → VPS: `/opt/apps/tfm/backup-mariadb.sh`
* Tu PC: `scripts/render-nginx-config.sh` → VPS: `/opt/apps/tfm/scripts/render-nginx-config.sh`
* Tu PC: `scripts/certbot-first-cert.sh` → VPS: `/opt/apps/tfm/scripts/certbot-first-cert.sh`

Vía A — `scp` (recomendada, desde tu PC, con `/opt/apps/{edge,tfm}` ya creados):

```bash
scp edge/compose.yml deploy@IP:/opt/apps/edge/compose.yml
scp edge/nginx.conf deploy@IP:/opt/apps/edge/nginx.conf
scp edge/conf.d/tfm.conf deploy@IP:/opt/apps/edge/conf.d/tfm.conf
scp edge/conf.d/tfm-http-only.conf deploy@IP:/opt/apps/edge/conf.d/tfm-http-only.conf
scp tfm/compose.yml deploy@IP:/opt/apps/tfm/compose.yml
scp tfm/deploy-tfm.sh deploy@IP:/opt/apps/tfm/deploy-tfm.sh
scp tfm/backup-mariadb.sh deploy@IP:/opt/apps/tfm/backup-mariadb.sh
scp scripts/render-nginx-config.sh deploy@IP:/opt/apps/tfm/scripts/render-nginx-config.sh
scp scripts/certbot-first-cert.sh deploy@IP:/opt/apps/tfm/scripts/certbot-first-cert.sh
```

En el VPS (una vez, red compartida + propiedad):

```bash
sudo mkdir -p /opt/apps/edge/conf.d /opt/apps/edge/logs
sudo chown -R deploy:deploy /opt/apps /opt/backups
docker network create front
chmod +x /opt/apps/tfm/deploy-tfm.sh /opt/apps/tfm/backup-mariadb.sh /opt/apps/tfm/scripts/*.sh
```

Vía B — `nano` + pegar (si `scp` te lía): crea los mismos 4 ficheros a mano con el
contenido de las plantillas (`Ctrl+O Enter Ctrl+X`).

QUÉ adaptar antes del primer `up` (3 cosas, nada más):

1. En `.env`: crea `/opt/apps/tfm/.env` desde `.env.tfm.example` con `chmod 600` y **edita `TFM_DOMAIN=tu-dominio.real`**.
2. En `tfm/compose.yml`: `image: ghcr.io/tu-usuario/tfm:1.0.0` → tu imagen real del TFM (o usa `GHCR_REPOSITORY` y `TFM_TAG` del `.env`).
3. Los configs nginx (`tfm.conf`, `tfm-http-only.conf`) usan placeholder `${TFM_DOMAIN}`; **se renderizan automáticos** con `deploy-tfm.sh`.
   Sin ese fichero `up` falla con `env file not found` (normal, créalo).
4. **Respalda el `.env` en Bitwarden ANTES del primer `up` (obligatorio, junior):**
   el VPS es reemplazable, el `.env` no. Crea la nota `vps-tfm-env` y pega los 14 valores
   (`DB_URL/DB_DATABASE/DB_USER/DB_PASSWORD/DB_ROOT_PASSWORD/APP_SECURITY_USERNAME/APP_SECURITY_PASSWORD/FINNHUB_API_TOKEN/POLYGON_API_TOKEN/OPENROUTER_API_KEY/OPENROUTER_MODEL/TFM_DOMAIN/CERTBOT_EMAIL/VPS_HOST/GHCR_REPOSITORY/TFM_TAG` + fallbacks).
   Nunca en Git ni en Drive sin cifrar. Verificación: bloquea Bitwarden, reabre la nota y
   confirma que están los 14 (los `DB_PASSWORD`/`APP_SECURITY_*` generados con `openssl rand`,
   no `admin` ni placeholders). Sin esta copia no hay restauración posible si el VPS muere.

Orden de arranque (junior: aquí todavía NO se levanta nada, eso es FASE 14):

```bash
# SOLO VALIDACIÓN (no hace up): imprime la receta resuelta
docker compose -f /opt/apps/tfm/compose.yml config
docker compose -f /opt/apps/edge/compose.yml config
# esperado: imprimen la receta sin errores. Si dice env file not found → crea el .env.
# Si dice network front not found → crea: docker network create front
```

> **REGLA DE ORO (Capa 1, anti-despiste):** en el VPS solo se hace `up` desde
> `/opt/apps/tfm` y `/opt/apps/edge`. El repo clonado en `/opt/apps/tfm/repo`
> **solo se lee** (`build: ./repo`). Nunca `cd repo && docker compose up`.
> Por qué (junior, 2 motivos reales de tu proyecto):
> 
> 1. Tu compose local publica `8080:8080` y `5005:5005` en `0.0.0.0`: en tu PC da igual,
>    en el VPS el debug Java quedaría público y la app bypasearía a Nginx. UFW no te
>    salvaría (Docker publica vía iptables por encima de UFW).
> 2. El nombre de proyecto Compose sale de la carpeta: levantar desde `repo` crearía
>    redes/volúmenes `repo_*` separados → **dos MySQL con datos distintos** sin saber cuál es el bueno.

---

# 16. Redes Docker

Docker puede crear una red privada entre contenedores.

Por ejemplo:

```text
tfm
 |
 +---- mariadb
 |
 +---- nginx
```

Dentro de esa red los servicios pueden utilizar nombres:

```text
mariadb
tfm
```

En vez de utilizar IPs manualmente.

---

# 17. MariaDB

## ¿Qué es?

MariaDB es el motor de base de datos que utilizaremos para producción.

El TFM tendrá:

```text
Spring Boot
     |
     v
 MariaDB
```

Pero MariaDB **no debe estar expuesta directamente a Internet**.

Por eso no queremos:

```text
Internet -> IP:3306 -> MariaDB
```

Queremos:

```text
Spring Boot (app) -> market-analysis-mysql:3306
```

dentro de Docker (nombres de TUS servicios; la plantilla vieja decía `mariadb:3306`).

---

# 18. Credenciales de producción (nombres REALES de tu proyecto)

No debemos poner contraseñas directamente en Git.

Crearemos `/opt/apps/tfm/.env` en el servidor con TUS nombres (tu `application-prod.properties`
lee `DB_URL/DB_USER/DB_PASSWORD`, y tu compose `mysql` usa `MYSQL_*`; la imagen mariadb los acepta):

```env
DB_URL=jdbc:mariadb://market-analysis-mysql:3306/marketanalysisdb?rewriteBatchedStatements=true
DB_DATABASE=marketanalysisdb
DB_USER=marketuser
DB_PASSWORD=CAMBIAR_openssl_1
DB_ROOT_PASSWORD=CAMBIAR_openssl_2
APP_SECURITY_USERNAME=CAMBIAR_openssl_3
APP_SECURITY_PASSWORD=CAMBIAR_openssl_4
```

> Plantilla completa y comentada: `.env.tfm.example` en esta carpeta.
> En el VPS: `chmod 600` cada `.env`. OJO: es `DB_USER`, no `DB_USERNAME`.

Genera contraseñas aleatorias:

```bash
openssl rand -hex 32
```

Hazlo varias veces y utiliza valores distintos.

---

# 19. Proteger `.env`

El archivo:

```text
.env
```

contendrá información sensible.

Configura:

```bash
chmod 600 .env
```

Esto restringe el acceso al propietario.

En el repositorio Git:

```gitignore
.env
*.env
```

---

# 20. Configuración de Spring Boot

En producción no queremos:

```properties
spring.datasource.password=mi-password-real
```

Queremos:

```properties
spring.datasource.password=${DB_PASSWORD}
```

Spring Boot leerá la variable de entorno.

Ejemplo (nombres reales de tu `application-prod.properties`):

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
```

Y en `/opt/apps/tfm/.env`:

```env
DB_URL=jdbc:mariadb://market-analysis-mysql:3306/marketanalysisdb?rewriteBatchedStatements=true
DB_USER=marketuser
DB_PASSWORD=...
```

---

# 21. Importante: `market-analysis-mysql` no es una IP

En:

```text
jdbc:mariadb://market-analysis-mysql:3306/marketanalysisdb
```

`market-analysis-mysql` es el nombre del contenedor Docker (tu servicio `mysql`).

Docker se encarga de resolver ese nombre dentro de la red `back`.

Esto es una de las ventajas principales de Compose.

---

# 22. FASE 14 — Desplegar el TFM (con código, vía wrapper)

Antes de automatizar nada con GitHub Actions, desplegamos una vez a mano con el
wrapper (si algo falla, sabremos en qué paso fue).

## QUÉ es

`tfm/deploy-tfm.sh` hace en orden: `git pull` del repo → `up mysql` (sola, sin tu
código) → `build + up app` desde tu `Dockerfile` multistage → checks de puertos.
**Además, renderiza automáticamente los configs de Nginx** sustituyendo `${TFM_DOMAIN}`
en `tfm.conf` y `tfm-http-only.conf` (usa `scripts/render-nginx-config.sh`).
Tú no escribes `docker compose` a mano: el wrapper se niega si lo ejecutas dentro
del repo (Capa 1).

## CÓMO se hace

```bash
cd /opt/apps/tfm
git clone <URL_DE_TU_REPO> ./repo
cp /ruta/a/.env.tfm.example /opt/apps/tfm/.env  # o nano .env con tus valores
chmod 600 /opt/apps/tfm/.env
# IMPORTANTE: edita /opt/apps/tfm/.env y pon tu dominio real en TFM_DOMAIN
./deploy-tfm.sh
# verás: commit desplegado (rollback = ese commit) → mysql healthy → Started Application → ps
#       → configs nginx renderizados con tu TFM_DOMAIN
```

Después la puerta (edge) en 3 pasos manuales (A/B/C), porque sin cert Nginx no arranca.
**Los ficheros ya llevan tu dominio** (renderizados por deploy-tfm.sh):

```bash
# Paso A — HTTP sin cert (tfm-http-only.conf, NUNCA tfm.conf completo aún):
cd /opt/apps/edge/conf.d
mv tfm.conf tfm.conf.full                    # aparta el completo (pide .pem que no existen)
# tfm-http-only.conf YA está renderizado con tu dominio
cd /opt/apps/edge && docker compose up -d
docker compose exec nginx getent hosts app
# esperado: una IP (edge ve a la app por la red front compartida)
curl -I http://$TFM_DOMAIN
# esperado: 200 'edge HTTP OK'. Si nginx sale 'restarting': mira logs,
# casi seguro es 'cannot load certificate' = levantaste tfm.conf sin certs. Vuelve al paso A.
```

```bash
# Paso B — Certbot (FASE HTTPS): nacen los .pem, verifica:
# certbot-first-cert.sh lee TFM_DOMAIN y CERTBOT_EMAIL de /opt/apps/tfm/.env
./scripts/certbot-first-cert.sh
ls /etc/letsencrypt/live/$TFM_DOMAIN/
# esperado: fullchain.pem + privkey.pem
```

```bash
# Paso C — server completo con HTTPS:
cd /opt/apps/edge/conf.d
rm tfm-http-only.conf
mv tfm.conf.full tfm.conf   # tfm.conf YA está renderizado con tu dominio
cd /opt/apps/edge && docker compose up -d
docker compose exec nginx nginx -s reload
curl -I https://$TFM_DOMAIN
# esperado: 200. Desde tu PC: nmap TU_IP (solo 22/80/443)
```

## Si alguien levantó el repo por error (emergencia, junior)

```bash
cd /opt/apps/tfm/repo && docker compose down   # apaga el duplicado YA
docker volume ls | grep repo_                   # si hay repo_* con datos, no los borres a ciegas
sudo ss -tulpn | grep -E '8080|5005'            # ya no debe salir 0.0.0.0:8080/5005
cd /opt/apps/tfm && docker compose ps          # el bueno (mysql_tfm_data) sigue up
```

## Nota `script-bd.sql` (tu init de BD, junior)

Tu `script-bd.sql` es **solo estructura** (`CREATE TABLE`, sin usuarios ni passwords):
**no se toca al cambiar claves** — las passwords viven solo en el `.env` antes del primer `up`.
Dos reglas:

1. Solo actúa al nacer el volumen `mysql_tfm_data` (vacío). Cambiar el `.env` después **no**
   cambia la BD en marcha: haría falta `ALTER USER` o recrear el volumen (con backup previo).
2. **Nunca lo re-ejecutes a mano contra prod**: trae `DROP TABLE IF EXISTS` por tabla y
   borraría `candles`, `stocks`, `strategies` y demás. Cambios de esquema futuros =
   migraciones versionadas (Flyway/Liquibase), no re-dumps.

## Capa 2 — 2 líneas en tu repo (hazlo en tu proyecto, invisible en dev)

En tu `docker-compose.yml` local, ata los puertos a localhost para que un `up`
accidental en el VPS no exponga nada (en tu PC sigues abriendo `localhost:8080` igual):

```yaml
    ports:
      - "127.0.0.1:${APP_PORT_EXTERNAL:-8080}:8080"
      - "127.0.0.1:5005:5005"
```

Ojo honesto: esto tapa la **exposición**, no la **duplicación** de BD (eso lo evita la
regla de oro + el wrapper). Por eso van juntas: Capa 1 + Capa 2.

---

# 23. Dockerfile del TFM

El proyecto deberá tener un `Dockerfile`.

La idea es:

```text
Código Java
     |
     v
Maven
     |
     v
JAR
     |
     v
Imagen Docker
     |
     v
Contenedor Spring Boot
```

No es necesario instalar Java/Maven directamente en el VPS si el Dockerfile realiza el build.

---

# 24. No utilizar `latest` indiscriminadamente

En producción es mejor fijar versiones.

Por ejemplo:

```text
mariadb:10.11
```

en vez de:

```text
mariadb:latest
```

Así evitamos que una actualización automática cambie inesperadamente el software.

---

# 25. Nginx

## ¿Qué es?

Nginx será nuestro "recepcionista".

Internet hará:

```text
https://$TFM_DOMAIN
```

Nginx recibirá la petición y la enviará internamente a:

```text
app:8080
```

(servicio `app` de `tfm/compose.yml`; mantenemos tus nombres del repo).

El usuario nunca necesita saber que Spring Boot está escuchando en 8080.

Arquitectura:

```text
Internet
   |
   v
Nginx :443
   |
   v
Spring Boot :8080
```

---

# 26. Reverse proxy

> **Un solo contenedor Nginx (edge) para todas las apps futuras.** No se crea un Nginx por app:
> cada proyecto aporta su fichero en `edge/conf.d/` (hoy solo `tfm.conf`) con su
> `server { server_name ...; proxy_pass ...; }`. Solo edge bindea `80/443`; el resto llega por `server_name`.

Esto se llama **reverse proxy**.

Nginx recibe la petición y decide a qué servicio interno debe enviarla.

Podemos tener:

```text
$TFM_DOMAIN
        |
        v
      Nginx (edge)
        |
        v
      app:8080
```

---

# 27. HTTPS

No queremos que el TFM funcione solamente con:

```text
http://$TFM_DOMAIN
```

Queremos:

```text
https://$TFM_DOMAIN
```

HTTPS cifra la comunicación entre el navegador y el servidor.

Utilizaremos certificados de Let's Encrypt.

Certbot ayuda a obtener y renovar esos certificados.

---

# 28. Certbot webroot en host

> No usar `certbot --nginx`: solo sirve con Nginx en host. Aquí Nginx va en Docker,
> así que Certbot corre en host en modo `webroot` y comparte volúmenes (ver `edge/compose.yml` y `edge/conf.d/tfm.conf`).
> Solo pedimos cert para `tfm.*`.

Nginx sirve el reto en `/.well-known/acme-challenge/` desde `/var/www/certbot`:

```nginx
location /.well-known/acme-challenge/ {
  root /var/www/certbot;
}
```

Primer certificado (DNS `tfm` propagado + Nginx Docker levantado con bloque :80):

```bash
sudo mkdir -p /var/www/certbot
# ver scripts/certbot-first-cert.sh (DOMAINS por defecto: solo tfm):
DOMAINS=tfm.tudominio.es EMAIL=tu-email@example.com ./scripts/certbot-first-cert.sh
# equivale a:
sudo certbot certonly --webroot -w /var/www/certbot -d tfm.tudominio.es
```

El "secreto" que pide Let's Encrypt es un token aleatorio de un solo uso por dominio.
No es contraseña: cualquiera lo puede ver, solo demuestra que controlas el DNS
al servirlo en esa URL. Caduca tras la emisión.

Renovación + recarga Nginx Docker:

```bash
sudo certbot renew --dry-run
# hook automático: /etc/letsencrypt/renewal-hooks/deploy/reload-nginx-docker.sh
# ver scripts/renew-hook-reload-nginx.sh (usa /opt/apps/edge):
docker compose -f /opt/apps/edge/compose.yml exec nginx nginx -s reload
```

---

# 28b. FASE 15a — Jail nginx-401 (tras edge `up`, NO en FASE 8)

## QUÉ es (junior, 1 frase)

Tu web ya registra quién recibe `401/403` (login mal, rutas protegidas) en
`/opt/apps/edge/logs/access.log`. Esta jail lee ese fichero y echa 1h a la IP que
falle 10 veces en 5 min. `sshd` no se toca.

## Requisito previo (si falta, no seguir): 2 comandos, 10 segundos

```bash
cd /opt/apps/edge && docker compose ps nginx
# esperado: STATUS = Up (no restarting, no exited)
ls -l /opt/apps/edge/logs/access.log
# esperado: el fichero EXISTE (puede estar vacío o con pocas líneas: normal si aún
# no hay 401/403; lo importante es que exista, porque es lo que lee fail2ban)
```

Por qué estos 2 (junior): el 1º dice “Nginx vive”; el 2º dice “su diario existe”.
Sin diario no hay nada que vigilar y la jail fallaría con `logpath not found`.

Plantillas en esta carpeta: `fail2ban/filter.d/nginx-401.conf`,
`fail2ban/jail.d/nginx-401.local`, `scripts/fail2ban-install-nginx-401.sh`.

## Instalación (junior: 2 ficheros + 4 comandos)

```bash
# 1. Desde tu PC, copia las 2 plantillas al VPS:
scp fail2ban/filter.d/nginx-401.conf deploy@TU_IP:/tmp/nginx-401.conf
scp fail2ban/jail.d/nginx-401.local deploy@TU_IP:/tmp/nginx-401.local
# 2. En el VPS, colócalas y activa (10 fallos en 5 min -> ban 1h):
sudo cp /tmp/nginx-401.conf /etc/fail2ban/filter.d/nginx-401.conf
sudo cp /tmp/nginx-401.local /etc/fail2ban/jail.d/nginx-401.local
sudo fail2ban-regex /opt/apps/edge/logs/access.log /etc/fail2ban/filter.d/nginx-401.conf
# esperado: lines matched (si dice 0 matched con log vacío, normal: aún no hay 401/403)
sudo systemctl restart fail2ban
sudo fail2ban-client status nginx-401
# esperado: la jail existe, 0 baneadas al principio (normal)
# Si te baneas probando, sal por otra IP o KVM y libera la tuya:
sudo fail2ban-client set nginx-401 unbanip TU_IP
```

## Para entenderlo (junior, sin prisa): programa, patrón y celda

Ni el `.conf` ni el `.local` hacen nada solos (son texto). Quien vigila, cuenta y banea
es el programa **`fail2ban`** (el portero del edificio, ya corriendo). Cada **jail** es
una “celda” que le dice qué diario leer, qué patrón buscar y cuánto castigar
(jail = celda en inglés: cada una encierra a un tipo de pesado distinto).

| Pieza | Qué es | Ejemplo tuyo |
|---|---|---|
| Programa | el que ejecuta todo | `fail2ban` (uno solo) |
| Jail | una celda: diario + patrón + castigo | `sshd`, `nginx-401` (las ves en `fail2ban-client status`) |
| Filtro (`.conf`) | el patrón: qué cuenta como fallo | `nginx-401.conf`: línea con `401`/`403` |
| Celda (`.local`) | dónde mirar y cuánto castigar | `nginx-401.local`: `access.log`, 10 en 5 min → 1h sin 80/443 |

El filtro, pieza a pieza (línea 7 de `nginx-401.conf`):

```regex
^<HOST> - \S+ \[.*?\] "(GET|POST|...) .* HTTP/.*" (401|403)( |$).*
```

* `^<HOST>` = la línea empieza por una IP (es la futura baneada; vale IPv4 e IPv6).
* `- \S+ \[...\]` = relleno del formato de log (usuario, fecha) que hay que saltar.
* `"(GET|POST|...)"` + `.* HTTP/.*` = método y URL dan igual (cualquiera vale).
* `(401|403)` = **el corazón**: solo cuentan No autorizado/Prohibido.
* La línea 10 (`ignoreregex`) excluye `/.well-known/acme-challenge/`: aunque un reto ACME
  trajera un 401 (no es el caso: da 200), nunca contaría. Así Let's Encrypt renovando
  jamás puede autobanearse.

La celda, en 1 frase (`nginx-401.local`): *“vigila `access.log` con ese filtro; a la IP
que sume 10 fallos en 5 minutos (`maxretry`/`findtime`), ciérrale el 80/443 una hora
(`bantime`, `port = http,https)`”*. El SSH de esa IP seguiría funcionando (eso lo lleva
la celda `sshd`). `unbanip TU_IP` es el perdón: saca una IP de la lista (sustituye `TU_IP`
por la real de `Banned IP list`; con la lista vacía da error porque no hay a quién perdonar).

---

# 35. FASE 17 — Backups

## ¿Por qué?

Tener el TFM funcionando no significa que nuestros datos estén protegidos.

Si MariaDB contiene:

```text
estrategias
análisis
usuarios
configuración
```

necesitamos poder recuperarlos.

---

# 36. Backup de MariaDB (junior: tu primera copia, 2 comandos)

> Script listo: `tfm/backup-mariadb.sh` en esta carpeta (usa TUS nombres:
> contenedor `market-analysis-mysql` + `DB_USER/DB_PASSWORD/DB_DATABASE` del `.env`).
> Copia y ejecuta en el VPS:

```bash
cp tfm/backup-mariadb.sh /opt/apps/tfm/backup-mariadb.sh  # desde la carpeta del proyecto, en tu PC usa scp
chmod +x /opt/apps/tfm/backup-mariadb.sh
/opt/apps/tfm/backup-mariadb.sh
ls -l /opt/backups
# esperado: mariadb-FECHA.sql.gz con tamaño > 0. Si dice contenedor no encontrado: ps en /opt/apps/tfm.
```

> Cron (cuando el manual funcione; log en `/opt/backups` porque `deploy` no puede
> escribir en `/var/log` y fallaría en silencio):
> `30 2 * * * /opt/apps/tfm/backup-mariadb.sh >> /opt/backups/backup-cron.log 2>&1`.
> Verificación al día siguiente: `ls -l /opt/backups` (copia ~02:30) + `tail /opt/backups/backup-cron.log`.

Utilizaremos `mysqldump`/herramienta equivalente para generar una copia.

Conceptualmente:

```text
MariaDB
   |
   v
backup.sql
   |
   v
backup.sql.gz
```

Comprimiremos los backups para ahorrar espacio.

---

# 37. Backup local NO es suficiente

Esto:

```text
VPS
 |
 +-- aplicación
 +-- MariaDB
 +-- backup
```

no es suficiente.

Si perdemos el VPS:

```text
VPS perdido
     |
     +-- aplicación perdida
     +-- MariaDB perdida
     +-- backup perdido
```

Por eso queremos:

```text
VPS
 |
 +-- backup local
 |
 +---> almacenamiento externo
```

---

# 38. Probar restauración

No basta con crear backups.

Hay que comprobar que funcionan.

Periódicamente:

```text
backup
  |
  v
restaurar
  |
  v
comprobar datos
```

Un backup que nunca hemos restaurado es solamente una suposición.

---

# 39. FASE 18 — GitHub Actions

Una vez que el despliegue manual funcione, automatizamos.

Nuestro flujo será:

```text
Código
   |
   v
Pull Request
   |
   +-- tests
   +-- compile
   +-- JaCoCo
   +-- Sonar
   |
   v
merge a main
   |
   v
GitHub Actions
   |
   v
VPS
   |
   v
Docker Compose
   |
   v
nueva versión
```

---

# 40. ¿Por qué no empezar directamente con CI/CD?

Porque si automatizamos antes de entender el despliegue:

```text
algo falla
   |
   v
¿GitHub?
¿SSH?
¿Docker?
¿Nginx?
¿Spring?
¿MariaDB?
```

será difícil localizarlo.

Primero:

```text
deploy manual
```

Después:

```text
deploy automático
```

---

# 41. SSH para GitHub Actions

GitHub Actions necesitará entrar al VPS.

No utilizaremos:

```text
root
```

Crearemos una clave específica para el despliegue.

Conceptualmente:

```text
GitHub Actions
      |
      | clave SSH de deploy
      v
VPS
      |
      v
usuario deploy
```

La clave privada se almacenará como GitHub Secret.

---

# 42. GitHub Secrets

En GitHub:

```text
Repository
  -> Settings
  -> Secrets and variables
  -> Actions
```

Podremos almacenar:

```text
VPS_HOST
VPS_USER
VPS_SSH_KEY
```

Nunca meter estas claves directamente en:

```text
.github/workflows/*.yml
```

---

# 43. Secretos de la aplicación

No es necesario que GitHub Actions conozca todas las claves de producción.

Podemos mantener en el VPS:

```text
/opt/apps/tfm/.env
```

y hacer que Docker Compose lo utilice.

Así las API keys de producción no tienen que viajar por GitHub Actions.

---

# 44. FASE 19 — Comprobaciones habituales

## Ver contenedores

```bash
docker ps
```

Muestra contenedores activos.

---

## Ver todos los contenedores

```bash
docker ps -a
```

Incluye contenedores detenidos.

---

## Ver logs

```bash
docker compose logs
```

Para seguirlos en tiempo real:

```bash
docker compose logs -f
```

Para un servicio (nombres reales del stack TFM: `app` y `mysql`):

```bash
cd /opt/apps/tfm && docker compose logs -f app
cd /opt/apps/tfm && docker compose logs -f mysql
```

---

## Ver consumo

```bash
docker stats
```

Muestra:

- CPU
- memoria
- red
- procesos

---

# 45. Comprobar RAM (junior: mira `available`, no `free`)

```bash
free -h
```

`-h` significa "human readable".

Linux usa la RAM libre como caché (`buff/cache`): ver `free` bajo es normal.
La columna que importa es **`available`** = lo realmente usable.
Peligro real = `available` < 300MB con `load` alto (ver `htop` arriba).

Foto base real de este VPS (01-10-2026, para comparar cuando llegue el 2º proyecto):

```text
Mem: 3.7Gi total | available 2.6Gi
nginx 4.7MiB | market-analysis-app 430MiB (-Xmx512m) | market-analysis-mysql 60MiB (innodb 64M)
Docker total ~495MB. Un 2º Spring+MySQL (~500MB) cabe.
```

```bash
docker stats --no-stream
# MEM USAGE por contenedor. Si app supera su Xmx o mysql crece sin límite, aquí se ve.
```

---

# 45b. Swap 2GB (red de seguridad, una vez, junior)

Sin swap, un pico de RAM (build Maven + 2 apps + import BD a la vez) termina en OOM killer
matando lo que pille. Con swap, se degrada lento y te da tiempo a verlo. Nuestro VPS
vino con `Swap: 0B`: lo creamos una vez.

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
free -h
# esperado: Swap: 2.0Gi (puede mostrar 0B en uso, normal: solo se usa al necesitarla)
swapon --show
# esperado: /swapfile ... 2G
```

Si algún día quieres quitarlo: `sudo swapoff /swapfile` + borra su línea de `/etc/fstab`.

---

# 46. Comprobar disco

```bash
df -h
```

Muy importante porque Docker y los logs pueden consumir espacio.

---

# 47. Ver procesos

```bash
htop
```

Salir normalmente con:

```text
F10
```

---

# 48. Comprobar servicios Linux

```bash
systemctl --failed
```

Muestra servicios que han fallado.

---

# 49. Comprobar firewall

```bash
sudo ufw status
```

---

# 50. Comprobar Fail2ban

```bash
sudo fail2ban-client status
```

Y:

```bash
sudo fail2ban-client status sshd
```

Tras FASE 15a (Nginx `up`):

```bash
sudo fail2ban-client status nginx-401
```

Si `nginx-401` dice `logpath not found`, aún no desplegaste Nginx: vuelve a FASE 15a.

---

# 51. FASE 20 — Comprobación de puertos

Una vez desplegado todo, queremos comprobar que no hemos abierto accidentalmente servicios.

Desde nuestro ordenador podemos utilizar:

```bash
nmap IP_DEL_VPS
```

El objetivo es que Internet vea aproximadamente:

```text
22
80
443
```

No queremos:

```text
3306
8080
```

abiertos públicamente.

---

# 52. Regla de oro con Docker + UFW (CRÍTICO)

> **Docker se salta UFW por defecto.** Publicar con `ports:` abre el puerto vía `iptables`
> aunque UFW diga `deny`. UFW solo te protege de verdad si **solo Nginx tiene `ports:`**.

```bash
# Ver qué escucha de verdad (no te fíes solo de `ufw status`):
sudo ss -tulpn
sudo ufw status verbose
# Desde tu PC:
nmap IP_DEL_VPS  # objetivo: solo 22,80,443
```

No debemos confundir:

```yaml
ports:
  - "8080:8080"
```

con:

```yaml
expose:
  - "8080"
```

`ports` puede publicar el puerto hacia el host.

Si ponemos:

```yaml
ports:
  - "8080:8080"
```

podemos terminar teniendo:

```text
Internet -> VPS:8080 -> Spring Boot
```

cuando queremos:

```text
Internet -> Nginx -> Spring Boot
```

Por eso revisaremos cuidadosamente los puertos de Compose.

---

# 53. Spring Boot detrás de Nginx

La arquitectura final será:

```text
Navegador
    |
    | HTTPS :443
    v
 edge/nginx
    |
    | red Docker front
    v
 app:8080 (servicio app de tfm/compose.yml)
    |
    v
 market-analysis-mysql:3306 (red back privada)
```

El usuario nunca necesita conectarse directamente a:

```text
IP_VPS:8080
```

---

# 55. Qué NO guardar en Git

Nunca subir:

```text
.env
API keys
passwords
private SSH keys
certificados privados
backups
tokens
cookies
datos personales
```

Antes de subir cambios:

```bash
git status
```

Y revisar especialmente:

```text
application.properties
application.yml
compose.yml
.env
logs
```

---

# 56. Si una API key se publica accidentalmente

No basta con borrar el archivo.

Si una clave real ha llegado a GitHub:

1. Revocarla.
2. Generar otra.
3. Revisar dónde se utilizó.
4. Eliminar el secreto del repositorio/historial si procede.

Una clave publicada debe considerarse comprometida.

---

# 57. Actualizaciones del VPS

Periódicamente:

```bash
sudo apt update
```

Después:

```bash
sudo apt upgrade
```

Antes de actualizaciones importantes:

```text
1. Backup
2. Actualizar
3. Comprobar servicios
4. Comprobar aplicación
5. Revisar logs
```

---

# 58. Actualizar Docker

No actualizar imágenes de producción sin pensar.

Primero comprobar qué estamos utilizando:

```bash
docker compose config
```

Después actualizar de forma controlada.

Especialmente cuidado con:

```text
MariaDB
Spring Boot
Nginx
OmniRoute
```

---

# 59. Rollback

Si una versión nueva del TFM rompe producción:

```text
versión nueva
     |
     v
problema
     |
     v
volver a versión anterior
```

Por eso debemos conservar:

- commit anterior
- imagen anterior si es necesario
- backup de base de datos

No debemos realizar migraciones destructivas de base de datos sin backup.

---

# 60. Checklist de seguridad

## SSH

- [ ] Usuario `deploy` con clave Ed25519 y `sudo` verificado
- [ ] Login root desactivado (`PermitRootLogin no`, root OVH intacto)
- [ ] Login por contraseña desactivado (`PasswordAuthentication no`)
- [ ] SSH funcionando como `deploy` por clave en segunda terminal
- [ ] Usuario `ubuntu` deshabilitado tras lo anterior (`passwd -l`, sin `authorized_keys`, `expiredate 1`)

## Firewall (UFW dentro + Edge OVH delante)

- [ ] UFW activo
- [ ] 22 abierto
- [ ] 80 abierto
- [ ] 443 abierto
- [ ] 3306 NO abierto
- [ ] 8080 NO abierto
- [ ] Edge OVH activo (0-2 autorizar 22/80/443, 10-11 denegar resto, `nmap` verificado tras activar)

## Docker

- [ ] Docker instalado
- [ ] Compose funcionando
- [ ] Contenedores en red privada
- [ ] Sin `privileged` innecesario
- [ ] Sin Docker socket innecesario
- [ ] Imágenes con versiones controladas

## TFM

- [ ] Perfil `prod`
- [ ] MariaDB
- [ ] Secrets mediante variables (nunca en Git ni en imágenes)
- [ ] `.env` de `/opt/apps/tfm/.env` respaldado en Bitwarden (nota `vps-tfm-env`, fuera del VPS)
- [ ] Spring Security
- [ ] CSRF correctamente configurado
- [ ] CORS restringido si se utiliza
- [ ] Actuator protegido
- [ ] Logs sin secretos

## Nginx

- [ ] Reverse proxy
- [ ] HTTPS
- [ ] HTTP redirige a HTTPS
- [ ] Solo Nginx expone 80/443

## Backups

- [ ] Backup MariaDB
- [ ] Backup externo
- [ ] Restauración probada
- [ ] Retención definida

## CI/CD

- [ ] Tests
- [ ] Build
- [ ] JaCoCo
- [ ] Sonar
- [ ] GitHub Actions
- [ ] SSH deploy key independiente
- [ ] GitHub Secrets
- [ ] Rollback documentado

---

# 61. Orden real de trabajo

No intentes hacerlo todo en un día.

La secuencia recomendada es:

```text
1. Contratar VPS
       ↓
2. SSH
       ↓
3. Usuario deploy
       ↓
4. Seguridad SSH
       ↓
5. Firewall
       ↓
6. Fail2ban (sshd; nginx-401 aparcada hasta FASE 15a)
        ↓
7. Docker
       ↓
8. Contenedor hello-world
       ↓
9. Dominio/DNS
       ↓
10. Docker Compose
       ↓
11. MariaDB
       ↓
12. TFM
       ↓
13. Nginx
        ↓
14. HTTPS + FASE 15a jail nginx-401 (tras Nginx up)
        ↓
15. Backups (TFM verde)
        ↓
16. GitHub Actions (TFM)
        ↓
17. Monitorización y mantenimiento
```

---

# 62. Regla durante el despliegue

Cuando algo falle:

**NO cambies cinco cosas a la vez.**

Haz:

```text
1. Leer el error.
2. Identificar qué componente falla.
3. Comprobar su estado.
4. Revisar sus logs.
5. Cambiar una cosa.
6. Volver a probar.
```

Comandos que utilizaremos constantemente:

```bash
docker ps
docker compose ps
docker compose logs
docker compose logs -f
docker stats
df -h
free -h
sudo ufw status
systemctl --failed
```

## Troubleshooting rápido (Nginx Docker + webroot)

| Síntoma                                                         | Causa probable                                                      | Comprobar                                                                                                                                                                                           |
| --------------------------------------------------------------- | ------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `502 Bad Gateway` en `https://tfm...`                           | `app:8080` no responde o red `front` externa mal                    | `cd /opt/apps/tfm && docker compose ps && docker compose logs -f app`, `cd /opt/apps/edge && docker compose exec nginx getent hosts app`, `docker network ls \| grep front`                         |
| `404` en `/.well-known/...`                                     | volumen `/var/www/certbot` no montado o bloque `:80` sin `location` | `cd /opt/apps/edge && docker compose exec nginx cat /etc/nginx/nginx.conf && docker compose exec nginx cat /etc/nginx/conf.d/tfm.conf`, `ls /var/www/certbot`, `curl http://tfm.../.well-known/...` |
| `nginx restarting` + `cannot load certificate ... No such file` | levantaste `tfm.conf` completo sin certs emitidos                   | paso A: aparta `tfm.conf`, usa `tfm-http-only.conf`, `up -d`; el completo solo tras Certbot (paso C)                                                                                                |
| Certbot `DNS problem / NXDOMAIN`                                | DNS A aún no propagado                                              | `dig +short $TFM_DOMAIN`, espera TTL, reintenta                                                                                                                                                |
| `nmap` muestra `8080/3306` abiertos                             | pusiste `ports:` en tfm/mariadb (bypass UFW)                        | cambia a `expose:`, `docker compose up -d`, repite `ss -tulpn`                                                                                                                                      |
| `permission denied docker.sock`                                 | sesión sin grupo `docker`                                           | `exit` + `ssh` de nuevo, `groups`, `docker ps`                                                                                                                                                      |
| `ubuntu: Account expired / Permission denied` tras FASE 6       | es lo esperado si ya deshabilitaste `ubuntu`                        | usa `ssh deploy@IP`; si perdiste `deploy`, rescata por consola KVM OVH con `usermod --expiredate "" ubuntu`                                                                                         |
| Perdí el `.env` y no hay copia en Bitwarden                     | sin copia no hay restore mágico                                     | regenerar tokens en proveedores + nuevas claves `openssl rand` + recrear volumen/usuarios (`ALTER USER` o `down -v` con backup previo). Con copia: restaurar fichero + `chmod 600` + `up`           |
| Disco lleno                                                     | logs/backups sin rotación                                           | `df -h`, `docker system df`, `logging max-size` en compose, retención backups                                                                                                                       |

Aprender estos comandos te dará una base mucho más útil que memorizar una instalación completa.

---

# 63. Estado final

Al terminar deberíamos tener:

```text
                          INTERNET
                              |
                          HTTPS 443
                              |
                              v
                          NGINX
                           |
                           v
                        TFM
                         |
                         v
                      MariaDB
                         |
                         v
                      BACKUPS
                         |
                         v
                 almacenamiento
                externo
```

Y MariaDB permanece privada:

```text
Internet
    X
    |
    X 3306
    |
    v
MariaDB
```

---

# 64. Qué aprenderás con este despliegue

Al terminar no solo tendrás el TFM online.

Habrás trabajado con:

- Linux
- SSH
- usuarios y permisos
- firewall
- Docker
- Docker Compose
- redes Docker
- volúmenes
- MariaDB
- Nginx
- reverse proxy
- DNS
- HTTPS/TLS
- Let's Encrypt
- gestión de secretos
- backups
- GitHub Actions
- CI/CD
- APIs
- seguridad básica de servidores

Esto convierte el despliegue en una parte importante de tu propio aprendizaje y también en una pieza interesante de tu portfolio.

---

# 65. Próximo paso

Cuando contrates el VPS, **no instales todavía todo lo anterior**.

Empieza solamente por:

```text
FASE 1
Primer acceso SSH

FASE 2
Actualizar Ubuntu

FASE 3
Crear deploy

FASE 4
Configurar SSH

FASE 5
Firewall
```

Después comprueba que puedes entrar correctamente como:

```bash
ssh deploy@IP_DEL_VPS
```

y que:

```bash
sudo ufw status
```

funciona.

A partir de ahí continuaremos con Docker.

> **Principio de esta guía:** primero entender, después ejecutar. El objetivo no es que tengas un montón de comandos copiados, sino que sepas qué hace cada componente y puedas solucionar un problema cuando aparezca.
