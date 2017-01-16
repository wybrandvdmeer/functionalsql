package functionalsql.commands;

import functionalsql.Function;

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
        if (!getCompiler().getStatement().isVirginSelectClause()) {
            getCompiler().syntaxError(ERR_SELECT_ALREADY_DEFINED, getCompiler().getStatement().selectClause);
        }

        getCompiler().getStatement().selectClause = "SELECT";
        getCompiler().getStatement().groupByClause = "GROUP BY";

        /* Expand the select and group clause.
        */
        for (int idx = 0; idx < columns.size(); idx++) {
            getCompiler().getStatement().selectClause += " " + columns.get(idx);
            getCompiler().getStatement().groupByClause += " " + columns.get(idx);

            if (idx < columns.size() - 1) {
                getCompiler().getStatement().selectClause += ",";
                getCompiler().getStatement().groupByClause += ",";
            }
        }
    }
}