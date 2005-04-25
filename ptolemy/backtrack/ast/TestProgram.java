package ptolemy.backtrack.ast;

class TestProgram /*implements Rollbackable*/ {

    private int[][] buf;

    int[][] getBuf() {
        return buf;
    }

    TestProgram getThis(int[][] x) {
        return this;
    }

    void f() {
        int[][] b = getThis(buf).buf;
    }

    public static void main(String[] args) {
        int length1 = 10;
        int length2 = 20;
        do
            length1--;
        while (length1 > 0);

        TestProgram t = new TestProgram();
        t.buf = new int[length1][length2];
        for (int i = 0, k = 0; i < length1; i++)
            for (int j = 0; j < length2; j++, k++)
                t.buf[i][j] = k;

        int[][] buf = new int[length1][length2];
        for (int i = 0, k = 0; i < length1; i++)
            for (int j = 0; j < length2; j++, k++)
                buf[i][j] = length1 * length2 - k - 1;

        System.arraycopy(buf, 0, t.buf, 0, length1);

        for (int i = 0; i < length1; i++) {
            for (int j = 0; j < length2; j++)
                System.out.print(formatInteger(t.buf[i][j], 4));
            System.out.println();
        }
    }

    private static String formatInteger(int i, int length) {
        StringBuffer buf = new StringBuffer();
        buf.append(Integer.toString(i));
        int len = buf.length();
        while (len++ < length)
            buf.insert(0, ' ');
        return buf.toString();
    }

    /*private int i;
    void f() {
        i = 0;
        i = 1;
        i = 2;
    }*/

    /*private Object o = new C();

    class C {
    }*/

    /*private TestProgram o = new TestProgram();
    private Object o2;

    TestProgram f() {
        o.f().o = new TestProgram();
        o.f().o2 = new Object();
        return null;
    }*/

    /*private char[][][][] buf;
    void f() {
        buf = new char[1][][][];
        buf[0] = new char[2][][];
        buf[0][1] = new char[3][];
        buf[0][1][2] = new char[4];
        buf[0][1][2][3] = 'C';
    }*/

    /*private TestProgram[] t = new TestProgram[10];

    private int i;

    void f() {
        t[1] = new TestProgram();
        t[1].i = 10;
        t[1].i = 20;
        t = new TestProgram[1];
        t[0] = new TestProgram();
        t[0].i = 30;
        t[0] = null;
    }*/

    /*public static void main(String[] args) {
        TestProgram p = new TestProgram();
        p.f();
        p.$CHECKPOINT.rollback(3, true);
        System.out.println(p.t[1].i);
    }

    protected ptolemy.backtrack.Checkpoint $CHECKPOINT = new ptolemy.backtrack.Checkpoint(this);

    private TestProgram[] t = new TestProgram[10];

    private int i;

    void f() {
        $CHECKPOINT.createCheckpoint(); // #1
        $ASSIGN$t(1, new TestProgram());
        $CHECKPOINT.createCheckpoint(); // #2
        t[1].$ASSIGN$i(10);
        $CHECKPOINT.createCheckpoint(); // #3
        t[1].$ASSIGN$i(20);
        $CHECKPOINT.createCheckpoint(); // #4
        $ASSIGN$t(new TestProgram[1]);
        $CHECKPOINT.createCheckpoint(); // #5
        $ASSIGN$t(0, new TestProgram());
        $CHECKPOINT.createCheckpoint(); // #6
        t[0].$ASSIGN$i(30);
        $CHECKPOINT.createCheckpoint(); // #7
        $ASSIGN$t(0, null);
        $CHECKPOINT.createCheckpoint(); // #8
    }

    private final ptolemy.backtrack.ast.TestProgram $ASSIGN$t(int index0, ptolemy.backtrack.ast.TestProgram newValue) {
        if ($CHECKPOINT != null) {
            $RECORD$t.add(new int[] {
                    index0
                }, t[index0], $CHECKPOINT.getTimestamp());
        }
        if (newValue != null && $CHECKPOINT != newValue.$CHECKPOINT) {
            newValue.$SET$CHECKPOINT($CHECKPOINT);
        }
        return t[index0] = newValue;
    }

    private final ptolemy.backtrack.ast.TestProgram[] $ASSIGN$t(ptolemy.backtrack.ast.TestProgram[] newValue) {
        if ($CHECKPOINT != null) {
            $RECORD$t.add(null, t, $CHECKPOINT.getTimestamp());
        }
        return t = newValue;
    }

    private final int $ASSIGN$i(int newValue) {
        if ($CHECKPOINT != null) {
            $RECORD$i.add(null, i, $CHECKPOINT.getTimestamp());
        }
        return i = newValue;
    }

    public void $RESTORE(long timestamp, boolean trim) {
        t = (ptolemy.backtrack.ast.TestProgram[])$RECORD$t.restore(t, timestamp, trim);
        i = $RECORD$i.restore(i, timestamp, trim);
    }

    public final ptolemy.backtrack.Checkpoint $GET$CHECKPOINT() {
        return $CHECKPOINT;
    }

    public final void $SET$CHECKPOINT(ptolemy.backtrack.Checkpoint checkpoint) {
        if ($CHECKPOINT != checkpoint) {
            ptolemy.backtrack.Checkpoint oldCheckpoint = $CHECKPOINT;
            $CHECKPOINT = checkpoint;
            oldCheckpoint.setCheckpoint(checkpoint);
            checkpoint.addObject(this);
        }
    }

    private ptolemy.backtrack.util.FieldRecord $RECORD$t = new ptolemy.backtrack.util.FieldRecord(1);

    private ptolemy.backtrack.util.FieldRecord $RECORD$i = new ptolemy.backtrack.util.FieldRecord(0);

    public TestProgram() {
    }

    public TestProgram(ptolemy.backtrack.Checkpoint $CHECKPOINT) {
        this();
        $SET$CHECKPOINT($CHECKPOINT);
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
