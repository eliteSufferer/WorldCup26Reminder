# WC26 Reminder

Личное Android-приложение: напоминает о выбранных матчах ЧМ-2026 по футболу.
Заранее отмечаешь интересные матчи → приложение кладёт их в системный календарь
и присылает локальное уведомление за N минут до начала. Без серверов, без аккаунтов,
без мусорных новостных пушей.

## Возможности
- Расписание всех 104 матчей из открытого источника (без API-ключа).
- Два таба: **Groups** (список группового этапа) и **Playoff** (турнирная сетка
  с подписями 1/16 → 1/8 → 1/4 → 1/2 → Final + матч за 3-е место).
- Флаги сборных (картинки с flagcdn.com по карте «страна → ISO-код»).
- Отметка «слежу» по любому матчу (в списке и в сетке).
- Точное локальное уведомление перед матчем (по умолчанию за 30 минут).
- Запись выбранного матча в системный календарь (синкается с Google Calendar
  средствами самого Android).
- Фильтры на Groups: поиск по команде, группа, «только избранные»; прошедшие
  матчи скрыты по умолчанию, чип **Show previous** их показывает (со счётом).
- Настройки: время напоминания, целевой календарь, стартовый таб.
- Ежедневное фоновое обновление расписания (ловит правки времени и определившихся
  соперников в плей-офф).
- Время каждого матча конвертируется из таймзоны стадиона в таймзону телефона.

## Источник данных
[`openfootball/worldcup.json`](https://github.com/openfootball/worldcup.json) —
public domain, без ключа:
`https://raw.githubusercontent.com/openfootball/worldcup.json/master/2026/worldcup.json`
URL задаётся в `data/remote/ScheduleApi.kt`.

## Стек
- Kotlin + Jetpack Compose (Material 3)
- Room (кэш матчей + список выбранного)
- Ktor + kotlinx.serialization (загрузка/парсинг JSON)
- WorkManager (ежедневное обновление), AlarmManager (точные напоминания)
- CalendarContract (запись в календарь)

## Сборка
1. Открыть папку проекта в Android Studio (Giraffe+) — Gradle-wrapper уже на месте,
   IDE сама подтянет дистрибутив Gradle 8.11.1 и Android SDK.
2. Указать путь к Android SDK (Android Studio создаст `local.properties`).
3. Запустить конфигурацию `app` на устройстве/эмуляторе с Android 8.0+ (minSdk 26).

Из командной строки (нужен установленный Android SDK и `local.properties`):
```
./gradlew :app:assembleDebug      # собрать APK
./gradlew :app:testDebugUnitTest  # юнит-тесты (парсер таймзон)
```

## Структура
```
app/src/main/java/com/worldcup26/reminder/
 ├─ data/
 │   ├─ remote/   ScheduleApi, DTO, KickoffParser (таймзоны)
 │   ├─ local/    Room: entities, DAO, AppDatabase
 │   ├─ MatchRepository.kt   единый источник правды
 │   └─ AppContainer.kt      ручной DI-граф
 ├─ domain/       Match (UI-модель)
 ├─ work/         ScheduleRefreshWorker, AlarmScheduler, BootReceiver
 ├─ calendar/     CalendarWriter (CalendarContract)
 ├─ notify/       Notifications (канал), ReminderReceiver
 └─ ui/           MainActivity, MatchesViewModel, MatchListScreen, theme/
```

## Разрешения
- `INTERNET` — загрузка расписания.
- `POST_NOTIFICATIONS` (Android 13+) — уведомления.
- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` — точные напоминания.
- `READ_CALENDAR` / `WRITE_CALENDAR` — запись матча в календарь.
- `RECEIVE_BOOT_COMPLETED` — пере-постановка будильников после перезагрузки.

## Дальнейшие шаги (бэклог)
- Сетка плей-офф: соединительные линии между матчами (сейчас выравнивание по
  центру без линий).
- Локализация UI на русский.
- (Опционально) FCM, если захочется пушей в реальном времени про голы.
