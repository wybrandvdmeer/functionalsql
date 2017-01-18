package functionalsql.commands;

import functionalsql.Function;
import functionalsql.consumer.TokenConsumer;

import static functionalsql.FunctionalSQLCompiler.ERR_SELECT_ALREADY_DEFINED;

/**
 * Syntax: (sum|max|min)( summation_column | nummerical_constant , field1 , table.field2 , ... )
 *
 */
public class Report extends Function {
    private final String function;

    private String reportFunction = null;

    public Report(String function) {
        this.function = function;

        argumentsTakesTableOrColumn(1);
        argumentsTakesTableOrColumn(2);

        build(1, new TokenConsumer(this, token -> reportFunction = token).singleValue().mandatory());
        build(2, new TokenConsumer(this, token -> columns.add(token)));
    }

    public void execute() throws Exception {
        if (!getCompiler().getStatement().isVirginSelectClause()) {
            getCompiler().syntaxError(ERR_SELECT_ALREADY_DEFINED, getCompiler().getStatement().selectClause);
        }

        /* If anything else, then it is a program error.
        */
        assert ("SUM".equals(function) || "MAX".equals(function) || "MIN".equals(function));

        getCompiler().getStatement().selectClause = "SELECT";

        if (columns.size() > 0) {
            getCompiler().getStatement().groupByClause = "GROUP BY";
        }

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

        /* Add the summation function to the select clause.
        */
        getCompiler().getStatement().selectClause += String.format("%s %s( %s )", columns.size() > 0 ? "," : "", function, reportFunction);
    }
}
