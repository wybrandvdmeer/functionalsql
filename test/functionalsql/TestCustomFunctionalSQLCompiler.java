package functionalsql;

import customfunctionalsql.CustomFunctionalSQLCompiler;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestCustomFunctionalSQLCompiler {

    @Test
    public void testId() throws Exception {
        CustomFunctionalSQLCompiler c = new CustomFunctionalSQLCompiler();
        c.addCustomMapping("a", "id", "b", "id");
        assertEquals("SELECT * FROM a t0 WHERE id = 10", c.parse("a id(10)"));
        assertEquals("SELECT * FROM a t0, b t1 WHERE t0.id = t1.id AND t1.id = 10", c.parse("a join(b) id(b, 10)"));

        try {
            c.parse("a id(b)");
            fail();
        } catch(Exception e ) {
            checkException(e, "Argument (b) should be nummerical.");
        }

        try {
            c.parse("a id(table, 10)");
            fail();
        } catch(Exception e ) {
            checkException(e, "Refering to a non existing table (table).");
        }
    }

    private void checkException(Throwable e , String message) {
        if( e.getMessage().indexOf(message) < 0 ) {
            fail();
        }
    }
}
