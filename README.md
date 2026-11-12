# TaskFlow

Личный трекер задач, дедлайнов и заметок по проектам для Windows и Android.
Один Kotlin-код: общий слой данных и UI на Compose Multiplatform, тонкие
оболочки под каждую платформу.

## Что уже есть

- Проекты и задачи с дедлайнами и заметками.
- Сортировка: активные выше, ближайший срок — первым, просрочка подсвечивается.
- Локальное хранение в JSON, атомарная запись, резервная копия при повреждении файла.
- Тесты бизнес-логики и хранилища.

## Чего пока нет

- Синхронизации между телефоном и ноутбуком (следующий этап).
- Редактирования задачи после создания.
- Напоминаний и уведомлений.

## Запуск

Нужны JDK 21 и Android SDK (ставится вместе с Android Studio).

```powershell
# Десктоп
.\gradlew.bat :desktopApp:run

# Android (APK в androidApp\build\outputs\apk\debug)
.\gradlew.bat :androidApp:assembleDebug

# Тесты
.\gradlew.bat :shared:desktopTest
```

Если `JAVA_HOME` не задан:

```powershell
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.13.21-hotspot"
```

## Документация

- `AGENTS.md` — правила проекта, команды, границы.
- `docs/architecture.md` — модули, модель данных, план синхронизации.
