package functionalsql.functions;

import functionalsql.Function;
import functionalsql.consumer.TableOrColumnConsumer;

import java.util.ArrayList;
import java.util.List;

import static functionalsql.FunctionalSQLCompiler.ERR_SELECT_ALREADY_DEFINED;

/**
 * Syntax: (sum|max|min)( summation_column | nummerical_constant , field1 , table.field2 , ... )
 *
 */
public class Report extends Function {
    private final String function;
    private List<String> columns = new ArrayList<>();

    private String reportFunction = null;

    public Report(String function) {
        this.function = function;

        build(new TableOrColumnConsumer(this, token -> reportFunction = token).singleValue().mandatory());
        build(new TableOrColumnConsumer(this, token -> columns.add(token)));
    }

    public void execute() throws Exception {
        if (!getCompiler().getStatement().isVirginSelectClause()) {
            getCompiler().syntaxError(ERR_SELECT_ALREADY_DEFINED, getCompiler().getStatement().getSelectClause());
        }

        /* If anything else, then it is a program error.
        */
        assert ("SUM".equals(function) || "MAX".equals(function) || "MIN".equals(function));

        String selectClause = "SELECT";
        String groupByClause=null;

        if (columns.size() > 0) {
             groupByClause = "GROUP BY";
        }

        for (int idx = 0; idx < columns.size(); idx++) {
            selectClause += " " + columns.get(idx);
            groupByClause += " " + columns.get(idx);

            if (idx < columns.size() - 1) {
                selectClause += ",";
                groupByClause += ",";
            }
        }

        getCompiler().getStatement().setGroupByClause(groupByClause);
        getCompiler().getStatement().setSelectClause(
                selectClause + String.format("%s %s( %s )", columns.size() > 0 ? "," : "", function, reportFunction));
    }
}
