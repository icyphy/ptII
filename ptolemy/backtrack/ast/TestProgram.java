package ptolemy.backtrack.ast;

class TestProgram {
    
    /*public static void main(String[] args) {
        java.util.ArrayList list = new java.util.ArrayList();
        list.setSize(10);
    }*/
    
    /*private int i;
    void f() {
        i = 0;
    }*/
    
    /*private TestProgram o;
    private Object o2;
    
    TestProgram f() {
        o.f().o = (TestProgram)new Object();
        o.f().o2 = new Object();
        return null;
    }*/
    
    private char[][][][] buf;
    void f() {
        buf = new char[1][2][3][4];
        buf[0] = new char[2][][];
        buf[0][1] = new char[3][];
        buf[0][1][2] = new char[4];
        buf[0][1][2][3] = 'C';
    }
    
    /*public static void main(String[] args) {
        new TestProgram().f();
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
