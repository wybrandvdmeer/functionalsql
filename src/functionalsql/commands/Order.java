package functionalsql.commands;

import functionalsql.Function;
import functionalsql.consumer.TokenConsumer;

/**
 * Syntax: asc( fielda, table.fieldb , ... )
 *
 */
public class Order extends Function {
    private boolean asc = true;

    public Order() {
        argumentsTakesTableOrColumn(1);
        build(1, new TokenConsumer(this, token -> columns.add(token)).mandatory());
    }

    protected void setDesc() {
        asc = false;
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
