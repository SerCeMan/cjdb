package ru.cjdb.scheme.types;

import java.nio.ByteBuffer;

/**
 * Тип колонки в базе
 *
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public interface Type {

    String name();

    int bytes();

    /**
     * Записывает в буфер объект в виде массива байт.
     *
     * @throws java.lang.IllegalArgumentException
     *      Если пришел объект неправильного типа
     */
    void write(ByteBuffer buffer, Object o);

    /**
     * Читает из буфера объект
     */
    Object read(ByteBuffer buffer);

    Object valueOf(String value);
}
