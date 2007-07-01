package ptolemy.actor.gt.data;

import java.util.Collection;
import java.util.LinkedList;

public class Tuple<E> extends LinkedList<E> {

    public Tuple(Collection<E> collection) {
        super(collection);
    }

    public Tuple(E ... elements) {
        for (E element : elements) {
            add(element);
        }
    }

    private static final long serialVersionUID = -8376318257178238209L;

}
