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
        if (statement.clauses[1] == null) {
            statement.clauses[1] = "ORDER BY";
        }

		/* Expand the order by clause.
		*/
        for (int idx = 0; idx < columns.size(); idx++) {
            statement.clauses[1] += " " + columns.get(idx);

            if (idx < columns.size() - 1) {
                statement.clauses[1] += ",";
            }
        }

        statement.clauses[1] += asc ? " ASC" : " DESC";
    }
}