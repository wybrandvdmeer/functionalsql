package functionalsql.commands;

import functionalsql.Function;
import functionalsql.Relation;
import functionalsql.consumer.TokenConsumer;
import functionalsql.consumer.Consumer;
import functionalsql.consumer.FunctionConsumer;

import static functionalsql.FunctionalSQLCompiler.ERR_JOIN_SHOULD_FOLLOW_JOIN;
import static functionalsql.FunctionalSQLCompiler.ERR_NO_JOIN_COLUMNS_DEFINED_AND_NO_RELATION_FOUND;

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

    private String table;

    public Join() {
        /* Function consumers.
        */
        build(0, new FunctionConsumer(this, function -> {
            if(NewTable.class == function.getClass()) {
                joinTable = ((NewTable)function).getTable();
                aliasJoinTable = ((NewTable)function).getTableAlias();
            } else if(Statement.class == function.getClass()) {
                joinTable = "(" + ((Statement)function).getSql() + ")";
            }

            if (aliasJoinTable == null) {
                aliasJoinTable = getCompiler().getStatement().getAlias(joinTable);
            }
        }).expect(NewTable.class).expect(Statement.class).singleValue().mandatory());

        Consumer consumerStep2 = new FunctionConsumer(this).expect(Join.class).singleValue();
        build(1, consumerStep2);
        setNextStepForConsumer(consumerStep2, 3);

        build(2, new FunctionConsumer(this).expect(Join.class).singleValue());
        build(3, new FunctionConsumer(this).expect(Join.class));

        /* Token consumers.
        */
        build(0, new TokenConsumer(this, token -> {
            getCompiler().checkTableOrColumnFormat(token);
            joinTable = token;

            /* To make sure that the order of the aliases (e.g. t0, t1, t2) follows the order of the table in the statement
            (e.g. 'a join(b, join( c ) )' becomes 'SELECT * FROM a t0, b t1, c t2' as opposed to '... FROM a t0, c t1, b t2'
            the table is now registrated in the alias administration.
            */
            if (aliasJoinTable == null) {
                aliasJoinTable = getCompiler().getStatement().getAlias(joinTable);
            }
        }).singleValue().mandatory());
        build(1, new TokenConsumer(this, token -> {
            getCompiler().checkTableOrColumnFormat(token);
            joinFieldDriveTable = token;
        }).singleValue());
        build(2, new TokenConsumer(this, token -> {
            getCompiler().checkTableOrColumnFormat(token);
            joinFieldJoinTable = token;
        }).singleValue());
        build(3, new TokenConsumer(this, token ->  getCompiler().syntaxError(ERR_JOIN_SHOULD_FOLLOW_JOIN, token)));
    }

    public Join(JOIN_TYPE joinType) {
        this();
        this.joinType = joinType;
    }

    public void setDriveTable(String driveTable, String aliasDriveTable) {
        this.driveTable = driveTable;
        this.aliasDriveTable = aliasDriveTable;
    }

    public String getJoinTable() {
        return joinTable;
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

        if(joinType == null) {
            getCompiler().getStatement().addFromClause(fromClause);
        }

        /* Syntax:
        join( table )
        join( table, driveTableColumn )
        join( table, driveTableColumn , joinTableColumn )

        RULE: If joinTableColumn is present, then also driveTableColumn is present.
        */
        if (joinColumnJoinTable == null) {
            Relation relation = getCompiler().getRelation(driveTable, joinColumnDriveTable, joinTable);

            /* If join fields are not programmed and there are also no cumstom mappings, then we cannot define the join.
            */
            if (relation == null) {
                getCompiler().syntaxError(ERR_NO_JOIN_COLUMNS_DEFINED_AND_NO_RELATION_FOUND);
            }

            joinColumnJoinTable = relation.getColumn(joinTable);

            /* If joinFieldJoinTable should be null at this point, it is a program error.
            */
            assert (joinColumnJoinTable != null);

            if (joinColumnDriveTable == null) {
                joinColumnDriveTable = relation.getColumn(driveTable);
            }

            /* If joinFieldDriveTable should be null at this point, it is a program error.
            */
            assert (joinColumnDriveTable != null);
        }

        /* Expand the where clause if necessary.
        */
        String clause;

        if (getCompiler().aliasToNumber(aliasDriveTable) < getCompiler().aliasToNumber(aliasJoinTable)) {
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
            getCompiler().getStatement().addFilterClause(clause);
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

            getCompiler().getStatement().addJoinClause(joinClause);
        }
    }
}

