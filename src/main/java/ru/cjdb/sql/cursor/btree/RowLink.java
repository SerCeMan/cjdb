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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RowLink rowLink = (RowLink) o;
        return pageId == rowLink.pageId && rowId == rowLink.rowId;
    }

    @Override
    public int hashCode() {
        int result = pageId;
        result = 31 * result + rowId;
        return result;

    }

    @Override
    public String toString() {
        return "RowLink{" +
                "pageId=" + pageId +
                ", rowId=" + rowId +
                '}';
    }
}
