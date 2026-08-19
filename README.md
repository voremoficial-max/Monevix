# Monevix

Aplicación Android nativa para administrar y calcular pagos de personal
según trabajos realizados y cantidades producidas.

## Estado actual: FASE 2 — Base de datos y trabajadores

- Fase 1: proyecto base con Jetpack Compose + GitHub Actions. ✅
- Fase 2: base de datos Room + módulo de Personal (trabajadores). ✅

## Tecnología

- Kotlin 1.9.24
- Jetpack Compose (BOM 2024.06.00) + Material 3
- Room 2.6.1 (persistencia local)
- Navigation Compose 2.7.7
- Android Gradle Plugin 8.5.2 / Gradle 8.7
- Java 17
- compileSdk 34 / targetSdk 34 / minSdk 24
- GitHub Actions para compilación continua

## Arquitectura

```
UI (Compose) -> ViewModel -> Repository -> Room (DAO/Entity)
```

La lógica de validación vive en `domain/WorkerValidator.kt`, separada de
la interfaz, para poder probarla con JUnit sin depender de Android.

## Compilar localmente

Abrir la carpeta del proyecto en Android Studio (Koala o superior) y
ejecutar (Run ▶) sobre un emulador o dispositivo físico.

## Compilar vía GitHub Actions

Cada push a `main` ejecuta `.github/workflows/android-build.yml`, que
compila el proyecto, corre las pruebas unitarias y publica el APK Debug
como Artifact descargable desde la pestaña "Actions" de GitHub.


## Fase 5
Liquidaciones persistentes e historial con filtros por trabajador y fechas. Los precios unitarios usados quedan guardados como copia histórica.


## Firma del APK y nombre del archivo

El proyecto está preparado para generar un **APK Release firmado** y entregarlo como `Monevix.apk` en GitHub Actions. No se incluye ninguna clave privada dentro del repositorio.

En GitHub, ve a **Settings → Secrets and variables → Actions** y crea estos secretos:

- `MONEVIX_KEYSTORE_BASE64`: contenido Base64 de tu archivo `.keystore` o `.jks`.
- `MONEVIX_KEYSTORE_PASSWORD`: contraseña del keystore.
- `MONEVIX_KEY_ALIAS`: alias de la clave.
- `MONEVIX_KEY_PASSWORD`: contraseña de la clave.

La firma usa esos secretos solamente durante el workflow y el archivo final se copia como `Monevix.apk`.
