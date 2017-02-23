package functionalsql.functions;

import functionalsql.Function;
import functionalsql.consumer.TableOrColumnConsumer;

public class In extends Statement {
    private String column;

    public In() {
        buildAndReplace(0, new TableOrColumnConsumer(this, token -> column = token).singleValue().mandatory());
        addStatementArgument(1);
    }

    protected void preParse() {
        getCompiler().getStatements().add(this);
    }

    @Override
    public void execute() throws Exception {
        getCompiler().getStatements().remove(this);
        super.execute();
        getCompiler().getStatement().addFilterClause(String.format("%s IN (%s)", column, getSql()));
    }
}
