package functionalsql.commands;

import functionalsql.Function;
import functionalsql.FunctionalSQLCompiler;

/**
 * Syntax: desc( fielda, table.fieldb , ... )
 *
 */
public class Desc extends Order {
    public Desc() {
        setDesc();
    }

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
    public static class FilterDate extends Function {
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
                statement.filterClauses.add(String.format("%s %s '%s'", column, secondValueOrOperator, value));
            } else {
                statement.filterClauses.add(String.format("%s >= '%s'", column, value));
                statement.filterClauses.add(String.format("%s < '%s'", column, secondValueOrOperator));
            }
        }
    }
}
