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
    public void testDefaultMappingHasNoEqualColumns() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_DEFAULT_MAPPING_HAS_NO_EQUAL_COLUMNS));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addCustomMapping("", "id1", "", "id2");
    }

    @Test
    public void testFunctionHasNoArguments() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_FUNCTION_HAS_NO_ARGUMENTS));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a print()");
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
        c.addCustomMapping("a", "id", "b", "id");
        c.addCustomMapping("b", "id", "c", "id");
        c.parse("a join b");
    }

    @Test
    public void testJoinShouldFollowJoin() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_JOIN_SHOULD_FOLLOW_JOIN, "id"));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addCustomMapping("a", "id", "b", "id");
        c.addCustomMapping("b", "id", "c", "id");
        c.parse("a join(b, join(c), id, id)");
    }

    @Test
    public void testIfTableHasMultipleInstancesUseRefFunction() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(createError(FunctionalSQLCompiler.ERR_IF_TABLE_HAS_MULTIPLE_INSTANCES_USE_REF_FUNCTION, "a"));
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addCustomMapping("a", "id", "a", "id");
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
    public void testSelect() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
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
    public void testQuery() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();

        c.addCustomMapping("a", "id", "b", "id");
        c.addCustomMapping("b", "id", "c", "id");
        c.addCustomMapping("a", "id", "c", "id");

        assertEquals("SELECT t0.field, t1.field, t3.field2, MAX( t3.field ) FROM a t0, b t1, c t2, c t3 WHERE t0.id = t1.id AND t0.id = t3.id AND t1.id = t2.id GROUP BY t0.field, t1.field, t3.field2",
             c.parse("(a) join(b, join(c)) join(newtable(c)) max(ref(c.field, 2), a.field, b.field, ref(c.field2, 2))"));
    }

    @Test
    public void testNestedQuery() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addCustomMapping("a", "id", "b", "id");
        c.addCustomMapping("b", "id", "c", "id");

        assertEquals( "SELECT * FROM a t0, (SELECT * FROM b t0, c t1 WHERE t0.id = t1.id) t1 WHERE t0.id = t1.id",
                c.parse("a join((b join(c)), id, id )") );
    }

    @Test
    public void testJoin() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();

        assertEquals( "SELECT * FROM a t0, b t1 WHERE t0.v_a = t1.v_b", c.parse( "a join(b, v_a, v_b ) ") );

        try {
            c.parse("a join(b) ");
            fail();
        } catch( Exception e ) {
            checkException(e, FunctionalSQLCompiler.ERR_NO_JOIN_COLUMNS_DEFINED_AND_NO_CUSTOM_MAPPING_PRESENT);
        }

        c.addCustomMapping("a", "v_a", "b", "v_b");
        assertEquals("SELECT * FROM a t0, b t1 WHERE t0.v_a = t1.v_b", c.parse( "a join(b) ") );
        assertEquals("SELECT * FROM a t0, b t1, c t2 WHERE t0.v_a = t1.v_b AND t0.v_a = t2.v_c", c.parse("a join(b) join(c, v_a, v_c)"));

        c.addCustomMapping("a", "v_a", "c", "v_c");
        assertEquals("SELECT * FROM a t0, b t1, c t2 WHERE t0.v_a = t1.v_b AND t0.v_a = t2.v_c", c.parse("a join(b) join(c)"));

        c.addCustomMapping("b", "v_b", "c", "v_c");
        assertEquals("SELECT * FROM a t0, b t1, c t2 WHERE t0.v_a = t1.v_b AND t1.v_b = t2.v_c", c.parse("a join(b, join(c))"));

        try {
            c.parse("a join(b, like(c))");
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

        c.addCustomMapping("a", "v_a", "b", "v_b");

        assertEquals( "SELECT t1.v FROM a t0, b t1 WHERE t0.v_a = t1.v_b", c.parse( "a join(b) print( b.v ) "));
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
        c.addCustomMapping("a", "id", "b", "id");
        assertEquals("SELECT field, t1.field FROM a t0, b t1 WHERE t0.id = t1.id GROUP BY field, t1.field", c.parse("a join(b) group(field, b.field)"));
    }

    @Test
    public void testAsc() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addCustomMapping("a", "v_a", "b", "v_b");
        assertEquals("SELECT * FROM a t0 ORDER BY v1, v2 ASC", c.parse("a asc(v1, v2)"));
    }

    @Test
    public void testDesc() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addCustomMapping("a", "v_a", "b", "v_b");
        assertEquals("SELECT * FROM a t0 ORDER BY v1, v2 DESC", c.parse("a desc(v1, v2)"));
    }

    @Test
    public void testSum() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addCustomMapping("a", "v_a", "b", "v_b");
        assertEquals("SELECT SUM( 1 ) FROM a t0", c.parse("a sum(1)"));
        assertEquals("SELECT SUM( v ) FROM a t0", c.parse("a sum(v)"));
        assertEquals("SELECT v1, v2, SUM( 1 ) FROM a t0 GROUP BY v1, v2", c.parse("a sum(1, v1, v2)"));
    }

    @Test
    public void testMax() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addCustomMapping("a", "v_a", "b", "v_b");
        assertEquals("SELECT MAX( 1 ) FROM a t0", c.parse("a max(1)"));
        assertEquals("SELECT MAX( v ) FROM a t0", c.parse("a max(v)"));
        assertEquals("SELECT v1, v2, MAX( 1 ) FROM a t0 GROUP BY v1, v2", c.parse("a max(1, v1, v2)"));
    }

    @Test
    public void testMin() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addCustomMapping("a", "v_a", "b", "v_b");
        assertEquals("SELECT MIN( 1 ) FROM a t0", c.parse("a min(1)"));
        assertEquals("SELECT MIN( v ) FROM a t0", c.parse("a min(v)"));
        assertEquals("SELECT v1, v2, MIN( 1 ) FROM a t0 GROUP BY v1, v2", c.parse("a min(1, v1, v2)"));
    }

    @Test
    public void testDistinct() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addCustomMapping("a", "v_a", "b", "v_b");
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
        c.addCustomMapping("a", "va", "b", "vb");

        assertEquals("SELECT * FROM a t0 WHERE field = '1'", c.parse("a filter(field, '1')"));
        assertEquals("SELECT * FROM a t0 WHERE field = 1", c.parse("a filter(field, 1)"));
        assertEquals("SELECT * FROM a t0 WHERE v = 'a b'", c.parse("a filter(v, 'a b')"));
        assertEquals("SELECT * FROM a t0 WHERE v IN ( 'a', 'b' )", c.parse("a filter(v, 'a', 'b')"));
        assertEquals("SELECT * FROM a t0, b t1 WHERE t0.va = t1.vb AND v IN ( 'a', 'b' )", c.parse("a join(b) filter(v, 'a', 'b')"));
        assertEquals("SELECT * FROM a t0, b t1 WHERE t0.va = t1.vb AND t1.v IN ( 'a', 'b' )", c.parse("a join(b) filter(b.v, 'a', 'b')"));

        try {
            c.parse("a filter(1)");
            fail();
        } catch(Exception e) {
            checkException(e, FunctionalSQLCompiler.ERR_UNEXPECTED_END_OF_FUNCTION);
        }

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
        c.addCustomMapping("a", "id", "a", "id");
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
        c.addCustomMapping("a", "id", "b", "id");
        assertEquals("SELECT * FROM a t0 INNER JOIN b t1 ON t0.id = t1.id", c.parse("a innerjoin(b)"));
    }

    @Test
    public void testLeftJoin() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addCustomMapping("a", "id", "b", "id");
        assertEquals("SELECT * FROM a t0 LEFT JOIN b t1 ON t0.id = t1.id", c.parse("a leftjoin(b)"));
    }

    @Test
    public void testRightJoin() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addCustomMapping("a", "id", "b", "id");
        assertEquals("SELECT * FROM a t0 RIGHT JOIN b t1 ON t0.id = t1.id", c.parse("a rightjoin(b)"));
    }

    @Test
    public void testFullJoin() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addCustomMapping("a", "id", "b", "id");
        c.addCustomMapping("b", "id", "c", "id");
        c.addCustomMapping("a", "id", "c", "id");
        assertEquals("SELECT * FROM a t0 FULL JOIN b t1 ON t0.id = t1.id", c.parse("a fulljoin(b)"));
        assertEquals("SELECT * FROM a t0 FULL JOIN b t1 ON t0.id = t1.id LEFT JOIN c t2 ON t1.id = t2.id",
                c.parse("a fulljoin(b, leftjoin(c))"));
        assertEquals("SELECT * FROM a t0 FULL JOIN b t1 ON t0.id = t1.id LEFT JOIN c t2 ON t0.id = t2.id",
                c.parse("a fulljoin(b) leftjoin(c)"));
    }

    @Test
    public void testRenameFunction() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.renameFunction("fulljoin", "fjoin");
        c.addCustomMapping("a", "id", "b", "id");
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
