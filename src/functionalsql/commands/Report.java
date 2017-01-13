package functionalsql.commands;

import functionalsql.Function;
import functionalsql.FunctionalSQLCompiler;

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
    }

    /* FIND REPORT COLUMN/NUMMERICAL CONSTANT.
    */
    protected void processor1(String s) throws Exception {
        reportFunction = s;

        nextStep(); // User programmed group by columns for intstance sum( 1 , field1 , field2 ).
    }

    /* FIND THE COLUMN(S) FOR THE GROUP BY.
    */
    protected void processor2(String s) throws Exception {
        columns.add(s);
    }

    public void execute() throws Exception {
        if (statement.clauses[0] != null) {
            compiler.syntaxError(ERR_SELECT_ALREADY_DEFINED, statement.clauses[0]);
        }

        /* If anything else, then it is a program error.
        */
        assert ("SUM".equals(function) || "MAX".equals(function) || "MIN".equals(function));

        statement.clauses[0] = "SELECT";

        if (columns.size() > 0) {
            statement.clauses[2] = "GROUP BY";
        }

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

        /* Add the summation function to the select clause.
        */
        statement.clauses[0] += String.format("%s %s( %s )", columns.size() > 0 ? "," : "", function, reportFunction);
    }
}
