package functionalsql.commands;

import functionalsql.Function;
import functionalsql.consumer.TableOrColumnConsumer;
import functionalsql.consumer.TokenConsumer;

/**
 * Syntax:
 *
 *    a filterdate( column, 20120101 )
 *    a filterdate( column, 20120101 , >= )
 *    a filterdate( column, 20120101 , <= )
 *    a filterdate( column, 20120101 , '>' )
 *    a filterdate( column, 20120101 , '<' )
 *    a filterdate( column, 20120101, 20140101 )
 *
 */
public class FilterDate extends Function {
    private String secondValueOrOperator = "=", column, value;

    public FilterDate() {
        build(new TableOrColumnConsumer(this, token -> column = token).singleValue().mandatory());
        build(new TokenConsumer(this, token -> value = token).singleValue().mandatory());
        build(new TokenConsumer(this, token -> secondValueOrOperator = token));
    }

    public void execute() throws Exception {
        if (secondValueOrOperator == null) {
            secondValueOrOperator = "=";
        }

        if ("=".equals(secondValueOrOperator) ||
                "<=".equals(secondValueOrOperator) ||
                ">=".equals(secondValueOrOperator) ||
                "<".equals(secondValueOrOperator) ||
                ">".equals(secondValueOrOperator)) {
            getCompiler().getStatement().addFilterClause(String.format("%s %s '%s'", column, secondValueOrOperator, value));
        } else {
            getCompiler().getStatement().addFilterClause(String.format("%s >= '%s'", column, value));
            getCompiler().getStatement().addFilterClause(String.format("%s < '%s'", column, secondValueOrOperator));
        }
    }
}
