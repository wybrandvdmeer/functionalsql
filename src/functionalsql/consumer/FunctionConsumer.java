package functionalsql.consumer;

import functionalsql.Function;
import functionalsql.FunctionalSQLCompiler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FunctionConsumer extends Consumer<Function> {
    private List<Class<? extends Function>> functions = new ArrayList<>();

    public FunctionConsumer(Function function) {
        super(function, null);
    }

    public FunctionConsumer(Function function, Processor<Function> processor) {
        super(function, processor);
    }

    public FunctionConsumer add(Class<? extends Function> functionClass) {
        functions.add(functionClass);
        return this;
    }

    public void consume(Function function) throws Exception {
        boolean allowed=false;

        for(Class<? extends Function> clzz : functions) {
            if(clzz.isAssignableFrom(function.getClass())) {
                allowed = true;
                break;
            }
        }

        if(functions.size() > 0 && !allowed) {
            getCompiler().syntaxError(FunctionalSQLCompiler.ERR_CANNOT_USE_FUNCTION_AS_ARGUMENT_OF_FUNCTION,
                    getCompiler().getFSNameForFunction(function),
                    getCompiler().getFSNameForFunction(getFunction()));
        }

        super.consume(function);
    }
}