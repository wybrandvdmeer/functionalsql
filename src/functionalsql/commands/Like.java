package functionalsql.commands;


import functionalsql.Function;
import functionalsql.FunctionalSQLCompiler;

import static functionalsql.FunctionalSQLCompiler.*;

/**
 * Syntax: like( column , 'aa%bb' )
 */
public class Like extends Function {
    public Like() {
        argumentsTakesTableOrColumn(1);
    }

    /* FIND FIELD ON WHICH TO FILTER.
    */
    protected void processor1(String s) throws Exception {
        column = s;
        nextMandatoryStep();
    }

    /* Process like pattern.
    */
    protected void processor2(String s) throws Exception {
        value = s;
        finished();
    }

    public void execute() throws Exception {
        if(!compiler.isNummeric(value) && !compiler.isQuoted(value)) {
            compiler.syntaxError(ERR_VALUE_SHOULD_BE_QUOTED, value);
        }

        statement.filterClauses.add(String.format("%s LIKE %s", column, value));
    }
}
