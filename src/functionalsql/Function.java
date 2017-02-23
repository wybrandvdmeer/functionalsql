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
    private Map<Integer, Consumers> consumersPerArgument = new HashMap<>();
    private Map<Consumer, Integer> nextArgumentForConsumer = new HashMap<>();
    private List<Integer> statementArguments = new ArrayList<>();

    private int argument = 0;

    public void setCompiler(FunctionalSQLCompiler compiler) {
        this.compiler = compiler;
    }

    public FunctionalSQLCompiler getCompiler() {
        return compiler;
    }

    public boolean expectTableOrColumn() {
        Consumers consumers = consumersPerArgument.get(argument);
        if(consumers == null) {
            return false;
        }
        return consumers.tokenConsumer instanceof TableOrColumnConsumer;
    }

    protected void preParse() {
    }

    @SuppressWarnings("unchecked")
    public void process(Object token) throws Exception {
        Consumers consumers = consumersPerArgument.get(argument);
        if(consumers == null) {
            getCompiler().syntaxError(FunctionalSQLCompiler.ERR_FUNCTION_HAS_TOO_MANY_ARGUMENTS);
        }

        Consumer consumer;

        if(token instanceof Function) {
            if((consumer = consumers.functionConsumer) == null) {
                getCompiler().syntaxError(FunctionalSQLCompiler.ERR_CANNOT_USE_FUNCTION_AS_ARGUMENT_OF_FUNCTION,
                        getCompiler().getFSNameForFunction((Function)token),
                        getCompiler().getFSNameForFunction(this));
            }
        } else if((consumer = consumers.tokenConsumer) == null) {
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

        if(hasConsumed()) {
            return false;
        }

        for(Consumer consumer : consumersPerArgument.get(argument).consumers) {
            if(consumer.isMandatory()) {
                return true;
            }
        }

        return false;
    }

    public void build(Consumer consumer) {
        Integer argument = consumersPerArgument.keySet().stream().max(Comparator.naturalOrder()).map(s -> s + 1).orElse(0);
        consumersPerArgument.computeIfAbsent(argument, v -> new Consumers()).add(consumer);
    }

    public void build(Integer argument, Consumer consumer) {
        consumersPerArgument.computeIfAbsent(argument, v -> new Consumers()).add(consumer);
    }

    public void buildAndReplace(Integer argument, Consumer consumer) {
        consumersPerArgument.put(argument + 1, consumersPerArgument.get(argument));
        consumersPerArgument.remove(argument);
        build(argument, consumer);
    }

    protected void setNextStepForConsumer(Consumer consumer, Integer nextStep) {
        nextArgumentForConsumer.put(consumer, nextStep);
    }

    private class Consumers {
        FunctionConsumer functionConsumer;
        TokenConsumer tokenConsumer;
        List<Consumer> consumers = new ArrayList<>();

        void add(Consumer consumer) {
            if(consumer instanceof FunctionConsumer) {
                if(functionConsumer != null) {
                    throw new RuntimeException("Consumer already defined.");
                }
                functionConsumer = (FunctionConsumer)consumer;
            } else {
                if(tokenConsumer != null) {
                    throw new RuntimeException("Consumer already defined.");
                }
                tokenConsumer = (TokenConsumer)consumer;
            }

            consumers.add(consumer);
        }
    }

    public void addStatementArgument(Integer argument) {
        statementArguments.add(argument);
    }

    public boolean isProcessingStatementArgument() {
        return statementArguments.contains(argument) && hasConsumed();
    }

    private boolean hasConsumed() {
        for(Consumer consumer : consumersPerArgument.get(argument).consumers) {
            if(consumer.hasConsumed()) {
                return true;
            }
        }
        return false;
    }


    public abstract void execute() throws Exception;
}
