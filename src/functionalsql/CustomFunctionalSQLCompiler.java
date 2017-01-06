package functionalsql;

public class CustomFunctionalSQLCompiler extends FunctionalSQLCompiler {

    /** Error. */
    public static final String ERR_ARGUMENT_SHOULD_BE_NUMMERICAL = "Argument (%s) should be nummerical.";

    public CustomFunctionalSQLCompiler() {
        super.addCustomFunction("id", Id.class);
    }

    /**
     * Example of a custom function.
     * Function works on tables which contains a field called id.
     * It adds a filter to an expression similar to table.id = 12.
     *
     * Syntax:
     *
     * id(10)
     * id(table, 10)
     */
    protected class Id extends Function {

        String value1, value2;

        protected void processor1(String s) throws Exception {
            value1 = s;
            nextStep();
        }

        protected void processor2(String s) throws Exception {
            value2 = s;
            finished();
        }

        @Override
        protected void post() throws Exception {

            try {
                Integer.parseInt(value2 == null ? value1 : value2);
            } catch(NumberFormatException e) {
                syntaxError(ERR_ARGUMENT_SHOULD_BE_NUMMERICAL, value2 == null ? value1 : value2);
            }

            if(value2 == null) {
                compileFSFragment(String.format("filter(id,%s)", value1));
            } else {
                compileFSFragment(String.format("filter(%s.id,%s)", value1, value2));
            }
        }
    }
}
