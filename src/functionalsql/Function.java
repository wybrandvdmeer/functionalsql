package functionalsql;

import java.util.ArrayList;
import java.util.List;

public abstract class Function {

    protected FunctionalSQLCompiler compiler;

    protected Statement statement;

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

    public void process(String languageElement) throws Exception{
        expectAnotherArgument = false;
        switch(step) {
            case 1: processor1(languageElement); break;
            case 2: processor2(languageElement); break;
            case 3: processor3(languageElement); break;
            case 4: processor4(languageElement); break;
        }
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

    public abstract void execute() throws Exception;
}
