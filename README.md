# Learn Languages Bitch

Офлайн Android-приложение для изучения английского, русского и вьетнамского. Носитель любого из трёх языков может учить остальные два.

- Пакет: `dev.sergey.triad`
- minSdk 31, targetSdk 35, JDK 17
- Kotlin, Jetpack Compose, Material 3, Hilt, Room
- Несколько локальных профилей, интервальные повторения (FSRS), озвучка TTS устройства, тихий клик при выборе ответа
- Без сервера, аккаунтов, Google Play, AAB и биллинга
- Версия задаётся в `gradle.properties` (`app.versionName` / `app.versionCode`) и попадает в имя APK

Целевой телефон для установки — Samsung Galaxy S24 Ultra, но подойдёт любой Android 12+.

## Скачать APK

- Последняя версия: https://github.com/ATOMCK542/LearnLanguagesBitch/releases/latest
- Архив предыдущих: https://github.com/ATOMCK542/LearnLanguagesBitch/releases

Каждый тег `v{versionName}` — отдельный релиз с APK `LearnLanguagesBitch-{versionName}.apk`. Старые не удаляются.

## Требования

- JDK 17
- Android SDK с platform 35 и build-tools (переменная `ANDROID_HOME` или `local.properties`)
- Для установки на телефон: USB-отладка и режим передачи файлов

`local.properties` в git не входит. После клонирования укажи SDK:

```bash
echo "sdk.dir=/path/to/android-sdk" > local.properties
```

Если в корне есть каталог `.tools/` (локальный JDK, SDK и Gradle), `./gradlew` подхватит его сам.

## Сборка APK

Одна команда из корня репозитория:

```bash
./gradlew assembleDebug
```

Готовый файл (версия из `versionName`):

```
app/build/outputs/apk/debug/LearnLanguagesBitch-1.6.0.apk
```

В Cursor: задача **Build APK** (Ctrl+Shift+B). Android Studio для релиза не нужна. AAB и Play Console не используются.

Публикация версии в GitHub: после коммита `scripts/release.sh` (тег `v{versionName}`). Actions собирает APK и открывает Release.

## Установка

Телефон по USB, отладка включена:

```bash
./gradlew installDebug
```

В Cursor: задача **Install APK**. Либо скопируй `LearnLanguagesBitch-*.apk` на устройство и установи как обычный APK.

С 1.3.2 все APK подписываются одним ключом (`keystore/debug.jks`), поэтому новую версию можно ставить поверх. Сборки до 1.3.2 с GitHub подписаны разными ключами раннера — их Android не заменит: один раз удали приложение и поставь заново (сначала экспорт профиля в Ещё).

## Тесты

```bash
./gradlew test
```

В Cursor: задача **Test** (`./gradlew check`).

## Gradle Wrapper

В репозитории лежат `gradlew` и `gradle/wrapper/`. Отдельная установка Gradle не нужна: wrapper скачает Gradle 8.11.1 сам.

Лицензия: MIT.
