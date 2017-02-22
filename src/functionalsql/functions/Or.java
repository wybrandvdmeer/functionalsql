package functionalsql.functions;

import functionalsql.Function;
import functionalsql.consumer.FunctionConsumer;

import java.util.ArrayList;
import java.util.List;

public class Or extends Function {
    private List<String> filterClauses = new ArrayList<>();
    private boolean orMode=true;

    private FilterClauseCatcher filterClauseCatcher;

    public Or() {
        build(new FunctionConsumer(this).expect(Filter.class).expect(FilterDate.class).expect(Or.class));
    }

    public Or(boolean orMode) {
        this();
        this.orMode = orMode;
    }

    public void preParse() {
        filterClauseCatcher = getCompiler().getStatement().getFilterClauseCatcher();
        getCompiler().getStatement().setFilterClauseCatcher(filterClause -> {
            if(!filterClauses.contains(filterClause)) {
                filterClauses.add(filterClause);
             }
        });
    }

    @Override
    public void execute() throws Exception {
        getCompiler().getStatement().setFilterClauseCatcher(filterClauseCatcher);

        String sql = "( ";
        for(int idx=0; idx < filterClauses.size(); idx++) {
            sql += ( filterClauses.get(idx) + " " );
            if(idx < filterClauses.size() - 1) {
                sql += (orMode ? "OR " : "AND ");
            }
        }

        getCompiler().getStatement().addFilterClause(sql + ")");
    }
}
