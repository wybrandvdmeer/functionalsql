package functionalsql;

import functionalsql.commands.*;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Functional sql compiler.
 */
public class FunctionalSQLCompiler {

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

	private boolean printSQL = false;
	private List<String> textElements;

	private List<CustomMapping> customMappings = new ArrayList<CustomMapping>();

	private String originalStatement;
	private int popCounter = 0;

	private final static Pattern TABLE_COLUMN_FORMAT=Pattern.compile("[a-zA-Z0-9_]*");

	private final static Pattern NUMMERIC_FORMAT=Pattern.compile("[-]*[0-9.]*");

	/**
	 * Constructor.
	 */
	public FunctionalSQLCompiler() {
		functions.put("join", Join.class);
        functions.put("innerjoin", InnerJoin.class);
		functions.put("leftjoin", LeftJoin.class);
		functions.put("rightjoin", RightJoin.class);
		functions.put("fulljoin", Filter.FullJoin.class);
		functions.put("printSQL", null);
		functions.put("print", Print.class);
		functions.put("like", Like.class);
		functions.put("group", Filter.Group.class);
		functions.put("asc", Order.class);
		functions.put("desc", Desc.class);
		functions.put("sum", Sum.class);
		functions.put("(", Statement.class);
		functions.put("distinct", Distinct.class);
		functions.put("min", Min.class);
		functions.put("max", Max.class);
		functions.put("filter", Filter.class);
		functions.put("filterdate", Desc.FilterDate.class);
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
		printSQL = false;
		originalStatement = statement;
		textElements = new ArrayList<>();

		/* Chop statement into language elements. The ')' token is put at the end of the statement, because a statement should
		always be closed by a closing bracket. The opening bracket is implicitly added by executing the statement method.
		*/
		for (String s : new StatementChopper(statement + ")")) {
			textElements.add(s);
		}

		Statement s = new Statement();
		parse(s);

		if (printSQL) {
			throw new Exception(s.getSql());
		}

		return s.getSql();
	}

	public void parse(Statement statement) throws Exception {

        /* Administrate this statement to the list of statements which functions as a stack.
		*/
		statements.add(statement);

		String token = null;

		/* Token can be either a '(' (which is a new statement), a ')' (which is the end of the statement) or the drive-table.
		*/
		while (textElements.size() > 0) {
			token = textElements.get(0);
			textElements.remove(0);

			if (functions.containsKey(token)) {
				Class<? extends Function> function = functions.get(token);

				if (function == null) {
					printSQL = true; /* PrintSQL can popup anytime. */
				} else {
					Function instance = exec(statement, function, getDriveTableOfQuery());

					if (instance instanceof Statement) {
						String nestedQuery = ((Statement) instance).getSql();

						if(statement.isFullSelect()) {
							statement.copyStatement(((Statement)instance));
						} else {
							statement.fromClauses.add(String.format("(%s) %s", nestedQuery, statement.getAlias(nestedQuery)));
						}
					}
				}
			} else {
				/* If token is not a function, then it can either be a ')' which is the end of the function or the drive-table.
				Note: The openings bracket '(' is a member of the function map.
				*/
				if (")".equals(token)) {
					break;
				}

                /* Get the drive table of the query.
				*/
				if (statement.getTable() == null) {
					statement.setTable(token);

                    /* Put drive table in the FROM clause.
					NOTE: statement below must be placed here and not in compileSQL(). WHY IS THIS???????????????????
					*/
					statement.fromClauses.add(String.format("%s %s", statement.getTable(), statement.getAlias(statement.getTable())));
				} else {
					syntaxError(ERR_UNKNOWN_FUNCTION, token);
				}
			}
		}

		if (!")".equals(token)) {
			syntaxError(ERR_UNEXPECTED_END_OF_STATEMENT);
		}

		statement.compileSQL();

		/* Remove statement from the stack.
		*/
		statements.remove(this);
	}

    void parse(Function function) throws Exception {

        /* Function should always begin with an opening bracket.
		*/
        if (!"(".equals(pop())) {
            syntaxError(ERR_EXP_OPENING_BRACKET);
        }

        String languageElement=null;

        while (!function.isFinished()) {

            languageElement = pop();

            if("'".equals(languageElement)) {
                languageElement = "'" + pop() + "'";

                if(!"'".equals(pop())) {
                    syntaxError(ERR_MISSING_END_QUOTE);
                }
            }

            /* If argument is denoted as a table/column argument, do some table/column processing.
            The processing consists of 2 things:
            1>
              Process the ref function. The ref function can be used to refer tables who have multiple instances in the from clause.
              For instance SELECT t2.field FROM a t0 , b t1 , a t2 WHERE ... In this case the t2.field in the SELECT clause is denoted as
              print( ref( a , 2 ) ). The trick is now to resolve the correct alias (t1).
              This functions opposite equivalent is newtable() which is used to add an other instance of the same table to the query.
            2>
              Columns can be noted as table.column.
            */
            if (function.isColumn(function.getStep())) {
                Class<? extends Function> functionClass = getFunction(languageElement);

                if(function != null && Ref.class == functionClass) {
                    Function ref = exec(getTopStatement(), functionClass, null);
                    languageElement = ((Ref)ref).getReference();
                } else {
                    languageElement = resolveColumn(languageElement);
                }
            }

            if(function.getStep() == 1 && ")".equals(languageElement)) {
                syntaxError(ERR_FUNCTION_HAS_NO_ARGUMENTS);
            }

            function.process(languageElement);

            /* Argument is processed. Process now the element which follows the argument. This element can be a ',' in case the function continues
			or a ')' in case the function closes.
			*/
            languageElement = pop();

            if(")".equals(languageElement)) {
                if(function.expectAnotherArgument()) {
                    syntaxError(UNEXPECTED_END_OF_FUNCTION);
                }
                break;
            } else {
                if(!",".equals(languageElement)) {
                    syntaxError(ERR_EXP_COMMA, languageElement);
                }
            }
        }

        if(!")".equals(languageElement)) {
            syntaxError(ERR_FUNCTION_HAS_TOO_MANY_ARGUMENTS);
        }
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
		throw new Exception(String.format("Syntax error: %s\n", error));
	}

	private Statement getTopStatement() {
		return statements.get(statements.size() - 1);
	}

	/**
	 * Method gets the drive table of the query.
	 *
	 * @return The drive table.
	 */
	protected String getDriveTableOfQuery() {
		/* Search the drive table of the query. Drive table has always alias t0.
		*/
		for (Map.Entry<String, String> entry : getTopStatement().aliases.entrySet()) {
			if (entry.getKey().equals("t0")) {
				return entry.getValue();
			}
		}

		return null;
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

    public Function exec(Statement statement, Class<? extends Function> function, String driveTable) throws Exception {
		/* If you want to instantiate an inner class, you have to search for the constructor which takes the super class as
		an argument.
		*/
        Constructor<? extends Function> cons = function.getDeclaredConstructor();
        Function instance = cons.newInstance();

        instance.setCompiler(this);
        instance.setStatement(statement);

        if (instance instanceof Join) {
            ((Join) instance).setDriveTable(driveTable, statement.getAlias(driveTable));
        }

        if(instance instanceof Statement) {
            parse((Statement)instance);
        } else {
            parse(instance);
            instance.execute();
        }

        return instance;
    }

	/**
	 * Syntax: notfilter( column , value1 , value2 , ... )
	 */
	private class NotFilter extends Filter {
		public NotFilter() { super(false); }
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
