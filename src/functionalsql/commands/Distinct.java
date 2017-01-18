package functionalsql.commands;

import functionalsql.Function;
import functionalsql.consumer.TokenConsumer;

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

        /* Check if argument is a table. If so, all fields of table are selected.
        Argument can also be a reference when the table was referred with the ref( table, occ ) function.
        */
        build(1, new TokenConsumer(this, token -> {
            if (getCompiler().getStatement().isTable(token)) {
                token = getCompiler().getStatement().getAlias(token) + ".*";
            } else if (getCompiler().getStatement().isAlias(token)) {
                token = token + ".*";
            }

            columns.add(token);
        }).mandatory());
    }

    public void execute() throws Exception {
        if (columns.size() == 0) {
            return;
        }

        if(!getCompiler().getStatement().isVirginSelectClause()) {
            getCompiler().syntaxError(ERR_SELECT_ALREADY_DEFINED, getCompiler().getStatement().selectClause);
        }

        getCompiler().getStatement().selectClause = "SELECT DISTINCT";

        /* Expand the select clause.
        */
        for (int idx = 0; idx < columns.size(); idx++) {
            getCompiler().getStatement().selectClause += " " + columns.get(idx);

            if (idx < columns.size() - 1) {
                getCompiler().getStatement().selectClause += ",";
            }
        }
    }
}
