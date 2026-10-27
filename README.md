# 💱 Обмен валют (Currency Exchange Service)

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Type-REST%20API%20(WAR)-informational" alt="REST API"/>
  <img src="https://img.shields.io/badge/Jakarta%20Servlet-6.0-blueviolet" alt="Jakarta Servlet 6.0"/>
  <img src="https://img.shields.io/badge/Tomcat-10.1%2B-yellow?logo=apachetomcat&logoColor=white" alt="Tomcat 10.1+"/>
  <img src="https://img.shields.io/badge/DB-SQLite-003B57?logo=sqlite&logoColor=white" alt="SQLite"/>
  <img src="https://img.shields.io/badge/Pool-HikariCP-3a7ca5" alt="HikariCP"/>
  <img src="https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white" alt="Maven"/>
</p>

<p align="center">
  REST API на чистых Java-сервлетах для работы с валютами и обменными курсами: справочник валют, курсы, конвертация суммы (прямой, обратный и кросс-курс через USD) и небольшой веб-интерфейс поверх API.
</p>

---

## 📖 О проекте

Учебный проект **«Обмен валют»** курса [`zhukovsd/java-backend-learning-course`](https://zhukovsd.github.io/java-backend-learning-course/). Полное техническое задание — в [`SPEC.md`](./SPEC.md). Замечания первого код-ревью — в [`REVIEW.md`](./REVIEW.md) (это исторический документ: большинство пунктов из него уже исправлено, актуальный статус см. в разделе [«Что изменилось после ревью»](#-что-изменилось-после-ревью)).

Приложение собирается в `WAR`-артефакт и разворачивается на Tomcat. Данные хранятся во встроенной SQLite, отдельный сервер БД не нужен. Схема и начальные данные создаются автоматически при старте приложения.

Что умеет API:

- просматривать список валют и получать валюту по коду;
- добавлять новые валюты;
- просматривать список обменных курсов и получать курс по паре кодов;
- добавлять и изменять обменные курсы;
- считать конвертацию произвольной суммы из одной валюты в другую.

Удаление записей через API, как и требует ТЗ, не реализовано (методы `delete` в DAO есть, но наружу не выставлены).

## 🛠️ Стек технологий

- **Язык:** Java 21 (`<release>21</release>`). Используются `record` (DTO и `ErrorResponse`), pattern matching для `instanceof`, `Optional`.
- **Веб:** Jakarta Servlet API 6.0 (`provided`) — нужен контейнер с Jakarta EE 10, то есть **Tomcat 10.1+** (на Tomcat 9 и старше не заработает: другой пакет `javax.servlet`).
- **База данных:** SQLite через [`org.xerial:sqlite-jdbc`](https://github.com/xerial/sqlite-jdbc) 3.53.4.0, чистый JDBC (`PreparedStatement`), без ORM.
- **Пул соединений:** [HikariCP](https://github.com/brettwooldridge/HikariCP) 7.0.2.
- **Маппинг:** [MapStruct](https://mapstruct.org/) 1.6.3 (entity ↔ DTO) и собственные `ResultSetMapper` (строка БД → entity).
- **JSON:** [Gson](https://github.com/google/gson) 2.11.0.
- **Деньги:** `BigDecimal` во всех слоях (`rate`, `amount`, `convertedAmount`).
- **Сборка:** Maven, упаковка `war`, имя артефакта — `currency-exchange-service.war`.
- **Фронтенд:** статическая страница `index.html` на Bootstrap и jQuery (`app.js`), общается с API через AJAX.

## 🏗️ Архитектура

Приложение разбито на слои: **servlet → service → dao → SQLite**. Между слоями передаются DTO (`record`), а не сущности БД. Все зависимости создаются в одном месте — `AppInitListener` — и передаются через конструкторы.

```
src/main/
├── java/
│   ├── listener/
│   │   └── AppInitListener            точка сборки: DataSource, DAO, сервисы, валидаторы, регистрация сервлетов
│   ├── filter/
│   │   └── ExceptionHandlingFilter    единый обработчик исключений → JSON {"message": "..."} + HTTP-код
│   ├── servlet/
│   │   ├── AbstractJsonServlet        writeJson / writeError, разбор form-параметров (для PATCH)
│   │   ├── CurrenciesServlet          /currencies          (GET, POST)
│   │   ├── CurrencyServlet            /currency/*          (GET)
│   │   ├── ExchangeRatesServlet       /exchangeRates       (GET, POST)
│   │   ├── ExchangeRateServlet        /exchangeRate/*      (GET, PATCH)
│   │   └── ExchangeServlet            /exchange            (GET)
│   ├── validator/
│   │   ├── RequestValidator           базовые проверки: isBlank, isValidLength
│   │   ├── CurrencyValidator          name / code / sign
│   │   └── ExchangeRateValidator      пара кодов, rate
│   ├── parser/
│   │   └── BigDecimalParser           строка → BigDecimal (принимает и «,», и «.»)
│   ├── service/
│   │   ├── CurrencyService → CurrencyServiceImpl
│   │   ├── ExchangeRateService → ExchangeRateServiceImpl     CRUD курсов
│   │   └── ExchangeService → ExchangeServiceImpl             расчёт конвертации
│   ├── dao/
│   │   ├── Dao<E>                     create / read / readAll / update / delete
│   │   ├── CurrencyDao → JdbcCurrencyDao
│   │   └── ExchangeRateDao → JdbcExchangeRateDao             + операции по кодам валют
│   ├── mapper/
│   │   ├── CurrencyMapper, ExchangeRateMapper                MapStruct: entity ↔ DTO
│   │   └── ResultSetMapper → CurrencyResultSetMapper, ExchangeRateResultSetMapper
│   ├── entity/                        Currency, ExchangeRate
│   ├── dto/                           CurrencyRequest/Response, ExchangeRateRequest/Response, ExchangeRequest/Response
│   ├── exception/
│   │   └── AppException → ValidationException (400), NotFoundException (404), ConflictException (409), DatabaseException (500)
│   ├── db/
│   │   └── DatabaseInitializer        создание схемы и seed-данных при старте
│   └── util/
│       ├── DataSourceFactory          HikariCP + SQLite (foreign_keys, WAL, busy_timeout)
│       ├── PropertiesLoader           чтение настроек: переменная окружения → system property → файл
│       └── SqlLoader                  загрузка .sql-файлов из classpath
├── resources/
│   ├── data-source.properties         URL базы и параметры пула
│   └── sql/
│       ├── schema.sql, seed.sql       схема и начальные данные
│       └── currencies.*.sql, exchange-rates.*.sql   запросы DAO
└── webapp/
    ├── index.html                     веб-интерфейс
    ├── css/bootstrap*.css
    └── js/app.js, bootstrap*.js, jquery-3.6.3.min.js
```

### Как это работает

1. **Старт.** `AppInitListener` (`@WebListener`) создаёт пул `HikariDataSource` через `DataSourceFactory`, `DatabaseInitializer` в одной транзакции выполняет `schema.sql` (`CREATE TABLE IF NOT EXISTS`), а если таблица `currencies` пуста — ещё и `seed.sql`. Затем собираются DAO, сервисы, валидаторы и программно регистрируются сервлеты. Повторные запуски данные не затирают. При остановке приложения пул закрывается.
2. **Запрос.** Сервлет достаёт параметры из адреса/формы, вызывает валидатор и передаёт DTO в сервис.
3. **Сервис** содержит бизнес-логику (в том числе расчёт конвертации) и переводит сущности в DTO через MapStruct.
4. **DAO** выполняет SQL из файлов в `resources/sql`. Каждый метод берёт соединение из пула в `try-with-resources` и возвращает его сразу после запроса.
5. **Ответ.** `AbstractJsonServlet` сериализует DTO в JSON (Gson, UTF-8).
6. **Ошибки.** Сервлеты и DAO просто бросают исключения. `ExceptionHandlingFilter` (`@WebFilter("/*")`) перехватывает их и превращает в `{"message": "..."}` с нужным HTTP-кодом. Всё неожиданное отдаётся как `500` с сообщением `Unknown error`.

> 💡 `PATCH` не поддерживается `HttpServlet` «из коробки» (нет `doPatch`), поэтому `ExchangeRateServlet` переопределяет `service()` и вручную направляет `PATCH` в собственный метод `doPatch`. Тело `PATCH`-запроса Tomcat не разбирает в параметры, поэтому `AbstractJsonServlet.getFormParameter()` вручную парсит `x-www-form-urlencoded`.

### Используемые приёмы

| Приём | Где применяется |
|---|---|
| **Слоистая архитектура** | `servlet` → `service` → `dao`, слои общаются через интерфейсы и DTO |
| **Внедрение зависимостей через конструктор** | всё собирается в `AppInitListener` |
| **DAO + пул соединений** | `Dao<E>`, `CurrencyDao`, `ExchangeRateDao`, `HikariDataSource` |
| **DTO на `record`** | `dto` — наружу не протекают сущности `Currency` / `ExchangeRate` |
| **Централизованная обработка ошибок** | `ExceptionHandlingFilter` + иерархия `AppException` |
| **Отдельные валидаторы** | `CurrencyValidator`, `ExchangeRateValidator` |
| **SQL вне кода** | запросы лежат в `resources/sql/*.sql`, читаются через `SqlLoader` |
| **Маппинг** | MapStruct для DTO, `ResultSetMapper` для строк БД |
| **`Optional` + композиция стратегий** | `findRate(...).or(() -> findRateViaUsd(...))` в расчёте конвертации |

## 🗄️ База данных

SQLite, таблицы создаются при старте из [`schema.sql`](./src/main/resources/sql/schema.sql).

### Таблица `currencies`

| Поле | Тип | Ограничения |
|---|---|---|
| `id` | INTEGER | первичный ключ, автоинкремент |
| `code` | TEXT | `NOT NULL`, `UNIQUE`, `CHECK (LENGTH(code) = 3)` |
| `name` | TEXT | `NOT NULL` |
| `sign` | TEXT | `CHECK (LENGTH(sign) <= 5)` |

### Таблица `exchange_rates`

| Поле | Тип | Ограничения |
|---|---|---|
| `id` | INTEGER | первичный ключ, автоинкремент |
| `base_currency_id` | INTEGER | `NOT NULL`, внешний ключ → `currencies(id)` |
| `target_currency_id` | INTEGER | `NOT NULL`, внешний ключ → `currencies(id)` |
| `rate` | NUMERIC | `NOT NULL`, `CHECK (rate > 0)` |

Пара `(base_currency_id, target_currency_id)` уникальна. Внешние ключи включены для каждого соединения пула (`foreign_keys=true` в `DataSourceFactory`).

> Схема соответствует ТЗ: курс ссылается на валюты по `BaseCurrencyId` / `TargetCurrencyId`. Названия таблиц и колонок — в `snake_case`.

### Начальные данные

При первом запуске загружаются 11 валют (`USD`, `RUB`, `EUR`, `GBP`, `JPY`, `AUD`, `CAD`, `CHF`, `CNY`, `SEK`, `NZD`) и 10 курсов, у всех базовая валюта — `USD`: `USD → EUR/GBP/JPY/AUD/CAD/CHF/CNY/SEK/NZD/RUB`.

> ⚠️ `seed.sql` выполняется только при **пустой** таблице `currencies`. Если база уже создана, изменения в `seed.sql` на неё не повлияют: удалите файл БД или поправьте курс через `PATCH`. Курс `USD → RUB` должен означать «сколько рублей за 1 доллар» (порядка 70–90), а не `0.014`.

## 🌐 REST API

Все ответы — JSON в UTF-8. Данные в `POST`/`PATCH` передаются как `application/x-www-form-urlencoded`. Коды валют регистронезависимы — приводятся к верхнему регистру. Числа с запятой (`3,5`) принимаются так же, как с точкой.

Формат ошибки для всех запросов:

```json
{
    "message": "Currency with code XXX not found"
}
```

| Код | Когда |
|---|---|
| `400` | не передан обязательный параметр, он пустой, слишком длинный или не является числом; `rate`/`amount` ≤ 0 |
| `404` | валюта или курс не найдены; при `POST /exchangeRates` одной из валют пары нет |
| `409` | валюта или пара курсов уже существует |
| `500` | ошибка БД или непредвиденная ошибка |

### Валюты

| Метод | Путь | Описание | Коды ответа |
|---|---|---|---|
| `GET` | `/currencies` | список всех валют | `200`, `500` |
| `GET` | `/currency/{CODE}` | валюта по коду, например `/currency/EUR` | `200`, `400`, `404`, `500` |
| `POST` | `/currencies` | добавить валюту, поля: `name`, `code`, `sign` (до 3 символов) | `201`, `400`, `409`, `500` |

Пример ответа:

```json
{
    "id": 3,
    "code": "EUR",
    "name": "Euro",
    "sign": "€"
}
```

### Обменные курсы

| Метод | Путь | Описание | Коды ответа |
|---|---|---|---|
| `GET` | `/exchangeRates` | список всех курсов | `200`, `500` |
| `GET` | `/exchangeRate/{BASE}{TARGET}` | курс пары, например `/exchangeRate/USDRUB` | `200`, `400`, `404`, `500` |
| `POST` | `/exchangeRates` | добавить курс, поля: `baseCurrencyCode`, `targetCurrencyCode`, `rate` | `201`, `400`, `404`, `409`, `500` |
| `PATCH` | `/exchangeRate/{BASE}{TARGET}` | изменить курс, поле: `rate` | `200`, `400`, `404`, `500` |

Пара `A → B` и обратная ей `B → A` — независимые записи: добавление `B → A` при существующей `A → B` разрешено, `409` возвращается только при повторе той же пары.

Пример ответа:

```json
{
    "id": 1,
    "baseCurrency": { "id": 1, "code": "USD", "name": "United States Dollar", "sign": "$" },
    "targetCurrency": { "id": 3, "code": "EUR", "name": "Euro", "sign": "€" },
    "rate": 0.85
}
```

### Конвертация

`GET /exchange?from=USD&to=AUD&amount=10`

```json
{
    "baseCurrency": { "id": 1, "code": "USD", "name": "United States Dollar", "sign": "$" },
    "targetCurrency": { "id": 6, "code": "AUD", "name": "Australian Dollar", "sign": "$" },
    "rate": 1.35,
    "amount": 10,
    "convertedAmount": 13.50
}
```

Курс между валютами `A` и `B` определяется по трём сценариям (в таком порядке):

1. **Прямой** — в таблице есть пара `A → B`, берётся её курс как есть.
2. **Обратный** — есть пара `B → A`, курс `A → B` = `1 / rate(B → A)`.
3. **Кросс-курс через USD** — есть пары `USD → A` и `USD → B` (каждая — прямая или обратная), курс `A → B` = `rate(USD → B) / rate(USD → A)`.

Если ни один сценарий не подошёл — `404`.

**Округление.** Курсы, вычисленные для обратного и кросс-сценариев, округляются до 6 знаков (`HALF_UP`). Итоговая `convertedAmount` — до 2 знаков (`HALF_UP`).

### Примеры запросов

```bash
# Список валют
curl http://localhost:8080/currency-exchange-service/currencies

# Добавить валюту
curl -X POST http://localhost:8080/currency-exchange-service/currencies \
     -d "name=Polish Zloty" -d "code=PLN" -d "sign=zł"

# Добавить курс
curl -X POST http://localhost:8080/currency-exchange-service/exchangeRates \
     -d "baseCurrencyCode=USD" -d "targetCurrencyCode=PLN" -d "rate=3.9"

# Изменить курс
curl -X PATCH http://localhost:8080/currency-exchange-service/exchangeRate/USDPLN -d "rate=4.05"

# Конвертация (кросс-курс через USD: EUR → JPY)
curl "http://localhost:8080/currency-exchange-service/exchange?from=EUR&to=JPY&amount=100"
```

## 🖥️ Веб-интерфейс

Страница `index.html` (Bootstrap + jQuery) доступна по корню приложения: `http://<host>:8080/currency-exchange-service/`. На ней можно посмотреть список валют и курсов, добавить валюту, добавить и изменить курс, выполнить конвертацию. Адрес API формируется в `app.js` как `window.location.origin + "/currency-exchange-service"`, поэтому **контекстный путь приложения должен совпадать с именем WAR-файла** (`currency-exchange-service`).

## ⚙️ Конфигурация

Настройки лежат в [`src/main/resources/data-source.properties`](./src/main/resources/data-source.properties):

```properties
url=jdbc:sqlite:./data/currency.db
maximumPoolSize=10
minimumIdle=2
connectionTimeout=30000
idleTimeout=600000
maxLifetime=1800000
```

- **`url`** можно переопределить без пересборки: переменной окружения или system property `DB_URL` (приоритет: окружение → system property → файл). Родительский каталог файла БД создаётся автоматически. Относительный путь считается от рабочей директории процесса Tomcat — на сервере лучше задавать абсолютный.
- **`maximumPoolSize`** — размер пула. Значение `2` слишком мало, если сервис получает пачки одновременных запросов (например, при проверке ботом): запросы ждут свободное соединение до `connectionTimeout`. Рекомендуется `10`. SQLite в режиме WAL читает параллельно, а записи выстраиваются в очередь (`busy_timeout=5000`).
- Параметры `foreign_keys`, `journal_mode=WAL` и `busy_timeout` задаются в `DataSourceFactory`.

Пример переопределения пути к БД на сервере:

```bash
export DB_URL=jdbc:sqlite:/var/lib/tomcat10/data/currency.db
# или в setenv.sh Tomcat: CATALINA_OPTS="-DDB_URL=jdbc:sqlite:/var/lib/tomcat10/data/currency.db"
```

Каталог должен существовать, а пользователь, под которым работает Tomcat, — иметь право писать в него.

## 🚀 Сборка и запуск

### Требования

- JDK 21+
- Maven 3.9+
- Apache Tomcat **10.1+**

### 1. Соберите WAR

```bash
mvn clean package
# результат: target/currency-exchange-service.war
```

MapStruct генерирует реализации мапперов на этапе компиляции (annotation processor настроен в `pom.xml`).

### 2. Разверните на Tomcat

Скопируйте WAR в каталог `webapps` Tomcat (или загрузите через Manager App) и запустите сервер:

```bash
cp target/currency-exchange-service.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/startup.sh
```

После старта приложение доступно по адресу:

```text
http://localhost:8080/currency-exchange-service/
```

На удалённом сервере адрес будет `http://<server_ip>:8080/currency-exchange-service/`.

> ⚠️ При обновлении версии на сервере удалите старый развёрнутый каталог `webapps/currency-exchange-service/` вместе со старым WAR и **перезапустите Tomcat**, иначе может продолжить работать прежняя сборка.

### Запуск из IntelliJ IDEA

1. Откройте проект как Maven-проект с JDK 21.
2. Создайте конфигурацию **Tomcat Server → Local** (Tomcat 10.1+) и добавьте артефакт `war exploded`.
3. В поле *Application context* укажите `/currency-exchange-service`.
4. При необходимости задайте `DB_URL` в переменных окружения конфигурации.

## ✅ Что изменилось после ревью

По сравнению с версией, с которой писался [`REVIEW.md`](./REVIEW.md), реализовано:

- схема приведена к ТЗ: курсы ссылаются на валюты по `id`;
- все денежные значения переведены с `float` на `BigDecimal`;
- вместо одного долгоживущего `Connection` — пул HikariCP, соединения берутся и возвращаются на каждый запрос;
- внешние ключи включены для всех соединений;
- зависимости собираются в `AppInitListener`, а не внутри сервлетов и сервисов;
- обработка ошибок вынесена в `ExceptionHandlingFilter`, иерархия исключений сохраняет причину (`cause`);
- валидация вынесена в `CurrencyValidator` / `ExchangeRateValidator`, добавлена проверка длины `sign` и обязательных параметров (`400` вместо `500` / `NullPointerException`);
- нарушения `UNIQUE` дают `409`, отсутствие одной из валют при добавлении курса — `404`;
- убрано ограничение на «зеркальные» пары;
- `ExchangeServiceImpl` и `ExchangeRateServiceImpl` разделены;
- DAO переименованы в единственное число, DTO — `record`, добавлены MapStruct-мапперы;
- JDBC URL можно переопределить через `DB_URL`;
- исправлен `PATCH /exchangeRate/{pair}`: используется запрос по кодам валют (`exchange-rates.update-by-currency-codes.sql`);
- исправлена утечка соединения в `JdbcCurrencyDao.readByCode`, из-за которой при пуле из 2 соединений сервис переставал отвечать под нагрузкой.

## ⚠️ Известные ограничения

- В проекте нет автотестов — проверка выполняется ботом и вручную.
- `ExchangeRateMapper.toEntity(...)` и `ExchangeServiceImpl.hasMirrorExchangeRate(...)` сейчас не используются и могут быть удалены.
- Сообщение валидации суммы в `/exchange` ссылается на поле `rate` (используется общий `validateRate`).
- Курсы в обратном и кросс-сценариях хранят 6 знаков после запятой; для валют с очень маленьким курсом (например, `JPY → USD`) точность итоговой суммы ограничена этим значением.
- Не проверяется, что `from` и `to` в `/exchange` различаются.

## 🧪 Проверка работоспособности

Корректность API проверялась ботом [@currency_exchange_api_bot](https://t.me/currency_exchange_api_bot) из материалов курса (запускается командой `/run <url>`). Результат последнего прогона (27.10.2026): **98 кейсов — 96 прошли, 2 упали** (`TC-033`, `TC-034`). В обоих упавших кейсах бот проверял «несуществующую» валюту, но отправил запрос `GET /currency/JPY`, а такая валюта в базе есть, поэтому `200` — корректный ответ сервера. Для отсутствующего кода (например, `/currency/ZZZ`) приложение возвращает `404` с `{"message": ...}`. Все три сценария конвертации (`TC-079`–`TC-090`), валидация, `409` и `404` проходят.

## 🎓 Контекст

Проект выполнен в рамках курса **[Java Backend Learning Course](https://zhukovsd.github.io/java-backend-learning-course/)** ([zhukovsd](https://github.com/zhukovsd)) как практика работы с MVC, REST API, HTTP-кодами ответа и SQL. Полное техническое задание — в [`SPEC.md`](./SPEC.md).

---

<p align="center"><i>Учебный проект. Реализован в образовательных целях для практики построения REST API на сервлетах, работы с SQL и слоистой архитектуры.</i></p>
