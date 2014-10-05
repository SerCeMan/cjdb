package sql.types;

/**
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public enum Type {
    INT(4),
    VARCHAR(20);

    final int byteCount;

    Type(int byteCount) {
        this.byteCount = byteCount;
    }

    public int byteCount() {
        return byteCount;
    }
}
