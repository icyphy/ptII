package ptolemy.caltrop.util;

import ptolemy.data.ArrayToken;

import java.util.AbstractList;
import java.util.List;

/**
 *  @author Jörn W. Janneck <janneck@eecs.berkeley.edu>
 */

public class PtArrayList extends AbstractList implements List {
    private ArrayToken _arrayToken;

    public PtArrayList(ArrayToken arrayToken) {
        _arrayToken = arrayToken;
    }

    public Object get(int index) {
        return _arrayToken.getElement(index);
    }

    public int size() {
        return _arrayToken.length();
    }

}

