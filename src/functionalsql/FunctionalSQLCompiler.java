package functionalsql;

import functionalsql.commands.*;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Functional sql compiler.
 */
public class FunctionalSQLCompiler {

    public static final String ERR_EXPECT_A_FUNCTION_CALL = "Expect a function call instead of (%s).";

    public static final String ERR_ORDER_BY_CLAUSE_ALREADY_DEFINED = "OrderBy clause already defined.";

    public static final String ERR_CANNOT_USE_FUNCTION_AS_ARGUMENT_OF_FUNCTION = "Cannot use function (%s) as argument of function (%s).";

    public static final String ERR_NEED_VALUE_WHEN_USING_OPERATOR_IN_FILTER = "Need a value to filter on when using operator in filter function.";

    public static final String ERR_ONLY_ONE_VALUE_WHEN_USING_OPERATOR_IN_FILTER = "Only one value when using operator in filter function (%s).";

    public static final String ERR_UNKNOWN_FUNCTION = "Unknown function (%s).";

    public static final String ERR_WRONG_FORMAT_TABLE_OR_COLUMN_NAME = "Wrong format table or column name: %s.";

    public static final String ERR_VALUE_SHOULD_BE_QUOTED = "Value (%s) should be quoted.";

    public static final String ERR_IF_TABLE_HAS_MULTIPLE_INSTANCES_USE_REF_FUNCTION = "If table has multiple instances, use the ref function (table=%s).";

    public static final String ERR_TABLE_REFERENCE_SHOULD_BE_EQUAL_OR_GREATER_THEN_ONE = "Reference should be equal or greater than one (%s).";

    public static final String ERR_TABLE_REFERENCE_IS_NOT_CORRECT = "Table reference (%s) is not correct.";

    public static final String ERR_REFERING_TO_A_NON_EXISTING_TABLE = "Refering to a non existing table (%s).";

    public static final String ERR_TABLE_REFERENCE_SHOULD_BE_NUMMERICAL = "Table reference should be nummerical (%s).";

    public static final String ERR_UNEXPECTED_END_OF_FUNCTION = "Unexpected end of function.";

    public static final String ERR_JOIN_SHOULD_FOLLOW_JOIN = "A join can only be followed by another join. Instead found '%s'.";

    public static final String ERR_EXP_OPENING_BRACKET = "Expected opening bracket.";

    public static final String ERR_MISSING_END_QUOTE = "Missing end quote.";

    public static final String ERR_NULL_TABLE = "Null table.";

    public static final String ERR_NULL_FIELD = "Null field.";

    public static final String ERR_FUNCTION_HAS_NO_ARGUMENTS = "Function has no arguments.";

    public static final String ERR_SELECT_ALREADY_DEFINED = "Select clause (%s) is already defined.";

    public static final String ERR_NO_JOIN_COLUMNS_DEFINED_AND_NO_RELATION_FOUND = "No join columns defined in statement and no relation found.";

    public static final String ERR_DEFAULT_RELATION_HAS_NO_EQUAL_COLUMNS = "Default relation has no equal columns.";

    public static final String ERR_FUNCTION_HAS_TOO_MANY_ARGUMENTS = "Function has to many arguments.";

    private Map<String, Class<? extends Function>> functions = new HashMap<>();

    private List<Statement> statements = new ArrayList<>();

    private List<String> textElements;

    private List<Relation> relations = new ArrayList<Relation>();

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

    public String parse(String statement) throws Exception {
        if (isNull(statement)) {
            throw new Exception("No statement.");
        }

        /* Initializing.
        */
        originalStatement = statement;
        textElements = new ArrayList<>();

        for (String s : new StatementChopper(statement)) {
            textElements.add(s);
        }

        Statement s = new Statement();
        s.setCompiler(this);
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

        String token;

        do {
            token = pop();

            if(token == null || ")".equals(token)) {
                break;
            }

            if("'".equals(token)) {
                token = "'" + pop() + "'";

                if(!"'".equals(pop())) {
                    syntaxError(ERR_MISSING_END_QUOTE);
                }
            }

            Class<? extends Function> functionClass = getFunction(token);

            if(functionClass != null) {
                if(functionClass == Ref.class && !function.expectTableOrColumn()) {
                    syntaxError(ERR_CANNOT_USE_FUNCTION_AS_ARGUMENT_OF_FUNCTION, token, getFSNameForFunction(function));
                }

                if(functionClass == Ref.class) {
                    function.process(((Ref)exec(functionClass, null)).getReference());
                } else if(Join.class.isAssignableFrom(function.getClass())) {
                    function.process(exec(functionClass,((Join)function).getJoinTable()));
                } else if(function.getClass() == Statement.class){
                    function.process(exec(functionClass, ((Statement)function).getDriveTableOfQuery()));
                } else {
                    function.process(exec(functionClass,null));
                }
            } else {
                if (function.expectTableOrColumn()) {
                    token = resolveColumn(token);
                }

                function.process(token);
            }

            /* Dont process comma when dealing with a statement.
            */
            if(function instanceof Statement) {
                continue;
            }

            token = pop();  //Expect ',' or ')'.

            switch(token != null ? token : "") {
                case ",":
                    if(function.isFinished()) {
                        syntaxError(ERR_FUNCTION_HAS_TOO_MANY_ARGUMENTS);
                    }
                    break;
                case ")": break;
                default: syntaxError(ERR_UNEXPECTED_END_OF_FUNCTION);

            }
        } while(!")".equals(token));

        if(function.expectArgument() || (token == null && !(function instanceof Statement))) {
            syntaxError(ERR_UNEXPECTED_END_OF_FUNCTION);
        }

        if(function instanceof Statement) {
            statements.remove(function);
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

    public String getFSNameForFunction(Function function) {
        return functions.entrySet().stream().filter(e -> e.getValue() == function.getClass()).findFirst().map(Map.Entry::getKey).get();
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

    public void addCustomFunction(String name, Class<? extends Function> function) {
        functions.put(name, function);
    }

    public void addRelation(String table1, String column1, String table2, String column2) throws Exception {
        Relation relation = new Relation(table1, column1, table2, column2);

        if (!relations.contains(relation)) {
            relations.add(relation);
        }
    }

    public void addDefaultRelation(String column1, String column2) throws Exception {
        Relation relation = new Relation(column1, column2);
        if(!relations.contains(relation)) {
            relations.add(relation);
        }
    }

    private boolean isNull(String s) {
        return s == null || s.length() == 0;
    }

    public Relation getRelation(String table1, String column1, String table2) {
        Relation defaultRelation = null;

        for (Relation relation : relations) {
            if (relation.isDefault()) {
                defaultRelation = relation;
            }

            if(relation.matches(table1, column1, table2)) {
                return relation;
            }
        }

        /* Found no relation. Check if we can return the default relation. RULE: Compiler does not override programmed columns names.
        */
        if (defaultRelation != null) {
            if (column1 != null) {
                if (defaultRelation.defaultRelationMatches(column1)) {
                    return defaultRelation;
                }
            } else {
                return defaultRelation;
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

    public Statement getStatement() {
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
    public String resolveColumn(String value) throws Exception {
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

        return getStatement().getAlias(tableAndColumn[0]) + "." + tableAndColumn[1];
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

        instance.setCompiler(this);

        if (instance instanceof Join) {
            ((Join) instance).setDriveTable(driveTable, getStatement().getAlias(driveTable));
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
}
