# 📁 CLOUD FILE STORAGE

#### **Backend Core & Framework**

![Java 21](https://img.shields.io/badge/Java_21-007396?style=flat-square&logo=openjdk&logoColor=white)
![Spring Framework 7.0](https://img.shields.io/badge/Spring_Framework_7.0-6DB33F?style=flat-square&logo=spring&logoColor=white)
![Jakarta Servlet 6.1](https://img.shields.io/badge/Jakarta_Servlet_6.1-2C2255?style=flat-square&logo=jakartaee&logoColor=white)
![Tomcat 11](https://img.shields.io/badge/Tomcat_11-F8DC75?style=flat-square&logo=apachetomcat&logoColor=black)

#### **Data & Persistence**

![Spring Data JPA 4.1](https://img.shields.io/badge/Spring_Data_JPA_4.1-59A637?style=flat-square&logo=spring&logoColor=white)
![Hibernate 7.4](https://img.shields.io/badge/Hibernate_7.4-59666C?style=flat-square&logo=hibernate&logoColor=white)
![MySQL 9.7](https://img.shields.io/badge/MySQL_9.7-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Redis 8.8](https://img.shields.io/badge/Redis_8.8-DC382D?style=flat-square&logo=redis&logoColor=white)
![MinIO 9.0](https://img.shields.io/badge/MinIO_9.0-C72C48?style=flat-square&logo=minio&logoColor=white)
![Flyway 13.5](https://img.shields.io/badge/Flyway_13.5-D91F26?style=flat-square&logo=flyway&logoColor=white)

#### **Tools & Testing**

![Docker 29.6](https://img.shields.io/badge/Docker_29.6-2496ED?style=flat-square&logo=docker&logoColor=white)
![Gradle 8.8](https://img.shields.io/badge/Gradle_8.8-02303A?style=flat-square&logo=gradle&logoColor=white)
![Springdoc OpenAPI 3.1](https://img.shields.io/badge/Springdoc_OpenAPI_3.1-6BA539?style=flat-square&logo=openapiinitiative&logoColor=white)
![Lombok 1.18](https://img.shields.io/badge/Lombok_1.18-9E1111?style=flat-square&logo=lombok&logoColor=white)
![MapStruct 1.6](https://img.shields.io/badge/MapStruct_1.6-F8981D?style=flat-square&logo=java&logoColor=white)
![JUnit 6.1](https://img.shields.io/badge/JUnit_6.1-25A162?style=flat-square&logo=junit5&logoColor=white)
![Testcontainers 2.0](https://img.shields.io/badge/Testcontainers_2.0-0081CB?style=flat-square&logo=testcontainers&logoColor=white)

**Cloud file storage** — веб-приложение для реализации персонального хранения файлов на удалённом сервере. Через
него можно сохранять, скачивать, переименовывать, перемещать, удалять, искать ресурс, как в классическом диспетчере
файлов. Проект написан с целью обучения работы со Spring Framework для оптимизации проекта, MinIO Java SDK для практики
симуляции файловой структуры, Docker для оптимизации работы с инфраструктурой и прочее.

---

## 🚀 Быстрый старт

### Предварительные требования

Перед запуском убедитесь, что у вас установлены:

* **Java 21** (или выше)
* **Tomcat 11** (сервер приложений)
* **Docker**

### Пошаговая инструкция по локальному запуску

В проекте настроены миграции БД и инициализация бакета minIO хранилища при запуске.

1. Склонируйте текущий репозиторий


2. Настройте переменные окружения по приложенному к репозиторию `application.properties.example`: по умолчанию
конфигурация приложения ожидает в папке src/main/resources/ файл с названием `application-dev.properties` и для тестового
окружения `application-test.properties`


3. Запустите через Docker базу данных и хранилища

```Bash
docker compose up -d
```

4. Соберите WAR-архив приложения через **wrapper** без проверки тестов

```Bash
   ./gradlew clean build -x test
```

5. Скопируйте полученный файл `cloudfilestorage-1.0.war` в папку `webapps/` вашего установленного Tomcat 11. Запустите
   Tomcat из его директории

```Bash
   ./startup.bat
```

После того как веб-контейнер распакует архив, приложение будет доступно в браузере по адресу:
> 👉 http://localhost:8080/cloudfilestorage-1.0/

Также будет доступен Swagger OpenAPI
> 👉 http://localhost:8080/cloudfilestorage-1.0/api/swagger-ui/index.html#/

### Приятного пользования!

---

## 🔄 Функционал приложения

Приложение реализовано в REST API архитектуре. Все эндпоинты работают по общему `/api` префиксу.

**Эндпоинты по адресу `/auth`**

- POST `/sign-up`: позволяет зарегистрироваться пользователю и получить актуальную сессию
- POST `/sign-in`: позволяет авторизоваться пользователю и получить актуальную сессию
- POST `/sign-out`: позволяет преждевременно закончить сессию пользователю

**Эндпоинты по адресу `/directory`**

- Текущий GET: позволяет получить информацию о ресурсах в директории по указанному пути
- Текущий POST: позволяет создать новую директорию по указанному пути

**Эндпоинты по адресу `/resource`**

- Текущий GET: позволяет получить информацию о ресурсе по указанному пути
- Текущий POST: позволяет сохранить ресурс в хранилище по указанному пути
- Текущий DELETE: позволяет удалить ресурс из хранилища по указанному пути
- GET `/download`: позволяет скачать ресурс из хранилища в исходном формате, либо в ZIP-архиве,
если это директория
- POST `/move`: позволяет переименовать/переместить ресурс внутри хранилища по указанному пути
- GET `/search`: позволяет найти ресурс по совпадению его имени с указанной подстрокой

**Эндпоинты по адресу `/user`**

- GET `/me`: позволяет получить актуальную информацию о текущем пользователе

---

## ⚙️ Технологический стек

* **Сборка**: Gradle
* **Backend:** Java 21, Spring Framework 7.0, Jakarta Servlet API 6.1, Apache Tomcat 11
* **Data & Persistence:** Spring Data JPA 4.1, MySQL 9.7, Hibernate 7.4, HikariCP 7.1, Redis 8.8, MinIO 9.0,
Flyway 13.5
* **Утилиты:** Springdoc OpenAPI, Lombok, MapStruct, Jackson Databind, Logback + SLF4J, JBCrypt
* **Tests**: JUnit5 + TestContainers
* **Frontend:** В проект интегрирован базовый веб-интерфейс для взаимодействия с REST API
[[ссылка на первоисточник](https://github.com/zhukovsd/cloud-storage-frontend/)]

---

## 📊 Структура Базы Данных

В текущем проекте в роли базы данных выбрана MySQL с подключенным Flyway для контроля миграций схемы таблицы

### Таблица `USERS` (пользователи)

| Столбец      | Тип данных  | Ограничения        | Описание                                |
|:-------------|:------------|:-------------------|:----------------------------------------|
| **ID**       | INT         | PK, Auto-Increment | Уникальный идентификатор пользователя   |
| **NAME**     | VARCHAR(40) | UNIQUE, NOT NULL   | Уникальное имя или никнейм пользователя |
| **PASSWORD** | VARCHAR(60) | NOT NULL           | Пароль пользователя                     |

> 🛡️ **Constraints:** 
> 1. для имени пользователя установлено ограничение от 5 до 40 символов 
> 2. для пароля пользователя установлено значение в 60 символов, чтобы в БД допускался только сгенерированный хэш

---

## 🧪 Тестирование

Для проекта реализовано интеграционное тестирование с использованием TestContainers для проверок, 
максимально приближенных к prod-среде. В рамках данных тестов были проверены сценарии работы сервисного слоя приложения.
Проверены как положительные, так и негативные сценарии.

### Тесты для сервиса аутентификации и сессий (AuthenticationService)

- Успешная регистрация пользователя с записью в mySQL и redis
- Исключение UserAlreadyExistsException при попытке зарегистрировать существующего пользователя
- Успешный выход из сессии и повторная авторизация с созданием новой актуальной сессии
- Выброс AuthenticationException при вводе некорректного пароля
- Успешный поиск и получение профиля авторизованного пользователя по ключу сессии

### Тесты для сервиса файлов и директорий (ResourceService)

- Успешное создание директории с проверкой на корректность пользовательской директории
- Успешная загрузка файла в хранилище
- Получение информации о существующем файле/директории
- Успешное получение информации о содержимых ресурсах в целевой директории
- Выброс InvalidInputException при попытке удаления пользовательской директории
- Успешное переименование/перемещение файла/директории в хранилище
- Успешное удаление файла/директории из хранилища
- Исключение ResourceNotFoundException при попытке создания ресурса в несуществующем пути
- Выброс ResourceAlreadyExistsException при повторном создании уже существующего ресурса
- Скачивание файлов: получение массива байтов и оригинального имени файла
- Глобальный поиск ресурсов (файлов и папок) по подстроке во всех директориях пользователя

---

## 📋 Логирование

В приложении настроена система логирования на базе **SLF4J** и его реализации **Logback**. Сбор логов ведется
параллельно по двум каналам, консольная и файловая, с базовым уровнем фильтрации `INFO`.

### Консольный вывод (`ConsoleAppender`)
* Логи дублируются в стандартный поток вывода (`STDOUT`) контейнера сервлетов Tomcat.

### Запись в файл (`FileAppender`)
* Все события системы автоматически сохраняются в текстовый файл по пути: logs/app.log
> ⚠️ **Важное предупреждение:** при каждом перезапуске контекста приложения или рестарте сервера Tomcat старый файл
логов app.log полностью очищается и перезаписывается с нуля.