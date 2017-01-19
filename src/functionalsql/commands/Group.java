package functionalsql.commands;

import functionalsql.Function;
import functionalsql.consumer.TokenConsumer;

import java.util.ArrayList;
import java.util.List;

import static functionalsql.FunctionalSQLCompiler.ERR_SELECT_ALREADY_DEFINED;

/**
 * Syntax: group( fielda, table.fieldb , ... )
 *
 */
public class Group extends Function {
    private List<String> columns = new ArrayList<>();

    public Group() {
        argumentsTakesTableOrColumn(1);
        build(1, new TokenConsumer(this, token -> columns.add(token)).mandatory());
    }

    public void execute() throws Exception {
        if (!getCompiler().getStatement().isVirginSelectClause()) {
            getCompiler().syntaxError(ERR_SELECT_ALREADY_DEFINED, getCompiler().getStatement().getSelectClause());
        }

        String selectClause = "SELECT";
        String groupByClause = "GROUP BY";

        /* Expand the select and group clause.
        */
        for (int idx = 0; idx < columns.size(); idx++) {
            selectClause += " " + columns.get(idx);
            groupByClause += " " + columns.get(idx);

            if (idx < columns.size() - 1) {
                selectClause += ",";
                groupByClause += ",";
            }
        }

        getCompiler().getStatement().setSelectClause(selectClause);
        getCompiler().getStatement().setGroupByClause(groupByClause);
    }
}