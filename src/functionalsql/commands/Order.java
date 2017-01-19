package functionalsql.commands;

import functionalsql.Function;
import functionalsql.consumer.TokenConsumer;

import static functionalsql.FunctionalSQLCompiler.ERR_ORDER_BY_CLAUSE_ALREADY_DEFINED;

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

        if (getCompiler().getStatement().getOrderByClause() != null) {
            getCompiler().syntaxError(ERR_ORDER_BY_CLAUSE_ALREADY_DEFINED);
        }

        String orderByClause = "ORDER BY";

        for (int idx = 0; idx < columns.size(); idx++) {
            orderByClause += " " + columns.get(idx);

            if (idx < columns.size() - 1) {
                orderByClause += ",";
            }
        }

        getCompiler().getStatement().setOrderByClause( orderByClause + (asc ? " ASC" : " DESC"));
    }
}
