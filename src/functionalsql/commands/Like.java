package functionalsql.commands;

import functionalsql.Function;

import static functionalsql.FunctionalSQLCompiler.ERR_VALUE_SHOULD_BE_QUOTED;

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
        if(!getCompiler().isNummeric(value) && !getCompiler().isQuoted(value)) {
            getCompiler().syntaxError(ERR_VALUE_SHOULD_BE_QUOTED, value);
        }

        getCompiler().getStatement().filterClauses.add(String.format("%s LIKE %s", column, value));
    }
}
