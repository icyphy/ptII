package ptolemy.backtrack.test.array1;

import ptolemy.backtrack.ast.TestProgram;

public class ArrayTest1 {
    
    public ArrayTest1() {
        init();
    }
    
    public void dump() {
        for (int i = 0; i < _length1; i++) {
            for (int j = 0; j < _length2; j++)
                System.out.print(_formatInteger(_buf[i][j], 4));
            System.out.println();
        }
    }
    
    public void init() {
        _buf = new int[_length1][_length2];
        for (int i = 0, k = 0; i < _length1; i++)
            for (int j = 0; j < _length2; j++, k++)
                _buf[i][j] = k;
    }
    
    public static void main(String[] args) {
        TestProgram t = new TestProgram();
        t.modify();
        t.dump();
    }
    
    public void modify() {
        int[][] newBuf = new int[_length1][_length2];
        for (int i = 0, k = 0; i < _length1; i++)
            for (int j = 0; j < _length2; j++, k++)
                newBuf[i][j] = _length1 * _length2 - k - 1;
        
        System.arraycopy(newBuf, 0, _buf, 0, _length1);
    }
    
    private String _formatInteger(int i, int length) {
        StringBuffer buf = new StringBuffer();
        buf.append(Integer.toString(i));
        int len = buf.length();
        while (len++ < length)
            buf.insert(0, ' ');
        return buf.toString();
    }
    
    private int[][] _buf;
    
    private final int _length1 = 10;
    private final int _length2 = 20;
}
