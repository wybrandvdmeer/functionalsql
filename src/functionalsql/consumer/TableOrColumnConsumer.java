package functionalsql.consumer;

import functionalsql.Function;

public class TableOrColumnConsumer extends TokenConsumer {
    public TableOrColumnConsumer(Function function, Processor<String> processor) {
        super(function, processor);
    }
}
