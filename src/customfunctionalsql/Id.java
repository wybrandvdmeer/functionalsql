package customfunctionalsql;

import functionalsql.commands.Filter;

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
public class Id extends Filter {

    String value1, value2;

    protected void processor1(String s) throws Exception {
        value1 = s;
        nextStep();
    }

    protected void processor2(String s) throws Exception {
        value2 = s;
        finished();
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
        List<String> values = new ArrayList<>();
        values.add(value2 != null ? value2 : value1);

        filter(column, values, true);
    }
}