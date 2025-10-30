# 📱 GeoNote - App de Notas con Geolocalizacion

# Integrantes
- Ignacio Escalona
- Máximo garcía
  
---

## 📖 Descripción del Proyecto

**GeoNote** es una aplicación de notas innovadora que combina la escritura tradicional con la geolocalización, creando un diario personal enriquecido con contexto espacial. Cada nota captura no solo pensamientos y momentos, sino también el lugar exacto donde ocurrieron, permitiendo a los usuarios crear un mapa personal de sus experiencias, recuerdos y emociones.

### Visión del Proyecto

GeoNote busca ser un **diario geolocalizado** que combina memoria, lugar y emoción. La idea es capturar pensamientos o momentos en el lugar donde ocurrieron, creando un mapa personal de experiencias. Esta fusión entre lo textual y lo geográfico permite:

- **Revivir momentos** al ver dónde ocurrieron
- **Descubrir patrones** de lugares significativos
- **Enriquecer recuerdos** con contexto espacial
- **Crear narrativas visuales** de la vida diaria

---

## 🎯 Características Principales

### ✅ Funcionalidades Core

1. **Gestión de Notas**
   - Crear notas con título, contenido y etiquetas
   - Editar notas existentes
   - Archivar notas (soft delete)
   - Visualización en lista con diseño moderno

2. **Geolocalización**
   - Captura automática de ubicación GPS
   - Visualización de coordenadas y precisión
   - Marcadores en mapa interactivo
   - Información de ubicación en cada nota

3. **Multimedia**
   - Captura de fotos con la cámara
   - Almacenamiento seguro de imágenes
   - Visualización de fotos en notas

4. **Navegación Intuitiva**
   - Barra de navegación inferior (Bottom Navigation)
   - Dos secciones principales: Notas y Mapa
   - Transiciones fluidas entre pantallas
   - Arquitectura de navegación por tabs

5. **Diseño Material 3**
   - Tema personalizado con paleta de colores moderna
   - Soporte completo para modo claro y oscuro
   - Interfaz "soft" y minimalista
   - Componentes Material You

---

## 🛠️ Tecnologías y Herramientas Utilizadas

### Stack Tecnológico Principal

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **Kotlin** | 2.0.21 | Lenguaje de programación principal |
| **Jetpack Compose** | BOM 2024.09.00 | Framework UI moderno y declarativo |
| **Material 3** | Latest | Sistema de diseño y componentes UI |
| **Room Database** | 2.6.1 | Persistencia local con SQLite |
| **Coroutines** | 1.9.0 | Programación asíncrona |
| **Flow** | - | Flujos de datos reactivos |
| **Navigation Compose** | 2.8.3 | Navegación entre pantallas |
| **Google Maps Compose** | 4.3.3 | Integración de mapas |
| **Google Location Services** | 21.3.0 | Servicios de geolocalización |
| **Coil** | 2.5.0 | Carga y gestión de imágenes |

### Herramientas de Desarrollo

- **Android Studio** - IDE oficial de Android
- **Gradle** (8.13) - Sistema de build
- **KAPT** - Procesamiento de anotaciones para Room
- **Git** - Control de versiones

### Arquitectura y Patrones

- **MVVM** (Model-View-ViewModel) - Patrón arquitectónico
- **Repository Pattern** - Capa de abstracción de datos
- **Clean Architecture** - Separación de responsabilidades
- **Single Activity** - Arquitectura moderna de Android

---

## 📁 Estructura de Paquetes

```
com.example.geonote/
│
├── 📦 model/                      # Capa de Datos
│   ├── AppDatabase.kt            # Configuración de Room Database
│   ├── NoteEntity.kt             # Entidad de nota (tabla)
│   ├── NoteDao.kt                # Data Access Object (consultas)
│   └── LocationProvider.kt       # Proveedor de ubicación GPS
│
├── 📦 repository/                 # Capa de Repositorio
│   └── NoteRepository.kt         # Lógica de acceso a datos
│
├── 📦 viewmodel/                  # Capa de Presentación
│   ├── NoteViewModel.kt          # ViewModel principal
│   └── NoteVMFactory.kt          # Factory para ViewModel
│
├── 📦 ui/                         # Interfaz de Usuario
│   ├── NoteListScreen.kt         # Pantalla de lista de notas
│   ├── NoteCreateScreen.kt       # Pantalla de creación
│   ├── NoteDetailScreen.kt       # Pantalla de detalle/edición
│   ├── MapScreen.kt              # Pantalla de mapa
│   │
│   └── 📦 theme/                  # Sistema de Temas
│       ├── Color.kt              # Paleta de colores
│       ├── Theme.kt              # Configuración de tema
│       └── Type.kt               # Tipografía
│
└── MainActivity.kt                # Actividad principal y navegación
```

### Descripción de Componentes

#### 📊 Model (Capa de Datos)

**NoteEntity.kt**
- Define la estructura de una nota en la base de datos
- Campos: id, title, body, lat, lon, accuracy, createdAt, updatedAt, tags, archived, imageUri

**NoteDao.kt**
- Interface que define operaciones de base de datos
- Métodos: getActiveNotes(), getById(), upsert(), archive(), delete()

**AppDatabase.kt**
- Configuración de Room Database
- Singleton pattern para instancia única
- Versión 2 con estrategia de migración destructiva

**LocationProvider.kt**
- Wrapper para Google Location Services
- Obtiene ubicación actual con alta precisión
- Manejo de permisos y errores

#### 🗄️ Repository (Capa de Negocio)

**NoteRepository.kt**
- Abstrae el acceso a datos
- Expone Flow<List<NoteEntity>> para observabilidad reactiva
- Métodos: getNotes(), get(), save(), archive(), delete()

#### 🎭 ViewModel (Lógica de Presentación)

**NoteViewModel.kt**
- Gestiona estado de la UI
- StateFlow para notas, errores de validación y nota actual
- SharedFlow para eventos de guardado exitoso
- Funciones: saveNote(), loadNote(), archive(), clearError()

**NoteVMFactory.kt**
- Factory pattern para crear ViewModel con dependencias
- Inyecta Repository en el ViewModel

#### 🎨 UI (Interfaz de Usuario)

**NoteListScreen.kt**
- Lista de notas con LazyColumn
- Diseño de tarjetas con imagen, título, contenido, ubicación y fecha
- Menu contextual para archivar
- Estado vacío personalizado

**NoteCreateScreen.kt**
- Formulario de creación de nota
- Campos: título, contenido, etiquetas
- Botones para capturar ubicación y foto
- Validación en tiempo real
- Feedback visual de estados

**NoteDetailScreen.kt**
- Vista de detalle con modo lectura/edición
- Toggle entre visualización y edición
- Actualización de ubicación e imagen
- Confirmación de cambios

**MapScreen.kt**
- Integración de Google Maps
- Marcadores para cada nota con ubicación
- InfoWindow con título y preview
- Contador de notas en el mapa

**MainActivity.kt**
- Configuración de Navigation Graph
- Bottom Navigation Bar con dos tabs
- Gestión de navegación entre pantallas
- Integración del tema personalizado

---

## 🧭 Navegación

### Arquitectura de Navegación

GeoNote utiliza **Navigation Compose** con una estructura de **Bottom Navigation** que contiene dos tabs principales, cada uno con su propia sub-navegación.

```
Bottom Navigation Bar
├── 📝 Tab: NOTAS (notes_tab)
│   ├── notes_list           [Pantalla inicial]
│   ├── note_create          [Modal desde FAB]
│   └── note_detail/{noteId} [Al tocar una nota]
│
└── 🗺️ Tab: MAPA (map_tab)
    └── Mapa interactivo      [Standalone]
```

### Flujos de Navegación

#### Flujo Principal: Gestión de Notas

1. **Lista de Notas** (`notes_list`)
   - Pantalla inicial al abrir la app
   - Muestra todas las notas activas
   - FAB (+) para crear nueva nota
   - Tap en nota → navega a detalle

2. **Crear Nota** (`note_create`)
   - Se abre desde el FAB
   - Transición: slide up
   - Botón "Guardar" → vuelve a lista
   - Botón "Atrás" → cancela y vuelve

3. **Detalle/Edición** (`note_detail/{noteId}`)
   - Se abre al tocar una nota
   - Transición: slide left
   - Modo lectura por defecto
   - Botón "Editar" → habilita edición
   - Botón "Guardar" → guarda y vuelve
   - Botón "Cancelar" → descarta cambios

#### Flujo Secundario: Mapa

1. **Mapa** (`map_tab`)
   - Acceso directo desde bottom nav
   - Muestra todas las notas con ubicación
   - Marcadores clickeables con InfoWindow
   - No tiene sub-navegación

### Transiciones entre Pantallas

- **Lista ↔ Crear**: Slide vertical (arriba/abajo)
- **Lista ↔ Detalle**: Slide horizontal (izquierda/derecha)
- **Notas ↔ Mapa**: Fade in/out
- **Bottom Nav**: Mantiene estado de cada tab

---

## 🎨 Diseño y Tematización

### Paleta de Colores

#### Modo Claro (Light Mode)

| Color | Hex Code | Uso |
|-------|----------|-----|
| **Lavanda Tech** | `#6C63FF` | Color primario, acciones principales |
| **Verde Menta** | `#00BFA6` | Color secundario, ubicación |
| **Amarillo Acento** | `#FFB703` | Elementos de énfasis |
| **Coral Error** | `#FF6B6B` | Mensajes de error |
| **Fondo** | `#FAFAFF` | Background de la app |
| **Superficie** | `#FFFFFF` | Cards y componentes |
| **Texto Principal** | `#2D2D2D` | Contenido principal |
| **Texto Secundario** | `#6F6F6F` | Metadata y subtítulos |

#### Modo Oscuro (Dark Mode)

| Color | Hex Code | Uso |
|-------|----------|-----|
| **Lavanda Suave** | `#928CFF` | Color primario adaptado |
| **Menta Suave** | `#22D9C0` | Color secundario adaptado |
| **Amarillo Suave** | `#FFCF5C` | Acento adaptado |
| **Coral Error** | `#FF6B6B` | Mantiene consistencia |
| **Fondo** | `#121212` | Background oscuro |
| **Superficie** | `#1E1E1E` | Cards elevados |
| **Texto Principal** | `#EDEDED` | Texto claro |
| **Texto Secundario** | `#A4A4A4` | Metadata tenue |

### Tipografía

- **Familia de fuentes**: System Default (San Francisco / Roboto)
- **Estilos aplicados**:
  - Display Large: 57sp, Bold (Títulos principales)
  - Headline Medium: 28sp, SemiBold (Encabezados)
  - Title Large: 22sp, SemiBold (Títulos de tarjetas)
  - Body Large: 16sp, Normal (Contenido)
  - Label Small: 11sp, Medium (Metadata)

### Componentes Material 3

- **Cards**: Elevation 2dp, esquinas redondeadas
- **TextFields**: Outlined style con estados de error
- **Buttons**: Filled y Outlined variants
- **NavigationBar**: Tonalidad elevada con indicadores
- **FAB**: Icono + en color primario
- **Chips**: Para etiquetas (AssistChip)

---

## 🗄️ Base de Datos

### Esquema de Tabla `notes`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | Long (PK) | Identificador único (auto-incrementable) |
| `title` | String | Título de la nota (no nulo) |
| `body` | String | Contenido de la nota (no nulo) |
| `lat` | Double? | Latitud GPS (nullable) |
| `lon` | Double? | Longitud GPS (nullable) |
| `accuracy` | Float? | Precisión en metros (nullable) |
| `createdAt` | Long | Timestamp de creación |
| `updatedAt` | Long | Timestamp de última actualización |
| `tags` | String? | Etiquetas separadas por comas (nullable) |
| `archived` | Boolean | Estado de archivado (false por defecto) |
| `imageUri` | String? | URI de la imagen adjunta (nullable) |

### Consultas Principales

```kotlin
// Obtener notas activas ordenadas por fecha
@Query("SELECT * FROM notes WHERE archived = 0 ORDER BY updatedAt DESC")
fun getActiveNotes(): Flow<List<NoteEntity>>

// Buscar por ID
@Query("SELECT * FROM notes WHERE id = :id")
suspend fun getById(id: Long): NoteEntity?

// Guardar o actualizar
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun upsert(note: NoteEntity): Long

// Archivar (soft delete)
@Query("UPDATE notes SET archived = 1 WHERE id = :id")
suspend fun archive(id: Long)
```

---

## 🔒 Permisos

### Permisos Declarados

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.CAMERA" />
```

### Features Opcionales

```xml
<uses-feature android:name="android.hardware.location.gps" android:required="false"/>
<uses-feature android:name="android.hardware.camera" android:required="false"/>
```

### Manejo Runtime

- **Ubicación**: Solicita ACCESS_FINE_LOCATION al tocar "Obtener ubicación"
- **Cámara**: Solicita CAMERA al tocar "Foto"
- **Feedback**: Mensajes claros si se deniegan permisos

---

## 🔑 Integración de Google Maps

### Configuración de API Key

```gradle
manifestPlaceholders["MAPS_API_KEY"] = "AIzaSyAOINRRbpFd4XmnJrc1rIK1VQ330RjscpE"
```

### AndroidManifest.xml

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${MAPS_API_KEY}" />
```

### Características del Mapa

- **Tipo**: Mapa estándar de Google
- **Controles**: Zoom, brújula
- **Marcadores**: Por cada nota con lat/lon
- **InfoWindow**: Título + snippet (60 chars del body)
- **Cámara inicial**: Santiago, Chile (-33.4489, -70.6693)

---

## 📸 Gestión de Imágenes

### FileProvider

Configurado para compartir archivos de forma segura:

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

### Almacenamiento

- **Carpeta**: `{app_internal_storage}/images/`
- **Formato**: `IMG_{timestamp}.jpg`
- **Acceso**: URI generado por FileProvider
- **Persistencia**: Almacenado como String en Room

---

## 🚀 Instalación y Configuración

### Requisitos Previos

- Android Studio Ladybug (2024.2.1) o superior
- JDK 11 o superior
- SDK de Android API 24+ (Android 7.0)
- Dispositivo o emulador con Google Play Services

### Pasos de Instalación

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/mxgrc/GeoNote.git
   cd GeoNote
   ```

2. **Abrir en Android Studio**
   - File → Open → Seleccionar carpeta del proyecto

3. **Sync Gradle**
   - Android Studio sincronizará automáticamente
   - Esperar descarga de dependencias

4. **Configurar API Key de Google Maps** (Opcional: ya configurada)
   - Reemplazar en `app/build.gradle.kts`:
   ```kotlin
   manifestPlaceholders["MAPS_API_KEY"] = "TU_API_KEY"
   ```

5. **Ejecutar la App**
   - Seleccionar dispositivo/emulador
   - Run → Run 'app' o presionar Shift+F10

---

## 🧪 Testing

### Tests Incluidos

- `ExampleUnitTest.kt`: Test unitario básico
- `ExampleInstrumentedTest.kt`: Test de instrumentación

### Áreas de Testing Recomendadas

- **ViewModel**: Lógica de validación y guardado
- **Repository**: Operaciones CRUD
- **UI**: Navegación y estados de pantallas
- **Database**: Migraciones y consultas

---

## 📱 Requisitos del Sistema

### Requisitos Mínimos

- **Android**: 7.0 Nougat (API 24)
- **RAM**: 2 GB
- **Almacenamiento**: 50 MB libres
- **GPS**: Recomendado (opcional)
- **Cámara**: Recomendada (opcional)

### Requisitos Recomendados

- **Android**: 12.0 o superior (API 31+)
- **RAM**: 4 GB
- **Google Play Services**: Actualizado
- **Conexión**: Internet para mapas

---

## 🔮 Futuras Mejoras

### Funcionalidades Planificadas

1. **Búsqueda y Filtros**
   - Buscar por título, contenido o etiquetas
   - Filtrar por fecha, ubicación o tags
   - Ordenamiento personalizable

2. **Exportación/Importación**
   - Exportar notas a JSON/CSV
   - Backup y restauración
   - Compartir notas individuales

3. **Notas Archivadas**
   - Pantalla dedicada a notas archivadas
   - Restaurar notas archivadas
   - Eliminación permanente

4. **Mejoras de Mapa**
   - Clustering de marcadores cercanos
   - Filtros en el mapa
   - Rutas entre ubicaciones
   - Heatmap de frecuencia

5. **Sincronización Cloud**
   - Backup automático en la nube
   - Sincronización entre dispositivos
   - Compartir notas con otros usuarios

6. **Personalización**
   - Temas personalizados
   - Fuentes configurables
   - Widgets de pantalla principal

7. **Analytics**
   - Estadísticas de uso
   - Lugares más visitados
   - Línea temporal de notas

---

## 👥 Créditos

**Desarrollador**: mxgrc  
**Repositorio**: [github.com/mxgrc/GeoNote](https://github.com/mxgrc/GeoNote)  
**Framework**: Android Jetpack Compose  
**Mapas**: Google Maps Platform  

---

## 📄 Licencia

Este proyecto es de código abierto. Consultar el archivo LICENSE en el repositorio para más detalles.

---

## 📞 Contacto y Soporte

Para reportar bugs, sugerir features o contribuir al proyecto, por favor visita el repositorio en GitHub y crea un issue.

---

**Documento generado el:** Octubre 2025  
**Versión del documento:** 1.0  
**Estado del proyecto:** ✅ Funcional y en desarrollo activo
