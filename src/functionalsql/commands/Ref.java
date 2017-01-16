package functionalsql.commands;

import functionalsql.Function;

import java.util.Map;

import static functionalsql.FunctionalSQLCompiler.*;

/**
 Syntax:
 ref( table , reference );
 ref( table.column , reference );

 Note:
 1> Reference is number which should equal or greater then 1.

 2> The opposite equivalent of this function is the newtable function which can be used to add another instance of the same table to
 the query.

 3> Note: Ref can only called as an argument of an other function.
 */
public class Ref extends Function {
    private String reference = null;

    /* FIND TABLE/COLUMN ON FOR WHICH TO FIND THE ALIAS.
    */
    protected void processor1(String s) throws Exception {
        column = s;
        nextMandatoryStep();
    }

    /* FIND THE REFERENCE.
    */
    protected void processor2(String s) throws Exception {
        value = s;
        finished();
    }

    public void execute() throws Exception {
        reference = ref(column, value);
    }

    public String getReference() {
        return reference;
    }

    private String ref(String tableColumn, String reference) throws Exception {
        String[] tableAndColumn = getCompiler().splitTableColumn(tableColumn);

        /* If ref is programmed, the referenced table should already be processed.
        */
        if (!getCompiler().getStatement().isTable(tableAndColumn[0])) {
            getCompiler().syntaxError(ERR_REFERING_TO_A_NON_EXISTING_TABLE, tableAndColumn[0]);
        }

        if (!getCompiler().isNummeric(reference)) {
            getCompiler().syntaxError(ERR_TABLE_REFERENCE_SHOULD_BE_NUMMERICAL, reference);
        }

        if (Integer.parseInt(reference) < 1) {
            getCompiler().syntaxError(ERR_TABLE_REFERENCE_SHOULD_BE_EQUAL_OR_GREATER_THEN_ONE, reference);
        }

        int idx = 0;
        String alias = null;

        for (Map.Entry<String, String> entry : getCompiler().getStatement().aliases.entrySet()) {

            if (!tableAndColumn[0].equals(entry.getValue())) {
                continue;
            }

            if (idx == Integer.parseInt(reference) - 1) {
                alias = entry.getKey();
            }

            idx++;
        }

        if (alias == null) {
            getCompiler().syntaxError(ERR_TABLE_REFERENCE_IS_NOT_CORRECT, reference);
        }

        return alias + (tableAndColumn.length > 1 ? ("." + tableAndColumn[1]) : ""); // E.g. t0 or t0.column
    }
}
