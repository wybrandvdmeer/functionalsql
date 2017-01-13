package functionalsql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Function {

    protected FunctionalSQLCompiler compiler;
    protected Statement statement;

    private Map<Integer, List<Class<? extends Function>>> expectedFunctionsPerStep = new HashMap<>();

    /* Useful vars!!!!
    */
    protected List<String> columns = new ArrayList<>();
    protected List<String> values = new ArrayList<>();
    protected String column = null, value = null;

    private String table=null;

    private Integer step = 1;
    private boolean finished = false, expectAnotherArgument = false;

    private List<Integer> columnArguments = new ArrayList<>();

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    public void setCompiler(FunctionalSQLCompiler compiler) {
        this.compiler = compiler;
    }

    public boolean isColumn(int argument) {
        return columnArguments.contains(argument);
    }

    public void process(Function function) throws Exception {
        expectAnotherArgument = false;
        switch(step) {
            case 1: processor1(function); break;
            case 2: processor2(function); break;
            case 3: processor3(function); break;
            case 4: processor4(function); break;
        }
    }

    public void process(String languageElement) throws Exception{
        expectAnotherArgument = false;
        switch(step) {
            case 1: processor1(languageElement); break;
            case 2: processor2(languageElement); break;
            case 3: processor3(languageElement); break;
            case 4: processor4(languageElement); break;
        }
    }

    protected void processor1(Function function) throws Exception {
    }

    protected void processor2(Function function) throws Exception {
    }

    protected void processor3(Function function) throws Exception {
    }

    protected void processor4(Function function) throws Exception {
    }

    protected void processor1(String languageElement) throws Exception {
    }

    protected void processor2(String languageElement) throws Exception {
    }

    protected void processor3(String languageElement) throws Exception {
    }

    protected void processor4(String languageElement) throws Exception {
    }

    protected void argumentsTakesTableOrColumn(int argument) {
        columnArguments.add(argument);
    }

    public int getStep() {
        return step;
    }

    protected void finished() {
        finished = true;
    }

    protected boolean isFinished() {
        return finished;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    /**
     * When function wants to go to the next (optional!!) processing step, this method should be called.
     */
    protected void nextStep() {
        step++;
    }

    protected void nextStep(int nextStep) {
        step = nextStep;
    }

    /** When function wants to go to the next (mandatory!!) processing step, this method should be called.
     */
    protected void nextMandatoryStep() {
        step++;
        expectAnotherArgument = true;
    }

    public boolean expectAnotherArgument() {
        return expectAnotherArgument && !finished;
    }

    public void addExpectedFunction(Integer step, Class<? extends Function> function) {
        expectedFunctionsPerStep.computeIfAbsent(step, value -> new ArrayList<>()).add(function);
    }

    public boolean isFunctionExpected(Class<? extends Function> function) {
        List<Class<? extends Function>> expectedFunctions = expectedFunctionsPerStep.get(step);
        return expectedFunctions != null ? expectedFunctions.contains(function) : false;
    }

    public abstract void execute() throws Exception;
}
