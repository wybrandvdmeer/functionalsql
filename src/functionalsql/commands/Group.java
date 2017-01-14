package functionalsql.commands;

import functionalsql.Function;
import functionalsql.FunctionalSQLCompiler;
import functionalsql.Statement;

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
        if (statement.selectClause != Statement.SELECT_ALL_COLUMNS_CLAUSE) {
            compiler.syntaxError(ERR_SELECT_ALREADY_DEFINED, statement.selectClause);
        }

        statement.selectClause = "SELECT";
        statement.groupByClause = "GROUP BY";

            /* Expand the select and group clause.
            */
        for (int idx = 0; idx < columns.size(); idx++) {
            statement.selectClause += " " + columns.get(idx);
            statement.groupByClause += " " + columns.get(idx);

            if (idx < columns.size() - 1) {
                statement.selectClause += ",";
                statement.groupByClause += ",";
            }
        }
    }
}