package functionalsql.commands;

import functionalsql.Function;
import functionalsql.FunctionalSQLCompiler;
import functionalsql.Statement;

import static functionalsql.FunctionalSQLCompiler.*;

/**
 Syntax:
 join( joinTable )
 join( joinTable , joinFieldDriveTable )
 join( joinTable , joinFieldDriveTable , joinFieldJoinTable )
 join( joinTable , join() , ... )
 join( joinTable , joinFieldDriveTable , joinFieldJoinTable , join() , ... )

 */
public class Join extends Function {

    public enum JOIN_TYPE {
        INNER, LEFT, RIGHT, FULL;
    }

    private String driveTable = null;
    private String aliasDriveTable = null;

    private String joinTable = null;
    private String aliasJoinTable = null;

    private String joinFieldDriveTable = null;
    private String joinFieldJoinTable = null;

    private JOIN_TYPE joinType=null;

    public Join() {}

    public Join(JOIN_TYPE joinType) {
        this.joinType = joinType;
    }

    public void setDriveTable(String driveTable, String aliasDriveTable) {
        this.driveTable = driveTable;
        this.aliasDriveTable = aliasDriveTable;
    }

    /* Find table to which to join.
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

        Class<? extends Function> function = compiler.getFunction(s);

        if(function != null && NewTable.class == function) {
            Function newTable = compiler.exec(statement, function, joinTable);
            joinTable = newTable.getTable();
            aliasJoinTable = ((NewTable)newTable).getTableAlias();
        } else if ("(".equals(s)) {
            Statement statement = new Statement();
            compiler.parse(statement);

            joinTable = "(" + statement.getSql() + ")";
        } else {
            compiler.checkTableOrColumnFormat(s);
            joinTable = s;
        }

        /* To make sure that the order of the aliases (e.g. t0, t1, t2) follows the order of the table in the statement
        (e.g. 'a join(b, join( c ) )' becomes 'SELECT * FROM a t0, b t1, c t2' as opposed to '... FROM a t0, c t1, b t2'
        the table is now registrated in the alias administration.
        */
        if (aliasJoinTable == null) {
            aliasJoinTable = statement.getAlias(joinTable);
        }

        nextStep();
    }

    /* FIND JOIN FIELD DRIVE TABLE OR ANOTHER JOIN.
    */
    protected void processor2(String s) throws Exception {

        Class<? extends Function> function = compiler.getFunction(s);

        if(function != null && Join.class.isAssignableFrom(function)) {
            compiler.exec(statement, function, joinTable);

            /* If user programs a join, it can only be followed by anthor join.
			*/
            nextStep(4);

            return;
        }

        compiler.checkTableOrColumnFormat(s);

        joinFieldDriveTable = s;

        nextStep();
    }

    /* FIND JOIN FIELD JOIN TABLE OR ANOTHER JOIN.
    */
    protected void processor3(String s) throws Exception {
        Class<? extends Function> function = compiler.getFunction(s);

        if(function != null && Join.class.isAssignableFrom(function)) {
            compiler.exec(statement, function, joinTable);

            /* If user programs a join, it can only be followed by anthor join.
			*/
            nextStep(4);

            return;
        }

        compiler.checkTableOrColumnFormat(s);
        joinFieldJoinTable = s;

        nextStep();
    }

    /* CHECK IF USER PROGRAMMED ANOTHER JOIN(S).
    */
    protected void processor4(String s) throws Exception {

        Class<? extends Function> function = compiler.getFunction(s);

        if(function != null && Join.class.isAssignableFrom(function)) {
            compiler.exec(statement, function, joinTable);
        } else {
            compiler.syntaxError(ERR_JOIN_SHOULD_FOLLOW_JOIN, s);
        }

        /* User can program additional joins, but nothing else (exp: join( table, field , join() , join() , join() ).
		*/
    }

    public void execute() throws Exception {
        join(driveTable, aliasDriveTable, joinFieldDriveTable, joinTable, aliasJoinTable, joinFieldJoinTable, joinType);
    }

    /**
     * Function can be used by macros who want create their own joins.
     *
     * @param driveTable The drive table.
     * @param joinColumnDriveTable Join column drive table.
     * @param joinTable The join table.
     * @param joinColumnJoinTable Join column join table.
     * @param joinType join-type.
     */
    protected void join(String driveTable,
                        String aliasDriveTable,
                        String joinColumnDriveTable,
                        String joinTable,
                        String aliasJoinTable,
                        String joinColumnJoinTable,
                        JOIN_TYPE joinType) throws Exception {

        String fromClause = String.format("%s %s", joinTable, aliasJoinTable);

		/* Expand the from clause with the join table if necessary.
		*/
        if(joinType == null && !statement.fromClauses.contains(fromClause)) {
            statement.fromClauses.add(fromClause);
        }

		/* Syntax:
		join( table )
		join( table, driveTableColumn )
		join( table, driveTableColumn , joinTableColumn )

		RULE: If joinTableColumn is present, then also driveTableColumn is present.
		*/
        if (joinColumnJoinTable == null) {
            FunctionalSQLCompiler.CustomMapping c = compiler.getCustomMapping(driveTable, joinColumnDriveTable, joinTable);

			/* If join fields are not programmed and there are also no cumstom mappings, then we cannot define the join.
			*/
            if (c == null) {
                compiler.syntaxError(ERR_NO_JOIN_COLUMNS_DEFINED_AND_NO_CUSTOM_MAPPING_PRESENT);
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
        String clause;

        if (compiler.aliasToNumber(aliasDriveTable) < compiler.aliasToNumber(aliasJoinTable)) {
            clause = String.format("%s.%s = %s.%s",
                    aliasDriveTable,
                    joinColumnDriveTable,
                    aliasJoinTable,
                    joinColumnJoinTable);
        } else {
            clause = String.format("%s.%s = %s.%s",
                    aliasJoinTable,
                    joinColumnJoinTable,
                    aliasDriveTable,
                    joinColumnDriveTable);
        }

		/* The inner join is depicted as SELECT ... FROM a, b WHERE ... (instead of using the JOIN keyword).
		*/
        if(joinType == null) {
            if (!statement.filterClauses.contains(clause)) {
                statement.filterClauses.add(clause);
            }
        } else {
            String joinClause=null;

            switch (joinType) {
                case INNER:
                    joinClause = String.format("INNER JOIN %s ON %s", fromClause, clause);
                    break;

                case LEFT:
                    joinClause = String.format("LEFT JOIN %s ON %s", fromClause, clause);
                    break;

                case RIGHT:
                    joinClause = String.format("RIGHT JOIN %s ON %s", fromClause, clause);
                    break;

                case FULL:
                    joinClause = String.format("FULL JOIN %s ON %s", fromClause, clause);
                    break;
            }

            if (!statement.joinClauses.contains(joinClause)) {
                statement.joinClauses.add(joinClause);
            }
        }
    }
}

