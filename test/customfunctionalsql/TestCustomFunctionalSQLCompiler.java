package customfunctionalsql;

import functionalsql.FunctionalSQLCompiler;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestCustomFunctionalSQLCompiler {

    @Test
    public void testId() throws Exception {
        CustomFunctionalSQLCompiler c = new CustomFunctionalSQLCompiler();
        c.addRelation("a", "id", "b", "id");
        assertEquals("SELECT * FROM a t0 WHERE id = 10", c.parse("a id(10)"));
        assertEquals("SELECT * FROM a t0, b t1 WHERE t0.id = t1.id AND t1.id = 10", c.parse("a join(b) id(b, 10)"));

        try {
            c.parse("a id(b)");
            fail();
        } catch(Exception e ) {
            checkException(e, createError(CustomFunctionalSQLCompiler.ERR_ARGUMENT_SHOULD_BE_NUMMERICAL, "b"));
        }

        try {
            c.parse("a id(table, 10)");
            fail();
        } catch(Exception e ) {
            checkException(e, createError(FunctionalSQLCompiler.ERR_REFERING_TO_A_NON_EXISTING_TABLE, "table"));
        }

        try {
            c.parse("'a' id(table, 10)");
            fail();
        } catch(Exception e) {
            checkException(e, createError(FunctionalSQLCompiler.ERR_WRONG_FORMAT_TABLE_OR_COLUMN_NAME, "'a'"));
        }
    }

    private void checkException(Throwable e , String message) {
        if( e.getMessage().indexOf(message) < 0 ) {
            fail();
        }
    }

    private String createError(String format, Object... args) {
        return String.format(format, args);
    }
}
