package functionalsql.commands;

import functionalsql.Function;
import functionalsql.consumer.TableOrColumnConsumer;
import functionalsql.consumer.TokenConsumer;

import static functionalsql.FunctionalSQLCompiler.ERR_VALUE_SHOULD_BE_QUOTED;

/**
 * Syntax: like( column , 'aa%bb' )
 */
public class Like extends Function {

    private String column, value;

    public Like() {
        build(new TableOrColumnConsumer(this, token -> column = token).singleValue().mandatory());
        build(new TokenConsumer(this, token -> value = token).singleValue().mandatory());
    }

    public void execute() throws Exception {
        if(!getCompiler().isNummeric(value) && !getCompiler().isQuoted(value)) {
            getCompiler().syntaxError(ERR_VALUE_SHOULD_BE_QUOTED, value);
        }

        getCompiler().getStatement().addFilterClause(String.format("%s LIKE %s", column, value));
    }
}
