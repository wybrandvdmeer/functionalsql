package functionalsql;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestCustomFunctionalSQLCompiler {

    @Test
    public void testId() throws Exception {
        CustomFunctionalSQLCompiler c = new CustomFunctionalSQLCompiler();
        c.addCustomMapping("a", "id", "b", "id");
        assertEquals("SELECT * FROM a t0 WHERE id = 10", c.parse("a id(10)"));
        assertEquals("SELECT * FROM a t0, b t1 WHERE t0.id = t1.id AND t1.id = 10", c.parse("a join(b) id(b, 10)"));
    }
}
