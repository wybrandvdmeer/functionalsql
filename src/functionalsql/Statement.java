package functionalsql;

import java.util.*;

import static functionalsql.FunctionalSQLCompiler.ERR_IF_TABLE_HAS_MULTIPLE_INSTANCES_USE_REF_FUNCTION;
import static functionalsql.FunctionalSQLCompiler.ERR_NULL_TABLE;
import static functionalsql.FunctionalSQLCompiler.ERR_UNKNOWN_FUNCTION;

public class Statement extends Function {
    public String selectClause=VIRGIN_SELECT_CLAUSE;
    public String groupByClause;
    public String orderByClause;
    public List<String> fromClauses = new ArrayList<>();
    public List<String> joinClauses = new ArrayList<>();
    public List<String> filterClauses = new ArrayList<String>();
    public Map<String, String> aliases = new HashMap<>(); //alias, table
    private final static String VIRGIN_SELECT_CLAUSE="SELECT *";

    private String sql;

    public Statement() {
        allowAllFunctionsAsArgument();
        argumentsTakesTableOrColumn(1);
    }

    public void processor1(String s) throws Exception {
        if(getTable() != null) {
            getCompiler().syntaxError(ERR_UNKNOWN_FUNCTION, s);
        }
        setTable(s);
        fromClauses.add(String.format("%s %s", getTable(), getAlias(getTable())));
    }

    public void processor1(Function function) throws Exception {
        if (function instanceof Statement) {
            String nestedQuery = ((Statement) function).getSql();

            if(isFullSelect()) {
                copyStatement(((Statement)function));
            } else {
                fromClauses.add(String.format("(%s) %s", nestedQuery, getAlias(nestedQuery)));
            }
        }
    }

    public void execute() throws Exception {
        sql = String.format("%s ", selectClause); // SELECT ...

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

        for(String clause : joinClauses) {
            sql += ( " " + clause);
        }

        Collections.sort(filterClauses);

        boolean where=true;

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
        if (groupByClause != null) {
            sql += (" " + groupByClause);
        }

        /* ORDER clause.
        */
        if (orderByClause != null) {
            sql += (" " + orderByClause);
        }
    }

    public boolean isVirginSelectClause() {
        return VIRGIN_SELECT_CLAUSE == selectClause;
    }

    public String getSql() {
        return sql;
    }

    public String toString() {
        return sql;
    }

    public boolean isFullSelect() {
        return joinClauses.size() == 0 && filterClauses.size() == 0;
    }

    public void copyStatement(Statement statement) {
        this.selectClause = statement.selectClause;
        this.groupByClause = statement.groupByClause;
        this.orderByClause = statement.orderByClause;

        this.aliases.putAll(statement.aliases);
        this.fromClauses.addAll(statement.fromClauses);
        this.joinClauses.addAll(statement.joinClauses);
        this.filterClauses.addAll(statement.filterClauses);
    }

    public String getAlias(String table) throws Exception {
        return getAlias(table, false);
    }

    public String getAlias(String table, boolean forceNewAlias) throws Exception {
        if (table == null) {
            getCompiler().syntaxError(ERR_NULL_TABLE);
        }

        String alias = null;

        if (!forceNewAlias) {
            for (Map.Entry<String, String> entry : aliases.entrySet()) {
                if (entry.getValue().equals(table)) {
                    if (alias != null) {
                        getCompiler().syntaxError(ERR_IF_TABLE_HAS_MULTIPLE_INSTANCES_USE_REF_FUNCTION, table);
                    }

                    alias = entry.getKey();
                }
            }

            if (alias != null) {
                return alias;
            }
        }

        alias = "t" + aliases.keySet().size();

        aliases.put(alias, table);
        return alias;
    }

    public String getDriveTableOfQuery() {
        for (Map.Entry<String, String> entry : aliases.entrySet()) {
            if (entry.getKey().equals("t0")) {
                return entry.getValue();
            }
        }

        return null;
    }

    public boolean isAlias(String s) {
        return aliases.keySet().contains(s);
    }

    public boolean isTable(String s) {
        return aliases.values().contains(s);
    }
}