package functionalsql.functions;

import functionalsql.Function;
import functionalsql.consumer.TableOrColumnConsumer;

/**
 * Syntax: newtable( table )
 *
 * Note: NewTable can only called as an argument of an other function.
 */
public class NewTable extends Function {
    private String table, alias;

    public NewTable() {
        build(new TableOrColumnConsumer(this, token -> table = token).singleValue().mandatory());
    }

    public void execute() throws Exception {
        alias = getCompiler().getStatement().getAlias(table, true);
    }

    public String getTable() {
        return table;
    }

    public String getTableAlias() {
        return alias;
    }
}

