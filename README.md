# Tik2WA

Aplicación Android Kotlin + Jetpack Compose/Material 3 para explorar visualmente una futura integración de stickers. Proyecto independiente, minSdk 26, targetSdk 36, MVVM-ready y navegación inferior animada.

## Estado importante de las integraciones

Este build **no inicia sesión ni lee Favoritos de TikTok y no importa stickers en WhatsApp**. No hay credenciales, cookies, scraping ni datos de ejemplo presentados como reales. TikTok no publica un endpoint oficial para que una app de terceros liste los stickers guardados en Favoritos. WhatsApp sí define una integración de paquetes de stickers para Android, pero el flujo requiere un pack entregado por la app y la aprobación/acción del usuario dentro de WhatsApp; no equivale a vincular la cuenta ni permite una importación silenciosa arbitraria. Los adaptadores actuales responden explícitamente `Unavailable` en vez de fingir éxito.

Para completar el producto hace falta conseguir un método autorizado de origen para esos stickers (API/colaboración de TikTok o una fuente que el usuario pueda autorizar) y construir la publicación del pack que cumpla el contrato vigente de WhatsApp, incluyendo sus metadatos, proveedor de contenido y consentimiento del usuario. La interfaz muestra claramente ese bloqueo y la pantalla de transferencia está rotulada como vista previa, no ejecuta una sincronización ficticia.

## Implementado en este prototipo

- UI oscura premium hecha en Compose: inicio/login visual, Settings, Perfil, colección vacía con estado explicativo y pantalla de sincronización.
- Navegación inferior, transiciones de contenido, microanimaciones, indicador animado, feedback háptico y controles de preferencias.
- Interfaces `TikTokStickerProvider`, `WhatsAppStickerProvider`, `AuthProvider`, `StickerRepository` y `SyncRepository`; implementaciones deshabilitadas que devuelven una razón legible.
- Conversor estático de bytes de imagen a WebP cuadrado de 512 px, con optimización al límite de 100 KiB y procesamiento en memoria. Animación de origen se rechaza expresamente: la conversión animada no está implementada.
- Deduplicación por hash (SHA-256) y persistencia local de hashes ya sincronizados.
- `SecureStorage` basado en AndroidX Security/Keystore para credenciales futuras. Esta versión no recoge tokens.
- Prueba unitaria para deduplicación y hash.

## Compilar y publicar

El workflow `.github/workflows/android-release.yml` instala JDK 17 y Android SDK 36, ejecuta las pruebas unitarias y genera `app-debug.apk`. En cada push a `main` y pull request construye el APK y lo publica como artefacto de Actions. Para publicar en Releases, ejecuta el workflow manualmente indicando un tag como `v0.1.0`, o empuja un tag `v*`.

El APK de Release está firmado con la clave debug efímera de CI: sirve para pruebas, no para distribución de producción ni para actualizaciones firmadas entre builds. Para producción se debe configurar firma persistente mediante GitHub Secrets y un keystore del propietario.

También puedes compilar localmente con JDK 17 y Android SDK 36:

```sh
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

El entorno de generación no tiene JDK/Android SDK, por lo que la compilación debe verificarse en GitHub Actions antes de considerar el artefacto listo.

## Módulos/paquetes

- `domain/model`, `domain/provider`: entidades y contratos independientes de proveedores.
- `data/integration`, `data/stickers`, `data/sync`: adaptadores, conversión, hash y sincronización.
- `core/security`: almacenamiento cifrado de secretos futuros.
- `MainActivity.kt`: composición visual y navegación del prototipo.
