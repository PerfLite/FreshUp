# FreshUp (Свежо) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a working Android MVP for tracking product expiration dates with Room DB, Jetpack Compose, and MVVM architecture.

**Architecture:** Single-module Android app with clean architecture layers (data → domain → presentation). Room for persistence, Flow for reactive data, Compose Material 3 for UI.

**Tech Stack:** Kotlin, Jetpack Compose (Material 3), Room DB, MVVM, Coroutines + Flow, Gradle Kotlin DSL

## Global Constraints

- Package: `com.example.freshup`
- Min SDK: 26, Target SDK: 35, Compile SDK: 35
- Kotlin 2.0+, Compose BOM latest stable
- Room with KSP annotation processing
- All code must compile — no TODOs or placeholders
- Dates stored as Long (epoch millis)

---

### Task 1: Project Scaffolding

**Covers:** Project setup (not in spec sections, but required foundation)

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts` (root)
- Create: `gradle.properties`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/example/freshup/FreshUpApp.kt`
- Create: `app/src/main/java/com/example/freshup/MainActivity.kt`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/themes.xml`
- Create: `local.properties`

- [ ] **Step 1: Create root build files**

`settings.gradle.kts`:
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "FreshUp"
include(":app")
```

`build.gradle.kts` (root):
```kotlin
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}
```

`gradle.properties`:
```
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

- [ ] **Step 2: Create app module build file**

`app/build.gradle.kts`:
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.freshup"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.freshup"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

- [ ] **Step 3: Create AndroidManifest and app entry points**

`app/src/main/AndroidManifest.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:name=".FreshUpApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.FreshUp">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.FreshUp">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

`app/src/main/java/com/example/freshup/FreshUpApp.kt`:
```kotlin
package com.example.freshup

import android.app.Application
import com.example.freshup.data.database.AppDatabase

class FreshUpApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
}
```

`app/src/main/java/com/example/freshup/MainActivity.kt`:
```kotlin
package com.example.freshup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.freshup.presentation.main.ProductListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ProductListScreen()
                }
            }
        }
    }
}
```

`app/src/main/res/values/strings.xml`:
```xml
<resources>
    <string name="app_name">Свежо</string>
</resources>
```

`app/src/main/res/values/themes.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.FreshUp" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

- [ ] **Step 4: Verify project builds**

Run from project root:
```bash
export ANDROID_HOME=~/Android/Sdk
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git init && git add -A && git commit -m "feat: project scaffolding with Gradle, Compose, Room dependencies"
```

---

### Task 2: Domain Layer

**Covers:** Spec §2 (Domain Layer)

**Files:**
- Create: `app/src/main/java/com/example/freshup/domain/model/Product.kt`
- Create: `app/src/main/java/com/example/freshup/domain/repository/ProductRepository.kt`

- [ ] **Step 1: Create Product data class**

`app/src/main/java/com/example/freshup/domain/model/Product.kt`:
```kotlin
package com.example.freshup.domain.model

data class Product(
    val id: Int = 0,
    val name: String,
    val category: String,
    val expirationTimestamp: Long,
    val isArchived: Boolean = false
) {
    val daysLeft: Long
        get() {
            val now = System.currentTimeMillis()
            val diff = expirationTimestamp - now
            return diff / (1000 * 60 * 60 * 24)
        }
}
```

- [ ] **Step 2: Create repository interface**

`app/src/main/java/com/example/freshup/domain/repository/ProductRepository.kt`:
```kotlin
package com.example.freshup.domain.repository

import com.example.freshup.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getActiveProducts(): Flow<List<Product>>
    fun getAllProducts(): Flow<List<Product>>
    suspend fun insert(product: Product)
    suspend fun update(product: Product)
    suspend fun archiveProduct(id: Int)
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/freshup/domain/
git commit -m "feat: domain layer with Product model and repository interface"
```

---

### Task 3: Data Layer

**Covers:** Spec §1 (Data Layer)

**Files:**
- Create: `app/src/main/java/com/example/freshup/data/database/ProductEntity.kt`
- Create: `app/src/main/java/com/example/freshup/data/database/ProductDao.kt`
- Create: `app/src/main/java/com/example/freshup/data/database/AppDatabase.kt`
- Create: `app/src/main/java/com/example/freshup/data/repository/ProductRepositoryImpl.kt`

- [ ] **Step 1: Create ProductEntity**

`app/src/main/java/com/example/freshup/data/database/ProductEntity.kt`:
```kotlin
package com.example.freshup.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.freshup.domain.model.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val expirationTimestamp: Long,
    val isArchived: Boolean = false
) {
    fun toDomainModel(): Product = Product(
        id = id,
        name = name,
        category = category,
        expirationTimestamp = expirationTimestamp,
        isArchived = isArchived
    )

    companion object {
        fun fromDomainModel(product: Product): ProductEntity = ProductEntity(
            id = product.id,
            name = product.name,
            category = product.category,
            expirationTimestamp = product.expirationTimestamp,
            isArchived = product.isArchived
        )
    }
}
```

- [ ] **Step 2: Create ProductDao**

`app/src/main/java/com/example/freshup/data/database/ProductDao.kt`:
```kotlin
package com.example.freshup.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isArchived = 0 ORDER BY expirationTimestamp ASC")
    fun getActiveProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY expirationTimestamp ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Insert
    suspend fun insert(product: ProductEntity)

    @Update
    suspend fun update(product: ProductEntity)

    @Query("UPDATE products SET isArchived = 1 WHERE id = :id")
    suspend fun archiveById(id: Int)
}
```

- [ ] **Step 3: Create AppDatabase**

`app/src/main/java/com/example/freshup/data/database/AppDatabase.kt`:
```kotlin
package com.example.freshup.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ProductEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "freshup_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

- [ ] **Step 4: Create ProductRepositoryImpl**

`app/src/main/java/com/example/freshup/data/repository/ProductRepositoryImpl.kt`:
```kotlin
package com.example.freshup.data.repository

import com.example.freshup.data.database.ProductDao
import com.example.freshup.data.database.ProductEntity
import com.example.freshup.domain.model.Product
import com.example.freshup.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(private val productDao: ProductDao) : ProductRepository {
    override fun getActiveProducts(): Flow<List<Product>> =
        productDao.getActiveProducts().map { entities ->
            entities.map { it.toDomainModel() }
        }

    override fun getAllProducts(): Flow<List<Product>> =
        productDao.getAllProducts().map { entities ->
            entities.map { it.toDomainModel() }
        }

    override suspend fun insert(product: Product) {
        productDao.insert(ProductEntity.fromDomainModel(product))
    }

    override suspend fun update(product: Product) {
        productDao.update(ProductEntity.fromDomainModel(product))
    }

    override suspend fun archiveProduct(id: Int) {
        productDao.archiveById(id)
    }
}
```

- [ ] **Step 5: Verify build**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/freshup/data/
git commit -m "feat: data layer with Room DB, DAO, entity, and repository implementation"
```

---

### Task 4: Presentation Layer

**Covers:** Spec §3 (Presentation Layer)

**Files:**
- Create: `app/src/main/java/com/example/freshup/presentation/main/ProductScreenState.kt`
- Create: `app/src/main/java/com/example/freshup/presentation/main/ProductViewModel.kt`
- Create: `app/src/main/java/com/example/freshup/presentation/main/components/ProductCard.kt`
- Create: `app/src/main/java/com/example/freshup/presentation/main/ProductListScreen.kt`

- [ ] **Step 1: Create screen state**

`app/src/main/java/com/example/freshup/presentation/main/ProductScreenState.kt`:
```kotlin
package com.example.freshup.presentation.main

import com.example.freshup.domain.model.Product

sealed class ProductScreenState {
    data object Loading : ProductScreenState()
    data class Success(val products: List<Product>) : ProductScreenState()
    data object Empty : ProductScreenState()
}
```

- [ ] **Step 2: Create ViewModel**

`app/src/main/java/com/example/freshup/presentation/main/ProductViewModel.kt`:
```kotlin
package com.example.freshup.presentation.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshup.FreshUpApp
import com.example.freshup.data.repository.ProductRepositoryImpl
import com.example.freshup.domain.model.Product
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ProductRepositoryImpl

    val screenState: StateFlow<ProductScreenState>

    init {
        val dao = (application as FreshUpApp).database.productDao()
        repository = ProductRepositoryImpl(dao)

        screenState = repository.getActiveProducts()
            .map { products ->
                if (products.isEmpty()) ProductScreenState.Empty
                else ProductScreenState.Success(products)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ProductScreenState.Loading
            )
    }

    fun addTestProduct() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val day = 24 * 60 * 60 * 1000L
            val products = listOf(
                Product(name = "Молоко", category = "Молочные продукты", expirationTimestamp = now + 1 * day),
                Product(name = "Хлеб", category = "Выпечка", expirationTimestamp = now + 3 * day),
                Product(name = "Сыр", category = "Молочные продукты", expirationTimestamp = now + 10 * day),
                Product(name = "Йогурт", category = "Молочные продукты", expirationTimestamp = now + 5 * day),
                Product(name = "Курица", category = "Мясо", expirationTimestamp = now + 2 * day),
            )
            products.forEach { repository.insert(it) }
        }
    }

    fun archiveProduct(id: Int) {
        viewModelScope.launch {
            repository.archiveProduct(id)
        }
    }
}
```

- [ ] **Step 3: Create ProductCard component**

`app/src/main/java/com/example/freshup/presentation/main/components/ProductCard.kt`:
```kotlin
package com.example.freshup.presentation.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.freshup.domain.model.Product

@Composable
fun ProductCard(
    product: Product,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        product.daysLeft <= 2 -> MaterialTheme.colorScheme.errorContainer
        product.daysLeft <= 5 -> Color(0xFFFFF3CD)
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    val contentColor = when {
        product.daysLeft <= 2 -> MaterialTheme.colorScheme.onErrorContainer
        product.daysLeft <= 5 -> Color(0xFF856404)
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${product.daysLeft} дн.",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
        }
    }
}
```

- [ ] **Step 4: Create ProductListScreen**

`app/src/main/java/com/example/freshup/presentation/main/ProductListScreen.kt`:
```kotlin
package com.example.freshup.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.freshup.presentation.main.components.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    viewModel: ProductViewModel = viewModel()
) {
    val state by viewModel.screenState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Свежо") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.addTestProduct() }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить продукт")
            }
        }
    ) { padding ->
        when (val currentState = state) {
            is ProductScreenState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Загрузка...", style = MaterialTheme.typography.bodyLarge)
                }
            }
            is ProductScreenState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Нет продуктов",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            "Нажмите + чтобы добавить",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            is ProductScreenState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currentState.products, key = { it.id }) { product ->
                        ProductCard(product = product)
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 5: Verify full build**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/freshup/presentation/
git commit -m "feat: presentation layer with ViewModel, ProductCard, and ProductListScreen"
```

---

### Task 5: Final Verification

**Covers:** All spec sections — end-to-end compilation check

- [ ] **Step 1: Clean build**

```bash
./gradlew clean assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Verify file structure**

```bash
find app/src -name "*.kt" | sort
```

Expected output:
```
app/src/main/java/com/example/freshup/FreshUpApp.kt
app/src/main/java/com/example/freshup/MainActivity.kt
app/src/main/java/com/example/freshup/data/database/AppDatabase.kt
app/src/main/java/com/example/freshup/data/database/ProductDao.kt
app/src/main/java/com/example/freshup/data/database/ProductEntity.kt
app/src/main/java/com/example/freshup/data/repository/ProductRepositoryImpl.kt
app/src/main/java/com/example/freshup/domain/model/Product.kt
app/src/main/java/com/example/freshup/domain/repository/ProductRepository.kt
app/src/main/java/com/example/freshup/presentation/main/ProductListScreen.kt
app/src/main/java/com/example/freshup/presentation/main/ProductScreenState.kt
app/src/main/java/com/example/freshup/presentation/main/ProductViewModel.kt
app/src/main/java/com/example/freshup/presentation/main/components/ProductCard.kt
```

- [ ] **Step 3: Final commit**

```bash
git add -A && git commit -m "feat: FreshUp MVP complete — all layers implemented and compiling"
```
