package ru.cjdb.sql.cursor.btree;

/**
 * @author Sergey Tselovalnikov
 * @since 16.01.15
 */
public class RowLink {
    private final int pageId;
    private final int rowId;

    public RowLink(int pageId, int rowId) {
        this.pageId = pageId;
        this.rowId = rowId;
    }

    public int getPageId() {
        return pageId;
    }

    public int getRowId() {
        return rowId;
    }
}
