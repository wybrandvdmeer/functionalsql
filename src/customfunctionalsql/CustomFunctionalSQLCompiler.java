package customfunctionalsql;

import functionalsql.Filter;
import functionalsql.FunctionalSQLCompiler;

import java.util.ArrayList;
import java.util.List;

public class CustomFunctionalSQLCompiler extends FunctionalSQLCompiler {
    public static final String ERR_ARGUMENT_SHOULD_BE_NUMMERICAL = "Argument (%s) should be nummerical.";

    public CustomFunctionalSQLCompiler() {
        super.addCustomFunction("id", Id.class);
    }
}
