package functionalsql;

import functionalsql.commands.*;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Functional sql compiler.
 */
public class FunctionalSQLCompiler {

    public static final String ERR_CANNOT_USE_FUNCTION_AS_ARGUMENT_OF_FUNCTION = "Cannot use function (%s) as argument of function (%s).";

    public static final String ERR_NEED_VALUE_WHEN_USING_OPERATOR_IN_FILTER = "Need a value to filter on when using operator in filter function.";

    public static final String ERR_ONLY_ONE_VALUE_WHEN_USING_OPERATOR_IN_FILTER = "Only one value when using operator in filter function (%s).";

    public static final String ERR_UNKNOWN_FUNCTION = "Unknown function (%s).";

    public static final String ERR_WRONG_FORMAT_TABLE_OR_COLUMN_NAME = "Wrong format table or column name: %s.";

    public static final String ERR_VALUE_SHOULD_BE_QUOTED = "Value (%s) should be quoted.";

    public static final String ERR_UNEXPECTED_END_OF_STATEMENT = "Unexpected end of statement.";

    public static final String ERR_IF_TABLE_HAS_MULTIPLE_INSTANCES_USE_REF_FUNCTION = "If table has multiple instances, use the ref function (table=%s).";

    public static final String ERR_TABLE_REFERENCE_SHOULD_BE_EQUAL_OR_GREATER_THEN_ONE = "Reference should be equal or greater than one (%s).";

    public static final String ERR_TABLE_REFERENCE_IS_NOT_CORRECT = "Table reference (%s) is not correct.";

    public static final String ERR_REFERING_TO_A_NON_EXISTING_TABLE = "Refering to a non existing table (%s).";

    public static final String ERR_TABLE_REFERENCE_SHOULD_BE_NUMMERICAL = "Table reference should be nummerical (%s).";

    public static final String UNEXPECTED_END_OF_FUNCTION = "Unexpected end of function.";

    public static final String ERR_JOIN_SHOULD_FOLLOW_JOIN = "A join can only be followed by another join. Instead found '%s'.";

    public static final String ERR_EXP_OPENING_BRACKET = "Expected opening bracket.";

    public static final String ERR_EXP_COMMA = "Expected ',' instead of (%s).";

    public static final String ERR_MISSING_END_QUOTE = "Missing end quote.";

    public static final String ERR_NULL_TABLE = "Null table.";

    public static final String ERR_NULL_FIELD = "Null field.";

    public static final String ERR_FUNCTION_HAS_NO_ARGUMENTS = "Function has no arguments.";

    public static final String ERR_SELECT_ALREADY_DEFINED = "Select clause (%s) is already defined.";

    public static final String ERR_NO_JOIN_COLUMNS_DEFINED_AND_NO_CUSTOM_MAPPING_PRESENT = "No join columns defined in statement and no custom mapping found.";

    public static final String ERR_DEFAULT_MAPPING_HAS_NO_EQUAL_COLUMNS = "Default mapping has no equal columns.";

    public static final String ERR_FUNCTION_HAS_TOO_MANY_ARGUMENTS = "Function has to many arguments.";

    private static String HELP = "<table> function1 function2 ... ";

    private Map<String, Class<? extends Function>> functions = new HashMap<>();

    private List<Statement> statements = new ArrayList<>();

    private List<String> textElements;

    private List<CustomMapping> customMappings = new ArrayList<CustomMapping>();

    private String originalStatement;
    private int popCounter = 0;

    private final static Pattern TABLE_COLUMN_FORMAT=Pattern.compile("[a-zA-Z0-9_]*");

    private final static Pattern NUMMERIC_FORMAT=Pattern.compile("[-]*[0-9.]*");

    public FunctionalSQLCompiler() {
        functions.put("join", Join.class);
        functions.put("innerjoin", InnerJoin.class);
        functions.put("leftjoin", LeftJoin.class);
        functions.put("rightjoin", RightJoin.class);
        functions.put("fulljoin", FullJoin.class);
        functions.put("print", Print.class);
        functions.put("like", Like.class);
        functions.put("group", Group.class);
        functions.put("asc", Order.class);
        functions.put("desc", Desc.class);
        functions.put("sum", Sum.class);
        functions.put("(", Statement.class);
        functions.put("distinct", Distinct.class);
        functions.put("min", Min.class);
        functions.put("max", Max.class);
        functions.put("filter", Filter.class);
        functions.put("filterdate", FilterDate.class);
        functions.put("notfilter", NotFilter.class);
        functions.put("newtable", NewTable.class);
        functions.put("ref", Ref.class);
    }

    public Class<? extends Function> getFunction(String function) {
        return functions.get(function);
    }

    public void renameFunction(String existingFunction, String newFunction) throws Exception {
        Class<? extends Function> function = functions.get(existingFunction);
        if(function == null) {
            syntaxError(ERR_UNKNOWN_FUNCTION, existingFunction);
        }

        functions.remove(existingFunction);

        functions.put(newFunction, function);
    }

    /**
     * To add customized functions.
     * @param name The name of the function as it is mentioned in the language.
     * @param function The function class.
     */
    public void addCustomFunction(String name, Class<? extends Function> function) {
        functions.put(name, function);
    }

    /**
     * To add a custom database mapping.
     *
     * @param table1 Drive table.
     * @param column1 Drive table column.
     * @param table2 Join table.
     * @param column2 Join table column.
     */
    public void addCustomMapping(String table1, String column1, String table2, String column2) throws Exception {
        CustomMapping c = new CustomMapping(table1, column1, table2, column2);

        if (!customMappings.contains(c)) {
            customMappings.add(c);
        }
    }

    private String pop() {
        if (textElements.size() == 0) {
            return null;
        }

        String element = textElements.get(0);
        textElements.remove(0);
        popCounter++;
        return element;
    }

    /**
     * Parse method.
     * @param statement The statement to parse.
     * @return If succesful, a SQL statement.
     * @throws Exception Thrown in case of an error in the syntax.
     */
    public String parse(String statement) throws Exception {
        if (isNull(statement)) {
            throw new Exception("No statement.");
        }

        if ("help".equalsIgnoreCase(statement)) {
            throw new Exception(HELP);
        }

        /* Initializing.
        */
        originalStatement = statement;
        textElements = new ArrayList<>();

        for (String s : new StatementChopper(statement + ")")) {
            textElements.add(s);
        }

        Statement s = new Statement();
        s.setCompiler(this);
        s.setStatement(s);
        parse(s);
        s.execute();

        return s.getSql();
    }

    /* Parsing of a Statement requires a little different parsing then parsing a normal function.
    A statement is always of the form 'table function function' and a normal function is always of the form
    function(a,b,c) e.g. usage of commas.
    */
    private void parse(Function function) throws Exception {
        if(function instanceof Statement) {
            statements.add((Statement)function);
        } else {
            /* All Functions, except the Statement should always begin with an opening bracket.
            */
            if (!"(".equals(pop())) {
                syntaxError(ERR_EXP_OPENING_BRACKET);
            }
        }

        String languageElement=null;

        while (!function.isFinished()) {
            languageElement = pop();

            if(")".equals(languageElement)) {
                break;
            }

            if("'".equals(languageElement)) {
                languageElement = "'" + pop() + "'";

                if(!"'".equals(pop())) {
                    syntaxError(ERR_MISSING_END_QUOTE);
                }
            }

            if(function.getStep() == 1 && ")".equals(languageElement)) {
                syntaxError(ERR_FUNCTION_HAS_NO_ARGUMENTS);
            }

            Class<? extends Function> functionClass = getFunction(languageElement);

            if(functionClass != null) {
                if((!function.isFunctionExpected(functionClass) && !(functionClass == Ref.class && function.isColumn(function.getStep())))) {
                    syntaxError(ERR_CANNOT_USE_FUNCTION_AS_ARGUMENT_OF_FUNCTION, languageElement, getFSNameForFunction(function));
                }

                if(functionClass == Ref.class) {
                    Function ref = exec(functionClass, null);
                    function.process(((Ref)ref).getReference());
                } else {
                    if(Join.class.isAssignableFrom(function.getClass())) {
                        function.process(exec(functionClass,((Join)function).getJoinTable()));
                    } else if(function.getClass() == Statement.class){
                        function.process(exec(functionClass, ((Statement)function).getDriveTableOfQuery()));
                    } else {
                        function.process(exec(functionClass,null));
                    }
                }
            } else {
                if (function.isColumn(function.getStep())) {
                    languageElement = resolveColumn(languageElement);
                }

                function.process(languageElement);
            }

            /* Dont process comma when dealing with a statement.
            */
            if(function instanceof Statement) {
                continue;
            }

            languageElement = pop();

            if(")".equals(languageElement)) {
                if(function.expectAnotherArgument()) {
                    syntaxError(UNEXPECTED_END_OF_FUNCTION);
                }
                function.finished();
            } else if(!",".equals(languageElement)) {
                syntaxError(ERR_EXP_COMMA, languageElement);
            }
        }

        if(!")".equals(languageElement)) {
            syntaxError(ERR_FUNCTION_HAS_TOO_MANY_ARGUMENTS);
        }

        if(function instanceof Statement) {
            statements.remove(function);
        }
    }

    private String getFSNameForFunction(Function function) {
        for(Map.Entry<String, Class<? extends Function>> entry : functions.entrySet()) {
            if(entry.getValue() == function.getClass()) {
                return entry.getKey();
            }
        }
        return null;
    }

    private boolean isNull(String s) {
        return s == null || s.length() == 0;
    }

    public CustomMapping getCustomMapping(String table1, String column1, String table2) {
        CustomMapping defaultMapping = null;

        for (CustomMapping c : customMappings) {
            if (c.isDefaultMapping()) {
                defaultMapping = c;
            }

            if (c.toString().indexOf(table1) >= 0) {
                /* Mapping could be correct: check it precisly.
                Note: check both ways because we have no knowlegde of how the mapping was added to the compiler
                E.g. (table1|column1 , table2|column2) OR (table2|column2 , table1|column1)
                */
                if ((table1.equals(c.table1) && table2.equals(c.table2)) ||
                        (table1.equals(c.table2) && table2.equals(c.table1))) {
                    /* User can have only the drive column programmed and not the join column. If so, this column should be mentioned
                    in the custom mapping.
                    */
                    if (column1 != null) {
                        if (table1.equals(c.table1) && column1.equals(c.column1)) {
                            return c;
                        }

                        if (table1.equals(c.table2) && column1.equals(c.column2)) {
                            return c;
                        }
                    } else {
                        return c;
                    }
                }
            }
        }

        /* Found no custom mapping. Check if we can return default mapping. RULE: Compile does not override programmed columns names.
        */
        if (defaultMapping != null) {
            if (column1 != null) {
                if (defaultMapping.column1.equals(column1)) {
                    return defaultMapping;
                }
            } else {
                return defaultMapping;
            }
        }

        return null;
    }

    public void syntaxError(String format, Object... args) throws Exception {
        String error = String.format(format, args);

        String indicator = "", arrowLine = "";

        for (int idx = 0; idx < popCounter - 1; idx++) {
            indicator += " ";
            arrowLine += "-";
        }

        indicator += "|";
        arrowLine += "-";

        throw new Exception(String.format("Syntax error: %s\n%s\n%s\n%s",
                error,
                originalStatement,
                indicator,
                arrowLine));
    }

    private Statement getTopStatement() {
        return statements.get(statements.size() - 1);
    }

    public int aliasToNumber(String alias) {
        return Integer.parseInt(alias.substring(1));
    }

    public String toString() {
        return originalStatement;
    }

    public boolean isQuoted(String value) {
        return value.getBytes()[0] == '\''; // End quote is checked by the Function class.
    }

    /**
     * Method splits table.column format in table and/or column.
     * @param s The string.
     * @return Table and or column.
     * @throws Exception Thrown in case of an error.
     */
    public String[] splitTableColumn(String s) throws Exception {
        int idx = s.indexOf('.');

        if (idx == 0) {
            syntaxError(ERR_NULL_TABLE);
        }

        if (idx == s.length() - 1) {
            syntaxError(ERR_NULL_FIELD);
        }

        if (idx < 0) {
            return new String[] { s };
        }

        return new String[] { s.substring(0, idx), s.substring(idx + 1) };
    }

    /** Column can be noted as table.column or just a column. In the first case function translates it into alias.column
     * When value is a nummerical (for instance in function sum(1)), then argument is left untouched.
    */
    private String resolveColumn(String value) throws Exception {
        String[] tableAndColumn = splitTableColumn(value);

        if (isNummeric(tableAndColumn[0])) {
            return value;
        }

        for(String s : tableAndColumn) {
            checkTableOrColumnFormat(s);
        }

        if (tableAndColumn.length == 1) {
            return value;
        }

        return getTopStatement().getAlias(tableAndColumn[0]) + "." + tableAndColumn[1];
    }

    public void checkTableOrColumnFormat(String s) throws Exception {
        if(s != null && !isTableOrColumnName(s)) {
            syntaxError(ERR_WRONG_FORMAT_TABLE_OR_COLUMN_NAME, s);
        }
    }

    public boolean isTableOrColumnName(String value) {
        return TABLE_COLUMN_FORMAT.matcher(value).matches();
    }

    public boolean isNummeric(String s) {
        return NUMMERIC_FORMAT.matcher(s).matches();
    }

    private Function exec(Class<? extends Function> function, String driveTable) throws Exception {
        Constructor<? extends Function> cons = function.getDeclaredConstructor();
        Function instance = cons.newInstance();

        Statement statement = getTopStatement();

        instance.setCompiler(this);
        instance.setStatement(statement);

        if (instance instanceof Join) {
            ((Join) instance).setDriveTable(driveTable, statement.getAlias(driveTable));
        }

        parse(instance);
        instance.execute();

        return instance;
    }

    private class StatementChopper implements Iterable<String> {
        private final char[] chars;

        private int pointer = 0;

        private boolean quotedArea = false;

        public StatementChopper(String statement) {
            this.chars = statement.toCharArray();
        }

        @Override
        public Iterator<String> iterator() {
            return new Iterator<String>() {
                public boolean hasNext() {
                    /* Process white spaces.
                    */
                    for (; !quotedArea && pointer < chars.length && isWhiteSpace(chars[pointer]); pointer++) {
                    }

                    if (pointer < chars.length) {
                        return true;
                    }

                    return false;
                }

                public String next() {
                    if (isQuote(chars[pointer])) {
                        quotedArea = !quotedArea;
                        return "" + chars[pointer++];
                    }

                    if (isSpecialChar(chars[pointer])) {
                        return "" + chars[pointer++];
                    }

                    StringBuilder s = new StringBuilder();

                    /* When quoted, include whitespace/special chars.
                    */
                    if (quotedArea) {
                        for (; pointer < chars.length && !isQuote(chars[pointer]); pointer++) {
                            s.append(chars[pointer]);
                        }
                    } else {
                        for (;
                             pointer < chars.length &&
                                     !isSpecialChar(chars[pointer]) &&
                                     !isWhiteSpace(chars[pointer]) &&
                                     !isQuote(chars[pointer]); pointer++) {
                            s.append(chars[pointer]);
                        }
                    }

                    return s.toString();
                }

                @Override
                public void remove() {
                }

                private boolean isSpecialChar(char c) {
                    if (c == '(' || c == ')' || c == ',') {
                        return true;
                    }

                    return false;
                }

                /* Quote is also a special char, but requires unique processing.
                */
                private boolean isQuote(char c) {
                    return c == '\'' ? true : false;
                }

                private boolean isWhiteSpace(char c) {
                    if (c == ' ' || c == '\t' || c == '\n') {
                        return true;
                    }

                    return false;
                }
            };
        }
    }

    public class CustomMapping {
        private String table1, table2, column1, column2;

        public CustomMapping(String table1, String column1, String table2, String column2) throws Exception {
            this.table1 = table1;
            this.column1 = column1;
            this.table2 = table2;
            this.column2 = column2;

            assert (table1 != null && column1 != null && table2 != null && column2 != null);

            if (table1.length() == 0 && table2.length() == 0 && !column1.equals(column2)) {
                throw new Exception(ERR_DEFAULT_MAPPING_HAS_NO_EQUAL_COLUMNS);
            }
        }

        public boolean isDefaultMapping() {
            return table1.length() == 0 && table2.length() == 0 && column1.equals(column2);
        }

        public String getColumn(String table) {
            /* Mapping could be the default mapping.
            */
            if (table1.length() == 0) {
                return column1;
            }

            if (table1.equals(table)) {
                return column1;
            }

            if (table2.equals(table)) {
                return column2;
            }

            return null;
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (other instanceof CustomMapping) {
                CustomMapping c = (CustomMapping) other;
                return table1.equals(c.table1) && column1.equals(c.column1) && table2.equals(c.table2) && column2.equals(c.column2);
            }

            return false;
        }

        public int hashCode() {
            return 1;
        }

        public String toString() {
            return table1 + table2;
        }
    }
}
