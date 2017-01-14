package functionalsql.commands;

import functionalsql.Function;
import functionalsql.FunctionalSQLCompiler;

/**
 * Syntax: asc( fielda, table.fieldb , ... )
 *
 */
public class Order extends Function {
    private boolean asc = true;

    public Order() {
        argumentsTakesTableOrColumn(1);
    }

    protected void setDesc() {
        asc = false;
    }

    /* FIND COLUMN(S) FOR THE ORDER BY.
    */
    protected void processor1(String s) throws Exception {
        columns.add(s);
    }

    public void execute() throws Exception {
        if (statement.orderByClause == null) {
            statement.orderByClause = "ORDER BY";
        }

        /* Expand the order by clause.
        */
        for (int idx = 0; idx < columns.size(); idx++) {
            statement.orderByClause += " " + columns.get(idx);

            if (idx < columns.size() - 1) {
                statement.orderByClause += ",";
            }
        }

        statement.orderByClause += asc ? " ASC" : " DESC";
    }
}
