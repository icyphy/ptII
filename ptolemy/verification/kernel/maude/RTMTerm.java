package ptolemy.verification.kernel.maude;

abstract public class RTMTerm {

    protected int indentWidth = 4;

    public static String transId(String name) {
        return "'" + name;
    }

    abstract public String print(int indent, boolean newline);

    protected String front(int indent) {
        StringBuffer a = new StringBuffer("");
        for (int i = 0; i < indent; i++ ) a.append(" ");
        return a.toString();
    }
}
