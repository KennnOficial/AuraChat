# AuraChat

Plugin de chat por KennnDev para Paper 1.21.4 a 1.21.11, ambas incluidas. Requiere Java 21.

## Instalacion

1. Compila con `gradlew.bat build` (Java 21).
2. Copia `build/libs/AuraChat-1.0-SNAPSHOT.jar` a `plugins/`.
3. Reinicia el servidor. Edita `plugins/AuraChat/config.yml` y ejecuta `/aurachat reload`.

LuckPerms y PlaceholderAPI son opcionales. El prefijo usa la API de LuckPerms directamente; los placeholders de otros plugins requieren las expansiones correspondientes de PlaceholderAPI. El formato y los mensajes configurables admiten colores con `&`.

## Comandos y permisos

| Comando | Permiso | Por defecto |
| --- | --- | --- |
| `/msg <jugador> <mensaje>` (`/tell`, `/w`) | `aurachat.msg` | Todos |
| `/reply <mensaje>` (`/r`) | `aurachat.msg` | Todos |
| `/ignore <jugador>` | `aurachat.ignore` | Todos |
| `/mute <jugador>` | `aurachat.moderate` | OP |
| `/unmute <jugador>` | `aurachat.moderate` | OP |
| `/chat lock` | `aurachat.admin` | OP |
| `/chat clear` | `aurachat.admin` | OP |
| `/aurachat help` | Todos | Todos |
| `/aurachat reload` | `aurachat.admin` | OP |

`aurachat.color` permite colores en los mensajes. `aurachat.bypass.antispam`, `aurachat.bypass.lock` y `aurachat.mute.exempt` permiten las excepciones correspondientes. Todos estos permisos son de OP por defecto. El silencio se aplica incluso a quien tiene excepcion de antispam.

Los silencios, el bloqueo y los ignorados se guardan por UUID en `players.yml`. Los comandos de moderacion e ignorar requieren que el objetivo este conectado; para dejar de ignorar se repite `/ignore`. Ignorar oculta el chat publico de ese jugador e impide mensajes privados en ambos sentidos. El bloqueo afecta al chat publico; los silencios y el antispam afectan tambien a los mensajes privados enviados con AuraChat. No intercepta comandos de mensajeria de otros plugins.

La espera entre mensajes y la deteccion de repetidos comparten estado entre chat publico y privado. Un mensaje rechazado no reinicia la espera. Las conversaciones para `/reply` se eliminan al desconectarse cualquiera de los participantes. Usa AsyncChatEvent y traslada el procesamiento al hilo principal mediante el scheduler de Paper antes de completar el evento. Asi las expansiones, permisos y datos permanecen en el hilo correcto, conservando los destinatarios y cancelaciones anteriores de otros plugins.

## Verificacion en servidor

Con dos jugadores sin OP:

- Enviar chat, comprobar formato y prefijo; repetir con y sin plugins opcionales.
- Enviar dos mensajes seguidos y repetir el mismo tras la espera; comprobar ambos rechazos.
- Enviar mas de diez letras con todas en mayusculas; comprobar el rechazo.
- Probar `/msg`, `/reply`, `/ignore`, y repetir `/ignore` para restaurar la conversacion.
- Silenciar a uno con la consola; comprobar que no puede enviar chat ni privados; quitar el silencio.
- Bloquear y desbloquear el chat; comprobar la excepcion de un OP.
- Reiniciar y comprobar que se conservan silencios, ignorados y bloqueo.
- Cambiar el formato, recargar y comprobar el nuevo mensaje.

Los alias pueden entrar en conflicto con otros plugins. Usa `/aurachat:msg` y los demas comandos con el prefijo `aurachat:` para seleccionar esta implementacion.

## Compatibilidad entre versiones

Rango objetivo: Paper 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10 y 1.21.11. Se distribuye un solo JAR compilado contra la API minima 1.21.4 con bytecode Java 21, sin NMS ni CraftBukkit.

Para verificar las fuentes contra las ocho APIs y generar el JAR:

```powershell
./gradlew.bat build checkSupportedVersions
```

`checkSupportedVersions` verifica cada API por separado. No cambia la API con la que se genera el JAR de distribucion. La tarea `checkCompatibility` permite comprobar individualmente otra version:

```powershell
./gradlew.bat checkCompatibility -PcompatibilityApiVersion=1.21.10-R0.1-SNAPSHOT --rerun-tasks
```

Estas tareas verifican compilacion, no sustituyen las pruebas de funcionamiento dentro de cada servidor descritas arriba. No se declara soporte fuera del rango 1.21.4 a 1.21.11 ni para Spigot, Bukkit puro o Folia. LuckPerms, PlaceholderAPI y sus expansiones deben ser compatibles con la version de servidor elegida.

## Integracion con AuraLobby

Al detectar AuraLobby, AuraChat suspende exclusivamente su listener de formato publico `com.kennndev.auralobby.chat.ChatListener` y aplica `chat.format` de AuraChat. No modifica archivos de AuraLobby ni desactiva sus otros listeners. AuraLobby es una dependencia opcional; AuraChat funciona sin el.

La deteccion ocurre al iniciar AuraChat, al habilitar AuraLobby y al ejecutar `/aurachat reload`. Al desactivar AuraChat se restaura el listener suspendido si AuraLobby sigue activo. Si AuraLobby se deshabilita, se descarta la referencia antigua para no restaurarla tras una recarga. El hook corresponde a la clase verificada en el proyecto actual de AuraLobby; una futura reorganizacion de ese plugin requeriria actualizar el hook.

Los mensajes privados y sus comandos de AuraLobby permanecen intactos. Usa `/aurachat:msg` para seleccionar explicitamente los mensajes privados de AuraChat si ambos plugins registran `/msg`.

Para verificar en servidor: instala ambos JAR, usa formatos publicos distintos y confirma que aparece solo el de AuraChat. Recarga ambos plugins y repite; comprueba tambien que las preferencias y demas funciones de lobby siguen funcionando. Las pruebas automatizadas verifican que solo se retire el listener objetivo, que no se duplique al recargar y que se restaure o descarte segun el estado de AuraLobby.

## Organizacion del codigo

El paquete base es `wtf.kennn.aurachat`. Las carpetas coinciden con los paquetes declarados:

```text
wtf/kennn/aurachat/
|-- AuraChat.java
|-- chat/
|   `-- ChatFormatter.java
|-- command/
|   `-- ChatCommands.java
|-- integration/
|   |-- AuraLobbyHook.java
|   `-- LobbyIntegration.java
|-- listener/
|   `-- ChatListener.java
|-- message/
|   `-- Messages.java
|-- messaging/
|   `-- PrivateMessageService.java
|-- moderation/
|   `-- ModerationService.java
`-- storage/
    `-- PlayerState.java
```

`AuraChat` inicializa los componentes y registra los comandos y eventos. Cada paquete agrupa una responsabilidad: formato, comandos, integraciones, eventos, mensajes configurables, conversaciones privadas, moderacion y persistencia. Las colecciones internas permanecen privadas. Las pruebas de integracion y persistencia estan en sus paquetes correspondientes bajo `src/test/java`. `plugin.yml` apunta a `wtf.kennn.aurachat.AuraChat`.

`/aurachat` y `/aurachat help` muestran ayuda en ingles, filtrada por permisos. El autocompletado incluye `help` y muestra `reload` a administradores.
