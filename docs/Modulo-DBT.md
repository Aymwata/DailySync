# Módulo DBT del PMV

Este módulo permite consultar un catálogo local de habilidades sintéticas, leer una habilidad, registrar una práctica y consultar, editar o eliminar esa práctica. Los registros aparecen en el historial unificado. Está construido con Compose, ViewModel, StateFlow, Repository y Room, siguiendo la organización del proyecto.

## Recorrido funcional

Inicio → Habilidades DBT → Catálogo → Habilidad → Registrar práctica → Detalle de práctica.

Desde el detalle se puede editar, eliminar con confirmación o ir al historial. Desde el historial, los registros DBT permiten abrir su detalle, editar y eliminar. El selector de nuevo registro de Inicio también utiliza el acceso DBT existente.

El catálogo ofrece filtros por los grupos presentes en sus datos y una opción Todas. Actualmente contiene Mente Sabia, STOP, DEAR MAN y Acción Opuesta. Se conservan las cuatro habilidades del proyecto. Sus explicaciones se reformularon con una definición sencilla, pasos y un ejemplo ficticio por habilidad.

## Archivos nuevos y su responsabilidad

Las rutas de producción parten de `app/src/main/java/com/kiberkot/dailysync/`.

| Archivo | Función |
|---|---|
| `model/RegistroDbt.kt` | Define `RegistroDbt`, la práctica que se muestra en el detalle, y `FormularioDbt`, sus campos editables y reglas de validación. |
| `viewmodel/RegistroDbtViewModel.kt` | Contiene `RegistroDbtUiState` y el ViewModel. El estado agrupa catálogo, práctica, formulario, carga y errores. El ViewModel carga por ID y usuario, mantiene los campos, valida, guarda y elimina. Se utiliza una instancia por entrada de formulario o detalle. |
| `ui/dbt/CatalogoDbtScreen.kt` | Dibuja las habilidades y filtros. Comunica el ID de la habilidad pulsada. |
| `ui/dbt/DetalleHabilidadDbtScreen.kt` | Muestra nombre, grupo y contenido de una habilidad del catálogo, y permite comenzar una práctica. |
| `ui/dbt/RegistroDbtScreen.kt` | Dibuja el formulario para crear y editar, con mensajes de validación y escalas de intensidad y utilidad. |
| `ui/dbt/DetalleRegistroDbtScreen.kt` | Muestra la práctica completa y ofrece editar, eliminar con confirmación y consultar el historial. |

## Archivos existentes modificados en esta entrega

| Archivo | Cambio |
|---|---|
| `data/local/dao/DailySyncDao.kt` | Actualiza el catálogo con Upsert, conserva la inserción de usuario con IGNORE y añade alta de prácticas con ABORT, consulta observable de una práctica por ID y usuario, búsqueda de habilidad y actualización restringida al usuario. Reutiliza la eliminación por usuario que ya existía. |
| `data/local/AppDatabase.kt` | Contiene la lista `catalogoDbtInicial` dentro del companion object, junto a la configuración de Room. La carga inicial y el Repository utilizan esa lista y actualizan sus textos con Upsert. No cambia la versión ni la estructura de la base. |
| `data/repository/DbtRepository.kt` | Amplía la lectura del catálogo con preparación de datos, lectura de práctica, creación, edición y eliminación. Convierte entidades Room en modelos de la aplicación. |
| `viewmodel/DbtViewModel.kt` | Contiene `DbtUiState` y el ViewModel del catálogo. Asegura que el catálogo exista antes de observarlo y mantiene la carga y los filtros. |
| `navigation/AppNavigation.kt` | Contiene todas las rutas de la aplicación, incluidas las cinco rutas DBT en `Destinos` y sus bloques composable dentro del NavHost. Conecta Inicio e historial con el módulo. |
| `ui/historial/HistorialScreen.kt` | Habilita Editar también para el ámbito DBT. La lista común, filtros y eliminación siguen usando tu implementación. |

`HabilidadDbt.kt`, el historial común y las pantallas de Ánimo se aprovechan para integrar el módulo. No se modificó la lógica de login, Neurodesarrollo, Adicciones ni el diseño de Inicio para implementar DBT.

## Organización simplificada de estados

Cada clase `UiState` se declara encima de su ViewModel en el mismo archivo, dentro del paquete `viewmodel`. Sigue siendo una clase independiente; no está anidada dentro del ViewModel. La separación MVVM se conserva porque los datos, las acciones y la presentación mantienen sus responsabilidades.

| Estado | Archivo |
|---|---|
| `HomeUiState` | `viewmodel/HomeViewModel.kt`, junto con `ActividadDiaUi`, `ModuloHome` y `SeccionHome`. |
| `HistorialUiState` | `viewmodel/HistorialViewModel.kt`. |
| `DbtUiState` | `viewmodel/DbtViewModel.kt`. |
| `RegistroDbtUiState` | `viewmodel/RegistroDbtViewModel.kt`. |
| `AnimoUiState` | `viewmodel/AnimoViewModel.kt`. |
| `LoginUiState` | `viewmodel/LoginViewModel.kt`. |
| `RegisterUiState` | `viewmodel/RegisterViewModel.kt`. |

Las vistas importan estos tipos desde `com.kiberkot.dailysync.viewmodel`. Se retiraron los cuatro archivos separados de estados que había en `ui`; los tres estados de Ánimo, login y registro ya seguían esta convención.

## Navegación, catálogo y componentes reunidos

DBT siempre utiliza la misma base SQLite `dailysync_offline_db`. El archivo separado del catálogo solo almacenaba una lista Kotlin; no era otra base de datos. Esa lista ahora se declara directamente en `AppDatabase.catalogoDbtInicial`.

Se retiraron `DbtCatalogoInicial.kt`, `DbtNavigation.kt` y `DbtComponents.kt`. Las rutas se declaran en `AppNavigation.kt`. Los componentes visuales se encuentran en `ui/components/Componentes.kt` y las pantallas DBT los importan desde ese paquete:

- `DbtScaffold`: cabecera con regreso y lista desplazable que organiza cada pantalla DBT.
- `DbtSeccion`: tarjeta con título y contenido para reutilizar el mismo aspecto.
- `DbtError`: mensaje de error y botón para volver a intentarlo.

Estas separaciones de funciones siguen siendo útiles, pero no requieren archivos independientes.

## Referencias del contenido educativo

Las explicaciones son adaptaciones breves en español, con ejemplos ficticios propios. No se copian manuales completos.

- Mente Sabia: [VA MIRECC, DBT Visual Review Skills Flash Cards](https://www.mirecc.va.gov/mirecc/visn16/docs/DBT_Visual_Review_Flash_Cards.pdf).
- STOP: [DBT Self Help, STOP](https://dbtselfhelp.com/stop/).
- DEAR MAN: [DBT Self Help, Objectives Effectiveness](https://dbtselfhelp.com/objectives-effectiveness-dear-man/).
- Acción Opuesta: [Mindful Teen, Opposite Action](https://www.mindfulteen.org/dbt/emotion-regulation/opposite-action/).

## Cómo se aplica MVVM

La vista recibe datos y callbacks. Por ejemplo, `RegistroDbtScreen` recibe el formulario y llama a `onCambioSituacion`; no ejecuta consultas SQL.

`RegistroDbtViewModel` mantiene un `MutableStateFlow` privado y expone un `StateFlow` de lectura. Al escribir, crea una copia del formulario y publica un nuevo estado. Compose observa ese estado mediante `collectAsState`.

El Repository concentra el acceso a datos. Al guardar, verifica los datos y la habilidad, genera el UUID y la fecha si es un alta, y elige entre inserción y actualización.

El DAO contiene las instrucciones que Room ejecuta sobre SQLite. Las pantallas no conocen las tablas. La consulta de una práctica y sus cambios se observan con Flow.

La navegación determina qué ID se abre y qué sucede al terminar una operación. Para editar se utiliza el mismo formulario con los datos existentes. La habilidad se puede cambiar durante la edición.

## Validación y persistencia

- Habilidad obligatoria y existente en el catálogo.
- Situación obligatoria de 3 a 250 caracteres, después de quitar espacios exteriores.
- Intensidad y utilidad percibida de 1 a 5.
- UUID automático y fecha en formato `yyyy-MM-dd HH:mm:ss.SSS` al crear.
- Al editar se conservan ID, usuario y fecha de creación. La actualización SQL no modifica esos campos.
- Altas y cambios quedan con `pendiente_sync = true`.
- El botón de guardado se bloquea durante la operación para evitar duplicados por pulsaciones repetidas. La navegación de regreso también se bloquea mientras se escribe.
- Una eliminación requiere confirmación y se restringe a ID más usuario.
- Si una operación falla se muestra un error y se permite volver a intentarla.
- Los formularios se conservan durante una rotación gracias al ViewModel. No se implementó recuperación de borradores después de que Android termine el proceso.

No hay cambios de esquema ni migraciones nuevas. Los datos se guardan en las tablas existentes. El catálogo se actualiza con `@Upsert`: inserta habilidades nuevas y actualiza las existentes sin borrarlas. Así los textos nuevos aparecen también en instalaciones anteriores y se conservan las prácticas asociadas por clave foránea.

## Usuario de prueba y trabajo pendiente transversal

DBT usa `DbtRepository.USUARIO_DEMO`, cuyo valor es `usr_demo_01`. Coincide con el usuario que utilizan actualmente Ánimo y el historial. Este punto está explícito; todavía no se vincula el autor del registro al login real.

Cuando se termine la sesión, se debe cambiar `usuarioDbtId` dentro de `AppNavigation` por el ID activo. Hay que actualizar también el usuario que consulta el historial y el que utiliza Ánimo para mantener la coherencia.

La aplicación funciona localmente. No se implementó una API ni sincronización; pendiente de sincronización describe un estado almacenado, no una conexión activa. No se añadieron duración ni nota porque no forman parte de la entidad DBT actual.

El gráfico semanal de Inicio sigue siendo un trabajo transversal pendiente; este módulo no cambia su fuente de datos.

## Pruebas añadidas

| Archivo | Cobertura |
|---|---|
| `app/src/test/java/com/kiberkot/dailysync/FormularioDbtTest.kt` | Cinco pruebas locales: formulario válido, habilidad obligatoria, longitud de situación, límites válidos y rangos de escalas. |
| `app/src/androidTest/java/com/kiberkot/dailysync/DbtRepositoryTest.kt` | Cuatro pruebas con Room: CRUD e historial conservando ID y fecha, aislamiento entre usuarios, persistencia al reabrir SQLite y actualización de textos antiguos sin borrar prácticas. |
| `app/src/androidTest/java/com/kiberkot/dailysync/DbtFlujoTest.kt` | Recorrido Compose de login, catálogo, validación, alta, edición, historial y eliminación. Se limita a API 35 o inferior porque Espresso 3.6.1 falla antes de ejecutar la interfaz en el emulador Android 17 disponible. |

Las pruebas de Repository utilizan bases de datos de prueba. La prueba Compose identifica y limpia únicamente el registro ficticio que crea. `testTag` en el contenido DBT y en el campo de situación permite localizar controles en las pruebas sin afectar a su aspecto.

Comandos de verificación:

```powershell
./gradlew.bat :app:assembleDebug :app:testDebugUnitTest
./gradlew.bat :app:connectedDebugAndroidTest --no-configuration-cache
```

El APK está en `app/build/outputs/apk/debug/app-debug.apk`. Los informes están en `app/build/reports/tests/testDebugUnitTest/` y `app/build/reports/androidTests/connected/debug/`.

### Resultado de esta entrega

La compilación del APK y las cinco pruebas nuevas de validación pasaron. Las tres pruebas nuevas de Repository pasaron en el emulador Android 17/API 37; también pasó la prueba de contexto Android existente. La prueba Compose se reserva para API 35 y no se cuenta como ejecutada en ese emulador.

Se comprobó además el recorrido de pantallas directamente en el emulador con las herramientas de Android: login de prueba, acceso DBT, catálogo, selección de STOP, rechazo de situación vacía, alta, detalle, edición, aparición en historial, cierre completo y reapertura de la app conservando la práctica, cancelación del borrado y eliminación confirmada. Se eliminó la práctica ficticia utilizada para esa comprobación.

La verificación instrumentada instala y desinstala la aplicación de prueba en el emulador. Al terminar se reinstaló el APK del proyecto para dejarlo disponible. Para conservar datos importantes de una demostración, ejecutar las pruebas instrumentadas en un emulador dedicado.

### Verificación de la simplificación

Se compilaron el APK, las pruebas locales y el APK de pruebas Android después de reunir catálogo, navegación y componentes. Las pruebas locales pasaron. La nueva prueba `actualizaTextosDelCatalogoSinBorrarPracticasExistentes` quedó compilada; su ejecución está pendiente porque no había un emulador conectado en esta revisión. Los resultados de las tres pruebas de Room anteriores corresponden a la entrega funcional inicial.

## Guía para explicarlo en la defensa

1. Abre el catálogo y explica cómo `DbtViewModel` obtiene un Flow del Repository y publica `DbtUiState`.
2. Cambia un filtro: no cambia la base, solo la lista visible derivada del estado.
3. Abre STOP: la ruta definida en `AppNavigation` transporta su ID entero y se recupera su información del catálogo.
4. Intenta guardar con la situación vacía: `FormularioDbt.errores` devuelve los errores y la vista los muestra.
5. Guarda una situación ficticia: el Repository genera UUID y fecha y el DAO inserta una fila de `REGISTRO_DBT`.
6. Abre el historial: `HistorialRepository` ya convierte esa entidad al formato común, por lo que aparece sin añadir manualmente una tarjeta.
7. Edita: el DAO actualiza los campos conservando identidad y fecha original.
8. Cancela y luego confirma una eliminación: el diálogo controla la decisión; Repository y DAO realizan el borrado.

Para practicar una modificación sencilla, cambia el máximo de la situación en `FormularioDbt.errores` y el contador visible de `RegistroDbtScreen`; después ajusta y ejecuta sus pruebas de límites.
