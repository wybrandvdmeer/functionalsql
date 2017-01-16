package functionalsql.commands;

import functionalsql.Function;

/**
 * Syntax:
 *
 *    a filterdate( column, 20120101 )
 *    a filterdate( column, 20120101 , >= )
 *    a filterdate( column, 20120101 , <= )
 *    a filterdate( column, 20120101 , '>' )
 *    a filterdate( column, 20120101 , '<' )
 *    a filterdate( column, 20120101, 20140101 )
 *
 */
public class FilterDate extends Function {
    private String secondValueOrOperator = "=";

    public FilterDate() {
        argumentsTakesTableOrColumn(1);
    }

    /* Find column.
    */
    protected void processor1(String s) throws Exception {
        column = s;
        nextMandatoryStep();
    }

    /* Find first value. */
    protected void processor2(String s) throws Exception {
        value = s;
        nextStep();
    }

    /* Find second value (between) or operator if provided.
    */
    protected void processor3(String s) throws Exception {
        secondValueOrOperator = s;
        finished();
    }

    public void execute() throws Exception {
        if (secondValueOrOperator == null) {
            secondValueOrOperator = "=";
        }

        if ("=".equals(secondValueOrOperator) ||
                "<=".equals(secondValueOrOperator) ||
                ">=".equals(secondValueOrOperator) ||
                "<".equals(secondValueOrOperator) ||
                ">".equals(secondValueOrOperator)) {
            getCompiler().getStatement().filterClauses.add(String.format("%s %s '%s'", column, secondValueOrOperator, value));
        } else {
            getCompiler().getStatement().filterClauses.add(String.format("%s >= '%s'", column, value));
            getCompiler().getStatement().filterClauses.add(String.format("%s < '%s'", column, secondValueOrOperator));
        }
    }
}
