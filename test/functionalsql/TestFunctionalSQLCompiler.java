package functionalsql;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestFunctionalSQLCompiler {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testExtraClosingParenthesis() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_UNEXPECTED_END_OF_FUNCTION));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a asc(a))");
    }

    @Test
    public void testOrderByAlreadyDefined() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_ORDER_BY_CLAUSE_ALREADY_DEFINED));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a asc(a) asc(a)");
    }

    @Test
    public void testUnexpectedEndOfFilterDate() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_UNEXPECTED_END_OF_FUNCTION));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a filterdate()");
    }

    @Test
    public void testUnexpectedEndOfDistinct() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_UNEXPECTED_END_OF_FUNCTION));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a distinct()");
    }

    @Test
    public void testUnexpectedEndOfNewTable() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_UNEXPECTED_END_OF_FUNCTION));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a newtable()");
    }

    @Test
    public void testUnexpectedEndOfRef() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_UNEXPECTED_END_OF_FUNCTION));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a ref()");
    }

    @Test
    public void testUnexpectedEndOfJoin() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_UNEXPECTED_END_OF_FUNCTION));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a join()");
    }

    @Test
    public void testUnexpectedEndOfFilter() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_UNEXPECTED_END_OF_FUNCTION));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a filter()");
    }

    @Test
    public void testUnexpectedEndOfSum() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_UNEXPECTED_END_OF_FUNCTION));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a sum()");
    }

    @Test
    public void testUnexpectedEndOfGroup() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_UNEXPECTED_END_OF_FUNCTION));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a group()");
    }

    @Test
    public void testUnexpectedEndOfFunction() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_UNEXPECTED_END_OF_FUNCTION));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a filter()");
    }

    @Test
    public void testDefaultMappingHasNoEqualColumns() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_DEFAULT_RELATION_HAS_NO_EQUAL_COLUMNS));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addDefaultRelation("id1", "id2");
    }

    @Test
    public void testUnexpectedEndOfFunctionPrint() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_UNEXPECTED_END_OF_FUNCTION));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a print()");
    }

    @Test
    public void testUnexpectedEndOfFunctionAsc() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_UNEXPECTED_END_OF_FUNCTION));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a asc()");
    }

    @Test
    public void testNullField() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_NULL_FIELD));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a print( a.)");
    }

    @Test
    public void testNullTable() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_NULL_TABLE));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a print( .field)");
    }

    @Test
    public void testExpectedOpeningBracket() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_EXP_OPENING_BRACKET));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addRelation("a", "id", "b", "id");
        c.addRelation("b", "id", "c", "id");
        c.parse("a join b");
    }

    @Test
    public void testJoinShouldFollowJoin() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_JOIN_SHOULD_FOLLOW_JOIN, "id"));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addRelation("a", "id", "b", "id");
        c.addRelation("b", "id", "c", "id");
        c.parse("a join(b, join(c), id, id)");
    }

    @Test
    public void testIfTableHasMultipleInstancesUseRefFunction() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_IF_TABLE_HAS_MULTIPLE_INSTANCES_USE_REF_FUNCTION, "a"));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addRelation("a", "id", "a", "id");
        c.parse("a join(newtable(a)) print(a)");
    }

    @Test
    public void testOnlyOneValueWhenUsingOperatorInFilter() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_ONLY_ONE_VALUE_WHEN_USING_OPERATOR_IN_FILTER, "[1, 2]"));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a filter(c, >, 1, 2)");
    }

    @Test
    public void testFunctionHasTooManyArguments() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(FunctionalSQLCompiler.ERR_FUNCTION_HAS_TOO_MANY_ARGUMENTS);
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a like(c, 1, 2)");
    }

    @Test
    public void testUnexpectedEndOfFunction1() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(FunctionalSQLCompiler.ERR_UNEXPECTED_END_OF_FUNCTION);
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a join(b");
    }

    @Test
    public void testUnexpectedEndOfFunction2() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(FunctionalSQLCompiler.ERR_UNEXPECTED_END_OF_FUNCTION);
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a join(b,");
    }

    @Test
    public void testUnexpectedEndOfFunction3() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(FunctionalSQLCompiler.ERR_UNEXPECTED_END_OF_FUNCTION);
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a join(b a");
    }

    @Test
    public void testQuotedTableNameStatement() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_WRONG_FORMAT_TABLE_OR_COLUMN_NAME, "'a'"));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("'a'");
    }

    @Test
    public void testQuotedTableNameJoin() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_WRONG_FORMAT_TABLE_OR_COLUMN_NAME, "'b'"));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a join('b')");
    }

    @Test
    public void testQuotedTableNameNewTable() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_WRONG_FORMAT_TABLE_OR_COLUMN_NAME, "'a'"));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a join(newtable('a'))");
    }

    @Test
    public void testQuotedTableNamePrint() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_WRONG_FORMAT_TABLE_OR_COLUMN_NAME, "'b'"));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a print('b')");
    }

    @Test
    public void testMissingEndQuote() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(FunctionalSQLCompiler.ERR_MISSING_END_QUOTE);

        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a filter(field, 'value");
    }

    @Test
    public void testComplexQuery() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();

        c.addRelation("a", "id", "b", "id");
        c.addRelation("b", "id", "c", "id");
        c.addRelation("a", "id", "c", "id");

        assertEquals("SELECT t0.field, t1.field, t3.field2, MAX( t3.field ) FROM a t0, b t1, c t2, c t3 WHERE t0.id = t1.id AND t0.id = t3.id AND t1.id = t2.id GROUP BY t0.field, t1.field, t3.field2",
             c.parse("(a) join(b, join(c)) join(newtable(c)) max(ref(c.field, 2), a.field, b.field, ref(c.field2, 2))"));
    }

    @Test
    public void testNestedQuery() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addRelation("a", "id", "b", "id");
        c.addRelation("b", "id", "c", "id");
        assertEquals( "SELECT * FROM a t0, (SELECT * FROM b t0, c t1 WHERE t0.id = t1.id) t1 WHERE t0.id = t1.id",
                c.parse("a join((b join(c)), id, id )") );
        assertEquals("SELECT * FROM a t0" , c.parse( "a" ) );
        assertEquals("SELECT * FROM a t0" , c.parse( "(a)" ) );
        assertEquals("SELECT * FROM a t0" , c.parse( "((a))" ) );
        assertEquals("SELECT * FROM a t0" , c.parse( "(((a)))" ) );
        assertEquals("SELECT * FROM a t0 WHERE field = 2", c.parse("a filter(field, 2)"));
        assertEquals("SELECT * FROM a t0 WHERE field = 2", c.parse("(a) filter(field, 2)"));
        assertEquals("SELECT * FROM a t0 WHERE field = 2", c.parse("((a) filter(field, 2))"));
        assertEquals("SELECT * FROM a t0 WHERE field = 2", c.parse("((((a))) filter(field, 2))"));
        assertEquals("SELECT * FROM a t0 WHERE field = 2", c.parse("((a filter(field, 2)))"));
        assertEquals(c.parse("(((((a) filter(field, 2)))))"), c.parse("((((a))) filter(field, 2))"));
    }

    @Test
    public void testJoin() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();

        assertEquals( "SELECT * FROM a t0, b t1 WHERE t0.v_a = t1.v_b", c.parse( "a join(b, v_a, v_b ) ") );

        try {
            c.parse("a join(b) ");
            fail();
        } catch( Exception e ) {
            checkException(e, FunctionalSQLCompiler.ERR_NO_JOIN_COLUMNS_DEFINED_AND_NO_RELATION_FOUND);
        }

        c.addRelation("a", "v_a", "b", "v_b");
        assertEquals("SELECT * FROM a t0, b t1 WHERE t0.v_a = t1.v_b", c.parse( "a join(b) ") );
        assertEquals("SELECT * FROM a t0, b t1, c t2 WHERE t0.v_a = t1.v_b AND t0.v_a = t2.v_c", c.parse("a join(b) join(c, v_a, v_c)"));

        c.addRelation("a", "v_a", "c", "v_c");
        assertEquals("SELECT * FROM a t0, b t1, c t2 WHERE t0.v_a = t1.v_b AND t0.v_a = t2.v_c", c.parse("a join(b) join(c)"));

        c.addRelation("b", "v_b", "c", "v_c");
        assertEquals("SELECT * FROM a t0, b t1, c t2 WHERE t0.v_a = t1.v_b AND t1.v_b = t2.v_c", c.parse("a join(b, join(c))"));

        try {
            c.parse("a join(b, like(c, 'a%b'))");
            fail();
        } catch(Exception e) {
            checkException(e, createError(FunctionalSQLCompiler.ERR_CANNOT_USE_FUNCTION_AS_ARGUMENT_OF_FUNCTION, "like", "join"));
        }

        try {
            c.parse("a print(asc(v1))");
            fail();
        } catch(Exception e) {
            checkException(e, createError(FunctionalSQLCompiler.ERR_CANNOT_USE_FUNCTION_AS_ARGUMENT_OF_FUNCTION, "asc", "print"));
        }
    }

    @Test
    public void testPrint() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        assertEquals( "SELECT v FROM a t0", c.parse("a print(v)"));
        assertEquals( "SELECT t0.* FROM a t0", c.parse("a print(a)"));
    }

    @Test
    public void testLike() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        assertEquals( "SELECT * FROM a t0 WHERE v LIKE 'a%b'", c.parse("a like( v, 'a%b')"));
        assertEquals( "SELECT * FROM a t0 WHERE v LIKE 1", c.parse("a like(v, 1)"));

        try {
            c.parse("a like(v, a)");
        } catch(Exception e) {
            checkException(e, createError(FunctionalSQLCompiler.ERR_VALUE_SHOULD_BE_QUOTED, "a"));
        }
    }

    @Test
    public void testGroup() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addRelation("a", "id", "b", "id");
        assertEquals("SELECT field, t1.field FROM a t0, b t1 WHERE t0.id = t1.id GROUP BY field, t1.field", c.parse("a join(b) group(field, b.field)"));
    }

    @Test
    public void testAsc() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addRelation("a", "v_a", "b", "v_b");
        assertEquals("SELECT * FROM a t0 ORDER BY v1, v2 ASC", c.parse("a asc(v1, v2)"));
    }

    @Test
    public void testDesc() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addRelation("a", "v_a", "b", "v_b");
        assertEquals("SELECT * FROM a t0 ORDER BY v1, v2 DESC", c.parse("a desc(v1, v2)"));
    }

    @Test
    public void testSum() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addRelation("a", "v_a", "b", "v_b");
        assertEquals("SELECT SUM( 1 ) FROM a t0", c.parse("a sum(1)"));
        assertEquals("SELECT SUM( v ) FROM a t0", c.parse("a sum(v)"));
        assertEquals("SELECT v1, v2, SUM( 1 ) FROM a t0 GROUP BY v1, v2", c.parse("a sum(1, v1, v2)"));
    }

    @Test
    public void testMax() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addRelation("a", "v_a", "b", "v_b");
        assertEquals("SELECT MAX( 1 ) FROM a t0", c.parse("a max(1)"));
        assertEquals("SELECT MAX( v ) FROM a t0", c.parse("a max(v)"));
        assertEquals("SELECT v1, v2, MAX( 1 ) FROM a t0 GROUP BY v1, v2", c.parse("a max(1, v1, v2)"));
    }

    @Test
    public void testMin() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addRelation("a", "v_a", "b", "v_b");
        assertEquals("SELECT MIN( 1 ) FROM a t0", c.parse("a min(1)"));
        assertEquals("SELECT MIN( v ) FROM a t0", c.parse("a min(v)"));
        assertEquals("SELECT v1, v2, MIN( 1 ) FROM a t0 GROUP BY v1, v2", c.parse("a min(1, v1, v2)"));
    }

    @Test
    public void testDistinct() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addRelation("a", "v_a", "b", "v_b");
        assertEquals("SELECT DISTINCT va, 1, vb FROM a t0", c.parse("a distinct(va, 1, vb)"));
        assertEquals("SELECT DISTINCT t0.* FROM a t0", c.parse("a distinct(a)"));
        assertEquals("SELECT DISTINCT a, b FROM table t0", c.parse("table distinct(a, b)"));

        try {
            c.parse("a print(v1) distinct(v2)");
            fail();
        } catch(Exception e) {
            checkException(e, createError(FunctionalSQLCompiler.ERR_SELECT_ALREADY_DEFINED, "SELECT v1"));
        }
    }

    @Test
    public void testNotFilter() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        assertEquals("SELECT * FROM a t0 WHERE field != 1", c.parse("a notfilter(field, 1)"));
        assertEquals("SELECT * FROM a t0 WHERE field NOT IN ( 1, 2 )", c.parse("a notfilter(field, 1, 2)"));
        assertEquals("SELECT * FROM a t0 WHERE field != '1'", c.parse("a notfilter(field, '1')"));
        assertEquals("SELECT * FROM a t0 WHERE field NOT IN ( '1', '2' )", c.parse("a notfilter(field, '1', '2')"));
    }

    @Test
    public void testFilter() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addRelation("a", "va", "b", "vb");

        assertEquals("SELECT * FROM a t0 WHERE field = '1'", c.parse("a filter(field, '1')"));
        assertEquals("SELECT * FROM a t0 WHERE field = 1", c.parse("a filter(field, 1)"));
        assertEquals("SELECT * FROM a t0 WHERE v = 'a b'", c.parse("a filter(v, 'a b')"));
        assertEquals("SELECT * FROM a t0 WHERE v IN ( 'a', 'b' )", c.parse("a filter(v, 'a', 'b')"));
        assertEquals("SELECT * FROM a t0 WHERE v IS NULL", c.parse("a filter(v)"));


        try {
            c.parse("a filter(1, a)");
            fail();
        } catch(Exception e) {
            checkException(e, "Value (a) should be quoted.");
        }

        assertEquals("SELECT * FROM a t0 WHERE v != 1", c.parse("a filter(v, !=, 1)"));
        assertEquals("SELECT * FROM a t0 WHERE v == 1", c.parse("a filter(v, ==, 1)"));
        assertEquals("SELECT * FROM a t0 WHERE v < 1", c.parse("a filter(v, <, 1)"));
        assertEquals("SELECT * FROM a t0 WHERE v > 1", c.parse("a filter(v, >, 1)"));
        assertEquals("SELECT * FROM a t0 WHERE v <= 1", c.parse("a filter(v, <=, 1)"));
        assertEquals("SELECT * FROM a t0 WHERE v >= 1", c.parse("a filter(v, >=, 1)"));
        assertEquals("SELECT * FROM a t0 WHERE v = '<'", c.parse("a filter(v, '<')"));

        try {
            c.parse("a filter(v,!=)");
            fail();
        } catch(Exception e) {
            checkException(e, FunctionalSQLCompiler.ERR_NEED_VALUE_WHEN_USING_OPERATOR_IN_FILTER);
        }

        try {
            c.parse("a filter(v,!=, 'a', 'b')");
        } catch(Exception e) {
            checkException(e, "Only one value when using operator in filter function (['a', 'b']).");
        }
    }

    @Test
    public void testFilterDate() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        assertEquals("SELECT * FROM a t0 WHERE v = '20010101'", c.parse("a filterdate(v, 20010101)"));
        assertEquals("SELECT * FROM a t0 WHERE v >= '20010101'", c.parse("a filterdate(v, 20010101, >= )"));
        assertEquals("SELECT * FROM a t0 WHERE v <= '20010101'", c.parse("a filterdate(v, 20010101, <= )"));
        assertEquals("SELECT * FROM a t0 WHERE v > '20010101'", c.parse("a filterdate(v, 20010101, > )"));
        assertEquals("SELECT * FROM a t0 WHERE v < '20010101'", c.parse("a filterdate(v, 20010101, < )"));
        assertEquals("SELECT * FROM a t0 WHERE v < '20020101' AND v >= '20010101'", c.parse("a filterdate(v, 20010101, 20020101)"));
    }

    @Test
    public void testRefAndNewTable() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addRelation("a", "id", "a", "id");
        assertEquals("SELECT t1.* FROM a t0, a t1 WHERE t0.id = t1.id", c.parse("a join(newtable(a)) print(ref(a,2))"));

        try {
            c.parse("a join(newtable(a)) print(ref(a,3))");
            fail();
        } catch( Exception e ) {
            checkException(e, createError(FunctionalSQLCompiler.ERR_TABLE_REFERENCE_IS_NOT_CORRECT, "3"));
        }

        try {
            c.parse("a join(newtable(a)) print(ref(z,3))");
            fail();
        } catch( Exception e ) {
            checkException(e, createError(FunctionalSQLCompiler.ERR_REFERING_TO_A_NON_EXISTING_TABLE, "z"));
        }

        try {
            c.parse("a join(newtable(a)) print(ref(a,i))");
            fail();
        } catch( Exception e ) {
            checkException(e, createError(FunctionalSQLCompiler.ERR_TABLE_REFERENCE_SHOULD_BE_NUMMERICAL, "i"));
        }

        try {
            c.parse("a join(newtable(a)) print(ref(a,0))");
            fail();
        } catch( Exception e ) {
            checkException(e, createError(FunctionalSQLCompiler.ERR_TABLE_REFERENCE_SHOULD_BE_EQUAL_OR_GREATER_THEN_ONE, "0"));
        }
    }

    @Test
    public void testInnerJoin() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addRelation("a", "id", "b", "id");
        assertEquals("SELECT * FROM a t0 INNER JOIN b t1 ON t0.id = t1.id", c.parse("a innerjoin(b)"));
    }

    @Test
    public void testLeftJoin() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addRelation("a", "id", "b", "id");
        assertEquals("SELECT * FROM a t0 LEFT JOIN b t1 ON t0.id = t1.id", c.parse("a leftjoin(b)"));
    }

    @Test
    public void testRightJoin() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addRelation("a", "id", "b", "id");
        assertEquals("SELECT * FROM a t0 RIGHT JOIN b t1 ON t0.id = t1.id", c.parse("a rightjoin(b)"));
    }

    @Test
    public void testFullJoin() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addRelation("a", "id", "b", "id");
        c.addRelation("b", "id", "c", "id");
        c.addRelation("a", "id", "c", "id");
        assertEquals("SELECT * FROM a t0 FULL JOIN b t1 ON t0.id = t1.id", c.parse("a fulljoin(b)"));
        assertEquals("SELECT * FROM a t0 FULL JOIN b t1 ON t0.id = t1.id LEFT JOIN c t2 ON t1.id = t2.id",
                c.parse("a fulljoin(b, leftjoin(c))"));
        assertEquals("SELECT * FROM a t0 FULL JOIN b t1 ON t0.id = t1.id LEFT JOIN c t2 ON t0.id = t2.id",
                c.parse("a fulljoin(b) leftjoin(c)"));
    }

    @Test
    public void testAndOr() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        assertEquals("SELECT * FROM a t0 WHERE ( v = 10 OR v = 11 )", c.parse("a or(filter(v, 10), filter(v,11))"));
        assertEquals("SELECT * FROM a t0 WHERE ( v = 10 AND v = 11 )", c.parse("a and(filter(v, 10), filter(v,11))"));
        assertEquals("SELECT * FROM a t0 WHERE ( ( v = 1 AND v = 2 ) OR v = 3 )", c.parse("a or(and(filter(v,1), filter(v,2)), filter(v,3))"));
        assertEquals("SELECT * FROM a t0 WHERE ( ( v != 1 AND v = 2 ) OR v = 3 )", c.parse("a or(and(notfilter(v,1), filter(v,2)), filter(v,3))"));
        assertEquals("SELECT * FROM a t0 WHERE ( ( v = '20020101' AND v = 2 ) OR v = 3 )", c.parse("a or(and(filterdate(v,20020101), filter(v,2)), filter(v,3))"));

        try {
            c.parse("a or()");
            fail();
        } catch(Exception e) {
            checkException(e,createError(FunctionalSQLCompiler.ERR_UNEXPECTED_END_OF_FUNCTION));
        }
    }

    @Test
    public void testRenameFunction() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.renameFunction("fulljoin", "fjoin");
        c.addRelation("a", "id", "b", "id");
        assertEquals("SELECT * FROM a t0 FULL JOIN b t1 ON t0.id = t1.id", c.parse("a fjoin(b)"));

        try {
            c.parse("a fulljoin(b)");
            fail();
        } catch(Exception e) {
            checkException(e, createError(FunctionalSQLCompiler.ERR_UNKNOWN_FUNCTION, "fulljoin"));
        }
    }

    private String createError(String format, Object... args) {
        return String.format(format, args);
    }

    private void checkException(Throwable e , String message) {
        if( e.getMessage().indexOf(message) < 0 ) {
            fail();
        }
    }
}
