# Golpea al Topo — edición con anuncios (Happy Mole Tap)

Juego arcade de golpear topos para Android, con gráficos coloridos, sonidos alegres, modos de dificultad y logros. **Esta variante integra publicidad**, a diferencia del proyecto base `Happy-Mole-Tap`.

## Características

- Tablero de 6 agujeros (2 filas de 3) con topos que aparecen y se esconden.
- Partidas de 30 segundos con puntuación, racha de aciertos (combo) y bonus por combo.
- Varios tipos de topo: normal, travieso, constructor con casco y Rey Topo dorado.
- Modos de dificultad que ajustan la frecuencia y el tiempo de aparición de los topos.
- Tickets de feria que se canjean por premios (peluche, martillo, corona, globo, trofeo).
- 7 logros desbloqueables con recompensa de tickets.
- Récord guardado por dificultad, y ajustes de efectos de sonido y música.
- Pausa, pantalla de fin de partida con confeti y valoración con estrellas.

## Diferencias de esta edición con anuncios

- Integración de **Google Mobile Ads (AdMob)** con anuncios de banner e intersticiales en el código.
- Pantalla de información de edad antes de inicializar publicidad, con un tratamiento restringido cuando procede.
- Gestión de consentimiento a través de **Google User Messaging Platform (UMP)** y opciones de privacidad según los requisitos aplicables.
- En compilaciones **debug**, `AdManager.kt` utiliza identificadores publicitarios de prueba; la configuración de publicación usa identificadores propios.
- La integración publicitaria **debe validarse** antes de distribución, especialmente por tratarse de un juego orientado a menores y por los requisitos legales y de Google Play. Tener estas clases no certifica cumplimiento.

Archivos relevantes: `app/src/main/java/com/example/ads/AdManager.kt`, `AgeGate.kt`, `ConsentManager.kt` y `app/src/main/java/com/example/ui/components/BannerAdView.kt`.

## Tecnologías

- Kotlin
- Jetpack Compose y Material 3
- `ViewModel` + `StateFlow` para el estado del juego
- Gradle con Kotlin DSL

## Estructura del proyecto

```text
app/src/main/java/com/example/
├── MainActivity.kt   # Punto de entrada y navegación por estado
├── audio/            # Efectos de sonido y música
├── data/             # Preferencias y progreso guardado
├── model/            # Topos, dificultad, logros y premios
├── ui/               # Pantallas, componentes y tema
└── viewmodel/        # GameViewModel y estado de la UI
```

## Compilar y ejecutar

Requisitos: Android Studio y un dispositivo o emulador Android.

```bash
git clone https://github.com/Stevengc360/Happy-Mole-Tap-con-anuncion.git
cd Happy-Mole-Tap-con-anuncion
cp .env.example .env   # si necesitas configurar variables de entorno
./gradlew assembleDebug
```

El APK de depuración se genera en `app/build/outputs/apk/debug/app-debug.apk`.

## Cómo se juega

1. Elige la dificultad en el menú principal.
2. Pulsa jugar y toca los topos antes de que se escondan.
3. Encadena aciertos para subir el combo; fallar o dejar escapar un topo lo reinicia.
4. Al terminar, gana tickets y canjéalos por premios.

## Estado y publicación

La aplicación incluye código para mostrar publicidad, pero se debe comprobar el comportamiento real de los anuncios, el consentimiento, las declaraciones de datos, las categorías de edad y las políticas de publicidad para familias antes de subir una versión a Google Play. Los anuncios reales no deben usarse durante pruebas de clics. Esta documentación no confirma que la aplicación esté publicada ni aprobada.
