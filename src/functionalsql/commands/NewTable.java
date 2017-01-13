package functionalsql.commands;

import functionalsql.Function;
import functionalsql.FunctionalSQLCompiler;

/**
 * Syntax: newtable( table )
 *
 * Note: NewTable can only called as an argument of an other function.
 */
public class NewTable extends Function {
    private String alias = null;

    public NewTable() {
        this.argumentsTakesTableOrColumn(1);
    }

    /* Fetch table name.
    */
    protected void processor1(String s) throws Exception {
        setTable(s);
        finished();
    }

    public void execute() throws Exception {
        alias = statement.getAlias(getTable(), true);
    }

    public String getTableAlias() {
        return alias;
    }
}

