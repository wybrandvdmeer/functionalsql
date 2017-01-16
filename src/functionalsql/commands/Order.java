package functionalsql.commands;

import functionalsql.Function;

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
        if (getCompiler().getStatement().orderByClause == null) {
            getCompiler().getStatement().orderByClause = "ORDER BY";
        }

        /* Expand the order by clause.
        */
        for (int idx = 0; idx < columns.size(); idx++) {
            getCompiler().getStatement().orderByClause += " " + columns.get(idx);

            if (idx < columns.size() - 1) {
                getCompiler().getStatement().orderByClause += ",";
            }
        }

        getCompiler().getStatement().orderByClause += asc ? " ASC" : " DESC";
    }
}
