package functionalsql;

import functionalsql.consumer.TableOrColumnConsumer;
import functionalsql.consumer.Consumer;
import functionalsql.consumer.FunctionConsumer;
import functionalsql.consumer.TokenConsumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Function {
    private FunctionalSQLCompiler compiler;
    private Map<Integer, List<Consumer>> consumersPerStep = new HashMap<>();
    private Map<Consumer, Integer> nextStepForConsumer = new HashMap<>();

    private Integer step = 1;

    public void setCompiler(FunctionalSQLCompiler compiler) {
        this.compiler = compiler;
    }

    public FunctionalSQLCompiler getCompiler() {
        return compiler;
    }

    public boolean expectTableOrColumn() {
        List<Consumer> consumers = consumersPerStep.get(step);
        if(consumers == null) {
            return false;
        }
        return getConsumer(consumers, TableOrColumnConsumer.class) != null;
    }

    public void build(Integer step, Consumer consumer) {
        consumersPerStep.computeIfAbsent(step, v -> new ArrayList<>()).add(consumer);
    }

    protected void setNextStepForConsumer(Consumer consumer, Integer nextStep) {
        nextStepForConsumer.put(consumer, nextStep);
    }

    private Consumer getConsumer(List<Consumer> consumers, Class<? extends Consumer> type) {
        return consumers.stream().filter(c -> type.isInstance(c)).findAny().orElse(null);
    }

    public void process(Object token) throws Exception {
        List<Consumer> consumers = consumersPerStep.get(step);
        if(consumers == null) {
            getCompiler().syntaxError(FunctionalSQLCompiler.ERR_FUNCTION_HAS_TOO_MANY_ARGUMENTS);
        }

        Consumer consumer;

        if(token instanceof Function) {
            if((consumer = getConsumer(consumers, FunctionConsumer.class)) == null) {
                getCompiler().syntaxError(FunctionalSQLCompiler.ERR_CANNOT_USE_FUNCTION_AS_ARGUMENT_OF_FUNCTION,
                        getCompiler().getFSNameForFunction((Function)token),
                        getCompiler().getFSNameForFunction(this));
            }
        } else if((consumer = getConsumer(consumers, TokenConsumer.class)) == null) {
                getCompiler().syntaxError(FunctionalSQLCompiler.ERR_CANNOT_USE_FUNCTION_AS_ARGUMENT_OF_FUNCTION,
                        getCompiler().getFSNameForFunction((Function)token),
                        getCompiler().getFSNameForFunction(this));
        }

        consumer.consume(token);

        if(consumer.isSingleValue()) {
            step++;
        }

        if(nextStepForConsumer.containsKey(consumer)) {
            step = nextStepForConsumer.get(consumer);
        }
    }

    protected boolean isFinished() {
        return consumersPerStep.get(step) == null;
    }

    protected boolean expectArgument() {
        if(isFinished()) {
            return false;
        }

        for(Consumer consumer : consumersPerStep.get(step)) {
            if(consumer.isMandatory() && !consumer.hasConsumed()) {
                return true;
            }
        }

        return false;
    }

    public abstract void execute() throws Exception;
}
