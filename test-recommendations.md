# Тестирование системы рекомендаций

## Запуск приложения

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=default"
```

## Тестовые HTTP запросы

После запуска приложения можно протестировать API рекомендаций:

### 1. Создание пользователей

```bash
# Пользователь 1
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user1@example.com",
    "login": "user1",
    "name": "User One",
    "birthday": "1990-01-01"
  }'

# Пользователь 2
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user2@example.com",
    "login": "user2", 
    "name": "User Two",
    "birthday": "1991-02-02"
  }'

# Пользователь 3
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user3@example.com",
    "login": "user3",
    "name": "User Three", 
    "birthday": "1992-03-03"
  }'
```

### 2. Создание фильмов

```bash
# Фильм 1
curl -X POST http://localhost:8080/films \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Film One",
    "description": "First test film",
    "releaseDate": "2020-01-01",
    "duration": 120,
    "mpa": {"id": 1}
  }'

# Фильм 2
curl -X POST http://localhost:8080/films \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Film Two", 
    "description": "Second test film",
    "releaseDate": "2021-01-01",
    "duration": 130,
    "mpa": {"id": 1}
  }'

# Фильм 3
curl -X POST http://localhost:8080/films \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Film Three",
    "description": "Third test film", 
    "releaseDate": "2022-01-01",
    "duration": 140,
    "mpa": {"id": 1}
  }'
```

### 3. Добавление лайков

```bash
# Пользователь 1 лайкает фильмы 1 и 2
curl -X PUT http://localhost:8080/films/1/like/1
curl -X PUT http://localhost:8080/films/2/like/1

# Пользователь 2 лайкает фильмы 1, 2 и 3 (наиболее похож на пользователя 1)
curl -X PUT http://localhost:8080/films/1/like/2
curl -X PUT http://localhost:8080/films/2/like/2
curl -X PUT http://localhost:8080/films/3/like/2

# Пользователь 3 лайкает только фильм 3
curl -X PUT http://localhost:8080/films/3/like/3
```

### 4. Получение рекомендаций

```bash
# Рекомендации для пользователя 1
# Должен получить фильм 3 (так как пользователь 2 наиболее похож и лайкнул фильм 3)
curl http://localhost:8080/users/1/recommendations

# Рекомендации для пользователя 2 
# Должен получить пустой список (уже лайкнул все фильмы)
curl http://localhost:8080/users/2/recommendations

# Рекомендации для пользователя 3
# Должен получить пустой список (нет пользователей с пересечениями)
curl http://localhost:8080/users/3/recommendations
```

### 5. Проверка работы алгоритма

Добавим еще сценариев:

```bash
# Создаем пользователя 4
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user4@example.com",
    "login": "user4",
    "name": "User Four",
    "birthday": "1993-04-04"
  }'

# Пользователь 4 лайкает фильм 1 (как пользователи 1 и 2)
curl -X PUT http://localhost:8080/films/1/like/4

# Теперь рекомендации для пользователя 4 должны включать фильмы 2 и 3
curl http://localhost:8080/users/4/recommendations
```

## Ожидаемые результаты

1. **Пользователь 1**: Получит фильм 3 в рекомендациях
2. **Пользователь 2**: Пустой список (лайкнул все фильмы)
3. **Пользователь 3**: Пустой список (нет пересечений)
4. **Пользователь 4**: Получит фильмы 2 и 3 в рекомендациях

## Альтернативный способ тестирования

Если curl недоступен, можно использовать любой HTTP клиент (Postman, Insomnia) или браузер для GET запросов.

## Логи

При запуске приложения в логах будут видны сообщения о работе алгоритма рекомендаций:
- Генерация рекомендаций для пользователя
- Поиск наиболее похожего пользователя
- Количество найденных рекомендаций
