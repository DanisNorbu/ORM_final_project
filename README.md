# ORM_Learn Platform

Учебная платформа на **Spring Boot + Hibernate/JPA + PostgreSQL**, с полноценной ORM-моделью (15 сущностей), сервисным
слоем, REST API, валидацией/ошибками и тестами.

Ключевая цель: покрыть критерии ТЗ — модель данных, CRUD, бизнес-сценарии (курсы, запись, ДЗ, тесты), демонстрация Lazy
Loading и интеграционные тесты, которые запускаются **на H2 без Docker**.

## Стек

- Java 17+
- Spring Boot (Web, Data JPA, Validation)
- Hibernate/JPA
- PostgreSQL (runtime)
- H2 (tests)
- JUnit 5, Mockito, MockMvc

## Модель данных (15 сущностей)

Реализованы сущности и связи (1-1 / 1-M / M-M):
`User, Profile, Category, Course, Enrollment, Module, Lesson, Assignment, Submission, Quiz, Question, AnswerOption, QuizSubmission, CourseReview, Tag` +
join-table `course_tag`.

По умолчанию `open-in-view=false`, коллекции связей — **LAZY** (см. тест на `LazyInitializationException`).

## Запуск тестов (без Docker, H2)

```bash
mvn test
```

Тесты используют `src/test/resources/application-test.yml` (`ddl-auto=create-drop`).

## Запуск приложения (PostgreSQL)

Параметры подключения задаются через env-переменные (есть дефолты):

- `DB_HOST` (default: `localhost`)
- `DB_PORT` (default: `5432`)
- `DB_NAME` (default: `learn_platform`)
- `DB_USER` (default: `postgres`)
- `DB_PASSWORD` (default: `postgres`)

```bash
mvn spring-boot:run
```

### Docker Compose (опционально)

```bash
docker compose up --build
```

## Demo-данные (профиль `dev`)

При запуске с профилем `dev` включён `DataSeeder`, который создаёт:

- преподавателя `teacher@example.com`
- студентов `alice@example.com`, `bob@example.com`
- категорию, курс со структурой (модули/уроки/задание), теги, квиз
- записи на курс, пример сдачи ДЗ, результат квиза, отзыв

## REST API

Формат — JSON. Ошибки возвращаются единообразно через `@RestControllerAdvice`.

### Users

- `POST /api/users` — создать
- `GET /api/users` — список
- `GET /api/users/{id}` — получить
- `PATCH /api/users/{id}` — обновить
- `DELETE /api/users/{id}` — удалить (если не используется в обучении)

### Profile (1-1 с User)

- `PUT /api/users/{userId}/profile` — создать/обновить профиль
- `GET /api/users/{userId}/profile` — получить профиль
- `DELETE /api/users/{userId}/profile` — удалить профиль

### Categories

- `POST /api/categories`
- `GET /api/categories`
- `GET /api/categories/{id}`
- `PATCH /api/categories/{id}`
- `DELETE /api/categories/{id}`

### Tags

- `POST /api/tags`
- `GET /api/tags`
- `GET /api/tags/{id}`
- `PATCH /api/tags/{id}`
- `DELETE /api/tags/{id}`

### Courses + структура (modules/lessons)

- `POST /api/courses` — создать курс (categoryId, teacherId, tagNames)
- `GET /api/courses` — список
- `GET /api/courses/{id}` — детали (модули/уроки + теги)
- `PATCH /api/courses/{id}` — обновить
- `DELETE /api/courses/{id}` — удалить (если нет enrollments/submissions/quiz results/reviews)

Структура:

- `POST /api/courses/{courseId}/modules` — добавить модуль
- `POST /api/courses/modules/{moduleId}/lessons` — добавить урок

Доп. CRUD:

- `GET /api/modules/{id}` / `PATCH /api/modules/{id}` / `DELETE /api/modules/{id}`
- `GET /api/lessons/{id}` / `PATCH /api/lessons/{id}` / `DELETE /api/lessons/{id}`

### Enrollment (запись на курс)

- `POST /api/courses/{courseId}/enroll?userId=...` — записать
- `DELETE /api/courses/{courseId}/enroll?userId=...` — отписать
- `GET /api/courses/{courseId}/enrollments` — список записей по курсу
- `GET /api/users/{userId}/enrollments` — список записей по студенту

### Assignments / Submissions (ДЗ)

- `POST /api/lessons/{lessonId}/assignments` — создать задание
- `GET /api/assignments/{id}` — получить
- `PATCH /api/assignments/{id}` — обновить
- `DELETE /api/assignments/{id}` — удалить (если нет submissions)

Решения:

- `POST /api/assignments/{assignmentId}/submissions` — отправить решение (1 на student+assignment)
- `GET /api/assignments/{assignmentId}/submissions` — список по заданию
- `GET /api/users/{studentId}/submissions` — список по студенту
- `PATCH /api/submissions/{submissionId}/grade` — оценить
- `DELETE /api/submissions/{submissionId}` — удалить

### Quiz

- `POST /api/quizzes` — создать квиз с вопросами/вариантами
- `GET /api/quizzes/{id}` — получить
- `POST /api/quizzes/{id}/take` — пройти тест (ответы) → сохраняется `QuizSubmission`
- `GET /api/quizzes/{id}/results` — результаты по квизу

### Reviews

- `POST /api/courses/{courseId}/reviews` — оставить отзыв (1 на student+course)
- `GET /api/courses/{courseId}/reviews` — отзывы по курсу
- `GET /api/users/{studentId}/reviews` — отзывы студента
- `PATCH /api/reviews/{reviewId}` — обновить
- `DELETE /api/reviews/{reviewId}` — удалить

## Lazy Loading

- `spring.jpa.open-in-view=false`
- Коллекции `@OneToMany/@ManyToMany` — **LAZY**
- Есть интеграционный тест, который демонстрирует `LazyInitializationException` при доступе к коллекции вне транзакции.
- Также показан вариант решения через `@EntityGraph`.

## Автоматизация

- `Dockerfile` + `docker-compose.yml`
- GitHub Actions workflow (`.github/workflows/ci.yml`) — сборка и `mvn test`.
