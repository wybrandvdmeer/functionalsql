package functionalsql.commands;

import functionalsql.Function;
import functionalsql.FunctionalSQLCompiler;

import static functionalsql.FunctionalSQLCompiler.ERR_SELECT_ALREADY_DEFINED;

/**
 * Syntax: group( fielda, table.fieldb , ... )
 *
 */
public class Group extends Function {
    public Group() {
        argumentsTakesTableOrColumn(1);
    }

    /* FIND COLUMN(S) FOR THE GROUP.
    */
    protected void processor1(String s) throws Exception {
        columns.add(s);
    }

    public void execute() throws Exception {
        if (statement.clauses[0] != null) {
            compiler.syntaxError(ERR_SELECT_ALREADY_DEFINED, statement.clauses[0]);
        }

        statement.clauses[0] = "SELECT";
        statement.clauses[2] = "GROUP BY";

            /* Expand the select and group clause.
            */
        for (int idx = 0; idx < columns.size(); idx++) {
            statement.clauses[0] += " " + columns.get(idx);
            statement.clauses[2] += " " + columns.get(idx);

            if (idx < columns.size() - 1) {
                statement.clauses[0] += ",";
                statement.clauses[2] += ",";
            }
        }
    }
}