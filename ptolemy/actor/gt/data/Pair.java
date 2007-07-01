package ptolemy.actor.gt.data;

public class Pair<E1, E2> extends Tuple<Object> {

    public Pair(E1 first, E2 second) {
        super(first, second);
    }

    @SuppressWarnings("unchecked")
    public E1 getFirst() {
        return (E1) get(0);
    }

    @SuppressWarnings("unchecked")
    public E2 getSecond() {
        return (E2) get(1);
    }

    public void set(E1 first, E2 second) {
        set(0, first);
        set(1, second);
    }

    public void setFirst(E1 first) {
        set(0, first);
    }

    public void setSecond(E2 second) {
        set(1, second);
    }

    private static final long serialVersionUID = -2700656323692235563L;
}
