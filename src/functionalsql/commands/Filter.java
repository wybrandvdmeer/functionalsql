package functionalsql.commands;

import functionalsql.Function;
import functionalsql.FunctionalSQLCompiler;

import java.util.List;

import static functionalsql.FunctionalSQLCompiler.*;

/**
 * Syntax:
 * filter(column, value1, value2, ...)
 * filter(column, operator, value)
 *
 * Note: if you want to filter on for instance '<', you have to put the '<' sign between quotes.
 *       Thus: filter(column, '<')
 *       When leaving out the quotes, then it is seen as an operator.
 */
public class Filter extends Function {
    private boolean inclusive = true;

    public Filter() {
        argumentsTakesTableOrColumn(1);
    }

    public Filter(boolean inclusive) {
        this();
        this.inclusive = inclusive;
    }

    /* Find column on which to filter.
    */
    protected void processor1(String s) throws Exception {
        column = s;
        nextMandatoryStep();
    }

    /* Find values/operator on which to filter.
    */
    protected void processor2(String s) throws Exception {
        values.add(s);
    }

    public void execute() throws Exception {
        filter(column, values, inclusive);
    }

    protected void filter(String column, List<String> values, boolean inclusive) throws Exception {
        if(values.size() >= 1 && !compiler.isQuoted(values.get(0)) &&
                ( values.get(0).equals("<") ||
                        values.get(0).equals(">") ||
                        values.get(0).equals("<=") ||
                        values.get(0).equals(">=") ||
                        values.get(0).equals("==") ||
                        values.get(0).equals("!="))) {
            filterOnOperator(column, values);
        } else {
            filterOnValues(column, values, inclusive);
        }
    }

    protected void filterOnOperator(String column, List<String> values) throws Exception {

        String operator = values.remove(0);

        if(values.size() == 0) {
            compiler.syntaxError(ERR_NEED_VALUE_WHEN_USING_OPERATOR_IN_FILTER);
        }

        if(values.size() > 1) {
            compiler.syntaxError(ERR_ONLY_ONE_VALUE_WHEN_USING_OPERATOR_IN_FILTER, values);
        }

        String filterClause = String.format("%s %s %s", column, operator, values.get(0));

        if (!statement.filterClauses.contains(filterClause)) {
            statement.filterClauses.add(filterClause);
        }
    }

    protected void filterOnValues(String column, List<String> values, boolean inclusive) throws Exception {
		/* If list is null or has no values, it is a program error.
		*/
        assert (values != null && values.size() > 0);

        for(String value : values) {
            if(!compiler.isNummeric(value) && !compiler.isQuoted(value)) {
                compiler.syntaxError(ERR_VALUE_SHOULD_BE_QUOTED, value);
            }
        }

        String filterClause;

		/* Expand the where clause
		*/
        if (values.size() == 0) {
            filterClause = String.format("%s %s NULL", column, inclusive ? "IS" : "IS NOT");
        } else if (values.size() == 1) {
            filterClause = String.format("%s %s %s",
                    column,
                    inclusive ? "=" : "!=",
                    values.get(0));
        } else {
            String argumentListINFunction = inclusive ? " IN (" : " NOT IN (";

            for (int idx = 0; idx < values.size(); idx++) {
                argumentListINFunction += String.format(" %s", values.get(idx));

                if (idx < values.size() - 1) {
                    argumentListINFunction += ",";
                }
            }

            argumentListINFunction += " )";

            filterClause = String.format("%s%s", column, argumentListINFunction);
        }

        if (!statement.filterClauses.contains(filterClause)) {
            statement.filterClauses.add(filterClause);
        }
    }

    public static class FullJoin extends Join {
        public FullJoin() {
            super(JOIN_TYPE.FULL);
        }
    }

    /**
     * Syntax: group( fielda, table.fieldb , ... )
     *
     */
    public static class Group extends Function {
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
}
