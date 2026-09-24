# Привет!

## Работоспособность твоей реализации

- Фронт прикручен 👍

- Корректную работу API проверял с помощью бота @currency_exchange_api_bot:
  ```
  Всего: 98 | ✅ Прошло: 91 | ❌ Упало: 7

  ## Упавшие кейсы

  ### TC-018 POST /currencies повтор с тем же code → 409

  - Причина: Ожидали HTTP 409, получили 500
  - Request: `POST /currencies | Form: name=Conflict&code=RBE&sign=$`
  - Response: `HTTP 500 | Content-Type: application/json;charset=UTF-8 | Body: {"message":"Error creating currency: RBE"}`

  ### TC-025a sign длиннее 3 символов → 400 + {message}

  - Причина: Ожидали HTTP 400, получили 201
  - Request: `POST /currencies | Form: name=LongSign&code=EKO&sign=ABCD`
  - Response: `HTTP 201 | Content-Type: application/json;charset=UTF-8 | Body: {"id":13,"code":"EKO","name":"LongSign","sign":"ABCD"}`

  ### TC-054 повтор POST той же пары A/B → 409

  - Причина: Ожидали HTTP 409, получили 500
  - Request: `POST /exchangeRates | Form: baseCurrencyCode=CGS&targetCurrencyCode=RHE&rate=1.5655`
  - Response: `HTTP 500 | Content-Type: application/json;charset=UTF-8 | Body: {"message":"Error creating exchange rate for CGS/RHE"}`

  ### TC-056 нет baseCurrencyCode → 400 + {message}

  - Причина: Ожидали HTTP 400, получили 500
  - Request: `POST /exchangeRates | Form: targetCurrencyCode=RHE&rate=1`
  - Response: `HTTP 500 | Content-Type: text/html;charset=utf-8 | Body: <!doctype html><html lang="en"><head><title>HTTP Status 500 – Internal Server Error</title>...`

  ### TC-058 нет targetCurrencyCode → 400 + {message}

  - Причина: Ожидали HTTP 400, получили 500
  - Request: `POST /exchangeRates | Form: baseCurrencyCode=CGS&rate=1`
  - Response: `HTTP 500 | Content-Type: text/html;charset=utf-8 | Body: <!doctype html><html lang="en"><head><title>HTTP Status 500 – Internal Server Error</title>...`

  ### TC-091 нет from → 400 + {message}

  - Причина: Ожидали HTTP 400, получили 500
  - Request: `GET /exchange?to=USD&amount=10`
  - Response: `HTTP 500 | Content-Type: text/html;charset=utf-8 | Body: <!doctype html><html lang="en"><head><title>HTTP Status 500 – Internal Server Error</title>...`

  ### TC-092 нет to → 400 + {message}

  - Причина: Ожидали HTTP 400, получили 500
  - Request: `GET /exchange?from=USD&amount=10`
  - Response: `HTTP 500 | Content-Type: text/html;charset=utf-8 | Body: <!doctype html><html lang="en"><head><title>HTTP Status 500 – Internal Server Error</title>...`

  ```
  Нужно обязательно исправить недочеты реализации


## Общие замечания

- Хорошая работа с коммитами, молодец

- Неинформативный `README.md`. Это просто копия ТЗ самого проекта. Нет, это конечно чем-то лучше, чем ничего, но `README.md` предназначено для ознакомления с проектом. Твоя реализация, твои решения, используемые технологии, как поднять, из чего состоит, что прописать и все такое прочее. Само ТЗ на эти вопросы напрямую не отвечает, лишь описывает план действий, и кто его знает как именно ты его реализовал

- И сразу же коснемся разногласия. Схема твоей БД отличается от ТЗ. По ТЗ у тебя в `ExchangeRates` должны храниться `BaseCurrencyId` и `TargetCurrencyId` со ссылками на `Currencies.ID`, а у тебя хранятся непосредственно коды валют:
  ```sql
  base_currency_code TEXT,
  target_currency_code TEXT
  ```
  Тут ты можешь наблюдать проблему того, что копипастить ТЗ в `README.md` не очень здорово. Твоя реализация отличается от того, что требует ТЗ, и по хорошему тебе стоило явно описать это отличие и дать понять почему тобой было принято такое решение. Кроме того, если ты решил отойти от ТЗ ради упрощения, то это встанет тебе боком в будущем. Ты не всегда можешь сделать все так, как хочешь ты сам, и нужно уметь решать проблемы и справляться с неудобными, но необходимыми требованиями
  
- Путь к БД захардкожен внутри `db.properties`:
  ```properties
  url=jdbc:sqlite:/var/lib/tomcat10/data/currency.db
  ```
  На твоем конкретном сервере это может работать, но для запуска на другой машине придется менять адрес этого ресурса вручную и пересобирать приложение. Лучше дать возможность переопределить URL через переменную окружения, например `DB_URL`. И по хорошему описать это в `README.md`, потому что незнакомый с проектом человек не поймет что ему нужно поменять и где для запуска проекта

- Есть важный недочет — использование `float` для курсов, суммы и результатов конвертации. Для денежных и подобных расчетов использовать `float` является плохой практикой из-за особенностей представления чисел с плавающей точкой. Мы обычно работаем с числами в десятичной системе счисления, тогда как компьютер хранит `float` в двоичном виде. Из-за этого многие десятичные дроби не могут быть точно представлены и хранятся их приближенные значения. Это само по себе опасно для таких важных штук как денежные операции, так чем больше мы махинаций проводим с этими числами, тем больше накапливается погрешность. Если хочешь можешь почитать об этой проблеме. В общем, есть такая штука как `BigDecimal`, которая эти проблемы решает и используется в подобных случаях, этот момент нужно везде исправить (DTO, entity, сервисы, DAO) и научиться с ним работать

- Зависимости JUnit лежать в `pom.xml`, но тестов в проекте нет. Тесты в этом проекте не требуются по ТЗ, и раз ты их не пишешь, то не следует тащить лишние зависимости

- Нет единого места для валидации. Часть проверок находится в сервлетах, часть в `DefaultExchangeService`, а часть фактически перекладывается на CHECK констрейнты БД. Из-за этого одинаковые данные в разных эндпоинтах проверяются по-разному. Я бы вынес нормализацию и валидацию кодов, `rate`, `amount`, `name`, `sign` и тд в отдельный класс-валидатор, например

- Нет единой точки сборки зависимостей. Каждый сервлет создает свой сервис, а каждый сервис в свою очередь сам создает конкретные DAO. Из-за этого классы жестко связаны с конкретными реализациями. Раз уж у тебя уже есть `ServletContextListener`, то вместо того, чтобы в полях сервлетов и сервисов инициализировать конкретные реализации, более правильным решением было бы использование конструкторов с последующим внедрением необходимых зависимостей (DI). В нашем случае мы создаем объекты перед стартом приложения. И с использованием конструкторов с необходимыми зависимости и помещаем эти объекты в сервлет-контекст. Примерно идея такая:
  ```java
  @WebListener
  public class AppContextListener implements ServletContextListener {

      @Override
      public void contextInitialized(ServletContextEvent sce) {
          //...

          CurrencyDao currencyDao = new CurrencyDaoImpl();
          ExchangeRateDao exchangeRateDao = new ExchangeRateDaoImpl();

          CurrencyService currencyService = new CurrencyServiceImpl(currencyDao);
          ExchangeRateService exchangeRateService = new ExchangeRateServiceImpl(exchangeRateDao, currencyDao);

          ServletContext context = sce.getServletContext();
          context.setAttribute("currencyService", currencyService);
          context.setAttribute("exchangeRateService", exchangeRateService);
      }
  }
  ```
  ```java
  @WebServlet("/exchangeRate/*")
  public class ExchangeRateServlet extends BaseServlet {

      private ExchangeRateService exchangeRateService;

      @Override
      public void init() throws ServletException {
          super.init();
          this.exchangeRateService =
                  (ExchangeRateService) getServletContext().getAttribute("exchangeRateService");

          if (exchangeRateService == null) {
              throw new IllegalStateException("...");
          }
      }
  ```
  ```java
  public class ExchangeRateServiceImpl implements ExchangeRateService {

      private final ExchangeRateDao exchangeRateDao;
      private final CurrencyDao currencyDao;

      public ExchangeRateServiceImpl(ExchangeRateDao exchangeRateDao, CurrencyDAO currencyDao) {
          this.exchangeRateDao = exchangeRateDao;
          this.currencyDao = currencyDao;
      }
  ```

- Нет централизованной обработки исключений. Почти каждый сервлет содержит одинаковые `try-catch`, а любое неожиданное исключение вообще уйдет в стандартный обработчик Tomcat и может вернуть HTML вместо ожидаемого JSON (что и происходит в некоторых случаях, см. раздел `Работоспособность твоей реализации`). Стандартная практика здесь в плане обработки ошибок — использование Servlet Filters. Запрос с использованием фильтров примерно выглядит так: клиент -> фильтры (на входе) -> servlet -> фильтры (на выходе) -> клиент. То есть до сервлета ты уже можешь выставить общие штуки, вроде content-type и прочего. А на обратном пути ты можешь использовать фильтр как обработчик исключений, он будет работать с выбрасываемыми исключениями и формировать соответствующие ответы. У dmdev есть хорошие уроки по этой теме (HTTP Servlets, уроки 49 и 50). В итоге у тебя получится примерно такой централизованный обработчик исключений:
  ```java
  @WebFilter("/*")
  public class ExceptionHandlingFilter extends HttpFilter {

      @Override
      public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
          HttpServletRequest request = (HttpServletRequest) req;
          HttpServletResponse response = (HttpServletResponse) res;

          try {
              chain.doFilter(req, res);

          } catch (NotFoundException e) {
              sendError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());

          } catch (AlreadyExistsException e) {
              sendError(response, HttpServletResponse.SC_CONFLICT, e.getMessage());

          } catch (ValidationException e) {
              sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());

          } catch (DatabaseException e) {
              sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());

          } catch (Exception e) {
              sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unknown error");
          }
      }
  }
  ```
  И все. Теперь ты просто выбрасываешь в каких-то местах программы ошибки, передаешь нужное сообщение, например `throw new NotFoundException("Currency with code XXX not found")`, после чего эта ошибка ловится на верхнем слое в фильтре и отдается соответствующий ответ с подходящим HTTP-кодом. При таком подходе сервлеты вообще не занимаются обработкой ошибок, они просто делают свою работу, а все исключения обрабатываются в одном месте


## Пакет `util`

### Класс `SqlLoader`

- По смыслу это утилитный класс, поэтому его стоит сделать `final` и закрыть публичное создание приватным конструктором

- Вместо общих `RuntimeException` здесь лучше использовать свое кастомное исключение, например тот же `DatabaseException`


## Пакет `listener`

### Класс `DbInitListener`

- Хорошо, что создание схемы выполняется один раз при старте приложения

- В классе колоссальное количество неиспользуемых импортов. В  IntelliJ IDEA можно либо горячими клавишами (Ctrl + Alt + O) удалить их, либо настроить автоудаление неиспользуемых импортов

- Сейчас listener отвечает только за инициализацию БД. Я бы расширил его ответственность до общей точки сборки приложения: создать здесь DAO, сервисы и положить их в `ServletContext`. Тогда вся конфигурация приложения будет расположена в одном месте. Выше был пример


## Пакет `dao`

- Хорошо, что используется `PreparedStatement`, а SQL вынесен из кода в отдельные файлы

- Интерфейсы DAO тоже хороший шаг. Только названия я бы сделал в единственном числе: `CurrencyDao`, `ExchangeRateDao`, `JdbcCurrencyDao`, `JdbcExchangeRateDao`. Они отвечают за работу с одной какой-либо конкретной сущностью, а не группой сущностей

- Очень важный для этого проекта недочет — DAO хранит один `Connection` в поле на все время жизни объекта:
  ```java
  private final Connection connection;
  ```
  Это плохая идея, потому что DAO используется несколькими запросами одновременно (а сервлет — это многопоточный объект), и одно соединение становится общей точкой доступа к БД. Лучше использовать connection pool, например HikariCP: каждый запрос получает свободный `Connection`, работает с ним и возвращает обратно в пул. Попробуй подключить, это просто

- Обработка SQL-ошибок сейчас ломает контракт. Например при добавлении уже существующей валюты UNIQUE-констрейнт в SQLite бросит `SQLException`, после чего код попадет сюда:
  ```java
  catch (SQLException e) {
      throw new InternalServerErrorException(...);
  }
  ```
  То есть вместо ожидаемого `409` клиент получит `500`

- Та же проблема есть с обменными курсами. Нарушение UNIQUE, CHECK и прочего сейчас сваливается в один `InternalServerErrorException`. Следует различать ожидаемые нарушения ограничений и реальные проблемы с БД. Например, дубликаты — `ConflictException`, отсутствие связанной валюты — `NotFoundException`, остальные SQL ошибки — `DatabaseException`

- Ну и да, я бы вообще не называл исключение из DAO `InternalServerErrorException`. DAO не должен знать, что наверху существует HTTP и код `500`, это выходит за рамки его ответственности. Более чистое название — `DatabaseException`, а уже фильтр (пример был выше) решит, что это HTTP 500

- Методы `create()` ведут себя неединообразно. `JdbcCurrenciesDao.create()` после INSERT заново читает сущность и возвращает ее с настоящим `id`, а `JdbcExchangeRatesDao.create()` просто возвращает входной объект. Получается, контракт одного и того же CRUD отличается в двух реализациях. Нужно выбрать один вариант


## Пакет `entity`

- Модели простые и иммутабельные, это плюс

- Вспоминая проблему с `float` — используй `BigDecimal`

- При создании новой валюты в сервисе используется искусственный `id = 0`:
  ```java
  new Currency(0, ...)
  ```
  Хотя id еще не существует и будет создан базой данных. Более корректным вариантом будет `Integer id` с `null` до сохранения или отдельный конструктор у `Currency` без `id`

- В `Currency` одновременно существуют `getName()` и `getFullName()`, которые возвращают одно и то же значение. Второй метод нигде не нужен, его стоит удалить


## Пакет `dto`

- Хорошо, что request и response DTO разделены, и наружу не протекают конкретные сущности `Currency` и `ExchangeRate`

- DTO полностью состоят из финальных полей, конструктора и геттеров. Здесь идеально подойдет `record`, например:
  ```java
  public record CurrencyResponse(
          int id,
          String code,
          String name,
          String sign
  ) {}
  ```
  Кода станет ощутимо меньше, а смысл останется тот же

- Опять же, проблема использования `float`


## Пакет `service`

### Класс `DefaultCurrencyService`

- В целом все хорошо. Единственное — маппинг сейчас написан вручную и похожий `toResponse(Currency)` повторяется еще и в `DefaultExchangeService`. Можно вынести это в отдельный `CurrencyMapper`. Можно и библиотеку для маппинга MapStruct подключить, но это уже сам смотри по желанию, для такого количества и ручной маппер ок, хотя ради практики было бы здорово


### Класс `DefaultExchangeService`

- Этот класс сейчас занимается и CRUD операциями над обменными курсами, и непосредственно вычислением конвертации. Это нарушение ответственности. Следует разделить это на `ExchangeRateService` и `ExchangeService`. Тогда ответственность классов станет понятнее

- Сам расчет курса реализован правильно, все ок. Однако весь этот расчет идет через `float`, ну, выше уже обсуждали

- В `addExchangeRate()` есть непонятное ограничение:
  ```java
  if (hasMirrorExchangeRate(baseCurrencyCode, targetCurrencyCode)) {
      throw new BadRequestException(...);
  }
  ```
  По ТЗ уникальной должна быть именно "прямая" пара `(BaseCurrencyId, TargetCurrencyId)`. Наличие `EUR — USD` не должно запрещать существование `USD — EUR`. Банально потому что обратный курс может иметь отличный от прямого курса курс, извиняюсь за масляное масло. Сейчас же приложение не даст добавить обратную пару

- Метод `hasMirrorExchangeRate()` ищет обычную ситуацию отсутствия записи через исключение:
  ```java
  try {
      exchangeRatesDao.readByCurrencyCodes(...);
      return true;
  } catch (NotFoundException e) {
      return false;
  }
  ```
  Это семантически неверное использование `try-catch`. Не нужно использовать исключения для управления потока кода, это не их задача. Здесь удобнее, чтобы DAO-метод поиска возвращал `Optional<ExchangeRate>`. Отсутствие записи для поиска — ожидаемый исход, а не исключительная ситуация

- В конце класса есть перегруженный `toResponse(ExchangeRate, float, float)`, который нигде не используется. Следует избавиться


## Пакет `servlet`

- Общая проблема всех сервлетов — повторяющиеся `try-catch`. Как уже писал выше, стоит вынести обработку исключений в общий `ExceptionHandlingFilter`

- Проверка кодов валют неединообразная. Например в GET `/currency` проверяется длина кода, а в POST `/currencies` длина вообще не проверяется. Нужен единый валидатор кода, например после нормализации проверять `[A-Z]{3}`


### Класс `AbstractJsonServlet`

- `Gson` хранится в единственном `static final` экземпляре, это хорошо

- `readJsonBody()` не используется вообще. Нужно удалить


# Вывод

- У проекта неплохой фундамент. Есть нормальное разделение на слои, которые не протекают друг в друга (за исключением HTTP исключения в DAO). Есть интерфейсы DAO и сервисов, отдельные request/response DTO, инициализация схемы на старте, работающая (хоть и не без недочетов) логика, включающая хорошо читающуюся реализацию прямого, обратного и кросс-курса

- Основными проблемами являются использование одного вечно живущего коннекшна с БД на весь DAO-экземпляр, недостаточно тщательно обработанные негативные сценарии (в самом начале можно видеть что некоторые тесты не прошли), использование `float` для денежных расчетов

- Плюс в сервлетах есть несколько мест, где отсутствующий параметр приводит к `NPE`, и чтобы совсем дожать проект не хватает централизованной валидации, единого обработчика исключений и общей точки сборки зависимостей


# Рекомендации

- Исправить все непройденные тесты

- Перевести `rate`, `amount`, `convertedAmount` на `BigDecimal` во всех слоях

- Сделать нормальную работу с соединениями через `DataSource` или HikariCP, брать connection на время конкретной DAO операции и закрывать через try-with-resources

- Вынести зависимости в `ServletContextListener`, а обработку исключений в `WebFilter`

- Вынести валидацию и нормализацию входящих данных в одно место

- Этого будет достаточно чтобы получить от ревью основную массу пользы. Остальное можешь исправлять по возможности

- После всех правок прогони все тесты через бота @currency_exchange_api_bot
