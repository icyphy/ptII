package ptolemy.caltrop.util;

import caltrop.interpreter.Context;

import java.util.AbstractList;


/**
 *  @author Jörn W. Janneck <janneck@eecs.berkeley.edu>
 */

public class IntegerList extends AbstractList {

    public IntegerList(Context context, int a, int b) {
        assert a <= b;

        this.context = context;
        this.a = a;
        this.b = b;
    }

    public Object get(int n) {
        if (a + n > b)
            throw new IndexOutOfBoundsException();
        return context.createInteger(a + n);
    }

    public int  size() {
        return (b - a) + 1;
    }

    private Context context;
    private int  a;
    private int  b;
}