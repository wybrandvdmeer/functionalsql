package functionalsql.functions;

import functionalsql.Function;
import functionalsql.consumer.TableOrColumnConsumer;

import java.util.ArrayList;
import java.util.List;

import static functionalsql.FunctionalSQLCompiler.ERR_SELECT_ALREADY_DEFINED;

/**
 * Syntax:
 *  distinct-column( column, column , ... )
 *
 * Note: user can expect columns and constants to the function in a random order.
 */
public class Distinct extends Function {
    private List<String> columns = new ArrayList<>();

    public Distinct() {
        /* Check if argument is a table. If so, all fields of table are selected.
        Argument can also be a reference when the table was referred with the ref( table, occ ) function.
        */
        build(new TableOrColumnConsumer(this, token -> {
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
            getCompiler().syntaxError(ERR_SELECT_ALREADY_DEFINED, getCompiler().getStatement().getSelectClause());
        }

        getCompiler().getStatement().setSelectClause("SELECT DISTINCT");

        /* Expand the select clause.
        */
        for (int idx = 0; idx < columns.size(); idx++) {
            getCompiler().getStatement().setSelectClause(getCompiler().getStatement().getSelectClause() + " " + columns.get(idx));

            if (idx < columns.size() - 1) {
                getCompiler().getStatement().setSelectClause(getCompiler().getStatement().getSelectClause() + ",");
            }
        }
    }
}
