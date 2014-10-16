CjDB
====
[![Build Status](https://travis-ci.org/SerCeMan/cjdb.svg?branch=master)](https://travis-ci.org/SerCeMan/cjdb)

Cool Java Database

Настройка IDE
---
* IntelliJ IDEA: 
 * Cделать import from Gradle 
 * Для корректной работы Dagger из IDEA также необходимо включить Annotation Processing в настройках компилятора.


Конфигурирование проекта
---
В classpath должен лежать файлик db.properties, в котором параметр db.url указывает на папку до базы (пример - файл db.properties.template в проекте). Для тестов аналогично, поэтому рекоммендуется создавать файлик рядом с template, (он добавлен в .gitignore, ваша конфигурация не закоммитится) 

Сборка
---
```bash
./gradlew build
```

Запуск тестов
---
```bash
./gradlew check
```
