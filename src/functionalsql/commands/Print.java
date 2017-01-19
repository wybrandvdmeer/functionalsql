package functionalsql.commands;

import functionalsql.Function;
import functionalsql.consumer.TableOrColumnConsumer;

import java.util.ArrayList;
import java.util.List;

import static functionalsql.FunctionalSQLCompiler.ERR_SELECT_ALREADY_DEFINED;

/**
 * Syntax: print( column1 , column2 , ... )
 */
public class Print extends Function {
    private List<String> columns = new ArrayList<>();

    public Print() {
        build(new TableOrColumnConsumer(this, token -> columns.add(token)).mandatory());
    }

    public void execute() throws Exception {
        if (!getCompiler().getStatement().isVirginSelectClause()) {
            getCompiler().syntaxError(ERR_SELECT_ALREADY_DEFINED, getCompiler().getStatement().getSelectClause());
        }

        String selectClause = "SELECT";

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

            selectClause += " " + column;

            if (idx < columns.size() - 1) {
                selectClause += ",";
            }
        }

        getCompiler().getStatement().setSelectClause(selectClause);
    }
}
