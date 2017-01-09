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
    public void testMissingEndQuote() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(FunctionalSQLCompiler.ERR_MISSING_END_QUOTE);

        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.parse("a filter(field, 'value");
    }

    @Test
    public void testSelect() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        assertEquals( "SELECT * FROM a t0" , c.parse( "a" ) );
        assertEquals( "SELECT * FROM a t0" , c.parse( "(a)" ) );
        assertEquals( "SELECT * FROM a t0" , c.parse( "((a))" ) );
        assertEquals( "SELECT * FROM a t0" , c.parse( "(((a)))" ) );
        assertEquals("SELECT * FROM a t0 WHERE field = 2", c.parse("(a) filter(field, 2)"));
        assertEquals("SELECT * FROM a t0 WHERE field = 2", c.parse("((a) filter(field, 2))"));
        assertEquals(c.parse("(((((a) filter(field, 2)))))"), c.parse("((((a))) filter(field, 2))"));
    }

    @Test
    public void testQuery() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();

        c.addCustomMapping("a", "id", "b", "id");
        c.addCustomMapping("b", "id", "c", "id");
        c.addCustomMapping("a", "id", "c", "id");

        assertEquals("SELECT t0.field, t1.field, t3.field2, MAX( t3.field ) FROM a t0, b t1, c t2, c t3 WHERE t0.id = t1.id AND t0.id = t3.id AND t1.id = t2.id GROUP BY t0.field, t1.field, t3.field2",
             c.parse("a join(b, join(c)) join(newtable(c)) max(ref(c.field, 2), a.field, b.field, ref(c.field2, 2))"));
    }

    @Test
    public void testNestedQuery() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();

        c.addCustomMapping("b", "id", "c", "id");

        assertEquals( "SELECT * FROM a t0, (SELECT * FROM b t0, c t1 WHERE t0.id = t1.id) t1 WHERE t0.id = t1.id",
                c.parse("a join( (b join(c)), id, id )") );
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
            checkException(e, "Value (a) should be quoted.");
        }
    }

    @Test
    public void testGroup() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addCustomMapping("a", "v_a", "b", "v_b");
        assertEquals("SELECT field1, t1.field1 FROM a t0, b t1 WHERE t0.v_a = t1.v_b GROUP BY field1, t1.field1", c.parse("a join(b) group( field1, b.field1)"));
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
            checkException(e, FunctionalSQLCompiler.UNEXPECTED_END_OF_FUNCTION);
        }

        try {
            c.parse("a filter(1, a)");
            fail();
        } catch(Exception e) {
            checkException(e, "Value (a) should be quoted.");
        }
    }

    @Test
    public void testFilterDate() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        assertEquals("SELECT * FROM a t0 WHERE v = convert( datetime , '20010101' )", c.parse("a filterdate(v, 20010101)"));
        assertEquals("SELECT * FROM a t0 WHERE v >= convert( datetime , '20010101' )", c.parse("a filterdate(v, 20010101, >= )"));
        assertEquals("SELECT * FROM a t0 WHERE v <= convert( datetime , '20010101' )", c.parse("a filterdate(v, 20010101, <= )"));
        assertEquals("SELECT * FROM a t0 WHERE v > convert( datetime , '20010101' )", c.parse("a filterdate(v, 20010101, > )"));
        assertEquals("SELECT * FROM a t0 WHERE v < convert( datetime , '20010101' )", c.parse("a filterdate(v, 20010101, < )"));

        try {
            c.parse("a filterdate(v, 20010101, 1 )");
            fail();
        } catch(Exception e) {
            checkException(e, "Unknown operator");
        }

    }

    @Test
    public void testRefAndNewTable() throws Exception {
        FunctionalSQLCompiler c = new FunctionalSQLCompiler();
        c.addCustomMapping("a", "id", "a", "id");
        assertEquals("SELECT t1.* FROM a t0, a t1 WHERE t0.id = t1.id", c.parse("a join(newtable(a)) print( ref(a,2))"));

        try {
            c.parse("a join(newtable(a)) print(ref(a,3))");
            fail();
        } catch( Exception e ) {
            checkException(e, "Table reference (3) is not correct");
        }

        try {
            c.parse("a join(newtable(a)) print(ref(z,3))");
            fail();
        } catch( Exception e ) {
            checkException(e, "Refering to a non existing table (z)");
        }

        try {
            c.parse("a join(newtable(a)) print(ref(a,i))");
            fail();
        } catch( Exception e ) {
            checkException(e, "Table reference should be nummerical (i)");
        }

        try {
            c.parse("a join(newtable(a)) print(ref(a,0))");
            fail();
        } catch( Exception e ) {
            checkException(e, "Reference should be equal or greater than one (0)");
        }
    }

    private void checkException(Throwable e , String message) {
        if( e.getMessage().indexOf(message) < 0 ) {
            fail();
        }
    }
}
