package ru.cjdb.sql.parser;

/**
 * @author Sergey Tselovalnikov
 * @since 29.11.14
 */
public class SqlParseException extends RuntimeException {
    public SqlParseException(String message) {
        super(message);
    }

    public SqlParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
