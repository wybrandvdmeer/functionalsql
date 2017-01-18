package functionalsql.commands;

import functionalsql.Function;
import functionalsql.consumer.TokenConsumer;

/**
 * Syntax: newtable( table )
 *
 * Note: NewTable can only called as an argument of an other function.
 */
public class NewTable extends Function {
    private String alias = null;

    public NewTable() {
        argumentsTakesTableOrColumn(1);
        build(1, new TokenConsumer(this, token -> setTable(token)).singleValue().mandatory());
    }

    public void execute() throws Exception {
        alias = getCompiler().getStatement().getAlias(getTable(), true);
    }

    public String getTableAlias() {
        return alias;
    }
}

