package ptolemy.backtrack.ast.test;

public class ASTTest {
    public ASTTest() {
        foo = 1;
    }
    public int incrementFoo() {
        foo += 1;
        return foo;
    }
    private int foo;
}
