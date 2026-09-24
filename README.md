# 💱 Обмен валют (Currency Exchange Service)

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Type-REST%20API%20(WAR)-informational" alt="REST API"/>
  <img src="https://img.shields.io/badge/Jakarta%20Servlet-6.0-blueviolet" alt="Jakarta Servlet 6.0"/>
  <img src="https://img.shields.io/badge/Tomcat-10.1%2B-yellow?logo=apachetomcat&logoColor=white" alt="Tomcat 10.1+"/>
  <img src="https://img.shields.io/badge/DB-SQLite-003B57?logo=sqlite&logoColor=white" alt="SQLite"/>
  <img src="https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white" alt="Maven"/>
</p>

<p align="center">
  REST API на чистых Java-сервлетах для работы с валютами и обменными курсами: справочник валют, курсы, конвертация суммы (прямой, обратный и кросс-курс через USD) и небольшой веб-интерфейс поверх API.
</p>

---

## 📖 О проекте

Учебный проект **«Обмен валют»** курса [`zhukovsd/java-backend-learning-course`](https://zhukovsd.github.io/java-backend-learning-course/). Полное техническое задание — в [`SPEC.md`](./SPEC.md), замечания по итогам код-ревью — в [`REVIEW.md`](./REVIEW.md).

Приложение собирается в `WAR`-артефакт и разворачивается на Tomcat. Данные хранятся во встроенной SQLite, отдельный сервер БД не нужен. Схема и начальные данные создаются автоматически при старте приложения.

Что умеет API:

- просматривать список валют и получать валюту по коду;
- добавлять новые валюты;
- просматривать список обменных курсов и получать курс по паре кодов;
- добавлять и изменять обменные курсы;
- считать конвертацию произвольной суммы из одной валюты в другую.

Удаление записей через API, как и требует ТЗ, не реализовано.

Комментарии автора курса по проекту: [YouTube](https://www.youtube.com/watch?v=013b_b7PszM).

> ⚠️ Раздел [«Отличия от ТЗ и известные ограничения»](#-отличия-от-тз-и-известные-ограничения) — обязательный к прочтению: там перечислено, где реализация сознательно или вынужденно расходится с ТЗ.

## 🛠️ Стек технологий

- **Язык:** Java 21 (`maven-compiler-plugin`, `<release>21</release>`) — в коде используются `record` (для `ErrorResponse`), Stream API, `Optional`.
- **Веб:** Jakarta Servlet API 6.0 (`jakarta.servlet`, scope `provided`) — нужен контейнер с поддержкой Jakarta EE 10, то есть **Tomcat 10.1+** (на Tomcat 9 и старше не заработает: другой пакет `javax.servlet`).
- **База данных:** SQLite через [`org.xerial:sqlite-jdbc`](https://github.com/xerial/sqlite-jdbc) 3.53.4.0, чистый JDBC (`PreparedStatement`), без ORM.
- **JSON:** [Gson](https://github.com/google/gson) 2.11.0.
- **Сборка:** Maven, упаковка `war`, итоговое имя артефакта — `currency-exchange-service.war`.
- **Фронтенд:** статическая страница `index.html` на Bootstrap и jQuery (`app.js`), общается с API через AJAX.

## 🏗️ Архитектура

Приложение разбито на слои: **servlet → service → dao → SQLite**. Наружу между слоями передаются DTO, а не сущности БД.

```
src/main/
├── java/
│   ├── servlet/
│   │   ├── AbstractJsonServlet              общие утилиты: writeJson/writeError, разбор form-параметров
│   │   ├── CurrenciesServlet                /currencies          (GET, POST)
│   │   ├── CurrencyServlet                  /currency/*          (GET)
│   │   ├── ExchangeRatesServlet             /exchangeRates       (GET, POST)
│   │   ├── ExchangeRateServlet              /exchangeRate/*      (GET, PATCH)
│   │   └── ExchangeServlet                  /exchange            (GET)
│   ├── service/
│   │   ├── CurrencyService → DefaultCurrencyService
│   │   └── ExchangeService → DefaultExchangeService     CRUD курсов + расчёт конвертации
│   ├── dao/
│   │   ├── Dao<E> (интерфейс)               create / read / readAll / update / delete
│   │   ├── CurrenciesDao → JdbcCurrenciesDao
│   │   └── ExchangeRatesDao → JdbcExchangeRatesDao      + операции по кодам валют
│   ├── entity/
│   │   └── Currency, ExchangeRate           иммутабельные модели предметной области
│   ├── dto/
│   │   ├── CurrencyRequest / CurrencyResponse
│   │   ├── ExchangeRateRequest / ExchangeRateResponse
│   │   └── ExchangeRequest / ExchangeResponse
│   ├── exception/
│   │   └── AppException → BadRequest-, NotFound-, Conflict-, InternalServerErrorException
│   ├── listener/
│   │   └── DbInitListener                   создание схемы и seed-данных при старте
│   └── util/
│       └── SqlLoader                        загрузка .sql-файлов из classpath
├── resources/
│   ├── db.properties                        JDBC URL базы данных
│   └── sql/
│       ├── schema.sql, seed.sql             схема и начальные данные
│       └── currencies.*.sql, exchange-rates.*.sql   запросы DAO (create / read / read-all / update / delete ...)
└── webapp/
    ├── index.html                           веб-интерфейс
    ├── css/bootstrap*.css
    └── js/app.js, bootstrap*.js, jquery-3.6.3.min.js
```

### Как это работает

1. **Старт.** `DbInitListener` (`@WebListener`) читает `db.properties`, открывает соединение с SQLite, в одной транзакции выполняет `schema.sql` (`CREATE TABLE IF NOT EXISTS`), а если таблица `currencies` пуста — ещё и `seed.sql`. Повторные запуски данные не затирают.
2. **Запрос.** Сервлет (`@WebServlet`) достаёт параметры из адреса/формы, проводит первичные проверки и вызывает сервис.
3. **Сервис** содержит бизнес-логику (в том числе расчёт конвертации) и переводит сущности в DTO.
4. **DAO** выполняет SQL из файлов в `resources/sql`, подгружаемых через `SqlLoader`, и мапит `ResultSet` в сущности.
5. **Ответ.** `AbstractJsonServlet` сериализует DTO в JSON (Gson, UTF-8). Ошибки возвращаются в виде `{"message": "..."}`.

> 💡 `PATCH` не поддерживается `HttpServlet` «из коробки» (нет `doPatch`), поэтому `ExchangeRateServlet` переопределяет `service()` и вручную перенаправляет `PATCH` в собственный метод `doPatch`. Тело `PATCH`-запроса Tomcat не разбирает в параметры, поэтому `AbstractJsonServlet.getFormParameter()` вручную парсит `x-www-form-urlencoded`.

### Используемые приёмы

| Приём | Где применяется |
|---|---|
| **Слоистая архитектура** | `servlet` → `service` → `dao`, слои общаются через интерфейсы и DTO |
| **DAO** | `Dao<E>`, `CurrenciesDao`, `ExchangeRatesDao` и их JDBC-реализации |
| **DTO (request/response)** | `dto` — наружу не протекают сущности `Currency` / `ExchangeRate` |
| **Template Method / базовый класс** | `AbstractJsonServlet` — общая работа с JSON и параметрами |
| **SQL вне кода** | запросы лежат в `resources/sql/*.sql` и читаются через `SqlLoader` |
| **Иерархия исключений** | `AppException` и наследники соответствуют HTTP-кодам ответа |
| **`Optional` + композиция стратегий** | `findRate(...).or(() -> findRateViaUsd(...))` в расчёте конвертации |

## 🗄️ База данных

SQLite, таблицы создаются при старте из [`schema.sql`](./schema.sql).

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
| `base_currency_code` | TEXT | `NOT NULL`, `CHECK (LENGTH = 3)`, внешний ключ → `currencies(code)` |
| `target_currency_code` | TEXT | `NOT NULL`, `CHECK (LENGTH = 3)`, внешний ключ → `currencies(code)` |
| `rate` | NUMERIC | `NOT NULL`, `CHECK (rate > 0)` |

Пара `(base_currency_code, target_currency_code)` уникальна.

> ⚠️ **Отличие от ТЗ.** По ТЗ курс должен ссылаться на валюты через `BaseCurrencyId` / `TargetCurrencyId` → `Currencies.ID`. В проекте вместо ID хранятся **коды валют**. Подробности — в разделе [«Отличия от ТЗ»](#-отличия-от-тз-и-известные-ограничения).

### Начальные данные

При первом запуске загружаются 11 валют (`USD`, `RUB`, `EUR`, `GBP`, `JPY`, `AUD`, `CAD`, `CHF`, `CNY`, `SEK`, `NZD`) и 10 курсов: `USD → EUR/GBP/JPY/AUD/CAD/CHF/CNY/SEK/NZD` и `RUB → USD`.

## 🌐 REST API

Все ответы — JSON в UTF-8. Данные в `POST`/`PATCH` передаются как `application/x-www-form-urlencoded`. Коды валют регистронезависимы — приводятся к верхнему регистру.

Формат ошибки для всех запросов:

```json
{
    "message": "Currency with code XXX not found"
}
```

### Валюты

| Метод | Путь | Описание | Коды ответа |
|---|---|---|---|
| `GET` | `/currencies` | список всех валют | `200`, `500` |
| `GET` | `/currency/{CODE}` | валюта по коду, например `/currency/EUR` | `200`, `400`, `404`, `500` |
| `POST` | `/currencies` | добавить валюту, поля: `name`, `code`, `sign` | `201`, `400`, `409`, `500` |

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
    "amount": 10.0,
    "convertedAmount": 13.5
}
```

Курс между валютами `A` и `B` определяется по трём сценариям (в таком порядке):

1. **Прямой** — в таблице есть пара `A → B`, берётся её курс.
2. **Обратный** — есть пара `B → A`, курс `A → B` = `1 / rate(B → A)`.
3. **Кросс-курс через USD** — есть пары `USD → A` и `USD → B` (каждая — прямая или обратная), курс `A → B` = `rate(USD → B) / rate(USD → A)`.

Если ни один сценарий не подошёл — `404`.

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

## 🚀 Сборка и запуск

### Требования

- JDK 21+
- Maven 3.9+
- Apache Tomcat **10.1+**

### 1. Настройте путь к базе данных

Путь к файлу SQLite задаётся в [`src/main/resources/db.properties`](./db.properties):

```properties
url=jdbc:sqlite:/var/lib/tomcat10/data/currency.db
```

> ⚠️ По умолчанию значение рассчитано на Linux-сервер с Tomcat 10 из пакетного менеджера. **На другой машине его нужно поменять** (например, `jdbc:sqlite:/home/user/currency.db` или `jdbc:sqlite:C:/data/currency.db`) и **пересобрать приложение** — переопределения через переменную окружения пока нет (см. [ограничения](#-отличия-от-тз-и-известные-ограничения)). Каталог должен существовать, а пользователь, под которым работает Tomcat, — иметь право писать в него. Сам файл БД создаётся автоматически.

### 2. Соберите WAR

```bash
mvn clean package
# результат: target/currency-exchange-service.war
```

### 3. Разверните на Tomcat

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

### Запуск из IntelliJ IDEA

1. Откройте проект как Maven-проект с JDK 21.
2. Создайте конфигурацию **Tomcat Server → Local** (Tomcat 10.1+) и добавьте артефакт `war exploded`.
3. В поле *Application context* укажите `/currency-exchange-service`.
4. Проверьте, что путь в `db.properties` указывает на существующий каталог.

## ⚠️ Отличия от ТЗ и известные ограничения

Ниже — честный список расхождений с [`SPEC.md`](./SPEC.md) и недоработок. Большая часть из них разобрана в [`REVIEW.md`](./REVIEW.md) и является планом улучшений.

**Схема БД**

- В `exchange_rates` хранятся **коды валют** (`base_currency_code`, `target_currency_code`), а не `BaseCurrencyId` / `TargetCurrencyId`, как требует ТЗ. Это было сделано ради упрощения запросов (не нужен предварительный поиск ID валюты по коду), но противоречит ТЗ и делает схему менее нормализованной: код валюты дублируется в двух таблицах. Приведение к схеме из ТЗ — в планах.
- Имена таблиц и колонок — в `snake_case` (`currencies`, `exchange_rates`), а не `Currencies` / `ExchangeRates`, как в ТЗ; поле `name` вместо `FullName`.
- `rate` хранится как `NUMERIC` без явного ограничения в 6 знаков после запятой.

**Числа**

- Для `rate`, `amount` и `convertedAmount` используется `float`. Для денежных расчётов это плохая практика (накопление погрешности), правильное решение — `BigDecimal`.

**Валидация и ошибки**

- Проверки данных разбросаны по сервлетам, `DefaultExchangeService` и `CHECK`-ограничениям БД — единого валидатора нет.
- Нарушения `UNIQUE`-ограничений на уровне БД пока приводят к `500` вместо `409` (повторное добавление существующей валюты или пары курсов).
- Не все обязательные параметры проверяются на `null`: отсутствие `baseCurrencyCode` / `targetCurrencyCode` в `POST /exchangeRates` или `from` / `to` в `GET /exchange` приводит к `NullPointerException` и `500`.
- Длина `sign` при `POST /currencies` в сервлете не проверяется.
- Обработка исключений дублируется в каждом сервлете (`try-catch`), централизованного обработчика нет.
- `POST /exchangeRates` запрещает добавление пары `B → A`, если уже существует `A → B` (возвращается `400`). ТЗ этого не требует: уникальной должна быть только «прямая» пара, а обратный курс может отличаться.

**Соединения с БД**

- Каждый DAO-объект держит одно `Connection` на всё время жизни, а сервлеты создают DAO в полях. Сервлеты — многопоточные объекты, поэтому это единая точка конкуренции; нужен пул соединений (например, HikariCP) или `DataSource`.
- Внешние ключи (`PRAGMA foreign_keys = ON`) включаются только в соединении инициализации, в соединениях DAO — нет.

**Конфигурация и структура**

- JDBC URL зашит в `db.properties`, переопределить его переменной окружения (например, `DB_URL`) нельзя.
- Зависимости собираются в самих сервлетах и сервисах (`new DefaultExchangeService(new JdbcExchangeRatesDao())`), а не через единую точку сборки в `ServletContextListener`.
- `DefaultExchangeService` совмещает CRUD над курсами и расчёт конвертации — планируется разделение на `ExchangeRateService` и `ExchangeService`.
- В `pom.xml` остался `junit:3.8.1`, хотя тестов в проекте нет.
- Стартовые данные: курс `RUB → USD = 75.0` в `seed.sql` означает «1 рубль = 75 долларов»; по смыслу здесь нужна пара `USD → RUB`. При желании поправьте seed.

**Статика**

- В присланном архиве файл `jquery-3.6.3.min.js` пустой (0 байт) — если веб-интерфейс не работает, убедитесь, что в `webapp/js/` лежит настоящая библиотека jQuery.

## ✅ Проверка работоспособности

Корректность API проверялась ботом [@currency_exchange_api_bot](https://t.me/currency_exchange_api_bot) из материалов курса. На момент ревью: **98 кейсов — 91 прошёл, 7 упали** (`TC-018`, `TC-025a`, `TC-054`, `TC-056`, `TC-058`, `TC-091`, `TC-092`) — все относятся к пунктам про `409` при дубликатах, валидацию `sign` и `NullPointerException` при отсутствии параметров, перечисленным выше. Подробности — в [`REVIEW.md`](./REVIEW.md).

## 🗺️ План доработок

По итогам ревью, в порядке приоритета:

1. Исправить непройденные кейсы бота (`409` вместо `500`, `400` вместо `500` при отсутствии параметров, проверка `sign`).
2. Перевести `rate`, `amount`, `convertedAmount` на `BigDecimal` во всех слоях.
3. Заменить одно долгоживущее `Connection` на `DataSource` / HikariCP.
4. Вынести сборку зависимостей в `ServletContextListener` (DI через конструкторы), а обработку исключений — в `@WebFilter`.
5. Вынести валидацию и нормализацию входных данных в отдельный класс-валидатор.
6. Привести схему БД к ТЗ (`BaseCurrencyId` / `TargetCurrencyId`), убрать запрет на «зеркальную» пару.
7. Переименовать DAO в единственное число (`CurrencyDao`, `ExchangeRateDao`), заменить DTO на `record`, убрать неиспользуемый код и лишние зависимости, вынести `CurrencyMapper`.
8. Добавить переопределение JDBC URL через переменную окружения `DB_URL`.

## 🎓 Контекст

Проект выполнен в рамках курса **[Java Backend Learning Course](https://zhukovsd.github.io/java-backend-learning-course/)** ([zhukovsd](https://github.com/zhukovsd)) как практика работы с MVC, REST API, HTTP-кодами ответа и SQL. Полное техническое задание — в [`SPEC.md`](./SPEC.md).

---

<p align="center"><i>Учебный проект. Реализован в образовательных целях для практики построения REST API на сервлетах, работы с SQL и слоистой архитектуры.</i></p>

