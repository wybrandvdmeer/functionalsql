package functionalsql;

import functionalsql.consumer.TableOrColumnConsumer;
import functionalsql.consumer.Consumer;
import functionalsql.consumer.FunctionConsumer;
import functionalsql.consumer.TokenConsumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

public abstract class Function {
    private FunctionalSQLCompiler compiler;
    private Map<Integer, List<Consumer>> consumersPerArgument = new HashMap<>();
    private Map<Consumer, Integer> nextArgumentForConsumer = new HashMap<>();

    private int argument = 0;

    public void setCompiler(FunctionalSQLCompiler compiler) {
        this.compiler = compiler;
    }

    public FunctionalSQLCompiler getCompiler() {
        return compiler;
    }

    public boolean expectTableOrColumn() {
        List<Consumer> consumers = consumersPerArgument.get(argument);
        if(consumers == null) {
            return false;
        }
        return getConsumer(consumers, TableOrColumnConsumer.class) != null;
    }

    private Consumer getConsumer(List<Consumer> consumers, Class<? extends Consumer> type) {
        return consumers.stream().filter(c -> type.isInstance(c)).findAny().orElse(null);
    }

    public void process(Object token) throws Exception {
        List<Consumer> consumers = consumersPerArgument.get(argument);
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
            /* At this point there are consumers, so the remaining consumer has to be a FunctionConsumer.
            */
            getCompiler().syntaxError(FunctionalSQLCompiler.ERR_EXPECT_A_FUNCTION_CALL, token);
        }

        consumer.consume(token);

        if(consumer.isSingleValue()) {
            argument++;
        }

        if(nextArgumentForConsumer.containsKey(consumer)) {
            argument = nextArgumentForConsumer.get(consumer);
        }
    }

    protected boolean isFinished() {
        return consumersPerArgument.get(argument) == null;
    }

    protected boolean expectArgument() {
        if(isFinished()) {
            return false;
        }

        for(Consumer consumer : consumersPerArgument.get(argument)) {
            if(consumer.isMandatory() && !consumer.hasConsumed()) {
                return true;
            }
        }

        return false;
    }

    public void build(Consumer consumer) {
        Integer argument = consumersPerArgument.keySet().stream().max(Comparator.naturalOrder()).map(s -> s + 1).orElse(0);
        consumersPerArgument.computeIfAbsent(argument, v -> new ArrayList<>()).add(consumer);
        validate(argument);
    }

    public void build(Integer argument, Consumer consumer) {
        consumersPerArgument.computeIfAbsent(argument, v -> new ArrayList<>()).add(consumer);
        validate(argument);
    }

    private void validate(int argument) {
        if(consumersPerArgument.get(argument).size() > 2) {
            throw new RuntimeException("Only 2 consumers per argument allowed.");
        }

        if(consumersPerArgument.get(argument).size() == 2) {
            List<Consumer> consumers = consumersPerArgument.get(argument);
            if((consumers.get(0) instanceof TokenConsumer && consumers.get(1) instanceof TokenConsumer) ||
                    (consumers.get(0) instanceof FunctionConsumer && consumers.get(1) instanceof FunctionConsumer)) {
                throw new RuntimeException("Cannot program 2 consumers of same type for one argument.");
            }
        }
    }

    protected void setNextStepForConsumer(Consumer consumer, Integer nextStep) {
        nextArgumentForConsumer.put(consumer, nextStep);
    }

    public abstract void execute() throws Exception;
}
