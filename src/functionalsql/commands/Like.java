package functionalsql.commands;

import functionalsql.Function;
import functionalsql.consumer.TokenConsumer;

import static functionalsql.FunctionalSQLCompiler.ERR_VALUE_SHOULD_BE_QUOTED;

/**
 * Syntax: like( column , 'aa%bb' )
 */
public class Like extends Function {
    public Like() {
        argumentsTakesTableOrColumn(1);
        build(1, new TokenConsumer(this, token -> column = token).singleValue().mandatory());
        build(2, new TokenConsumer(this, token -> value = token).singleValue().mandatory());
    }

    public void execute() throws Exception {
        if(!getCompiler().isNummeric(value) && !getCompiler().isQuoted(value)) {
            getCompiler().syntaxError(ERR_VALUE_SHOULD_BE_QUOTED, value);
        }

        getCompiler().getStatement().filterClauses.add(String.format("%s LIKE %s", column, value));
    }
}
