package ptolemy.backtrack.ast;

public class TestProgram {
    
    private int i;
    void f() {
        i = 0;
    }
    
    /*private TestProgram o;
    private Object o2;
    
    TestProgram f() {
        o.f().o = (TestProgram)new Object();
        o.f().o2 = new Object();
        return null;
    }*/
    
    /*private char[][] buf;
    void f() {
        buf = new char[1][];
        buf[1] = new char[2];
        buf[1+1][2<<2] = 'B';
    }*/
    
    /*void f() {
        new Object() {
            private char[][] buf;
            void g() {
                buf[1] = new char[2];
                buf[1+1][2<<2] = 'B';
            }
        };
    }*/
    
    /*void f() {
        char[][] buf = null;
        buf[1] = new char[2];
        buf[1+1][2<<2] = 'B';
    }*/
    
    //------------------
    //     Problems
    //------------------
    
    /*private char[][] buf;
    void g() {
        char[][] buf = this.buf;
        buf[1] = new char[2];
        buf[1+1][2<<2] = 'B';
    }*/

    /*private static int i;
    void f() {
        i = 0;
    }*/
}
