package functionalsql.commands;

import functionalsql.Function;

import static functionalsql.FunctionalSQLCompiler.ERR_SELECT_ALREADY_DEFINED;

/**
 * Syntax: print( column1 , column2 , ... )
 */
public class Print extends Function {
    public Print() {
        argumentsTakesTableOrColumn(1);
    }

    /* FIND COLUMN WHICH TO PRINT.
    */
    protected void processor1(String s) throws Exception {
        columns.add(s);
    }

    public void execute() throws Exception {
        if (!getCompiler().getStatement().isVirginSelectClause()) {
            getCompiler().syntaxError(ERR_SELECT_ALREADY_DEFINED, getCompiler().getStatement().selectClause);
        }

        getCompiler().getStatement().selectClause = "SELECT";

        /* Expand the select clause.
        */
        for (int idx = 0; idx < columns.size(); idx++) {
            String column = columns.get(idx);

            /* Check if argument is a table. If so, all fields of table are selected.
            Argument can also be a reference when the table was referred with the ref( table, occ ) function.
            */
            if (getCompiler().getStatement().isTable(column)) {
                column = getCompiler().getStatement().getAlias(column) + ".*";
            } else if (getCompiler().getStatement().isAlias(column)) {
                column = column + ".*";
            }

            getCompiler().getStatement().selectClause += " " + column;

            if (idx < columns.size() - 1) {
                getCompiler().getStatement().selectClause += ",";
            }
        }
    }
}
