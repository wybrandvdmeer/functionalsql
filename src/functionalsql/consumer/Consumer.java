package functionalsql.consumer;

import functionalsql.Function;
import functionalsql.FunctionalSQLCompiler;

public abstract class Consumer<T> {

    private boolean singleValue=false, mandatory=false, consumed=false, consumesStatement=false;

    private final Function function;

    private final Processor<T> processor;

    public Consumer(Function function, Processor<T> processor) {
        this.function = function;
        this.processor = processor;
    }

    public Function getFunction() {
        return function;
    }

    public FunctionalSQLCompiler getCompiler() {
        return function.getCompiler();
    }

    public Consumer<T> singleValue() {
        singleValue = true;
        return this;
    }

    public Consumer<T> mandatory() {
        mandatory = true;
        return this;
    }

    public Consumer<T> consumesStatement() {
        consumesStatement = true;
        return this;
    }

    public boolean isSingleValue() {
        return singleValue;
    }

    public boolean isMandatory() { return mandatory; }

    public boolean hasConsumed() {
        return consumed;
    }

    public boolean isConsumingAStatement() {
        return consumesStatement;
    }

    public void consume(T token) throws Exception {
        consumed = true;
        if(processor != null) {
            processor.process(token);
        }
    }
}
