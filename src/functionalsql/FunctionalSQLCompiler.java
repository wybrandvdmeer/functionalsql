package functionalsql;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Functional sql compiler.
 */
public class FunctionalSQLCompiler {
	/** Error. */
	public static final String ERR_UNEXPECTED_END_OF_STATEMENT = "Unexpected end of statement.";

	/** Error. */
	public static final String ERR_IF_TABLE_HAS_MULTIPLE_INSTANCES_USE_REF_FUNCTION = "If table has multiple instances, use the ref function (table=%s).";

	/** Error. */
	public static final String ERR_TABLE_REFERENCE_SHOULD_BE_EQUAL_OR_GREATER_THEN_ONE = "Reference should be equal or greater than one (%s).";

	/** Error. */
	public static final String ERR_TABLE_REFERENCE_IS_NOT_CORRECT = "Table reference (%s) is not correct.";

	/** Error. */
	public static final String ERR_REFERING_TO_A_NON_EXISTING_TABLE = "Refering to a non existing table (%s).";

	/** Error. */
	public static final String ERR_TABLE_REFERENCE_SHOULD_BE_NUMMERICAL = "Table reference should be nummerical (%s).";

	/** Error. */
	public static final String ERR_UNEXPECTED_END = "Unexpected end of function.";

	/** Error. */
	public static final String ERR_JOIN_SHOULD_FOLLOW_JOIN = "A join can only be followed by another join. Instead found '%s'.";

	/** Error. */
	public static final String ERR_EXP_OPENING_BRACKET = "Expected opening bracket.";

	/** Error. */
	public static final String ERR_EXP_COMMA = "Expected ','.";

	/** Error. */
	public static final String ERR_MISSING_END_QUOTE = "Missing end quote.";

	/** Error. */
	public static final String ERR_RESTRICTED_KEY_WORD_USE_QUOTES = "Restricted keyword: %s. Use quotes.";

	/** Error. */
	public static final String ERR_NULL_TABLE = "Null table.";

	/** Error. */
	public static final String ERR_NULL_FIELD = "Null field.";

	/** Error. */
	public static final String ERR_NO_WILD_CARD_IN_PATTERN = "Pattern (%s) contains no wildcard.";

	/** Error. */
	public static final String ERR_FUNCTION_HAS_NO_ARGUMENTS = "Function has no arguments.";

	/** Error. */
	public static final String ERR_SELECT_ALREADY_DEFINED = "Select clause (%s) is already defined.";

	/** Error. */
	public static final String ERR_NO_JOIN_COLUMNS_DEFINED_AND_NO_CUSTOM_MAPPING_PRESENT = "No join columns defined in statement and no custom mapping found.";

	/** Error. */
	public static final String ERR_DEFAULT_MAPPING_HAS_NO_EQUAL_COLUMNS = "Default mapping has no equal columns.";

	/** Error. */
	public static final String ERR_UNKNOWN_OPERATOR = "Unknown operator %s.";

	/** Error. */
	public static final String ERR_FUNCTION_HAS_TOO_MANY_ARGUMENTS = "Function has to many arguments.";

	private static String HELP = "<table> function1 function2 ... ";

	private Map<String, Class<? extends Function>> functions = new HashMap<String, Class<? extends Function>>();

	private List<Statement> statements = new ArrayList<>();

	private boolean printSQL = false;
	private List<String> textElements;

	private List<CustomMapping> customMappings = new ArrayList<CustomMapping>();

	private String originalStatement;
	private int popCounter = 0;

	private FunctionalSQLCompiler compiler;

	/**
	 * Constructor.
	 */
	public FunctionalSQLCompiler() {
		functions.put("join", Join.class);
		functions.put("printSQL", null);
		functions.put("print", Print.class);
		functions.put("like", Like.class);
		functions.put("group", Group.class);
		functions.put("asc", Order.class);
		functions.put("desc", Desc.class);
		functions.put("sum", Sum.class);
		functions.put("(", Statement.class);
		functions.put("distinct", Distinct.class);
		functions.put("distinct-constant", DistinctConstant.class);
		functions.put("min", Min.class);
		functions.put("max", Max.class);
		functions.put("filter", Filter.class);
		functions.put("filterdate", FilterDate.class);
		functions.put("stringfilter", StringFilter.class);
		functions.put("notfilter", NotFilter.class);
		functions.put("stringnotfilter", StringNotFilter.class);
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
		textElements = new ArrayList<String>();
		compiler = this;

		/* Chop statement into language elements. The ')' token is put at the end of the statement, because a statement should
		always be closed by a closing bracket. The opening bracket is implicitly added by executing the statement method.
		*/
		for (String s : new StatementChopper(statement + ")")) {
			textElements.add(s);
		}

		Statement s = new Statement();
		s.execute();

		if (printSQL) {
			throw new Exception(s.sql);
		}

		return s.sql;
	}

	private boolean isNull(String s) {
		return s == null || s.length() == 0;
	}

	/**
	 * FilterDate function.
	 *
	 * @param column The column to filter.
	 * @param value The value to filter.
	 * @param operator Filter operator.
	 *
	 * @throws Exception Thrown in case of an error.
	 */
	protected void filterDate(String column, String value, String operator) throws Exception {
		if (!"=".equals(operator) &&
		        !"<=".equals(operator) &&
		        !">=".equals(operator) &&
		        !"<".equals(operator) &&
		        !">".equals(operator)) {
			syntaxError(ERR_UNKNOWN_OPERATOR, operator);
		}

		getTopStatement().filterClauses.add(String.format("%s %s convert( datetime , '%s' )", column, operator, value));
	}

	/**
	 * Report function (SUM, MIN and MAX).
	 *
	 * @param reportFunction The report function.
	 * @param summationArgument Argument for the report function..
	 * @param columns Columns on which to report.
	 *
	 * @throws Exception Thrown in case of an error.
	 */
	protected void report(String reportFunction, String summationArgument, List<String> columns) throws Exception {
		if (getTopStatement().clauses[0] != null) {
			syntaxError(ERR_SELECT_ALREADY_DEFINED, getTopStatement().clauses[0]);
		}

		/* If anything else, then it is a program error.
		*/
		assert ("SUM".equals(reportFunction) || "MAX".equals(reportFunction) || "MIN".equals(reportFunction));

		getTopStatement().clauses[0] = "SELECT";

		if (columns.size() > 0) {
			getTopStatement().clauses[2] = "GROUP BY";
		}

		/* Expand the select and group clause.
		*/
		for (int idx = 0; idx < columns.size(); idx++) {
			getTopStatement().clauses[0] += " " + columns.get(idx);
			getTopStatement().clauses[2] += " " + columns.get(idx);

			if (idx < columns.size() - 1) {
				getTopStatement().clauses[0] += ",";
				getTopStatement().clauses[2] += ",";
			}
		}

		/* Add the summation function to the select clause.
		*/
		getTopStatement().clauses[0] += String.format("%s %s( %s )", columns.size() > 0 ? "," : "", reportFunction,
		                                              summationArgument);
	}

	/**
	 * Order function.
	 * @param asc True if order is ascending. Otherwise false.
	 * @param columns The columns on which to order.
	 */
	protected void order(boolean asc, List<String> columns) {
		if (getTopStatement().clauses[1] == null) {
			getTopStatement().clauses[1] = "ORDER BY";
		}

		/* Expand the order by clause.
		*/
		for (int idx = 0; idx < columns.size(); idx++) {
			getTopStatement().clauses[1] += " " + columns.get(idx);

			if (idx < columns.size() - 1) {
				getTopStatement().clauses[1] += ",";
			}
		}

		getTopStatement().clauses[1] += asc ? " ASC" : " DESC";
	}

	/**
	 * Group function.
	 *
	 * @param columns The columns on which to order.
	 *
	 * @throws Exception Thrown in case of an error.
	 */
	protected void group(List<String> columns) throws Exception {
		if (getTopStatement().clauses[0] != null) {
			syntaxError(ERR_SELECT_ALREADY_DEFINED, getTopStatement().clauses[0]);
		}

		getTopStatement().clauses[0] = "SELECT";
		getTopStatement().clauses[2] = "GROUP BY";

		/* Expand the select and group clause.
		*/
		for (int idx = 0; idx < columns.size(); idx++) {
			getTopStatement().clauses[0] += " " + columns.get(idx);
			getTopStatement().clauses[2] += " " + columns.get(idx);

			if (idx < columns.size() - 1) {
				getTopStatement().clauses[0] += ",";
				getTopStatement().clauses[2] += ",";
			}
		}
	}

	/**
	 * Function creates distinct clause.
	 * @param constants The constants.
	 * @throws Exception Thrown in case of an error.
	 */
	protected void distinctConstants(List<String> constants) throws Exception {
		List<String> quotedConstants = new ArrayList<>();

		constants.stream().forEach(c -> quotedConstants.add("'" + c + "'"));

		distinct(quotedConstants);
	}

	/**
	 * Function creates a distinct select clause.
	 * @param columns The columns.
	 * @throws Exception Thrown in case of an error.
	 */
	protected void distinct(List<String> columns) throws Exception {
		if (columns.size() == 0) {
			return;
		}

		if (getTopStatement().clauses[0] != null) {
			if (!getTopStatement().clauses[0].startsWith("SELECT DISTINCT")) {
				syntaxError(ERR_SELECT_ALREADY_DEFINED, getTopStatement().clauses[0]);
			}

			getTopStatement().clauses[0] += ",";
		} else {
			getTopStatement().clauses[0] = "SELECT DISTINCT";
		}

		/* Expand the select clause.
		*/
		for (int idx = 0; idx < columns.size(); idx++) {
			getTopStatement().clauses[0] += " " + columns.get(idx);

			if (idx < columns.size() - 1) {
				getTopStatement().clauses[0] += ",";
			}
		}
	}

	/**
	 * Filter function.
	 *
	 * @param column The column on which to filter.
	 * @param values The values on which to filter.
	 */
	protected void filter(String column, List<String> values) {
		filter(column, values, true, false);
	}

	/**
	 * Filter function.
	 *
	 * @param column The column on which to filter.
	 * @param values The values on which to filter.
	 * @param inclusive If filter should be inclusive or exclusive.
	 * @param stringFilter If true then a string filter is forced, e.g. using quoted around the value.
	 */
	protected void filter(String column, List<String> values, boolean inclusive, boolean stringFilter) {
		/* If list is null or has no values, it is a program error.
		*/
		assert (values != null && values.size() > 0);

		boolean searchArgIsString = false;

		/* RULE: If user filters on only nummerical values, the column will be treated as a nummerical, e.g. not using quotes.
		If one or more values have characters, then quotes will be used.
		*/
		for (String value : values) {
			if (!isNummerical(value)) {
				searchArgIsString = true;
			}
		}

		/* StringFilter is true, overrides everything.
		*/
		if (stringFilter) {
			searchArgIsString = true;
		}

		String filterClause;

		/* Expand the where clause
		*/
		if (values.size() == 0) {
			filterClause = String.format("%s %s NULL", column, inclusive ? "IS" : "IS NOT");
		} else if (values.size() == 1) {
			filterClause = String.format("%s %s %s%s%s",
			                             column,
			                             inclusive ? "=" : "!=",
			                             searchArgIsString ? "'" : "",
			                             values.get(0),
			                             searchArgIsString ? "'" : "");
		} else {
			String argumentListINFunction = inclusive ? " IN (" : " NOT IN (";

			for (int idx = 0; idx < values.size(); idx++) {
				argumentListINFunction += String.format(" %s%s%s",
				                                        searchArgIsString ? "'" : "",
				                                        values.get(idx),
				                                        searchArgIsString ? "'" : "");

				if (idx < values.size() - 1) {
					argumentListINFunction += ",";
				}
			}

			argumentListINFunction += " )";

			filterClause = String.format("%s%s", column, argumentListINFunction);
		}

		if (!getTopStatement().filterClauses.contains(filterClause)) {
			getTopStatement().filterClauses.add(filterClause);
		}
	}

	private CustomMapping getCustomMapping(String table1, String column1, String table2) {
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

	/**
	 * Method throws an error as exception.
	 *
	 * @param format Format.
	 * @param args Arguments.
	 *
	 * @throws Exception The error.
	 */
	protected void syntaxError(String format, Object... args) throws Exception {
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

	/**
	 * Method gets the alias of a table.
	 *
	 * @param table The table for which the alias is requested.
	 *
	 * @return Alias.
	 *
	 */
	protected String getAlias(String table) throws Exception {
		return getAlias(table, false);
	}

	private Statement getTopStatement() {
		return statements.get(statements.size() - 1);
	}

	/**
	 * Method gets the alias of a table.
	 *
	 * @param table The table for which the alias is requested.
	 * @param forceNewAlias If true a new alias is created, no matter if the table is already in the alias administration.
	 *
	 * @return Alias.
	 *
	 * @throws Exception In case of an error.
	 */
	protected String getAlias(String table, boolean forceNewAlias) throws Exception {
		if (table == null) {
			syntaxError(ERR_NULL_TABLE);
		}

		String alias = null;

		if (!forceNewAlias) {
			for (Entry<String, String> entry : getTopStatement().aliases.entrySet()) {
				if (entry.getValue().equals(table)) {
					if (alias != null) {
						syntaxError(ERR_IF_TABLE_HAS_MULTIPLE_INSTANCES_USE_REF_FUNCTION, table);
					}

					alias = entry.getKey();
				}
			}

			if (alias != null) {
				return alias;
			}
		}

		alias = "t" + getTopStatement().aliases.keySet().size();

		getTopStatement().aliases.put(alias, table);
		return alias;
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

	/**
	 * To join the tables this function must use the custom-mappings.
	 */
	protected void join(String driveTable, String joinTable) throws Exception {
		join(driveTable, getAlias(driveTable), null, joinTable, getAlias(joinTable), null);
	}

	/**
	 * Join method.
	 *
	 * @param driveTable The drive table.
	 * @param aliasDriveTable alias drive table.
	 * @param joinTable Join table.
	 * @param aliasJoinTable Alias join table.
	 * @throws Exception Thrown in case of an error.
	 */
	protected void join(String driveTable,
	                    String aliasDriveTable,
	                    String joinTable,
	                    String aliasJoinTable) throws Exception {
		join(driveTable, aliasDriveTable, null, joinTable, aliasJoinTable, null);
	}

	/**
	 * Function can be used by macros who want create their own joins.
	 *
	 * @param driveTable The drive table.
	 * @param joinColumnDriveTable Join column drive table.
	 * @param joinTable The join table.
	 * @param joinColumnJoinTable Join column join table.
	 */
	protected void join(String driveTable,
	                    String aliasDriveTable,
	                    String joinColumnDriveTable,
	                    String joinTable,
	                    String aliasJoinTable,
	                    String joinColumnJoinTable) throws Exception {
		/* Expand the from clause with the join table if necessary.
		*/
		String fromClause = String.format("%s %s", joinTable, aliasJoinTable);

		if (!getTopStatement().fromClauses.contains(fromClause)) {
			getTopStatement().fromClauses.add(fromClause);
		}

		/* Syntax:
		join( table )
		join( table, driveTableColumn )
		join( table, driveTableColumn , joinTableColumn )

		RULE: If joinTableColumn is present, then also driveTableColumn is present.
		*/
		if (joinColumnJoinTable == null) {
			CustomMapping c = getCustomMapping(driveTable, joinColumnDriveTable, joinTable);

			/* If join fields are not programmed and there are also no cumstom mappings, then we cannot define the join.
			*/
			if (c == null) {
				syntaxError(ERR_NO_JOIN_COLUMNS_DEFINED_AND_NO_CUSTOM_MAPPING_PRESENT);
			}

			joinColumnJoinTable = c.getColumn(joinTable);

			/* If joinFieldJoinTable should be null at this point, it is a program error.
			*/
			assert (joinColumnJoinTable != null);

			if (joinColumnDriveTable == null) {
				joinColumnDriveTable = c.getColumn(driveTable);
			}

			/* If joinFieldDriveTable should be null at this point, it is a program error.
			*/
			assert (joinColumnDriveTable != null);
		}

		/* Expand the where clause if necessary.
		*/
		String whereClause;

		if (aliasToNumber(aliasDriveTable) < aliasToNumber(aliasJoinTable)) {
			whereClause = String.format("%s.%s = %s.%s",
			                            aliasDriveTable,
			                            joinColumnDriveTable,
			                            aliasJoinTable,
			                            joinColumnJoinTable);
		} else {
			whereClause = String.format("%s.%s = %s.%s",
			                            aliasJoinTable,
			                            joinColumnJoinTable,
			                            aliasDriveTable,
			                            joinColumnDriveTable);
		}

		if (!getTopStatement().joinClauses.contains(whereClause)) {
			getTopStatement().joinClauses.add(whereClause);
		}
	}

	private int aliasToNumber(String alias) {
		return Integer.parseInt(alias.substring(1));
	}

	/**
	 * @see java.lang.String#toString()
	 */
	public String toString() {
		return originalStatement;
	}

	/**
	 * Print function.
	 *
	 * @param columns The columns which to print.
	 * @throws Exception Thrown in case of an exception.
	 */
	protected void print(List<String> columns) throws Exception {
		if (getTopStatement().clauses[0] != null) {
			syntaxError(ERR_SELECT_ALREADY_DEFINED, getTopStatement().clauses[0]);
		}

		getTopStatement().clauses[0] = "SELECT";

		/* Expand the select clause.
		*/
		for (int idx = 0; idx < columns.size(); idx++) {
			String column = columns.get(idx);

			/* Check if argument is a table. If so, all fields of table are selected.
			Argument can also be a reference when the table was referred with the ref( table, occ ) function.
			*/
			if (isTable(column)) {
				column = getAlias(column) + ".*";
			} else if (isAlias(column)) {
				column = column + ".*";
			}

			getTopStatement().clauses[0] += " " + column;

			if (idx < columns.size() - 1) {
				getTopStatement().clauses[0] += ",";
			}
		}
	}

	/**
	 * Like function.
	 *
	 * @param column The column for the like filter.
	 * @param value The value for the like filter.
	 * @throws Exception Thrown in case of an error.
	 */
	protected void like(String column, String value) throws Exception {
		/* Check if pattern contains a wild-card. If not, throw an error.
		Note: Strictly speaking is this error check not necessary.
		*/
		if (value.indexOf('%') < 0) {
			syntaxError(ERR_NO_WILD_CARD_IN_PATTERN, value);
		}

		getTopStatement().filterClauses.add(String.format("%s LIKE '%s'", column, value));
	}

	/**
	 * Method tries to find the alias for the table.
	 * @param tableColumn Table and an optional column.
	 * @param reference The nummerical reference (1..n).
	 * @return If found the alias. Otherwise an exception is thrown.
	 * @throws Exception In case of an error or alias is not found.
	 */
	protected String ref(String tableColumn, String reference) throws Exception {
		String[] tableAndColumn = splitTableColumn(tableColumn);

		/* If ref is programmed, the referenced table should already be processed.
		 */
		if (!isTable(tableAndColumn[0])) {
			syntaxError(ERR_REFERING_TO_A_NON_EXISTING_TABLE, tableAndColumn[0]);
		}

		if (!isNummerical(reference)) {
			syntaxError(ERR_TABLE_REFERENCE_SHOULD_BE_NUMMERICAL, reference);
		}

		if (Integer.parseInt(reference) < 1) {
			syntaxError(ERR_TABLE_REFERENCE_SHOULD_BE_EQUAL_OR_GREATER_THEN_ONE, reference);
		}

		int idx = 0;
		String alias = null;

		for (Entry<String, String> entry : getTopStatement().aliases.entrySet()) {

			if (!tableAndColumn[0].equals(entry.getValue())) {
				continue;
			}

			if (idx == Integer.parseInt(reference) - 1) {
				alias = entry.getKey();
			}

			idx++;
		}

		if (alias == null) {
			syntaxError(ERR_TABLE_REFERENCE_IS_NOT_CORRECT, reference);
		}

		return alias + (tableAndColumn.length > 1 ? ("." + tableAndColumn[1]) : ""); // E.g. t0 or t0.column
	}

	/**
	 * Method determines if string is a table.
	 * @param s The string.
	 * @return True is string is table. Otherwise false.
	 */
	protected boolean isTable(String s) {
		return getTopStatement().aliases.values().contains(s);
	}

	/**
	 * Method determines if string is alias.
	 * @param s The string.
	 * @return True if string is alias. Otherwise false.
	 */
	protected boolean isAlias(String s) {
		return getTopStatement().aliases.keySet().contains(s);
	}

	/**
	 * Method splits table.column format in table and/or column.
	 * @param s The string.
	 * @return Table and or column.
	 * @throws Exception Thrown in case of an error.
	 */
	protected String[] splitTableColumn(String s) throws Exception {
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
	*/
	private String resolveColumn(String s) throws Exception {
		String[] tableAndColumn = splitTableColumn(s);

		/* If table turns out to be a nummerical, then compiler assumes that argument is a nummerical with a decimal point.
		*/
		if (isNummerical(tableAndColumn[0])) {
			return s;
		}

		if (tableAndColumn.length == 1) {
			return s;
		}

		return getAlias(tableAndColumn[0]) + "." + tableAndColumn[1];
	}

	/**
	 * Method determines if string is a nummerical.
	 * @param s The string.
	 * @return True is string is nummerical. Otherwise false.
	 */
	protected boolean isNummerical(String s) {
		for (int idx = 0; idx < s.getBytes().length; idx++) {
			int c = s.getBytes()[idx];

			/* It can be a negative number: which is still a number. */
			if (idx == 0 && c == '-') {
				continue;
			}

			if (!Character.isDigit(c) && c != '.') {
				return false;
			}
		}

		return true;
	}

	private class Statement extends Function {
		public String[] clauses = new String[3]; // Contains SELECT, FROM, ORDER AND GROUP.
		public List<String> fromClauses = new ArrayList<String>();
		public List<String> joinClauses = new ArrayList<String>();
		public List<String> filterClauses = new ArrayList<String>();
		public Map<String, String> aliases = new HashMap<String, String>(); //alias, table

		private String sql;

		protected void parse() throws Exception {
			/* Administrate this statement to the list of statements which functions as a stack.
			*/
			statements.add(this);

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
						/* If you want to instantiate an inner class, you have to search for the constructor which takes the super class as
						an argument.
						*/
						Constructor<? extends Function> cons = function.getDeclaredConstructor(function.getEnclosingClass());
						Function instance = cons.newInstance(new Object[] { compiler });

						if (instance instanceof Join) {
							((Join) instance).setDriveTable(getDriveTableOfQuery(), getAlias(getDriveTableOfQuery()));
						}

						instance.execute();

						if (instance instanceof Statement) {
							String nestedQuery = ((Statement) instance).sql;

							if(isFullSelect()) {
                                copyStatement(((Statement)instance));
							} else {
								fromClauses.add(String.format("(%s) %s", nestedQuery, getAlias(nestedQuery)));
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
					if (table == null) {
						table = token;

						/* Put drive table in the FROM clause.
						NOTE: statement below must be placed here and not in compileSQL(). WHY IS THIS???????????????????
						*/
						fromClauses.add(String.format("%s %s", table, getAlias(table)));
					} else {
						syntaxError("Unknown function: " + token);
					}
				}
			}

			if (!")".equals(token)) {
				syntaxError(ERR_UNEXPECTED_END_OF_STATEMENT);
			}

			sql = compileSQL();

			/* Remove statement from the stack.
			*/
			statements.remove(this);
		}

		private String compileSQL() throws Exception {
			if (clauses[0] == null) {
				clauses[0] = "SELECT *";
			}

			String sql = String.format("%s ", clauses[0]); // SELECT ...

			Collections.sort(fromClauses, (s1, s2) -> s1.toCharArray()[s1.toCharArray().length - 1] - s2.toCharArray()[s2.toCharArray().length - 1]);

			for (int idx = 0; idx < fromClauses.size(); idx++) {
				if (idx == 0) {
					sql += ("FROM " + fromClauses.get(idx));
				} else {
					sql += (" " + fromClauses.get(idx));
				}

				if (idx < fromClauses.size() - 1) {
					sql += ",";
				}
			}

			Collections.sort(joinClauses);

			boolean where = true;

			for (String clause : joinClauses) {
				if (where) {
					sql += String.format(" WHERE %s", clause);
					where = false;
				} else {
					sql += String.format(" AND %s", clause);
				}
			}

			Collections.sort(filterClauses);

			where = joinClauses.size() == 0;

			for (String clause : filterClauses) {
				if (where) {
					sql += String.format(" WHERE %s", clause);
					where = false;
				} else {
					sql += String.format(" AND %s", clause);
				}
			}

			/* GROUP BY clause.
			*/
			if (clauses[2] != null) {
				sql += (" " + clauses[2]);
			}

			/* ORDER clause.
			 */
			if (clauses[1] != null) {
				sql += (" " + clauses[1]);
			}

			return sql;
		}

		protected void post() throws Exception {
		}

		public String toString() {
			return sql;
		}

		private boolean isFullSelect() {
			return joinClauses.size() == 0 && filterClauses.size() == 0;
		}

		private void copyStatement(Statement statement) {
		    this.clauses[0] = statement.clauses[0];
            this.clauses[1] = statement.clauses[1];
            this.clauses[2] = statement.clauses[2];

            this.aliases.putAll(statement.aliases);
            this.fromClauses.addAll(statement.fromClauses);
            this.joinClauses.addAll(statement.joinClauses);
            this.filterClauses.addAll(statement.filterClauses);
        }
	}

	/*

	/**
	 * Syntax:
	 *
	 *    a filterdate( column, 20120101 )
	 *    a filterdate( column, 20120101 , >= )
	 *    a filterdate( column, 20120101 , <= )
	 *    a filterdate( column, 20120101 , '>' )
	 *    a filterdate( column, 20120101 , '<' )
	 *
	 */
	private class FilterDate extends Function {
		private String operator = "=";

		public FilterDate() {
			argumentsTakesTableOrColumn(1);
		}

		/* FIND COLUMN.
		*/
		protected void processor1(String s) throws Exception {
			column = s;
			nextMandatoryStep();
		}

		/* FIND VALUE.
		*/
		protected void processor2(String s) throws Exception {
			value = s;
			nextStep();
		}

		/* FIND FILTER OPERATOR IF PROVIDED.
		*/
		protected void processor3(String s) throws Exception {
			operator = s;
			finished();
		}

		public void post() throws Exception {
			filterDate(column, value, operator);
		}
	}

	private class Sum extends Report {
		public Sum() {
			super("SUM");
		}
	}

	private class Min extends Report {
		public Min() {
			super("MIN");
		}
	}

	private class Max extends Report {
		public Max() {
			super("MAX");
		}
	}

	/**
	 * Syntax: (sum|max|min)( summation_column | nummerical_constant , field1 , table.field2 , ... )
	 *
	 */
	private class Report extends Function {
		private final String function;

		private String summationArgument = null;

		public Report(String function) {
			this.function = function;

			argumentsTakesTableOrColumn(1);
			argumentsTakesTableOrColumn(2);
		}

		/* FIND REPORT COLUMN/NUMMERICAL CONSTANT.
		*/
		protected void processor1(String s) throws Exception {
			summationArgument = s;

			nextStep(); // User programmed group by columns for intstance sum( 1 , field1 , field2 ).
		}

		/* FIND THE COLUMN(S) FOR THE GROUP BY.
		*/
		protected void processor2(String s) throws Exception {
			columns.add(s);
		}

		public void post() throws Exception {
			report(function, summationArgument, columns);
		}
	}

	/**
	 * Syntax: desc( fielda, table.fieldb , ... )
	 *
	 */
	private class Desc extends Order {
		public Desc() {
			setDesc();
		}
	}

	/**
	 * Syntax: asc( fielda, table.fieldb , ... )
	 *
	 */
	private class Order extends Function {
		private boolean asc = true;

		public Order() {
			argumentsTakesTableOrColumn(1);
		}

		protected void setDesc() {
			asc = false;
		}

		/* FIND COLUMN(S) FOR THE ORDER BY.
		*/
		protected void processor1(String s) throws Exception {
			columns.add(s);
		}

		public void post() throws Exception {
			order(asc, columns);
		}
	}

	/**
	 * Syntax: group( fielda, table.fieldb , ... )
	 *
	 */
	private class Group extends Function {
		public Group() {
			argumentsTakesTableOrColumn(1);
		}

		/* FIND COLUMN(S) FOR THE GROUP.
		*/
		protected void processor1(String s) throws Exception {
			columns.add(s);
		}

		public void post() throws Exception {
			group(columns);
		}
	}

	/**
	 * Syntax: print( column1 , column2 , ... )
	 */
	private class Print extends Function {
		public Print() {
			argumentsTakesTableOrColumn(1);
		}

		/* FIND COLUMN WHICH TO PRINT.
		*/
		protected void processor1(String s) throws Exception {
			columns.add(s);
		}

		public void post() throws Exception {
			print(columns);
		}
	}

	/**
	 * Syntax: like( column , 'aa%bb' )
	 */
	private class Like extends Function {
		public Like() {
			argumentsTakesTableOrColumn(1);
		}

		/* FIND FIELD ON WHICH TO FILTER.
		*/
		protected void processor1(String s) throws Exception {
			column = s;
			nextMandatoryStep();
		}

		/* CONSUME LIKE PATTERN.
		*/
		protected void processor2(String s) throws Exception {
			value = s;

			finished();
		}

		public void post() throws Exception {
			like(column, value);
		}
	}

	/**
	 * Syntax: stringnotfilter( column , value1 , value2 , ... )
	 */
	private class StringNotFilter extends Filter {
		public StringNotFilter() {
			super(false, true);
		}
	}

	/**
	 * Syntax: stringfilter( column , value1 , value2 , ... )
	 */
	private class StringFilter extends Filter {
		public StringFilter() {
			super(true, true);
		}
	}

	/**
	 * Syntax: notfilter( column , value1 , value2 , ... )
	 */
	private class NotFilter extends Filter {
		public NotFilter() {
			super(false);
		}
	}

	/**
	 * Syntax: filter( column , value1 , value2 , ... )
	 */
	private class Filter extends Function {
		private boolean inclusive = true, stringFilter = false;

		public Filter() {
			argumentsTakesTableOrColumn(1);
		}

		public Filter(boolean inclusive) {
			this();
			this.inclusive = inclusive;
		}

		public Filter(boolean inclusive, boolean stringFilter) {
			this();
			this.inclusive = inclusive;
			this.stringFilter = stringFilter;
		}

		/* FIND COLUMN ON WHICH TO FILTER.
		*/
		protected void processor1(String s) throws Exception {
			column = s;
			nextStep();
		}

		/* FIND VALUES ON WHICH TO FILTER.
		*/
		protected void processor2(String s) throws Exception {
			values.add(s);
		}

		public void post() throws Exception {
			filter(column, values, inclusive, stringFilter);
		}
	}

	/**
	 Syntax:
	  ref( table , reference );
	  ref( table.column , reference );

	  Note:
	  1> Reference is number which should equal or greater then 1.

	  2> The opposite equivalent of this function is the newtable function which can be used to add another instance of the same table to
	  the query.

	  3> Note: Ref can only called as an argument of an other function.
	 */
	private class Ref extends Function {
		private String reference = null;

		/* FIND TABLE/COLUMN ON FOR WHICH TO FIND THE ALIAS.
		*/
		protected void processor1(String s) throws Exception {
			column = s;
			nextMandatoryStep();
		}

		/* FIND THE REFERENCE.
		*/
		protected void processor2(String s) throws Exception {
			value = s;
			finished();
		}

		protected void post() throws Exception {
			reference = ref(column, value);
		}

		public String getReference() {
			return reference;
		}
	}

	/**
	 * Syntax: newtable( table )
	 *
	 * Note: NewTable can only called as an argument of an other function.
	 */
	private class NewTable extends Function {
		private String alias = null;

		/* FETCH THE TABLE NAME.
		*/
		protected void processor1(String s) throws Exception {
			table = s;
			finished();
		}

		protected void post() throws Exception {
			alias = getAlias(table, true);
		}

		public String getTable() {
			return table;
		}

		public String getTableAlias() {
			return alias;
		}
	}

	/**
	 * Syntax:
	 *  distinct-constant( constant, constant , ... )
	   *
	 * Note: user can add columns and constants to the function in a random order.
	 */
	private class DistinctConstant extends Function {
		public DistinctConstant() { // Needed for instantiation.
		}

		/* FIND CONSTANT(S) FOR THE DISTINCT.
		*/
		protected void processor1(String s) throws Exception {
			values.add(s);
		}

		public void post() throws Exception {
			distinctConstants(values);
		}
	}

	/**
	 * Syntax:
	 *  distinct-column( column, column , ... )
	   *
	 * Note: user can add columns and constants to the function in a random order.
	 */
	private class Distinct extends Function {
		public Distinct() {
			argumentsTakesTableOrColumn(1);
		}

		/* FIND COLUMN(S) FOR THE DISTINCT.
		*/
		protected void processor1(String s) throws Exception {
			/* Check if argument is a table. If so, all fields of table are selected.
			Argument can also be a reference when the table was referred with the ref( table, occ ) function.
			*/
			if (isTable(s)) {
				s = getAlias(s) + ".*";
			} else if (isAlias(s)) {
				s = s + ".*";
			}

			columns.add(s);
		}

		public void post() throws Exception {
			distinct(columns);
		}
	}

	/**
	 Syntax:
	    join( joinTable )
	    join( joinTable , joinFieldDriveTable )
	    join( joinTable , joinFieldDriveTable , joinFieldJoinTable )
	    join( joinTable , join() , ... )
	    join( joinTable , joinFieldDriveTable , joinFieldJoinTable , join() , ... )

	 */
	private class Join extends Function {
		private String driveTable = null;
		private String aliasDriveTable = null;

		private String joinTable = null;
		private String aliasJoinTable = null;

		private String joinFieldDriveTable = null;
		private String joinFieldJoinTable = null;

		public Join() {
		}

		public void setDriveTable(String driveTable, String aliasDriveTable) {
			this.driveTable = driveTable;
			this.aliasDriveTable = aliasDriveTable;
		}

		/* FIND TABLE TO WHICH TO JOIN.
		*/
		protected void processor1(String s) throws Exception {
			/*
			  First argument can be three things:

			  1> The name of the table.
			  2> A call to the function newtable().
			  3> A nested statement.

			The second case is used when the same table is refered multiple times in the from clause.
			E.g. SELECT t2.field FROM a t0, b t1 , a t2 WHERE t0.id = t1.id AND t1.id = t2.id
			This statement can be programmed as:

			  a join( b , join( newtable( a ) ) print( ref( a.field , 2 )

			Where the table is added to the query by using the newtable function, it can be referred by the ref function.
			*/
			if ("newtable".equals(s)) {
				NewTable newTable = new NewTable();
				newTable.execute();

				joinTable = newTable.getTable();
				aliasJoinTable = newTable.getTableAlias();
			} else if ("(".equals(s)) {
				Statement statement = new Statement();
				statement.execute();

				joinTable = "(" + statement.sql + ")";
			} else {
				joinTable = s;
			}

			/* To make sure that the order of the aliases (e.g. t0, t1, t2) follows the order of the table in the statement
			(e.g. 'a join(b, join( c ) )' becomes 'SELECT * FROM a t0, b t1, c t2' as opposed to '... FROM a t0, c t1, b t2'
			the table is now registrated in the alias administration.
			*/
			if (aliasJoinTable == null) {
				aliasJoinTable = getAlias(joinTable);
			}

			nextStep();
		}

		/* FIND JOIN FIELD DRIVE TABLE OR ANOTHER JOIN.
		*/
		protected void processor2(String s) throws Exception {
			/* Check if user wants another join.
			*/
			if ("join".equals(s)) {
				Join join = new Join();
				join.setDriveTable(joinTable, aliasJoinTable);
				join.execute();

				/* If user programs a join, it can only be followed by anthor join.
				*/
				nextStep(4);
				return;
			}

			joinFieldDriveTable = s;

			nextStep();
		}

		/* FIND JOIN FIELD JOIN TABLE OR ANOTHER JOIN.
		*/
		protected void processor3(String s) throws Exception {
			/* Check if user wants another join.
			*/
			if ("join".equals(s)) {
				Join join = new Join();
				join.setDriveTable(joinTable, aliasJoinTable);
				join.execute();

				/* If user programs a join, it can only be followed by anthor join.
				*/
				nextStep(4);
				return;
			}

			joinFieldJoinTable = s;

			nextStep();
		}

		/* CHECK IF USER PROGRAMMED ANOTHER JOIN(S).
		*/
		protected void processor4(String s) throws Exception {
			/* Check if user wants another join.
			*/
			if ("join".equals(s)) {
				Join join = new Join();
				join.setDriveTable(joinTable, aliasJoinTable);
				join.execute();
			} else {
				syntaxError(ERR_JOIN_SHOULD_FOLLOW_JOIN, s);
			}

			/* User can program additional joins, but nothing else (exp: join( table, field , join() , join() , join() ).
			*/
		}

		public void post() throws Exception {
			/* Expand the where clause.
			*/
			join(driveTable, aliasDriveTable, joinFieldDriveTable, joinTable, aliasJoinTable, joinFieldJoinTable);
		}
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
						quotedArea = (quotedArea ? false : true);
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

	protected abstract class Function {
		/* Useful vars!!!!
		*/
		protected List<String> columns = new ArrayList<String>();
		protected List<String> values = new ArrayList<String>();
		protected String table = null, column = null, value = null;

		private int status = 1;
		protected boolean processedExpectedArguments = false;
		private boolean expectAnotherArgument = false;
		private boolean zeroArgumentsIsPossible = false;

		private List<Integer> columnArguments = new ArrayList<Integer>();

		protected void processor1(String languageElement) throws Exception {
		}

		protected void processor2(String languageElement) throws Exception {
		}

		protected void processor3(String languageElement) throws Exception {
		}

		protected void processor4(String languageElement) throws Exception {
		}

		protected void processor5(String languageElement) throws Exception {
		}

		protected void argumentsTakesTableOrColumn(int argument) {
			columnArguments.add(argument);
		}

		/**
		 * If called by a function (in constructor) then there is the option to give the function no arguments.
		 */
		protected void zeroArgumentsIsPossible() {
			zeroArgumentsIsPossible = true;
		}

		/**
		 * When function has determined that function is finished (from analyzing the arguments), this method should be called.
		 */
		protected void finished() {
			processedExpectedArguments = true;
		}

		/**
		 * When function wants to go to the next (optional!!) processing step, this method should be called.
		 */
		protected void nextStep() {
			status++;
		}

		protected void nextStep(int nextStep) {
			status = nextStep;
		}

		/** When function wants to go to the next (mandatory!!) processing step, this method should be called.
		*/
		protected void nextMandatoryStep() {
			status++;
			expectAnotherArgument = true;
		}

		protected void parse() throws Exception {
			boolean functionContinues = true;

			/* Function should always begin with an opening bracket.
			*/
			String languageElement = getQuotedValue(pop());

			if (!"(".equals(languageElement)) {
				syntaxError(ERR_EXP_OPENING_BRACKET);
			}

			while (!processedExpectedArguments) {
				/* Init the expected var, so it can be set by the processors.
				*/
				expectAnotherArgument = false;

				languageElement = getQuotedValue(pop());

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
				if (columnArguments.contains(new Integer(status))) {
					if ("ref".equals(languageElement)) {
						Ref ref = new Ref();
						ref.execute();
						languageElement = ref.getReference();
					} else {
						languageElement = resolveColumn(languageElement);
					}
				}

				switch (status) {
					case 1:

					  /* A function can indicate that 'no arguments' is possible. If this is not indicated, the next element
					  should not(!!) be a closing bracket.
					  */
					  if (")".equals(languageElement)) {
						  if (zeroArgumentsIsPossible) {
							  functionContinues = false;

							  /* Break immediately the while loop to avoid that the closing quote of the statement is consumed by this function.
							  */
							  finished();
							  continue;
							}

						  syntaxError(ERR_FUNCTION_HAS_NO_ARGUMENTS);
						}

					  processor1(languageElement);
					  break;

					case 2:
					  processor2(languageElement);
					  break;

					case 3:
					  processor3(languageElement);
					  break;

					case 4:
					  processor4(languageElement);
					  break;

					case 5:
					  processor5(languageElement);
					  break;
				}

				/* Argument is processed. Process now the element which follows the argument. This element can be a ',' in case the function continues
				 or a ')' in case the function closes.
				*/
				functionContinues = functionContinues(!processedExpectedArguments);

				if (!functionContinues) {
					if (expectAnotherArgument) {
						syntaxError(ERR_UNEXPECTED_END);
					} else {
						break;
					}
				}
			}

			if (functionContinues) {
				syntaxError(ERR_FUNCTION_HAS_TOO_MANY_ARGUMENTS);
			}
		}

		private String getQuotedValue(String value) throws Exception {
			/* Value can begin and end with a quote (').
			*/
			if ("'".equals(value)) {
				value = pop();

				/* Consume (and check) the end quote.
				*/
				if (!"'".equals(pop())) {
					syntaxError(ERR_MISSING_END_QUOTE);
				}
			}

			return value;
		}

		private boolean functionContinues(boolean expectAnotherArgument) throws Exception {
			String languageElement = pop();

			/* Check if function closes.
			*/
			if (")".equals(languageElement)) {
				return false;
			}

			/* If function continues (as it must at this point) and a next argument is expected, then the next element should be a ','.
			*/
			if (expectAnotherArgument && !",".equals(languageElement)) {
				syntaxError(ERR_EXP_COMMA);
			}

			return true;
		}

		public void execute() throws Exception {
			parse();
			post();
		}

		protected abstract void post() throws Exception;
	}

	private class CustomMapping {
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
