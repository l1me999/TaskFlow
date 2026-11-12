# TaskFlow — правила проекта

Личное приложение для задач, дедлайнов и заметок. Работает на Windows и Android,
данные пока хранятся только локально (local-first).

## Стек

| Слой | Технология |
| --- | --- |
| Язык | Kotlin 2.4.21 |
| UI | Compose Multiplatform 1.12.2, Material 3 |
| Общий модуль | Kotlin Multiplatform (`android` + `desktop`/JVM) |
| Хранилище | JSON-документ через okio, атомарная запись |
| Сборка | Gradle 9.x + AGP 9.5.x, JDK 21 |
| Android | compileSdk 37, targetSdk 36, minSdk 26 |

Версии зафиксированы в `gradle/libs.versions.toml`. Не обновляй их без запроса.

Подводные камни AGP 9:

- Kotlin в `androidApp` встроен в AGP. Плагин `org.jetbrains.kotlin.android`
  подключать нельзя — сборка упадёт.
- KMP-модуль с Android-таргетом использует только
  `com.android.kotlin.multiplatform.library` (`kotlin { android { ... } }`),
  а не `com.android.library` + `androidTarget()`.
- Compose-зависимости `compose.runtime` / `compose.foundation` / `compose.material3`
  в `shared` пока дают deprecation-warning — это ожидаемо, не «исправляй» наугад.

## Структура

```text
shared/      # модели, TaskStore, UI на Compose — почти весь код здесь
androidApp/  # MainActivity, манифест, ресурсы; тонкая оболочка
desktopApp/  # точка входа окна, упаковка MSI; тонкая оболочка
docs/        # архитектура и принятые решения
.opencode/   # субагенты-разработчики
```

## Команды

Все команды — из корня репозитория, в PowerShell.

```powershell
# сборка и запуск десктопа
.\gradlew.bat :desktopApp:run
.\gradlew.bat :desktopApp:packageMsi

# Android
.\gradlew.bat :androidApp:assembleDebug

# тесты общей логики
.\gradlew.bat :shared:desktopTest

# всё сразу
.\gradlew.bat build
```

Требуется JDK 21. Если `JAVA_HOME` в текущей консоли не задан:

```powershell
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.13.21-hotspot"
```

Android SDK ожидается в `%LOCALAPPDATA%\Android\Sdk`; путь также можно прописать
в `local.properties` (`sdk.dir=...`). Файл `local.properties` не коммитится.

## Данные

| Платформа | Файл |
| --- | --- |
| Windows | `%USERPROFILE%\.taskflow\taskflow.json` |
| Android | `filesDir/taskflow.json` внутри приложения |

Схема: `schemaVersion`, `projects[]`, `tasks[]`. Дедлайн хранится строкой ISO
(`YYYY-MM-DD`), поэтому сортировка и сравнение — обычное сравнение строк.

## Соглашения

- Идентификаторы, имена файлов и пакетов — на английском; тексты интерфейса — на русском.
- Общая логика и UI — в `shared/src/commonMain`. Платформенное — только через
  `expect`/`actual` или параметры конструктора.
- `commonMain` не использует `java.*`, `android.*` и файловые пути.
- Пакеты: `dev.amirg.taskflow.*`, `applicationId` — `dev.amirg.taskflow`.
- Новые зависимости — только после согласования с пользователем.
- Не рефакторить код вне задачи.

## Тесты

- Любое изменение поведения в `shared` сопровождается тестом в `commonTest`.
- Новый баг — сначала regression test, потом исправление.
- Перед завершением задачи проверяй затронутые модули, а не только «свой» файл.

## Границы

- Не коммитить секреты, ключи подписи, `local.properties`.
- `git push`, публикация APK/MSI и удаление данных — только с подтверждения пользователя.
- Не менять `applicationId`, `minSdk`, формат хранилища без явного запроса.
- Сетевой слой и синхронизацию не добавлять, пока пользователь не попросил.

## Агенты

В `.opencode/agents/` живут субагенты: `architect`, `kmp-core`, `android-dev`,
`desktop-dev`, `reviewer`. Они запускаются через `subagent` и работают с чистым
контекстом, поэтому вся нужная информация должна быть в этом файле и в
`docs/architecture.md`.
