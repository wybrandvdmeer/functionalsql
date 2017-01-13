package functionalsql;

import java.util.*;

import static functionalsql.FunctionalSQLCompiler.ERR_IF_TABLE_HAS_MULTIPLE_INSTANCES_USE_REF_FUNCTION;
import static functionalsql.FunctionalSQLCompiler.ERR_NULL_TABLE;

public class Statement extends Function {
    public String[] clauses = new String[3]; // Contains SELECT, FROM, ORDER AND GROUP.
    public List<String> fromClauses = new ArrayList<String>();
    public List<String> joinClauses = new ArrayList<String>();
    public List<String> filterClauses = new ArrayList<String>();
    public Map<String, String> aliases = new HashMap<>(); //alias, table

    private String sql;

    public void compileSQL() throws Exception {
        if (clauses[0] == null) {
            clauses[0] = "SELECT *";
        }

        sql = String.format("%s ", clauses[0]); // SELECT ...

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
        if (clauses[2] != null) {
            sql += (" " + clauses[2]);
        }

        /* ORDER clause.
        */
        if (clauses[1] != null) {
            sql += (" " + clauses[1]);
        }
    }

    public String getSql() {
        return sql;
    }

    public void execute() throws Exception {
    }

    public String toString() {
        return sql;
    }

    boolean isFullSelect() {
        return joinClauses.size() == 0 && filterClauses.size() == 0;
    }

    void copyStatement(Statement statement) {
        this.clauses[0] = statement.clauses[0];
        this.clauses[1] = statement.clauses[1];
        this.clauses[2] = statement.clauses[2];

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
            compiler.syntaxError(ERR_NULL_TABLE);
        }

        String alias = null;

        if (!forceNewAlias) {
            for (Map.Entry<String, String> entry : aliases.entrySet()) {
                if (entry.getValue().equals(table)) {
                    if (alias != null) {
                        compiler.syntaxError(ERR_IF_TABLE_HAS_MULTIPLE_INSTANCES_USE_REF_FUNCTION, table);
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

    public boolean isAlias(String s) {
        return aliases.keySet().contains(s);
    }

    public boolean isTable(String s) {
        return aliases.values().contains(s);
    }
}
