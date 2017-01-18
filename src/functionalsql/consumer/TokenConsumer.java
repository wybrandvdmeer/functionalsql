package functionalsql.consumer;

import functionalsql.Function;

public class TokenConsumer extends Consumer<String> {
    public TokenConsumer(Function function, Processor<String> processor) {
        super(function, processor);
    }
}
