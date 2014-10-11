package ru.cjdb.sql.types;

/**
 * Тип колонки в базе
 *
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public interface Type {

    String name();

    int bytes();
}
