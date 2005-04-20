package ptolemy.backtrack.util.java.util;

import java.util.EmptyStackException;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.FieldRecord;

public class Stack extends Vector implements Rollbackable {

    private static final long serialVersionUID = 1224463164541339165L;

    public Stack() {
    }

    public Object push(Object item) {
        addElement(item);
        return item;
    }

    public synchronized Object pop() {
        if (getElementCount() == 0)
            throw new EmptyStackException();
        setModCount(getModCount() + 1);
        setElementCount(getElementCount() - 1);
        Object obj = getElementData()[getElementCount()];
        getElementData()[getElementCount()] = null;
        return obj;
    }

    public synchronized Object peek() {
        if (getElementCount() == 0)
            throw new EmptyStackException();
        return getElementData()[getElementCount() - 1];
    }

    public synchronized boolean empty() {
        return getElementCount() == 0;
    }

    public synchronized int search(Object o) {
        int i = getElementCount();
        while (--i >= 0) 
            if (equals(o, getElementData()[i]))
                return getElementCount() - i;
        return -1;
    }

    public void $RESTORE(long timestamp, boolean trim) {
        super.$RESTORE(timestamp, trim);
    }

    private FieldRecord[] $RECORDS = new FieldRecord[] {
        };
}
