package customfunctionalsql;

import functionalsql.Function;
import functionalsql.commands.Filter;
import functionalsql.consumer.TableOrColumnConsumer;
import functionalsql.consumer.TokenConsumer;

import java.util.ArrayList;
import java.util.List;

import static customfunctionalsql.CustomFunctionalSQLCompiler.ERR_ARGUMENT_SHOULD_BE_NUMMERICAL;
import static functionalsql.FunctionalSQLCompiler.ERR_REFERING_TO_A_NON_EXISTING_TABLE;

/**
 * Example of a custom function.
 * Function works on tables which contains a field called id.
 * It adds a filter to an expression similar to table.id = <value>.
 *
 * Syntax:
 *
 * id(value)
 * id(table, value)
 */
public class Id extends Function {

    private String value1, value2;

    public Id() {
        build(new TableOrColumnConsumer(this, token -> value1 = token).singleValue());
        build(new TokenConsumer(this, token -> value2 = token).singleValue());
    }

    @Override
    public void execute() throws Exception {
        try {
            Integer.parseInt(value2 == null ? value1 : value2);
        } catch(NumberFormatException e) {
            getCompiler().syntaxError(ERR_ARGUMENT_SHOULD_BE_NUMMERICAL, value2 == null ? value1 : value2);
        }

        if(value2 != null) {
            if(!getCompiler().getStatement().isTable(value1)) {
                getCompiler().syntaxError(ERR_REFERING_TO_A_NON_EXISTING_TABLE, value1);
            }

            value1 = getCompiler().getStatement().getAlias(value1);
        }

        String column = value2 != null ? (value1 + ".id") : "id";

        String filterClause = String.format("%s = %s", column, value2 != null ? value2 : value1);

        getCompiler().getStatement().addFilterClause(filterClause);
    }
}