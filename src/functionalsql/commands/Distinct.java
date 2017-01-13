package functionalsql.commands;

import functionalsql.Function;
import functionalsql.FunctionalSQLCompiler;

import java.util.List;

import static functionalsql.FunctionalSQLCompiler.ERR_SELECT_ALREADY_DEFINED;

/**
 * Syntax:
 *  distinct-column( column, column , ... )
 *
 * Note: user can add columns and constants to the function in a random order.
 */
public class Distinct extends Function {
    public Distinct() {
        argumentsTakesTableOrColumn(1);
    }

    /* FIND COLUMN(S) FOR THE DISTINCT.
    */
    protected void processor1(String s) throws Exception {
        /* Check if argument is a table. If so, all fields of table are selected.
        Argument can also be a reference when the table was referred with the ref( table, occ ) function.
        */
        if (statement.isTable(s)) {
            s = statement.getAlias(s) + ".*";
        } else if (statement.isAlias(s)) {
            s = s + ".*";
        }

        columns.add(s);
    }

    public void execute() throws Exception {
        distinct(columns);
    }

    protected void distinct(List<String> columns) throws Exception {
        if (columns.size() == 0) {
            return;
        }

        if (statement.clauses[0] != null) {
            if (!statement.clauses[0].startsWith("SELECT DISTINCT")) {
                compiler.syntaxError(ERR_SELECT_ALREADY_DEFINED, statement.clauses[0]);
            }

            statement.clauses[0] += ",";
        } else {
            statement.clauses[0] = "SELECT DISTINCT";
        }

        /* Expand the select clause.
        */
        for (int idx = 0; idx < columns.size(); idx++) {
            statement.clauses[0] += " " + columns.get(idx);

            if (idx < columns.size() - 1) {
                statement.clauses[0] += ",";
            }
        }
    }
}
