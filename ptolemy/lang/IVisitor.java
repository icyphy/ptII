package ptolemy.lang;

public interface IVisitor {

    int traversalMethod();
    
    public static final int TM_CHILDREN_FIRST = 0;
    public static final int TM_SELF_FIRST = 1;
    public static final int TM_CUSTOM = 2;
}
