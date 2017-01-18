package functionalsql;

import static functionalsql.FunctionalSQLCompiler.ERR_DEFAULT_RELATION_HAS_NO_EQUAL_COLUMNS;

public class Relation {
    final private String table1, table2, column1, column2;

    public Relation(String column1, String column2) throws Exception {
        this(null, column1, null, column2);
    }

    public Relation(String table1, String column1, String table2, String column2) throws Exception {
        this.table1 = table1 != null ? table1 : "";
        this.column1 = column1;
        this.table2 = table2 != null ? table2 : "";
        this.column2 = column2;

        if (table1 == null && table2 == null && !column1.equals(column2)) {
            throw new Exception(ERR_DEFAULT_RELATION_HAS_NO_EQUAL_COLUMNS);
        }
    }

    public boolean isDefault() {
        return table1 == null && table2 == null && column1.equals(column2);
    }

    public boolean matches(String table1, String column1, String table2) {
        if (toString().indexOf(table1) >= 0) {
            /* Mapping could be correct: check it precisly.
            Note: check both ways because we have no knowlegde of how the mapping was added to the compiler
            E.g. (table1|column1 , table2|column2) OR (table2|column2 , table1|column1)
            */
            if ((table1.equals(this.table1) && table2.equals(this.table2)) || (table1.equals(this.table2) && table2.equals(this.table1))) {
                /* User can have only the drive column programmed and not the join column. If so, this column should be mentioned
                in the relation.
                */
                if (column1 != null) {
                    if (table1.equals(this.table1) && column1.equals(this.column1)) {
                        return true;
                    }

                    if (table1.equals(this.table2) && column1.equals(this.column2)) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }

        return false;
    }

    public String getColumn(String table) {
        /* Mapping could be the default relation.
        */
        if (table1.length() == 0) {
            return column1;
        }

        if (table1.equals(table)) {
            return column1;
        }

        if (table2.equals(table)) {
            return column2;
        }

        return null;
    }

    public boolean defaultRelationMatches(String column) {
        return isDefault() && column1.equals(column);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof Relation) {
            Relation c = (Relation) other;
            return table1.equals(c.table1) && column1.equals(c.column1) && table2.equals(c.table2) && column2.equals(c.column2);
        }

        return false;
    }

    public int hashCode() {
        return table1.hashCode() + column1.hashCode() + table2.hashCode() + column2.hashCode();
    }

    public String toString() {
        return table1 + table2;
    }
}